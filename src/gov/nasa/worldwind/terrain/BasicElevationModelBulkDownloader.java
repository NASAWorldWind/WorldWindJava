/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Downloads elevation data not currently available in the WorldWind file cache or a specified {@link FileStore}. The
 * class derives from {@link Thread} and is meant to operate in its own thread.
 * <p>
 * The sector and resolution associated with the downloader are specified during construction and are final.
 *
 * @author tag
 * @version $Id: BasicElevationModelBulkDownloader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicElevationModelBulkDownloader extends BulkRetrievalThread
{
    protected final static int MAX_TILE_COUNT_PER_REGION = 200;
    protected final static long DEFAULT_AVERAGE_FILE_SIZE = 45000L;

    protected final BasicElevationModel elevationModel;
    protected final int level;

    protected ArrayList<Tile> missingTiles;

    /**
     * Constructs a downloader to retrieve elevations not currently available in the WorldWind file cache.
     * <p>
     * The thread returned is not started during construction, the caller must start the thread.
     *
     * @param elevationModel the elevation model for which to download elevations.
     * @param sector         the sector to download data for. This value is final.
     * @param resolution     the target resolution, provided in radians of latitude per texel. This value is final.
     * @param listener       an optional retrieval listener. May be null.
     *
     * @throws IllegalArgumentException if either the elevation model or sector are null, or the resolution is less than
     *                                  zero.
     */
    public BasicElevationModelBulkDownloader(BasicElevationModel elevationModel, Sector sector, double resolution,
        BulkRetrievalListener listener)
    {
        // Arguments checked in parent constructor
        super(elevationModel, sector, resolution, elevationModel.getDataFileStore(), listener);

        this.elevationModel = elevationModel;
        this.level = computeLevelForResolution(sector, resolution);
    }

    /**
     * Constructs a downloader to retrieve elevations not currently available in a specified file store.
     * <p>
     * The thread returned is not started during construction, the caller must start the thread.
     *
     * @param elevationModel the elevation model for which to download elevations.
     * @param sector         the sector to download data for. This value is final.
     * @param resolution     the target resolution, provided in radians of latitude per texel. This value is final.
     * @param fileStore      the file store in which to place the downloaded elevations.
     * @param listener       an optional retrieval listener. May be null.
     *
     * @throws IllegalArgumentException if either the elevation model, the sector or file store are null, or the
     *                                  resolution is less than zero.
     */
    public BasicElevationModelBulkDownloader(BasicElevationModel elevationModel, Sector sector, double resolution,
        FileStore fileStore, BulkRetrievalListener listener)
    {
        // Arguments checked in parent constructor
        super(elevationModel, sector, resolution, fileStore, listener);

        this.elevationModel = elevationModel;
        this.level = computeLevelForResolution(sector, resolution);
    }

    public void run()
    {
        try
        {
            // Init progress with missing tiles count estimate
            this.progress.setTotalCount(this.estimateMissingTilesCount(20));
            this.progress.setTotalSize(this.progress.getTotalCount() * estimateAverageTileSize());

            // Determine and request missing tiles by level/region
            for (int levelNumber = 0; levelNumber <= this.level; levelNumber++)
            {
                if (elevationModel.getLevels().isLevelEmpty(levelNumber))
                    continue;

                int div = this.computeRegionDivisions(this.sector, levelNumber, MAX_TILE_COUNT_PER_REGION);
                Iterator<Sector> regionsIterator = this.getRegionIterator(this.sector, div);

                Sector region;
                while (regionsIterator.hasNext())
                {
                    region = regionsIterator.next();
                    // Determine missing tiles
                    this.missingTiles = getMissingTilesInSector(region, levelNumber);

                    // Submit missing tiles requests at intervals
                    while (this.missingTiles.size() > 0)
                    {
                        submitMissingTilesRequests();
                        if (this.missingTiles.size() > 0)
                            Thread.sleep(RETRIEVAL_SERVICE_POLL_DELAY);
                    }
                }
            }
            // Set progress to 100%
            this.progress.setTotalCount(this.progress.getCurrentCount());
            this.progress.setTotalSize(this.progress.getCurrentSize());
        }
        catch (InterruptedException e)
        {
            String message = Logging.getMessage("generic.BulkRetrievalInterrupted", elevationModel.getName());
            Logging.logger().log(java.util.logging.Level.WARNING, message, e);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionDuringBulkRetrieval", elevationModel.getName());
            Logging.logger().severe(message);
            throw new RuntimeException(message);
        }
    }

//    protected int countMissingTiles() throws InterruptedException
//    {
//        int count = 0;
//        for (int levelNumber = 0; levelNumber <= this.level; levelNumber++)
//        {
//            if (this.elevationModel.getLevels().isLevelEmpty(levelNumber))
//                continue;
//
//            count += getMissingTilesInSector(this.sector, levelNumber).size();
//        }
//
//        return count;
//    }

    protected synchronized void submitMissingTilesRequests() throws InterruptedException
    {
        RetrievalService rs = WorldWind.getRetrievalService();
        int i = 0;
        while (this.missingTiles.size() > i && rs.isAvailable())
        {
            Thread.sleep(1); // generates InterruptedException if thread has been interrupted

            Tile tile = this.missingTiles.get(i);

            if (this.elevationModel.getLevels().isResourceAbsent(tile))
            {
                removeAbsentTile(tile);  // tile is absent, count it off.
                continue;
            }

            URL url = this.fileStore.findFile(tile.getPath(), false);
            if (url != null)
            {
                // tile has been retrieved and is local now, count it as retrieved.
                removeRetrievedTile(tile);
                continue;
            }

            this.elevationModel.downloadElevations(tile,
                new BulkDownloadPostProcessor(tile, this.elevationModel, this.fileStore));
            i++;
        }
    }

    protected class BulkDownloadPostProcessor extends BasicElevationModel.DownloadPostProcessor
    {
        public BulkDownloadPostProcessor(Tile tile, BasicElevationModel elevationModel, FileStore fileStore)
        {
            super(tile, elevationModel, fileStore);
        }

        public ByteBuffer run(Retriever retriever)
        {
            ByteBuffer buffer = super.run(retriever);

            if (retriever.getState().equals(Retriever.RETRIEVER_STATE_SUCCESSFUL))
                removeRetrievedTile(this.tile);

            if (hasRetrievalListeners())
                callRetrievalListeners(retriever, this.tile);

            return buffer;
        }
    }

    protected void callRetrievalListeners(Retriever retriever, Tile tile)
    {
        String eventType = (retriever.getState().equals(Retriever.RETRIEVER_STATE_SUCCESSFUL))
            ? BulkRetrievalEvent.RETRIEVAL_SUCCEEDED : BulkRetrievalEvent.RETRIEVAL_FAILED;
        super.callRetrievalListeners(new BulkRetrievalEvent(this.elevationModel, eventType, tile.getPath()));
    }

    protected synchronized void removeRetrievedTile(Tile tile)
    {
        this.missingTiles.remove(tile);
        // Update progress
        this.progress.setCurrentCount(this.progress.getCurrentCount() + 1);
        this.progress.setCurrentSize(this.progress.getCurrentSize() + estimateAverageTileSize());
        this.progress.setLastUpdateTime(System.currentTimeMillis());
        this.normalizeProgress();
    }

    protected synchronized void removeAbsentTile(Tile tile)
    {
        this.missingTiles.remove(tile);
        // Decrease progress expected total count and size
        this.progress.setTotalCount(this.progress.getTotalCount() - 1);
        this.progress.setTotalSize(this.progress.getTotalSize() - estimateAverageTileSize());
        this.progress.setLastUpdateTime(System.currentTimeMillis());
        this.normalizeProgress();
    }

    protected void normalizeProgress()
    {
        if (this.progress.getTotalCount() < this.progress.getCurrentCount())
        {
            this.progress.setTotalCount(this.progress.getCurrentCount());
            this.progress.setTotalSize(this.progress.getCurrentSize());
        }
    }

    protected long getEstimatedMissingDataSize()
    {
        // Get missing tiles count estimate
        long totMissing = estimateMissingTilesCount(6);
        // Get average tile size estimate
        long averageTileSize = estimateAverageTileSize();

        return totMissing * averageTileSize;
    }

    protected long estimateMissingTilesCount(int numSamples)
    {
        int maxLevel = computeLevelForResolution(sector, resolution);
        // Total expected tiles
        long totCount = 0;
        for (int levelNumber = 0; levelNumber <= maxLevel; levelNumber++)
        {
            if (!this.elevationModel.getLevels().isLevelEmpty(levelNumber))
                totCount += this.countTilesInSector(sector, levelNumber);
        }
        // Sample random small sized sectors at finest level
        int div = this.computeRegionDivisions(this.sector, maxLevel, 36); // max 6x6 tiles per region
        Sector[] regions = computeRandomRegions(this.sector, div, numSamples);
        long regionMissing = 0;
        long regionCount = 0;
        try
        {
            if (regions.length < numSamples)
            {
                regionCount = this.countTilesInSector(this.sector, maxLevel);
                regionMissing = getMissingTilesInSector(this.sector, maxLevel).size();
            }
            else
            {
                for (Sector region : regions)
                {
                    // Count how many tiles are missing in each sample region
                    regionCount += this.countTilesInSector(region, maxLevel);
                    regionMissing += getMissingTilesInSector(region, maxLevel).size();
                }
            }
        }
        catch (InterruptedException e)
        {
            return 0;
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionDuringDataSizeEstimate", this.getName());
            Logging.logger().severe(message);
            throw new RuntimeException(message);
        }

        // Extrapolate total missing count
        return (long)(totCount * ((double)regionMissing / regionCount));
    }

    protected long estimateAverageTileSize()
    {
        Long previouslyComputedSize = (Long) this.elevationModel.getValue(AVKey.AVERAGE_TILE_SIZE);
        if (previouslyComputedSize != null)
            return previouslyComputedSize;

        long size = 0;
        int count = 0;

        // Average cached tile files size in a few directories from first non empty level
        Level targetLevel = this.elevationModel.getLevels().getFirstLevel();
        while (targetLevel.isEmpty() && !targetLevel.equals(this.elevationModel.getLevels().getLastLevel()))
        {
            targetLevel = this.elevationModel.getLevels().getLevel(targetLevel.getLevelNumber() + 1);
        }
        File cacheRoot = new File(this.fileStore.getWriteLocation(), targetLevel.getPath());
        if (cacheRoot.exists())
        {
            File[] rowDirs = cacheRoot.listFiles(new FileFilter()
            {
                public boolean accept(File file)
                {
                    return file.isDirectory();
                }
            });
            for (File dir : rowDirs)
            {
                long averageSize = computeAverageTileSize(dir);
                if (averageSize > 0)
                {
                    size += averageSize;
                    count++;
                }
                if (count >= 2) // average content from up to 2 cache folders
                    break;
            }
        }

        Long averageTileSize = DEFAULT_AVERAGE_FILE_SIZE;
        if (count > 0 && size > 0)
        {
            averageTileSize = size / count;
            this.elevationModel.setValue(AVKey.AVERAGE_TILE_SIZE, averageTileSize);
        }

        return averageTileSize;
    }

    protected static long computeAverageTileSize(File dir)
    {
        long size = 0;
        int count = 0;

        File[] files = dir.listFiles();
        for (File file : files)
        {
            try
            {
                FileInputStream fis = new FileInputStream(file);
                size += fis.available();
                fis.close();
                count++;
            }
            catch (IOException e)
            {
                count += 0;
            }
        }

        return count > 0 ? size / count : 0;
    }

    protected int computeLevelForResolution(Sector sector, double resolution)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        // Find the first level exceeding the desired resolution
        double texelSize;
        Level targetLevel = this.elevationModel.getLevels().getLastLevel();
        for (int i = 0; i < this.elevationModel.getLevels().getLastLevel().getLevelNumber(); i++)
        {
            if (this.elevationModel.getLevels().isLevelEmpty(i))
                continue;

            texelSize = this.elevationModel.getLevels().getLevel(i).getTexelSize();
            if (texelSize > resolution)
                continue;

            targetLevel = this.elevationModel.getLevels().getLevel(i);
            break;
        }

        // Choose the level closest to the resolution desired
        if (targetLevel.getLevelNumber() != 0 && !this.elevationModel.getLevels().isLevelEmpty(
            targetLevel.getLevelNumber() - 1))
        {
            Level nextLowerLevel = this.elevationModel.getLevels().getLevel(targetLevel.getLevelNumber() - 1);
            double dless = Math.abs(nextLowerLevel.getTexelSize() - resolution);
            double dmore = Math.abs(targetLevel.getTexelSize() - resolution);
            if (dless < dmore)
                targetLevel = nextLowerLevel;
        }

        return targetLevel.getLevelNumber();
    }

    protected long countTilesInSector(Sector sector, int levelNumber)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Level targetLevel = this.elevationModel.getLevels().getLastLevel();
        if (levelNumber >= 0)
        {
            for (int i = levelNumber; i < this.elevationModel.getLevels().getLastLevel().getLevelNumber(); i++)
            {
                if (this.elevationModel.getLevels().isLevelEmpty(i))
                    continue;

                targetLevel = this.elevationModel.getLevels().getLevel(i);
                break;
            }
        }

        // Collect all the tiles intersecting the input sector.
        LatLon delta = targetLevel.getTileDelta();
        LatLon origin = this.elevationModel.getLevels().getTileOrigin();
        final int nwRow = Tile.computeRow(delta.getLatitude(), sector.getMaxLatitude(), origin.getLatitude());
        final int nwCol = Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(), origin.getLongitude());
        final int seRow = Tile.computeRow(delta.getLatitude(), sector.getMinLatitude(), origin.getLatitude());
        final int seCol = Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(), origin.getLongitude());

        long numRows = nwRow - seRow + 1;
        long numCols = seCol - nwCol + 1;

        return numRows * numCols;
    }

    protected Tile[][] getTilesInSector(Sector sector, int levelNumber)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Level targetLevel = this.elevationModel.getLevels().getLastLevel();
        if (levelNumber >= 0)
        {
            for (int i = levelNumber; i < this.elevationModel.getLevels().getLastLevel().getLevelNumber(); i++)
            {
                if (this.elevationModel.getLevels().isLevelEmpty(i))
                    continue;

                targetLevel = this.elevationModel.getLevels().getLevel(i);
                break;
            }
        }

        // Collect all the tiles intersecting the input sector.
        LatLon delta = targetLevel.getTileDelta();
        LatLon origin = this.elevationModel.getLevels().getTileOrigin();
        final int nwRow = Tile.computeRow(delta.getLatitude(), sector.getMaxLatitude(), origin.getLatitude());
        final int nwCol = Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(), origin.getLongitude());
        final int seRow = Tile.computeRow(delta.getLatitude(), sector.getMinLatitude(), origin.getLatitude());
        final int seCol = Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(), origin.getLongitude());

        int numRows = nwRow - seRow + 1;
        int numCols = seCol - nwCol + 1;
        Tile[][] sectorTiles = new Tile[numRows][numCols];

        for (int row = nwRow; row >= seRow; row--)
        {
            for (int col = nwCol; col <= seCol; col++)
            {
                TileKey key = new TileKey(targetLevel.getLevelNumber(), row, col, targetLevel.getCacheName());
                Sector tileSector = this.elevationModel.getLevels().computeSectorForKey(key);
                sectorTiles[nwRow - row][col - nwCol] = new Tile(tileSector, targetLevel, row, col);
            }
        }

        return sectorTiles;
    }

    protected ArrayList<Tile> getMissingTilesInSector(Sector sector, int levelNumber) throws InterruptedException
    {
        ArrayList<Tile> tiles = new ArrayList<Tile>();

        Tile[][] tileArray = getTilesInSector(sector, levelNumber);
        for (Tile[] row : tileArray)
        {
            for (Tile tile : row)
            {
                Thread.sleep(1); // generates InterruptedException if thread has been interrupted

                if (tile == null)
                    continue;

                if (isTileLocalOrAbsent(tile))
                    continue;  // tile is local or absent

                tiles.add(tile);
            }
        }
        return tiles;
    }

    protected int computeRegionDivisions(Sector sector, int levelNumber, int maxCount)
    {
        long tileCount = countTilesInSector(sector, levelNumber);

        if (tileCount <= maxCount)
            return 1;

        // Divide sector in regions that will contain no more tiles then maxCount
        return (int) Math.ceil(Math.sqrt((float) tileCount / maxCount));
    }

    protected Sector[] computeRandomRegions(Sector sector, int div, int numRegions)
    {
        if (numRegions > div * div)
            return sector.subdivide(div);

        final double dLat = sector.getDeltaLat().degrees / div;
        final double dLon = sector.getDeltaLon().degrees / div;
        ArrayList<Sector> regions = new ArrayList<Sector>(numRegions);
        Random rand = new Random();
        while (regions.size() < numRegions)
        {
            int row = rand.nextInt(div);
            int col = rand.nextInt(div);

            double maxLat = (row+1 < div) ? sector.getMinLatitude().degrees + dLat * row + dLat
                    : sector.getMaxLatitude().degrees;

            double maxLon = (col+1 < div) ? sector.getMinLongitude().degrees + dLon * col + dLon
                    : sector.getMaxLongitude().degrees;

            Sector s = Sector.fromDegrees(
                sector.getMinLatitude().degrees + dLat * row, maxLat,
                sector.getMinLongitude().degrees + dLon * col, maxLon );

            if (!regions.contains(s))
                regions.add(s);
        }

        return regions.toArray(new Sector[numRegions]);
    }

    protected Iterator<Sector> getRegionIterator(final Sector sector, final int div)
    {
        final double dLat = sector.getDeltaLat().degrees / div;
        final double dLon = sector.getDeltaLon().degrees / div;

        return new Iterator<Sector>()
        {
            int row = 0;
            int col = 0;

            public boolean hasNext()
            {
                return row < div;
            }

            public Sector next()
            {
                double maxLat = (row+1 < div) ? sector.getMinLatitude().degrees + dLat * row + dLat
                        : sector.getMaxLatitude().degrees;

                double maxLon = (col+1 < div) ? sector.getMinLongitude().degrees + dLon * col + dLon
                        : sector.getMaxLongitude().degrees;

                Sector s = Sector.fromDegrees(
                    sector.getMinLatitude().degrees + dLat * row, maxLat,
                    sector.getMinLongitude().degrees + dLon * col, maxLon );

                col++;
                if (col >= div)
                {
                    col = 0;
                    row++;
                }
                return s;
            }

            public void remove()
            {

            }
        };
    }

    protected boolean isTileLocalOrAbsent(Tile tile)
    {
        if (this.elevationModel.getLevels().isResourceAbsent(tile))
            return true;  // tile is absent

        URL url = this.fileStore.findFile(tile.getPath(), false);

        return url != null && !this.elevationModel.isFileExpired(tile, url, this.fileStore);
    }
}

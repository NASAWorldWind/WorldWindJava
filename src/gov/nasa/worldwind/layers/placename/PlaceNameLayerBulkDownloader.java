/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.layers.placename;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.util.Logging;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Downloads placenames not currently available in the WorldWind file cache or a specified {@link FileStore}. The class
 * derives from {@link Thread} and is meant to operate in its own thread.
 * <p>
 * The sector and resolution associated with the downloader are specified during construction and are final.
 *
 * @author tag
 * @version $Id: PlaceNameLayerBulkDownloader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PlaceNameLayerBulkDownloader extends BulkRetrievalThread
{
    protected static final long AVG_TILE_SIZE = 8 * 1024;
    protected int MAX_TILE_COUNT_PER_REGION = 200;

    protected final PlaceNameLayer layer;
    protected ArrayList<PlaceNameLayer.Tile> missingTiles;
    protected long pollDelay = RETRIEVAL_SERVICE_POLL_DELAY;

    /**
     * Constructs a downloader to retrieve placenames not currently available in the WorldWind file cache.
     * <p>
     * The thread returned is not started during construction, the caller must start the thread.
     *
     * @param layer      the layer for which to download placenames.
     * @param sector     the sector to download data for. This value is final.
     * @param resolution the target resolution, provided in radians of latitude per texel. This value is final.
     * @param listener   an optional retrieval listener. May be null.
     *
     * @throws IllegalArgumentException if either the layer or sector are null, or the resolution is less than zero.
     */
    public PlaceNameLayerBulkDownloader(PlaceNameLayer layer, Sector sector, double resolution,
        BulkRetrievalListener listener)
    {
        // Arguments checked in parent constructor
        //resolution is compared to the maxDsiatnce value in each placenameservice
        super(layer, sector, resolution, layer.getDataFileStore(), listener);

        this.layer = layer;
    }

    /**
     * Constructs a downloader to retrieve placenames not currently available in a specified file store and places it
     * there.
     * <p>
     * The thread returned is not started during construction, the caller must start the thread.
     *
     * @param layer      the layer for which to download placenames.
     * @param sector     the sector to download data for. This value is final.
     * @param resolution the target resolution, provided in radians of latitude per texel. This value is final.
     * @param fileStore  the file store in which to place the downloaded elevations.
     * @param listener   an optional retrieval listener. May be null.
     *
     * @throws IllegalArgumentException if either the layer, the sector or file store are null, or the resolution is
     *                                  less than zero.
     */
    public PlaceNameLayerBulkDownloader(PlaceNameLayer layer, Sector sector, double resolution, FileStore fileStore,
        BulkRetrievalListener listener)
    {
        // Arguments checked in parent constructor
        //resolution is compared to the maxDsiatnce value in each placenameservice
        super(layer, sector, resolution, fileStore, listener);

        this.layer = layer;
    }

    public void run()
    {
        try
        {
            // Cycle though placenameservices and find missing tiles
            this.missingTiles = new ArrayList<PlaceNameLayer.Tile>();
            ArrayList<PlaceNameLayer.Tile> allMissingTiles = this.getMissingTilesInSector(this.sector);

            this.progress.setTotalCount(allMissingTiles.size());
            // Submit missing tiles requests at 10 sec intervals
            while (allMissingTiles.size() > 0)
            {
                transferMissingTiles(allMissingTiles, missingTiles, MAX_TILE_COUNT_PER_REGION);

                while (missingTiles.size() > 0)
                {
                    submitMissingTilesRequests();
                    if (missingTiles.size() > 0)
                        Thread.sleep(this.pollDelay);
                }
            }
        }
        catch (InterruptedException e)
        {
            String message = Logging.getMessage("generic.BulkRetrievalInterrupted", layer.getName());
            Logging.logger().log(java.util.logging.Level.WARNING, message, e);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionDuringBulkRetrieval", layer.getName());
            Logging.logger().severe(message);
            throw new RuntimeException(message);
        }
    }

    protected void transferMissingTiles(ArrayList<PlaceNameLayer.Tile> source,
        ArrayList<PlaceNameLayer.Tile> destination, int maxCount)
    {
        int i = 0;
        while (i < maxCount && source.size() > 0)
        {
            destination.add(source.remove(0));
            i++;
        }
    }

    protected synchronized void submitMissingTilesRequests() throws InterruptedException
    {
        RetrievalService rs = WorldWind.getRetrievalService();
        int i = 0;
        while (this.missingTiles.size() > i && rs.isAvailable())
        {
            Thread.sleep(1); // generates InterruptedException if thread has been interrupted

            PlaceNameLayer.Tile tile = this.missingTiles.get(i);
            if (this.isTileLocalOrAbsent(tile))
            {
                // No need to request that tile anymore
                this.missingTiles.remove(i);
            }
            else
            {
                this.layer.downloadTile(tile, new BulkDownloadPostProcessor(this.layer, tile, this.fileStore));
                i++;
            }
        }
    }

    protected class BulkDownloadPostProcessor extends PlaceNameLayer.DownloadPostProcessor
    {
        public BulkDownloadPostProcessor(PlaceNameLayer layer, PlaceNameLayer.Tile tile, FileStore fileStore)
        {
            super(layer, tile, fileStore);
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

    protected void callRetrievalListeners(Retriever retriever, PlaceNameLayer.Tile tile)
    {
        String eventType = (retriever.getState().equals(Retriever.RETRIEVER_STATE_SUCCESSFUL))
            ? BulkRetrievalEvent.RETRIEVAL_SUCCEEDED : BulkRetrievalEvent.RETRIEVAL_FAILED;
        super.callRetrievalListeners(new BulkRetrievalEvent(this.layer, eventType, tile.getFileCachePath()));
    }

    protected synchronized void removeRetrievedTile(PlaceNameLayer.Tile tile)
    {
        this.missingTiles.remove(tile);
        this.progress.setCurrentCount(this.progress.getCurrentCount() + 1);
        this.progress.setCurrentSize(this.progress.getCurrentSize() + AVG_TILE_SIZE);
        this.progress.setLastUpdateTime(System.currentTimeMillis());
        // Estimate total size
        this.progress.setTotalSize(
            this.progress.getCurrentSize() / this.progress.getCurrentCount() * this.progress.getTotalCount());
    }

    protected long getEstimatedMissingDataSize()
    {
        int tileCount;
        try
        {
            tileCount = this.getMissingTilesCountEstimate(sector, resolution);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionDuringDataSizeEstimate", this.getName());
            Logging.logger().severe(message);
            throw new RuntimeException(message);
        }
        return tileCount * AVG_TILE_SIZE;
    }

    protected int getMissingTilesCountEstimate(Sector sector, double resolution)
    {
        int tileCount = 0;
        int serviceCount = this.layer.getPlaceNameServiceSet().getServiceCount();
        for (int i = 0; i < serviceCount; i++)
        {
            int serviceTileCount = 0;
            PlaceNameService service = this.layer.getPlaceNameServiceSet().getService(i);
            if (service.getMaxDisplayDistance() > resolution)
            {
                PlaceNameLayer.NavigationTile navTile = this.layer.navTiles.get(i);
                // drill down into tiles to find bottom level navTiles visible
                List<PlaceNameLayer.NavigationTile> list = this.navTilesVisible(navTile, sector);
                for (PlaceNameLayer.NavigationTile nt : list)
                {
                    serviceTileCount += this.estimateNumberTilesinSector(nt, sector);
                }
            }

            tileCount += serviceTileCount;
        }

        return tileCount;
    }

    protected ArrayList<PlaceNameLayer.Tile> getMissingTilesInSector(Sector sector) throws InterruptedException
    {
        ArrayList<PlaceNameLayer.Tile> allMissingTiles = new ArrayList<PlaceNameLayer.Tile>();
        int serviceCount = this.layer.getPlaceNameServiceSet().getServiceCount();
        for (int i = 0; i < serviceCount; i++)
        {
            PlaceNameService service = this.layer.getPlaceNameServiceSet().getService(i);
            if (service.getMaxDisplayDistance() > this.resolution)
            {
                // get tiles in sector
                ArrayList<PlaceNameLayer.Tile> baseTiles = new ArrayList<PlaceNameLayer.Tile>();

                PlaceNameLayer.NavigationTile navTile = this.layer.navTiles.get(i);
                // drill down into tiles to find bottom level navTiles visible
                List<PlaceNameLayer.NavigationTile> list = this.navTilesVisible(navTile, sector);
                for (PlaceNameLayer.NavigationTile nt : list)
                {
                    baseTiles.addAll(nt.getTiles());
                }

                for (PlaceNameLayer.Tile tile : baseTiles)
                {
                    if ((tile.getSector().intersects(sector)) && (!this.isTileLocalOrAbsent(tile)))
                        allMissingTiles.add(tile);
                }
            }
        }

        return allMissingTiles;
    }

    protected List<PlaceNameLayer.NavigationTile> navTilesVisible(PlaceNameLayer.NavigationTile tile, Sector sector)
    {
        ArrayList<PlaceNameLayer.NavigationTile> navList = new ArrayList<PlaceNameLayer.NavigationTile>();
        if (tile.navSector.intersects(sector))
        {
            if (tile.level > 0 && !tile.hasSubTiles())
                tile.buildSubNavTiles();

            if (tile.hasSubTiles())
            {
                for (PlaceNameLayer.NavigationTile nav : tile.subNavTiles)
                {
                    navList.addAll(this.navTilesVisible(nav, sector));
                }
            }
            else  //at bottom level navigation tile
            {
                navList.add(tile);
            }
        }

        return navList;
    }

    protected int estimateNumberTilesinSector(PlaceNameLayer.NavigationTile tile, Sector searchSector)
    {
        final Angle dLat = tile.placeNameService.getTileDelta().getLatitude();
        final Angle dLon = tile.placeNameService.getTileDelta().getLongitude();

        // Determine the row and column offset from the global tiling origin for the southwest tile corner
        int firstRow = PlaceNameLayer.Tile.computeRow(dLat, tile.navSector.getMinLatitude());
        int firstCol = PlaceNameLayer.Tile.computeColumn(dLon, tile.navSector.getMinLongitude());
        int lastRow = PlaceNameLayer.Tile.computeRow(dLat, tile.navSector.getMaxLatitude().subtract(dLat));
        int lastCol = PlaceNameLayer.Tile.computeColumn(dLon, tile.navSector.getMaxLongitude().subtract(dLon));

        int tileCount = 0;
        Angle p1 = PlaceNameLayer.Tile.computeRowLatitude(firstRow, dLat);
        boolean needToCheckDisk = true;
        for (int row = 0; row <= lastRow - firstRow; row++)
        {
            Angle p2;
            p2 = p1.add(dLat);

            Angle t1 = PlaceNameLayer.Tile.computeColumnLongitude(firstCol, dLon);
            for (int col = 0; col <= lastCol - firstCol; col++)
            {
                Angle t2;
                t2 = t1.add(dLon);
                Sector tileSector = new Sector(p1, p2, t1, t2);

                if (tileSector.intersects(searchSector))
                {
                    if (needToCheckDisk)
                    {
                        //now check if on disk
                        String filePath = tile.placeNameService.createFileCachePathFromTile(row + firstRow,
                            col + firstCol);
                        final java.net.URL tileURL = this.fileStore.findFile(filePath, false);
                        if (tileURL == null)
                            needToCheckDisk = false; //looked and found nothing
                        else
                            return 0;  //found one, assume rest are there
                    }

                    tileCount++;
                }
                t1 = t2;
            }
            p1 = p2;
        }

        return tileCount;
    }

    protected boolean isTileLocalOrAbsent(PlaceNameLayer.Tile tile)
    {
        if (tile.getPlaceNameService().isResourceAbsent(
            tile.getPlaceNameService().getTileNumber(tile.row, tile.column)))
            return true;    // tile is absent

        URL url = this.fileStore.findFile(tile.getFileCachePath(), false);
        return url != null; // tile is already in cache
    }
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.placename;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.event.BulkRetrievalListener;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe2D;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;

/**
 * @author Paul Collins
 * @version $Id: PlaceNameLayer.java 2392 2014-10-20 20:02:44Z tgaskins $
 */
public class PlaceNameLayer extends AbstractLayer implements BulkRetrievable
{
    protected final PlaceNameServiceSet placeNameServiceSet;
    protected PriorityBlockingQueue<Runnable> requestQ = new PriorityBlockingQueue<Runnable>(64);
    protected Vec4 referencePoint;
    protected final Object fileLock = new Object();
    protected boolean cullNames = true; // this flag is no longer used. placenames participate in global decluttering

    public static final double LEVEL_A = 0x1 << 26; // 67,108 km
    public static final double LEVEL_B = 0x1 << 24; // 16,777 km
    public static final double LEVEL_C = 0x1 << 23; // 8,388 km
    public static final double LEVEL_D = 0x1 << 22; // 4,194 km
    public static final double LEVEL_E = 0x1 << 21; // 2,097 km
    public static final double LEVEL_F = 0x1 << 20; // 1,048 km
    public static final double LEVEL_G = 0x1 << 19; // 524 km
    public static final double LEVEL_H = 0x1 << 18; // 262 km
    public static final double LEVEL_I = 0x1 << 17; // 131 km
    public static final double LEVEL_J = 0x1 << 16; // 65 km
    public static final double LEVEL_K = 0x1 << 15; // 32 km
    public static final double LEVEL_L = 0x1 << 14; // 16 km
    public static final double LEVEL_M = 0x1 << 13; // 8 km
    public static final double LEVEL_N = 0x1 << 12; // 4 km
    public static final double LEVEL_O = 0x1 << 11; // 2 km
    public static final double LEVEL_P = 0x1 << 10; // 1 km
    public static final LatLon GRID_1x1 = new LatLon(Angle.fromDegrees(180d), Angle.fromDegrees(360d));
    public static final LatLon GRID_4x8 = new LatLon(Angle.fromDegrees(45d), Angle.fromDegrees(45d));
    public static final LatLon GRID_8x16 = new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(22.5d));
    public static final LatLon GRID_16x32 = new LatLon(Angle.fromDegrees(11.25d), Angle.fromDegrees(11.25d));
    public static final LatLon GRID_36x72 = new LatLon(Angle.fromDegrees(5d), Angle.fromDegrees(5d));
    public static final LatLon GRID_72x144 = new LatLon(Angle.fromDegrees(2.5d), Angle.fromDegrees(2.5d));
    public static final LatLon GRID_144x288 = new LatLon(Angle.fromDegrees(1.25d), Angle.fromDegrees(1.25d));
    public static final LatLon GRID_288x576 = new LatLon(Angle.fromDegrees(0.625d), Angle.fromDegrees(0.625d));
    public static final LatLon GRID_576x1152 = new LatLon(Angle.fromDegrees(0.3125d), Angle.fromDegrees(0.3125d));
    public static final LatLon GRID_1152x2304 = new LatLon(Angle.fromDegrees(0.1563d), Angle.fromDegrees(0.1563d));

    protected List<NavigationTile> navTiles = new ArrayList<NavigationTile>();
    //top navigation tiles for each service

    /**
     * @param placeNameServiceSet the set of PlaceNameService objects that PlaceNameLayer will render.
     *
     * @throws IllegalArgumentException if  {@link gov.nasa.worldwind.layers.placename.PlaceNameServiceSet} is null
     */
    public PlaceNameLayer(PlaceNameServiceSet placeNameServiceSet)
    {
        if (placeNameServiceSet == null)
        {
            String message = Logging.getMessage("nullValue.PlaceNameServiceSetIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        //  
        this.placeNameServiceSet = placeNameServiceSet.deepCopy();
        for (int i = 0; i < this.placeNameServiceSet.getServiceCount(); i++)
        {
            //todo do this for long as well and pick min
            int calc1 = (int) (PlaceNameService.TILING_SECTOR.getDeltaLatDegrees()
                / this.placeNameServiceSet.getService(i).getTileDelta().getLatitude().getDegrees());
            int numLevels = (int) Math.log(calc1);
            navTiles.add(
                new NavigationTile(this.placeNameServiceSet.getService(i), PlaceNameService.TILING_SECTOR, numLevels,
                    "top"));
        }

        if (!WorldWind.getMemoryCacheSet().containsCache(Tile.class.getName()))
        {
            long size = Configuration.getLongValue(AVKey.PLACENAME_LAYER_CACHE_SIZE, 2000000L);
            MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
            cache.setName("Placename Tiles");
            WorldWind.getMemoryCacheSet().addCache(Tile.class.getName(), cache);
        }
    }

    /**
     * @return not in use
     *
     * @deprecated This flag no longer has any effect. Placenames participate in global decluttering.
     */
    @Deprecated
    public boolean isCullNames()
    {
        return cullNames;
    }

    /**
     * @param cullNames not used
     *
     * @deprecated This flag no longer has any effect. Placenames participate in global decluttering.
     */
    @Deprecated
    public void setCullNames(boolean cullNames)
    {
        this.cullNames = cullNames;
    }

    public final PlaceNameServiceSet getPlaceNameServiceSet()
    {
        return this.placeNameServiceSet;
    }

    protected PriorityBlockingQueue<Runnable> getRequestQ()
    {
        return this.requestQ;
    }

    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //

    protected class NavigationTile
    {
        String id;
        protected PlaceNameService placeNameService;
        public Sector navSector;
        protected List<NavigationTile> subNavTiles = new ArrayList<NavigationTile>();
        protected List<String> tileKeys = new ArrayList<String>();
        protected int level;

        NavigationTile(PlaceNameService placeNameService, Sector sector, int levels, String id)
        {
            this.placeNameService = placeNameService;
            this.id = id;
            this.navSector = sector;
            level = levels;
        }

        protected void buildSubNavTiles()
        {
            if (level > 0)
            {
                //split sector, create a navTile for each quad
                Sector[] subSectors = this.navSector.subdivide();
                for (int j = 0; j < subSectors.length; j++)
                {
                    subNavTiles.add(new NavigationTile(placeNameService, subSectors[j], level - 1, this.id + "." + j));
                }
            }
        }

        public List<NavigationTile> navTilesVisible(DrawContext dc, double minDistSquared, double maxDistSquared)
        {
            ArrayList<NavigationTile> navList = new ArrayList<NavigationTile>();
            if (this.isNavSectorVisible(dc, minDistSquared, maxDistSquared))
            {
                if (this.level > 0 && !this.hasSubTiles())
                    this.buildSubNavTiles();

                if (this.hasSubTiles())
                {
                    for (NavigationTile nav : subNavTiles)
                    {
                        navList.addAll(nav.navTilesVisible(dc, minDistSquared, maxDistSquared));
                    }
                }
                else  //at bottom level navigation tile
                {
                    navList.add(this);
                }
            }

            return navList;
        }

        public boolean hasSubTiles()
        {
            return !subNavTiles.isEmpty();
        }

        protected boolean isNavSectorVisible(DrawContext dc, double minDistanceSquared, double maxDistanceSquared)
        {
            if (!navSector.intersects(dc.getVisibleSector()))
                return false;

            if (dc.is2DGlobe())
            {
                Sector limits = ((Globe2D)dc.getGlobe()).getProjection().getProjectionLimits();
                if (limits != null && !limits.intersectsInterior(navSector))
                    return false;
            }

            View view = dc.getView();
            Position eyePos = view.getEyePosition();
            if (eyePos == null)
                return false;

            //check for eyePos over globe
            if (Double.isNaN(eyePos.getLatitude().getDegrees()) || Double.isNaN(eyePos.getLongitude().getDegrees()))
                return false;

            double distSquared;
            if (dc.isContinuous2DGlobe())
            {
                // Just use the eye altitude since the majority of non-visible sectors are culled elsewhere.
                distSquared = eyePos.getAltitude() * eyePos.getAltitude();
            }
            else
            {
                Angle lat = clampAngle(eyePos.getLatitude(), navSector.getMinLatitude(),
                    navSector.getMaxLatitude());
                Angle lon = clampAngle(eyePos.getLongitude(), navSector.getMinLongitude(),
                    navSector.getMaxLongitude());
                Vec4 p = dc.getGlobe().computePointFromPosition(lat, lon, 0d);
                distSquared = dc.getView().getEyePoint().distanceToSquared3(p);
            }

            //noinspection RedundantIfStatement
            if (minDistanceSquared > distSquared || maxDistanceSquared < distSquared)
                return false;

            return true;
        }

        public List<Tile> getTiles()
        {
            if (tileKeys.isEmpty())
            {
                Tile[] tiles = buildTiles(this.placeNameService, this);
                //load tileKeys
                for (Tile t : tiles)
                {
                    tileKeys.add(t.getFileCachePath());
                    WorldWind.getMemoryCache(Tile.class.getName()).add(t.getFileCachePath(), t);
                }
                return Arrays.asList(tiles);
            }
            else
            {
                List<Tile> dataTiles = new ArrayList<Tile>();
                for (String s : tileKeys)
                {
                    Tile t = (Tile) WorldWind.getMemoryCache(Tile.class.getName()).getObject(s);
                    if (t != null)
                    {
                        dataTiles.add(t);
                    }
                }
                return dataTiles;
            }
        }
    }

    protected static class Tile implements Cacheable
    {
        protected final PlaceNameService placeNameService;
        protected final Sector sector;
        protected final int row;
        protected final int column;
        private Integer hashInt = null;
        // Computed data.
        protected String fileCachePath = null;
        protected double extentVerticalExaggeration = Double.MIN_VALUE;
        protected double priority = Double.MAX_VALUE; // Default is minimum priority
        protected PlaceNameChunk dataChunk = null;

        Tile(PlaceNameService placeNameService, Sector sector, int row, int column)
        {
            this.placeNameService = placeNameService;
            this.sector = sector;
            this.row = row;
            this.column = column;
            this.fileCachePath = this.placeNameService.createFileCachePathFromTile(this.row, this.column);
            this.hashInt = this.computeHash();
        }

        public void setDataChunk(PlaceNameChunk chunk)
        {
            dataChunk = chunk;
        }

        public PlaceNameChunk getDataChunk()
        {
            return dataChunk;
        }

        public long getSizeInBytes()
        {

            long result = 32; //references

            result += this.getSector().getSizeInBytes();
            if (this.getFileCachePath() != null)
                result += this.getFileCachePath().length();

            if (dataChunk != null)
            {
                result += dataChunk.estimatedMemorySize;
            }

            return result;
        }

        static int computeRow(Angle delta, Angle latitude)
        {
            if (delta == null || latitude == null)
            {
                String msg = Logging.getMessage("nullValue.AngleIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            return (int) ((latitude.getDegrees() + 90d) / delta.getDegrees());
        }

        static int computeColumn(Angle delta, Angle longitude)
        {
            if (delta == null || longitude == null)
            {
                String msg = Logging.getMessage("nullValue.AngleIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            return (int) ((longitude.getDegrees() + 180d) / delta.getDegrees());
        }

        static Angle computeRowLatitude(int row, Angle delta)
        {
            if (delta == null)
            {
                String msg = Logging.getMessage("nullValue.AngleIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            return Angle.fromDegrees(-90d + delta.getDegrees() * row);
        }

        static Angle computeColumnLongitude(int column, Angle delta)
        {
            if (delta == null)
            {
                String msg = Logging.getMessage("nullValue.AngleIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            return Angle.fromDegrees(-180 + delta.getDegrees() * column);
        }

        public Integer getHashInt()
        {
            return hashInt;
        }

        int computeHash()
        {
            return this.getFileCachePath() != null ? this.getFileCachePath().hashCode() : 0;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final Tile tile = (Tile) o;

            return !(this.getFileCachePath() != null ? !this.getFileCachePath().equals(tile.getFileCachePath())
                : tile.getFileCachePath() != null);
        }

        public String getFileCachePath()
        {
            if (this.fileCachePath == null)
                this.fileCachePath = this.placeNameService.createFileCachePathFromTile(this.row, this.column);

            return this.fileCachePath;
        }

        public PlaceNameService getPlaceNameService()
        {
            return placeNameService;
        }

        public java.net.URL getRequestURL() throws java.net.MalformedURLException
        {
            return this.placeNameService.createServiceURLFromSector(this.sector);
        }

        public Sector getSector()
        {
            return sector;
        }

        public int hashCode()
        {
            return this.hashInt;
        }

        protected boolean isTileInMemoryWithData()
        {
            Tile t = (Tile) WorldWind.getMemoryCache(Tile.class.getName()).getObject(this.getFileCachePath());
            return !(t == null || t.getDataChunk() == null);
        }

        public double getPriority()
        {
            return priority;
        }

        public void setPriority(double priority)
        {
            this.priority = priority;
        }
    }

    protected Tile[] buildTiles(PlaceNameService placeNameService, NavigationTile navTile)
    {
        final Angle dLat = placeNameService.getTileDelta().getLatitude();
        final Angle dLon = placeNameService.getTileDelta().getLongitude();

        // Determine the row and column offset from the global tiling origin for the southwest tile corner
        int firstRow = Tile.computeRow(dLat, navTile.navSector.getMinLatitude());
        int firstCol = Tile.computeColumn(dLon, navTile.navSector.getMinLongitude());
        int lastRow = Tile.computeRow(dLat, navTile.navSector.getMaxLatitude().subtract(dLat));
        int lastCol = Tile.computeColumn(dLon, navTile.navSector.getMaxLongitude().subtract(dLon));

        int nLatTiles = lastRow - firstRow + 1;
        int nLonTiles = lastCol - firstCol + 1;

        Tile[] tiles = new Tile[nLatTiles * nLonTiles];

        Angle p1 = Tile.computeRowLatitude(firstRow, dLat);
        for (int row = 0; row <= lastRow - firstRow; row++)
        {
            Angle p2;
            p2 = p1.add(dLat);

            Angle t1 = Tile.computeColumnLongitude(firstCol, dLon);
            for (int col = 0; col <= lastCol - firstCol; col++)
            {
                Angle t2;
                t2 = t1.add(dLon);
                //Need offset row and column to correspond to total ro/col numbering
                tiles[col + row * nLonTiles] = new Tile(placeNameService, new Sector(p1, p2, t1, t2),
                    row + firstRow, col + firstCol);
                t1 = t2;
            }
            p1 = p2;
        }

        return tiles;
    }

    // ============== Place Name Data Structures ======================= //
    // ============== Place Name Data Structures ======================= //
    // ============== Place Name Data Structures ======================= //

    protected static class PlaceNameChunk implements Cacheable
    {
        protected final PlaceNameService placeNameService;
        protected final CharBuffer textArray;
        protected final int[] textIndexArray;
        protected final double[] latlonArray;
        protected final int numEntries;
        protected final long estimatedMemorySize;

        protected PlaceNameChunk(PlaceNameService service, CharBuffer text, int[] textIndices,
            double[] positions, int numEntries)
        {
            this.placeNameService = service;
            this.textArray = text;
            this.textIndexArray = textIndices;
            this.latlonArray = positions;
            this.numEntries = numEntries;
            this.estimatedMemorySize = this.computeEstimatedMemorySize();
        }

        protected long computeEstimatedMemorySize()
        {
            long result = 0;
            if (!textArray.isDirect())
                result += (Character.SIZE / 8) * textArray.capacity();
            result += (Integer.SIZE / 8) * textIndexArray.length;
            result += (Double.SIZE / 8) * latlonArray.length;
            return result;
        }

        protected Position getPosition(int index)
        {
            int latlonIndex = 2 * index;
            return Position.fromDegrees(latlonArray[latlonIndex], latlonArray[latlonIndex + 1], 0);
        }

        protected PlaceNameService getPlaceNameService()
        {
            return this.placeNameService;
        }

        protected CharSequence getText(int index)
        {
            int beginIndex = textIndexArray[index];
            int endIndex = (index + 1 < numEntries) ? textIndexArray[index + 1] : textArray.length();

            // Cast the textArray CharBuffer to a CharSequence before calling subSequence. Java 7 broke interface
            // compatibility on this method by changing the return type to a CharBuffer. Compiling
            // CharBuffer.subSequence on Java 7 results in a NoSuchMethodError on Java 6. We cast to a CharSequence in
            // order to ensure that this code works on both Java 6 and Java 7 when compiled with Java 7.
            return ((CharSequence) this.textArray).subSequence(beginIndex, endIndex);
        }

        public long getSizeInBytes()
        {
            return this.estimatedMemorySize;
        }

        protected Iterable<GeographicText> makeIterable(DrawContext dc)
        {
            //get dispay dist for this service for use in label annealing
            double maxDisplayDistance = this.getPlaceNameService().getMaxDisplayDistance();
            ArrayList<GeographicText> list = new ArrayList<GeographicText>();
            for (int i = 0; i < this.numEntries; i++)
            {
                CharSequence str = getText(i);
                Position pos = getPosition(i);
                GeographicText text = new UserFacingText(str, pos);
                text.setFont(this.placeNameService.getFont());
                text.setColor(this.placeNameService.getColor());
                text.setBackgroundColor(this.placeNameService.getBackgroundColor());
                text.setVisible(isNameVisible(dc, this.placeNameService, pos));
                text.setPriority(maxDisplayDistance);
                list.add(text);
            }
            return list;
        }
    }

    // ============== Rendering ======================= //
    // ============== Rendering ======================= //
    // ============== Rendering ======================= //

    @Override
    protected void doRender(DrawContext dc)
    {
        this.referencePoint = this.computeReferencePoint(dc);

        int serviceCount = this.placeNameServiceSet.getServiceCount();
        for (int i = 0; i < serviceCount; i++)
        {
            PlaceNameService placeNameService = this.placeNameServiceSet.getService(i);
            if (!isServiceVisible(dc, placeNameService))
                continue;

            double minDistSquared = placeNameService.getMinDisplayDistance() * placeNameService.getMinDisplayDistance();
            double maxDistSquared = placeNameService.getMaxDisplayDistance() * placeNameService.getMaxDisplayDistance();

            if (isSectorVisible(dc, placeNameService.getMaskingSector(), minDistSquared, maxDistSquared))
            {
                ArrayList<Tile> baseTiles = new ArrayList<Tile>();
                NavigationTile navTile = this.navTiles.get(i);
                //drill down into tiles to find bottom level navTiles visible
                List<NavigationTile> list = navTile.navTilesVisible(dc, minDistSquared, maxDistSquared);
                for (NavigationTile nt : list)
                {
                    baseTiles.addAll(nt.getTiles());
                }

                for (Tile tile : baseTiles)
                {
                    try
                    {
                        drawOrRequestTile(dc, tile, minDistSquared, maxDistSquared);
                    }
                    catch (Exception e)
                    {
                        Logging.logger().log(Level.FINE,
                            Logging.getMessage("layers.PlaceNameLayer.ExceptionRenderingTile"),
                            e);
                    }
                }
            }
        }

        this.sendRequests();
        this.requestQ.clear();
    }

    protected Vec4 computeReferencePoint(DrawContext dc)
    {
        if (dc.getViewportCenterPosition() != null)
            return dc.getGlobe().computePointFromPosition(dc.getViewportCenterPosition());

        java.awt.geom.Rectangle2D viewport = dc.getView().getViewport();
        int x = (int) viewport.getWidth() / 2;
        for (int y = (int) (0.5 * viewport.getHeight()); y >= 0; y--)
        {
            Position pos = dc.getView().computePositionFromScreenPoint(x, y);
            if (pos == null)
                continue;

            return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), 0d);
        }

        return null;
    }

    protected Vec4 getReferencePoint()
    {
        return this.referencePoint;
    }

    protected void drawOrRequestTile(DrawContext dc, Tile tile, double minDisplayDistanceSquared,
        double maxDisplayDistanceSquared)
    {
        if (!isTileVisible(dc, tile, minDisplayDistanceSquared, maxDisplayDistanceSquared))
            return;

        if (tile.isTileInMemoryWithData())
        {
            PlaceNameChunk placeNameChunk = tile.getDataChunk();
            if (placeNameChunk.numEntries > 0)
            {
                Iterable<GeographicText> renderIter = placeNameChunk.makeIterable(dc);
                dc.getDeclutteringTextRenderer().render(dc, renderIter);
            }
            return;
        }

        // Tile's data isn't available, so request it
        if (!tile.getPlaceNameService().isResourceAbsent(tile.getPlaceNameService().getTileNumber(
            tile.row, tile.column)))
        {
            this.requestTile(dc, tile);
        }
    }

    protected static boolean isServiceVisible(DrawContext dc, PlaceNameService placeNameService)
    {
        //noinspection SimplifiableIfStatement
        if (!placeNameService.isEnabled())
            return false;

        return (dc.getVisibleSector() != null) && placeNameService.getMaskingSector().intersects(dc.getVisibleSector());
//
//        return placeNameService.getExtent(dc).intersects(dc.getView().getFrustumInModelCoordinates());
    }

    protected static boolean isSectorVisible(DrawContext dc, Sector sector, double minDistanceSquared,
        double maxDistanceSquared)
    {

        View view = dc.getView();
        Position eyePos = view.getEyePosition();
        if (eyePos == null)
            return false;

        double distSquared;
        if (dc.isContinuous2DGlobe())
        {
            // Just use the eye altitude since the majority of non-visible sectors are culled elsewhere.
            distSquared = eyePos.getAltitude() * eyePos.getAltitude();
        }
        else
        {
            Angle lat = clampAngle(eyePos.getLatitude(), sector.getMinLatitude(),
                sector.getMaxLatitude());
            Angle lon = clampAngle(eyePos.getLongitude(), sector.getMinLongitude(),
                sector.getMaxLongitude());
            Vec4 p = dc.getGlobe().computePointFromPosition(lat, lon, 0d);
            distSquared = dc.getView().getEyePoint().distanceToSquared3(p);
        }

        //noinspection RedundantIfStatement
        if (minDistanceSquared > distSquared || maxDistanceSquared < distSquared)
            return false;

        return true;
    }

    protected static boolean isTileVisible(DrawContext dc, Tile tile, double minDistanceSquared,
        double maxDistanceSquared)
    {
        if (!tile.getSector().intersects(dc.getVisibleSector()))
            return false;

        View view = dc.getView();
        Position eyePos = view.getEyePosition();
        if (eyePos == null)
            return false;

        double distSquared;
        if (dc.isContinuous2DGlobe())
        {
            // Just use the eye altitude since the majority of non-visible sectors are culled elsewhere.
            distSquared = eyePos.getAltitude() * eyePos.getAltitude();
        }
        else
        {
            Angle lat = clampAngle(eyePos.getLatitude(), tile.getSector().getMinLatitude(),
                tile.getSector().getMaxLatitude());
            Angle lon = clampAngle(eyePos.getLongitude(), tile.getSector().getMinLongitude(),
                tile.getSector().getMaxLongitude());
            Vec4 p = dc.getGlobe().computePointFromPosition(lat, lon, 0d);
            distSquared = dc.getView().getEyePoint().distanceToSquared3(p);
        }

        //noinspection RedundantIfStatement
        if (minDistanceSquared > distSquared || maxDistanceSquared < distSquared)
            return false;

        return true;
    }

    protected static boolean isNameVisible(DrawContext dc, PlaceNameService service, Position namePosition)
    {
        double elevation = dc.getVerticalExaggeration() * namePosition.getElevation();
        Vec4 namePoint = dc.getGlobe().computePointFromPosition(namePosition.getLatitude(),
            namePosition.getLongitude(), elevation);
        Vec4 eyeVec = dc.getView().getEyePoint();

        double dist = eyeVec.distanceTo3(namePoint);
        return dist >= service.getMinDisplayDistance() && dist <= service.getMaxDisplayDistance();
    }

    protected static Angle clampAngle(Angle a, Angle min, Angle max)
    {
        double degrees = a.degrees;
        double minDegrees = min.degrees;
        double maxDegrees = max.degrees;
        return Angle.fromDegrees(degrees < minDegrees ? minDegrees : (degrees > maxDegrees ? maxDegrees : degrees));
    }

    // ============== Image Reading and Downloading ======================= //
    // ============== Image Reading and Downloading ======================= //
    // ============== Image Reading and Downloading ======================= //

    protected void requestTile(DrawContext dc, Tile tile)
    {
        Vec4 centroid = dc.getGlobe().computePointFromPosition(tile.getSector().getCentroid(), 0);
        if (this.getReferencePoint() != null)
            tile.setPriority(centroid.distanceTo3(this.getReferencePoint()));

        RequestTask task = new RequestTask(tile, this);
        this.getRequestQ().add(task);
    }

    protected void sendRequests()
    {
        Runnable task = this.requestQ.poll();
        while (task != null)
        {
            if (!WorldWind.getTaskService().isFull())
            {
                WorldWind.getTaskService().addTask(task);
            }
            task = this.requestQ.poll();
        }
    }

    protected static class RequestTask implements Runnable, Comparable<RequestTask>
    {
        protected final PlaceNameLayer layer;
        protected final Tile tile;

        RequestTask(Tile tile, PlaceNameLayer layer)
        {
            this.layer = layer;
            this.tile = tile;
        }

        public void run()
        {
            if (Thread.currentThread().isInterrupted())
                return;

            if (this.tile.isTileInMemoryWithData())
                return;

            final java.net.URL tileURL = this.layer.getDataFileStore().findFile(tile.getFileCachePath(), false);
            if (tileURL != null)
            {
                if (this.layer.loadTile(this.tile, tileURL))
                {
                    tile.getPlaceNameService().unmarkResourceAbsent(tile.getPlaceNameService().getTileNumber(
                        tile.row,
                        tile.column));
                    this.layer.firePropertyChange(AVKey.LAYER, null, this);
                    return;
                }
            }

            this.layer.downloadTile(this.tile);
        }

        /**
         * @param that the task to compare
         *
         * @return -1 if <code>this</code> less than <code>that</code>, 1 if greater than, 0 if equal
         *
         * @throws IllegalArgumentException if <code>that</code> is null
         */
        public int compareTo(RequestTask that)
        {
            if (that == null)
            {
                String msg = Logging.getMessage("nullValue.RequestTaskIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            return this.tile.getPriority() == that.tile.getPriority() ? 0 :
                this.tile.getPriority() < that.tile.getPriority() ? -1 : 1;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final RequestTask that = (RequestTask) o;

            // Don't include layer in comparison so that requests are shared among layers
            return !(tile != null ? !tile.equals(that.tile) : that.tile != null);
        }

        public int hashCode()
        {
            return (tile != null ? tile.hashCode() : 0);
        }

        public String toString()
        {
            return this.tile.toString();
        }
    }

    protected boolean loadTile(Tile tile, java.net.URL url)
    {
        if (WWIO.isFileOutOfDate(url, this.placeNameServiceSet.getExpiryTime()))
        {
            // The file has expired. Delete it then request download of newer.
            this.getDataFileStore().removeFile(url);
            String message = Logging.getMessage("generic.DataFileExpired", url);
            Logging.logger().fine(message);
            return false;
        }

        PlaceNameChunk tileData;
        synchronized (this.fileLock)
        {
            tileData = readTileData(tile, url);
        }

        if (tileData == null)
        {
            // Assume that something's wrong with the file and delete it.
            this.getDataFileStore().removeFile(url);
            tile.getPlaceNameService().markResourceAbsent(tile.getPlaceNameService().getTileNumber(tile.row,
                tile.column));
            String message = Logging.getMessage("generic.DeletedCorruptDataFile", url);
            Logging.logger().fine(message);
            return false;
        }

        tile.setDataChunk(tileData);
        WorldWind.getMemoryCache(Tile.class.getName()).add(tile.getFileCachePath(), tile);
        return true;
    }

    protected static PlaceNameChunk readTileData(Tile tile, java.net.URL url)
    {
        java.io.InputStream is = null;

        try
        {
            String path = url.getFile();
            path = path.replaceAll("%20", " "); // TODO: find a better way to get a path usable by FileInputStream

            java.io.FileInputStream fis = new java.io.FileInputStream(path);
            java.io.BufferedInputStream buf = new java.io.BufferedInputStream(fis);
            is = new java.util.zip.GZIPInputStream(buf);

            GMLPlaceNameSAXHandler handler = new GMLPlaceNameSAXHandler();
            javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().parse(is, handler);
            return handler.createPlaceNameChunk(tile.getPlaceNameService());
        }
        catch (Exception e)
        {
            //todo log actual error 
            Logging.logger().log(Level.FINE,
                Logging.getMessage("layers.PlaceNameLayer.ExceptionAttemptingToReadFile", url.toString()), e);
        }
        finally
        {
            try
            {
                if (is != null)
                    is.close();
            }
            catch (java.io.IOException e)
            {
                Logging.logger().log(Level.FINE,
                    Logging.getMessage("layers.PlaceNameLayer.ExceptionAttemptingToReadFile", url.toString()), e);
            }
        }

        return null;
    }

    protected static CharBuffer newCharBuffer(int numElements)
    {
        ByteBuffer bb = ByteBuffer.allocateDirect((Character.SIZE / 8) * numElements);
        bb.order(ByteOrder.nativeOrder());
        return bb.asCharBuffer();
    }

    protected static class GMLPlaceNameSAXHandler extends org.xml.sax.helpers.DefaultHandler
    {
        protected static final String GML_FEATURE_MEMBER = "gml:featureMember";
        protected static final String TOPP_FULL_NAME_ND = "topp:full_name_nd";
        protected static final String TOPP_LATITUDE = "topp:latitude";
        protected static final String TOPP_LONGITUDE = "topp:longitude";
        protected final LinkedList<String> internedQNameStack = new LinkedList<String>();
        protected boolean inBeginEndPair = false;
        protected StringBuilder latBuffer = new StringBuilder();
        protected StringBuilder lonBuffer = new StringBuilder();

        StringBuilder textArray = new StringBuilder();
        int[] textIndexArray = new int[16];
        double[] latlonArray = new double[16];
        int numEntries = 0;

        protected GMLPlaceNameSAXHandler()
        {
        }

        protected PlaceNameChunk createPlaceNameChunk(PlaceNameService service)
        {
            int numChars = this.textArray.length();
            CharBuffer textBuffer = newCharBuffer(numChars);
            textBuffer.put(this.textArray.toString());
            textBuffer.rewind();
            return new PlaceNameChunk(service, textBuffer, this.textIndexArray, this.latlonArray, this.numEntries);
        }

        protected void beginEntry()
        {
            int textIndex = this.textArray.length();
            this.textIndexArray = append(this.textIndexArray, this.numEntries, textIndex);
            this.inBeginEndPair = true;
        }

        protected void endEntry()
        {
            double lat = this.parseDouble(this.latBuffer);
            double lon = this.parseDouble(this.lonBuffer);
            int numLatLon = 2 * this.numEntries;
            this.latlonArray = this.append(this.latlonArray, numLatLon, lat);
            numLatLon++;
            this.latlonArray = this.append(this.latlonArray, numLatLon, lon);

            this.latBuffer.delete(0, this.latBuffer.length());
            this.lonBuffer.delete(0, this.lonBuffer.length());
            this.inBeginEndPair = false;
            this.numEntries++;
        }

        protected double parseDouble(StringBuilder sb)
        {
            double value = 0;
            try
            {
                value = Double.parseDouble(sb.toString());
            }
            catch (NumberFormatException e)
            {
                Logging.logger().log(Level.FINE,
                    Logging.getMessage("layers.PlaceNameLayer.ExceptionAttemptingToReadFile", ""), e);
            }
            return value;
        }

        protected int[] append(int[] array, int index, int value)
        {
            if (index >= array.length)
                array = this.resizeArray(array);
            array[index] = value;
            return array;
        }

        protected int[] resizeArray(int[] oldArray)
        {
            int newSize = 2 * oldArray.length;
            int[] newArray = new int[newSize];
            System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
            return newArray;
        }

        protected double[] append(double[] array, int index, double value)
        {
            if (index >= array.length)
                array = this.resizeArray(array);
            array[index] = value;
            return array;
        }

        protected double[] resizeArray(double[] oldArray)
        {
            int newSize = 2 * oldArray.length;
            double[] newArray = new double[newSize];
            System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
            return newArray;
        }

        @SuppressWarnings({"StringEquality"})
        public void characters(char ch[], int start, int length)
        {
            if (!this.inBeginEndPair)
                return;

            // Top of QName stack is an interned string,
            // so we can use pointer comparison.
            String internedTopQName = this.internedQNameStack.getFirst();

            StringBuilder sb = null;
            if (TOPP_LATITUDE == internedTopQName)
                sb = this.latBuffer;
            else if (TOPP_LONGITUDE == internedTopQName)
                sb = this.lonBuffer;
            else if (TOPP_FULL_NAME_ND == internedTopQName)
                sb = this.textArray;

            if (sb != null)
                sb.append(ch, start, length);
        }

        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes)
        {
            // Don't validate uri, localName or attributes because they aren't used.
            // Intern the qName string so we can use pointer comparison.
            String internedQName = qName.intern();
            //noinspection StringEquality
            if (GML_FEATURE_MEMBER == internedQName)
                this.beginEntry();
            this.internedQNameStack.addFirst(internedQName);
        }

        public void endElement(String uri, String localName, String qName)
        {
            // Don't validate uri or localName because they aren't used.
            // Intern the qName string so we can use pointer comparison.
            String internedQName = qName.intern();
            //noinspection StringEquality
            if (GML_FEATURE_MEMBER == internedQName)
                this.endEntry();
            this.internedQNameStack.removeFirst();
        }
    }

    protected void downloadTile(final Tile tile)
    {
        downloadTile(tile, null);
    }

    protected void downloadTile(final Tile tile, DownloadPostProcessor postProcessor)
    {
        if (!this.isNetworkRetrievalEnabled())
            return;

        if (!WorldWind.getRetrievalService().isAvailable())
            return;

        java.net.URL url;
        try
        {
            url = tile.getRequestURL();
            if (WorldWind.getNetworkStatus().isHostUnavailable(url))
                return;
        }
        catch (java.net.MalformedURLException e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE,
                Logging.getMessage("layers.PlaceNameLayer.ExceptionCreatingUrl", tile), e);
            return;
        }

        Retriever retriever;

        if ("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol()))
        {
            if (postProcessor == null)
                postProcessor = new DownloadPostProcessor(this, tile);
            retriever = new HTTPRetriever(url, postProcessor);
            retriever.setValue(URLRetriever.EXTRACT_ZIP_ENTRY, "true"); // supports legacy layers
        }
        else
        {
            Logging.logger().severe(
                Logging.getMessage("layers.PlaceNameLayer.UnknownRetrievalProtocol", url.toString()));
            return;
        }

        // Apply any overridden timeouts.
        Integer cto = AVListImpl.getIntegerValue(this, AVKey.URL_CONNECT_TIMEOUT);
        if (cto != null && cto > 0)
            retriever.setConnectTimeout(cto);
        Integer cro = AVListImpl.getIntegerValue(this, AVKey.URL_READ_TIMEOUT);
        if (cro != null && cro > 0)
            retriever.setReadTimeout(cro);
        Integer srl = AVListImpl.getIntegerValue(this, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
        if (srl != null && srl > 0)
            retriever.setStaleRequestLimit(srl);

        WorldWind.getRetrievalService().runRetriever(retriever, tile.getPriority());
    }

    protected void saveBuffer(java.nio.ByteBuffer buffer, java.io.File outFile) throws java.io.IOException
    {
        synchronized (this.fileLock) // sychronized with read of file in RequestTask.run()
        {
            WWIO.saveBuffer(buffer, outFile);
        }
    }

    protected static class DownloadPostProcessor extends AbstractRetrievalPostProcessor
    {
        protected final PlaceNameLayer layer;
        protected final Tile tile;
        protected final FileStore fileStore;

        public DownloadPostProcessor(PlaceNameLayer layer, Tile tile)
        {
            // No arg check; the class has protected access.
            this(layer, tile, null);
        }

        public DownloadPostProcessor(PlaceNameLayer layer, Tile tile, FileStore fileStore)
        {
            // No arg check; the class has protected access.

            //noinspection RedundantCast
            super((AVList) layer);

            this.layer = layer;
            this.tile = tile;
            this.fileStore = fileStore;
        }

        protected FileStore getFileStore()
        {
            return this.fileStore != null ? this.fileStore : this.layer.getDataFileStore();
        }

        @Override
        protected void markResourceAbsent()
        {
            this.tile.getPlaceNameService().markResourceAbsent(
                this.tile.getPlaceNameService().getTileNumber(this.tile.row, this.tile.column));
        }

        @Override
        protected Object getFileLock()
        {
            return this.layer.fileLock;
        }

        protected File doGetOutputFile()
        {
            return this.getFileStore().newFile(this.tile.getFileCachePath());
        }

        @Override
        protected ByteBuffer handleSuccessfulRetrieval()
        {
            ByteBuffer buffer = super.handleSuccessfulRetrieval();

            if (buffer != null)
            {
                // Fire a property change to denote that the layer's backing data has changed.
                this.layer.firePropertyChange(AVKey.LAYER, null, this);
            }

            return buffer;
        }

        @Override
        protected ByteBuffer handleXMLContent() throws IOException
        {
            // Check for an exception report
            String s = WWIO.byteBufferToString(this.getRetriever().getBuffer(), 1024, null);
            if (s.contains("<ExceptionReport>"))
            {
                // TODO: Parse the xml and include only the message text in the log message.

                StringBuilder sb = new StringBuilder(this.getRetriever().getName());

                sb.append("\n");
                sb.append(WWIO.byteBufferToString(this.getRetriever().getBuffer(), 2048, null));
                Logging.logger().warning(sb.toString());

                return null;
            }

            this.saveBuffer();

            return this.getRetriever().getBuffer();
        }
    }

    // *** Bulk download ***
    // *** Bulk download ***
    // *** Bulk download ***

    /**
     * Start a new {@link BulkRetrievalThread} that downloads all placenames for a given sector and resolution to the
     * current WorldWind file cache.
     * <p>
     * This method creates and starts a thread to perform the download. A reference to the thread is returned. To create
     * a downloader that has not been started, construct a {@link PlaceNameLayerBulkDownloader}.
     * <p>
     * Note that the target resolution must be provided in radians of latitude per texel, which is the resolution in
     * meters divided by the globe radius.
     *
     * @param sector     the sector to download data for.
     * @param resolution the target resolution, provided in radians of latitude per texel.
     * @param listener   an optional retrieval listener. May be null.
     *
     * @return the {@link PlaceNameLayerBulkDownloader} that executes the retrieval.
     *
     * @throws IllegalArgumentException if the sector is null or the resolution is less than zero.
     * @see PlaceNameLayerBulkDownloader
     */
    public BulkRetrievalThread makeLocal(Sector sector, double resolution, BulkRetrievalListener listener)
    {
        PlaceNameLayerBulkDownloader thread = new PlaceNameLayerBulkDownloader(this, sector, resolution,
            this.getDataFileStore(), listener);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /**
     * Start a new {@link BulkRetrievalThread} that downloads all placenames for a given sector and resolution to a
     * specified file store.
     * <p>
     * This method creates and starts a thread to perform the download. A reference to the thread is returned. To create
     * a downloader that has not been started, construct a {@link PlaceNameLayerBulkDownloader}.
     * <p>
     * Note that the target resolution must be provided in radians of latitude per texel, which is the resolution in
     * meters divided by the globe radius.
     *
     * @param sector     the sector to download data for.
     * @param resolution the target resolution, provided in radians of latitude per texel.
     * @param fileStore  the file store in which to place the downloaded elevations. If null the current WorldWind file
     *                   cache is used.
     * @param listener   an optional retrieval listener. May be null.
     *
     * @return the {@link PlaceNameLayerBulkDownloader} that executes the retrieval.
     *
     * @throws IllegalArgumentException if the sector is null or the resolution is less than zero.
     * @see PlaceNameLayerBulkDownloader
     */
    public BulkRetrievalThread makeLocal(Sector sector, double resolution, FileStore fileStore,
        BulkRetrievalListener listener)
    {
        PlaceNameLayerBulkDownloader thread = new PlaceNameLayerBulkDownloader(this, sector, resolution,
            fileStore != null ? fileStore : this.getDataFileStore(), listener);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /**
     * Get the estimated size in bytes of the placenames not in the WorldWind file cache for the given sector and
     * resolution.
     * <p>
     * Note that the target resolution must be provided in radians of latitude per texel, which is the resolution in
     * meters divided by the globe radius.
     *
     * @param sector     the sector to estimate.
     * @param resolution the target resolution, provided in radians of latitude per texel.
     *
     * @return the estimated size in bytes of the missing placenames.
     *
     * @throws IllegalArgumentException if the sector is null or the resolution is less than zero.
     */
    public long getEstimatedMissingDataSize(Sector sector, double resolution)
    {
        return this.getEstimatedMissingDataSize(sector, resolution, this.getDataFileStore());
    }

    /**
     * Get the estimated size in bytes of the placenames not in a specified file store for the given sector and
     * resolution.
     * <p>
     * Note that the target resolution must be provided in radians of latitude per texel, which is the resolution in
     * meters divided by the globe radius.
     *
     * @param sector     the sector to estimate.
     * @param resolution the target resolution, provided in radians of latitude per texel.
     * @param fileStore  the file store to examine. If null the current WorldWind file cache is used.
     *
     * @return the estimated size in byte of the missing placenames.
     *
     * @throws IllegalArgumentException if the sector is null or the resolution is less than zero.
     */
    public long getEstimatedMissingDataSize(Sector sector, double resolution, FileStore fileStore)
    {
        try
        {
            PlaceNameLayerBulkDownloader downloader = new PlaceNameLayerBulkDownloader(this, sector, resolution,
                fileStore != null ? fileStore : this.getDataFileStore(), null);
            return downloader.getEstimatedMissingDataSize();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionDuringDataSizeEstimate", this.getName());
            Logging.logger().severe(message);
            throw new RuntimeException(message);
        }
    }
}

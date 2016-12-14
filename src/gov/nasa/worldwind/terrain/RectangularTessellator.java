/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.terrain;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.nio.*;
import java.util.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: RectangularTessellator.java 2922 2015-03-24 23:56:58Z tgaskins $
 */
public class RectangularTessellator extends WWObjectImpl implements Tessellator
{
    protected static class RenderInfo
    {
        protected final int density;
        protected final Vec4 referenceCenter;
        protected final FloatBuffer vertices;
        protected final FloatBuffer texCoords;
        protected final IntBuffer indices;
        protected long time;
        protected Object vboCacheKey = new Object();
        protected boolean isVboBound = false;

        protected RenderInfo(DrawContext dc, int density, FloatBuffer vertices, Vec4 refCenter)
        {
            //Fill in the buffers and buffer IDs and store them in hash maps by density
            createIndices(density);
            createTextureCoordinates(density);

            //Fill in the member variables from the parameters
            this.density = density;
            this.referenceCenter = refCenter;
            this.vertices = vertices;

            //Fill in the remaining variables from the stored buffers and buffer IDs for easier access
            this.indices = indexLists.get(this.density);
            this.texCoords = textureCoords.get(this.density);
            this.time = System.currentTimeMillis();

            if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
                this.fillVerticesVBO(dc);
        }

        public int getDensity()
        {
            return this.density;
        }

        public Vec4 getReferenceCenter()
        {
            return this.referenceCenter;
        }

        public FloatBuffer getVertices()
        {
            return this.vertices;
        }

        public FloatBuffer getTexCoords()
        {
            return this.texCoords;
        }

        public IntBuffer getIndices()
        {
            return this.indices;
        }

        public long getTime()
        {
            return this.time;
        }

        public Object getVboCacheKey()
        {
            return this.vboCacheKey;
        }

        public boolean isVboBound()
        {
            return this.isVboBound;
        }

        /**
         * Resets this instance's update time to now and refills its VBO if VBOs should be used.
         *
         * @param dc the current draw context.
         */
        protected void update(DrawContext dc)
        {
            this.time = System.currentTimeMillis();

            if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
                this.fillVerticesVBO(dc);
        }

        protected long getSizeInBytes()
        {
            // Texture coordinates are shared among all tiles of the same density, so do not count towards size.
            // 8 references, floats in buffer.
            return 8 * 4 + (this.vertices.limit()) * Float.SIZE / 8;
        }

        protected void fillVerticesVBO(DrawContext dc)
        {
            GL gl = dc.getGL();

            int[] vboIds = (int[]) dc.getGpuResourceCache().get(this.vboCacheKey);
            if (vboIds == null)
            {
                vboIds = new int[1];
                gl.glGenBuffers(vboIds.length, vboIds, 0);
                int size = this.vertices.limit() * 4;
                dc.getGpuResourceCache().put(this.vboCacheKey, vboIds, GpuResourceCache.VBO_BUFFERS, size);
            }

            try
            {
                FloatBuffer vb = this.vertices;
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
                gl.glBufferData(GL.GL_ARRAY_BUFFER, vb.limit() * 4, vb.rewind(), GL.GL_STATIC_DRAW);
            }
            finally
            {
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            }
        }
    }

    protected static class RectTile implements SectorGeometry
    {
        protected final RectangularTessellator tessellator; // not needed if not a static class
        protected final int level;
        protected final Sector sector;
        protected final int density;
        protected final double cellSize;
        protected Extent extent; // extent of sector in object coordinates
        protected RenderInfo ri;

        protected int minColorCode = 0;
        protected int maxColorCode = 0;

        public RectTile(RectangularTessellator tessellator, Extent extent, int level, int density, Sector sector)
        {
            this.tessellator = tessellator;
            this.level = level;
            this.density = density;
            this.sector = sector;
            this.extent = extent;
            this.cellSize = sector.getDeltaLatRadians() / density;
        }

        public Sector getSector()
        {
            return this.sector;
        }

        public Extent getExtent()
        {
            return this.extent;
        }

        public RectangularTessellator getTessellator()
        {
            return tessellator;
        }

        public int getLevel()
        {
            return level;
        }

        public int getDensity()
        {
            return density;
        }

        public double getCellSize()
        {
            return cellSize;
        }

        public RenderInfo getRi()
        {
            return ri;
        }

        public int getMinColorCode()
        {
            return minColorCode;
        }

        public int getMaxColorCode()
        {
            return maxColorCode;
        }

        public void beginRendering(DrawContext dc, int numTextureUnits)
        {
            dc.getView().setReferenceCenter(dc, ri.referenceCenter);
            if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
            {
                if (this.tessellator.bindVbos(dc, this, numTextureUnits))
                    this.ri.isVboBound = true;
            }
        }

        public void endRendering(DrawContext dc)
        {
            if (this.ri.isVboBound)
            {
                dc.getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
                dc.getGL().glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
                this.ri.isVboBound = false;
            }
        }

        public void renderMultiTexture(DrawContext dc, int numTextureUnits)
        {
            this.tessellator.renderMultiTexture(dc, this, numTextureUnits);
        }

        public void renderMultiTexture(DrawContext dc, int numTextureUnits, boolean beginRenderingCalled)
        {
            if (beginRenderingCalled)
            {
                this.tessellator.renderMultiTexture(dc, this, numTextureUnits);
            }
            else
            {
                this.beginRendering(dc, numTextureUnits);
                this.tessellator.renderMultiTexture(dc, this, numTextureUnits);
                this.endRendering(dc);
            }
        }

        public void render(DrawContext dc)
        {
            this.beginRendering(dc, 1);
            this.tessellator.render(dc, this);
            this.endRendering(dc);
        }

        public void render(DrawContext dc, boolean beginRenderingCalled)
        {
            if (beginRenderingCalled)
            {
                this.tessellator.render(dc, this);
            }
            else
            {
                this.beginRendering(dc, 1);
                this.tessellator.render(dc, this);
                this.endRendering(dc);
            }
        }

        public void renderWireframe(DrawContext dc, boolean showTriangles, boolean showTileBoundary)
        {
            this.tessellator.renderWireframe(dc, this, showTriangles, showTileBoundary);
        }

        public void renderBoundingVolume(DrawContext dc)
        {
            this.tessellator.renderBoundingVolume(dc, this);
        }

        public void renderTileID(DrawContext dc)
        {
            this.tessellator.renderTileID(dc, this);
        }

        public PickedObject[] pick(DrawContext dc, List<? extends Point> pickPoints)
        {
            return this.tessellator.pick(dc, this, pickPoints);
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            this.tessellator.pick(dc, this, pickPoint);
        }

        public Vec4 getSurfacePoint(Angle latitude, Angle longitude, double metersOffset)
        {
            return this.tessellator.getSurfacePoint(this, latitude, longitude, metersOffset);
        }

        public double getResolution()
        {
            return this.sector.getDeltaLatRadians() / this.density;
        }

        public Intersection[] intersect(Line line)
        {
            return this.tessellator.intersect(this, line);
        }

        public Intersection[] intersect(double elevation)
        {
            return this.tessellator.intersect(this, elevation);
        }

        public DoubleBuffer makeTextureCoordinates(GeographicTextureCoordinateComputer computer)
        {
            return this.tessellator.makeGeographicTexCoords(this, computer);
        }
    }

    protected static class CacheKey
    {
        protected final Sector sector;
        protected final int density;
        protected final Object globeStateKey;

        public CacheKey(DrawContext dc, Sector sector, int density)
        {
            this.sector = sector;
            this.density = density;
            this.globeStateKey = dc.getGlobe().getStateKey(dc);
        }

        @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            CacheKey cacheKey = (CacheKey) o; // Note: no check of class type equivalence, for performance

            if (density != cacheKey.density)
                return false;
            if (globeStateKey != null ? !globeStateKey.equals(cacheKey.globeStateKey) : cacheKey.globeStateKey != null)
                return false;
            //noinspection RedundantIfStatement
            if (sector != null ? !sector.equals(cacheKey.sector) : cacheKey.sector != null)
                return false;

            return true;
        }

        public int hashCode()
        {
            int result;
            result = (sector != null ? sector.hashCode() : 0);
            result = 31 * result + density;
            result = 31 * result + (globeStateKey != null ? globeStateKey.hashCode() : 0);
            return result;
        }
    }

    protected static class TopLevelTiles
    {
        protected ArrayList<RectTile> topLevels;

        public TopLevelTiles(ArrayList<RectTile> topLevels)
        {
            this.topLevels = topLevels;
        }
    }

    // TODO: Make all this configurable
    protected static final int DEFAULT_MAX_LEVEL = 30;
    protected static final double DEFAULT_LOG10_RESOLUTION_TARGET = 1.3;
    protected static final int DEFAULT_NUM_LAT_SUBDIVISIONS = 3;
    protected static final int DEFAULT_NUM_LON_SUBDIVISIONS = 6;
    protected static final int DEFAULT_DENSITY = 20;
    protected static final String CACHE_NAME = "Terrain";
    protected static final String CACHE_ID = RectangularTessellator.class.getName();

    // Tri-strip indices and texture coordinates. These depend only on density and can therefore be statically cached.
    protected static final HashMap<Integer, FloatBuffer> textureCoords = new HashMap<Integer, FloatBuffer>();
    protected static final HashMap<Integer, IntBuffer> indexLists = new HashMap<Integer, IntBuffer>();
    protected static final HashMap<Integer, ByteBuffer> oddRowColorList = new HashMap<Integer, ByteBuffer>();
    protected static final HashMap<Integer, ByteBuffer> evenRowColorList = new HashMap<Integer, ByteBuffer>();

    protected static final HashMap<Integer, Object> textureCoordVboCacheKeys = new HashMap<Integer, Object>();
    protected static final HashMap<Integer, Object> indexListsVboCacheKeys = new HashMap<Integer, Object>();

    protected int numLevel0LatSubdivisions = DEFAULT_NUM_LAT_SUBDIVISIONS;
    protected int numLevel0LonSubdivisions = DEFAULT_NUM_LON_SUBDIVISIONS;
    protected SessionCache topLevelTilesCache = new BasicSessionCache(3);
    protected PickSupport pickSupport = new PickSupport();
    protected SectorGeometryList currentTiles = new SectorGeometryList();
    protected Frustum currentFrustum;
    protected Sector currentCoverage; // union of all tiles selected during call to render()
    protected boolean makeTileSkirts = true;
    protected int currentLevel;
    protected int maxLevel = DEFAULT_MAX_LEVEL;
    protected Globe globe;
    protected int density = DEFAULT_DENSITY;
    protected long updateFrequency = 2000; // milliseconds

    public SectorGeometryList tessellate(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getView() == null)
        {
            String msg = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        if (!WorldWind.getMemoryCacheSet().containsCache(CACHE_ID))
        {
            long size = Configuration.getLongValue(AVKey.SECTOR_GEOMETRY_CACHE_SIZE, 10000000L);
            MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
            cache.setName(CACHE_NAME);
            WorldWind.getMemoryCacheSet().addCache(CACHE_ID, cache);
        }

        this.maxLevel = Configuration.getIntegerValue(AVKey.RECTANGULAR_TESSELLATOR_MAX_LEVEL, DEFAULT_MAX_LEVEL);

        TopLevelTiles topLevels = (TopLevelTiles) this.topLevelTilesCache.get(dc.getGlobe().getStateKey(dc));
        if (topLevels == null)
        {
            topLevels = new TopLevelTiles(this.createTopLevelTiles(dc));
            this.topLevelTilesCache.put(dc.getGlobe().getStateKey(dc), topLevels);
        }

        this.currentTiles.clear();
        this.currentLevel = 0;
        this.currentCoverage = null;

        this.currentFrustum = dc.getView().getFrustumInModelCoordinates();
        for (RectTile tile : topLevels.topLevels)
        {
            this.selectVisibleTiles(dc, tile);
        }

        this.currentTiles.setSector(this.currentCoverage);

        for (SectorGeometry tile : this.currentTiles)
        {
            this.makeVerts(dc, (RectTile) tile);
        }

        // Make a copy of the SGL because the tessellator may be called multiple times per frame with a different globe.
        // See SceneController2D.
        SectorGeometryList sgl = new SectorGeometryList(this.currentTiles);
        sgl.setSector(this.currentTiles.getSector());
        return sgl;
    }

    protected ArrayList<RectTile> createTopLevelTiles(DrawContext dc)
    {
        ArrayList<RectTile> tops =
            new ArrayList<RectTile>(this.numLevel0LatSubdivisions * this.numLevel0LonSubdivisions);

        this.globe = dc.getGlobe();
        double deltaLat = 180d / this.numLevel0LatSubdivisions;
        double deltaLon = 360d / this.numLevel0LonSubdivisions;
        Angle lastLat = Angle.NEG90;

        for (int row = 0; row < this.numLevel0LatSubdivisions; row++)
        {
            Angle lat = lastLat.addDegrees(deltaLat);
            if (lat.getDegrees() + 1d > 90d)
                lat = Angle.POS90;

            Angle lastLon = Angle.NEG180;

            for (int col = 0; col < this.numLevel0LonSubdivisions; col++)
            {
                Angle lon = lastLon.addDegrees(deltaLon);
                if (lon.getDegrees() + 1d > 180d)
                    lon = Angle.POS180;

                Sector tileSector = new Sector(lastLat, lat, lastLon, lon);
                boolean skipTile = dc.is2DGlobe() && this.skipTile(dc, tileSector);

                if (!skipTile)
                {
                    tops.add(this.createTile(dc, tileSector, 0));
                }

                lastLon = lon;
            }
            lastLat = lat;
        }

        return tops;
    }

    /**
     * Determines whether a tile is within a 2D globe's projection limits.
     *
     * @param dc     the current draw context. The globe contained in the context must be a {@link
     *               gov.nasa.worldwind.globes.Globe2D}.
     * @param sector the tile's sector.
     *
     * @return <code>true</code> if the tile should be skipped -- it's outside the globe's projection limits --
     * otherwise <code>false</code>.
     */
    protected boolean skipTile(DrawContext dc, Sector sector)
    {
        Sector limits = ((Globe2D) dc.getGlobe()).getProjection().getProjectionLimits();
        if (limits == null || limits.equals(Sector.FULL_SPHERE))
            return false;

        return !sector.intersectsInterior(limits);
    }

    protected RectTile createTile(DrawContext dc, Sector tileSector, int level)
    {
        Extent extent = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), tileSector);

        return new RectTile(this, extent, level, this.density, tileSector);
    }

    public boolean isMakeTileSkirts()
    {
        return makeTileSkirts;
    }

    public void setMakeTileSkirts(boolean makeTileSkirts)
    {
        this.makeTileSkirts = makeTileSkirts;
    }

    public long getUpdateFrequency()
    {
        return this.updateFrequency;
    }

    public void setUpdateFrequency(long updateFrequency)
    {
        this.updateFrequency = updateFrequency;
    }

    protected void selectVisibleTiles(DrawContext dc, RectTile tile)
    {
        if (dc.is2DGlobe() && this.skipTile(dc, tile.getSector()))
            return;

        Extent extent = tile.getExtent();
        if (extent != null && !extent.intersects(this.currentFrustum))
            return;

        if (this.currentLevel < this.maxLevel - 1 && !this.atBestResolution(dc, tile) && this.needToSplit(dc, tile))
        {
            ++this.currentLevel;
            RectTile[] subtiles = this.split(dc, tile);
            for (RectTile child : subtiles)
            {
                this.selectVisibleTiles(dc, child);
            }
            --this.currentLevel;
            return;
        }
        this.currentCoverage = tile.getSector().union(this.currentCoverage);
        this.currentTiles.add(tile);
    }

    protected boolean atBestResolution(DrawContext dc, RectTile tile)
    {
        double bestResolution = dc.getGlobe().getElevationModel().getBestResolution(tile.getSector());

        return tile.getCellSize() <= bestResolution;
    }

    protected boolean needToSplit(DrawContext dc, RectTile tile)
    {
        // Compute the height in meters of a cell from the specified tile. Take care to convert from the radians to
        // meters by multiplying by the globe's radius, not the length of a Cartesian point. Using the length of a
        // Cartesian point is incorrect when the globe is flat.
        double cellSizeRadians = tile.getCellSize();
        double cellSizeMeters = dc.getGlobe().getRadius() * cellSizeRadians;

        // Compute the level of detail scale and the field of view scale. These scales are multiplied by the eye
        // distance to derive a scaled distance that is then compared to the cell size. The level of detail scale is
        // specified as a power of 10. For example, a detail factor of 3 means split when the cell size becomes more
        // than one thousandth of the eye distance. The field of view scale is specified as a ratio between the current
        // field of view and a the default field of view. In a perspective projection, decreasing the field of view by
        // 50% has the same effect on object size as decreasing the distance between the eye and the object by 50%.
        // The detail hint is reduced by 50% for tiles above 75 degrees north and below 75 degrees south.
        double s = this.computeTileResolutionTarget(dc, tile);
        if (tile.getSector().getMinLatitude().degrees >= 75 || tile.getSector().getMaxLatitude().degrees <= -75)
            s *= 0.5;
        double detailScale = Math.pow(10, -s);
        double fieldOfViewScale = dc.getView().getFieldOfView().tanHalfAngle() / Angle.fromDegrees(45).tanHalfAngle();
        fieldOfViewScale = WWMath.clamp(fieldOfViewScale, 0, 1);

        // Compute the distance between the eye point and the sector in meters, and compute a fraction of that distance
        // by multiplying the actual distance by the level of detail scale and the field of view scale.
        double eyeDistanceMeters = tile.getSector().distanceTo(dc, dc.getView().getEyePoint());
        double scaledEyeDistanceMeters = eyeDistanceMeters * detailScale * fieldOfViewScale;

        // Split when the cell size in meters becomes greater than the specified fraction of the eye distance, also in
        // meters. Another way to say it is, use the current tile if its cell size is less than the specified fraction
        // of the eye distance.
        //
        // NOTE: It's tempting to instead compare a screen pixel size to the cell size, but that calculation is
        // window-size dependent and results in selecting an excessive number of tiles when the window is large.
        return cellSizeMeters > scaledEyeDistanceMeters;
    }

    protected double computeTileResolutionTarget(DrawContext dc, RectTile tile)
    {
        // Compute the log10 detail target for the specified tile. Apply the elevation model's detail hint to the
        // default detail target.

        return DEFAULT_LOG10_RESOLUTION_TARGET + dc.getGlobe().getElevationModel().getDetailHint(tile.sector);
    }

    protected RectTile[] split(DrawContext dc, RectTile tile)
    {
        Sector[] sectors = tile.sector.subdivide();

        RectTile[] subTiles = new RectTile[4];
        subTiles[0] = this.createTile(dc, sectors[0], tile.level + 1);
        subTiles[1] = this.createTile(dc, sectors[1], tile.level + 1);
        subTiles[2] = this.createTile(dc, sectors[2], tile.level + 1);
        subTiles[3] = this.createTile(dc, sectors[3], tile.level + 1);

        return subTiles;
    }

    protected RectangularTessellator.CacheKey createCacheKey(DrawContext dc, RectTile tile)
    {
        return new CacheKey(dc, tile.sector, tile.density);
    }

    protected void makeVerts(DrawContext dc, RectTile tile)
    {
        // First see if the vertices have been previously computed and are in the cache. Since the elevation model
        // contents can change between frames, regenerate and re-cache vertices every second.
        MemoryCache cache = WorldWind.getMemoryCache(CACHE_ID);
        CacheKey cacheKey = this.createCacheKey(dc, tile);
        tile.ri = (RenderInfo) cache.getObject(cacheKey);
        if (tile.ri != null && tile.ri.time >= System.currentTimeMillis() - this.getUpdateFrequency())
            return;

        if (this.buildVerts(dc, tile, this.makeTileSkirts))
            cache.add(cacheKey, tile.ri, tile.ri.getSizeInBytes());
    }

    public boolean buildVerts(DrawContext dc, RectTile tile, boolean makeSkirts)
    {
        int density = tile.density;
        int numVertices = (density + 3) * (density + 3);

        FloatBuffer verts;

        //Re-use the RenderInfo vertices buffer. If it has not been set or the density has changed, create a new buffer
        if (tile.ri == null || tile.ri.vertices == null || density != tile.ri.density)
        {
            verts = Buffers.newDirectFloatBuffer(numVertices * 3);
        }
        else
        {
            verts = tile.ri.vertices;
            verts.rewind();
        }

        ArrayList<LatLon> latlons = this.computeLocations(tile);
        double[] elevations = new double[latlons.size()];
        dc.getGlobe().getElevations(tile.sector, latlons, tile.getResolution(), elevations);

        double verticalExaggeration = dc.getVerticalExaggeration();

        // When making skirts, apply vertical exaggeration to the skirt depth only if the exaggeration is 0 or less. If
        // applied to positive exaggerations, the skirt base might rise above the terrain at positive elevations if the
        // minimum globe elevation is not uniform over the globe. For example, a globe may hold only a local elevation
        // model that does not span the globe, making elevations outside the local elevation model 0. If the minimum
        // elevation of the local elevation model is above zero, and the globe reports that minimum as the globe's
        // minimum, then exaggeration will push the skirt bases above 0. That the globe reports a minimum elevation that
        // is not its true minimum is a bug, and this constraint on applying exaggeration to the minimum here is a
        // workaround for that bug. See WWJINT-435.
        Double exaggeratedMinElevation = makeSkirts ? globe.getMinElevation() : null;
        if (exaggeratedMinElevation != null && (exaggeratedMinElevation < 0 || verticalExaggeration <= 0))
            exaggeratedMinElevation *= verticalExaggeration;

        LatLon centroid = tile.sector.getCentroid();
        Vec4 refCenter = globe.computePointFromPosition(centroid.getLatitude(), centroid.getLongitude(), 0d);

        int ie = 0;
        int iv = 0;
        Iterator<LatLon> latLonIter = latlons.iterator();
        for (int j = 0; j <= density + 2; j++)
        {
            for (int i = 0; i <= density + 2; i++)
            {
                LatLon latlon = latLonIter.next();
                double elevation = verticalExaggeration * elevations[ie++];

                // Tile edges use min elevation to draw the skirts
                if (exaggeratedMinElevation != null &&
                    (j == 0 || j >= tile.density + 2 || i == 0 || i >= tile.density + 2))
                    elevation = exaggeratedMinElevation;

                Vec4 p = globe.computePointFromPosition(latlon.getLatitude(), latlon.getLongitude(), elevation);
                verts.put(iv++, (float) (p.x - refCenter.x));
                verts.put(iv++, (float) (p.y - refCenter.y));
                verts.put(iv++, (float) (p.z - refCenter.z));
            }
        }

        verts.rewind();

        if (tile.ri != null)
        {
            tile.ri.update(dc);
            return false;
        }

        tile.ri = new RenderInfo(dc, density, verts, refCenter);
        return true;
    }

    protected ArrayList<LatLon> computeLocations(RectTile tile)
    {
        int density = tile.density;
        int numVertices = (density + 3) * (density + 3);

        Angle latMax = tile.sector.getMaxLatitude();
        Angle dLat = tile.sector.getDeltaLat().divide(density);
        Angle lat = tile.sector.getMinLatitude();

        Angle lonMin = tile.sector.getMinLongitude();
        Angle lonMax = tile.sector.getMaxLongitude();
        Angle dLon = tile.sector.getDeltaLon().divide(density);

        ArrayList<LatLon> latlons = new ArrayList<LatLon>(numVertices);
        for (int j = 0; j <= density + 2; j++)
        {
            Angle lon = lonMin;
            for (int i = 0; i <= density + 2; i++)
            {
                latlons.add(new LatLon(lat, lon));

                if (i > density)
                    lon = lonMax;
                else if (i != 0)
                    lon = lon.add(dLon);

                if (lon.degrees < -180)
                    lon = Angle.NEG180;
                else if (lon.degrees > 180)
                    lon = Angle.POS180;
            }

            if (j > density)
                lat = latMax;
            else if (j != 0)
                lat = lat.add(dLat);
        }

        return latlons;
    }

    protected void renderMultiTexture(DrawContext dc, RectTile tile, int numTextureUnits)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (numTextureUnits < 1)
        {
            String msg = Logging.getMessage("generic.NumTextureUnitsLessThanOne");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.render(dc, tile, numTextureUnits);
    }

    protected void render(DrawContext dc, RectTile tile)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.render(dc, tile, 1);
    }

    public void beginRendering(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glPushClientAttrib(GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

        // Tiles don't push their reference center, they set it, so push the reference center once here so it can be
        // restored later, in endRendering.
        dc.getView().pushReferenceCenter(dc, Vec4.ZERO);
    }

    public void endRendering(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        dc.getView().popReferenceCenter(dc);
        gl.glPopClientAttrib();
    }

    protected long render(DrawContext dc, RectTile tile, int numTextureUnits)
    {
        if (tile.ri == null)
        {
            String msg = Logging.getMessage("nullValue.RenderInfoIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
        {
            if (!this.renderVBO(dc, tile, numTextureUnits))
            {
                // Fall back to VA rendering. This is an error condition at this point because something went wrong with
                // VBO fill or binding. But we can still probably draw the tile using vertex arrays.
                dc.getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
                dc.getGL().glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
                this.renderVA(dc, tile, numTextureUnits);
            }
        }
        else
        {
            this.renderVA(dc, tile, numTextureUnits);
        }

        return tile.ri.indices.limit() - 2; // return number of triangles rendered
    }

    protected void renderVA(DrawContext dc, RectTile tile, int numTextureUnits)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glVertexPointer(3, GL.GL_FLOAT, 0, tile.ri.vertices.rewind());

        for (int i = 0; i < numTextureUnits; i++)
        {
            gl.glClientActiveTexture(GL2.GL_TEXTURE0 + i);
            gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
            Object texCoords = dc.getValue(AVKey.TEXTURE_COORDINATES);
            if (texCoords != null && texCoords instanceof DoubleBuffer)
                gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, ((DoubleBuffer) texCoords).rewind());
            else
                gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, tile.ri.texCoords.rewind());
        }

        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, tile.ri.indices.limit(), GL.GL_UNSIGNED_INT, tile.ri.indices.rewind());
    }

    protected boolean renderVBO(DrawContext dc, RectTile tile, int numTextureUnits)
    {
        if (tile.ri.isVboBound || this.bindVbos(dc, tile, numTextureUnits))
        {
            // Render the tile
            dc.getGL().glDrawElements(GL.GL_TRIANGLE_STRIP, tile.ri.indices.limit(), GL.GL_UNSIGNED_INT, 0);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected boolean bindVbos(DrawContext dc, RectTile tile, int numTextureUnits)
    {
        int[] verticesVboId = (int[]) dc.getGpuResourceCache().get(tile.ri.vboCacheKey);
        if (verticesVboId == null)
        {
            tile.ri.fillVerticesVBO(dc);
            verticesVboId = (int[]) dc.getGpuResourceCache().get(tile.ri.vboCacheKey);
            if (verticesVboId == null)
                return false;
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Bind vertices
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, verticesVboId[0]);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);

        // Bind texture coordinates
        if (numTextureUnits > 0)
        {
            Object texCoordsVboCacheKey = textureCoordVboCacheKeys.get(tile.density);
            int[] texCoordsVboId = (int[])
                (texCoordsVboCacheKey != null ? dc.getGpuResourceCache().get(texCoordsVboCacheKey) : null);
            if (texCoordsVboId == null)
                texCoordsVboId = this.fillTextureCoordsVbo(dc, tile.density, tile.ri.texCoords);
            for (int i = 0; i < numTextureUnits; i++)
            {
                gl.glClientActiveTexture(GL2.GL_TEXTURE0 + i);
                gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);

                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, texCoordsVboId[0]);
                gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);
            }
        }

        // Bind index list
        Object indexListVboCacheKey = indexListsVboCacheKeys.get(tile.density);
        int[] indexListVboId = (int[])
            (indexListVboCacheKey != null ? dc.getGpuResourceCache().get(indexListVboCacheKey) : null);
        if (indexListVboId == null)
            indexListVboId = this.fillIndexListVbo(dc, tile.density, tile.ri.indices);
        if (indexListVboId != null)
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexListVboId[0]);

        return indexListVboId != null;
    }

    protected int[] fillIndexListVbo(DrawContext dc, int density, IntBuffer indices)
    {
        GL gl = dc.getGL();

        Object indexListVboCacheKey = indexListsVboCacheKeys.get(density);
        int[] indexListVboId = (int[])
            (indexListVboCacheKey != null ? dc.getGpuResourceCache().get(indexListVboCacheKey) : null);
        if (indexListVboId == null)
        {
            indexListVboId = new int[1];
            gl.glGenBuffers(indexListVboId.length, indexListVboId, 0);

            if (indexListVboCacheKey == null)
            {
                indexListVboCacheKey = new Object();
                indexListsVboCacheKeys.put(density, indexListVboCacheKey);
            }

            int size = indices.limit() * 4;
            dc.getGpuResourceCache().put(indexListVboCacheKey, indexListVboId, GpuResourceCache.VBO_BUFFERS, size);
        }

        try
        {
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexListVboId[0]);
            gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indices.limit() * 4, indices.rewind(), GL.GL_STATIC_DRAW);
        }
        finally
        {
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        return indexListVboId;
    }

    protected int[] fillTextureCoordsVbo(DrawContext dc, int density, FloatBuffer texCoords)
    {
        GL gl = dc.getGL();

        Object texCoordVboCacheKey = textureCoordVboCacheKeys.get(density);
        int[] texCoordVboId = (int[])
            (texCoordVboCacheKey != null ? dc.getGpuResourceCache().get(texCoordVboCacheKey) : null);
        if (texCoordVboId == null)
        {
            texCoordVboId = new int[1];
            gl.glGenBuffers(texCoordVboId.length, texCoordVboId, 0);

            if (texCoordVboCacheKey == null)
            {
                texCoordVboCacheKey = new Object();
                textureCoordVboCacheKeys.put(density, texCoordVboCacheKey);
            }

            int size = texCoords.limit() * 4;
            dc.getGpuResourceCache().put(texCoordVboCacheKey, texCoordVboId, GpuResourceCache.VBO_BUFFERS, size);
        }

        try
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, texCoordVboId[0]);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, texCoords.limit() * 4, texCoords.rewind(), GL.GL_STATIC_DRAW);
        }
        finally
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }

        return texCoordVboId;
    }

    protected void renderWireframe(DrawContext dc, RectTile tile, boolean showTriangles, boolean showTileBoundary)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (tile.ri == null)
        {
            String msg = Logging.getMessage("nullValue.RenderInfoIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        dc.getView().pushReferenceCenter(dc, tile.ri.referenceCenter);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glPushAttrib(
            GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_POLYGON_BIT | GL2.GL_ENABLE_BIT | GL2.GL_CURRENT_BIT);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);
        gl.glColor4d(1d, 1d, 1d, 0.2);
        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);

        if (showTriangles)
        {
            OGLStackHandler ogsh = new OGLStackHandler();

            try
            {
                ogsh.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);

                gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

                gl.glVertexPointer(3, GL.GL_FLOAT, 0, tile.ri.vertices.rewind());
                gl.glDrawElements(GL.GL_TRIANGLE_STRIP, tile.ri.indices.limit(),
                    GL.GL_UNSIGNED_INT, tile.ri.indices.rewind());
            }
            finally
            {
                ogsh.pop(gl);
            }
        }

        dc.getView().popReferenceCenter(dc);

        gl.glPopAttrib();

        if (showTileBoundary)
            this.renderPatchBoundary(dc, tile);
    }

    protected void renderPatchBoundary(DrawContext dc, RectTile tile)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();

        ogsh.pushAttrib(gl, GL2.GL_ENABLE_BIT | GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT);
        try
        {
            gl.glDisable(GL.GL_BLEND);

            // Don't perform depth clipping but turn on backface culling
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);

            Vec4[] corners = tile.sector.computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration());

            gl.glColor4d(1d, 0, 0, 1d);
            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex3d(corners[0].x, corners[0].y, corners[0].z);
            gl.glVertex3d(corners[1].x, corners[1].y, corners[1].z);
            gl.glVertex3d(corners[2].x, corners[2].y, corners[2].z);
            gl.glVertex3d(corners[3].x, corners[3].y, corners[3].z);
            gl.glEnd();
        }
        finally
        {
            ogsh.pop(gl);
        }
    }

    protected void renderBoundingVolume(DrawContext dc, RectTile tile)
    {
        Extent extent = tile.getExtent();
        if (extent == null)
            return;

        if (extent instanceof Renderable)
            ((Renderable) extent).render(dc);
    }

    protected void renderTileID(DrawContext dc, RectTile tile)
    {
        java.awt.Rectangle viewport = dc.getView().getViewport();
        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            java.awt.Font.decode("Arial-Plain-15"));

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();

        try
        {
            ogsh.pushAttrib(gl, GL2.GL_ENABLE_BIT);

            dc.getGL().glDisable(GL.GL_DEPTH_TEST);
            dc.getGL().glDisable(GL.GL_BLEND);

            textRenderer.beginRendering(viewport.width, viewport.height);
            textRenderer.setColor(Color.RED);
            String tileLabel = Integer.toString(tile.level);
            double[] elevs = this.globe.getMinAndMaxElevations(tile.getSector());
            if (elevs != null)
                tileLabel += ", " + (int) elevs[0] + "/" + (int) elevs[1];

            LatLon ll = tile.getSector().getCentroid();
            Vec4 pt = dc.getGlobe().computePointFromPosition(ll.getLatitude(), ll.getLongitude(),
                dc.getGlobe().getElevation(ll.getLatitude(), ll.getLongitude()));
            pt = dc.getView().project(pt);
            textRenderer.draw(tileLabel, (int) pt.x, (int) pt.y);
            textRenderer.setColor(Color.WHITE);
            textRenderer.endRendering();
        }
        finally
        {
            ogsh.pop(gl);
        }
    }

    protected PickedObject[] pick(DrawContext dc, RectTile tile, List<? extends Point> pickPoints)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (pickPoints == null)
        {
            String msg = Logging.getMessage("nullValue.PickPointList");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (pickPoints.size() == 0)
            return null;

        if (tile.ri == null || tile.ri.vertices == null)
            return null;

        PickedObject[] pos = new PickedObject[pickPoints.size()];
        this.renderTrianglesWithUniqueColors(dc, tile);
        for (int i = 0; i < pickPoints.size(); i++)
        {
            pos[i] = this.resolvePick(dc, tile, pickPoints.get(i));
        }

        return pos;
    }

    protected void pick(DrawContext dc, RectTile tile, Point pickPoint)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (tile.ri == null || tile.ri.vertices == null)
            return;

        renderTrianglesWithUniqueColors(dc, tile);
        PickedObject po = this.resolvePick(dc, tile, pickPoint);
        if (po != null)
            dc.addPickedObject(po);
    }

    /**
     * Render each triangle of a tile in a unique color. Used during picking to identify triangles at the pick points.
     * <p/>
     * Note: This method modifies the GL_VERTEX_ARRAY and GL_COLOR_ARRAY state and does not restore it. Callers should
     * ensure that GL_CLIENT_VERTEX_ARRAY_BIT has been pushed, and eventually pop it when done using this method.
     *
     * @param dc   the current draw context.
     * @param tile the tile to render.
     */
    protected void renderTrianglesWithUniqueColors(DrawContext dc, RectTile tile)
    {
        //Fill the color buffers each frame with unique colors
        int sideSize = density + 2;
        int trianglesPerRow = sideSize * 2 + 4;
        int indexCount = 2 * sideSize * sideSize + 4 * sideSize - 2;
        int trianglesNum = indexCount - 2;
        int numVertices = (density + 3) * (density + 3);
        int verticesSize = numVertices * 3;

        ByteBuffer colorsOdd;
        ByteBuffer colorsEven;

        //Reuse the old color buffers if possible
        if (oddRowColorList.containsKey(density) && evenRowColorList.containsKey(density))
        {
            colorsOdd = oddRowColorList.get(density);
            colorsEven = evenRowColorList.get(density);
        }
        else
        {
            //Otherwise create new buffers
            colorsOdd = Buffers.newDirectByteBuffer(verticesSize);
            colorsEven = Buffers.newDirectByteBuffer(verticesSize);

            oddRowColorList.put(density, colorsOdd);
            evenRowColorList.put(density, colorsEven);
        }

        tile.minColorCode = dc.getUniquePickColor().getRGB();

        int prevPos = -1;
        int pos;

        for (int i = 0; i < trianglesNum; i++)
        {
            java.awt.Color color = dc.getUniquePickColor();

            //NOTE: Get the indices for the last point for the triangle (i+2).
            // The color of this point is used to fill the entire triangle with flat shading.
            pos = 3 * tile.ri.indices.get(i + 2);

            //Since we are using a single triangle strip for all rows, we need to store the colors in alternate rows.
            // (The same vertices are used in both directions, however, we need different colors for those vertices)
            if (pos > prevPos)
            {
                colorsOdd.position(pos);
                colorsOdd.put((byte) color.getRed()).put((byte) color.getGreen()).put((byte) color.getBlue());
            }
            else if (pos < prevPos)
            {
                colorsEven.position(pos);
                colorsEven.put((byte) color.getRed()).put((byte) color.getGreen()).put((byte) color.getBlue());
            }

            prevPos = pos;
        }

        tile.maxColorCode = dc.getUniquePickColor().getRGB();

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        try
        {
            if (null != tile.ri.referenceCenter)
                dc.getView().pushReferenceCenter(dc, tile.ri.referenceCenter);

            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);

            // If using VBOs, bind the vertices VBO and the indices VBO but not the tex coords VBOs.
            if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject() && this.bindVbos(dc, tile, 0))
            {
                // VBOs are not used for the colors since they change every frame.
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

                //Draw the odd rows
                gl.glColorPointer(3, GL.GL_UNSIGNED_BYTE, 0, colorsOdd.rewind());
                for (int i = 0; i < sideSize; i += 2)
                {
                    gl.glDrawElements(GL.GL_TRIANGLE_STRIP, trianglesPerRow,
                        GL.GL_UNSIGNED_INT, trianglesPerRow * i * 4);
                }

                //Draw the even rows
                gl.glColorPointer(3, GL.GL_UNSIGNED_BYTE, 0, colorsEven.rewind());
                for (int i = 1; i < sideSize - 1; i += 2)
                {
                    gl.glDrawElements(GL.GL_TRIANGLE_STRIP, trianglesPerRow,
                        GL.GL_UNSIGNED_INT, trianglesPerRow * i * 4);
                }
            }
            else
            {
                gl.glVertexPointer(3, GL.GL_FLOAT, 0, tile.ri.vertices.rewind());

                //Draw the odd rows
                gl.glColorPointer(3, GL.GL_UNSIGNED_BYTE, 0, colorsOdd.rewind());
                for (int i = 0; i < sideSize; i += 2)
                {
                    gl.glDrawElements(GL.GL_TRIANGLE_STRIP, trianglesPerRow,
                        GL.GL_UNSIGNED_INT, tile.ri.indices.position(trianglesPerRow * i));
                }

                //Draw the even rows
                gl.glColorPointer(3, GL.GL_UNSIGNED_BYTE, 0, colorsEven.rewind());
                for (int i = 1; i < sideSize - 1; i += 2)
                {
                    gl.glDrawElements(GL.GL_TRIANGLE_STRIP, trianglesPerRow,
                        GL.GL_UNSIGNED_INT, tile.ri.indices.position(trianglesPerRow * i));
                }
            }
        }
        finally
        {
            if (null != tile.ri.referenceCenter)
                dc.getView().popReferenceCenter(dc);
        }
    }

    protected PickedObject resolvePick(DrawContext dc, RectTile tile, Point pickPoint)
    {
        int colorCode = this.pickSupport.getTopColor(dc, pickPoint);
        if (colorCode < tile.minColorCode || colorCode > tile.maxColorCode)
            return null;

        double EPSILON = (double) 0.00001f;

        int triangleIndex = colorCode - tile.minColorCode - 1;

        if (tile.ri.indices == null || triangleIndex >= (tile.ri.indices.capacity() - 2))
            return null;

        double centerX = tile.ri.referenceCenter.x;
        double centerY = tile.ri.referenceCenter.y;
        double centerZ = tile.ri.referenceCenter.z;

        int[] indices = new int[3];
        tile.ri.indices.position(triangleIndex);
        tile.ri.indices.get(indices);

        float[] coords = new float[3];
        tile.ri.vertices.position(3 * indices[0]);
        tile.ri.vertices.get(coords);
        Vec4 v0 = new Vec4(coords[0] + centerX, coords[1] + centerY, coords[2] + centerZ);

        tile.ri.vertices.position(3 * indices[1]);
        tile.ri.vertices.get(coords);
        Vec4 v1 = new Vec4(coords[0] + centerX, coords[1] + centerY, coords[2] + centerZ);

        tile.ri.vertices.position(3 * indices[2]);
        tile.ri.vertices.get(coords);
        Vec4 v2 = new Vec4(coords[0] + centerX, coords[1] + centerY, coords[2] + centerZ);

        // get triangle edge vectors and plane normal
        Vec4 e1 = v1.subtract3(v0);
        Vec4 e2 = v2.subtract3(v0);
        Vec4 N = e1.cross3(e2);  // if N is 0, the triangle is degenerate, we are not dealing with it

        Line ray = dc.getView().computeRayFromScreenPoint(pickPoint.getX(), pickPoint.getY());

        Vec4 w0 = ray.getOrigin().subtract3(v0);
        double a = -N.dot3(w0);
        double b = N.dot3(ray.getDirection());
        if (java.lang.Math.abs(b) < EPSILON) // ray is parallel to triangle plane
            return null;                    // if a == 0 , ray lies in triangle plane
        double r = a / b;

        Vec4 intersect = ray.getOrigin().add3(ray.getDirection().multiply3(r));
        Position pp = dc.getGlobe().computePositionFromPoint(intersect);

        // Draw the elevation from the elevation model, not the geode.
        double elev = dc.getGlobe().getElevation(pp.getLatitude(), pp.getLongitude());
        elev *= dc.getVerticalExaggeration();
        Position p = new Position(pp.getLatitude(), pp.getLongitude(), elev);

        return new PickedObject(pickPoint, colorCode, p, pp.getLatitude(), pp.getLongitude(), elev, true);
    }

    /**
     * Determines if and where a ray intersects a <code>RectTile</code> geometry.
     *
     * @param tile the <Code>RectTile</code> which geometry is to be tested for intersection.
     * @param line the ray for which an intersection is to be found.
     *
     * @return an array of <code>Intersection</code> sorted by increasing distance from the line origin, or null if no
     * intersection was found.
     */
    protected Intersection[] intersect(RectTile tile, Line line)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (tile.ri.vertices == null)
            return null;

        // Compute 'vertical' plane perpendicular to the ground, that contains the ray
        Plane verticalPlane = null;
        Plane horizontalPlane = null;
        double effectiveRadiusVertical = Double.MAX_VALUE;
        double effectiveRadiusHorizontal = Double.MAX_VALUE;
        Vec4 surfaceNormal = globe.computeSurfaceNormalAtPoint(line.getOrigin());
        if (Math.abs(line.getDirection().normalize3().dot3(surfaceNormal)) < 1.0) // if not colinear
        {
            Vec4 normalV = line.getDirection().cross3(globe.computeSurfaceNormalAtPoint(line.getOrigin()));
            verticalPlane = new Plane(normalV.x(), normalV.y(), normalV.z(), -line.getOrigin().dot3(normalV));
            if (!tile.getExtent().intersects(verticalPlane))
                return null;

            // Compute 'horizontal' plane perpendicular to the vertical plane, that contains the ray
            Vec4 normalH = line.getDirection().cross3(normalV);
            horizontalPlane = new Plane(normalH.x(), normalH.y(), normalH.z(), -line.getOrigin().dot3(normalH));
            if (!tile.getExtent().intersects(horizontalPlane))
                return null;

            // Compute maximum cell size based on tile delta lat, density and globe radius
            effectiveRadiusVertical = tile.extent.getEffectiveRadius(verticalPlane);
            effectiveRadiusHorizontal = tile.extent.getEffectiveRadius(horizontalPlane);
        }

        Intersection[] hits;
        ArrayList<Intersection> list = new ArrayList<Intersection>();

        int[] indices = new int[tile.ri.indices.limit()];
        float[] coords = new float[tile.ri.vertices.limit()];
        tile.ri.indices.rewind();
        tile.ri.vertices.rewind();
        tile.ri.indices.get(indices, 0, indices.length);
        tile.ri.vertices.get(coords, 0, coords.length);
        tile.ri.indices.rewind();
        tile.ri.vertices.rewind();

        int trianglesNum = tile.ri.indices.capacity() - 2;
        double centerX = tile.ri.referenceCenter.x;
        double centerY = tile.ri.referenceCenter.y;
        double centerZ = tile.ri.referenceCenter.z;

        // Loop through all tile cells - triangle pairs
        int startIndex = (density + 2) * 2 + 6; // skip first skirt row and a couple degenerate cells
        int endIndex = trianglesNum - startIndex; // ignore last skirt row and a couple degenerate cells
        int k = -1;
        for (int i = startIndex; i < endIndex; i += 2)
        {
            // Skip skirts and degenerate triangle cells - based on index sequence.
            k = k == density - 1 ? -4 : k + 1; // density x terrain cells interleaved with 4 skirt and degenerate cells.
            if (k < 0)
                continue;

            // Triangle pair diagonal - v1 & v2
            int vIndex = 3 * indices[i + 1];
            Vec4 v1 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            vIndex = 3 * indices[i + 2];
            Vec4 v2 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            Vec4 cellCenter = Vec4.mix3(.5, v1, v2);

            // Test cell center distance to vertical plane
            if (verticalPlane != null)
            {
                if (Math.abs(verticalPlane.distanceTo(cellCenter)) > effectiveRadiusVertical)
                    continue;

                // Test cell center distance to horizontal plane
                if (Math.abs(horizontalPlane.distanceTo(cellCenter)) > effectiveRadiusHorizontal)
                    continue;
            }

            // Prepare to test triangles - get other two vertices v0 & v3
            Vec4 p;
            vIndex = 3 * indices[i];
            Vec4 v0 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            vIndex = 3 * indices[i + 3];
            Vec4 v3 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            // Test triangle 1 intersection w ray
            Triangle t = new Triangle(v0, v1, v2);
            if ((p = t.intersect(line)) != null)
            {
                list.add(new Intersection(p, false));
            }

            // Test triangle 2 intersection w ray
            t = new Triangle(v1, v2, v3);
            if ((p = t.intersect(line)) != null)
            {
                list.add(new Intersection(p, false));
            }
        }

        int numHits = list.size();
        if (numHits == 0)
            return null;

        hits = new Intersection[numHits];
        list.toArray(hits);

        final Vec4 origin = line.getOrigin();
        Arrays.sort(hits, new Comparator<Intersection>()
        {
            public int compare(Intersection i1, Intersection i2)
            {
                if (i1 == null && i2 == null)
                    return 0;
                if (i2 == null)
                    return -1;
                if (i1 == null)
                    return 1;

                Vec4 v1 = i1.getIntersectionPoint();
                Vec4 v2 = i2.getIntersectionPoint();
                double d1 = origin.distanceTo3(v1);
                double d2 = origin.distanceTo3(v2);
                return Double.compare(d1, d2);
            }
        });

        return hits;
    }

    protected Intersection[] intersect(RectTile tile, double elevation)
    {
        if (tile.ri.vertices == null)
            return null;

        // Check whether the tile includes the intersection elevation - assume cylinder as Extent
        // TODO: replace this test with a generic test against Extent
        if (tile.getExtent() instanceof Cylinder)
        {
            Cylinder cylinder = ((Cylinder) tile.getExtent());
            if (!(globe.isPointAboveElevation(cylinder.getBottomCenter(), elevation)
                ^ globe.isPointAboveElevation(cylinder.getTopCenter(), elevation)))
                return null;
        }

        Intersection[] hits;
        ArrayList<Intersection> list = new ArrayList<Intersection>();

        int[] indices = new int[tile.ri.indices.limit()];
        float[] coords = new float[tile.ri.vertices.limit()];
        tile.ri.indices.rewind();
        tile.ri.vertices.rewind();
        tile.ri.indices.get(indices, 0, indices.length);
        tile.ri.vertices.get(coords, 0, coords.length);
        tile.ri.indices.rewind();
        tile.ri.vertices.rewind();

        int trianglesNum = tile.ri.indices.capacity() - 2;
        double centerX = tile.ri.referenceCenter.x;
        double centerY = tile.ri.referenceCenter.y;
        double centerZ = tile.ri.referenceCenter.z;

        // Loop through all tile cells - triangle pairs
        int startIndex = (density + 2) * 2 + 6; // skip first skirt row and a couple degenerate cells
        int endIndex = trianglesNum - startIndex; // ignore last skirt row and a couple degenerate cells
        int k = -1;
        for (int i = startIndex; i < endIndex; i += 2)
        {
            // Skip skirts and degenerate triangle cells - based on indice sequence.
            k = k == density - 1 ? -4 : k + 1; // density x terrain cells interleaved with 4 skirt and degenerate cells.
            if (k < 0)
                continue;

            // Get the four cell corners
            int vIndex = 3 * indices[i];
            Vec4 v0 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            vIndex = 3 * indices[i + 1];
            Vec4 v1 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            vIndex = 3 * indices[i + 2];
            Vec4 v2 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            vIndex = 3 * indices[i + 3];
            Vec4 v3 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            Intersection[] inter;
            // Test triangle 1 intersection
            if ((inter = globe.intersect(new Triangle(v0, v1, v2), elevation)) != null)
            {
                list.add(inter[0]);
                list.add(inter[1]);
            }

            // Test triangle 2 intersection
            if ((inter = globe.intersect(new Triangle(v1, v2, v3), elevation)) != null)
            {
                list.add(inter[0]);
                list.add(inter[1]);
            }
        }

        int numHits = list.size();
        if (numHits == 0)
            return null;

        hits = new Intersection[numHits];
        list.toArray(hits);

        return hits;
    }

    protected Vec4 getSurfacePoint(RectTile tile, Angle latitude, Angle longitude, double metersOffset)
    {
        Vec4 result = this.getSurfacePoint(tile, latitude, longitude);
        if (metersOffset != 0 && result != null)
            result = applyOffset(this.globe, result, metersOffset);

        return result;
    }

    /**
     * Offsets <code>point</code> by <code>metersOffset</code> meters.
     *
     * @param globe        the <code>Globe</code> from which to offset
     * @param point        the <code>Vec4</code> to offset
     * @param metersOffset the magnitude of the offset
     *
     * @return <code>point</code> offset along its surface normal as if it were on <code>globe</code>
     */
    protected static Vec4 applyOffset(Globe globe, Vec4 point, double metersOffset)
    {
        Vec4 normal = globe.computeSurfaceNormalAtPoint(point);
        point = Vec4.fromLine3(point, metersOffset, normal);
        return point;
    }

    protected Vec4 getSurfacePoint(RectTile tile, Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!tile.sector.contains(latitude, longitude))
        {
            // not on this geometry
            return null;
        }

        if (tile.ri == null)
            return null;

        double lat = latitude.getDegrees();
        double lon = longitude.getDegrees();

        double bottom = tile.sector.getMinLatitude().getDegrees();
        double top = tile.sector.getMaxLatitude().getDegrees();
        double left = tile.sector.getMinLongitude().getDegrees();
        double right = tile.sector.getMaxLongitude().getDegrees();

        double leftDecimal = (lon - left) / (right - left);
        double bottomDecimal = (lat - bottom) / (top - bottom);

        int row = (int) (bottomDecimal * (tile.density));
        int column = (int) (leftDecimal * (tile.density));

        double l = createPosition(column, leftDecimal, tile.ri.density);
        double h = createPosition(row, bottomDecimal, tile.ri.density);

        Vec4 result = interpolate(row, column, l, h, tile.ri);
        result = result.add3(tile.ri.referenceCenter);

        return result;
    }

    /**
     * Computes from a column (or row) number, and a given offset ranged [0,1] corresponding to the distance along the
     * edge of this sector, where between this column and the next column the corresponding position will fall, in the
     * range [0,1].
     *
     * @param start   the number of the column or row to the left, below or on this position
     * @param decimal the distance from the left or bottom of the current sector that this position falls
     * @param density the number of intervals along the sector's side
     *
     * @return a decimal ranged [0,1] representing the position between two columns or rows, rather than between two
     * edges of the sector
     */
    protected static double createPosition(int start, double decimal, int density)
    {
        double l = ((double) start) / (double) density;
        double r = ((double) (start + 1)) / (double) density;

        return (decimal - l) / (r - l);
    }

    /**
     * Calculates a <code>Point</code> that sits at <code>xDec</code> offset from <code>column</code> to <code>column +
     * 1</code> and at <code>yDec</code> offset from <code>row</code> to <code>row + 1</code>. Accounts for the
     * diagonals.
     *
     * @param row    represents the row which corresponds to a <code>yDec</code> value of 0
     * @param column represents the column which corresponds to an <code>xDec</code> value of 0
     * @param xDec   constrained to [0,1]
     * @param yDec   constrained to [0,1]
     * @param ri     the render info holding the vertices, etc.
     *
     * @return a <code>Point</code> geometrically within or on the boundary of the quadrilateral whose bottom left
     * corner is indexed by (<code>row</code>, <code>column</code>)
     */
    protected static Vec4 interpolate(int row, int column, double xDec, double yDec, RenderInfo ri)
    {
        row++;
        column++;

        int numVerticesPerEdge = ri.density + 3;

        int bottomLeft = row * numVerticesPerEdge + column;

        bottomLeft *= 3;

        int numVertsTimesThree = numVerticesPerEdge * 3;

//        double[] a = new double[6];
        ri.vertices.position(bottomLeft);
//        ri.vertices.get(a);
//        Vec4 bL = new Vec4(a[0], a[1], a[2]);
//        Vec4 bR = new Vec4(a[3], a[4], a[5]);
        Vec4 bL = new Vec4(ri.vertices.get(), ri.vertices.get(), ri.vertices.get());
        Vec4 bR = new Vec4(ri.vertices.get(), ri.vertices.get(), ri.vertices.get());

        bottomLeft += numVertsTimesThree;

        ri.vertices.position(bottomLeft);
//        ri.vertices.get(a);
//        Vec4 tL = new Vec4(a[0], a[1], a[2]);
//        Vec4 tR = new Vec4(a[3], a[4], a[5]);
        Vec4 tL = new Vec4(ri.vertices.get(), ri.vertices.get(), ri.vertices.get());
        Vec4 tR = new Vec4(ri.vertices.get(), ri.vertices.get(), ri.vertices.get());

        return interpolate(bL, bR, tR, tL, xDec, yDec);
    }

    /**
     * Calculates the point at (xDec, yDec) in the two triangles defined by {bL, bR, tL} and {bR, tR, tL}. If thought of
     * as a quadrilateral, the diagonal runs from tL to bR. Of course, this isn't a quad, it's two triangles.
     *
     * @param bL   the bottom left corner
     * @param bR   the bottom right corner
     * @param tR   the top right corner
     * @param tL   the top left corner
     * @param xDec how far along, [0,1] 0 = left edge, 1 = right edge
     * @param yDec how far along, [0,1] 0 = bottom edge, 1 = top edge
     *
     * @return the point xDec, yDec in the co-ordinate system defined by bL, bR, tR, tL
     */
    protected static Vec4 interpolate(Vec4 bL, Vec4 bR, Vec4 tR, Vec4 tL, double xDec, double yDec)
    {
        double pos = xDec + yDec;
        if (pos == 1)
        {
            // on the diagonal - what's more, we don't need to do any "oneMinusT" calculation
            return new Vec4(
                tL.x * yDec + bR.x * xDec,
                tL.y * yDec + bR.y * xDec,
                tL.z * yDec + bR.z * xDec);
        }
        else if (pos > 1)
        {
            // in the "top right" half

            // vectors pointing from top right towards the point we want (can be thought of as "negative" vectors)
            Vec4 horizontalVector = (tL.subtract3(tR)).multiply3(1 - xDec);
            Vec4 verticalVector = (bR.subtract3(tR)).multiply3(1 - yDec);

            return tR.add3(horizontalVector).add3(verticalVector);
        }
        else
        {
            // pos < 1 - in the "bottom left" half

            // vectors pointing from the bottom left towards the point we want
            Vec4 horizontalVector = (bR.subtract3(bL)).multiply3(xDec);
            Vec4 verticalVector = (tL.subtract3(bL)).multiply3(yDec);

            return bL.add3(horizontalVector).add3(verticalVector);
        }
    }

    protected static double[] baryCentricCoordsRequireInside(Vec4 pnt, Vec4[] V)
    {
        // if pnt is in the interior of the triangle determined by V, return its
        // barycentric coordinates with respect to V. Otherwise return null.

        // b0:
        final double tol = 1.0e-4;
        double[] b0b1b2 = new double[3];
        double triangleHeight =
            distanceFromLine(V[0], V[1], V[2].subtract3(V[1]));
        double heightFromPoint =
            distanceFromLine(pnt, V[1], V[2].subtract3(V[1]));
        b0b1b2[0] = heightFromPoint / triangleHeight;
        if (Math.abs(b0b1b2[0]) < tol)
            b0b1b2[0] = 0.0;
        else if (Math.abs(1.0 - b0b1b2[0]) < tol)
            b0b1b2[0] = 1.0;
        if (b0b1b2[0] < 0.0 || b0b1b2[0] > 1.0)
            return null;

        // b1:
        triangleHeight = distanceFromLine(V[1], V[0], V[2].subtract3(V[0]));
        heightFromPoint = distanceFromLine(pnt, V[0], V[2].subtract3(V[0]));
        b0b1b2[1] = heightFromPoint / triangleHeight;
        if (Math.abs(b0b1b2[1]) < tol)
            b0b1b2[1] = 0.0;
        else if (Math.abs(1.0 - b0b1b2[1]) < tol)
            b0b1b2[1] = 1.0;
        if (b0b1b2[1] < 0.0 || b0b1b2[1] > 1.0)
            return null;

        // b2:
        b0b1b2[2] = 1.0 - b0b1b2[0] - b0b1b2[1];
        if (Math.abs(b0b1b2[2]) < tol)
            b0b1b2[2] = 0.0;
        else if (Math.abs(1.0 - b0b1b2[2]) < tol)
            b0b1b2[2] = 1.0;
        if (b0b1b2[2] < 0.0)
            return null;
        return b0b1b2;
    }

    protected static double distanceFromLine(Vec4 pnt, Vec4 P, Vec4 u)
    {
        // Return distance from pnt to line(P,u)
        // Pythagorean theorem approach: c^2 = a^2 + b^2. The
        // The square of the distance we seek is b^2:
        Vec4 toPoint = pnt.subtract3(P);
        double cSquared = toPoint.dot3(toPoint);
        double aSquared = u.normalize3().dot3(toPoint);
        aSquared *= aSquared;
        double distSquared = cSquared - aSquared;
        if (distSquared < 0.0)
            // must be a tiny number that really ought to be 0.0
            return 0.0;
        return Math.sqrt(distSquared);
    }

    protected DoubleBuffer makeGeographicTexCoords(SectorGeometry sg,
        SectorGeometry.GeographicTextureCoordinateComputer computer)
    {
        if (sg == null)
        {
            String msg = Logging.getMessage("nullValue.SectorGeometryIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (computer == null)
        {
            String msg = Logging.getMessage("nullValue.TextureCoordinateComputerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        RectTile rt = (RectTile) sg;

        int density = rt.density;
        if (density < 1)
            density = 1;

        int coordCount = (density + 3) * (density + 3);
        DoubleBuffer p = Buffers.newDirectDoubleBuffer(2 * coordCount);

        double deltaLat = rt.sector.getDeltaLatRadians() / density;
        double deltaLon = rt.sector.getDeltaLonRadians() / density;
        Angle minLat = rt.sector.getMinLatitude();
        Angle maxLat = rt.sector.getMaxLatitude();
        Angle minLon = rt.sector.getMinLongitude();
        Angle maxLon = rt.sector.getMaxLongitude();

        double[] uv; // for return values from computer

        int k = 2 * (density + 3);
        for (int j = 0; j < density; j++)
        {
            Angle lat = Angle.fromRadians(minLat.radians + j * deltaLat);

            // skirt column; duplicate first column
            uv = computer.compute(lat, minLon);
            p.put(k++, uv[0]).put(k++, uv[1]);

            // interior columns
            for (int i = 0; i < density; i++)
            {
                Angle lon = Angle.fromRadians(minLon.radians + i * deltaLon);
                uv = computer.compute(lat, lon);
                p.put(k++, uv[0]).put(k++, uv[1]);
            }

            // last interior column; force u to 1.
            uv = computer.compute(lat, maxLon);
            p.put(k++, uv[0]).put(k++, uv[1]);

            // skirt column; duplicate previous column
            p.put(k++, uv[0]).put(k++, uv[1]);
        }

        // Last interior row
        uv = computer.compute(maxLat, minLon); // skirt column
        p.put(k++, uv[0]).put(k++, uv[1]);

        for (int i = 0; i < density; i++)
        {
            Angle lon = Angle.fromRadians(minLon.radians + i * deltaLon); // u
            uv = computer.compute(maxLat, lon);
            p.put(k++, uv[0]).put(k++, uv[1]);
        }

        uv = computer.compute(maxLat, maxLon); // last interior column
        p.put(k++, uv[0]).put(k++, uv[1]);
        p.put(k++, uv[0]).put(k++, uv[1]); // skirt column

        // last skirt row
        int kk = k - 2 * (density + 3);
        for (int i = 0; i < density + 3; i++)
        {
            p.put(k++, p.get(kk++));
            p.put(k++, p.get(kk++));
        }

        // first skirt row
        k = 0;
        kk = 2 * (density + 3);
        for (int i = 0; i < density + 3; i++)
        {
            p.put(k++, p.get(kk++));
            p.put(k++, p.get(kk++));
        }

        return p;
    }

    protected static void createTextureCoordinates(int density)
    {
        if (density < 1)
            density = 1;

        if (textureCoords.containsKey(density))
            return;

        // Approximate 1 to avoid shearing off of right and top skirts in SurfaceTileRenderer.
        // TODO: dig into this more: why are the skirts being sheared off?
        final float one = 0.999999f;

        int coordCount = (density + 3) * (density + 3);
        FloatBuffer p = Buffers.newDirectFloatBuffer(2 * coordCount);
        double delta = 1d / density;
        int k = 2 * (density + 3);
        for (int j = 0; j < density; j++)
        {
            double v = j * delta;

            // skirt column; duplicate first column
            p.put(k++, 0f);
            p.put(k++, (float) v);

            // interior columns
            for (int i = 0; i < density; i++)
            {
                p.put(k++, (float) (i * delta)); // u
                p.put(k++, (float) v);
            }

            // last interior column; force u to 1.
            p.put(k++, one);//1d);
            p.put(k++, (float) v);

            // skirt column; duplicate previous column
            p.put(k++, one);//1d);
            p.put(k++, (float) v);
        }

        // Last interior row
        //noinspection UnnecessaryLocalVariable
        float v = one;//1d;
        p.put(k++, 0f); // skirt column
        p.put(k++, v);

        for (int i = 0; i < density; i++)
        {
            p.put(k++, (float) (i * delta)); // u
            p.put(k++, v);
        }
        p.put(k++, one);//1d); // last interior column
        p.put(k++, v);

        p.put(k++, one);//1d); // skirt column
        p.put(k++, v);

        // last skirt row
        int kk = k - 2 * (density + 3);
        for (int i = 0; i < density + 3; i++)
        {
            p.put(k++, p.get(kk++));
            p.put(k++, p.get(kk++));
        }

        // first skirt row
        k = 0;
        kk = 2 * (density + 3);
        for (int i = 0; i < density + 3; i++)
        {
            p.put(k++, p.get(kk++));
            p.put(k++, p.get(kk++));
        }

        textureCoords.put(density, p);
    }

    protected static void createIndices(int density)
    {
        if (density < 1)
            density = 1;

        if (indexLists.containsKey(density))
            return;

        int sideSize = density + 2;

        int indexCount = 2 * sideSize * sideSize + 4 * sideSize - 2;
        java.nio.IntBuffer buffer = Buffers.newDirectIntBuffer(indexCount);
        int k = 0;
        for (int i = 0; i < sideSize; i++)
        {
            buffer.put(k);
            if (i > 0)
            {
                buffer.put(++k);
                buffer.put(k);
            }

            if (i % 2 == 0) // even
            {
                buffer.put(++k);
                for (int j = 0; j < sideSize; j++)
                {
                    k += sideSize;
                    buffer.put(k);
                    buffer.put(++k);
                }
            }
            else // odd
            {
                buffer.put(--k);
                for (int j = 0; j < sideSize; j++)
                {
                    k -= sideSize;
                    buffer.put(k);
                    buffer.put(--k);
                }
            }
        }

        indexLists.put(density, buffer);
    }
//
//    protected SectorGeometry.ExtractedShapeDescription getIntersectingTessellationPieces(RectTile tile, Plane[] planes)
//    {
//        tile.ri.vertices.rewind();
//        tile.ri.indices.rewind();
//
//        Vec4 offset = tile.ri.referenceCenter;
//        if (offset == null)
//            offset = new Vec4(0.0);
//
//        int trianglesNum = tile.ri.indices.capacity() - 2;
//
//        int[] indices = new int[3];
//        float[] coords = new float[3];
//
//        SectorGeometry.ExtractedShapeDescription clippedTriangleList = null;
//        for (int i = 0; i < trianglesNum; i++)
//        {
//            tile.ri.indices.position(i);
//            tile.ri.indices.get(indices);
//
//            if ((indices[0] == indices[1]) || (indices[0] == indices[2]) ||
//                (indices[1] == indices[2]))
//                // degenerate triangle
//                continue;
//            Vec4[] triVerts = new Vec4[3];
//            for (int j = 0; j < 3; j++)
//            {
//                tile.ri.vertices.position(3 * indices[j]);
//                tile.ri.vertices.get(coords);
//                triVerts[j] = new Vec4(coords[0] + offset.getX(),
//                    coords[1] + offset.getY(),
//                    coords[2] + offset.getZ(), 1.0);
//            }
//            clippedTriangleList = addClippedPolygon(triVerts, planes, clippedTriangleList);
//        }
//        return clippedTriangleList;
//    }
//
//    protected SectorGeometry.ExtractedShapeDescription addClippedPolygon(Vec4[] triVerts, Plane[] planes,
//        SectorGeometry.ExtractedShapeDescription l)
//    {
//        // Clip the polygon defined by polyVerts to the region defined by the intersection of
//        // the negative halfspaces in 'planes'. If there is a non-empty clipped result, then
//        // add it to the given list.
//        // This routine is (currently) only used to clip triangles in the current tessellation,
//        // but it is actually general enough for n-sided polygons. Best results will be
//        // obtained if the polygon is convex.
//
//        // ignore triangles on skirts
//        if (isSkirt(triVerts))
//            return l;
//
//        // We use a multi-pass Sutherland-Hodgman-style clipping algorithm.
//        // There is one pass for each clipping plane. We begin by copying the
//        // original vertices to local working storage.
//        Vec4[] polyVerts = new Vec4[3];
//        System.arraycopy(triVerts, 0, polyVerts, 0, 3);
//
//        for (Plane p : planes)
//        {
//            polyVerts = doSHPass(p, polyVerts);
//            if (polyVerts == null)
//                // the polygon has been totally clipped away
//                return l;
//        }
//        // some part of the polygon survived. Store it in the list.
//        if (l == null)
//            l = new SectorGeometry.ExtractedShapeDescription(
//                new ArrayList<Vec4[]>(), new ArrayList<SectorGeometry.BoundaryEdge>());
//        l.interiorPolys.add(polyVerts);
//        addBoundaryEdges(polyVerts, triVerts, l.shapeOutline);
//
//        return l;
//    }
//
//    protected boolean isSkirt(Vec4[] triVerts)
//    {
//        Vec4 normal = globe.computeSurfaceNormalAtPoint(triVerts[0]);
//        // try to minimize numerical roundoff. The three triangle vertices
//        // are going to have coordinates with roughly the same magnitude,
//        // so we just sample triVerts[0].
//        double maxC = Math.max(Math.abs(triVerts[0].x), Math.abs(triVerts[0].y));
//        maxC = Math.max(maxC, Math.abs(triVerts[0].z));
//        Vec4 v0 = triVerts[0].divide3(maxC);
//        Vec4 u = triVerts[1].divide3(maxC).subtract3(v0);
//        Vec4 v = triVerts[triVerts.length - 1].divide3(maxC).subtract3(v0);
//        Vec4 w = u.cross3(v).normalize3();
//        return (Math.abs(w.dot3(normal)) < 0.0001);
//    }
//
//    protected Vec4[] doSHPass(Plane p, Vec4[] polyVerts)
//    {
//        // See comments in addClippedPolygon. Also note that, even if the
//        // original polygon is a triangle, the polygon here may have
//        // more than three vertices, depending on how it cuts the various
//        // planes whose volumetric intersection defines the clipping region.
//        ArrayList<Vec4> workingStorage = new ArrayList<Vec4>();
//        Vec4 startPnt = polyVerts[0];
//        boolean startPntIn = (p.dot(startPnt) <= 0.0);
//        for (int i = 1; i <= polyVerts.length; i++)
//        {
//            if (startPntIn)
//                workingStorage.add(startPnt);
//            Vec4 endPnt = polyVerts[i % polyVerts.length];
//            boolean endPntIn = (p.dot(endPnt) <= 0.0);
//            if (startPntIn != endPntIn)
//            {
//                // compute and store the intersection of this edge with p
//                Vec4[] clippedPnts;
//                if (startPntIn)
//                    clippedPnts = p.clip(startPnt, endPnt);
//                else
//                    clippedPnts = p.clip(endPnt, startPnt);
//                workingStorage.add(clippedPnts[0]);
//            }
//            // prepare for next edge
//            startPnt = endPnt;
//            startPntIn = endPntIn;
//        }
//        if (workingStorage.size() == 0)
//            return null;
//        Vec4[] verts = new Vec4[workingStorage.size()];
//        return workingStorage.toArray(verts);
//    }
//
//    protected void addBoundaryEdges(Vec4[] polyVerts, Vec4[] triVerts,
//        ArrayList<SectorGeometry.BoundaryEdge> beList)
//    {
//        // each edge of polyVerts not coincident with an edge of the original
//        // triangle (triVerts) belongs to the outer boundary.
//        for (int i = 0; i < polyVerts.length; i++)
//        {
//            int j = (i + 1) % polyVerts.length;
//            if (!edgeOnTriangle(polyVerts[i], polyVerts[j], triVerts))
//                beList.add(new SectorGeometry.BoundaryEdge(polyVerts, i, j));
//        }
//    }
//
//    protected boolean edgeOnTriangle(Vec4 a, Vec4 b, Vec4[] tri)
//    {
//        final double tol = 1.0e-4;
//        double[] coords_a = baryCentricCoordsRequireInside(a, tri);
//        double[] coords_b = baryCentricCoordsRequireInside(b, tri);
//        if ((coords_a == null) || (coords_b == null))
//            // mathematically not possible because 'a' and 'b' are
//            // known to be on edges of the triangle 'tri'.
//            return true;
//        for (int i = 0; i < 3; i++)
//        {
//            if ((coords_a[i] < tol) && (coords_b[i] < tol))
//                // 'a' and 'b' are on the same edge
//                return true;
//        }
//        return false;
//    }
//
//    protected SectorGeometry.ExtractedShapeDescription getIntersectingTessellationPieces(RectTile tile, Vec4 Cxyz,
//        Vec4 uHat, Vec4 vHat,
//        double uRadius, double vRadius)
//    {
//        tile.ri.vertices.rewind();
//        tile.ri.indices.rewind();
//
//        Vec4 offset = tile.ri.referenceCenter;
//        if (offset == null)
//            offset = new Vec4(0.0);
//
//        int trianglesNum = tile.ri.indices.capacity() - 2;
//
//        int[] indices = new int[3];
//        float[] coords = new float[3];
//
//        SectorGeometry.ExtractedShapeDescription clippedTriangleList = null;
//        for (int i = 0; i < trianglesNum; i++)
//        {
//            tile.ri.indices.position(i);
//            tile.ri.indices.get(indices);
//
//            if ((indices[0] == indices[1]) || (indices[0] == indices[2]) ||
//                (indices[1] == indices[2]))
//                // degenerate triangle
//                continue;
//            Vec4[] triVerts = new Vec4[3];
//            for (int j = 0; j < 3; j++)
//            {
//                tile.ri.vertices.position(3 * indices[j]);
//                tile.ri.vertices.get(coords);
//                triVerts[j] = new Vec4(coords[0] + offset.getX(),
//                    coords[1] + offset.getY(),
//                    coords[2] + offset.getZ(), 1.0);
//            }
//            clippedTriangleList = addClippedPolygon(triVerts,
//                Cxyz, uHat, vHat, uRadius, vRadius, clippedTriangleList);
//        }
//        return clippedTriangleList;
//    }
//
//    protected SectorGeometry.ExtractedShapeDescription addClippedPolygon(Vec4[] polyVerts, Vec4 Cxyz,
//        Vec4 uHat, Vec4 vHat, double uRadius,
//        double vRadius,
//        SectorGeometry.ExtractedShapeDescription l)
//    {
//        // ignore triangles on skirts
//        if (isSkirt(polyVerts))
//            return l;
//
//        int i = 0, nInNegHalfspace = 0, locIn = -1, locOut = -1;
//        for (Vec4 vtx : polyVerts)
//        {
//            Vec4 vMinusC = vtx.subtract3(Cxyz);
//            double xd = vMinusC.dot3(uHat);
//            double yd = vMinusC.dot3(vHat);
//            double halfspaceEqn = (xd * xd) / (uRadius * uRadius) + (yd * yd) / (vRadius * vRadius) - 1.0;
//            if (halfspaceEqn <= 0.0)
//            {
//                locIn = i++;
//                nInNegHalfspace++;
//            }
//            else
//                locOut = i++;
//        }
//        SectorGeometry.BoundaryEdge be = new SectorGeometry.BoundaryEdge(null, -1, -1);
//        switch (nInNegHalfspace)
//        {
//            case 1: // compute and return a trimmed triangle
//                if (locIn != 0)
//                {
//                    Vec4 h1 = polyVerts[locIn];
//                    polyVerts[locIn] = polyVerts[0];
//                    polyVerts[0] = h1;
//                }
//                polyVerts = computeTrimmedPoly(polyVerts, Cxyz, uHat, vHat, uRadius,
//                    vRadius, nInNegHalfspace, be);
//                break;
//            case 2: // compute and return a trimmed quadrilateral
//                if (locOut != 0)
//                {
//                    Vec4 h2 = polyVerts[locOut];
//                    polyVerts[locOut] = polyVerts[0];
//                    polyVerts[0] = h2;
//                }
//                polyVerts = computeTrimmedPoly(polyVerts, Cxyz, uHat, vHat, uRadius,
//                    vRadius, nInNegHalfspace, be);
//                break;
//            case 3: // triangle completely inside cylinder, so store it
//                break;
//        }
//        if (polyVerts == null)
//            return l;
//        if (l == null)
//            l = new SectorGeometry.ExtractedShapeDescription(new ArrayList<Vec4[]>(100),
//                new ArrayList<SectorGeometry.BoundaryEdge>(50));
//        l.interiorPolys.add(polyVerts);
//        if (be.vertices != null)
//            l.shapeOutline.add(be);
//        return l;
//    }
//
//    protected Vec4[] computeTrimmedPoly(Vec4[] polyVerts, Vec4 Cxyz,
//        Vec4 uHat, Vec4 vHat, double uRadius, double vRadius, int nInside,
//        SectorGeometry.BoundaryEdge be)
//    {
//        // Either 1 or 2 vertices are inside the ellipse. If exactly 1 is inside, it is in position 0
//        // of the array. If exactly 1 is outside, it is in position 0 of the array.
//        // We therefore compute the points of intersection between the two edges [0]-[1] and [0]-[2]
//        // with the cylinder and return either a triangle or a quadrilateral.
//        Vec4 p1 = intersectWithEllCyl(polyVerts[0], polyVerts[1], Cxyz, uHat, vHat, uRadius, vRadius);
//        Vec4 p2 = intersectWithEllCyl(polyVerts[0], polyVerts[2], Cxyz, uHat, vHat, uRadius, vRadius);
//        Vec4 midP1P2 = p1.multiply3(0.5).add3(p2.multiply3(0.5));
//        if (nInside == 1)
//        {
//            polyVerts[1] = p1;
//            polyVerts[2] = p2;
//            be.vertices = polyVerts;
//            be.i1 = 1;
//            be.i2 = 2;
//            be.toMidPoint = midP1P2.subtract3(polyVerts[0]);
//            return polyVerts;
//        }
//        Vec4[] ret = new Vec4[4];
//        ret[0] = p1;
//        ret[1] = polyVerts[1];
//        ret[2] = polyVerts[2];
//        ret[3] = p2;
//        be.vertices = ret;
//        be.i1 = 0;
//        be.i2 = 3;
//        be.toMidPoint = polyVerts[0].subtract3(midP1P2);
//        return ret;
//    }
//
//    protected Vec4 intersectWithEllCyl(Vec4 v0, Vec4 v1, Vec4 Cxyz,
//        Vec4 uHat, Vec4 vHat, double uRadius, double vRadius)
//    {
//        // Entry condition: one of (v0, v1) is inside the elliptical cylinder, and one is
//        // outside. We find 0<t<1 such that (1-t)*v0 + t*v1 is on the cylinder. We then return
//        // the corresponding point.
//
//        // First project v0 and v1 onto the plane of the ellipse
//        Vec4 v0MinusC = v0.subtract3(Cxyz);
//        double v0x = v0MinusC.dot3(uHat);
//        double v0y = v0MinusC.dot3(vHat);
//        Vec4 v1MinusC = v1.subtract3(Cxyz);
//        double v1x = v1MinusC.dot3(uHat);
//        double v1y = v1MinusC.dot3(vHat);
//
//        // Then compute the coefficients of the quadratic equation describing where
//        // the line segment (v0x,v0y)-(v1x,v1y) intersects the ellipse in the plane:
//        double v1xMinusV0x = v1x - v0x;
//        double v1yMinusV0y = v1y - v0y;
//        double uRsquared = uRadius * uRadius;
//        double vRsquared = vRadius * vRadius;
//
//        double a = v1xMinusV0x * v1xMinusV0x / uRsquared + v1yMinusV0y * v1yMinusV0y / vRsquared;
//        double b = 2.0 * (v0x * v1xMinusV0x / uRsquared + v0y * v1yMinusV0y / vRsquared);
//        double c = v0x * v0x / uRsquared + v0y * v0y / vRsquared - 1.0;
//
//        // now solve it
//        // if the entry condition is satsfied, the diuscriminant will not be negative...
//        double disc = Math.sqrt(b * b - 4.0 * a * c);
//        double t = (-b + disc) / (2.0 * a);
//        if ((t < 0.0) || (t > 1.0))
//            // need the other root
//            t = (-b - disc) / (2.0 * a);
//
//        // the desired point is obtained by using the computed t with the original points
//        // v0 and v1:
//        return v0.multiply3(1.0 - t).add3(v1.multiply3(t));
//    }
//
//    // The following method was brought over from BasicRectangularTessellator and is unchecked.
//    // Compute normals for a strip
//    protected static java.nio.DoubleBuffer getNormals(int density, DoubleBuffer vertices,
//        java.nio.IntBuffer indices, Vec4 referenceCenter)
//    {
//        int numVertices = (density + 3) * (density + 3);
//        int sideSize = density + 2;
//        int numFaces = indices.limit() - 2;
//        double centerX = referenceCenter.x;
//        double centerY = referenceCenter.y;
//        double centerZ = referenceCenter.z;
//        // Create normal buffer
//        java.nio.DoubleBuffer normals = Buffers.newDirectDoubleBuffer(numVertices * 3);
//        // Create per vertex normal lists
//        ArrayList<ArrayList<Vec4>> normalLists = new ArrayList<ArrayList<Vec4>>(numVertices);
//        for (int i = 0; i < numVertices; i++)
//            normalLists.set(i, new ArrayList<Vec4>());
//        // Go through all faces in the strip and store normals in lists
//        for (int i = 0; i < numFaces; i++)
//        {
//            int vIndex = 3 * indices.get(i);
//            Vec4 v0 = new Vec4((vertices.get(vIndex++) + centerX),
//                (vertices.get(vIndex++) + centerY),
//                (vertices.get(vIndex) + centerZ));
//
//            vIndex = 3 * indices.get(i + 1);
//            Vec4 v1 = new Vec4((vertices.get(vIndex++) + centerX),
//                (vertices.get(vIndex++) + centerY),
//                (vertices.get(vIndex) + centerZ));
//
//            vIndex = 3 * indices.get(i + 2);
//            Vec4 v2 = new Vec4((vertices.get(vIndex++) + centerX),
//                (vertices.get(vIndex++) + centerY),
//                (vertices.get(vIndex) + centerZ));
//
//            // get triangle edge vectors and plane normal
//            Vec4 e1 = v1.subtract3(v0);
//            Vec4 e2 = v2.subtract3(v0);
//            Vec4 N = e1.cross3(e2).normalize3();  // if N is 0, the triangle is degenerate
//
//            // Store the face's normal for each of the vertices that make up the face.
//            normalLists.get(indices.get(i)).add(N);
//            normalLists.get(indices.get(i + 1)).add(N);
//            normalLists.get(indices.get(i + 2)).add(N);
//            //System.out.println("Normal: " + N);
//        }
//
//        // Now loop through each vertex, and average out all the normals stored.
//        int idx = 0;
//        for (int i = 0; i < numVertices; i++)
//        {
//            Vec4 normal = Vec4.ZERO;
//            // Sum
//            for (int j = 0; j < normalLists.get(i).size(); ++j)
//                normal = normal.add3(normalLists.get(i).get(j));
//            // Average
//            normal = normal.multiply3(1.0f / normalLists.get(i).size()).normalize3();
//            // Fill normal buffer
//            normals.put(idx++, normal.x);
//            normals.put(idx++, normal.y);
//            normals.put(idx++, normal.z);
//            //System.out.println("Normal: " + normal + " - " + normalLists[i].size());
//            //System.out.println("Normal buffer: " + normals.get(idx - 3) + ", " + normals.get(idx - 2) + ", " + normals.get(idx - 1));
//        }
//
//        return normals;
//    }

    //
    // Exposes aspects of the RectTile.
    //
//
//    public static class RectGeometry
//    {
//        protected RectTile tile;
//        protected double rowFactor;
//        protected double colFactor;
//
//        public RectGeometry(RectTile tile)
//        {
//            this.tile = tile;
//            // Precompute as much as possible; computation in this class is a hot spot...
//            rowFactor = getNumRows() / tile.sector.getDeltaLatDegrees();
//            colFactor = getNumCols() / tile.sector.getDeltaLonDegrees();
//        }
//
//        public int getColAtLon(double longitude)
//        {
//            return (int) Math.floor((longitude - tile.sector.getMinLongitude().degrees) * colFactor);
//        }
//
//        public int getRowAtLat(double latitude)
//        {
//            return (int) Math.floor((latitude - tile.sector.getMinLatitude().degrees) * rowFactor);
//        }
//
//        public double getLatAtRow(int row)
//        {
//            return tile.sector.getMinLatitude().degrees + row / rowFactor;
//        }
//
//        public double getLonAtCol(int col)
//        {
//            return tile.sector.getMinLongitude().degrees + col / colFactor;
//        }
//
//        /*
//         * Bilinearly interpolate XYZ coords from the grid patch that contains the given lat-lon.
//         *
//         * Note:  The returned point is clamped along the nearest border if the given lat-lon is outside the
//         * region spanned by this tile.
//         *
//         */
//        public double[] getPointAt(double lat, double lon)
//        {
//            int col = getColAtLon(lon);
//            if (col < 0)
//            {
//                col = 0;
//                lon = getMinLongitude();
//            }
//            else if (col > getNumCols())
//            {
//                col = getNumCols();
//                lon = getMaxLongitude();
//            }
//
//            int row = getRowAtLat(lat);
//            if (row < 0)
//            {
//                row = 0;
//                lat = getMinLatitude();
//            }
//            else if (row > getNumRows())
//            {
//                row = getNumRows();
//                lat = getMaxLatitude();
//            }
//
//            float[] c0 = new float[3];
//            this.tile.ri.vertices.position(getVertexIndex(row, col));
//            this.tile.ri.vertices.get(c0);
//            float[] c1 = new float[3];
//            this.tile.ri.vertices.position(getVertexIndex(row, col + 1));
//            this.tile.ri.vertices.get(c1);
//            float[] c2 = new float[3];
//            this.tile.ri.vertices.position(getVertexIndex(row + 1, col));
//            this.tile.ri.vertices.get(c2);
//            float[] c3 = new float[3];
//            this.tile.ri.vertices.position(getVertexIndex(row + 1, col + 1));
//            this.tile.ri.vertices.get(c3);
//            double[] refCenter = new double[3];
//            this.tile.ri.referenceCenter.toArray3(refCenter, 0);
//
//            // calculate our parameters u and v...
//            double minLon = getLonAtCol(col);
//            double maxLon = getLonAtCol(col + 1);
//            double minLat = getLatAtRow(row);
//            double maxLat = getLatAtRow(row + 1);
//            double u = (lon - minLon) / (maxLon - minLon);
//            double v = (lat - minLat) / (maxLat - minLat);
//
//            double[] ret = new double[3];
//            // unroll the loop...this method is a definite hotspot!
//            ret[0] = c0[0] * (1. - u) * (1 - v) + c1[0] * (u) * (1. - v) + c2[0] * (1. - u) * (v) + c3[0] * u * v +
//                refCenter[0];
//            ret[1] = c0[1] * (1. - u) * (1 - v) + c1[1] * (u) * (1. - v) + c2[1] * (1. - u) * (v) + c3[1] * u * v +
//                refCenter[1];
//            ret[2] = c0[2] * (1. - u) * (1 - v) + c1[2] * (u) * (1. - v) + c2[2] * (1. - u) * (v) + c3[2] * u * v +
//                refCenter[2];
//            return ret;
//        }
//
//        public double getMinLongitude()
//        {
//            return this.tile.sector.getMinLongitude().degrees;
//        }
//
//        public double getMaxLongitude()
//        {
//            return this.tile.sector.getMaxLongitude().degrees;
//        }
//
//        public double getMinLatitude()
//        {
//            return this.tile.sector.getMinLatitude().degrees;
//        }
//
//        public double getMaxLatitude()
//        {
//            return this.tile.sector.getMaxLatitude().degrees;
//        }
//
//        public int getNumRows()
//        {
//            return this.tile.density;
//        }
//
//        public int getNumCols()
//        {
//            return this.tile.density;
//        }
//
//        private int getVertexIndex(int row, int col)
//        {
//            // The factor of 3 accounts for the 3 doubles that make up each node...
//            // The 3 added to density is 2 tile-skirts plus 1 ending column...
//            return (this.tile.density + 3) * (row + 1) * 3 + (col + 1) * 3;
//        }
//    }
//
//    public static RectGeometry getTerrainGeometry(SectorGeometry tile)
//    {
//        if (tile == null || !(tile instanceof RectTile))
//            throw new IllegalArgumentException("SectorGeometry instance not of type RectTile");
//
//        return new RectGeometry((RectTile) tile);
//    }
}

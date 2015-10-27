/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.shapefile;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.combine.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import java.awt.*;
import java.beans.*;
import java.nio.*;
import java.util.*;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: ShapefilePolygons.java 3053 2015-04-28 19:15:46Z dcollins $
 */
public class ShapefilePolygons extends ShapefileRenderable implements OrderedRenderable, PreRenderable, Combinable
{
    public static class Record extends ShapefileRenderable.Record
    {
        protected double[][] boundaryEffectiveArea;
        protected boolean[] boundaryCrossesAntimeridian;

        public Record(ShapefileRenderable shapefileRenderable, ShapefileRecord shapefileRecord)
        {
            super(shapefileRenderable, shapefileRecord);
        }

        protected double[] getBoundaryEffectiveArea(int boundaryIndex)
        {
            return this.boundaryEffectiveArea != null ? this.boundaryEffectiveArea[boundaryIndex] : null;
        }

        protected boolean isBoundaryCrossesAntimeridian(int boundaryIndex)
        {
            return this.boundaryCrossesAntimeridian != null && this.boundaryCrossesAntimeridian[boundaryIndex];
        }
    }

    protected static class RecordGroup
    {
        protected final ShapeAttributes attributes;
        protected IntBuffer indices;
        protected Range interiorIndexRange = new Range(0, 0);
        protected Range outlineIndexRange = new Range(0, 0);
        protected ArrayList<RecordIndices> recordIndices = new ArrayList<RecordIndices>();

        public RecordGroup(ShapeAttributes attributes)
        {
            this.attributes = attributes;
        }
    }

    protected static class RecordIndices
    {
        protected final int ordinal;
        protected Range vertexRange = new Range(0, 0);
        protected IntBuffer interiorIndices;
        protected IntBuffer outlineIndices;

        public RecordIndices(int ordinal)
        {
            this.ordinal = ordinal;
        }
    }

    protected static class ShapefileTile implements OrderedRenderable, SurfaceRenderable
    {
        // Properties that define the tile.
        protected final ShapefileRenderable shape;
        protected final Sector sector;
        protected final double resolution;
        // Properties supporting geometry caching.
        protected ShapefileTile fallbackTile;
        protected ShapefileGeometry geometry;
        protected final Object nullGeometryStateKey = new Object();

        public ShapefileTile(ShapefileRenderable shape, Sector sector, double resolution)
        {
            this.shape = shape;
            this.sector = sector;
            this.resolution = resolution;
        }

        public ShapefileRenderable getShape()
        {
            return this.shape;
        }

        public Sector getSector()
        {
            return this.sector;
        }

        public double getResolution()
        {
            return this.resolution;
        }

        public ShapefileGeometry getGeometry()
        {
            return this.geometry;
        }

        public void setGeometry(ShapefileGeometry geometry)
        {
            this.geometry = geometry;
        }

        public ShapefileTile[] subdivide()
        {
            Sector[] sectors = this.sector.subdivide();
            ShapefileTile[] tiles = new ShapefileTile[4];
            tiles[0] = new ShapefileTile(this.shape, sectors[0], this.resolution / 2);
            tiles[1] = new ShapefileTile(this.shape, sectors[1], this.resolution / 2);
            tiles[2] = new ShapefileTile(this.shape, sectors[2], this.resolution / 2);
            tiles[3] = new ShapefileTile(this.shape, sectors[3], this.resolution / 2);

            return tiles;
        }

        @Override
        public double getDistanceFromEye()
        {
            return 0;
        }

        @Override
        public List<Sector> getSectors(DrawContext dc)
        {
            return Arrays.asList(this.sector);
        }

        @Override
        public void pick(DrawContext dc, Point pickPoint)
        {
        }

        @Override
        public Object getStateKey(DrawContext dc)
        {
            return this.geometry != null ? new ShapefileGeometryStateKey(this.geometry) : this.nullGeometryStateKey;
        }

        @Override
        public void render(DrawContext dc)
        {
            ((ShapefilePolygons) this.shape).render(dc, this);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            ShapefileTile that = (ShapefileTile) o;
            return this.shape.equals(that.shape)
                && this.sector.equals(that.sector)
                && this.resolution == that.resolution;
        }

        @Override
        public int hashCode()
        {
            long temp = this.resolution != +0.0d ? Double.doubleToLongBits(this.resolution) : 0L;
            int result;
            result = this.shape.hashCode();
            result = 31 * result + this.sector.hashCode();
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    protected static class ShapefileGeometry implements Runnable, Cacheable, Comparable<ShapefileGeometry>
    {
        // Properties that define the geometry.
        protected final ShapefileRenderable shape;
        protected final Sector sector;
        protected final double resolution;
        // Properties supporting geometry tessellation.
        protected MemoryCache memoryCache;
        protected Object memoryCacheKey;
        protected PropertyChangeListener listener;
        protected double priority;
        // Properties supporting geometry rendering.
        protected FloatBuffer vertices;
        protected int vertexStride;
        protected int vertexCount;
        protected Vec4 vertexOffset;
        protected ArrayList<RecordIndices> recordIndices = new ArrayList<RecordIndices>();
        protected ArrayList<RecordGroup> attributeGroups = new ArrayList<RecordGroup>();
        protected long attributeStateID;

        public ShapefileGeometry(ShapefileRenderable shape, Sector sector, double resolution)
        {
            this.shape = shape;
            this.sector = sector;
            this.resolution = resolution;
        }

        @Override
        public void run()
        {
            try
            {
                ((ShapefilePolygons) this.shape).tessellate(this);
            }
            catch (Exception e)
            {
                String msg = Logging.getMessage("generic.ExceptionWhileTessellating", this.shape);
                Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
            }
            finally
            {
                if (this.memoryCache != null && this.memoryCacheKey != null)
                {
                    this.memoryCache.add(this.memoryCacheKey, this);
                }

                if (this.listener != null)
                {
                    this.listener.propertyChange(new PropertyChangeEvent(this, AVKey.REPAINT, null, null));
                }

                // don't need the caching and notification properties anymore
                this.memoryCache = null;
                this.memoryCacheKey = null;
                this.listener = null;
            }
        }

        @Override
        public long getSizeInBytes()
        {
            return 244 + this.sector.getSizeInBytes() + (this.vertices != null ? 4 * this.vertices.remaining() : 0);
        }

        @Override
        public int compareTo(ShapefileGeometry that)
        {
            return Double.compare(this.priority, that.priority);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            ShapefileGeometry that = (ShapefileGeometry) o;
            return this.shape.equals(that.shape)
                && this.sector.equals(that.sector)
                && this.resolution == that.resolution;
        }

        @Override
        public int hashCode()
        {
            long temp = this.resolution != +0.0d ? Double.doubleToLongBits(this.resolution) : 0L;
            int result;
            result = this.shape.hashCode();
            result = 31 * result + this.sector.hashCode();
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    protected static class ShapefileGeometryStateKey
    {
        protected final ShapefileGeometry geometry;
        protected final long attributeStateID;
        protected final ShapeAttributes[] attributeGroups;

        public ShapefileGeometryStateKey(ShapefileGeometry geom)
        {
            this.geometry = geom;
            this.attributeStateID = geom.attributeStateID;
            this.attributeGroups = new ShapeAttributes[geom.attributeGroups.size()];

            for (int i = 0; i < this.attributeGroups.length; i++)
            {
                this.attributeGroups[i] = geom.attributeGroups.get(i).attributes.copy();
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ShapefileGeometryStateKey that = (ShapefileGeometryStateKey) o;
            return this.geometry.equals(that.geometry)
                && this.attributeStateID == that.attributeStateID
                && Arrays.equals(this.attributeGroups, that.attributeGroups);
        }

        @Override
        public int hashCode()
        {
            int result = this.geometry.hashCode();
            result = 31 * result + (int) (this.attributeStateID ^ (this.attributeStateID >>> 32));
            result = 31 * result + Arrays.hashCode(this.attributeGroups);
            return result;
        }
    }

    static
    {
        if (!WorldWind.getMemoryCacheSet().containsCache(ShapefileGeometry.class.getName()))
        {
            long size = Configuration.getLongValue(AVKey.SHAPEFILE_GEOMETRY_CACHE_SIZE, (long) 50e6); // default 50MB
            MemoryCache cache = new BasicMemoryCache((long) (0.8 * size), size);
            cache.setName("Shapefile Geometry");
            WorldWind.getMemoryCacheSet().addCache(ShapefileGeometry.class.getName(), cache);
        }
    }

    // ShapefilePolygons properties.
    protected double detailHint = 0;
    protected double detailHintOrigin = 2.8;
    protected int outlinePickWidth = 10;
    // Properties supporting shapefile tile assembly and tessellation.
    protected BasicQuadTree<Record> recordTree;
    protected ArrayList<ShapefileTile> topLevelTiles = new ArrayList<ShapefileTile>();
    protected ArrayList<ShapefileTile> currentTiles = new ArrayList<ShapefileTile>();
    protected ShapefileTile currentAncestorTile;
    protected PriorityQueue<Runnable> requestQueue = new PriorityQueue<Runnable>();
    protected MemoryCache cache = WorldWind.getMemoryCache(ShapefileGeometry.class.getName());
    protected long recordStateID;
    // Properties supporting picking and rendering.
    protected PickSupport pickSupport = new PickSupport();
    protected HashMap<Integer, Color> pickColorMap = new HashMap<Integer, Color>();
    protected SurfaceObjectTileBuilder pickTileBuilder = new SurfaceObjectTileBuilder(new Dimension(512, 512),
        GL2.GL_RGBA8, false, false);
    protected ByteBuffer pickColors;
    protected Layer layer;
    protected double[] matrixArray = new double[16];
    protected double[] clipPlaneArray = new double[16];

    /**
     * Creates a new ShapefilePolygons with the specified shapefile. The normal attributes and the highlight attributes
     * for each ShapefileRenderable.Record are assigned default values. In order to modify ShapefileRenderable.Record
     * shape attributes or key-value attributes during construction, use {@link #ShapefilePolygons(gov.nasa.worldwind.formats.shapefile.Shapefile,
     * gov.nasa.worldwind.render.ShapeAttributes, gov.nasa.worldwind.render.ShapeAttributes,
     * gov.nasa.worldwind.formats.shapefile.ShapefileRenderable.AttributeDelegate)}.
     *
     * @param shapefile The shapefile to display.
     *
     * @throws IllegalArgumentException if the shapefile is null.
     */
    public ShapefilePolygons(Shapefile shapefile)
    {
        if (shapefile == null)
        {
            String msg = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.init(shapefile, null, null, null);
    }

    /**
     * Creates a new ShapefilePolygons with the specified shapefile. The normal attributes, the highlight attributes and
     * the attribute delegate are optional. Specifying a non-null value for normalAttrs or highlightAttrs causes each
     * ShapefileRenderable.Record to adopt those attributes. Specifying a non-null value for the attribute delegate
     * enables callbacks during creation of each ShapefileRenderable.Record. See {@link
     * gov.nasa.worldwind.formats.shapefile.ShapefileRenderable.AttributeDelegate} for more information.
     *
     * @param shapefile         The shapefile to display.
     * @param normalAttrs       The normal attributes for each ShapefileRenderable.Record. May be null to use the
     *                          default attributes.
     * @param highlightAttrs    The highlight attributes for each ShapefileRenderable.Record. May be null to use the
     *                          default highlight attributes.
     * @param attributeDelegate Optional callback for configuring each ShapefileRenderable.Record's shape attributes and
     *                          key-value attributes. May be null.
     *
     * @throws IllegalArgumentException if the shapefile is null.
     */
    public ShapefilePolygons(Shapefile shapefile, ShapeAttributes normalAttrs, ShapeAttributes highlightAttrs,
        AttributeDelegate attributeDelegate)
    {
        if (shapefile == null)
        {
            String msg = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.init(shapefile, normalAttrs, highlightAttrs, attributeDelegate);
    }

    @Override
    protected void assembleRecords(Shapefile shapefile)
    {
        // Store the shapefile records in a quad tree with eight levels. This depth provides fast access to records in
        // regions much smaller than the shapefile's sector while avoiding a lot of overhead in building the quad tree.
        this.recordTree = new BasicQuadTree<Record>(8, this.sector, null);
        super.assembleRecords(shapefile);
    }

    @Override
    protected boolean mustAssembleRecord(ShapefileRecord shapefileRecord)
    {
        return super.mustAssembleRecord(shapefileRecord)
            && (shapefileRecord.isPolylineRecord()
            || shapefileRecord.isPolygonRecord()); // accept both polyline and polygon records
    }

    @Override
    protected void assembleRecord(ShapefileRecord shapefileRecord)
    {
        ShapefilePolygons.Record record = this.createRecord(shapefileRecord);
        this.addRecord(shapefileRecord, record);
        this.recordTree.add(record, record.sector.asDegreesArray());
    }

    @Override
    protected void recordDidChange(ShapefileRenderable.Record record)
    {
        this.recordStateID++;
    }

    protected ShapefilePolygons.Record createRecord(ShapefileRecord shapefileRecord)
    {
        return new ShapefilePolygons.Record(this, shapefileRecord);
    }

    /**
     * Indicates the object's detail hint, which is described in {@link #setDetailHint(double)}.
     *
     * @return the detail hint
     *
     * @see #setDetailHint(double)
     */
    public double getDetailHint()
    {
        return this.detailHint;
    }

    /**
     * Modifies the default relationship of shape resolution to screen resolution as the viewing altitude changes.
     * Values greater than 0 cause shape detail to appear at higher resolution at greater altitudes than normal, but at
     * an increased performance cost. Values less than 0 decrease the default resolution at any given altitude. The
     * default value is 0. Values typically range between -0.5 and 0.5.
     * <p/>
     * Note: The resolution-to-height relationship is defined by a scale factor that specifies the approximate size of
     * discernible lengths in the shape relative to eye distance. The scale is specified as a power of 10. A value of 3,
     * for example, specifies that a length of 1 meter on the shape should be distinguishable from an altitude of 10^3
     * meters (1000 meters). The default scale is 1/10^2.8, (1 over 10 raised to the power 2.8). The detail hint
     * specifies deviations from that default. A detail hint of 0.2 specifies a scale of 1/1000, i.e., 1/10^(2.8 + .2) =
     * 1/10^3. Scales much larger than 3 typically cause the applied resolution to be higher than discernible for the
     * altitude. Such scales significantly decrease performance.
     *
     * @param detailHint the degree to modify the default relationship of shape resolution to screen resolution with
     *                   changing view altitudes. Values greater than 1 increase the resolution. Values less than zero
     *                   decrease the resolution. The default value is 0.
     */
    public void setDetailHint(double detailHint)
    {
        this.detailHint = detailHint;
    }

    protected double getDetailFactor()
    {
        return this.detailHintOrigin + this.getDetailHint();
    }

    /**
     * Indicates the outline line width to use during picking. A larger width than normal typically makes the outline
     * easier to pick.
     *
     * @return the outline line width used during picking.
     */
    public int getOutlinePickWidth()
    {
        return this.outlinePickWidth;
    }

    /**
     * Specifies the outline line width to use during picking. A larger width than normal typically makes the outline
     * easier to pick.
     * <p/>
     * Note that the size of the pick aperture also affects the precision necessary to pick.
     *
     * @param outlinePickWidth the outline pick width. The default is 10.
     *
     * @throws IllegalArgumentException if the width is less than 0.
     */
    public void setOutlinePickWidth(int outlinePickWidth)
    {
        if (outlinePickWidth < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlinePickWidth = outlinePickWidth;
    }

    @Override
    public double getDistanceFromEye()
    {
        return 0; // ordered surface renderables don't use eye distance
    }

    @Override
    public void preRender(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.visible)
            return;

        if (this.getRecordCount() == 0) // shapefile is empty or contains only null records
            return;

        Extent extent = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), this.sector);

        if (!dc.getView().getFrustumInModelCoordinates().intersects(extent))
            return;

        if (dc.isSmall(extent, 1))
            return;

        this.layer = dc.getCurrentLayer();

        // Assemble the tiles used for rendering, then add those tiles to the scene controller's list of renderables to
        // draw into the scene's shared surface tiles.
        this.assembleTiles(dc);
        for (ShapefileTile tile : this.currentTiles)
        {
            dc.addOrderedSurfaceRenderable(tile);
        }

        // Assemble the tiles used for picking, then build a set of surface object tiles containing unique colors for
        // each record.
        if (dc.getCurrentLayer().isPickEnabled())
        {
            try
            {
                // Setup the draw context state and GL state for creating pick tiles.
                dc.enablePickingMode();
                this.pickSupport.beginPicking(dc);
                // Assemble the tiles intersecting the pick frustums, then draw them with unique pick colors.
                this.assembleTiles(dc);
                this.pickTileBuilder.setForceTileUpdates(true);
                this.pickTileBuilder.buildTiles(dc, this.currentTiles);
            }
            finally
            {
                // Clear pick color map in order to use different pick colors for each globe.
                this.pickColorMap.clear();
                // Restore the draw context state and GL state.
                this.pickSupport.endPicking(dc);
                dc.disablePickingMode();
            }
        }

        // Send requests for tile geometry.
        this.sendRequests();
    }

    @Override
    public void pick(DrawContext dc, Point pickPoint)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.visible)
            return;

        if (this.getRecordCount() == 0) // shapefile is empty or contains only null records
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        try
        {
            this.pickSupport.beginPicking(dc);
            gl.glEnable(GL.GL_CULL_FACE);
            dc.getGeographicSurfaceTileRenderer().setUseImageTilePickColors(true);
            dc.getGeographicSurfaceTileRenderer().renderTiles(dc, this.pickTileBuilder.getTiles(dc));

            for (PickedObject po : this.pickTileBuilder.getPickCandidates(dc))
            {
                this.pickSupport.addPickableObject(po); // transfer picked objects captured during pre rendering
            }
        }
        finally
        {
            dc.getGeographicSurfaceTileRenderer().setUseImageTilePickColors(false);
            gl.glDisable(GL.GL_CULL_FACE);
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.layer);
            this.pickTileBuilder.clearTiles(dc);
            this.pickTileBuilder.clearPickCandidates(dc);
        }
    }

    @Override
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.visible)
            return;

        if (this.getRecordCount() == 0) // shapefile is empty or contains only null records
            return;

        if (dc.isPickingMode() && this.pickTileBuilder.getTileCount(dc) > 0)
        {
            dc.addOrderedSurfaceRenderable(this); // perform the pick during ordered surface rendering
        }
    }

    /** {@inheritDoc} */
    @Override
    public void combine(CombineContext cc)
    {
        if (cc == null)
        {
            String msg = Logging.getMessage("nullValue.CombineContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (cc.isBoundingSectorMode())
            this.combineBounds(cc);
        else
            this.combineContours(cc);
    }

    protected void assembleTiles(DrawContext dc)
    {
        this.currentTiles.clear();

        if (this.topLevelTiles.size() == 0)
        {
            this.createTopLevelTiles();
        }

        for (ShapefileTile tile : this.topLevelTiles)
        {
            this.currentAncestorTile = null;

            if (this.isTileVisible(dc, tile))
            {
                this.addTileOrDescendants(dc, tile);
            }
        }
    }

    protected void createTopLevelTiles()
    {
        Angle latDelta = Angle.fromDegrees(45);
        Angle lonDelta = Angle.fromDegrees(45);
        double resolution = latDelta.radians / 512;

        int firstRow = Tile.computeRow(latDelta, this.sector.getMinLatitude(), Angle.NEG90);
        int lastRow = Tile.computeRow(latDelta, this.sector.getMaxLatitude(), Angle.NEG90);
        int firstCol = Tile.computeColumn(lonDelta, this.sector.getMinLongitude(), Angle.NEG180);
        int lastCol = Tile.computeColumn(lonDelta, this.sector.getMaxLongitude(), Angle.NEG180);

        Angle p1 = Tile.computeRowLatitude(firstRow, latDelta, Angle.NEG90);
        for (int row = firstRow; row <= lastRow; row++)
        {
            Angle p2 = p1.add(latDelta);
            Angle t1 = Tile.computeColumnLongitude(firstCol, lonDelta, Angle.NEG180);
            for (int col = firstCol; col <= lastCol; col++)
            {
                Angle t2 = t1.add(lonDelta);
                this.topLevelTiles.add(new ShapefileTile(this, new Sector(p1, p2, t1, t2), resolution));
                t1 = t2;
            }
            p1 = p2;
        }
    }

    protected boolean isTileVisible(DrawContext dc, ShapefileTile tile)
    {
        Extent extent = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), tile.sector);

        if (dc.isPickingMode())
        {
            return dc.getPickFrustums().intersectsAny(extent);
        }

        return dc.getView().getFrustumInModelCoordinates().intersects(extent);
    }

    protected void addTileOrDescendants(DrawContext dc, ShapefileTile tile)
    {
        ShapefileGeometry geom = this.lookupGeometry(tile);
        tile.setGeometry(geom); // may be null

        if (this.meetsRenderCriteria(dc, tile))
        {
            this.addTile(dc, tile);
            return;
        }

        ShapefileTile previousAncestorTile = null;
        try
        {
            if (tile.getGeometry() != null)
            {
                previousAncestorTile = this.currentAncestorTile;
                this.currentAncestorTile = tile;
            }

            ShapefileTile[] children = tile.subdivide();
            for (ShapefileTile child : children)
            {
                if (child.sector.intersects(this.sector) && this.isTileVisible(dc, child))
                {
                    this.addTileOrDescendants(dc, child);
                }
            }
        }
        finally
        {
            if (previousAncestorTile != null)
            {
                this.currentAncestorTile = previousAncestorTile;
            }
        }
    }

    protected void addTile(DrawContext dc, ShapefileTile tile)
    {
        if (tile.getGeometry() == null)
        {
            this.requestGeometry(dc, tile); // request the tile's geometry

            if (this.currentAncestorTile != null) // try to use the ancestor's geometry
            {
                tile.setGeometry(this.currentAncestorTile.getGeometry());
            }
        }

        if (tile.getGeometry() == null) // no tile geometry, no ancestor geometry
            return;

        if (tile.getGeometry().vertexCount == 0) // don't use empty geometry
            return;

        if (this.mustAssembleAttributeGroups(
            tile.getGeometry())) // build geometry attribute groups on the rendering thread
        {
            this.assembleAttributeGroups(tile.getGeometry());
        }

        this.currentTiles.add(tile);
    }

    protected boolean meetsRenderCriteria(DrawContext dc, ShapefileTile tile)
    {
        return !this.needToSplit(dc, tile);
    }

    protected boolean needToSplit(DrawContext dc, ShapefileTile tile)
    {
        // Compute the resolution in meters of the specified tile. Take care to convert from the radians to meters by
        // multiplying by the globe's radius, not the length of a Cartesian point. Using the length of a Cartesian point
        // is incorrect when the globe is flat.
        double resolutionRadians = tile.resolution;
        double resolutionMeters = dc.getGlobe().getRadius() * resolutionRadians;

        // Compute the level of detail scale and the field of view scale. These scales are multiplied by the eye
        // distance to derive a scaled distance that is then compared to the resolution. The level of detail scale is
        // specified as a power of 10. For example, a detail factor of 3 means split when the resolution becomes more
        // than one thousandth of the eye distance. The field of view scale is specified as a ratio between the current
        // field of view and a the default field of view. In a perspective projection, decreasing the field of view by
        // 50% has the same effect on object size as decreasing the distance between the eye and the object by 50%.
        double detailScale = Math.pow(10, -this.getDetailFactor());
        double fieldOfViewScale = dc.getView().getFieldOfView().tanHalfAngle() / Angle.fromDegrees(45).tanHalfAngle();
        fieldOfViewScale = WWMath.clamp(fieldOfViewScale, 0, 1);

        // Compute the distance between the eye point and the sector in meters, and compute a fraction of that distance
        // by multiplying the actual distance by the level of detail scale and the field of view scale.
        double eyeDistanceMeters = tile.sector.distanceTo(dc, dc.getView().getEyePoint());
        double scaledEyeDistanceMeters = eyeDistanceMeters * detailScale * fieldOfViewScale;

        // Split when the resolution in meters becomes greater than the specified fraction of the eye distance, also in
        // meters. Another way to say it is, use the current tile if its texel size is less than the specified fraction
        // of the eye distance.
        //
        // NOTE: It's tempting to instead compare a screen pixel size to the resolution, but that calculation is
        // window-size dependent and results in selecting an excessive number of tiles when the window is large.
        return resolutionMeters > scaledEyeDistanceMeters;
    }

    protected ShapefileGeometry lookupGeometry(ShapefileTile tile)
    {
        return (ShapefileGeometry) this.cache.getObject(tile); // corresponds to the key used in requestGeometry
    }

    protected void requestGeometry(DrawContext dc, ShapefileTile tile)
    {
        Vec4 eyePoint = dc.getView().getEyePoint();
        Vec4 centroid = tile.sector.computeCenterPoint(dc.getGlobe(), dc.getVerticalExaggeration());

        ShapefileGeometry geom = new ShapefileGeometry(tile.shape, tile.sector, tile.resolution);
        geom.memoryCache = this.cache;
        geom.memoryCacheKey = tile; // corresponds to the key used in lookupGeometry
        geom.listener = this.layer;
        geom.priority = eyePoint.distanceTo3(centroid);

        this.requestQueue.offer(geom);
    }

    protected void sendRequests()
    {
        Runnable request;
        while ((request = this.requestQueue.poll()) != null)
        {
            if (WorldWind.getTaskService().isFull())
                break;

            WorldWind.getTaskService().addTask(request);
        }

        this.requestQueue.clear(); // clear any remaining requests
    }

    protected void tessellate(ShapefileGeometry geom)
    {
        // Get the records intersecting the geometry's sector. The implementation of getItemsInRegion may return entries
        // outside the requested sector, so we cull them further in the loop below.
        Set<Record> intersectingRecords = this.recordTree.getItemsInRegion(geom.sector, null);
        if (intersectingRecords.isEmpty())
            return;

        // Compute the minimum effective area for an entire record based on the geometry resolution. This suppresses
        // records that degenerate to one or two points.
        double minEffectiveArea = 4 * geom.resolution * geom.resolution;
        double xOffset = geom.sector.getCentroid().longitude.degrees;
        double yOffset = geom.sector.getCentroid().latitude.degrees;

        // Setup the polyline generalizer and the polygon tessellator that will be used to generalize and tessellate
        // each record intersecting the geometry's sector.
        PolylineGeneralizer generalizer = new PolylineGeneralizer(); // TODO: Consider using a ThreadLocal property.
        PolygonTessellator2 tess = new PolygonTessellator2(); // TODO: Consider using a ThreadLocal property.
        tess.setPolygonNormal(0, 0, 1); // tessellate in geographic coordinates
        tess.setPolygonClipCoords(geom.sector.getMinLongitude().degrees, geom.sector.getMaxLongitude().degrees,
            geom.sector.getMinLatitude().degrees, geom.sector.getMaxLatitude().degrees);
        tess.setVertexStride(2);
        tess.setVertexOffset(-xOffset, -yOffset, 0);

        // Generate the geographic coordinate vertices and indices for all records intersecting the geometry's sector
        // and meeting the geometry's resolution criteria. This may include records that are marked as not visible, as
        // recomputing the vertices and indices for record visibility changes would be expensive. We exclude non visible
        // records later in the relative less expensive routine assembleAttributeGroups.
        for (Record record : intersectingRecords)
        {
            if (!record.sector.intersects(geom.sector))
                continue; // the record quadtree may return entries outside the sector passed to getItemsInRegion

            double effectiveArea = record.sector.getDeltaLatRadians() * record.sector.getDeltaLonRadians();
            if (effectiveArea < minEffectiveArea)
                continue;  // ignore records that don't meet the resolution criteria

            this.computeRecordMetrics(record, generalizer);
            this.tessellateRecord(geom, record, tess);
        }

        if (tess.getVertexCount() == 0 || geom.recordIndices.size() == 0)
            return;

        FloatBuffer vertices = Buffers.newDirectFloatBuffer(2 * tess.getVertexCount());
        tess.getVertices(vertices);
        geom.vertices = (FloatBuffer) vertices.rewind();
        geom.vertexStride = 2;
        geom.vertexCount = tess.getVertexCount();
        geom.vertexOffset = new Vec4(xOffset, yOffset, 0);
    }

    protected void computeRecordMetrics(Record record, PolylineGeneralizer generalizer)
    {
        synchronized (record) // synchronize access to checking and computing a record's effective area
        {
            if (record.boundaryEffectiveArea != null)
                return;

            record.boundaryEffectiveArea = new double[record.getBoundaryCount()][];
            record.boundaryCrossesAntimeridian = new boolean[record.getBoundaryCount()];

            for (int i = 0; i < record.getBoundaryCount(); i++)
            {
                VecBuffer boundaryCoords = record.getBoundaryPoints(i);
                double[] coord = new double[2]; // lon, lat
                double[] prevCoord = new double[2]; // prevlon, prevlat

                generalizer.reset();
                generalizer.beginPolyline();

                for (int j = 0; j < boundaryCoords.getSize(); j++)
                {
                    boundaryCoords.get(j, coord);
                    generalizer.addVertex(coord[0], coord[1], 0); // lon, lat, 0

                    if (j > 0 && Math.signum(prevCoord[0]) != Math.signum(coord[0]) &&
                        Math.abs(prevCoord[0] - coord[0]) > 180)
                    {
                        record.boundaryCrossesAntimeridian[i] = true;
                    }

                    prevCoord[0] = coord[0]; // prevlon = lon
                    prevCoord[1] = coord[1]; // prevlat = lat
                }

                record.boundaryEffectiveArea[i] = new double[boundaryCoords.getSize()];
                generalizer.endPolyline();
                generalizer.getVertexEffectiveArea(record.boundaryEffectiveArea[i]);
            }
        }
    }

    protected void tessellateRecord(ShapefileGeometry geom, Record record, final PolygonTessellator2 tess)
    {
        // Compute the minimum effective area for a vertex based on the geometry resolution. We convert the resolution
        // from radians to square degrees. This ensures the units are consistent with the vertex effective area computed
        // by PolylineGeneralizer, which adopts the units of the source data (degrees).
        double resolutionDegrees = geom.resolution * 180.0 / Math.PI;
        double minEffectiveArea = resolutionDegrees * resolutionDegrees;

        tess.resetIndices(); // clear indices from previous records, but retain the accumulated vertices
        tess.beginPolygon();

        for (int i = 0; i < record.getBoundaryCount(); i++)
        {
            this.tessellateBoundary(record, i, minEffectiveArea, new TessBoundaryCallback()
            {
                @Override
                public void beginBoundary()
                {
                    tess.beginContour();
                }

                @Override
                public void vertex(double degreesLatitude, double degreesLongitude)
                {
                    tess.addVertex(degreesLongitude, degreesLatitude, 0);
                }

                @Override
                public void endBoundary()
                {
                    tess.endContour();
                }
            });
        }

        tess.endPolygon();

        Range range = tess.getPolygonVertexRange();
        if (range.length == 0) // this should never happen, but we check anyway
            return;

        IntBuffer interiorIndices = IntBuffer.allocate(tess.getInteriorIndexCount());
        IntBuffer outlineIndices = IntBuffer.allocate(tess.getBoundaryIndexCount());
        tess.getInteriorIndices(interiorIndices);
        tess.getBoundaryIndices(outlineIndices);

        RecordIndices ri = new RecordIndices(record.ordinal);
        ri.vertexRange.location = range.location;
        ri.vertexRange.length = range.length;
        ri.interiorIndices = (IntBuffer) interiorIndices.rewind();
        ri.outlineIndices = (IntBuffer) outlineIndices.rewind();
        geom.recordIndices.add(ri);
    }

    protected boolean mustAssembleAttributeGroups(ShapefileGeometry geom)
    {
        return geom.attributeGroups.size() == 0 || geom.attributeStateID != this.recordStateID;
    }

    protected void assembleAttributeGroups(ShapefileGeometry geom)
    {
        geom.attributeGroups.clear();
        geom.attributeStateID = this.recordStateID;

        // Assemble the tile's records into groups with common attributes. Attributes are grouped by reference using an
        // InstanceHashMap, so that subsequent changes to an Attribute instance will be reflected in the record group
        // automatically. We take care to avoid assembling groups based on any Attribute property, as those properties
        // may change without re-assembling these groups. However, changes to a record's visibility state, highlight
        // state, normal attributes reference and highlight attributes reference invalidate this grouping.
        Map<ShapeAttributes, RecordGroup> attrMap = new IdentityHashMap<ShapeAttributes, RecordGroup>();
        for (RecordIndices ri : geom.recordIndices)
        {
            ShapefileRenderable.Record record = this.getRecord(ri.ordinal);
            if (!record.isVisible()) // ignore records marked as not visible
                continue;

            ShapeAttributes attrs = this.determineActiveAttributes(record);
            RecordGroup group = attrMap.get(attrs);

            if (group == null) // create a new group if one doesn't already exist
            {
                group = new RecordGroup(attrs);
                attrMap.put(attrs, group); // add it to the map to prevent duplicates
                geom.attributeGroups.add(group); // add it to the tile's attribute group list
            }

            group.recordIndices.add(ri);
            group.interiorIndexRange.length += ri.interiorIndices != null ? ri.interiorIndices.remaining() : 0;
            group.outlineIndexRange.length += ri.outlineIndices != null ? ri.outlineIndices.remaining() : 0;
        }

        // Make the indices for each record group. We take care to make indices for both the interior and the outline,
        // regardless of the current state of Attributes.isDrawInterior and Attributes.isDrawOutline. This enable these
        // properties change state without needing to re-assemble these groups.
        for (RecordGroup group : geom.attributeGroups)
        {
            int indexCount = group.interiorIndexRange.length + group.outlineIndexRange.length;
            IntBuffer indices = Buffers.newDirectIntBuffer(indexCount);

            group.interiorIndexRange.location = indices.position();
            for (RecordIndices ri : group.recordIndices) // assemble the group's triangle indices in a single contiguous range
            {
                indices.put(ri.interiorIndices);
                ri.interiorIndices.rewind();
            }

            group.outlineIndexRange.location = indices.position();
            for (RecordIndices ri : group.recordIndices) // assemble the group's line indices in a single contiguous range
            {
                indices.put(ri.outlineIndices);
                ri.outlineIndices.rewind();
            }

            group.indices = (IntBuffer) indices.rewind();
            group.recordIndices.clear();
            group.recordIndices.trimToSize(); // Reduce memory overhead from unused ArrayList capacity.
        }
    }

    protected void render(DrawContext dc, ShapefileTile tile)
    {
        try
        {
            this.beginDrawing(dc);
            this.draw(dc, tile);
        }
        finally
        {
            this.endDrawing(dc);
        }
    }

    protected void beginDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY); // all drawing uses vertex arrays
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL.GL_BLEND);
            gl.glEnable(GL.GL_LINE_SMOOTH);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    protected void endDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        gl.glColor4f(1, 1, 1, 1);
        gl.glLineWidth(1);
        gl.glPopMatrix();

        Arrays.fill(this.clipPlaneArray, 0);
        for (int i = 0; i < 4; i++)
        {
            gl.glDisable(GL2.GL_CLIP_PLANE0 + i);
            gl.glClipPlane(GL2.GL_CLIP_PLANE0 + i, this.clipPlaneArray, 4 * i);
        }

        if (!dc.isPickingMode())
        {
            gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_LINE_SMOOTH);
            gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
        }
    }

    protected void draw(DrawContext dc, ShapefileTile tile)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        ShapefileGeometry geom = tile.getGeometry();

        SurfaceTileDrawContext sdc = (SurfaceTileDrawContext) dc.getValue(AVKey.SURFACE_TILE_DRAW_CONTEXT);
        Matrix modelview = sdc.getModelviewMatrix().multiply(Matrix.fromTranslation(geom.vertexOffset));
        modelview.toArray(this.matrixArray, 0, false);
        gl.glLoadMatrixd(this.matrixArray, 0);

        gl.glVertexPointer(geom.vertexStride, GL.GL_FLOAT, 0, geom.vertices);

        this.applyClipSector(dc, tile.sector, geom.vertexOffset); // clip rasterization to the tile's sector

        if (dc.isPickingMode())
        {
            this.applyPickColors(dc, geom); // setup per-vertex colors to display records in unique pick colors
        }

        for (RecordGroup attrGroup : geom.attributeGroups) // draw groups of aggregate records with the same attrs
        {
            this.drawAttributeGroup(dc, attrGroup);
        }
    }

    protected void applyClipSector(DrawContext dc, Sector sector, Vec4 vertexOffset)
    {
        fillArray4(this.clipPlaneArray, 0, 1, 0, 0, -(sector.getMinLongitude().degrees - vertexOffset.x));
        fillArray4(this.clipPlaneArray, 4, -1, 0, 0, sector.getMaxLongitude().degrees - vertexOffset.x);
        fillArray4(this.clipPlaneArray, 8, 0, 1, 0, -(sector.getMinLatitude().degrees - vertexOffset.y));
        fillArray4(this.clipPlaneArray, 12, 0, -1, 0, sector.getMaxLatitude().degrees - vertexOffset.y);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        for (int i = 0; i < 4; i++)
        {
            gl.glEnable(GL2.GL_CLIP_PLANE0 + i);
            gl.glClipPlane(GL2.GL_CLIP_PLANE0 + i, this.clipPlaneArray, 4 * i);
        }
    }

    protected void applyPickColors(DrawContext dc, ShapefileGeometry geom)
    {
        SurfaceTileDrawContext sdc = (SurfaceTileDrawContext) dc.getValue(AVKey.SURFACE_TILE_DRAW_CONTEXT);

        if (this.pickColors == null || this.pickColors.capacity() < 3 * geom.vertexCount)
        {
            this.pickColors = Buffers.newDirectByteBuffer(3 * geom.vertexCount);
        }
        this.pickColors.clear();

        for (RecordIndices ri : geom.recordIndices)
        {
            // Assign each record a unique RGB color. Generate vertex colors for every record - regardless of its
            // visibility - since the tile's color array must match the tile's vertex array. Keep a map of record
            // ordinals to pick colors in order to avoid drawing records in more than one unique color.
            Color color = this.pickColorMap.get(ri.ordinal);
            if (color == null)
            {
                color = dc.getUniquePickColor();
                this.pickColorMap.put(ri.ordinal, color);
            }

            // Associated the record's pickable object with the pickTileBuilder's list of pick candidates. This list
            // is saved during pre rendering and used during picking.
            ShapefileRenderable.Record record = this.getRecord(ri.ordinal);
            sdc.addPickCandidate(new PickedObject(color.getRGB(), record));

            // Add the unique color each vertex of the record.
            for (int i = 0; i < ri.vertexRange.length; i++)
            {
                this.pickColors.put((byte) color.getRed()).put((byte) color.getGreen()).put((byte) color.getBlue());
            }
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        gl.glColorPointer(3, GL.GL_UNSIGNED_BYTE, 0, this.pickColors.flip());
    }

    protected void drawAttributeGroup(DrawContext dc, RecordGroup attributeGroup)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        ShapeAttributes attrs = attributeGroup.attributes;

        if (attrs.isDrawInterior() && (dc.isPickingMode() || attrs.getInteriorOpacity() > 0))
        {
            if (!dc.isPickingMode())
            {
                Color rgb = attrs.getInteriorMaterial().getDiffuse();
                double alpha = attrs.getInteriorOpacity() * 255 + 0.5;
                gl.glColor4ub((byte) rgb.getRed(), (byte) rgb.getGreen(), (byte) rgb.getBlue(), (byte) alpha);
            }

            gl.glDrawElements(GL.GL_TRIANGLES, attributeGroup.interiorIndexRange.length, GL.GL_UNSIGNED_INT,
                attributeGroup.indices.position(attributeGroup.interiorIndexRange.location));
            attributeGroup.indices.rewind();
        }

        if (attrs.isDrawOutline() && (dc.isPickingMode() || attrs.getOutlineOpacity() > 0))
        {
            if (!dc.isPickingMode())
            {
                Color rgb = attrs.getOutlineMaterial().getDiffuse();
                double alpha = attrs.getOutlineOpacity() * 255 + 0.5;
                gl.glColor4ub((byte) rgb.getRed(), (byte) rgb.getGreen(), (byte) rgb.getBlue(), (byte) alpha);
                gl.glLineWidth((float) attrs.getOutlineWidth());
            }
            else
            {
                gl.glLineWidth((float) Math.max(attrs.getOutlineWidth(), this.getOutlinePickWidth()));
            }

            gl.glDrawElements(GL.GL_LINES, attributeGroup.outlineIndexRange.length, GL.GL_UNSIGNED_INT,
                attributeGroup.indices.position(attributeGroup.outlineIndexRange.location));
            attributeGroup.indices.rewind();
        }
    }

    protected static void fillArray4(double[] array, int offset, double x, double y, double z, double w)
    {
        array[0 + offset] = x;
        array[1 + offset] = y;
        array[2 + offset] = z;
        array[3 + offset] = w;
    }

    protected void combineBounds(CombineContext cc)
    {
        cc.addBoundingSector(this.sector);
    }

    protected void combineContours(CombineContext cc)
    {
        if (!cc.getSector().intersects(this.sector))
            return;  // the shapefile does not intersect the region of interest

        this.doCombineContours(cc);
    }

    protected void doCombineContours(CombineContext cc)
    {
        // Get the records intersecting the context's sector. The implementation of getItemsInRegion may return entries
        // outside the requested sector, so we cull them further in the loop below.
        Set<Record> intersectingRecords = this.recordTree.getItemsInRegion(cc.getSector(), null);
        if (intersectingRecords.isEmpty())
            return; // no records in the context's sector

        // Compute the minimum effective area for a vertex based on the context's resolution. We convert the resolution
        // from radians to square degrees. This ensures the units are consistent with the vertex effective area computed
        // by PolylineGeneralizer, which adopts the units of the source data (degrees).
        PolylineGeneralizer generalizer = new PolylineGeneralizer();
        double resolutionDegrees = cc.getResolution() * 180.0 / Math.PI;
        double minEffectiveArea = resolutionDegrees * resolutionDegrees;

        // Recursively tessellate the records to compute the boundaries of single polygon, then forward the resultant
        // contours to the context's GLU tessellator. We perform this recursive tessellation in order to draw the union
        // of the records into the context's GLU tessellator. Since we're eliminating vertices based on the context's
        // resolution, computing this union is necessary avoids incorrectly drawing regions where the absolute winding
        // order is greater than one due to two records overlapping.
        GLUtessellator tess = GLU.gluNewTess();

        try
        {
            GLUtessellatorCallback cb = new GLUTessellatorSupport.RecursiveCallback(cc.getTessellator());
            GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, cb);
            GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, cb);
            GLU.gluTessCallback(tess, GLU.GLU_TESS_END, cb);
            GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, cb);
            GLU.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GL.GL_TRUE);
            GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO); // union winding rule
            GLU.gluTessNormal(tess, 0, 0, 1);
            GLU.gluTessBeginPolygon(tess, null);

            for (Record record : intersectingRecords)
            {
                if (!record.isVisible())
                    continue; // ignore records marked as not visible

                if (!record.sector.intersects(cc.getSector()))
                    continue; // the record quadtree may return entries outside the sector passed to getItemsInRegion

                double effectiveArea = record.sector.getDeltaLatDegrees() * record.sector.getDeltaLonDegrees();
                if (effectiveArea < minEffectiveArea)
                    continue; // ignore records that don't meet the resolution criteria

                this.computeRecordMetrics(record, generalizer);
                this.doCombineRecord(tess, cc.getSector(), minEffectiveArea, record);
            }
        }
        finally
        {
            GLU.gluTessEndPolygon(tess);
            GLU.gluDeleteTess(tess);
        }
    }

    protected void doCombineRecord(GLUtessellator tess, Sector sector, double minEffectiveArea, Record record)
    {
        for (int i = 0; i < record.getBoundaryCount(); i++)
        {
            this.doCombineBoundary(tess, sector, minEffectiveArea, record, i);
        }
    }

    protected void doCombineBoundary(GLUtessellator tess, Sector sector, double minEffectiveArea, Record record,
        int boundaryIndex)
    {
        final ClippingTessellator clipTess = new ClippingTessellator(tess, sector);

        this.tessellateBoundary(record, boundaryIndex, minEffectiveArea, new TessBoundaryCallback()
        {
            @Override
            public void beginBoundary()
            {
                clipTess.beginContour();
            }

            @Override
            public void vertex(double degreesLatitude, double degreesLongitude)
            {
                clipTess.addVertex(degreesLatitude, degreesLongitude);
            }

            @Override
            public void endBoundary()
            {
                clipTess.endContour();
            }
        });
    }

    protected interface TessBoundaryCallback
    {
        void beginBoundary();

        void vertex(double degreesLatitude, double degreesLongitude);

        void endBoundary();
    }

    protected void tessellateBoundary(Record record, int boundaryIndex, double minEffectiveArea, TessBoundaryCallback callback)
    {
        VecBuffer boundaryCoords = record.getBoundaryPoints(boundaryIndex);
        double[] boundaryEffectiveArea = record.getBoundaryEffectiveArea(boundaryIndex);
        double[] coord = new double[2];

        if (!record.isBoundaryCrossesAntimeridian(boundaryIndex))
        {
            callback.beginBoundary();
            for (int j = 0; j < boundaryCoords.getSize(); j++)
            {
                if (boundaryEffectiveArea[j] < minEffectiveArea)
                    continue; // ignore vertices that don't meet the resolution criteria

                boundaryCoords.get(j, coord); // lon, lat
                callback.vertex(coord[1], coord[0]); // lat, lon
            }
            callback.endBoundary();
        }
        else
        {
            // Copy the boundary locations into a list of LatLon instances in order to utilize existing code that
            // handles locations that cross the antimeridian.
            ArrayList<LatLon> locations = new ArrayList<LatLon>();
            for (int j = 0; j < boundaryCoords.getSize(); j++)
            {
                if (boundaryEffectiveArea[j] < minEffectiveArea)
                    continue; // ignore vertices that don't meet the resolution criteria

                boundaryCoords.get(j, coord); // lon, lat
                locations.add(LatLon.fromDegrees(coord[1], coord[0])); // lat, lon
            }

            String pole = LatLon.locationsContainPole(locations);
            if (pole != null) // wrap the boundary around the pole and along the antimeridian
            {
                callback.beginBoundary();
                for (LatLon location : LatLon.cutLocationsAlongDateLine(locations, pole, null))
                {
                    callback.vertex(location.latitude.degrees, location.longitude.degrees);
                }
                callback.endBoundary();
            }
            else // tessellate on both sides of the antimeridian
            {
                for (List<LatLon> antimeridianLocations : LatLon.repeatLocationsAroundDateline(locations))
                {
                    callback.beginBoundary();
                    for (LatLon location : antimeridianLocations)
                    {
                        callback.vertex(location.latitude.degrees, location.longitude.degrees);
                    }
                    callback.endBoundary();
                }
            }
        }
    }
}

/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.shapefile;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.nio.*;
import java.util.*;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: ShapefilePolylines.java 2303 2014-09-14 22:33:36Z dcollins $
 */
public class ShapefilePolylines extends ShapefileRenderable implements OrderedRenderable, PreRenderable
{
    public static class Record extends ShapefileRenderable.Record
    {
        // Data structures supporting drawing.
        protected Tile tile;
        protected IntBuffer outlineIndices;

        public Record(ShapefileRenderable shapefileRenderable, ShapefileRecord shapefileRecord)
        {
            super(shapefileRenderable, shapefileRecord);
        }
    }

    protected static class RecordGroup
    {
        // Record group properties.
        public final ShapeAttributes attributes;
        public ArrayList<Record> records = new ArrayList<Record>();
        // Data structures supporting drawing.
        public IntBuffer indices;
        public Range outlineIndexRange = new Range(0, 0);
        public Object vboKey = new Object();

        public RecordGroup(ShapeAttributes attributes)
        {
            this.attributes = attributes;
        }
    }

    protected static class Tile implements OrderedRenderable, SurfaceRenderable
    {
        // Tile properties.
        public ShapefileRenderable shapefileRenderable;
        public final Sector sector;
        public final int level;
        // Tile records, attribute groups and child tiles.
        public ArrayList<Record> records = new ArrayList<Record>();
        public ArrayList<RecordGroup> attributeGroups = new ArrayList<RecordGroup>();
        public long attributeStateID;
        public Tile[] children;
        // Tile shape data.
        public FloatBuffer vertices;
        public int vertexStride;
        public Vec4 referencePoint;
        public Matrix transformMatrix;
        public Object vboKey = new Object();

        public Tile(ShapefileRenderable shapefileRenderable, Sector sector, int level)
        {
            this.shapefileRenderable = shapefileRenderable;
            this.sector = sector;
            this.level = level;
        }

        @Override
        public double getDistanceFromEye()
        {
            return 0; // distance from eye is irrelevant for ordered surface renderables
        }

        @Override
        public List<Sector> getSectors(DrawContext dc)
        {
            return Arrays.asList(this.sector);
        }

        @Override
        public Object getStateKey(DrawContext dc)
        {
            return new TileStateKey(this);
        }

        @Override
        public void pick(DrawContext dc, Point pickPoint)
        {
        }

        @Override
        public void render(DrawContext dc)
        {
            ((ShapefilePolylines) this.shapefileRenderable).renderTile(dc, this);
        }
    }

    protected static class TileStateKey
    {
        protected Tile tile;
        protected long attributeStateID;
        protected ShapeAttributes[] attributeGroups;

        public TileStateKey(Tile tile)
        {
            this.tile = tile;
            this.attributeStateID = tile.attributeStateID;
            this.attributeGroups = new ShapeAttributes[tile.attributeGroups.size()];

            for (int i = 0; i < this.attributeGroups.length; i++)
            {
                this.attributeGroups[i] = tile.attributeGroups.get(i).attributes.copy();
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            TileStateKey that = (TileStateKey) o;
            return this.tile.equals(that.tile)
                && this.attributeStateID == that.attributeStateID
                && Arrays.equals(this.attributeGroups, that.attributeGroups);
        }

        @Override
        public int hashCode()
        {
            int result = this.tile.hashCode();
            result = 31 * result + (int) (this.attributeStateID ^ (this.attributeStateID >>> 32));
            result = 31 * result + Arrays.hashCode(this.attributeGroups);
            return result;
        }
    }

    /** The default outline pick width. */
    protected static final int DEFAULT_OUTLINE_PICK_WIDTH = 10;

    // Tile quadtree structures.
    protected Tile rootTile;
    protected int tileMaxLevel = 3;
    protected int tileMaxCapacity = 10000;
    // Data structures supporting polygon tessellation and drawing.
    protected ArrayList<Tile> currentTiles = new ArrayList<Tile>();
    protected PolylineTessellator tess = new PolylineTessellator();
    protected byte[] colorByteArray = new byte[3];
    protected float[] colorFloatArray = new float[4];
    protected double[] matrixArray = new double[16];
    // Data structures supporting picking.
    protected int outlinePickWidth = DEFAULT_OUTLINE_PICK_WIDTH;
    protected Layer pickLayer;
    protected PickSupport pickSupport = new PickSupport();
    protected SurfaceObjectTileBuilder pickTileBuilder = new SurfaceObjectTileBuilder(new Dimension(512, 512),
        GL2.GL_RGBA8, false, false);
    protected ByteBuffer pickColors;
    protected Object pickColorsVboKey = new Object();

    /**
     * Creates a new ShapefilePolylines with the specified shapefile. The normal attributes and the highlight attributes
     * for each ShapefileRenderable.Record are assigned default values. In order to modify ShapefileRenderable.Record
     * shape attributes or key-value attributes during construction, use {@link #ShapefilePolylines(gov.nasa.worldwind.formats.shapefile.Shapefile,
     * gov.nasa.worldwind.render.ShapeAttributes, gov.nasa.worldwind.render.ShapeAttributes,
     * gov.nasa.worldwind.formats.shapefile.ShapefileRenderable.AttributeDelegate)}.
     *
     * @param shapefile The shapefile to display.
     *
     * @throws IllegalArgumentException if the shapefile is null.
     */
    public ShapefilePolylines(Shapefile shapefile)
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
     * Creates a new ShapefilePolylines with the specified shapefile. The normal attributes, the highlight attributes
     * and the attribute delegate are optional. Specifying a non-null value for normalAttrs or highlightAttrs causes
     * each ShapefileRenderable.Record to adopt those attributes. Specifying a non-null value for the attribute delegate
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
    public ShapefilePolylines(Shapefile shapefile, ShapeAttributes normalAttrs, ShapeAttributes highlightAttrs,
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
     * <p>
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
        return 0;
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

        if (this.getRecordCount() == 0) // Shapefile is empty or contains only null records.
            return;

        // Assemble the tiles used for rendering, then cause those tiles to be drawn into the scene controller's
        // composite surface object tiles.
        this.assembleTiles(dc);
        for (Tile tile : this.currentTiles)
        {
            dc.addOrderedSurfaceRenderable(tile);
        }

        // Assemble the tiles used for picking, then build a set of surface object tiles containing unique colors for
        // each record.
        if (dc.getCurrentLayer().isPickEnabled())
        {
            try
            {
                dc.enablePickingMode();
                this.assembleTiles(dc);
                this.pickSupport.clearPickList();
                this.pickTileBuilder.setForceTileUpdates(true); // force pick tiles to update with new pick colors
                this.pickTileBuilder.buildTiles(dc, this.currentTiles); // draw tiles and add candidates to pickSupport
            }
            finally
            {
                dc.disablePickingMode();
            }
        }
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

        int recordCount = this.getRecordCount();
        if (recordCount == 0) // Shapefile is empty or contains only null records.
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        try
        {
            // pick list cleared in preRender
            this.pickSupport.beginPicking(dc);
            gl.glEnable(GL.GL_CULL_FACE);
            dc.getGeographicSurfaceTileRenderer().setUseImageTilePickColors(true);
            dc.getGeographicSurfaceTileRenderer().renderTiles(dc, this.pickTileBuilder.getTiles(dc));
        }
        finally
        {
            dc.getGeographicSurfaceTileRenderer().setUseImageTilePickColors(false);
            gl.glDisable(GL.GL_CULL_FACE);
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer);
            this.pickTileBuilder.clearTiles(dc);
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

        if (this.getRecordCount() == 0) // Shapefile is empty or contains only null records.
            return;

        if (dc.isPickingMode() && this.pickTileBuilder.getTileCount(dc) > 0)
        {
            this.pickLayer = dc.getCurrentLayer();
            dc.addOrderedSurfaceRenderable(this); // perform the pick during ordered surface rendering
        }
    }

    @Override
    protected void assembleRecords(Shapefile shapefile)
    {
        this.rootTile = new Tile(this, this.sector, 0);

        super.assembleRecords(shapefile);

        if (this.mustSplitTile(this.rootTile))
        {
            this.splitTile(this.rootTile);
        }

        this.rootTile.records.trimToSize(); // Reduce memory overhead from unused ArrayList capacity.
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
        Record record = this.createRecord(shapefileRecord);
        this.addRecord(shapefileRecord, record);

        this.rootTile.records.add(record);
        record.tile = this.rootTile;
    }

    protected ShapefilePolylines.Record createRecord(ShapefileRecord shapefileRecord)
    {
        return new ShapefilePolylines.Record(this, shapefileRecord);
    }

    @Override
    protected void recordDidChange(ShapefileRenderable.Record record)
    {
        Tile tile = ((ShapefilePolylines.Record) record).tile;
        if (tile != null) // tile is null when attributes are specified during construction
        {
            this.invalidateTileAttributeGroups(tile);
        }
    }

    protected boolean mustSplitTile(Tile tile)
    {
        return tile.level < this.tileMaxLevel && tile.records.size() > this.tileMaxCapacity;
    }

    protected void splitTile(Tile tile)
    {
        // Create four child tiles by subdividing the tile's sector in latitude and longitude.
        Sector[] childSectors = tile.sector.subdivide();
        tile.children = new Tile[4];
        tile.children[0] = new Tile(this, childSectors[0], tile.level + 1);
        tile.children[1] = new Tile(this, childSectors[1], tile.level + 1);
        tile.children[2] = new Tile(this, childSectors[2], tile.level + 1);
        tile.children[3] = new Tile(this, childSectors[3], tile.level + 1);

        // Move any records completely contained in a child tile's sector into the child's list of records. This may
        // include records that are marked as not visible, as recomputing the tile tree for record visibility changes
        // would be expensive.
        Iterator<Record> iterator = tile.records.iterator();
        while (iterator.hasNext())
        {
            Record record = iterator.next();
            for (int i = 0; i < 4; i++)
            {
                if (tile.children[i].sector.contains(record.sector))
                {
                    tile.children[i].records.add(record); // add it to the child
                    record.tile = tile.children[i]; // assign the record's tile
                    iterator.remove(); // remove it from the parent
                    break; // skip to the next record
                }
            }
        }

        // Recursively split child tiles as necessary, moving their records into each child's descendants. The recursive
        // split stops when a child tile reaches a maximum level, or when the number of records contained within the
        // tile is small enough.
        for (int i = 0; i < 4; i++)
        {
            if (this.mustSplitTile(tile.children[i]))
            {
                this.splitTile(tile.children[i]);
            }

            tile.children[i].records.trimToSize(); // Reduce memory overhead from unused ArrayList capacity.
        }
    }

    protected void assembleTiles(DrawContext dc)
    {
        this.currentTiles.clear();
        this.addTileOrDescendants(dc, this.rootTile);
    }

    protected void addTileOrDescendants(DrawContext dc, Tile tile)
    {
        // Determine whether or not the tile is visible. If the tile is not visible, then neither are the tile's records
        // or the tile's children. Note that a tile with no records may have children, so we can't use the tile's record
        // count as a determination of whether or not to test its children.
        if (!this.isTileVisible(dc, tile))
        {
            return;
        }

        // Add the tile to the list of tiles to draw, regenerating the tile's geometry and the tile's attribute groups
        // as necessary.
        if (tile.records.size() > 0)
        {
            if (this.mustRegenerateTileGeometry(tile))
            {
                this.regenerateTileGeometry(tile);
            }

            if (this.mustAssembleTileAttributeGroups(tile))
            {
                this.assembleTileAttributeGroups(tile);
            }

            this.currentTiles.add(tile);
        }

        // Process the tile's children, if any.
        if (tile.children != null)
        {
            for (Tile childTile : tile.children)
            {
                this.addTileOrDescendants(dc, childTile);
            }
        }
    }

    protected boolean isTileVisible(DrawContext dc, Tile tile)
    {
        Extent extent = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), tile.sector);

        if (dc.isSmall(extent, 1))
        {
            return false;
        }

        if (dc.isPickingMode())
        {
            return dc.getPickFrustums().intersectsAny(extent);
        }

        return dc.getView().getFrustumInModelCoordinates().intersects(extent);
    }

    protected boolean mustRegenerateTileGeometry(Tile tile)
    {
        return tile.vertices == null;
    }

    protected void regenerateTileGeometry(Tile tile)
    {
        this.tessellateTile(tile);
    }

    protected void tessellateTile(Tile tile)
    {
        int numPoints = 0;
        for (Record record : tile.records)
        {
            numPoints += record.numberOfPoints;
        }

        // Allocate the geographic coordinate vertices to hold the coordinates for all records in the tile. The records
        // in the tile never changes, so the number of vertices in the tile never changes.
        int vertexStride = 2;
        FloatBuffer vertices = Buffers.newDirectFloatBuffer(vertexStride * numPoints);

        double[] location = new double[2];
        float[] vertex = new float[2];
        Vec4 rp = null;

        // Generate the geographic coordinate vertices and indices for all records in the tile. This may include records
        // that are marked as not visible, as recomputing the vertices and indices for record visibility changes would
        // be expensive. The tessellated indices are generated only once, since each record's indices never change.
        for (Record record : tile.records)
        {
            this.tess.reset();

            for (int i = 0; i < record.getBoundaryCount(); i++)
            {
                this.tess.beginPolyline();

                VecBuffer points = record.getBoundaryPoints(i);
                for (int j = 0; j < points.getSize(); j++)
                {
                    points.get(j, location);
                    double x = location[0]; // map longitude to x
                    double y = location[1]; // map latitude to y

                    int index = vertices.position() / vertexStride;
                    this.tess.addVertex(x, y, 0, index);

                    if (rp == null) // first vertex in the tile
                    {
                        rp = new Vec4(x, y, 0);
                    }

                    vertex[0] = (float) (x - rp.x);
                    vertex[1] = (float) (y - rp.y);
                    vertices.put(vertex);
                }

                this.tess.endPolyline();
            }

            this.assembleRecordIndices(this.tess, record);
        }

        tile.vertices = (FloatBuffer) vertices.rewind();
        tile.vertexStride = vertexStride;
        tile.referencePoint = rp;
        tile.transformMatrix = Matrix.fromTranslation(rp.x, rp.y, rp.z);
    }

    protected void assembleRecordIndices(PolylineTessellator tessellator, Record record)
    {
        // Get the tessellated boundary indices representing a line segment tessellation of the record parts.
        // Flip each buffer in order to limit the buffer range we use to values added during tessellation.
        IntBuffer tessBoundary = (IntBuffer) tessellator.getIndices().flip();

        // Allocate and fill the record's outline indices.
        IntBuffer outlineIndices = IntBuffer.allocate(tessBoundary.remaining());
        outlineIndices.put(tessBoundary);

        record.outlineIndices = (IntBuffer) outlineIndices.rewind();
    }

    protected void invalidateTileAttributeGroups(Tile tile)
    {
        tile.attributeGroups.clear();
    }

    protected boolean mustAssembleTileAttributeGroups(Tile tile)
    {
        return tile.attributeGroups.isEmpty();
    }

    protected void assembleTileAttributeGroups(Tile tile)
    {
        tile.attributeGroups.clear();
        tile.attributeStateID++;

        // Assemble the tile's records into groups with common attributes. Attributes are compared using the instance's
        // address, so subsequent changes to an Attribute instance will be reflected in the record group automatically.
        // We take care to avoid assembling groups based on any Attribute property, as those properties may change
        // without re-assembling these groups. However, changes to a record's visibility state, highlight state, normal
        // attributes reference and highlight attributes reference invalidate this grouping.
        HashMap<ShapeAttributes, RecordGroup> attrMap = new HashMap<ShapeAttributes, RecordGroup>();
        for (Record record : tile.records)
        {
            if (!record.isVisible()) // ignore records marked as not visible
                continue;

            ShapeAttributes attrs = this.determineActiveAttributes(record);
            RecordGroup group = attrMap.get(attrs);

            if (group == null) // create a new group if one doesn't already exist
            {
                group = new RecordGroup(attrs);
                attrMap.put(attrs, group); // add it to the map to prevent duplicates
                tile.attributeGroups.add(group); // add it to the tile's attribute group list
            }

            group.records.add(record);
            group.outlineIndexRange.length += record.outlineIndices.remaining();
        }

        // Make the indices for each record group. We take care to make indices for both the interior and the outline,
        // regardless of the current state of Attributes.isDrawInterior and Attributes.isDrawOutline. This enable these
        // properties change state without needing to re-assemble these groups.
        for (RecordGroup group : tile.attributeGroups)
        {
            int indexCount = group.outlineIndexRange.length;
            IntBuffer indices = Buffers.newDirectIntBuffer(indexCount);

            group.outlineIndexRange.location = indices.position();
            for (Record record : group.records) // assemble the group's line indices in a single contiguous range
            {
                indices.put(record.outlineIndices);
                record.outlineIndices.rewind();
            }

            group.indices = (IntBuffer) indices.rewind();
            group.records.clear();
            group.records.trimToSize(); // Reduce memory overhead from unused ArrayList capacity.
        }
    }

    protected void renderTile(DrawContext dc, Tile tile)
    {
        this.beginDrawing(dc);
        try
        {
            if (dc.isPickingMode())
            {
                this.drawTileInUniqueColors(dc, tile);
            }
            else
            {
                this.drawTile(dc, tile);
            }
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
        gl.glColor4f(1, 1, 1, 1);
        gl.glLineWidth(1);
        gl.glPopMatrix();

        if (!dc.isPickingMode())
        {
            gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_LINE_SMOOTH);
            gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
        }

        if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    protected void drawTile(DrawContext dc, Tile tile)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        int[] vboId = null;
        boolean useVbo = dc.getGLRuntimeCapabilities().isUseVertexBufferObject();
        if (useVbo && (vboId = (int[]) dc.getGpuResourceCache().get(tile.vboKey)) == null)
        {
            long vboSize = 4 * tile.vertices.remaining(); // 4 bytes for each float vertex component
            vboId = new int[1];
            gl.glGenBuffers(1, vboId, 0);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, vboSize, tile.vertices, GL.GL_STATIC_DRAW);
            gl.glVertexPointer(tile.vertexStride, GL.GL_FLOAT, 0, 0);
            dc.getGpuResourceCache().put(tile.vboKey, vboId, GpuResourceCache.VBO_BUFFERS, vboSize);
        }
        else if (useVbo)
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
            gl.glVertexPointer(tile.vertexStride, GL.GL_FLOAT, 0, 0);
        }
        else
        {
            gl.glVertexPointer(tile.vertexStride, GL.GL_FLOAT, 0, tile.vertices);
        }

        SurfaceTileDrawContext sdc = (SurfaceTileDrawContext) dc.getValue(AVKey.SURFACE_TILE_DRAW_CONTEXT);
        Matrix modelview = sdc.getModelviewMatrix().multiply(tile.transformMatrix);
        modelview.toArray(this.matrixArray, 0, false);
        gl.glLoadMatrixd(this.matrixArray, 0);

        for (RecordGroup attrGroup : tile.attributeGroups)
        {
            this.drawTileAttributeGroup(dc, attrGroup);
        }
    }

    protected void drawTileAttributeGroup(DrawContext dc, RecordGroup attributeGroup)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        ShapeAttributes attrs = attributeGroup.attributes;

        if (!attrs.isDrawOutline())
            return;

        int[] vboId = null;
        boolean useVbo = dc.getGLRuntimeCapabilities().isUseVertexBufferObject();
        if (useVbo && (vboId = (int[]) dc.getGpuResourceCache().get(attributeGroup.vboKey)) == null)
        {
            long vboSize = 4 * attributeGroup.indices.remaining(); // 4 bytes for each unsigned int index
            vboId = new int[1];
            gl.glGenBuffers(1, vboId, 0);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboId[0]);
            gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, vboSize, attributeGroup.indices, GL.GL_STATIC_DRAW);
            dc.getGpuResourceCache().put(attributeGroup.vboKey, vboId, GpuResourceCache.VBO_BUFFERS, vboSize);
        }
        else if (useVbo)
        {
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboId[0]);
        }

        if (!dc.isPickingMode())
        {
            float[] color = this.colorFloatArray;
            attrs.getOutlineMaterial().getDiffuse().getRGBComponents(color);
            gl.glColor4f(color[0], color[1], color[2], color[3]);
        }

        if (dc.isPickingMode() && attrs.getOutlineWidth() < this.getOutlinePickWidth())
            gl.glLineWidth(this.getOutlinePickWidth());
        else
            gl.glLineWidth((float) attrs.getOutlineWidth());

        if (useVbo)
        {
            gl.glDrawElements(GL.GL_LINES, attributeGroup.indices.remaining(), GL.GL_UNSIGNED_INT, 0);
        }
        else
        {
            gl.glDrawElements(GL.GL_LINES, attributeGroup.indices.remaining(), GL.GL_UNSIGNED_INT,
                attributeGroup.indices);
        }
    }

    protected void drawTileInUniqueColors(DrawContext dc, Tile tile)
    {
        GL2 gl = dc.getGL().getGL2();

        int pickColorsSize = 3 * (tile.vertices.remaining() / tile.vertexStride); // 1 RGB color for each XY vertex
        if (this.pickColors == null || this.pickColors.capacity() < pickColorsSize)
        {
            this.pickColors = Buffers.newDirectByteBuffer(pickColorsSize);
            dc.getGpuResourceCache().remove(this.pickColorsVboKey); // remove any associated VBO from GPU memory
        }
        this.pickColors.clear();

        ByteBuffer colors;
        int[] vboId = null;
        boolean useVbo = dc.getGLRuntimeCapabilities().isUseVertexBufferObject();
        if (useVbo && (vboId = (int[]) dc.getGpuResourceCache().get(this.pickColorsVboKey)) == null)
        {
            vboId = new int[1];
            gl.glGenBuffers(1, vboId, 0);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, this.pickColors.remaining(), this.pickColors, GL2.GL_DYNAMIC_DRAW);
            dc.getGpuResourceCache().put(this.pickColorsVboKey, vboId, GpuResourceCache.VBO_BUFFERS,
                this.pickColors.remaining());
            colors = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
        }
        else if (useVbo)
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
            colors = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
        }
        else
        {
            colors = pickColors;
        }

        byte[] vertexColors = this.colorByteArray;
        for (Record record : tile.records)
        {
            // Assign each record a unique RGB color. Generate vertex colors for every record - regardless of its
            // visibility - since the tile's color array must match the tile's vertex array.
            Color color = dc.getUniquePickColor();
            this.pickSupport.addPickableObject(color.getRGB(), record);
            vertexColors[0] = (byte) color.getRed();
            vertexColors[1] = (byte) color.getGreen();
            vertexColors[2] = (byte) color.getBlue();

            // Add the unique color each vertex of the record.
            for (int i = 0; i < record.numberOfPoints; i++)
            {
                colors.put(vertexColors, 0, 3);
            }
        }

        colors.flip();

        try
        {
            this.pickSupport.beginPicking(dc);
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);

            if (useVbo)
            {
                gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
                gl.glColorPointer(3, GL.GL_UNSIGNED_BYTE, 0, 0);
            }
            else
            {
                gl.glColorPointer(3, GL.GL_UNSIGNED_BYTE, 0, colors);
            }

            this.drawTile(dc, tile);
        }
        finally
        {
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
            this.pickSupport.endPicking(dc);
        }
    }
}

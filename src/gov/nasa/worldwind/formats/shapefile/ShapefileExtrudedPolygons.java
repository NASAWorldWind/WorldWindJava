/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.shapefile;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.awt.*;
import java.nio.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

/**
 * @author dcollins
 * @version $Id: ShapefileExtrudedPolygons.java 2324 2014-09-17 20:25:35Z dcollins $
 */
public class ShapefileExtrudedPolygons extends ShapefileRenderable implements OrderedRenderable
{
    public static class Record extends ShapefileRenderable.Record
    {
        // Record properties.
        protected Double height; // may be null
        // Data structures supporting drawing.
        protected Tile tile;
        protected IntBuffer interiorIndices;
        protected IntBuffer outlineIndices;

        public Record(ShapefileRenderable shapefileRenderable, ShapefileRecord shapefileRecord)
        {
            super(shapefileRenderable, shapefileRecord);

            this.height = ShapefileUtils.extractHeightAttribute(shapefileRecord); // may be null
        }

        public Double getHeight()
        {
            return this.height;
        }

        public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException
        {
            if (line == null)
            {
                String msg = Logging.getMessage("nullValue.LineIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            if (terrain == null)
            {
                String msg = Logging.getMessage("nullValue.TerrainIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            if (!this.visible) // records marked as not visible don't intersect anything
            {
                return null;
            }

            ArrayList<Intersection> intersections = new ArrayList<Intersection>();
            ((ShapefileExtrudedPolygons) this.shapefileRenderable).intersectTileRecord(line, terrain, this,
                intersections);

            return intersections.size() > 0 ? intersections : null;
        }
    }

    protected static class RecordGroup
    {
        // Record group properties.
        public final ShapeAttributes attributes;
        public ArrayList<Record> records = new ArrayList<Record>();
        // Data structures supporting drawing.
        public IntBuffer indices;
        public Range interiorIndexRange = new Range(0, 0);
        public Range outlineIndexRange = new Range(0, 0);
        public Object vboKey = new Object();

        public RecordGroup(ShapeAttributes attributes)
        {
            this.attributes = attributes;
        }
    }

    protected static class Tile
    {
        // Tile properties.
        public final Sector sector;
        public final int level;
        // Tile records, attribute groups and child tiles.
        public ArrayList<Record> records = new ArrayList<Record>();
        public ArrayList<RecordGroup> attributeGroups = new ArrayList<RecordGroup>();
        public Tile[] children;
        // Tile shape data.
        public ShapeDataCache dataCache = new ShapeDataCache(60000);
        public ShapeData currentData;
        public IntersectionData intersectionData = new IntersectionData();

        public Tile(Sector sector, int level)
        {
            this.sector = sector;
            this.level = level;
        }
    }

    protected static class ShapeData extends ShapeDataCache.ShapeDataCacheEntry
    {
        public FloatBuffer vertices;
        public Vec4 referencePoint;
        public Matrix transformMatrix;
        public Object vboKey = new Object();
        public boolean vboExpired;

        public ShapeData(DrawContext dc, long minExpiryTime, long maxExpiryTime)
        {
            super(dc, minExpiryTime, maxExpiryTime);
        }
    }

    protected static class IntersectionData extends ShapeData
    {
        protected Terrain terrain;
        protected boolean tessellationValid;

        public IntersectionData()
        {
            super(null, 0, 0);
        }

        public boolean isValid(Terrain terrain)
        {
            return this.terrain == terrain
                && this.verticalExaggeration == terrain.getVerticalExaggeration()
                && (this.globeStateKey != null && globeStateKey.equals(terrain.getGlobe().getGlobeStateKey()));
        }

        public void invalidate()
        {
            this.terrain = null;
            this.verticalExaggeration = 1;
            this.globeStateKey = null;
            this.tessellationValid = false;
        }

        public Terrain getTerrain()
        {
            return this.terrain;
        }

        public void setTerrain(Terrain terrain)
        {
            this.terrain = terrain;
        }

        public boolean isTessellationValid()
        {
            return this.tessellationValid;
        }

        public void setTessellationValid(boolean valid)
        {
            this.tessellationValid = valid;
        }
    }

    // Properties.
    protected double defaultHeight;
    protected double defaultBaseDepth;
    protected double maxHeight;
    // Tile quadtree structures.
    protected Tile rootTile;
    protected int tileMaxLevel = 3;
    protected int tileMaxCapacity = 10000;
    // Data structures supporting polygon tessellation and drawing.
    protected ArrayList<Tile> currentTiles = new ArrayList<Tile>();
    protected PolygonTessellator tess = new PolygonTessellator();
    protected byte[] colorByteArray = new byte[6];
    protected float[] colorFloatArray = new float[3];
    protected double[] matrixArray = new double[16];
    // Data structures supporting picking.
    protected Layer pickLayer;
    protected PickSupport pickSupport = new PickSupport();
    protected ByteBuffer pickColors;
    protected Object pickColorsVboKey = new Object();

    /**
     * Creates a new ShapefileExtrudedPolygons with the specified shapefile. The normal attributes and the highlight
     * attributes for each ShapefileRenderable.Record are assigned default values. In order to modify
     * ShapefileRenderable.Record shape attributes or key-value attributes during construction, use {@link
     * #ShapefileExtrudedPolygons(Shapefile, gov.nasa.worldwind.render.ShapeAttributes,
     * gov.nasa.worldwind.render.ShapeAttributes, gov.nasa.worldwind.formats.shapefile.ShapefileRenderable.AttributeDelegate)}.
     *
     * @param shapefile The shapefile to display.
     *
     * @throws IllegalArgumentException if the shapefile is null.
     */
    public ShapefileExtrudedPolygons(Shapefile shapefile)
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
     * Creates a new ShapefileExtrudedPolygons with the specified shapefile. The normal attributes, the highlight
     * attributes and the attribute delegate are optional. Specifying a non-null value for normalAttrs or highlightAttrs
     * causes each ShapefileRenderable.Record to adopt those attributes. Specifying a non-null value for the attribute
     * delegate enables callbacks during creation of each ShapefileRenderable.Record. See {@link AttributeDelegate} for
     * more information.
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
    public ShapefileExtrudedPolygons(Shapefile shapefile, ShapeAttributes normalAttrs, ShapeAttributes highlightAttrs,
        ShapefileRenderable.AttributeDelegate attributeDelegate)
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
        this.rootTile = new Tile(this.sector, 0);

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

        if (record.height != null && this.maxHeight < record.height)
        {
            this.maxHeight = record.height;
        }

        this.rootTile.records.add(record);
        record.tile = this.rootTile;
    }

    protected ShapefileExtrudedPolygons.Record createRecord(ShapefileRecord shapefileRecord)
    {
        return new ShapefileExtrudedPolygons.Record(this, shapefileRecord);
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
        tile.children[0] = new Tile(childSectors[0], tile.level + 1);
        tile.children[1] = new Tile(childSectors[1], tile.level + 1);
        tile.children[2] = new Tile(childSectors[2], tile.level + 1);
        tile.children[3] = new Tile(childSectors[3], tile.level + 1);

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

    @Override
    protected void recordDidChange(ShapefileRenderable.Record record)
    {
        Tile tile = ((ShapefileExtrudedPolygons.Record) record).tile;
        if (tile != null) // tile is null when attributes are specified during construction
        {
            this.invalidateTileAttributeGroups(tile);
        }
    }

    public double getDefaultHeight()
    {
        return this.defaultHeight;
    }

    public void setDefaultHeight(double defaultHeight)
    {
        this.defaultHeight = defaultHeight;
        this.invalidateAllTileGeometry();
    }

    public double getDefaultBaseDepth()
    {
        return this.defaultBaseDepth;
    }

    public void setDefaultBaseDepth(double defaultBaseDepth)
    {
        this.defaultBaseDepth = defaultBaseDepth;
        this.invalidateAllTileGeometry();
    }

    @Override
    public double getDistanceFromEye()
    {
        return 0;
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

        if (this.rootTile == null) // Shapefile is empty or contains only null records.
            return;

        this.pickOrderedSurfaceRenderable(dc, pickPoint); // pick is called during ordered rendering
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

        if (this.rootTile == null) // Shapefile is empty or contains only null records.
            return;

        if (dc.isOrderedRenderingMode())
            this.drawOrderedSurfaceRenderable(dc);
        else
            this.makeOrderedSurfaceRenderable(dc);
    }

    protected void makeOrderedSurfaceRenderable(DrawContext dc)
    {
        this.assembleTiles(dc); // performs a visibility test against the top level tile

        if (this.currentTiles.isEmpty()) // don't add an ordered renderable when there's nothing to draw
        {
            return;
        }

        this.pickLayer = dc.getCurrentLayer();
        dc.addOrderedSurfaceRenderable(this);
    }

    protected void assembleTiles(DrawContext dc)
    {
        this.currentTiles.clear();
        this.addTileOrDescendants(dc, this.rootTile);
    }

    protected void addTileOrDescendants(DrawContext dc, Tile tile)
    {
        // Get or create the tile's current shape data, which holds the rendered geometry for the current draw context.
        tile.currentData = (ShapeData) tile.dataCache.getEntry(dc.getGlobe());
        if (tile.currentData == null)
        {
            tile.currentData = new ShapeData(dc, 3000, 9000);
            tile.dataCache.addEntry(tile.currentData);
        }

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
            this.adjustTileExpiration(dc, tile); // reduce the remaining expiration time as the eye distance decreases
            if (this.mustRegenerateTileGeometry(dc, tile))
            {
                this.regenerateTileGeometry(dc, tile);
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
        Extent extent = this.makeTileExtent(dc.getTerrain(), tile);

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

    protected boolean mustRegenerateTileGeometry(DrawContext dc, Tile tile)
    {
        return tile.currentData.isExpired(dc) || !tile.currentData.isValid(dc);
    }

    protected void adjustTileExpiration(DrawContext dc, Tile tile)
    {
        // If the new eye distance is significantly closer than cached data's the current eye distance, reduce the
        // timer's remaining time by 50%. This reduction is performed only once each time the timer is reset.
        if (tile.currentData.referencePoint != null)
        {
            double newEyeDistance = dc.getView().getEyePoint().distanceTo3(tile.currentData.referencePoint);
            tile.currentData.adjustTimer(dc, newEyeDistance);
        }
    }

    protected void invalidateTileGeometry(Tile tile)
    {
        tile.dataCache.setAllExpired(true); // force the tile vertices to be regenerated

        synchronized (tile) // synchronize access to tile intersection data
        {
            tile.intersectionData.invalidate();
        }
    }

    protected void invalidateAllTileGeometry()
    {
        Queue<Tile> tileQueue = new ArrayDeque<Tile>();
        tileQueue.add(this.rootTile);

        while (!tileQueue.isEmpty())
        {
            Tile tile = tileQueue.poll();
            this.invalidateTileGeometry(tile);

            if (tile.children != null)
            {
                tileQueue.addAll(Arrays.asList(tile.children));
            }
        }
    }

    protected void regenerateTileGeometry(DrawContext dc, Tile tile)
    {
        ShapeData shapeData = tile.currentData;

        // Synchronize simultaneous tile updates between rendering, intersect and Record.intersect. Access to this
        // instance's coordinate buffer must be synchronized.
        synchronized (this)
        {
            this.tessellateTile(dc.getTerrain(), tile, shapeData);
        }

        shapeData.setEyeDistance(dc.getView().getEyePoint().distanceTo3(shapeData.referencePoint));
        shapeData.setGlobeStateKey(dc.getGlobe().getGlobeStateKey(dc));
        shapeData.setVerticalExaggeration(dc.getVerticalExaggeration());
        shapeData.restartTimer(dc);
    }

    protected Extent makeTileExtent(Terrain terrain, Tile tile)
    {
        // Compute the tile's minimum and maximum height as height above and below the extreme elevations in the tile's
        // sector. We use the overall maximum height of all records in order to ensure that a tile's extent includes its
        // descendants when the parent tile's max height is less than its descendants.
        double[] extremes = terrain.getGlobe().getMinAndMaxElevations(tile.sector);
        double minHeight = extremes[0] - this.defaultBaseDepth;
        double maxHeight = extremes[1] + Math.max(this.maxHeight, this.defaultHeight);

        // Compute the tile's extent for the specified terrain. Associated the shape data's extent with the terrain in
        // order to determine when it becomes invalid.
        return Sector.computeBoundingBox(terrain.getGlobe(), terrain.getVerticalExaggeration(), tile.sector,
            minHeight, maxHeight);
    }

    protected void tessellateTile(Terrain terrain, Tile tile, ShapeData shapeData)
    {
        // Allocate the model coordinate vertices to hold the upper and lower points for all records in the tile. The
        // records in the tile never changes, so the number of vertices in the tile never changes.
        int vertexStride = 3;
        FloatBuffer vertices = shapeData.vertices;
        if (vertices == null)
        {
            int numPoints = 0;
            for (Record record : tile.records)
            {
                numPoints += record.numberOfPoints;
            }

            vertices = Buffers.newDirectFloatBuffer(2 * vertexStride * numPoints);
        }

        double[] location = new double[2];
        float[] vertex = new float[6];
        Vec4 rp = null;

        // Generate the model coordinate vertices and indices for all records in the tile. This may include records that
        // are marked as not visible, as recomputing the vertices and indices for record visibility changes would be
        // expensive. The tessellated interior and outline indices are generated only once, since each record's indices
        // never change.
        for (Record record : tile.records)
        {
            double height = record.height != null ? record.height : this.defaultHeight;
            double depth = this.defaultBaseDepth;
            double NdotR = 0;
            Vec4 N = null;

            this.tess.setEnabled(record.interiorIndices == null); // generate polygon interior and outline indices once
            this.tess.reset();
            this.tess.setPolygonNormal(0, 0, 1); // tessellate in geographic coordinates
            this.tess.beginPolygon();

            for (int i = 0; i < record.getBoundaryCount(); i++)
            {
                this.tess.beginContour();

                VecBuffer points = record.getBoundaryPoints(i);
                for (int j = 0; j < points.getSize(); j++)
                {
                    points.get(j, location);
                    Vec4 p = terrain.getSurfacePoint(Angle.fromDegrees(location[1]), Angle.fromDegrees(location[0]), 0);

                    // Tessellate indices in geographic coordinates. This produces an index tessellation that is
                    // independent of the record's model coordinates, since the count and organization of top and bottom
                    // of vertices is always the same.
                    int index = vertices.position() / vertexStride; // index of top vertex
                    this.tess.addVertex(location[0], location[1], 0, index); // map lon,lat to x,y

                    if (rp == null) // first vertex in the tile
                    {
                        rp = p;
                    }

                    if (N == null) // first vertex in the record
                    {
                        N = terrain.getGlobe().computeSurfaceNormalAtPoint(p);
                        NdotR = p.x * N.x + p.y * N.y + p.z * N.z;
                    }

                    // Add the model coordinate top and bottom vertices, with heights relative to the terrain.
                    double t = height + NdotR - (p.x * N.x + p.y * N.y + p.z * N.z);
                    double b = -depth;
                    vertex[0] = (float) (p.x + N.x * t - rp.x);
                    vertex[1] = (float) (p.y + N.y * t - rp.y);
                    vertex[2] = (float) (p.z + N.z * t - rp.z);
                    vertex[3] = (float) (p.x + N.x * b - rp.x);
                    vertex[4] = (float) (p.y + N.y * b - rp.y);
                    vertex[5] = (float) (p.z + N.z * b - rp.z);
                    vertices.put(vertex);
                }

                this.tess.endContour();
            }

            this.tess.endPolygon();
            this.assembleRecordIndices(this.tess, record);
        }

        shapeData.vertices = (FloatBuffer) vertices.rewind();
        shapeData.referencePoint = rp;
        shapeData.transformMatrix = Matrix.fromTranslation(rp.x, rp.y, rp.z);
        shapeData.vboExpired = true;
    }

    protected void assembleRecordIndices(PolygonTessellator tessellator, Record record)
    {
        if (!tessellator.isEnabled())
            return;

        // Get the tessellated interior and boundary indices, representing a triangle tessellation and line segment
        // tessellation of the record's top vertices. Flip each buffer in order to limit the buffer range we use to
        // values added during tessellation.
        IntBuffer tessInterior = (IntBuffer) tessellator.getInteriorIndices().flip();
        IntBuffer tessBoundary = (IntBuffer) tessellator.getBoundaryIndices().flip();

        // Allocate the record's interior and outline indices. This accounts for the number of tessellated interior
        // and boundary indices, plus the indices necessary to tessellate the record's sides with triangles and lines.
        IntBuffer interiorIndices = IntBuffer.allocate(tessInterior.remaining() + 3 * tessBoundary.remaining());
        IntBuffer outlineIndices = IntBuffer.allocate(2 * tessBoundary.remaining());

        // Fill the triangle index buffer with the triangle tessellation of the polygon's top vertices.
        interiorIndices.put(tessInterior);

        // Fill the triangle index buffer with a triangle tessellation using two triangles to connect the top and bottom
        // vertices at each boundary line. Fill the line index buffer with a horizontal line for each boundary line
        // segment, and a vertical line at the first vertex of each boundary line segment.
        for (int i = tessBoundary.position(); i < tessBoundary.limit(); i += 2)
        {
            int top1 = tessBoundary.get(i);
            int top2 = tessBoundary.get(i + 1);
            int bot1 = top1 + 1; // top and bottom vertices are adjacent
            int bot2 = top2 + 1;
            // side top left triangle
            interiorIndices.put(top1);
            interiorIndices.put(bot1);
            interiorIndices.put(top2);
            // side bottom right triangle
            interiorIndices.put(top2);
            interiorIndices.put(bot1);
            interiorIndices.put(bot2);
            // top horizontal line
            outlineIndices.put(top1);
            outlineIndices.put(top2);
            // vertical line
            outlineIndices.put(top1);
            outlineIndices.put(bot1);
        }

        record.interiorIndices = (IntBuffer) interiorIndices.rewind();
        record.outlineIndices = (IntBuffer) outlineIndices.rewind();
    }

    protected boolean mustAssembleTileAttributeGroups(Tile tile)
    {
        return tile.attributeGroups.isEmpty();
    }

    protected void invalidateTileAttributeGroups(Tile tile)
    {
        tile.attributeGroups.clear();
    }

    protected void assembleTileAttributeGroups(Tile tile)
    {
        tile.attributeGroups.clear();

        // Assemble the tile's records into groups with common attributes. Attributes are grouped by reference using an
        // InstanceHashMap, so that subsequent changes to an Attribute instance will be reflected in the record group
        // automatically. We take care to avoid assembling groups based on any Attribute property, as those properties
        // may change without re-assembling these groups. However, changes to a record's visibility state, highlight
        // state, normal attributes reference and highlight attributes reference invalidate this grouping.
        Map<ShapeAttributes, RecordGroup> attrMap = new IdentityHashMap<ShapeAttributes, RecordGroup>();
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
            group.interiorIndexRange.length += record.interiorIndices.remaining();
            group.outlineIndexRange.length += record.outlineIndices.remaining();
        }

        // Make the indices for each record group. We take care to make indices for both the interior and the outline,
        // regardless of the current state of Attributes.isDrawInterior and Attributes.isDrawOutline. This enable these
        // properties change state without needing to re-assemble these groups.
        for (RecordGroup group : tile.attributeGroups)
        {
            int indexCount = group.interiorIndexRange.length + group.outlineIndexRange.length;
            IntBuffer indices = Buffers.newDirectIntBuffer(indexCount);

            group.interiorIndexRange.location = indices.position();
            for (Record record : group.records) // assemble the group's triangle indices in a single contiguous range
            {
                indices.put(record.interiorIndices);
                record.interiorIndices.rewind();
            }

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

    protected void pickOrderedSurfaceRenderable(DrawContext dc, Point pickPoint)
    {
        try
        {
            this.pickSupport.clearPickList();
            this.pickSupport.beginPicking(dc);
            this.beginDrawing(dc);

            for (Tile tile : this.currentTiles)
            {
                Color color = dc.getUniquePickColor();
                dc.getGL().getGL2().glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                this.pickSupport.addPickableObject(color.getRGB(), tile);
                this.drawTile(dc, tile);
            }

            // TODO: Pick rectangle support
            PickedObject po = this.pickSupport.getTopObject(dc, pickPoint); // resolve the picked tile, if any
            if (po != null)
            {
                this.pickSupport.clearPickList();
                this.drawTileInUniqueColors(dc, (Tile) po.getObject());
                this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer); // resolve the picked records, if any
            }
        }
        finally
        {
            this.endDrawing(dc);
            this.pickSupport.endPicking(dc);
            this.pickSupport.clearPickList();
        }
    }

    protected void drawOrderedSurfaceRenderable(DrawContext dc)
    {
        try
        {
            this.beginDrawing(dc);

            for (Tile tile : this.currentTiles)
            {
                if (dc.isPickingMode())
                {
                    Color color = dc.getUniquePickColor();
                    dc.getGL().getGL2().glColor3ub((byte) color.getRed(), (byte) color.getGreen(),
                        (byte) color.getBlue());
                    this.pickSupport.addPickableObject(color.getRGB(), tile);
                }

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
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY); // all drawing uses vertex arrays
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL.GL_BLEND);
            gl.glEnable(GL.GL_LINE_SMOOTH);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_FASTEST);
        }
    }

    protected void endDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glColor4f(1, 1, 1, 1);
        gl.glDepthFunc(GL.GL_LESS);
        gl.glLineWidth(1);
        gl.glPopMatrix();

        if (!dc.isPickingMode())
        {
            gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_LINE_SMOOTH);
            gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
            gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
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
        ShapeData shapeData = tile.currentData;

        int[] vboId = null;
        boolean useVbo = dc.getGLRuntimeCapabilities().isUseVertexBufferObject();
        if (useVbo && (vboId = (int[]) dc.getGpuResourceCache().get(shapeData.vboKey)) == null)
        {
            long vboSize = 4 * shapeData.vertices.remaining(); // 4 bytes for each float vertex component
            vboId = new int[1];
            gl.glGenBuffers(1, vboId, 0);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, vboSize, shapeData.vertices, GL.GL_STATIC_DRAW);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
            dc.getGpuResourceCache().put(shapeData.vboKey, vboId, GpuResourceCache.VBO_BUFFERS, vboSize);
        }
        else if (useVbo)
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId[0]);
            if (shapeData.vboExpired)
            {
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, 4 * shapeData.vertices.remaining(), shapeData.vertices);
                shapeData.vboExpired = false;
            }
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
        }
        else
        {
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, shapeData.vertices);
        }

        Matrix modelview = dc.getView().getModelviewMatrix().multiply(shapeData.transformMatrix);
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

        if (attributeGroup.attributes.isDrawInterior())
        {
            if (!dc.isPickingMode())
            {
                float[] color = this.colorFloatArray;
                attributeGroup.attributes.getInteriorMaterial().getDiffuse().getRGBColorComponents(color);
                gl.glColor3f(color[0], color[1], color[2]);
            }

            if (useVbo)
            {
                gl.glDrawElements(GL.GL_TRIANGLES, attributeGroup.interiorIndexRange.length, GL.GL_UNSIGNED_INT,
                    4 * attributeGroup.interiorIndexRange.location);
            }
            else
            {
                gl.glDrawElements(GL.GL_TRIANGLES, attributeGroup.interiorIndexRange.length, GL.GL_UNSIGNED_INT,
                    attributeGroup.indices.position(attributeGroup.interiorIndexRange.location));
                attributeGroup.indices.rewind();
            }
        }

        if (attributeGroup.attributes.isDrawOutline())
        {
            gl.glLineWidth((float) attributeGroup.attributes.getOutlineWidth());

            if (!dc.isPickingMode())
            {
                float[] color = this.colorFloatArray;
                attributeGroup.attributes.getOutlineMaterial().getDiffuse().getRGBColorComponents(color);
                gl.glColor3f(color[0], color[1], color[2]);
            }

            if (useVbo)
            {
                gl.glDrawElements(GL.GL_LINES, attributeGroup.outlineIndexRange.length, GL.GL_UNSIGNED_INT,
                    4 * attributeGroup.outlineIndexRange.location);
            }
            else
            {
                gl.glDrawElements(GL.GL_LINES, attributeGroup.outlineIndexRange.length, GL.GL_UNSIGNED_INT,
                    attributeGroup.indices.position(attributeGroup.outlineIndexRange.location));
                attributeGroup.indices.rewind();
            }
        }
    }

    protected void drawTileInUniqueColors(DrawContext dc, Tile tile)
    {
        GL2 gl = dc.getGL().getGL2();
        ShapeData shapeData = tile.currentData;

        int pickColorsSize = shapeData.vertices.remaining(); // 1 RGB color for each XYZ vertex
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
            // Get a unique pick color for the record, and add it to the list of pickable objects. We must generate a
            // color for every record, regardless of its visibility, since the tile's color array must match the
            // tile's vertex array, which includes invisible records.
            Color color = dc.getUniquePickColor();
            this.pickSupport.addPickableObject(color.getRGB(), record);

            // top vertex
            vertexColors[0] = (byte) color.getRed();
            vertexColors[1] = (byte) color.getGreen();
            vertexColors[2] = (byte) color.getBlue();
            // bottom vertex
            vertexColors[3] = vertexColors[0];
            vertexColors[4] = vertexColors[1];
            vertexColors[5] = vertexColors[2];

            // Add the unique color for the top and bottom vertices of the record.
            for (int i = 0; i < record.numberOfPoints; i++)
            {
                colors.put(vertexColors);
            }
        }

        colors.flip();

        try
        {
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
        }
    }

    /**
     * Compute the intersections of a specified line with the geometry corresponding to this shapefile's records. Each
     * record's geometry is created relative to the specified terrain rather than the terrain used during rendering,
     * which may be at lower level of detail than required for accurate intersection determination.
     *
     * @param line    the line to intersect.
     * @param terrain the {@link Terrain} to use when computing each record's geometry.
     *
     * @return a list of intersections identifying where the line intersects this shapefile's records, or null if the
     *         line does not intersect any record.
     *
     * @throws IllegalArgumentException if any argument is null.
     * @throws InterruptedException     if the operation is interrupted.
     * @see Terrain
     */
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (terrain == null)
        {
            String msg = Logging.getMessage("nullValue.TerrainIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        ArrayList<Intersection> intersections = new ArrayList<Intersection>();
        this.intersectTileOrDescendants(line, terrain, this.rootTile, intersections);

        return intersections.size() > 0 ? intersections : null;
    }

    protected void intersectTileOrDescendants(Line line, Terrain terrain, Tile tile, List<Intersection> results)
    {
        // Regenerate the tile's intersection geometry as necessary. Synchronized simultaneous read/write access to the
        // tile's intersection data between calls to intersect or Record.intersect on separate threads.
        ShapeData shapeData;
        synchronized (tile)
        {
            shapeData = this.prepareTileIntersectionData(line, terrain, tile);
        }

        if (shapeData == null) // The line does not intersect the tile.
        {
            return;
        }

        // Intersect the line with the tile's records. Translate the line from model coordinates to tile local
        // coordinates in order to perform this operation once on the line, rather than many times for each tile vertex.
        // Intersection points are translated back into model coordinates.
        if (tile.records.size() > 0)
        {
            Line localLine = new Line(line.getOrigin().subtract3(shapeData.referencePoint), line.getDirection());
            for (Record record : tile.records)
            {
                if (record.isVisible()) // records marked as not visible don't intersect anything
                {
                    this.intersectRecordInterior(localLine, terrain, record, shapeData, results);
                }
            }
        }

        // Intersect the line with the tile's children, if any.
        if (tile.children != null)
        {
            for (Tile childTile : tile.children)
            {
                this.intersectTileOrDescendants(line, terrain, childTile, results);
            }
        }
    }

    protected void intersectTileRecord(Line line, Terrain terrain, Record record, List<Intersection> results)
    {
        // Regenerate the tile's intersection geometry as necessary. Synchronized simultaneous read/write access to the
        // tile's intersection data between calls to intersect or Record.intersect on separate threads.
        ShapeData shapeData;
        synchronized (record.tile)
        {
            shapeData = this.prepareTileIntersectionData(line, terrain, record.tile);
        }

        if (shapeData == null) // The line does not intersect the tile.
        {
            return;
        }

        // Intersect the line with the record. Translate the line from model coordinates to tile local coordinates,
        // then translate intersection points back into model coordinates.
        Line localLine = new Line(line.getOrigin().subtract3(shapeData.referencePoint), line.getDirection());
        this.intersectRecordInterior(localLine, terrain, record, shapeData, results);
    }

    protected ShapeData prepareTileIntersectionData(Line line, Terrain terrain, Tile tile)
    {
        // Force regeneration of the tile's intersection extent and intersection geometry when the specified terrain
        // changes. We regenerate the extent now and flag the geometry as invalid in order to force its regeneration
        // later. This is necessary since we want to avoid regenerating the geometry when the line does not intersect
        // the tile's extent.
        IntersectionData shapeData = tile.intersectionData;
        if (!shapeData.isValid(terrain))
        {
            shapeData.setExtent(this.makeTileExtent(terrain, tile)); // regenerate the intersection extent
            shapeData.setTessellationValid(false); // force regeneration of the intersection geometry
            shapeData.setTerrain(terrain);
            shapeData.setGlobeStateKey(terrain.getGlobe().getGlobeStateKey());
            shapeData.setVerticalExaggeration(terrain.getVerticalExaggeration());
        }

        // Determine whether or not the tile's extent intersects the line. If the line does not intersect the tile's
        // extent, then it cannot intersect the tile's records or the tile's children. Note that a tile with no records
        // may have children, so we can't use the tile's record count as a determination of whether or not to test its
        // children.
        if (!shapeData.getExtent().intersects(line))
        {
            return null;
        }

        // Regenerate the tile's intersection geometry as necessary. Suppress tessellation of tiles with no records.
        // Synchronize simultaneous tile updates between rendering, intersect and Record.intersect. Access to this
        // instance's coordinate buffer must be synchronized.
        if (tile.records.size() > 0 && !shapeData.isTessellationValid())
        {
            synchronized (this)
            {
                this.tessellateTile(terrain, tile, shapeData);
            }

            shapeData.setTessellationValid(true);
        }

        return shapeData;
    }

    protected void intersectRecordInterior(Line localLine, Terrain terrain, Record record, ShapeData shapeData,
        List<Intersection> results)
    {
        FloatBuffer vertices = shapeData.vertices;
        IntBuffer indices = record.interiorIndices;

        List<Intersection> recordIntersections = Triangle.intersectTriangles(localLine, vertices, indices);
        if (recordIntersections != null)
        {
            for (Intersection intersection : recordIntersections)
            {
                // Translate the intersection point from tile local coordinates to model coordinates.
                Vec4 pt = intersection.getIntersectionPoint().add3(shapeData.referencePoint);
                intersection.setIntersectionPoint(pt);

                // Compute intersection position relative to ground.
                Position pos = terrain.getGlobe().computePositionFromPoint(pt);
                Vec4 gp = terrain.getSurfacePoint(pos.getLatitude(), pos.getLongitude(), 0);
                double dist = Math.sqrt(pt.dotSelf3()) - Math.sqrt(gp.dotSelf3());
                intersection.setIntersectionPosition(new Position(pos, dist));

                // Associate the record with the intersection and add it to the list of intersection results.
                intersection.setObject(record);
                results.add(intersection);
            }
        }
    }
}
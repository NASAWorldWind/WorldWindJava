/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import javax.xml.stream.*;
import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * /** A 3D polygon. The polygon may be complex with multiple internal but not intersecting contours.
 * <p>
 * Polygons are safe to share among WorldWindows. They should not be shared among layers in the same WorldWindow.
 * <p>
 * In order to support simultaneous use of this shape with multiple globes (windows), this shape maintains a cache of
 * data computed relative to each globe. During rendering, the data for the currently active globe, as indicated in the
 * draw context, is made current. Subsequently called methods rely on the existence of this current data cache entry.
 * <p>
 * When drawn on a 2D globe, this shape uses a {@link SurfacePolygon} to represent itself. The following features are
 * not provided in this case: rotation and texture.
 *
 * @author tag
 * @version $Id: Polygon.java 3431 2015-10-01 04:29:15Z dcollins $
 */
public class Polygon extends AbstractShape
{
    // TODO: Merge texture coordinates into the vertex+normal buffer rather than specifying them in a separate buffer.
    // TODO: Tessellate polygon's interior to follow globe curvature when in ABSOLUTE altitude mode.

    /**
     * This class holds globe-specific data for this shape. It's managed via the shape-data cache in {@link
     * gov.nasa.worldwind.render.AbstractShape.AbstractShapeData}.
     */
    protected static class ShapeData extends AbstractShapeData implements Iterable<BoundaryInfo>
    {
        /** This class holds the per-globe data for this shape. */
        protected List<BoundaryInfo> boundaries = new ArrayList<BoundaryInfo>();
        /** The rotation matrix for this shape data. */
        protected Matrix rotationMatrix; // will vary among globes
        /**
         * The vertex data buffer for this shape data. The first half contains vertex coordinates, the second half
         * contains normals.
         */
        protected FloatBuffer coordBuffer; // contains boundary vertices in first half and normals in second half
        /** The slice of the <code>coordBuffer</code> that contains normals. */
        protected FloatBuffer normalBuffer;
        /** The index of the first normal in the <code>coordBuffer</code>. */
        protected int normalBufferPosition;
        /** This shape's tessellation indices. */
        protected GLUTessellatorSupport.CollectIndexListsCallback cb; // the tessellated polygon indices
        /**
         * The indices identifying the cap vertices in a shape data's vertex buffer. Determined when this shape is
         * tessellated, which occurs only once unless the shape's boundaries are re-specified.
         */
        protected IntBuffer interiorIndicesBuffer;
        /** Indicates whether a tessellation error occurred. No more attempts to tessellate will be made if set to true. */
        protected boolean tessellationError = false; // set to true if the tessellator fails
        /** Indicates whether the index buffer needs to be filled because a new buffer is used or some other reason. */
        protected boolean refillIndexBuffer = true; // set to true if the index buffer needs to be refilled
        /**
         * Indicates whether the index buffer's VBO needs to be filled because a new buffer is used or some other
         * reason.
         */
        protected boolean refillIndexVBO = true; // set to true if the index VBO needs to be refilled

        /**
         * Construct a cache entry using the boundaries of this shape.
         *
         * @param dc    the current draw context.
         * @param shape this shape.
         */
        public ShapeData(DrawContext dc, Polygon shape)
        {
            super(dc, shape.minExpiryTime, shape.maxExpiryTime);

            if (shape.boundaries.size() < 1)
            {
                // add a placeholder for the outer boundary
                this.boundaries.add(new BoundaryInfo(new ArrayList<Position>()));
                return;
            }

            // Copy the shape's boundaries.
            for (List<? extends Position> boundary : shape.boundaries)
            {
                this.boundaries.add(new BoundaryInfo(boundary));
            }
        }

        /**
         * Returns the boundary information for this shape data's outer boundary.
         *
         * @return this shape data's outer boundary info.
         */
        protected BoundaryInfo getOuterBoundaryInfo()
        {
            return this.boundaries.get(0);
        }

        public Iterator<BoundaryInfo> iterator()
        {
            return this.boundaries.iterator();
        }

        /**
         * Returns this shape data's rotation matrix, if there is one.
         *
         * @return this shape data's rotation matrix, or null if there isn't one.
         */
        public Matrix getRotationMatrix()
        {
            return this.rotationMatrix;
        }

        /**
         * Specifies this shape data's rotation matrix.
         *
         * @param matrix the new rotation matrix.
         */
        public void setRotationMatrix(Matrix matrix)
        {
            this.rotationMatrix = matrix;
        }
    }

    protected AbstractShapeData createCacheEntry(DrawContext dc)
    {
        return new ShapeData(dc, this);
    }

    /**
     * Returns the current shape data cache entry.
     *
     * @return the current data cache entry.
     */
    protected ShapeData getCurrent()
    {
        return (ShapeData) this.getCurrentData();
    }

    /** Holds information for each contour of the polygon. The vertex values are updated at every geometry regeneration. */
    protected static class BoundaryInfo
    {
        /** The shape's boundary positions. */
        protected List<? extends Position> positions;
        /** The shape's computed vertices, arranged in one array. */
        protected Vec4[] vertices; // TODO: eliminate need for this; use the vertex buffer instead
        /** The shape's computed vertices, arranged in a buffer. */
        protected FloatBuffer vertexBuffer; // vertices passed to OpenGL

        /**
         * Construct an instance for a specified boundary.
         *
         * @param positions the boundary positions.
         */
        public BoundaryInfo(List<? extends Position> positions)
        {
            this.positions = positions;
        }
    }

    /**
     * This static hash map holds the vertex indices that define the shape's visual outline. The contents depend only on
     * the number of locations in the source polygon, so they can be reused by all shapes with the same location count.
     */
    protected static HashMap<Integer, IntBuffer> edgeIndexBuffers = new HashMap<Integer, IntBuffer>();

    /** Indicates the number of vertices that must be present in order for VBOs to be used to render this shape. */
    protected static final int VBO_THRESHOLD = Configuration.getIntegerValue(AVKey.VBO_THRESHOLD, 30);

    /**
     * The location of each vertex in this shape's boundaries. There is one list per boundary. There is always an entry
     * for the outer boundary, but its list is empty if an outer boundary has not been specified.
     */
    protected List<List<? extends Position>> boundaries; // the defining locations or positions of the boundary
    /** The total number of positions in the entire polygon. */
    protected int numPositions;

    /** If an image source was specified, this is the WWTexture form. */
    protected WWTexture texture; // an optional texture for the base polygon
    /** This shape's rotation, in degrees positive counterclockwise. */
    protected Double rotation; // in degrees; positive is CCW
    /** This shape's texture coordinates. */
    protected FloatBuffer textureCoordsBuffer; // texture coords if texturing

    // Fields used in intersection calculations
    /** The terrain used in the most recent intersection calculations. */
    protected Terrain previousIntersectionTerrain;
    /** The globe state key for the globe used in the most recent intersection calculation. */
    protected Object previousIntersectionGlobeStateKey;
    /** The shape data used for the previous intersection calculation. */
    protected ShapeData previousIntersectionShapeData;

    /** Construct a polygon with an empty outer boundary. */
    public Polygon()
    {
        this.boundaries = new ArrayList<List<? extends Position>>();
        this.boundaries.add(new ArrayList<Position>()); // placeholder for outer boundary
    }

    /**
     * Construct a polygon for a specified outer boundary.
     *
     * @param corners the list of locations defining the polygon.
     *
     * @throws IllegalArgumentException if the location list is null.
     */
    public Polygon(Iterable<? extends Position> corners)
    {
        this(); // to initialize the instance

        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setOuterBoundary(corners);
    }

    /**
     * Construct a polygon for a specified list of outer-boundary positions.
     *
     * @param corners the list of positions -- latitude longitude and altitude -- defining the polygon. The current
     *                altitude mode determines whether the positions are considered relative to mean sea level (they are
     *                "absolute") or the ground elevation at the associated latitude and longitude.
     *
     * @throws IllegalArgumentException if the position list is null.
     */
    public Polygon(Position.PositionList corners)
    {
        this(); // to initialize the boundaries

        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setOuterBoundary(corners.list);
    }

    @Override
    protected void initialize()
    {
        // Nothing unique to initialize in this class.
    }

    /** Void any computed data. Called when a factor affecting the computed data is changed. */
    protected void reset()
    {
        // Assumes that the boundary lists have already been established.

        for (List<? extends Position> boundary : this.boundaries)
        {
            if (boundary == null || boundary.size() < 3)
                continue;

            //noinspection StringEquality
            if (WWMath.computeWindingOrderOfLocations(boundary) != AVKey.COUNTER_CLOCKWISE)
                Collections.reverse(boundary);
        }

        this.numPositions = this.countPositions();

        this.previousIntersectionShapeData = null;
        this.previousIntersectionTerrain = null;
        this.previousIntersectionGlobeStateKey = null;

        super.reset(); // removes all shape-data cache entries
    }

    /**
     * Counts the total number of positions in this shape, including all positions in all boundaries.
     *
     * @return the number of positions in this shape.
     */
    protected int countPositions()
    {
        int count = 0;

        for (List<? extends Position> boundary : this.boundaries)
        {
            count += boundary.size();
        }

        return count;
    }

    /**
     * Returns the list of positions defining this polygon's outer boundary.
     *
     * @return this polygon's outer boundary positions. The list may be empty but will not be null.
     */
    public Iterable<? extends LatLon> getOuterBoundary()
    {
        return this.outerBoundary();
    }

    /**
     * Returns a reference to the outer boundary of this polygon.
     *
     * @return this polygon's outer boundary. The list may be empty but will not be null.
     */
    public List<? extends Position> outerBoundary()
    {
        return this.boundaries.get(0);
    }

    protected boolean isOuterBoundaryValid()
    {
        return this.boundaries.size() > 0 && this.boundaries.get(0).size() > 2;
    }

    /**
     * Specifies the latitude, longitude and altitude of the outer boundary positions defining this polygon.
     *
     * @param corners this polygon's positions. A copy of the list is made and retained, and a duplicate of the first
     *                position is appended to the copy if the first and last positions are not identical.
     *
     * @throws IllegalArgumentException if the location list is null or contains fewer than three locations.
     */
    public void setOuterBoundary(Iterable<? extends Position> corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.boundaries.set(0, this.fillBoundary(corners));
        if (this.surfaceShape != null)
            this.setSurfacePolygonBoundaries(this.surfaceShape);

        this.reset();
    }

    /**
     * Copies a boundary's positions to this shape's internal boundary list. Closes the boundary if it's not already
     * closed.
     *
     * @param corners the boundary's positions.
     *
     * @return a list of the boundary positions.
     */
    protected List<? extends Position> fillBoundary(Iterable<? extends Position> corners)
    {
        ArrayList<Position> list = new ArrayList<Position>();

        for (Position corner : corners)
        {
            if (corner != null)
                list.add(corner);
        }

        if (list.size() < 3)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Close the list if not already closed.
        if (list.size() > 0 && !list.get(0).equals(list.get(list.size() - 1)))
            list.add(list.get(0));

        list.trimToSize();

        return list;
    }

    /**
     * Add an inner boundary to this polygon. A duplicate of the first position is appended to the list if the list's
     * last position is not identical to the first.
     *
     * @param corners the new boundary positions. A copy of the list is created and retained, and a duplicate of the
     *                first position is added to the list if the first and last positions are not identical.
     *
     * @throws IllegalArgumentException if the location list is null or contains fewer than three locations.
     */
    public void addInnerBoundary(Iterable<? extends Position> corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.boundaries.add(this.fillBoundary(corners));
        if (this.surfaceShape != null)
            this.setSurfacePolygonBoundaries(this.surfaceShape);

        this.reset();
    }

    /**
     * Returns this shape's boundaries.
     *
     * @return this shape's boundaries.
     */
    public List<List<? extends Position>> getBoundaries()
    {
        return this.boundaries;
    }

    /**
     * Returns this polygon's texture image source.
     *
     * @return the texture image source, or null if no source has been specified.
     */
    public Object getTextureImageSource()
    {
        return this.getTexture() != null ? this.getTexture().getImageSource() : null;
    }

    /**
     * Get the texture applied to this polygon. The texture is loaded on a background thread. This method will return
     * null until the texture has been loaded.
     *
     * @return the texture, or null if there is no texture or the texture is not yet available.
     */
    protected WWTexture getTexture()
    {
        return this.texture;
    }

    /**
     * Returns the texture coordinates for this polygon.
     *
     * @return the texture coordinates, or null if no texture coordinates have been specified.
     */
    public float[] getTextureCoords()
    {
        if (this.textureCoordsBuffer == null)
            return null;

        float[] retCoords = new float[this.textureCoordsBuffer.limit()];
        this.textureCoordsBuffer.get(retCoords, 0, retCoords.length);
        this.textureCoordsBuffer.rewind();

        return retCoords;
    }

    /**
     * Specifies the texture to apply to this polygon.
     *
     * @param imageSource   the texture image source. May be a {@link String} identifying a file path or URL, a {@link
     *                      File}, or a {@link java.net.URL}.
     * @param texCoords     the (s, t) texture coordinates aligning the image to the polygon. There must be one texture
     *                      coordinate pair, (s, t), for each polygon location in the polygon's outer boundary.
     * @param texCoordCount the number of texture coordinates, (s, v) pairs, specified.
     *
     * @throws IllegalArgumentException if the image source is not null and either the texture coordinates are null or
     *                                  inconsistent with the specified texture-coordinate count, or there are fewer
     *                                  than three texture coordinate pairs.
     */
    public void setTextureImageSource(Object imageSource, float[] texCoords, int texCoordCount)
    {
        if (imageSource == null)
        {
            this.texture = null;
            this.textureCoordsBuffer = null;

            if (this.surfaceShape != null)
                this.setSurfacePolygonTexImageSource(this.surfaceShape);

            return;
        }

        if (texCoords == null)
        {
            String message = Logging.getMessage("generic.ListIsEmpty");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (texCoordCount < 3 || texCoords.length < 2 * texCoordCount)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.texture = this.makeTexture(imageSource);

        // Determine whether the tex-coord list needs to be closed.
        boolean closeIt = texCoords[0] != texCoords[texCoordCount - 2] || texCoords[1] != texCoords[texCoordCount - 1];

        int size = 2 * (texCoordCount + (closeIt ? 1 : 0));
        if (this.textureCoordsBuffer == null || this.textureCoordsBuffer.capacity() < size)
        {
            this.textureCoordsBuffer = Buffers.newDirectFloatBuffer(size);
        }
        else
        {
            this.textureCoordsBuffer.limit(this.textureCoordsBuffer.capacity());
            this.textureCoordsBuffer.rewind();
        }

        for (int i = 0; i < 2 * texCoordCount; i++)
        {
            this.textureCoordsBuffer.put(texCoords[i]);
        }

        if (closeIt)
        {
            this.textureCoordsBuffer.put(this.textureCoordsBuffer.get(0));
            this.textureCoordsBuffer.put(this.textureCoordsBuffer.get(1));
        }

        this.textureCoordsBuffer.rewind();

        if (this.surfaceShape != null)
            this.setSurfacePolygonTexImageSource(this.surfaceShape);
    }

    public Position getReferencePosition()
    {
        if (this.referencePosition != null)
            return this.referencePosition;

        if (this.outerBoundary().size() > 0)
            this.referencePosition = this.outerBoundary().get(0);

        return this.referencePosition;
    }

    /**
     * Indicates the amount of rotation applied to this polygon.
     *
     * @return the rotation in degrees, or null if no rotation is specified.
     */
    public Double getRotation()
    {
        return this.rotation;
    }

    /**
     * Specifies the amount of rotation applied to this polygon. Positive rotation is counter-clockwise.
     *
     * @param rotation the amount of rotation to apply, in degrees, or null to apply no rotation.
     */
    public void setRotation(Double rotation)
    {
        this.rotation = rotation;
        this.reset();
    }

    @Override
    protected SurfaceShape createSurfaceShape()
    {
        SurfacePolygon polygon = new SurfacePolygon();
        this.setSurfacePolygonBoundaries(polygon);
        this.setSurfacePolygonTexImageSource(polygon);

        return polygon;
    }

    protected void setSurfacePolygonBoundaries(SurfaceShape shape)
    {
        SurfacePolygon polygon = (SurfacePolygon) shape;

        polygon.setLocations(this.getOuterBoundary());

        List<List<? extends Position>> bounds = this.getBoundaries();
        for (int i = 1; i < bounds.size(); i++)
        {
            polygon.addInnerBoundary(bounds.get(i));
        }
    }

    protected void setSurfacePolygonTexImageSource(SurfaceShape shape)
    {
        SurfacePolygon polygon = (SurfacePolygon) shape;

        float[] texCoords = this.getTextureCoords();
        int texCoordCount = texCoords != null ? texCoords.length / 2 : 0;
        polygon.setTextureImageSource(this.getTextureImageSource(), texCoords, texCoordCount);
    }

    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        // See if we've cached an extent associated with the globe.
        Extent extent = super.getExtent(globe, verticalExaggeration);
        if (extent != null)
            return extent;

        return super.computeExtentFromPositions(globe, verticalExaggeration, this.getOuterBoundary());
    }

    /**
     * Computes the Cartesian extent of a polygon boundary.
     *
     * @param boundary The boundary to compute the extent for.
     * @param refPoint the shape's reference point.
     *
     * @return the boundary's extent. Returns null if the boundary's vertices have not been computed.
     */
    protected Extent computeExtent(BoundaryInfo boundary, Vec4 refPoint)
    {
        if (boundary == null || boundary.vertices == null)
            return null;

        // The bounding box is computed relative to the polygon's reference point, so it needs to be translated to
        // model coordinates in order to indicate its model-coordinate extent.
        Box boundingBox = Box.computeBoundingBox(Arrays.asList(boundary.vertices));

        return boundingBox != null ? boundingBox.translate(refPoint) : null;
    }

    public Sector getSector()
    {
        if (this.sector == null && this.isOuterBoundaryValid())
            this.sector = Sector.boundingSector(this.getOuterBoundary());

        return this.sector;
    }

    protected boolean mustApplyTexture(DrawContext dc)
    {
        return this.getTexture() != null && this.textureCoordsBuffer != null;
    }

    protected boolean shouldUseVBOs(DrawContext dc)
    {
        return this.numPositions > VBO_THRESHOLD && super.shouldUseVBOs(dc);
    }

    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        if (this.getCurrent().coordBuffer == null)
            return true;

        if (dc.getVerticalExaggeration() != this.getCurrent().getVerticalExaggeration())
            return true;

        if (this.mustApplyLighting(dc, null) && this.getCurrent().normalBuffer == null)
            return true;

        if (this.getAltitudeMode() == WorldWind.ABSOLUTE
            && this.getCurrent().getGlobeStateKey() != null
            && this.getCurrent().getGlobeStateKey().equals(dc.getGlobe().getGlobeStateKey(dc)))
            return false;

        return super.mustRegenerateGeometry(dc);
    }

    public void render(DrawContext dc)
    {
        if (!this.isOuterBoundaryValid())
            return;

        super.render(dc);
    }

    protected boolean doMakeOrderedRenderable(DrawContext dc)
    {
        if (dc.getSurfaceGeometry() == null || !this.isOuterBoundaryValid())
            return false;

        this.getCurrent().setRotationMatrix(this.getRotation() != null ?
            this.computeRotationMatrix(dc.getGlobe()) : null);

        this.createMinimalGeometry(dc, this.getCurrent());

        // If the shape is less that a pixel in size, don't render it.
        if (this.getCurrent().getExtent() == null || dc.isSmall(this.getExtent(), 1))
            return false;

        if (!this.intersectsFrustum(dc))
            return false;

        this.createFullGeometry(dc, dc.getTerrain(), this.getCurrent(), true);

        return true;
    }

    protected boolean isOrderedRenderableValid(DrawContext dc)
    {
        return this.getCurrent().coordBuffer != null && this.isOuterBoundaryValid();
    }

    protected OGLStackHandler beginDrawing(DrawContext dc, int attrMask)
    {
        OGLStackHandler ogsh = super.beginDrawing(dc, attrMask);

        if (!dc.isPickingMode())
        {
            // Push an identity texture matrix. This prevents drawSides() from leaking GL texture matrix state. The
            // texture matrix stack is popped from OGLStackHandler.pop(), in the finally block below.
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            ogsh.pushTextureIdentity(gl);
        }

        return ogsh;
    }

    protected void doDrawOutline(DrawContext dc)
    {
        if (this.shouldUseVBOs(dc))
        {
            int[] vboIds = this.getVboIds(dc);
            if (vboIds != null)
                this.doDrawOutlineVBO(dc, vboIds, this.getCurrent());
            else
                this.doDrawOutlineVA(dc, this.getCurrent());
        }
        else
        {
            this.doDrawOutlineVA(dc, this.getCurrent());
        }
    }

    protected void doDrawOutlineVA(DrawContext dc, ShapeData shapeData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glVertexPointer(3, GL.GL_FLOAT, 0, shapeData.coordBuffer.rewind());

        if (!dc.isPickingMode() && this.mustApplyLighting(dc, null))
            gl.glNormalPointer(GL.GL_FLOAT, 0, shapeData.normalBuffer.rewind());

        int k = 0;
        for (BoundaryInfo boundary : shapeData)
        {
            gl.glDrawArrays(GL.GL_LINE_STRIP, k, boundary.vertices.length);
            k += boundary.vertices.length;
        }

//        // Diagnostic to show the normal vectors.
//        if (this.mustApplyLighting(dc))
//            dc.drawNormals(1000, this.boundarySet.coordBuffer, this.boundarySet.normalBuffer);
    }

    protected void doDrawOutlineVBO(DrawContext dc, int[] vboIds, ShapeData shapeData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);

        if (!dc.isPickingMode() && this.mustApplyLighting(dc, null))
            gl.glNormalPointer(GL.GL_FLOAT, 0, 4 * shapeData.normalBufferPosition);

        int k = 0;
        for (BoundaryInfo boundary : shapeData)
        {
            // TODO: check use glMultiDrawArrays
            gl.glDrawArrays(GL.GL_LINE_STRIP, k, boundary.vertices.length);
            k += boundary.vertices.length;
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }

    protected void doDrawInterior(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!dc.isPickingMode() && mustApplyTexture(dc) && this.getTexture().bind(dc)) // bind initiates retrieval
        {
            this.getTexture().applyInternalTransform(dc);

            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, this.textureCoordsBuffer.rewind());
            dc.getGL().glEnable(GL.GL_TEXTURE_2D);
            gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        }
        else
        {
            dc.getGL().glDisable(GL.GL_TEXTURE_2D);
            gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        }

        if (this.shouldUseVBOs(dc))
        {
            int[] vboIds = this.getVboIds(dc);
            if (vboIds != null)
                this.doDrawInteriorVBO(dc, vboIds, this.getCurrent());
            else
                this.doDrawInteriorVA(dc, this.getCurrent());
        }
        else
        {
            this.doDrawInteriorVA(dc, this.getCurrent());
        }
    }

    protected void doDrawInteriorVA(DrawContext dc, ShapeData shapeData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!dc.isPickingMode() && this.mustApplyLighting(dc, null))
            gl.glNormalPointer(GL.GL_FLOAT, 0, shapeData.normalBuffer.rewind());

        FloatBuffer vb = shapeData.coordBuffer;
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, vb.rewind());

        IntBuffer ib = shapeData.interiorIndicesBuffer;
        gl.glDrawElements(GL.GL_TRIANGLES, ib.limit(), GL.GL_UNSIGNED_INT, ib);
    }

    protected void doDrawInteriorVBO(DrawContext dc, int[] vboIds, ShapeData shapeData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboIds[1]);

        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);

        if (!dc.isPickingMode() && this.mustApplyLighting(dc, null))
            gl.glNormalPointer(GL.GL_FLOAT, 0, 4 * shapeData.normalBufferPosition);

        gl.glDrawElements(GL.GL_TRIANGLES, shapeData.interiorIndicesBuffer.limit(), GL.GL_UNSIGNED_INT, 0);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    protected Matrix computeRotationMatrix(Globe globe)
    {
        if (this.getRotation() == null)
            return null;

        // Find the centroid of the polygon with all altitudes 0 and rotate around that using the surface normal at
        // that point as the rotation axis.

        double cx = 0;
        double cy = 0;
        double cz = 0;
        double outerBoundarySize = outerBoundary().size();
        for (int i = 0; i < this.outerBoundary().size(); i++)
        {
            Vec4 vert = globe.computePointFromPosition(this.outerBoundary().get(i), 0);

            cx += vert.x / outerBoundarySize;
            cy += vert.y / outerBoundarySize;
            cz += vert.z / outerBoundarySize;
        }

        Vec4 center = new Vec4(cx, cy, cz);
        Vec4 normalVec = globe.computeSurfaceNormalAtPoint(center);

        Matrix m1 = Matrix.fromTranslation(center.multiply3(-1));
        Matrix m3 = Matrix.fromTranslation(center);
        Matrix m2 = Matrix.fromAxisAngle(Angle.fromDegrees(this.getRotation()), normalVec);
        return m3.multiply(m2).multiply(m1);
    }

    /**
     * Compute enough geometry to determine this polygon's extent, reference point and eye distance.
     * <p>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc        the current draw context.
     * @param shapeData the current shape data for this shape.
     */
    protected void createMinimalGeometry(DrawContext dc, ShapeData shapeData)
    {
        Matrix rotationMatrix = shapeData.getRotationMatrix();

        Vec4 refPt = this.computeReferencePoint(dc.getTerrain(), rotationMatrix);
        if (refPt == null)
            return;
        shapeData.setReferencePoint(refPt);

        // Need only the outer-boundary vertices.
        this.computeBoundaryVertices(dc.getTerrain(), shapeData.getOuterBoundaryInfo(),
            shapeData.getReferencePoint(), rotationMatrix);

        if (shapeData.getExtent() == null || this.getAltitudeMode() != WorldWind.ABSOLUTE)
            shapeData.setExtent(this.computeExtent(shapeData.getOuterBoundaryInfo(),
                shapeData.getReferencePoint()));

        shapeData.setEyeDistance(this.computeEyeDistance(dc, shapeData));
        shapeData.setGlobeStateKey(dc.getGlobe().getGlobeStateKey(dc));
        shapeData.setVerticalExaggeration(dc.getVerticalExaggeration());
    }

    /**
     * Computes the minimum distance between this polygon and the eye point.
     * <p>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc        the draw context.
     * @param shapeData the current shape data for this shape.
     *
     * @return the minimum distance from the shape to the eye point.
     */
    protected double computeEyeDistance(DrawContext dc, ShapeData shapeData)
    {
        double minDistance = Double.MAX_VALUE;
        Vec4 eyePoint = dc.getView().getEyePoint();

        for (Vec4 point : shapeData.getOuterBoundaryInfo().vertices)
        {
            double d = point.add3(shapeData.getReferencePoint()).distanceTo3(eyePoint);
            if (d < minDistance)
                minDistance = d;
        }

        return minDistance;
    }

    protected Vec4 computeReferencePoint(Terrain terrain, Matrix rotationMatrix)
    {
        Position refPos = this.getReferencePosition();
        if (refPos == null)
            return null;

        Vec4 refPt = terrain.getSurfacePoint(refPos.getLatitude(), refPos.getLongitude(), 0);
        if (refPt == null)
            return null;

        return rotationMatrix != null ? refPt.transformBy4(rotationMatrix) : refPt;
    }

    /**
     * Computes a shape's full geometry.
     *
     * @param dc                the current draw context.
     * @param terrain           the terrain to use when computing the geometry.
     * @param shapeData         the current shape data for this shape.
     * @param skipOuterBoundary true if outer boundaries vertices do not need to be calculated, otherwise false.
     */
    protected void createFullGeometry(DrawContext dc, Terrain terrain, ShapeData shapeData,
        boolean skipOuterBoundary)
    {
        this.createVertices(terrain, shapeData, skipOuterBoundary);
        this.createGeometry(dc, shapeData);

        if (this.mustApplyLighting(dc, null))
            this.createNormals(shapeData);
        else
            shapeData.normalBuffer = null;
    }

    /**
     * Computes the Cartesian vertices for this shape.
     *
     * @param terrain           the terrain to use if the altitude mode is relative to the terrain.
     * @param shapeData         the current shape data for this shape.
     * @param skipOuterBoundary if true, don't calculate the vertices for the outer boundary. This is used when the
     *                          outer boundary vertices were computed as minimal geometry.
     */
    protected void createVertices(Terrain terrain, ShapeData shapeData, boolean skipOuterBoundary)
    {
        for (BoundaryInfo boundary : shapeData)
        {
            if (boundary != shapeData.getOuterBoundaryInfo() || !skipOuterBoundary)
                this.computeBoundaryVertices(terrain, boundary, shapeData.getReferencePoint(),
                    shapeData.getRotationMatrix());
        }
    }

    /**
     * Compute the vertices associated with a specified boundary.
     *
     * @param terrain        the terrain to use when calculating vertices relative to the ground.
     * @param boundary       the boundary to compute vertices for.
     * @param refPoint       the reference point. Vertices are computed relative to this point, which is usually the
     *                       shape's reference point.
     * @param rotationMatrix the rotation matrix to apply to the vertices.
     */
    protected void computeBoundaryVertices(Terrain terrain, BoundaryInfo boundary, Vec4 refPoint, Matrix rotationMatrix)
    {
        int n = boundary.positions.size();
        Vec4[] boundaryVertices = new Vec4[n];

        for (int i = 0; i < n; i++)
        {
            if (rotationMatrix == null)
                boundaryVertices[i] = this.computePoint(terrain, boundary.positions.get(i)).subtract3(refPoint);
            else
                boundaryVertices[i] = this.computePoint(terrain, boundary.positions.get(i)).transformBy4(
                    rotationMatrix).subtract3(refPoint);
        }

        boundary.vertices = boundaryVertices;
    }

    /**
     * Compute the cap geometry.
     * <p>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc        the current draw context.
     * @param shapeData boundary vertices are calculated during {@link #createMinimalGeometry(DrawContext,
     *                  gov.nasa.worldwind.render.Polygon.ShapeData)}).
     */
    protected void createGeometry(DrawContext dc, ShapeData shapeData)
    {
        int size = this.numPositions * (this.mustApplyLighting(dc, null) ? 6 : 3);

        if (shapeData.coordBuffer != null && shapeData.coordBuffer.capacity() >= size)
            shapeData.coordBuffer.clear();
        else
            shapeData.coordBuffer = Buffers.newDirectFloatBuffer(size);

        // Capture the position position at which normals buffer starts (in case there are normals)
        shapeData.normalBufferPosition = this.numPositions * 3;

        // Fill the vertex buffer. Simultaneously create individual buffer slices for each boundary.
        for (BoundaryInfo boundary : shapeData)
        {
            boundary.vertexBuffer = WWBufferUtil.copyArrayToBuffer(boundary.vertices, shapeData.coordBuffer.slice());
            shapeData.coordBuffer.position(shapeData.coordBuffer.position() + boundary.vertexBuffer.limit());
        }

        if (shapeData.cb == null && !shapeData.tessellationError)
            this.createTessllationGeometry(dc, shapeData);

        if (shapeData.refillIndexBuffer)
            this.generateInteriorIndices(shapeData);
    }

    /**
     * Create this shape's vertex normals.
     *
     * @param shapeData the current shape data holding the vertex coordinates and in which the normal vectors are added.
     *                  The normal vectors are appended to the vertex coordinates in the same buffer. The shape data's
     *                  coordinate buffer must have sufficient capacity to hold the vertex normals.
     */
    protected void createNormals(ShapeData shapeData)
    {
        shapeData.coordBuffer.position(shapeData.normalBufferPosition);
        shapeData.normalBuffer = shapeData.coordBuffer.slice();

        for (BoundaryInfo boundary : shapeData)
        {
            this.computeBoundaryNormals(boundary, shapeData.normalBuffer);
        }
    }

    /**
     * Fill this shape's vertex buffer objects. If the vertex buffer object resource IDs don't yet exist, create them.
     *
     * @param dc the current draw context.
     */
    protected void fillVBO(DrawContext dc)
    {
        GL gl = dc.getGL();
        ShapeData shapeData = this.getCurrent();

        int[] vboIds = (int[]) dc.getGpuResourceCache().get(shapeData.getVboCacheKey());
        if (vboIds == null)
        {
            int size = shapeData.coordBuffer.limit() * 4;
            size += shapeData.interiorIndicesBuffer.limit() * 4;

            vboIds = new int[2];
            gl.glGenBuffers(vboIds.length, vboIds, 0);
            dc.getGpuResourceCache().put(shapeData.getVboCacheKey(), vboIds, GpuResourceCache.VBO_BUFFERS, size);
            shapeData.refillIndexVBO = true;
        }

        if (shapeData.refillIndexVBO)
        {
            try
            {
                IntBuffer ib = shapeData.interiorIndicesBuffer;
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboIds[1]);
                gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, ib.limit() * 4, ib.rewind(), GL.GL_DYNAMIC_DRAW);

                shapeData.refillIndexVBO = false;
            }
            finally
            {
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
        }

        try
        {
            FloatBuffer vb = this.getCurrent().coordBuffer;
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, vb.limit() * 4, vb.rewind(), GL.GL_STATIC_DRAW);
        }
        finally
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }

    /**
     * Compute normal vectors for a boundary's vertices.
     *
     * @param boundary the boundary to compute normals for.
     * @param nBuf     the buffer in which to place the computed normals. Must have enough remaining space to hold the
     *                 normals.
     *
     * @return the buffer specified as input, with its limit incremented by the number of vertices copied, and its
     * position set to 0.
     */
    protected FloatBuffer computeBoundaryNormals(BoundaryInfo boundary, FloatBuffer nBuf)
    {
        int nVerts = boundary.positions.size();
        Vec4[] verts = boundary.vertices;
        double avgX, avgY, avgZ;

        // Compute normal for first point of boundary.
        Vec4 va = verts[1].subtract3(verts[0]);
        Vec4 vb = verts[nVerts - 2].subtract3(verts[0]); // nverts - 2 because last and first are same
        avgX = (va.y * vb.z) - (va.z * vb.y);
        avgY = (va.z * vb.x) - (va.x * vb.z);
        avgZ = (va.x * vb.y) - (va.y * vb.x);

        // Compute normals for interior boundary points.
        for (int i = 1; i < nVerts - 1; i++)
        {
            va = verts[i + 1].subtract3(verts[i]);
            vb = verts[i - 1].subtract3(verts[i]);
            avgX += (va.y * vb.z) - (va.z * vb.y);
            avgY += (va.z * vb.x) - (va.x * vb.z);
            avgZ += (va.x * vb.y) - (va.y * vb.x);
        }

        avgX /= nVerts - 1;
        avgY /= nVerts - 1;
        avgZ /= nVerts - 1;
        double length = Math.sqrt(avgX * avgX + avgY * avgY + avgZ * avgZ);

        for (int i = 0; i < nVerts; i++)
        {
            nBuf.put((float) (avgX / length)).put((float) (avgY / length)).put((float) (avgZ / length));
        }

        return nBuf;
    }

    /**
     * Tessellates the polygon.
     * <p>
     * This method catches {@link OutOfMemoryError} exceptions and if the draw context is not null passes the exception
     * to the rendering exception listener (see {@link WorldWindow#addRenderingExceptionListener(gov.nasa.worldwind.event.RenderingExceptionListener)}).
     *
     * @param dc        the draw context.
     * @param shapeData the current shape data for this shape.
     */
    protected void createTessllationGeometry(DrawContext dc, ShapeData shapeData)
    {
        // Wrap polygon tessellation in a try/catch block. We do this to catch and handle OutOfMemoryErrors caused during
        // tessellation of the polygon vertices. If the polygon cannot be tessellated, we replace the polygon's locations
        // with an empty list to prevent subsequent tessellation attempts, and to avoid rendering a misleading
        // representation by omitting the polygon.
        try
        {
            Vec4 normal = this.computePolygonNormal(dc, shapeData);

            // There's a fallback for non-computable normal in computePolygonNormal, but test here in case the fallback
            // doesn't work either.
            if (normal == null)
            {
                String message = Logging.getMessage("Geom.ShapeNormalVectorNotComputable", this);
                Logging.logger().log(java.util.logging.Level.SEVERE, message);
                shapeData.tessellationError = true;
                return;
            }

            this.tessellatePolygon(shapeData, normal.normalize3());
        }
        catch (OutOfMemoryError e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileTessellating", this);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);

            shapeData.tessellationError = true;

            if (dc != null)
            {
                //noinspection ThrowableInstanceNeverThrown
                dc.addRenderingException(new WWRuntimeException(message, e));
            }
        }
    }

    protected Vec4 computePolygonNormal(DrawContext dc, ShapeData shapeData)
    {
        // The coord buffer might contain space for normals, but use only the vertices to compute the polygon normal.
        shapeData.coordBuffer.rewind();
        FloatBuffer coordsOnly = shapeData.coordBuffer.slice();
        coordsOnly.limit(this.numPositions * 3);

        Vec4 normal = WWMath.computeBufferNormal(coordsOnly, 0);

        // The normal vector is null if this is a degenerate polygon representing a line or a single point. We fall
        // back to using the globe's surface normal at the reference point. This allows the tessellator to process
        // the degenerate polygon without generating an exception.
        if (normal == null)
            normal = dc.getGlobe().computeSurfaceNormalAtLocation(
                this.getReferencePosition().getLatitude(), this.getReferencePosition().getLongitude());

        return normal;
    }

    /**
     * Tessellates the polygon from its vertices.
     *
     * @param shapeData the polygon boundaries.
     * @param normal    a unit normal vector for the plane containing the polygon vertices. Even though the the vertices
     *                  might not be coplanar, only one representative normal is used for tessellation.
     */
    protected void tessellatePolygon(ShapeData shapeData, Vec4 normal)
    {
        GLUTessellatorSupport glts = new GLUTessellatorSupport();
        shapeData.cb = new GLUTessellatorSupport.CollectIndexListsCallback();

        glts.beginTessellation(shapeData.cb, normal);
        try
        {
            double[] coords = new double[3];

            GLU.gluTessBeginPolygon(glts.getGLUtessellator(), null);

            int k = 0;
            for (BoundaryInfo boundary : shapeData)
            {
                GLU.gluTessBeginContour(glts.getGLUtessellator());
                FloatBuffer vBuf = boundary.vertexBuffer;
                for (int i = 0; i < boundary.positions.size(); i++)
                {
                    coords[0] = vBuf.get(i * 3);
                    coords[1] = vBuf.get(i * 3 + 1);
                    coords[2] = vBuf.get(i * 3 + 2);

                    GLU.gluTessVertex(glts.getGLUtessellator(), coords, 0, k++);
                }
                GLU.gluTessEndContour(glts.getGLUtessellator());
            }

            GLU.gluTessEndPolygon(glts.getGLUtessellator());
        }
        finally
        {
            // Free any heap memory used for tessellation immediately. If tessellation has consumed all available
            // heap memory, we must free memory used by tessellation immediately or subsequent operations such as
            // message logging will fail.
            glts.endTessellation();
        }
    }

    protected void generateInteriorIndices(ShapeData shapeData)
    {
        GLUTessellatorSupport.CollectIndexListsCallback cb = shapeData.cb;
        int size = this.countTriangleVertices(cb.getPrims(), cb.getPrimTypes());

        if (shapeData.interiorIndicesBuffer == null || shapeData.interiorIndicesBuffer.capacity() < size)
            shapeData.interiorIndicesBuffer = Buffers.newDirectIntBuffer(size);
        else
            shapeData.interiorIndicesBuffer.clear();

        for (int i = 0; i < cb.getPrims().size(); i++)
        {
            switch (cb.getPrimTypes().get(i))
            {
                case GL.GL_TRIANGLES:
                    Triangle.expandTriangles(cb.getPrims().get(i), shapeData.interiorIndicesBuffer);
                    break;

                case GL.GL_TRIANGLE_FAN:
                    Triangle.expandTriangleFan(cb.getPrims().get(i), shapeData.interiorIndicesBuffer);
                    break;

                case GL.GL_TRIANGLE_STRIP:
                    Triangle.expandTriangleStrip(cb.getPrims().get(i), shapeData.interiorIndicesBuffer);
                    break;
            }
        }

        shapeData.interiorIndicesBuffer.flip();
        shapeData.refillIndexBuffer = false;
        shapeData.refillIndexVBO = true;
    }

    protected boolean isSameAsPreviousTerrain(Terrain terrain)
    {
        if (terrain == null || this.previousIntersectionTerrain == null || terrain != this.previousIntersectionTerrain)
            return false;

        if (terrain.getVerticalExaggeration() != this.previousIntersectionTerrain.getVerticalExaggeration())
            return false;

        return this.previousIntersectionGlobeStateKey != null &&
            terrain.getGlobe().getGlobeStateKey().equals(this.previousIntersectionGlobeStateKey);
    }

    public void clearIntersectionGeometry()
    {
        this.previousIntersectionGlobeStateKey = null;
        this.previousIntersectionShapeData = null;
        this.previousIntersectionTerrain = null;
    }

    /**
     * Compute the intersections of a specified line with this polygon. If the polygon's altitude mode is other than
     * {@link WorldWind#ABSOLUTE}, the polygon's geometry is created relative to the specified terrain rather than the
     * terrain used during rendering, which may be at lower level of detail than required for accurate intersection
     * determination.
     *
     * @param line    the line to intersect.
     * @param terrain the {@link Terrain} to use when computing the polygon's geometry.
     *
     * @return a list of intersections identifying where the line intersects the polygon, or null if the line does not
     * intersect the polygon.
     *
     * @throws InterruptedException if the operation is interrupted.
     * @see Terrain
     */
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException
    {
        Position refPos = this.getReferencePosition();
        if (refPos == null)
            return null;

        if (!this.isOuterBoundaryValid())
            return null;

        // Reuse the previously computed high-res shape data if the terrain is the same.
        ShapeData highResShapeData = this.isSameAsPreviousTerrain(terrain) ? this.previousIntersectionShapeData
            : null;

        if (highResShapeData == null)
        {
            highResShapeData = this.createIntersectionGeometry(terrain);
            if (highResShapeData == null)
                return null;

            this.previousIntersectionShapeData = highResShapeData;
            this.previousIntersectionTerrain = terrain;
            this.previousIntersectionGlobeStateKey = terrain.getGlobe().getGlobeStateKey();
        }

        if (highResShapeData.getExtent() != null && highResShapeData.getExtent().intersect(line) == null)
            return null;

        final Line localLine = new Line(line.getOrigin().subtract3(highResShapeData.getReferencePoint()),
            line.getDirection());
        List<Intersection> intersections = new ArrayList<Intersection>();

        this.intersect(localLine, highResShapeData, intersections);

        if (intersections.size() == 0)
            return null;

        for (Intersection intersection : intersections)
        {
            Vec4 pt = intersection.getIntersectionPoint().add3(highResShapeData.getReferencePoint());
            intersection.setIntersectionPoint(pt);

            // Compute intersection position relative to ground.
            Position pos = terrain.getGlobe().computePositionFromPoint(pt);
            Vec4 gp = terrain.getSurfacePoint(pos.getLatitude(), pos.getLongitude(), 0);
            double dist = Math.sqrt(pt.dotSelf3()) - Math.sqrt(gp.dotSelf3());
            intersection.setIntersectionPosition(new Position(pos, dist));

            intersection.setObject(this);
        }

        return intersections;
    }

    protected ShapeData createIntersectionGeometry(Terrain terrain)
    {
        ShapeData shapeData = new ShapeData(null, this);

        Matrix rotationMatrix = this.getRotation() != null ? this.computeRotationMatrix(terrain.getGlobe()) : null;

        shapeData.setReferencePoint(this.computeReferencePoint(terrain, rotationMatrix));
        if (shapeData.getReferencePoint() == null)
            return null;

        // Compute the boundary vertices first.
        this.createVertices(terrain, shapeData, false);
        this.createGeometry(null, shapeData);

        shapeData.setExtent(computeExtent(shapeData.getOuterBoundaryInfo(), shapeData.getReferencePoint()));

        return shapeData;
    }

    protected void intersect(Line line, ShapeData shapeData, List<Intersection> intersections)
        throws InterruptedException
    {
        if (shapeData.cb.getPrims() == null)
            return;

        IntBuffer ib = shapeData.interiorIndicesBuffer;
        ib.rewind();
        List<Intersection> ti = Triangle.intersectTriangleTypes(line, shapeData.coordBuffer, ib,
            GL.GL_TRIANGLES);

        if (ti != null && ti.size() > 0)
            intersections.addAll(ti);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method overwrites the boundary locations lists, and therefore no longer refer to the originally
     * specified boundary lists.
     *
     * @param position the new position of the shape's reference position.
     *
     * @throws java.lang.IllegalArgumentException if the position is null.
     */
    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.isOuterBoundaryValid())
            return;

        Position oldPosition = this.getReferencePosition();
        if (oldPosition == null)
            return;

        List<List<? extends Position>> newBoundaries = new ArrayList<List<? extends Position>>(this.boundaries.size());

        for (List<? extends Position> boundary : this.boundaries)
        {
            if (boundary == null || boundary.size() == 0)
                continue;

            List<Position> newList = Position.computeShiftedPositions(oldPosition, position, boundary);
            if (newList != null)
                newBoundaries.add(newList);
        }

        this.boundaries = newBoundaries;
        this.setReferencePosition(position);
        this.reset();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method overwrites the boundary locations lists, and therefore no longer refer to the originally
     * specified boundary lists.
     *
     * @param globe    the globe on which to move this shape.
     * @param position the new position of the shape's reference position.
     *
     * @throws java.lang.IllegalArgumentException if the globe or position is null.
     */
    public void moveTo(Globe globe, Position position)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.isOuterBoundaryValid())
            return;

        Position oldPosition = this.getReferencePosition();
        if (oldPosition == null)
            return;

        List<List<? extends Position>> newBoundaries = new ArrayList<List<? extends Position>>(this.boundaries.size());

        for (List<? extends Position> boundary : this.boundaries)
        {
            if (boundary == null || boundary.size() == 0)
                continue;

            List<Position> newList = Position.computeShiftedPositions(globe, oldPosition, position, boundary);
            if (newList != null)
                newBoundaries.add(newList);
        }

        this.boundaries = newBoundaries;
        this.setReferencePosition(position);
        this.reset();
    }

    /** {@inheritDoc} */
    protected void doExportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        // Write geometry
        xmlWriter.writeStartElement("Polygon");

        xmlWriter.writeStartElement("extrude");
        xmlWriter.writeCharacters("0");
        xmlWriter.writeEndElement();

        final String altitudeMode = KMLExportUtil.kmlAltitudeMode(getAltitudeMode());
        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters(altitudeMode);
        xmlWriter.writeEndElement();

        this.writeKMLBoundaries(xmlWriter);

        xmlWriter.writeEndElement(); // Polygon
    }

    /**
     * Write the boundary of the polygon as KML.
     *
     * @param xmlWriter XML writer to receive the output.
     *
     * @throws IOException        If an exception occurs writing the XML stream.
     * @throws XMLStreamException If an exception occurs writing the XML stream.
     */
    protected void writeKMLBoundaries(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        // Outer boundary
        Iterable<? extends LatLon> outerBoundary = this.getOuterBoundary();
        if (outerBoundary != null)
        {
            xmlWriter.writeStartElement("outerBoundaryIs");
            exportBoundaryAsLinearRing(xmlWriter, outerBoundary);
            xmlWriter.writeEndElement(); // outerBoundaryIs
        }

        // Inner boundaries. Skip outer boundary, we already dealt with it above

        for (int i = 1; i < this.boundaries.size(); i++)
        {
            xmlWriter.writeStartElement("innerBoundaryIs");
            exportBoundaryAsLinearRing(xmlWriter, this.boundaries.get(i));
            xmlWriter.writeEndElement(); // innerBoundaryIs
        }
    }

    /**
     * Writes the boundary in KML as either a list of lat, lon, altitude tuples or lat, lon tuples, depending on the
     * type originally specified.
     *
     * @param xmlWriter the XML writer.
     * @param boundary  the boundary to write.
     *
     * @throws XMLStreamException if an error occurs during writing.
     */
    protected void exportBoundaryAsLinearRing(XMLStreamWriter xmlWriter, Iterable<? extends LatLon> boundary)
        throws XMLStreamException
    {
        xmlWriter.writeStartElement("LinearRing");
        xmlWriter.writeStartElement("coordinates");
        for (LatLon location : boundary)
        {
            if (location instanceof Position)
            {
                xmlWriter.writeCharacters(String.format(Locale.US, "%f,%f,%f ",
                    location.getLongitude().getDegrees(),
                    location.getLatitude().getDegrees(),
                    ((Position) location).getAltitude()));
            }
            else
            {
                xmlWriter.writeCharacters(String.format(Locale.US, "%f,%f ",
                    location.getLongitude().getDegrees(),
                    location.getLatitude().getDegrees()));
            }
        }
        xmlWriter.writeEndElement(); // coordinates
        xmlWriter.writeEndElement(); // LinearRing
    }
}

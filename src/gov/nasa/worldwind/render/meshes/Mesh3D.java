package gov.nasa.worldwind.render.meshes;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;

import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

public class Mesh3D extends AbstractGeneralShape {

    /**
     * Geometry and attributes of a COLLADA {@code triangles} or {@code lines}
     * element.
     */
    public static class Geometry {

        /**
         * Geometry element that defines this geometry.
         */
        protected AbstractGeometry nativeGeometry;

        /**
         * Offset (in vertices) into the coord, normal, and texcoord buffers of
         * this coordinates for this geometry.
         */
        protected int offset = -1;

        /**
         * Texture applied to this geometry.
         */
        protected WWTexture texture;
        /**
         * Material applied to this geometry.
         */
        protected Material material;

        /**
         * Indicates whether or not the geometry is double sided. If double
         * sided, the geometry must be rendered with backface culling disabled.
         * This property is determined by the presence of a technique for the
         * "GOOGLEEARTH" profile that includes a <i>double_sided</i> field.
         *
         * @see
         * ColladaMeshShape#isDoubleSided(gov.nasa.worldwind.ogc.collada.ColladaAbstractGeometry)
         */
        protected boolean doubleSided;

        /**
         * Create a new geometry instance.
         *
         * @param geometry COLLADA geometry to render.
         */
        public Geometry(AbstractGeometry geometry) {
            this.nativeGeometry = geometry;
        }

        public AbstractGeometry getNativeGeometry() {
            return this.nativeGeometry;
        }

        public WWTexture getTexture() {
            return this.texture;
        }

        public void setTexture(WWTexture texture) {
            this.texture = texture;
        }
    }

    /**
     * Class to represent an instance of the mesh to be drawn as an ordered
     * renderable. We can't use the mesh itself as the ordered renderable
     * because it may be drawn multiple times with different transforms.
     */
    protected static class OrderedMeshShape implements OrderedRenderable {

        /**
         * Shape to render.
         */
        protected Mesh3D orderedMesh;
        /**
         * Distance from the eye to the shape's reference position.
         */
        protected double eyeDistance;
        /**
         * Transform applied to this instance of the mesh.
         */
        protected Matrix renderMatrix;

        /**
         * Create a new ordered renderable.
         *
         * @param mesh Mesh shape to render.
         * @param renderMatrix Transform matrix to apply when rendering the
         * shape.
         * @param eyeDistance Distance from the eye position to the shape's
         * reference position.
         */
        public OrderedMeshShape(Mesh3D mesh, Matrix renderMatrix, double eyeDistance) {
            this.orderedMesh = mesh;
            this.eyeDistance = eyeDistance;
            this.renderMatrix = renderMatrix;
        }

        public double getDistanceFromEye() {
            return this.eyeDistance;
        }

        public void pick(DrawContext dc, Point pickPoint) {
            this.orderedMesh.pick(dc, pickPoint, this.renderMatrix);
        }

        public void render(DrawContext dc) {
            this.orderedMesh.renderOriented(dc, this.renderMatrix);
        }
    }

    protected static class ExtentCacheKey {

        protected GlobeStateKey globeStateKey;
        protected Matrix matrix;

        public ExtentCacheKey(Globe globe, Matrix matrix) {
            this.globeStateKey = globe.getGlobeStateKey();
            this.matrix = matrix;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ExtentCacheKey that = (ExtentCacheKey) o;

            if (globeStateKey != null ? !globeStateKey.equals(that.globeStateKey) : that.globeStateKey != null) {
                return false;
            }
            if (matrix != null ? !matrix.equals(that.matrix) : that.matrix != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = globeStateKey != null ? globeStateKey.hashCode() : 0;
            result = 31 * result + (matrix != null ? matrix.hashCode() : 0);
            return result;
        }
    }

    /**
     * This class holds globe-specific data for this shape. It's managed via the
     * shape-data cache in {@link
     * gov.nasa.worldwind.render.AbstractShape.AbstractShapeData}.
     */
    protected static class ShapeData extends AbstractGeneralShape.ShapeData {

        /**
         * Construct a cache entry for this shape.
         *
         * @param dc the current draw context.
         * @param shape this shape.
         */
        public ShapeData(DrawContext dc, AbstractGeneralShape shape) {
            super(dc, shape);
        }

        /**
         * Matrix to orient the shape on the surface of the globe. Cached result
         * of {@link
         * gov.nasa.worldwind.globes.Globe#computeSurfaceOrientationAtPosition(gov.nasa.worldwind.geom.Position)}
         * evaluated at the reference position.
         */
        protected Matrix surfaceOrientationMatrix;
        /**
         * Transform matrix to apply when rendering the shape. This matrix is
         * determined by the COLLADA traversal matrix, and
         * {@link #surfaceOrientationMatrix}.
         */
        protected Matrix renderMatrix;
        /**
         * Cached reference center for the shape.
         */
        protected Vec4 referenceCenter;
    }
    /**
     * Cache of shape extents computed for different transform matrices.
     */
    protected Map<ExtentCacheKey, Extent> extentCache = new HashMap<>();
    /**
     * The vertex data buffer for this shape data. The first part contains
     * vertex coordinates, the second part contains normals, and the third part
     * contains texture coordinates.
     */
    protected FloatBuffer coordBuffer;

    /**
     * Geometry objects that describe different parts of the mesh.
     */
    private ArrayList<Geometry> geometries;

    /**
     * Texture coordinates for all geometries in this shape.
     */
    protected FloatBuffer textureCoordsBuffer;

    /**
     * Number of vertices per shape. Two in the case of a line mesh, three in
     * the case of a triangle mesh.
     */
    protected int vertsPerShape;

    /**
     * Total number of shapes (lines or triangles) in this mesh. Equal to the
     * sum of the shapes in each geometry.
     */
    protected int shapeCount;

    /**
     * The index of the first normal in the {@link #coordBuffer}.
     */
    protected int normalBufferPosition;
    /**
     * The index of the first texture coordinate in the {@link #coordBuffer}.
     */
    protected int texCoordBufferPosition;
    /**
     * The slice of the {@link #coordBuffer} that contains normals.
     */
    protected FloatBuffer normalBuffer;

    /**
     * OpenGL element type for this shape (GL.GL_LINES or GL.GL_TRIANGLES).
     */
    protected int elementType=-1;

    public Mesh3D(List<? extends AbstractGeometry> geometries) {
        this.setRenderableGeometries(geometries);
    }

    public Mesh3D() {

    }

    protected final void addRenderableGeometry(AbstractGeometry geometry) {
        this.geometries.add(new Geometry(geometry));
        this.shapeCount += geometry.getCount();
    }

    protected final void setRenderableGeometries(List<? extends AbstractGeometry> geometries) {
        this.geometries = new ArrayList<>(geometries.size());
        geometries.forEach((geometry) -> {
            this.addRenderableGeometry(geometry);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractShapeData createCacheEntry(DrawContext dc) {
        return new ShapeData(dc, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractShapeData getCurrentData() {
        return this.currentData;
    }

    /**
     * Compute the shape's extent, using the active orientation matrix.
     *
     * @param dc Current draw context.
     *
     * @return The spatial extent of the shape, or null if the extent cannot be
     * determined.
     */
    protected Extent computeExtent(DrawContext dc) {
        if (this.coordBuffer == null) {
            return null;
        }
//        WWUtil.printFloatBuffer(coordBuffer, 3);
        // Compute a bounding box around the vertices in this shape.
        this.coordBuffer.rewind();
        Box box = Box.computeBoundingBox(new BufferWrapper.FloatBufferWrapper(this.coordBuffer), AbstractGeometry.COORDS_PER_VERTEX);

        Matrix matrix = this.computeRenderMatrix(dc);
        if (matrix != null) {
            List<Vec4> extrema = new ArrayList<>();
            // Compute the corners of the bounding box and transform with the active transform matrix.
            Vec4[] corners = box.getCorners();

            for (Vec4 corner : corners) {
                extrema.add(corner.transformBy4(matrix));
            }

            if (extrema.isEmpty()) {
                return null;
            }

            // Compute the bounding box around the transformed corners.
            box = Box.computeBoundingBox(extrema);
        }
        return box;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(DrawContext dc) {
        this.renderOriented(dc, null);
    }

    /**
     * Render the mesh in a given orientation.
     *
     * @param dc Current draw context.
     * @param matrix Matrix to be multiply with the current modelview matrix to
     * orient the mesh.
     */
    public void renderOriented(DrawContext dc, Matrix matrix) {
        this.currentData = (AbstractShapeData) this.shapeDataCache.getEntry(dc.getGlobe());
        if (this.currentData == null) {
            this.currentData = this.createCacheEntry(dc);
            this.shapeDataCache.addEntry(this.currentData);
        }

        ShapeData current = (ShapeData) this.currentData;
        current.renderMatrix = matrix;

        // Update current extent from cached extents. This must be done on each call to render because the same shape
        // may be drawn multiple times during a single frame with different transforms. Attempt to calculate the extent
        // if not available in the cache. It may not be possible to calculate the extent if the shape geometry has not
        // been built, in which case the extent will be computed by createMinimalGeometry.
        ExtentCacheKey extentCacheKey = new ExtentCacheKey(dc.getGlobe(), matrix);
        Extent extent = this.extentCache.get(extentCacheKey);
        if (extent == null) {
            extent = this.computeExtent(dc);
            this.extentCache.put(extentCacheKey, extent);
        }
        current.setExtent(extent);

        try {
            super.render(dc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Computes the minimum distance between this shape and the eye point.
     * <p>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must
     * be current when this method is called.
     *
     * @param dc the current draw context.
     *
     * @return the minimum distance from the shape to the eye point.
     */
    protected double computeEyeDistance(DrawContext dc) {
        Vec4 eyePoint = dc.getView().getEyePoint();

        Vec4 refPt = this.computePoint(dc.getTerrain(), this.getModelPosition());
        if (refPt != null) {
            return refPt.distanceTo3(eyePoint);
        }

        return 0;
    }

    /**
     * {@inheritDoc} Overridden because this shape uses
     * {@link gov.nasa.worldwind.ogc.collada.impl.ColladaMeshShape.OrderedMeshShape}
     * to represent this drawn instance of the mesh in the ordered renderable
     * queue.
     */
    @Override
    protected void addOrderedRenderable(DrawContext dc) {

        ShapeData current = (ShapeData) this.getCurrent();

        double eyeDistance = this.computeEyeDistance(dc);
        OrderedRenderable or = new OrderedMeshShape(this, current.renderMatrix, eyeDistance);
        dc.addOrderedRenderable(or);
    }

    /**
     * Draw the shape as an OrderedRenderable, using the specified transform
     * matrix.
     *
     * @param dc Current draw context.
     * @param pickCandidates Pick candidates for this frame.
     * @param matrix Transform matrix to apply before trying shape. m
     */
    protected void doDrawOrderedRenderable(DrawContext dc, PickSupport pickCandidates, Matrix matrix) {
        ShapeData current = (ShapeData) this.getCurrent();
        current.renderMatrix = matrix;

        super.doDrawOrderedRenderable(dc, pickCandidates);
    }

    /**
     * Compute enough geometry to determine this shape's extent, reference point
     * and eye distance.
     * <p>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must
     * be current when this method is called.
     *
     * @param dc the current draw context.
     * @param shapeData the current shape data for this shape.
     */
    protected void createMinimalGeometry(DrawContext dc, ShapeData shapeData) {
        Vec4 refPt = this.computeReferencePoint(dc.getTerrain());
        if (refPt == null) {
            return;
        }
        shapeData.setReferencePoint(refPt);

        shapeData.setEyeDistance(this.computeEyeDistance(dc, shapeData));
        shapeData.setGlobeStateKey(dc.getGlobe().getGlobeStateKey(dc));
        shapeData.setVerticalExaggeration(dc.getVerticalExaggeration());

        if (this.coordBuffer == null) {
            this.createVertexCoords(dc);
        }

        if (shapeData.getExtent() == null) {
            Extent extent = this.computeExtent(dc);
            this.extentCache.put(new ExtentCacheKey(dc.getGlobe(), shapeData.renderMatrix), extent);
            shapeData.setExtent(extent);
        }
    }

    /**
     * Computes the transform to use during rendering to orient the model.
     *
     * @param dc the current draw context
     *
     * @return the modelview transform for this shape.
     */
    protected Matrix computeRenderMatrix(DrawContext dc) {
        ShapeData current = (ShapeData) this.getCurrent();
        if (current.referenceCenter == null || current.isExpired(dc)) {
            current.referenceCenter = this.computeReferenceCenter(dc);
// TODO: Fix
            Position refPosition = dc.getGlobe().computePositionFromPoint(current.referenceCenter);
            current.surfaceOrientationMatrix = dc.getGlobe().computeSurfaceOrientationAtPosition(refPosition);
            //Vec4 point = dc.getGlobe().computeEllipsoidalPointFromPosition(refPosition.latitude, refPosition.longitude, refPosition.elevation);
            //current.surfaceOrientationMatrix = Matrix.fromTranslation(point);
        }
        if (current.renderMatrix != null) {
            return current.surfaceOrientationMatrix.multiply(current.renderMatrix);
        }
        return current.surfaceOrientationMatrix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doMakeOrderedRenderable(DrawContext dc) {
        // Clear cached extents because we are creating new geometry.
        this.extentCache.clear();

        // Do the minimum necessary to determine the model's reference point, extent and eye distance.
        this.createMinimalGeometry(dc, (ShapeData) this.getCurrent());

        // If the shape is less that a pixel in size, don't render it.
        if (this.getCurrent().getExtent() == null || dc.isSmall(this.getExtent(), 1)) {
            return false;
        }

        if (!this.intersectsFrustum(dc)) {
            return false;
        }

        this.createFullGeometry(dc);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doDrawInterior(DrawContext dc) {
        if (this.elementType!=GL.GL_LINES && this.elementType!=GL.GL_TRIANGLES) {
            String msg = Logging.getMessage("generic.InvalidType",this.elementType);
            Logging.logger().severe(msg);
            throw new UnsupportedOperationException(msg);
        }
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Create an OpenGL stack handler to handle matrix stack push/pop. Explicitly track changes to the OpenGL
        // texture and cull face states in order to eliminate the need for attribute push/pop on a per mesh basis.
        OGLStackHandler stackHandler = new OGLStackHandler();
        boolean texturesEnabled = false;
        boolean cullingEnabled = false;
        try {
            stackHandler.pushModelview(gl);
            this.setModelViewMatrix(dc);

            Material defaultMaterial = this.activeAttributes.getInteriorMaterial();
            // Interior material is applied by super.prepareToDrawInterior. But, we may
            // need to change it if different geometry elements use different materials.
            Material activeMaterial = null; //defaultMaterial;

            // When drawing with vertex arrays we can bind the vertex buffer once. When using vertex buffer objects
            // we need to check to make sure that the vbo is available each time through the loop because loading
            // textures may force vbos out of the cache (see loop below).
            if (!this.shouldUseVBOs(dc)) {
                FloatBuffer vb = this.coordBuffer;
                gl.glVertexPointer(AbstractGeometry.COORDS_PER_VERTEX, GL.GL_FLOAT, 0, vb.rewind());
            }

            for (Geometry geometry : this.geometries) {
                Material nextMaterial = geometry.material != null ? geometry.material : defaultMaterial;

                // Apply new material if necessary
                if (!dc.isPickingMode() && !nextMaterial.equals(activeMaterial)) {
                    this.applyMaterial(dc, nextMaterial);
                    activeMaterial = nextMaterial;
                }

                if (!dc.isPickingMode()
                        && this.mustApplyTexture(geometry)
                        && this.getTexture(geometry).bind(dc)) // bind initiates retrieval
                {
                    this.getTexture(geometry).applyInternalTransform(dc);

                    if (!texturesEnabled) {
                        gl.glEnable(GL.GL_TEXTURE_2D);
                        gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                        texturesEnabled = true;
                    }

                    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
                    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

                    gl.glTexCoordPointer(AbstractGeometry.TEX_COORDS_PER_VERTEX, GL.GL_FLOAT, 0, this.textureCoordsBuffer.rewind());
                } else if (texturesEnabled) {
                    gl.glDisable(GL.GL_TEXTURE_2D);
                    gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                    texturesEnabled = false;
                }

                // If this geometry is double sided, then backface culling must be disabled. Otherwise backface culling
                // must be enabled, because some SketchUp models will not render correctly without it.
                if (geometry.doubleSided && cullingEnabled) {
                    gl.glDisable(GL.GL_CULL_FACE);
                    cullingEnabled = false;
                } else if (!geometry.doubleSided && !cullingEnabled) {
                    gl.glEnable(GL.GL_CULL_FACE);
                    cullingEnabled = true;
                }

                // Look up VBO IDs each time through the loop because binding a texture may bump a VBO out of the cache.
                // If VBOs are not used, the vertex array is bound once, before the loop.
                int[] vboIds = null;
                if (this.shouldUseVBOs(dc)) {
                    vboIds = this.getVboIds(dc);
                    if (vboIds == null) {
                        FloatBuffer vb = this.coordBuffer;
                        WWUtil.printFloatBuffer(vb, 3);
                        gl.glVertexPointer(AbstractGeometry.COORDS_PER_VERTEX, GL.GL_FLOAT, 0, vb.rewind());
                    }
                }

                if (vboIds != null) {
                    this.doDrawInteriorVBO(dc, geometry, vboIds);
                } else {
                    this.doDrawInteriorVA(dc, geometry);
                }
            }
        } finally {
            // Restore the OpenGL matrix stack state.
            stackHandler.pop(gl);

            // Restore the previous OpenGL texture state and cull face state. We do this in order to ensure that any
            // subsequent ColladaMeshShape instances processed during batch picking/rendering have the same initial
            // conditions as the first ColladaMeshShape. Without this restore, subsequent ColladaMeshShapes without a
            // texture will have the GL_TEXTURE_COORD_ARRAY state enabled during glDrawArrays.
            if (texturesEnabled) {
                gl.glDisable(GL.GL_TEXTURE_2D);
                gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
            }

            if (cullingEnabled) {
                gl.glDisable(GL.GL_CULL_FACE);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isOrderedRenderableValid(DrawContext dc
    ) {
        return this.coordBuffer != null;
    }

    /**
     * Create the shape's vertex coordinates. The coordinates are stored in
     * {@link #coordBuffer}.
     *
     * @param dc Current draw context.
     */
    protected void createVertexCoords(DrawContext dc) {
        int size = this.shapeCount * this.vertsPerShape * AbstractGeometry.COORDS_PER_VERTEX;

        // Capture the position at which normals buffer starts (in case there are normals)
        this.normalBufferPosition = size;
        if (this.mustApplyLighting(dc, null)) {
            size += (this.shapeCount * this.vertsPerShape * AbstractGeometry.COORDS_PER_VERTEX);
        }

        // Capture the position at which texture coordinate buffer starts (in case that textures are applied)
        this.texCoordBufferPosition = size;
        if (this.mustApplyTexture(dc)) {
            size += (this.shapeCount * this.vertsPerShape * AbstractGeometry.TEX_COORDS_PER_VERTEX);
        }

        if (this.coordBuffer != null && this.coordBuffer.capacity() >= size) {
            this.coordBuffer.clear();
        } else {
            this.coordBuffer = Buffers.newDirectFloatBuffer(size);
        }

        for (Geometry geometry : this.geometries) {
            geometry.offset = this.coordBuffer.position() / this.vertsPerShape;
            geometry.nativeGeometry.getVertices(this.coordBuffer);
        }
    }

    /**
     * Create this shape's vertex normals. The normals are stored in
     * {@link #normalBuffer}.
     */
    private void createNormals() {
        this.coordBuffer.position(this.normalBufferPosition);
        this.normalBuffer = this.coordBuffer.slice();

        for (Geometry geometry : this.geometries) {
            if (geometry.nativeGeometry.hasNormals()) { //getNormalAccessor() != null) {
                geometry.nativeGeometry.getNormals(this.normalBuffer);
            } else {
                int thisSize = geometry.nativeGeometry.getCount() * this.vertsPerShape * AbstractGeometry.COORDS_PER_VERTEX;
                this.normalBuffer.position(this.normalBuffer.position() + thisSize);
            }
        }
    }

    /**
     * Create this shape's texture coordinates. The texture coordinates are
     * stored in {@link #textureCoordsBuffer}.
     */
    protected void createTexCoords() {
        this.coordBuffer.position(this.texCoordBufferPosition);
        this.textureCoordsBuffer = this.coordBuffer.slice();

        for (Geometry geometry : this.geometries) {
            if (this.mustApplyTexture(geometry)) {
                geometry.nativeGeometry.getTextureCoordinates(this.textureCoordsBuffer);
            } else {
                int thisSize = geometry.nativeGeometry.getCount() * this.vertsPerShape
                        * AbstractGeometry.TEX_COORDS_PER_VERTEX;
                this.textureCoordsBuffer.position(this.textureCoordsBuffer.position() + thisSize);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillVBO(DrawContext dc) {
        GL gl = dc.getGL();
        ShapeData shapeData = (ShapeData) getCurrentData();

        int[] vboIds = this.getVboIds(dc);
        if (vboIds == null) {
            int size = this.coordBuffer.limit() * Buffers.SIZEOF_FLOAT;

            vboIds = new int[1];
            gl.glGenBuffers(vboIds.length, vboIds, 0);
            dc.getGpuResourceCache().put(shapeData.getVboCacheKey(), vboIds, GpuResourceCache.VBO_BUFFERS,
                    size);
        }

        try {
            FloatBuffer vb = this.coordBuffer;
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, vb.limit() * Buffers.SIZEOF_FLOAT, vb.rewind(), GL.GL_STATIC_DRAW);
        } finally {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }

    /**
     * Pick the mesh in a given orientation.
     *
     * @param dc Current draw context.
     * @param pickPoint Current pick point.
     * @param matrix Matrix to multiply with the current modelview matrix to
     * orient the mesh.
     */
    protected void pick(DrawContext dc, Point pickPoint, Matrix matrix) {
        // This method is called only when ordered renderables are being drawn.

        if (dc == null) {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.pickSupport.clearPickList();
        try {
            this.pickSupport.beginPicking(dc);
            this.renderOriented(dc, matrix);
        } finally {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer);
        }
    }

    /**
     * {@inheritDoc} Overridden because ColladaMeshShape uses OrderedMeshShape
     * instead of adding itself to the ordered renderable queue.
     */
    @Override
    protected void drawBatched(DrawContext dc) {
        // Draw as many as we can in a batch to save ogl state switching.
        Object nextItem = dc.peekOrderedRenderables();

        if (!dc.isPickingMode()) {
            while (nextItem != null && nextItem.getClass() == OrderedMeshShape.class) {
                OrderedMeshShape or = (OrderedMeshShape) nextItem;
                Mesh3D shape = or.orderedMesh;
                if (!shape.isEnableBatchRendering()) {
                    break;
                }

                dc.pollOrderedRenderables(); // take it off the queue
                shape.doDrawOrderedRenderable(dc, this.pickSupport, or.renderMatrix);

                nextItem = dc.peekOrderedRenderables();
            }
        } else if (this.isEnableBatchPicking()) {
            super.drawBatched(dc);
            while (nextItem != null && nextItem.getClass() == this.getClass()) {
                OrderedMeshShape or = (OrderedMeshShape) nextItem;
                Mesh3D shape = or.orderedMesh;
                if (!shape.isEnableBatchRendering() || !shape.isEnableBatchPicking()) {
                    break;
                }

                if (shape.pickLayer != this.pickLayer) // batch pick only within a single layer
                {
                    break;
                }

                dc.pollOrderedRenderables(); // take it off the queue
                shape.doDrawOrderedRenderable(dc, this.pickSupport, or.renderMatrix);

                nextItem = dc.peekOrderedRenderables();
            }
        }
    }

    /**
     * Computes this shape's reference center.
     *
     * @param dc the current draw context.
     *
     * @return the computed reference center, or null if it cannot be computed.
     */
    protected Vec4 computeReferenceCenter(DrawContext dc) {
        Position pos = this.getReferencePosition();
        if (pos == null) {
            return null;
        }

        return this.computePoint(dc.getTerrain(), pos);
    }

    /**
     * Create full geometry for the shape, including normals and texture
     * coordinates.
     *
     * @param dc Current draw context.
     */
    protected void createFullGeometry(DrawContext dc) {
        if (this.normalBuffer == null && this.mustApplyLighting(dc, null)) {
            this.createNormals();
        }

        if (this.textureCoordsBuffer == null && this.mustApplyTexture(dc)) {
            this.createTexCoords();
        }

        for (Geometry geometry : this.geometries) {
            if (geometry.material == null) {
                geometry.material = getMaterial(geometry.nativeGeometry);
            }

            geometry.doubleSided = isDoubleSided(geometry.nativeGeometry);
        }
    }

    /**
     * Called during drawing to set the modelview matrix to apply the correct
     * position, scale and orientation for this shape.
     *
     * @param dc the current DrawContext
     *
     * @throws IllegalArgumentException if draw context is null or the draw
     * context GL is null
     */
    protected void setModelViewMatrix(DrawContext dc) {
        if (dc.getGL() == null) {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Matrix renderMatrix = this.computeRenderMatrix(dc);
        Matrix modelViewMatrix = dc.getView().getModelviewMatrix();
        if (renderMatrix != null) {
            modelViewMatrix = modelViewMatrix.multiply(renderMatrix);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glMatrixMode(GL2.GL_MODELVIEW);

        double[] matrixArray = new double[16];
        modelViewMatrix.toArray(matrixArray, 0, false);
        gl.glLoadMatrixd(matrixArray, 0);

    }

    /**
     * Apply a material to the active draw context.
     *
     * @param dc Current draw context.
     * @param material Material to apply.
     */
    protected void applyMaterial(DrawContext dc, Material material) {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        ShapeAttributes activeAttrs = this.getActiveAttributes();
        double opacity = activeAttrs.getInteriorOpacity();

        // We don't need to enable or disable lighting; that's handled by super.prepareToDrawInterior.
        if (this.mustApplyLighting(dc, activeAttrs)) {
            material.apply(gl, GL2.GL_FRONT_AND_BACK, (float) opacity);
        } else {
            Color sc = material.getDiffuse();
            gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                    (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return True if any geometry in this shape includes a texture.
     */
    @Override
    protected boolean mustApplyTexture(DrawContext dc) {
        for (Geometry geometry : this.geometries) {
            if (this.mustApplyTexture(geometry)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Draw one geometry in the mesh interior using vertex arrays.
     *
     * @param dc Current draw context.
     * @param geometry Geometry to draw.
     */
    protected void doDrawInteriorVA(DrawContext dc, Geometry geometry) {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        if (geometry.offset == -1) {
            return;
        }

        if (!dc.isPickingMode() && this.mustApplyLighting(dc, null) && this.normalBuffer != null) {
            gl.glNormalPointer(GL.GL_FLOAT, 0, this.normalBuffer.rewind());
        }

        gl.glDrawArrays(this.elementType, geometry.offset, geometry.nativeGeometry.getCount() * this.vertsPerShape);
    }

    /**
     * Draw one geometry in the mesh interior using vertex buffer objects.
     *
     * @param dc Current draw context.
     * @param geometry Geometry to draw.
     * @param vboIds Array of vertex buffer identifiers. The first element of
     * the array identifies the buffer that contains vertex coordinates and
     * normal vectors.
     */
    protected void doDrawInteriorVBO(DrawContext dc, Geometry geometry, int[] vboIds) {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        if (geometry.offset == -1) {
            return;
        }

        try {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
            gl.glVertexPointer(AbstractGeometry.COORDS_PER_VERTEX, GL.GL_FLOAT, 0, 0);

            if (!dc.isPickingMode() && this.mustApplyLighting(dc, null) && this.normalBuffer != null) {
                gl.glNormalPointer(GL.GL_FLOAT, 0, this.normalBufferPosition * Buffers.SIZEOF_FLOAT);
            }

            gl.glDrawArrays(this.elementType, geometry.offset,
                    geometry.nativeGeometry.getCount() * this.vertsPerShape);
        } finally {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }

    /**
     * {@inheritDoc} Does nothing, all drawing is performed by
     * {@link #doDrawInterior(gov.nasa.worldwind.render.DrawContext)}.
     */
    @Override
    protected void doDrawOutline(DrawContext dc) {
        // Do nothing. All drawing is performed in doDrawInterior
    }

    /**
     * {@inheritDoc}
     * <p>
     * COLLADA shapes do not support intersection tests because the shape may be
     * rendered multiple times with different transform matrices. It's not
     * possible to determine intersection without the transform matrix applied
     * when the shape is rendered.
     *
     * @return Always returns {@code null}.
     */
    @Override
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException {
        return null;
    }

    /**
     * {@inheritDoc} Overridden to invalidate cached geometry when the model
     * position is changed.
     */
    @Override
    public void setModelPosition(Position modelPosition) {
        if (modelPosition != this.modelPosition) {
            this.modelPosition = modelPosition;
            this.reset();
        }
    }

    protected void setElementType(int elementType) {
        this.elementType = elementType;
    }

    protected void setVertsPerShape(int vertsPerShape) {
        this.vertsPerShape = vertsPerShape;
    }

    protected int getVertsPerShape() {
        return this.vertsPerShape;
    }

    protected int getShapeCount() {
        return this.shapeCount;
    }

    protected ArrayList<Geometry> getGeometries() {
        return this.geometries;
    }

    @Override
    protected OGLStackHandler beginDrawing(DrawContext dc, int attrMask) {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = super.beginDrawing(dc, attrMask);

        if (!dc.isPickingMode()) {
            // Push an identity texture matrix. This prevents drawSides() from leaking GL texture matrix state. The
            // texture matrix stack is popped from OGLStackHandler.pop(), in the finally block below.
            ogsh.pushTextureIdentity(gl);

            if (this.mustApplyLighting(dc, null)) {
                // We apply a scale transform on the modelview matrix, so the normal vectors must be re-normalized
                // before lighting is computed.
                gl.glEnable(GL2.GL_NORMALIZE);
            }
        }

        return ogsh;
    }

    /**
     * Indicates whether or not a texture must be applied to a geometry.
     *
     * @param geometry Geometry to test.
     *
     * @return True if the specified geometry includes a texture.
     */
    protected boolean mustApplyTexture(Geometry geometry) {
        return false;
    }

    /**
     * Indicates the texture applied to this shape.
     *
     * @param geometry The geometry to set the texture from.
     * @return The texture that must be applied to the shape, or null if there
     * is no texture, or the texture is not available.
     */
    protected WWTexture getTexture(Geometry geometry) {
        return null;
    }

    /**
     * Indicates the material applied to a geometry.
     *
     * @param geometry Geometry for which to find material.
     *
     * @return Material to apply to the geometry. If the COLLADA document does
     * not define a material, this method return a default material.
     */
    protected Material getMaterial(AbstractGeometry geometry) {
        return null;
    }

    /**
     * Indicates whether or not a geometry is double sided. A geometry is double
     * sided if its <i>effect</i> element contains a <i>technique</i> for the
     * profile "GOOGLEEARTH", and the technique includes a <i>double_sided</i>
     * field. The <i>double_sided</i> field is not part of the COLLADA
     * specification, but many COLLADA models packaged in KML include the
     * element.
     *
     * @param geometry Geometry to test.
     *
     * @return True if the geometry is marked as double sided. Otherwise false.
     */
    protected boolean isDoubleSided(AbstractGeometry geometry) {
        return false;
    }
}

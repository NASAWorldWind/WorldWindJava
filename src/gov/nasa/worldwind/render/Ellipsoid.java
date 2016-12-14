/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.airspaces.Geometry;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import javax.xml.stream.*;
import java.io.IOException;
import java.nio.*;

/**
 * A general ellipsoid volume defined by a center position and the three ellipsoid axis radii. If A is the radius in the
 * north-south direction, and b is the radius in the east-west direction, and c is the radius in the vertical direction
 * (increasing altitude), then A == B == C defines a sphere, A == B > C defines a vertically flattened spheroid
 * (disk-shaped), A == B < C defines a vertically stretched spheroid.
 *
 * @author tag
 * @version $Id: Ellipsoid.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Ellipsoid extends RigidShape
{
    protected static final int DEFAULT_SUBDIVISIONS = 2;

    // Geometry.
    protected int subdivisions = DEFAULT_SUBDIVISIONS;

    /** Construct a default ellipsoid with centerPosition ZERO and radii all equal to one. */
    public Ellipsoid()
    {
        this.setUpGeometryCache();
    }

    /**
     * Construct an ellipsoid from a specified center position and axes lengths.
     *
     * @param centerPosition   the ellipsoid's center position.
     * @param northSouthRadius the ellipsoid's north-south radius, in meters.
     * @param verticalRadius   the ellipsoid's vertical radius, in meters.
     * @param eastWestRadius   the ellipsoid's east-west radius, in meters.
     *
     * @throws IllegalArgumentException if the center position is null or any of the radii are not greater than 0.
     */
    public Ellipsoid(Position centerPosition, double northSouthRadius, double verticalRadius, double eastWestRadius)
    {
        if (centerPosition == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (northSouthRadius <= 0 || eastWestRadius <= 0 || verticalRadius <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.centerPosition = centerPosition;
        this.northSouthRadius = northSouthRadius;
        this.verticalRadius = verticalRadius;
        this.eastWestRadius = eastWestRadius;

        this.setUpGeometryCache();
    }

    /**
     * Construct an ellipsoid from a specified center position, axes lengths and rotation angles. All angles are
     * specified in degrees and positive angles are counter-clockwise.
     *
     * @param centerPosition   the ellipsoid's center position.
     * @param northSouthRadius the ellipsoid's north-south radius, in meters.
     * @param verticalRadius   the ellipsoid's vertical radius, in meters.
     * @param eastWestRadius   the ellipsoid's east-west radius, in meters.
     * @param heading          the ellipsoid's azimuth, its rotation about its vertical axis.
     * @param tilt             the ellipsoids pitch, its rotation about its east-west axis.
     * @param roll             the ellipsoid's roll, its rotation about its north-south axis.
     */
    public Ellipsoid(Position centerPosition, double northSouthRadius, double verticalRadius, double eastWestRadius,
        Angle heading, Angle tilt, Angle roll)
    {
        if (centerPosition == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (northSouthRadius <= 0 || eastWestRadius <= 0 || verticalRadius <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.centerPosition = centerPosition;
        this.northSouthRadius = northSouthRadius;
        this.verticalRadius = verticalRadius;
        this.eastWestRadius = eastWestRadius;
        this.heading = heading;
        this.tilt = tilt;
        this.roll = roll;

        this.setUpGeometryCache();
    }

    public int getSubdivisions()
    {
        return this.subdivisions;
    }

    /**
     * Computes a threshold value, based on the current detailHint, for use in the sufficientDetail() calculation.
     *
     * @return the detailThreshold
     */
    protected double computeDetailThreshold()
    {
        // these values must be calibrated on a shape-by-shape basis
        double detailThreshold = 20;
        double rangeDetailThreshold = 40;

        detailThreshold += this.getDetailHint() * rangeDetailThreshold;

        return detailThreshold;
    }

    /**
     * Computes the number of subdivisions necessary to achieve the expected Level of Detail given the shape's
     * relationship to the viewer.
     *
     * @param dc        the current drawContext.
     * @param shapeData the current globe-specific shape data
     */
    protected void computeSubdivisions(DrawContext dc, ShapeData shapeData)
    {
        // test again possible subdivision values
        int minDivisions = 0;
        int maxDivisions = 6;

        if (shapeData.getExtent() != null)
        {
            for (int divisions = minDivisions; divisions <= maxDivisions; divisions++)
            {
                this.subdivisions = divisions;
                if (this.sufficientDetail(dc, divisions, shapeData))
                    break;
            }
        }
    }

    protected boolean sufficientDetail(DrawContext dc, int subdivisions, ShapeData shapeData)
    {
        if (dc.getView() == null)
        {
            String message = "nullValue.DrawingContextViewIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (shapeData == null)
            return false;

        Extent extent = shapeData.getExtent();
        if (extent == null)
            return true;

        double thresholdDensity = this.computeDetailThreshold();

        double d = dc.getView().getEyePoint().distanceTo3(extent.getCenter());
        //double pixelSize = dc.getView().computePixelSizeAtDistance(d);
        double shapeScreenSize = extent.getDiameter() / d;

        // formula for this object's current vertex density
        double vertexDensity = Math.pow(subdivisions, 3) / shapeScreenSize;

        return vertexDensity > thresholdDensity;
    }

    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        // check if current LOD is sufficient
        int oldDivisions = this.subdivisions;
        computeSubdivisions(dc, this.getCurrentShapeData());
        if (oldDivisions != this.subdivisions)
            return true;

        return super.mustRegenerateGeometry(dc);
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    /**
     * Sets the Geometry mesh for this Ellipsoid, either by pulling it from the geometryCache, or by creating it anew if
     * the appropriate geometry does not yet exist in the cache.
     *
     * @param shapeData the current shape data.
     */
    protected void makeGeometry(ShapeData shapeData)
    {
        // attempt to retrieve a cached unit ellipsoid with the same number of subdivisions
        Object cacheKey = new Geometry.CacheKey(this.getClass(), "Sphere", this.subdivisions);
        Geometry geom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (geom == null)
        {
            // if none exists, create a new one
            shapeData.addMesh(0, new Geometry());
            makeUnitSphere(this.subdivisions, shapeData.getMesh(0));
            //this.restart(dc, geom);
            this.getGeometryCache().add(cacheKey, shapeData.getMesh(0));
        }
        else
        {
            // otherwise, just use the one from the cache
            shapeData.addMesh(0, geom);
        }
    }

    /**
     * Generates a unit sphere geometry, including the vertices, indices, normals and texture coordinates, tessellated
     * with the specified number of divisions.
     *
     * @param subdivisions the number of times to subdivide the unit sphere geometry
     * @param dest         the Geometry container to hold the computed points, etc.
     */
    protected void makeUnitSphere(int subdivisions, Geometry dest)
    {
        float radius = 1.0f;

        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(GeometryBuilder.OUTSIDE);

        // create ellipsoid in model space
        GeometryBuilder.IndexedTriangleBuffer itb =
            gb.tessellateSphereBuffer(radius, subdivisions);

        // add extra vertices so that texture will not have a seam
        int seamVerticesIndex = itb.getVertexCount();
        gb.fixSphereSeam(itb, (float) Math.PI);

        FloatBuffer normalBuffer = Buffers.newDirectFloatBuffer(3 * itb.getVertexCount());
        gb.makeEllipsoidNormals(itb, normalBuffer);

        FloatBuffer textureCoordBuffer = Buffers.newDirectFloatBuffer(2 * itb.getVertexCount());
        gb.makeUnitSphereTextureCoordinates(itb, textureCoordBuffer, seamVerticesIndex);

        dest.setElementData(GL.GL_TRIANGLES, itb.getIndexCount(), itb.getIndices());
        dest.setVertexData(itb.getVertexCount(), itb.getVertices());
        dest.setNormalData(normalBuffer.limit(), normalBuffer);
        dest.setTextureCoordData(textureCoordBuffer.limit(), textureCoordBuffer);
    }

    /**
     * Renders the Ellipsoid, using data from the provided buffer and the given parameters.
     *
     * @param dc            the current draw context
     * @param mode          the render mode
     * @param count         the number of elements to be drawn
     * @param type          the data type of the elements to be drawn
     * @param elementBuffer the buffer containing the list of elements to be drawn
     * @param shapeData     this shape's current globe-specific shape data
     * @param face          the shape face currently being drawn
     */
    protected void drawGeometry(DrawContext dc, int mode, int count, int type, Buffer elementBuffer,
        ShapeData shapeData, int face)
    {
        if (elementBuffer == null)
        {
            String message = "nullValue.ElementBufferIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Geometry mesh = shapeData.getMesh(face);

        if (mesh.getBuffer(Geometry.VERTEX) == null)
        {
            String message = "nullValue.VertexBufferIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        int size, glType, stride;
        Buffer vertexBuffer, normalBuffer;

        size = mesh.getSize(Geometry.VERTEX);
        glType = mesh.getGLType(Geometry.VERTEX);
        stride = mesh.getStride(Geometry.VERTEX);
        vertexBuffer = mesh.getBuffer(Geometry.VERTEX);

        normalBuffer = null;
        if (!dc.isPickingMode())
        {
            if (mustApplyLighting(dc, null))
            {
                normalBuffer = mesh.getBuffer(Geometry.NORMAL);
                if (normalBuffer == null)
                {
                    gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
                }
                else
                {
                    glType = mesh.getGLType(Geometry.NORMAL);
                    stride = mesh.getStride(Geometry.NORMAL);
                    gl.glNormalPointer(glType, stride, normalBuffer);
                }
            }
        }

        // cull the back face
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glFrontFace(GL.GL_CCW);

        // testing: disable VBO's
        // boolean vboState = dc.getGLRuntimeCapabilities().isVertexBufferObjectEnabled();
        // dc.getGLRuntimeCapabilities().setVertexBufferObjectEnabled(true);

        // decide whether to draw with VBO's or VA's
        if (this.shouldUseVBOs(dc) && (this.getVboIds(getSubdivisions(), dc)) != null)
        {
            // render using VBO's
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, getVboIds(getSubdivisions(), dc)[2 * face]);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, this.getVboIds(getSubdivisions(), dc)[2 * face + 1]);

            gl.glVertexPointer(size, glType, stride, 0);
            gl.glDrawElements(mode, count, type, 0);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        else
        {
            // render using vertex arrays
            gl.glVertexPointer(size, glType, stride, vertexBuffer.rewind());
            gl.glDrawElements(mode, count, type, elementBuffer);
        }

        // turn off normals rescaling, which was turned on because shape had to be scaled
        gl.glDisable(GL2.GL_RESCALE_NORMAL);

        // restore VBO state
        // testing: dc.getGLRuntimeCapabilities().setVertexBufferObjectEnabled(false);

        // disable back face culling
        gl.glDisable(GL.GL_CULL_FACE);

        if (!dc.isPickingMode())
        {
            if (mustApplyLighting(dc, null))
            {
                // re-enable normals if we temporarily turned them off earlier
                if (normalBuffer == null)
                    gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            }
            // this.logGeometryStatistics(dc, geom);
        }
    }

    /**
     * Generates ellipsoidal geometry, including the vertices, indices, normals and texture coordinates, tessellated
     * with the specified number of divisions.
     *
     * @param a            the Ellipsoid radius along the east-west axis
     * @param b            the Ellipsoid radius along the vertical axis
     * @param c            the Ellipsoid radius along the north-south axis
     * @param subdivisions the number of times to subdivide the unit sphere geometry
     * @param dest         the Geometry container to hold the computed points, etc.
     */

    protected void makeEllipsoid(double a, double b, double c, int subdivisions, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(GeometryBuilder.OUTSIDE);

        // create ellipsoid in model space
        GeometryBuilder.IndexedTriangleBuffer itb =
            gb.tessellateEllipsoidBuffer((float) a, (float) b, (float) c, subdivisions);

        FloatBuffer normalBuffer = Buffers.newDirectFloatBuffer(3 * itb.getVertexCount());
        gb.makeIndexedTriangleBufferNormals(itb, normalBuffer);

        dest.setElementData(GL.GL_TRIANGLES, itb.getIndexCount(), itb.getIndices());
        dest.setVertexData(itb.getVertexCount(), itb.getVertices());
        dest.setNormalData(normalBuffer.limit(), normalBuffer);
    }

    protected ShapeData createIntersectionGeometry(Terrain terrain)
    {
        ShapeData shapeData = new ShapeData(null, this);
        shapeData.setGlobeStateKey(terrain.getGlobe().getGlobeStateKey());

        Geometry geom = new Geometry();
        makeUnitSphere(6, geom);    // use maximum subdivisions for good intersection accuracy

        // transform the vertices from local to world coords
        Matrix matrix = computeRenderMatrix(terrain.getGlobe(), terrain.getVerticalExaggeration());
        FloatBuffer newVertices = computeTransformedVertices((FloatBuffer) geom.getBuffer(Geometry.VERTEX),
            geom.getCount(Geometry.VERTEX), matrix);
        geom.setVertexData(geom.getCount(Geometry.VERTEX), newVertices);

        shapeData.addMesh(0, geom);
        shapeData.setReferencePoint(this.computeReferencePoint(terrain.getGlobe(),
            terrain.getVerticalExaggeration()));
        shapeData.setExtent(getExtent(terrain.getGlobe(), terrain.getVerticalExaggeration()));

        return shapeData;
    }

    /** No export formats supported. */
    @Override
    public String isExportFormatSupported(String mimeType)
    {
        // Overridden because this shape does not support export to KML.
        return Exportable.FORMAT_NOT_SUPPORTED;
    }

    @Override
    protected void doExportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        String message = Logging.getMessage("generic.UnsupportedOperation", "doExportAsKML");
        Logging.logger().severe(message);
        throw new UnsupportedOperationException(message);
    }
}

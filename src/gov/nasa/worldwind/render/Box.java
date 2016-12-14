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
import java.util.List;

/**
 * A general box volume defined by a center position and three axis radii.
 *
 * @author ccrick
 * @version $Id: Box.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Box extends RigidShape
{
    protected static final int DEFAULT_SUBDIVISIONS = 0;

    @SuppressWarnings({"FieldCanBeLocal"})
    protected int faceCount = 6;   // number of separate geometric faces that comprise this Box
    // The faces are numbered as follows:
    // face 0: right face
    // face 1: front face
    // face 2: left face
    // face 3: back face
    // face 4: top face
    // face 5: bottom face
    protected int subdivisions = DEFAULT_SUBDIVISIONS;

    /** Construct a box with default parameters */
    public Box()
    {
        this.setUpGeometryCache();
    }

    /**
     * Construct a box from a specified center position and axes lengths.
     *
     * @param centerPosition   the box's center position.
     * @param northSouthRadius the box's north-south radius, in meters.
     * @param verticalRadius   the box's vertical radius, in meters.
     * @param eastWestRadius   the box's east-west radius, in meters.
     *
     * @throws IllegalArgumentException if the center position is null or any of the radii are not greater than 0.
     */
    public Box(Position centerPosition, double northSouthRadius, double verticalRadius, double eastWestRadius)
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
     * Construct a Box from a specified center position, axes lengths and rotation angles. All angles are specified in
     * degrees and positive angles are counter-clockwise.
     *
     * @param centerPosition   the Box's center position.
     * @param northSouthRadius the Box's north-south radius, in meters.
     * @param verticalRadius   the Box's vertical radius, in meters.
     * @param eastWestRadius   the Box's east-west radius, in meters.
     * @param heading          the Box's azimuth, its rotation about its vertical axis.
     * @param tilt             the Box pitch, its rotation about its east-west axis.
     * @param roll             the Box's roll, its rotation about its north-south axis.
     */
    public Box(Position centerPosition, double northSouthRadius, double verticalRadius, double eastWestRadius,
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

    @Override
    public int getFaceCount()
    {
        return this.faceCount;
    }

    public int getSubdivisions()
    {
        return this.subdivisions;
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
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    /**
     * Sets the Geometry mesh for this Box, either by pulling it from the geometryCache, or by creating it anew if the
     * appropriate geometry does not yet exist in the cache.
     *
     * @param shapeData this shape's current shape data.
     */
    protected void makeGeometry(ShapeData shapeData)
    {
        // attempt to retrieve a cached unit box with the same number of subdivisions
        Object cacheKey = new Geometry.CacheKey(this.getClass(), "Box0", this.subdivisions);
        Geometry geom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (geom == null)
        {
            // if none exists, create a new one
            makeUnitBox(this.subdivisions, shapeData.getMeshes());
            for (int piece = 0; piece < getFaceCount(); piece++)
            {
                if (offsets.get(piece) == null)  // if texture offsets don't exist, set default values to 0
                    offsets.put(piece, new OffsetsList());
                // add the new mesh pieces to the cache
                cacheKey = new Geometry.CacheKey(this.getClass(), "Box" + piece, this.subdivisions);
                this.getGeometryCache().add(cacheKey, shapeData.getMesh(piece));
            }
        }
        else
        {
            // otherwise, just use the one from the cache
            for (int piece = 0; piece < getFaceCount(); piece++)
            {
                if (offsets.get(piece) == null)  // if texture offsets don't exist, set default values to 0
                    offsets.put(piece, new OffsetsList());
                cacheKey = new Geometry.CacheKey(this.getClass(), "Box" + piece, this.subdivisions);
                geom = (Geometry) this.getGeometryCache().getObject(cacheKey);
                shapeData.addMesh(piece, geom);
            }
        }
    }

    /**
     * Generates a unit box geometry, including the vertices, indices, normals and texture coordinates, tessellated with
     * the specified number of divisions.
     *
     * @param subdivisions the number of times to subdivide the unit box geometry
     * @param dest         the Geometry container to hold the computed points, etc.
     */
    /*
    protected void makeUnitBox(int subdivisions, Geometry dest)
    {
        float radius = 1.0f;

        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(GeometryBuilder.OUTSIDE);

        // create box in model space
        GeometryBuilder.IndexedTriangleBuffer itb =
            gb.tessellateBoxBuffer(radius, subdivisions);

        FloatBuffer normalBuffer = Buffers.newDirectFloatBuffer(3 * itb.getVertexCount());
        gb.makeIndexedTriangleBufferNormals(itb, normalBuffer);

        FloatBuffer textureCoordBuffer = Buffers.newDirectFloatBuffer(2 * itb.getVertexCount());
        gb.makeUnitBoxTextureCoordinates(textureCoordBuffer, itb.getVertexCount());

        dest.setElementData(GL.GL_TRIANGLES, itb.getIndexCount(), itb.getIndices());
        dest.setVertexData(itb.getVertexCount(), itb.getVertices());
        dest.setNormalData(normalBuffer.limit(), normalBuffer);
        dest.setTextureCoordData(textureCoordBuffer.limit(), textureCoordBuffer);
    }
    */

    /**
     * Generates a unit box geometry, including the vertices, indices, normals and texture coordinates, tessellated with
     * the specified number of divisions.
     *
     * @param subdivisions the number of times to subdivide the unit box geometry
     * @param meshes       the Geometry list to hold the computed points, etc. for all Geometries
     */
    protected void makeUnitBox(int subdivisions, List<Geometry> meshes)
    {
        float radius = 1.0f;
        Geometry dest;

        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(GeometryBuilder.OUTSIDE);

        for (int index = 0; index < getFaceCount(); index++)
        {
            // create box in model space
            GeometryBuilder.IndexedTriangleBuffer itb =
                gb.tessellateBoxBuffer(index, radius, subdivisions);

            FloatBuffer normalBuffer = Buffers.newDirectFloatBuffer(3 * itb.getVertexCount());
            gb.makeIndexedTriangleBufferNormals(itb, normalBuffer);

            FloatBuffer textureCoordBuffer = Buffers.newDirectFloatBuffer(2 * itb.getVertexCount());
            gb.makeUnitBoxTextureCoordinates(index, textureCoordBuffer, itb.getVertexCount());

            dest = new Geometry();

            dest.setElementData(GL.GL_TRIANGLES, itb.getIndexCount(), itb.getIndices());
            dest.setVertexData(itb.getVertexCount(), itb.getVertices());
            dest.setNormalData(normalBuffer.limit(), normalBuffer);
            dest.setTextureCoordData(textureCoordBuffer.limit(), textureCoordBuffer);

            meshes.add(index, dest);
        }
    }

    /**
     * Renders the Box, using data from the provided buffer and the given parameters.
     *
     * @param dc            the current draw context
     * @param mode          the render mode
     * @param count         the number of elements to be drawn
     * @param type          the data type of the elements to be drawn
     * @param elementBuffer the buffer containing the list of elements to be drawn
     * @param shapeData     this shape's current globe-specific shape data
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
        //gl.glEnable(GL.GL_CULL_FACE);
        //gl.glFrontFace(GL.GL_CCW);

        // Testing: disable VBO's
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

        // Testing: restore VBO state
        // dc.getGLRuntimeCapabilities().setVertexBufferObjectEnabled(false);

        // disable back face culling
        // gl.glDisable(GL.GL_CULL_FACE);

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

    protected ShapeData createIntersectionGeometry(Terrain terrain)
    {
        // TODO: add error checking

        ShapeData shapeData = new ShapeData(null, this);
        shapeData.setGlobeStateKey(terrain.getGlobe().getGlobeStateKey());
        Geometry mesh;

        makeUnitBox(0, shapeData.getMeshes());    // use maximum subdivisions for good intersection accuracy

        Matrix matrix = computeRenderMatrix(terrain.getGlobe(), terrain.getVerticalExaggeration());

        for (int i = 0; i < getFaceCount(); i++)
        {
            mesh = shapeData.getMesh(i);
            // transform the vertices from local to world coords
            FloatBuffer newVertices = computeTransformedVertices((FloatBuffer) mesh.getBuffer(Geometry.VERTEX),
                mesh.getCount(Geometry.VERTEX), matrix);
            mesh.setVertexData(mesh.getCount(Geometry.VERTEX), newVertices);
        }

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


/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import com.jogamp.opengl.glu.*;
import java.nio.*;

/**
 * TODO: Combine these capabilities into PolygonTessellator with support for pattern used by ShapefileExtrudedPolygons.
 * TODO: Keep the combined class in package gov.nasa.worldwind.util.
 *
 * @author dcollins
 * @version $Id: PolygonTessellator2.java 2367 2014-10-02 23:37:12Z dcollins $
 */
public class PolygonTessellator2
{
    protected static class TessCallbackAdapter extends GLUtessellatorCallbackAdapter
    {
        @Override
        public void beginData(int type, Object userData)
        {
            ((PolygonTessellator2) userData).tessBegin(type);
        }

        @Override
        public void edgeFlagData(boolean boundaryEdge, Object userData)
        {
            ((PolygonTessellator2) userData).tessEdgeFlag(boundaryEdge);
        }

        @Override
        public void vertexData(Object vertexData, Object userData)
        {
            ((PolygonTessellator2) userData).tessVertex(vertexData);
        }

        @Override
        public void endData(Object userData)
        {
            ((PolygonTessellator2) userData).tessEnd();
        }

        @Override
        public void combineData(double[] coords, Object[] vertexData, float[] weight, Object[] outData, Object userData)
        {
            ((PolygonTessellator2) userData).tessCombine(coords, vertexData, weight, outData);
        }
    }

    protected GLUtessellator tess;
    protected FloatBuffer vertices = FloatBuffer.allocate(10);
    protected IntBuffer interiorIndices = IntBuffer.allocate(10);
    protected IntBuffer boundaryIndices = IntBuffer.allocate(10);
    protected Range polygonVertexRange = new Range(0, 0);
    protected int vertexStride = 3;
    protected boolean isBoundaryEdge;
    protected double[] coords = new double[6];
    protected double[] offset = new double[3];
    protected double[] clip = {-Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE};
    protected float[] vertex = new float[3];
    protected int prevClipCode;

    public PolygonTessellator2()
    {
        this.tess = GLU.gluNewTess();
        TessCallbackAdapter callback = new TessCallbackAdapter();
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_BEGIN_DATA, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_EDGE_FLAG_DATA, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_VERTEX_DATA, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_END_DATA, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_COMBINE_DATA, callback);
    }

    public int getVertexCount()
    {
        return this.vertices.position() / this.vertexStride;
    }

    public FloatBuffer getVertices(FloatBuffer buffer)
    {
        int lim = this.vertices.limit();
        int pos = this.vertices.position();

        buffer.put((FloatBuffer) this.vertices.flip());

        this.vertices.limit(lim);
        this.vertices.position(pos);

        return buffer;
    }

    public int getInteriorIndexCount()
    {
        return this.interiorIndices.position();
    }

    public IntBuffer getInteriorIndices(IntBuffer buffer)
    {
        int lim = this.interiorIndices.limit();
        int pos = this.interiorIndices.position();

        buffer.put((IntBuffer) this.interiorIndices.flip());

        this.interiorIndices.limit(lim);
        this.interiorIndices.position(pos);

        return buffer;
    }

    public int getBoundaryIndexCount()
    {
        return this.boundaryIndices.position();
    }

    public IntBuffer getBoundaryIndices(IntBuffer buffer)
    {
        int lim = this.boundaryIndices.limit();
        int pos = this.boundaryIndices.position();

        buffer.put((IntBuffer) this.boundaryIndices.flip());

        this.boundaryIndices.limit(lim);
        this.boundaryIndices.position(pos);

        return buffer;
    }

    public Range getPolygonVertexRange()
    {
        return this.polygonVertexRange;
    }

    public void reset()
    {
        this.vertices.clear();
        this.resetIndices();
    }

    public void resetIndices()
    {
        this.interiorIndices.clear();
        this.boundaryIndices.clear();
    }

    public void setPolygonNormal(double x, double y, double z)
    {
        GLU.gluTessNormal(this.tess, x, y, z);
    }

    public void setPolygonClipCoords(double xMin, double xMax, double yMin, double yMax)
    {
        this.clip[0] = xMin;
        this.clip[1] = xMax;
        this.clip[2] = yMin;
        this.clip[3] = yMax;
    }

    public void setVertexStride(int stride)
    {
        this.vertexStride = stride;
    }

    public void setVertexOffset(double x, double y, double z)
    {
        this.offset[0] = x;
        this.offset[1] = y;
        this.offset[2] = z;
    }

    public void beginPolygon()
    {
        GLU.gluTessBeginPolygon(this.tess, this); // Use this as the polygon user data to enable callbacks.

        this.polygonVertexRange.location = this.vertices.position() / this.vertexStride;
        this.polygonVertexRange.length = 0;
    }

    public void beginContour()
    {
        GLU.gluTessBeginContour(this.tess);
        this.prevClipCode = -1;
    }

    public void addVertex(double x, double y, double z)
    {
        this.coords[0] = x;
        this.coords[1] = y;
        this.coords[2] = z;

        // TODO Modify this logic to clip edges against the clip boundary, adding new vertices as necessary
        // TODO and storing the code to indicate whether or not the vertex should be included in boundary edges.
        int code = this.clipCode(x, y, z);
        if (this.prevClipCode > 0 && code != this.prevClipCode)
        {
            int index = this.putVertex(this.coords, 3); // add the previous vertex
            GLU.gluTessVertex(this.tess, this.coords, 3, index); // associate the vertex with its index
        }

        if (code == 0 || code != this.prevClipCode)
        {
            int index = this.putVertex(this.coords, 0); // add the current vertex
            GLU.gluTessVertex(this.tess, this.coords, 0, index); // associate the vertex with its index
        }

        System.arraycopy(this.coords, 0, this.coords, 3, 3); // copy the current vertex to the previous vertex
        this.prevClipCode = code; // copy the current clip code to the previous clip code
    }

    public void endContour()
    {
        GLU.gluTessEndContour(this.tess);
    }

    public void endPolygon()
    {
        GLU.gluTessEndPolygon(this.tess);

        this.polygonVertexRange.length = this.vertices.position() / this.vertexStride;
        this.polygonVertexRange.length -= this.polygonVertexRange.location;
    }

    @SuppressWarnings("UnusedParameters")
    protected void tessBegin(int type)
    {
        // Intentionally left blank.
    }

    protected void tessEdgeFlag(boolean boundaryEdge)
    {
        this.isBoundaryEdge = boundaryEdge;
    }

    protected void tessVertex(Object vertexData)
    {
        // Accumulate interior indices appropriate for use as GL_interiorIndices primitives. Based on the GLU
        // tessellator documentation we can assume that the tessellator is providing interiorIndices because it's
        // configured with the edgeFlag callback.
        int index = (Integer) vertexData;
        this.putInteriorIndex(index);

        // Accumulate outline indices appropriate for use as GL_boundaryIndices. The tessBoundaryEdge flag indicates
        // whether or not the triangle edge starting with the current vertex is a boundary edge.
        if ((this.boundaryIndices.position() % 2) == 1)
        {
            this.putBoundaryIndex(index);
        }
        if (this.isBoundaryEdge)
        {
            this.putBoundaryIndex(index);

            int interiorCount = this.interiorIndices.position();
            if (interiorCount > 0 && (interiorCount % 3) == 0)
            {
                int firstTriIndex = this.interiorIndices.get(interiorCount - 3);
                this.putBoundaryIndex(firstTriIndex);
            }
        }
    }

    protected void tessEnd()
    {
        // Intentionally left blank.
    }

    protected void tessCombine(double[] coords, Object[] vertexData, float[] weight, Object[] outData)
    {
        outData[0] = this.putVertex(coords, 0);

        // TODO: Implement a caller-specified combine callback to enable customizing the vertex data added.
    }

    protected int putVertex(double[] coords, int pos)
    {
        if (this.vertices.remaining() < this.vertexStride)
        {
            int capacity = this.vertices.capacity() + this.vertices.capacity() / 2; // increase capacity by 50%
            FloatBuffer buffer = FloatBuffer.allocate(capacity);
            buffer.put((FloatBuffer) this.vertices.flip());
            this.vertices = buffer;
        }

        int index = this.vertices.position() / this.vertexStride;

        this.vertex[0] = (float) (coords[0 + pos] + this.offset[0]);
        this.vertex[1] = (float) (coords[1 + pos] + this.offset[1]);
        this.vertex[2] = (float) (coords[2 + pos] + this.offset[2]);
        this.vertices.put(this.vertex, 0, this.vertexStride);

        return index;
    }

    protected void putInteriorIndex(int i)
    {
        if (!this.interiorIndices.hasRemaining())
        {
            int capacity = this.interiorIndices.capacity()
                + this.interiorIndices.capacity() / 2; // increase capacity by 50%
            IntBuffer buffer = IntBuffer.allocate(capacity);
            buffer.put((IntBuffer) this.interiorIndices.flip());
            this.interiorIndices = buffer;
        }

        this.interiorIndices.put(i);
    }

    protected void putBoundaryIndex(int i)
    {
        if (!this.boundaryIndices.hasRemaining())
        {
            int capacity = this.boundaryIndices.capacity()
                + this.boundaryIndices.capacity() / 2; // increase capacity by 50%
            IntBuffer buffer = IntBuffer.allocate(capacity);
            buffer.put((IntBuffer) this.boundaryIndices.flip());
            this.boundaryIndices = buffer;
        }

        this.boundaryIndices.put(i);
    }

    /**
     * Computes a 4-bit code indicating the vertex's location in the 9 cell grid defined by the clip bounds and the
     * eight adjacent spaces defined by extending the min/max boundaries to infinity. 0 indicates that the vertex is
     * inside the clip bounds.
     */
    protected int clipCode(double x, double y, double z)
    {
        // TODO: Add support for clipping z coordiantes.
        int code = 0;
        code |= (x < this.clip[0] ? 0x0001 : 0x0); // xMin
        code |= (x > this.clip[1] ? 0x0010 : 0x0); // xMax
        code |= (y < this.clip[2] ? 0x0100 : 0x0); // yMin
        code |= (y > this.clip[3] ? 0x1000 : 0x0); // yMax

        return code;
    }
}

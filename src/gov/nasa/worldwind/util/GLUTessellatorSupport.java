/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import java.nio.IntBuffer;
import java.util.*;

/**
 * GLUTessellatorSupport is a utility class for configuring and using a {@link javax.media.opengl.glu.GLUtessellator} to
 * tessellate complex polygons into triangles.
 * <p/>
 * The standard pattern for using GLUTessellatorSupport to prepare a GLUtessellator is as follows: <code>
 * GLUTessellatorSupport glts = new GLUTessellatorSupport();<br/> GLUtessellatorCallback cb = ...; // Reference to an
 * implementation of GLUtessellatorCallback.<br/> Vec4 normal = new Vec4(0, 0, 1); // The polygon's normal. This example
 * shows an appropriate normal for tessellating x-y coordinates.<br/> <br/><br/> glts.beginTessellation(cb, new Vec4(0,
 * 0, 1));<br/> try<br/> {<br/> GLUtessellator tess = glts.getGLUtessellator();<br/> }<br/> finally<br/> {<br/>
 * glts.endTessellation();<br/> }<br/> </code>
 *
 * @author dcollins
 * @version $Id: GLUTessellatorSupport.java 3427 2015-09-30 23:24:13Z dcollins $
 */
public class GLUTessellatorSupport
{
    protected GLUtessellator tess;

    /** Creates a new GLUTessellatorSupport, but otherwise does nothing. */
    public GLUTessellatorSupport()
    {
    }

    /**
     * Returns this GLUTessellatorSupport's internal {@link javax.media.opengl.glu.GLUtessellator} instance. This
     * returns a valid GLUtessellator instance if called between {@link #beginTessellation(javax.media.opengl.glu.GLUtessellatorCallback,
     * gov.nasa.worldwind.geom.Vec4)} and {@link #endTessellation()}. This returns null if called from outside a
     * beginTessellation/endTessellation block.
     *
     * @return the internal GLUtessellator instance, or null if called from outside a beginTessellation/endTessellation
     * block.
     */
    public GLUtessellator getGLUtessellator()
    {
        return this.tess;
    }

    /**
     * Prepares this GLUTessellatorSupport's internal GLU tessellator for use. This initializes the internal
     * GLUtessellator to a new instance by invoking {@link javax.media.opengl.glu.GLU#gluNewTess()}, and configures the
     * tessellator with the specified callback and normal with calls to {@link javax.media.opengl.glu.GLU#gluTessCallback(javax.media.opengl.glu.GLUtessellator,
     * int, javax.media.opengl.glu.GLUtessellatorCallback)} and {@link javax.media.opengl.glu.GLU#gluTessNormal(javax.media.opengl.glu.GLUtessellator,
     * double, double, double)}, respectively.
     *
     * @param callback the callback to configure the GLU tessellator with.
     * @param normal   the normal to configure the GLU tessellator with.
     *
     * @throws IllegalArgumentException if the callback or the normal is null.
     */
    public void beginTessellation(GLUtessellatorCallback callback, Vec4 normal)
    {
        if (callback == null)
        {
            String message = Logging.getMessage("nullValue.CallbackIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (normal == null)
        {
            String message = Logging.getMessage("nullValue.NormalIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.tess = GLU.gluNewTess();
        GLU.gluTessNormal(this.tess, normal.x, normal.y, normal.z);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_BEGIN, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_VERTEX, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_END, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_COMBINE, callback);
    }

    /**
     * Frees any GLU resources used by this GLUTessellatorSupport, and invalidates this instance's internal GLU
     * tessellator.
     */
    public void endTessellation()
    {
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_BEGIN, null);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_VERTEX, null);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_END, null);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_COMBINE, null);
        this.tess = null;
    }

    /**
     * Creates a new {@link javax.media.opengl.glu.GLUtessellatorCallback} that draws tessellated polygons as OpenGL
     * primitives by calling glBegin, glEnd, and glVertex.
     *
     * @param gl the GL context to draw into.
     *
     * @return a new GLUtessellatorCallback for drawing tessellated polygons as OpenGL primtives.
     *
     * @throws IllegalArgumentException if the GL is null.
     */
    public static GLUtessellatorCallback createOGLDrawPrimitivesCallback(GL2 gl)
    {
        if (gl == null)
        {
            String message = Logging.getMessage("nullValue.GLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new OGLDrawPrimitivesCallback(gl);
    }

    /**
     * Converts the specified GLU tessellator error number to a string description. This returns "unknown" if the error
     * number is not recognized.
     *
     * @param errno a GLU enumeration indicating the error.
     *
     * @return a string description of the error number.
     */
    public static String convertGLUTessErrorToString(int errno)
    {
        switch (errno)
        {
            case GLU.GLU_TESS_MISSING_BEGIN_POLYGON:
                return "missing begin polygon";
            case GLU.GLU_TESS_MISSING_END_POLYGON:
                return "missing end polygon";
            case GLU.GLU_TESS_MISSING_BEGIN_CONTOUR:
                return "missing begin contour";
            case GLU.GLU_TESS_MISSING_END_CONTOUR:
                return "missing end contour";
            case GLU.GLU_TESS_COORD_TOO_LARGE:
                return "coordinate too large";
            case GLU.GLU_TESS_NEED_COMBINE_CALLBACK:
                return "need combine callback";
            default:
                return "unknown";
        }
    }

    protected static class OGLDrawPrimitivesCallback extends GLUtessellatorCallbackAdapter
    {
        protected final GL2 gl;

        public OGLDrawPrimitivesCallback(GL2 gl)
        {
            if (gl == null)
            {
                String message = Logging.getMessage("nullValue.GLIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.gl = gl;
        }

        public void begin(int type)
        {
            this.gl.glBegin(type);
        }

        public void vertex(Object vertexData)
        {
            double[] coords = (double[]) vertexData;
            this.gl.glVertex3f((float) coords[0], (float) coords[1], (float) coords[2]);
        }

        public void end()
        {
            this.gl.glEnd();
        }

        public void combine(double[] coords, Object[] data, float[] weight, Object[] outData)
        {
            outData[0] = coords;
        }
    }

    /** Provides the callback class used to capture the shapes determined by the tessellator. */
    public static class CollectIndexListsCallback extends GLUtessellatorCallbackAdapter
    {
        protected int numIndices;
        protected int currentType;
        protected List<Integer> currentPrim;
        protected List<List<Integer>> prims = new ArrayList<List<Integer>>();
        protected List<Integer> primTypes = new ArrayList<Integer>();

        public List<List<Integer>> getPrims()
        {
            return prims;
        }

        public List<Integer> getPrimTypes()
        {
            return primTypes;
        }

        public int getNumIndices()
        {
            return this.numIndices;
        }

        public void begin(int type)
        {
            this.currentType = type;
            this.currentPrim = new ArrayList<Integer>();
        }

        public void vertex(Object vertexData)
        {
            this.currentPrim.add((Integer) vertexData);
            ++this.numIndices;
        }

        @Override
        public void end()
        {
            this.primTypes.add(this.currentType);
            this.prims.add(this.currentPrim);

            this.currentPrim = null;
        }

        public void combine(double[] coords, Object[] data, float[] weight, Object[] outData)
        {
//            System.out.println("COMBINE CALLED");
            outData[0] = data[0];
        }
    }

    /** Provides a container for associating a tessellator's vertex with its index and application-specified edge flag. */
    public static class VertexData
    {
        public final int index;
        public final boolean edgeFlag;

        public VertexData(int index, boolean edgeFlag)
        {
            this.index = index;
            this.edgeFlag = edgeFlag;
        }
    }

    /** Provides the callback class used to capture triangle and line primitive indices determined by the tessellator. */
    public static class CollectPrimitivesCallback extends GLUtessellatorCallbackAdapter
    {
        protected List<Integer> triangles = new ArrayList<Integer>();
        protected List<Integer> lines = new ArrayList<Integer>();
        protected IntBuffer triangleBuffer = IntBuffer.allocate(0);
        protected IntBuffer lineBuffer = IntBuffer.allocate(0);
        protected int error = 0;
        protected int index = 0;
        protected VertexData[] vertices = {null, null, null};
        protected boolean[] edgeFlags = {true, true, true};
        protected boolean edgeFlag = true;

        public CollectPrimitivesCallback()
        {
        }

        public IntBuffer getTriangleIndices()
        {
            return (IntBuffer) this.triangleBuffer.flip();
        }

        public IntBuffer getLineIndices()
        {
            return (IntBuffer) this.lineBuffer.flip();
        }

        public int getError()
        {
            return this.error;
        }

        public void attach(GLUtessellator tessellator)
        {
            GLU.gluTessCallback(tessellator, GLU.GLU_TESS_BEGIN, this);
            GLU.gluTessCallback(tessellator, GLU.GLU_TESS_END, this);
            GLU.gluTessCallback(tessellator, GLU.GLU_TESS_VERTEX, this);
            GLU.gluTessCallback(tessellator, GLU.GLU_TESS_EDGE_FLAG, this);
            GLU.gluTessCallback(tessellator, GLU.GLU_TESS_ERROR, this);
        }

        public void reset()
        {
            this.triangles.clear();
            this.lines.clear();
            this.triangleBuffer.clear();
            this.lineBuffer.clear();
            this.error = 0;
            this.index = 0;
            this.edgeFlag = true;
        }

        @Override
        public void begin(int type)
        {
            if (type != GL.GL_TRIANGLES)
            {
                String msg = Logging.getMessage("generic.UnexpectedPrimitiveType", type);
                Logging.logger().warning(msg);
            }
        }

        @Override
        public void end()
        {
            this.triangleBuffer = IntBuffer.allocate(this.triangles.size());
            for (Integer index : this.triangles)
            {
                this.triangleBuffer.put(index);
            }

            this.lineBuffer = IntBuffer.allocate(this.lines.size());
            for (Integer index : this.lines)
            {
                this.lineBuffer.put(index);
            }
        }

        @Override
        public void vertex(Object vertexData)
        {
            this.vertices[this.index] = (VertexData) vertexData;
            this.edgeFlags[this.index] = this.edgeFlag;
            this.index++;

            if (this.index == 3)
            {
                VertexData i = this.vertices[0];
                VertexData j = this.vertices[1];
                VertexData k = this.vertices[2];
                this.triangles.add(i.index);
                this.triangles.add(j.index);
                this.triangles.add(k.index);

                if (this.edgeFlags[0] && (i.edgeFlag || j.edgeFlag))
                {
                    this.lines.add(i.index);
                    this.lines.add(j.index);
                }

                if (this.edgeFlags[1] && (j.edgeFlag || k.edgeFlag))
                {
                    this.lines.add(j.index);
                    this.lines.add(k.index);
                }

                if (this.edgeFlags[2] && (k.edgeFlag || i.edgeFlag))
                {
                    this.lines.add(k.index);
                    this.lines.add(i.index);
                }

                this.index = 0;
            }
        }

        @Override
        public void edgeFlag(boolean flag)
        {
            this.edgeFlag = flag;
        }

        @Override
        public void error(int errno)
        {
            this.error = errno;
        }
    }

    /**
     * Recursively forwards boundary tessellation results from one GLU tessellator to another. The GLU tessellator this
     * callback forwards to may be configured in any way the caller chooses.
     * <p/>
     * RecursiveCallback must be used as the GLUtessellatorCallback for the begin, end, vertex, and combine callbacks
     * for a GLU tessellator configured to generate line loops. A GLU tessellator can be configured generate line loops
     * by calling gluTessProperty(GLU_TESS_BOUNDARY_ONLY, GL_TRUE). Additionally, the caller specified vertex data
     * passed to gluTessVertex must be a double array containing three elements - the x, y and z coordinates associated
     * with the vertex.
     */
    public static class RecursiveCallback extends GLUtessellatorCallbackAdapter
    {
        /**
         * The GLU tessellator that receives the tessellation results sent to this callback.
         */
        protected GLUtessellator tess;

        /**
         * Creates a new RecursiveCallback with the GLU tessellator that receives boundary tessellation results.
         *
         * @param tessellator the GLU tessellator that receives the tessellation results sent to this callback. This
         *                    tessellator may be configured in any way the caller chooses, but should be prepared to
         *                    receive contour input from this callback.
         *
         * @throws java.lang.IllegalArgumentException if the tessellator is null.
         */
        public RecursiveCallback(GLUtessellator tessellator)
        {
            if (tessellator == null)
            {
                String msg = Logging.getMessage("nullValue.TessellatorIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            this.tess = tessellator;
        }

        /**
         * Called by the GLU tessellator to indicate the beginning of a new line loop. This recursively begins a new
         * contour with the GLU tessellator specified during construction by calling gluTessBeginContour(tessellator).
         *
         * @param type the GL primitive type. Must be GL_LINE_LOOP.
         */
        @Override
        public void begin(int type)
        {
            GLU.gluTessBeginContour(this.tess);
        }

        /**
         * Called by the GLU tessellator to indicate the next vertex of the current contour. The vertex data must be a
         * double array containing three elements - the x, y and z coordinates associated with the vertex. This
         * recursively indicates the next contour vertex with the GLU tessellator specified during construction by
         * calling gluTessVertex(tessellator, (double[]) vertexData, 0, vertexData).
         *
         * @param vertexData the caller specified vertex data. Must be a double array containing three elements - the x,
         *                   y and z coordinates associated with the vertex.
         */
        @Override
        public void vertex(Object vertexData)
        {
            GLU.gluTessVertex(this.tess, (double[]) vertexData, 0, vertexData);
        }

        /**
         * Called by the GLU tessellator to indicate the end of the current line loop. This recursively ends the current
         * contour with the GLU tessellator specified during construction by calling gluTessEndContour(tessellator).
         */
        @Override
        public void end()
        {
            GLU.gluTessEndContour(this.tess);
        }

        /**
         * Called by the GLU tessellator to indicate that up to four vertices must be merged into a new vertex. The new
         * vertex is a linear combination of the original vertices. This assigns the first element of outData to coords,
         * the coordinates of the new vertex.
         *
         * @param coords     A three element array containing the x, y and z coordinates of the new vertex.
         * @param vertexData The caller specified vertex data of the original vertices.
         * @param weight     The coefficients of the linear combination. These weights sum to 1.
         * @param outData    A one element array that must contain the caller specified data associated with the new
         *                   vertex after this method returns.
         */
        @Override
        public void combine(double[] coords, Object[] vertexData, float[] weight, Object[] outData)
        {
            outData[0] = coords;
        }

        /**
         * Called by the GLU tessellator when the tessellation algorithm encounters an error. This logs a severe message
         * describing the error.
         *
         * @param errno a GLU enumeration indicating the error.
         */
        @Override
        public void error(int errno)
        {
            String errstr = convertGLUTessErrorToString(errno);
            String msg = Logging.getMessage("generic.ExceptionWhileTessellating", errstr);
            Logging.logger().severe(msg);
        }
    }
}

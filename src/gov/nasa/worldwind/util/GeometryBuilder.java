/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.terrain.Terrain;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.glu.*;
import java.nio.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: GeometryBuilder.java 3434 2015-10-08 18:17:48Z tgaskins $
 */
public class GeometryBuilder
{
    public static final int OUTSIDE = 0;
    public static final int INSIDE = 1;

    public static final int COUNTER_CLOCKWISE = 0;
    public static final int CLOCKWISE = 1;

    public static final int TOP = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;

    /**
     * Bit code indicating that the leader's location is inside the rectangle. Used by <code>{@link
     * #computeLeaderLocationCode(float, float, float, float, float, float)}</code>.
     */
    protected static final int LEADER_LOCATION_INSIDE = 0;
    /**
     * Bit code indicating that the leader's location is above the rectangle. Used by <code>{@link
     * #computeLeaderLocationCode(float, float, float, float, float, float)}</code>.
     */
    protected static final int LEADER_LOCATION_TOP = 1;
    /**
     * Bit code indicating that the leader's location is below the rectangle. Used by <code>{@link
     * #computeLeaderLocationCode(float, float, float, float, float, float)}</code>.
     */
    protected static final int LEADER_LOCATION_BOTTOM = 2;
    /**
     * Bit code indicating that the leader's location is to the right of the rectangle. Used by <code>{@link
     * #computeLeaderLocationCode(float, float, float, float, float, float)}</code>.
     */
    protected static final int LEADER_LOCATION_RIGHT = 4;
    /**
     * Bit code indicating that the leader's location is to the left of the rectangle. Used by <code>{@link
     * #computeLeaderLocationCode(float, float, float, float, float, float)}</code>.
     */
    protected static final int LEADER_LOCATION_LEFT = 8;

    private static final float[] coord = new float[3];

    private int orientation = OUTSIDE;

    public GeometryBuilder()
    {
    }

    public int getOrientation()
    {
        return this.orientation;
    }

    public void setOrientation(int orientation)
    {
        this.orientation = orientation;
    }

    //**************************************************************//
    //********************  Sphere  ********************************//
    //**************************************************************//

    public IndexedTriangleArray tessellateSphere(float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int[] indexArray = new int[ICOSAHEDRON_INDEX_COUNT];
        float[] vertexArray = new float[3 * ICOSAHEDRON_VERTEX_COUNT];
        System.arraycopy(icosahedronIndexArray, 0, indexArray, 0, ICOSAHEDRON_INDEX_COUNT);
        System.arraycopy(icosahedronVertexArray, 0, vertexArray, 0, 3 * ICOSAHEDRON_VERTEX_COUNT);

        // The static icosahedron tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (int index = 0; index < ICOSAHEDRON_INDEX_COUNT; index += 3)
            {
                int tmp = indexArray[index];
                indexArray[index] = indexArray[index + 2];
                indexArray[index + 2] = tmp;
            }
        }

        // Start with a triangular tessellated icosahedron.
        IndexedTriangleArray ita = new IndexedTriangleArray(
            ICOSAHEDRON_INDEX_COUNT, indexArray, ICOSAHEDRON_VERTEX_COUNT, vertexArray);

        // Subdivide the icosahedron a specified number of times. The subdivison step computes midpoints between
        // adjacent vertices. These midpoints are not on the sphere, but must be moved onto the sphere. We normalize
        // each midpoint vertex to acheive this.
        for (int i = 0; i < subdivisions; i++)
        {
            this.subdivideIndexedTriangleArray(ita);

            vertexArray = ita.getVertices();
            for (int vertex = 0; vertex < ita.vertexCount; vertex++)
            {
                norm3AndSet(vertexArray, 3 * vertex);
            }
        }

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexArray = ita.getVertices();
            for (int vertex = 0; vertex < ita.vertexCount; vertex++)
            {
                mul3AndSet(vertexArray, 3 * vertex, radius);
            }
        }

        return ita;
    }

    public IndexedTriangleBuffer tessellateSphereBuffer(float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(ICOSAHEDRON_INDEX_COUNT);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * ICOSAHEDRON_VERTEX_COUNT);
        indexBuffer.put(icosahedronIndexArray, 0, ICOSAHEDRON_INDEX_COUNT);
        vertexBuffer.put(icosahedronVertexArray, 0, 3 * ICOSAHEDRON_VERTEX_COUNT);

        // The static icosahedron tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (int index = 0; index < ICOSAHEDRON_INDEX_COUNT; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a triangular tessellated icosahedron.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            ICOSAHEDRON_INDEX_COUNT, indexBuffer, ICOSAHEDRON_VERTEX_COUNT, vertexBuffer);

        // Subdivide the icosahedron a specified number of times. The subdivison step computes midpoints between
        // adjacent vertices. These midpoints are not on the sphere, but must be moved onto the sphere. We normalize
        // each midpoint vertex to achieve this.
        for (int i = 0; i < subdivisions; i++)
        {
            this.subdivideIndexedTriangleBuffer(itb);

            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.getVertexCount(); vertex++)
            {
                norm3AndSet(vertexBuffer, 3 * vertex);
            }
        }

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    public IndexedTriangleBuffer tessellateEllipsoidBuffer(float a, float b, float c, int subdivisions)
    {
        IndexedTriangleBuffer itb = tessellateSphereBuffer(a, subdivisions);

        // normalize 2nd and 3rd radii in terms of the first one
        float bScale = b / a;
        float cScale = c / a;

        // scale Y and Z components of each vertex by appropriate scaling factor
        FloatBuffer vertexBuffer = itb.getVertices();
        for (int vertex = 0; vertex < itb.getVertexCount(); vertex++)
        {
            // offset = 0 for x coord, 1 for y coord, etc.
            mulAndSet(vertexBuffer, 3 * vertex, bScale, 2);
            mulAndSet(vertexBuffer, 3 * vertex, cScale, 1);
        }

        itb.vertices.rewind();

        return itb;
    }

    // Icosahedron tessellation taken from the
    // OpenGL Programming Guide, Chapter 2, Example 2-13: Drawing an Icosahedron.

    private static final int ICOSAHEDRON_INDEX_COUNT = 60;
    private static final int ICOSAHEDRON_VERTEX_COUNT = 12;
    private static final float X = 0.525731112119133606f;
    private static final float Z = 0.850650808352039932f;

    private static float[] icosahedronVertexArray =
        {
            -X, 0, Z,
            X, 0, Z,
            -X, 0, -Z,
            X, 0, -Z,
            0, Z, X,
            0, Z, -X,
            0, -Z, X,
            0, -Z, -X,
            Z, X, 0,
            -Z, X, 0,
            Z, -X, 0,
            -Z, -X, 0
        };

    private static int[] icosahedronIndexArray =
        {
            1, 4, 0,
            4, 9, 0,
            4, 5, 9,
            8, 5, 4,
            1, 8, 4,
            1, 10, 8,
            10, 3, 8,
            8, 3, 5,
            3, 2, 5,
            3, 7, 2,
            3, 10, 7,
            10, 6, 7,
            6, 11, 7,
            6, 0, 11,
            6, 1, 0,
            10, 1, 6,
            11, 0, 9,
            2, 11, 9,
            5, 2, 9,
            11, 2, 7
        };

    //**************************************************************//
    //***********************  Box  ********************************//
    //**************************************************************//

    // create the entire box
    public IndexedTriangleBuffer tessellateBoxBuffer(float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(BOX_INDEX_COUNT);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * BOX_VERTEX_COUNT);
        indexBuffer.put(boxIndexArray, 0, BOX_INDEX_COUNT);
        vertexBuffer.put(boxVertexArray, 0, 3 * BOX_VERTEX_COUNT);

        // The static box tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (int index = 0; index < BOX_INDEX_COUNT; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a tessellated box.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            BOX_INDEX_COUNT, indexBuffer, BOX_VERTEX_COUNT, vertexBuffer);

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    // create only one face of the box
    public IndexedTriangleBuffer tessellateBoxBuffer(int face, float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (face < 0 || face >= 6)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "face < 0 or face >= 6");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(BOX_INDEX_COUNT / 6);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * BOX_VERTEX_COUNT / 6);

        // fill subset of index buffer
        int[] subArray = new int[BOX_INDEX_COUNT / 6];
        for (int i = 0; i < BOX_INDEX_COUNT / 6; i++)
        {
            subArray[i] = boxFacesIndexArray[face * BOX_INDEX_COUNT / 6 + i];
        }
        indexBuffer.put(subArray, 0, BOX_INDEX_COUNT / 6);

        float[] vertexSubset = new float[3 * BOX_VERTEX_COUNT / 6];
        for (int i = 0; i < 3 * BOX_VERTEX_COUNT / 6; i++)
        {
            vertexSubset[i] = boxVertexArray[face * 3 * BOX_VERTEX_COUNT / 6 + i];
        }
        vertexBuffer.put(vertexSubset, 0, 3 * BOX_VERTEX_COUNT / 6);

        // The static box tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (int index = 0; index < BOX_INDEX_COUNT / 6; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a tessellated box.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            BOX_INDEX_COUNT / 6, indexBuffer, BOX_VERTEX_COUNT / 6, vertexBuffer);

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    private static final int BOX_INDEX_COUNT = 36;
    private static final int BOX_VERTEX_COUNT = 24;
    private static final float B = 1.0f;

    private static float[] boxVertexArray =
        {    // right
            B, -B, B,          // 0
            B, B, B,           // 1
            B, -B, -B,         // 2
            B, B, -B,          // 3

            // front
            -B, B, B,          // 4
            B, B, B,           // 5
            -B, -B, B,         // 6
            B, -B, B,          // 7

            // left
            -B, B, B,          // 8
            -B, -B, B,         // 9
            -B, B, -B,         // 10
            -B, -B, -B,        // 11

            // back
            B, B, -B,          // 12
            -B, B, -B,         // 13
            B, -B, -B,         // 14
            -B, -B, -B,        // 15

            // top
            B, B, B,           // 16
            -B, B, B,          // 17
            B, B, -B,          // 18
            -B, B, -B,         // 19

            // bottom
            -B, -B, B,          // 20
            B, -B, B,           // 21
            -B, -B, -B,         // 22
            B, -B, -B           // 23
        };

    private static int[] boxIndexArray =
        {
            2, 3, 1,             // right
            2, 1, 0,
            4, 6, 7,             // front
            4, 7, 5,
            8, 10, 11,           // left
            8, 11, 9,
            12, 14, 15,          // back
            12, 15, 13,
            16, 18, 19,          // top
            16, 19, 17,
            20, 22, 23,          // bottom
            20, 23, 21,
        };

    private static int[] boxFacesIndexArray =
        {
            2, 3, 1,            // right
            2, 1, 0,
            0, 2, 3,            // front
            0, 3, 1,
            0, 2, 3,            // left
            0, 3, 1,
            0, 2, 3,            // back
            0, 3, 1,
            0, 2, 3,            // top
            0, 3, 1,
            0, 2, 3,            // bottom
            0, 3, 1,
        };

    //**************************************************************//
    //***********************  Pyramid  ****************************//
    //**************************************************************//

    public IndexedTriangleBuffer tessellatePyramidBuffer(float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(PYRAMID_INDEX_COUNT);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * PYRAMID_VERTEX_COUNT);
        indexBuffer.put(pyramidIndexArray, 0, PYRAMID_INDEX_COUNT);
        vertexBuffer.put(pyramidVertexArray, 0, 3 * PYRAMID_VERTEX_COUNT);

        // The static box tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (int index = 0; index < PYRAMID_INDEX_COUNT; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a tessellated pyramid.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            PYRAMID_INDEX_COUNT, indexBuffer, PYRAMID_VERTEX_COUNT, vertexBuffer);

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    public IndexedTriangleBuffer tessellatePyramidBuffer(int face, float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // default values for pyramid side
        int faceIndexCount = 3;
        int faceVertexCount = 3;
        int faceIndicesOffset = face * faceIndexCount;
        int faceVerticesOffset = face * 3 * faceVertexCount;

        if (face == 4)   // the pyramid base
        {
            faceIndicesOffset = 4 * faceIndexCount;
            faceVerticesOffset = 4 * 3 * faceVertexCount;
            faceIndexCount = 6;
            faceVertexCount = 4;
        }

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(faceIndexCount);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * faceVertexCount);

        // fill subset of index buffer
        int[] subArray = new int[faceIndexCount];
        for (int i = 0; i < faceIndexCount; i++)
        {
            subArray[i] = pyramidFacesIndexArray[faceIndicesOffset + i];
        }
        indexBuffer.put(subArray, 0, faceIndexCount);

        float[] vertexSubset = new float[3 * faceVertexCount];
        for (int i = 0; i < 3 * faceVertexCount; i++)
        {
            vertexSubset[i] = pyramidVertexArray[faceVerticesOffset + i];
        }
        vertexBuffer.put(vertexSubset, 0, 3 * faceVertexCount);

        // The static box tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (int index = 0; index < faceIndexCount; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a tessellated pyramid.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            faceIndexCount, indexBuffer, faceVertexCount, vertexBuffer);

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    private static final int PYRAMID_INDEX_COUNT = 18;
    private static final int PYRAMID_VERTEX_COUNT = 16;
    private static final float P = 1.0f;

    private static float[] pyramidVertexArray =
        {    // right
            0, 0, P,           // 0   (point)
            P, -P, -P,         // 1
            P, P, -P,          // 2

            // front
            0, 0, P,           // 3   (point)
            -P, -P, -P,         // 4
            P, -P, -P,          // 5

            // left
            0, 0, P,           // 6   (point)
            -P, P, -P,         // 7
            -P, -P, -P,        // 8

            // back
            0, 0, P,           // 9   (point)
            P, P, -P,         // 10
            -P, P, -P,        // 11

            // bottom (base) face
            P, P, -P,          // 12
            -P, P, -P,         // 13
            P, -P, -P,         // 14
            -P, -P, -P         // 15
        };

    private static int[] pyramidIndexArray =
        {
            0, 1, 2,              // right
            3, 4, 5,              // front
            6, 7, 8,              // left
            9, 10, 11,            // back
            12, 14, 15,           // base
            12, 15, 13,
        };

    private static int[] pyramidFacesIndexArray =
        {
            0, 1, 2,              // right
            0, 1, 2,              // front
            0, 1, 2,              // left
            0, 1, 2,              // back
            0, 2, 3,              // base
            0, 3, 1,
        };

    //**************************************************************//
    //********************      Unit Cylinder    *******************//
    //**************************************************************//

    public IndexedTriangleBuffer tessellateCylinderBuffer(float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, index;
        float x, y, z, a;

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = 2.0f * (float) Math.PI / (float) slices;

        int cylinderIndexCount = 12 * slices;
        int cylinderVertexCount = 4 * slices + 4;

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(cylinderIndexCount);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * cylinderVertexCount);

        // VERTICES

        // top and bottom center points
        vertexBuffer.put(0, 0f);
        vertexBuffer.put(1, 0f);
        vertexBuffer.put(2, 1.0f);

        vertexBuffer.put(3 * (slices + 1), 0f);
        vertexBuffer.put(3 * (slices + 1) + 1, 0f);
        vertexBuffer.put(3 * (slices + 1) + 2, -1.0f);

        // rim points
        for (i = 0; i < slices; i++)
        {
            a = i * da;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            z = 1.0f;

            index = 3 * i + 3;

            // cylinder top
            vertexBuffer.put(index, x * radius);
            vertexBuffer.put(index + 1, y * radius);
            vertexBuffer.put(index + 2, z);

            index += 3;     // add 3 for the second center point

            // cylinder bottom
            vertexBuffer.put(index + 3 * slices, x * radius);
            vertexBuffer.put(index + 3 * slices + 1, y * radius);
            vertexBuffer.put(index + 3 * slices + 2, -z);

            index += 6 * slices + 3 * i;

            // core upper rim
            vertexBuffer.put(index, x * radius);
            vertexBuffer.put(index + 1, y * radius);
            vertexBuffer.put(index + 2, z);

            // core lower rim
            vertexBuffer.put(index + 3, x * radius);
            vertexBuffer.put(index + 4, y * radius);
            vertexBuffer.put(index + 5, -z);
        }

        // extra vertices for seamless texture mapping

        int wrapIndex = 3 * (4 * slices + 2);
        x = (float) Math.sin(0);
        y = (float) Math.cos(0);
        z = 1.0f;

        vertexBuffer.put(wrapIndex, x * radius);
        vertexBuffer.put(wrapIndex + 1, y * radius);
        vertexBuffer.put(wrapIndex + 2, z);

        vertexBuffer.put(wrapIndex + 3, x * radius);
        vertexBuffer.put(wrapIndex + 4, y * radius);
        vertexBuffer.put(wrapIndex + 5, -z);

        // INDICES

        int coreIndex = (2 * slices) + 2;
        int centerPoint = 0;

        for (i = 0; i < slices; i++)
        {
            // cylinder top
            index = 3 * i;

            indexBuffer.put(index, 0);                  // center point
            indexBuffer.put(index + 1, (i < slices - 1) ? i + 2 : 1);
            indexBuffer.put(index + 2, i + 1);

            // cylinder bottom
            index = 3 * (slices + i);

            indexBuffer.put(index, (slices + 1));             // center point
            indexBuffer.put(index + 1, (i < slices - 1) ? (slices + 1) + i + 2 : (slices + 1) + 1);
            indexBuffer.put(index + 2, (slices + 1) + i + 1);

            // cylinder core
            index = 6 * (slices + i);

            indexBuffer.put(index, coreIndex);
            indexBuffer.put(index + 1, coreIndex + 1);
            indexBuffer.put(index + 2, coreIndex + 2);

            indexBuffer.put(index + 3, coreIndex + 2);
            indexBuffer.put(index + 4, coreIndex + 1);
            indexBuffer.put(index + 5, coreIndex + 3);

            coreIndex += 2;
        }

        // The static cylinder tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (index = 0; index < cylinderIndexCount; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a triangular tessellated cylinder.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            cylinderIndexCount, indexBuffer, cylinderVertexCount, vertexBuffer);

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    public IndexedTriangleBuffer tessellateCylinderBuffer(int face, float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, index;
        float x, y, z, a;

        // face 0 = top
        // face 1 = bottom
        // face 2 = round cylinder core

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = 2.0f * (float) Math.PI / (float) slices;

        int cylinderIndexCount = 3 * slices;
        int cylinderVertexCount = slices + 1;

        if (face == 2)      // cylinder core
        {
            cylinderIndexCount = 6 * slices;
            cylinderVertexCount = 2 * slices + 2;
        }

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(cylinderIndexCount);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * cylinderVertexCount);

        // VERTICES

        if (face == 0 || face == 1)     // top or bottom cylinder face
        {
            int isTop = 1;
            if (face == 1)
                isTop = -1;

            // top center point
            vertexBuffer.put(0, 0f);
            vertexBuffer.put(1, 0f);
            vertexBuffer.put(2, isTop * 1.0f);

            // rim points
            for (i = 0; i < slices; i++)
            {
                a = i * da;
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                z = 1.0f;

                index = 3 * i + 3;

                // cylinder top
                vertexBuffer.put(index, x * radius);
                vertexBuffer.put(index + 1, y * radius);
                vertexBuffer.put(index + 2, isTop * z);
            }
        }
        else if (face == 2)     // cylinder core
        {
            // rim points
            for (i = 0; i < slices; i++)
            {
                a = i * da;
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                z = 1.0f;

                index = 6 * i;

                // core upper rim
                vertexBuffer.put(index, x * radius);
                vertexBuffer.put(index + 1, y * radius);
                vertexBuffer.put(index + 2, z);

                // core lower rim
                vertexBuffer.put(index + 3, x * radius);
                vertexBuffer.put(index + 4, y * radius);
                vertexBuffer.put(index + 5, -z);
            }

            // extra vertices for seamless texture mapping

            int wrapIndex = 3 * (2 * slices);
            x = (float) Math.sin(0);
            y = (float) Math.cos(0);
            z = 1.0f;

            vertexBuffer.put(wrapIndex, x * radius);
            vertexBuffer.put(wrapIndex + 1, y * radius);
            vertexBuffer.put(wrapIndex + 2, z);

            vertexBuffer.put(wrapIndex + 3, x * radius);
            vertexBuffer.put(wrapIndex + 4, y * radius);
            vertexBuffer.put(wrapIndex + 5, -z);
        }

        // INDICES

        int centerPoint = 0;

        if (face == 0 || face == 1)      // top or bottom cylinder face
        {
            for (i = 0; i < slices; i++)
            {
                index = 3 * i;

                indexBuffer.put(index, 0);                  // center point
                indexBuffer.put(index + 1, (i < slices - 1) ? i + 2 : 1);
                indexBuffer.put(index + 2, i + 1);
            }
        }
        else if (face == 2)             // cylinder core
        {
            int coreIndex = 0;

            for (i = 0; i < slices; i++)
            {
                index = 6 * i;

                indexBuffer.put(index, coreIndex);
                indexBuffer.put(index + 1, coreIndex + 1);
                indexBuffer.put(index + 2, coreIndex + 2);

                indexBuffer.put(index + 3, coreIndex + 2);
                indexBuffer.put(index + 4, coreIndex + 1);
                indexBuffer.put(index + 5, coreIndex + 3);

                coreIndex += 2;
            }
        }

        // The static cylinder tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (index = 0; index < cylinderIndexCount; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a triangular tessellated cylinder.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            cylinderIndexCount, indexBuffer, cylinderVertexCount, vertexBuffer);

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    //**************************************************************//
    //********************          Wedge        *******************//
    //**************************************************************//

    public IndexedTriangleBuffer tessellateWedgeBuffer(float radius, int subdivisions, Angle angle)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (angle.getRadians() < 0 || angle.getRadians() > 2 * Math.PI)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "angle < 0 or angle > 2 PI");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, index;
        float x, y, z, a;

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = (float) angle.getRadians() / slices;

        int wedgeIndexCount = 12 * slices + 12;
        int wedgeVertexCount = 4 * (slices + 1) + 2 + 8;

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(wedgeIndexCount);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * wedgeVertexCount);

        // VERTICES

        // top and bottom center points
        vertexBuffer.put(0, 0f);
        vertexBuffer.put(1, 0f);
        vertexBuffer.put(2, 1.0f);

        vertexBuffer.put(3 * (slices + 2), 0f);
        vertexBuffer.put(3 * (slices + 2) + 1, 0f);
        vertexBuffer.put(3 * (slices + 2) + 2, -1.0f);

        // rim points
        for (i = 0; i <= slices; i++)
        {
            a = i * da;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            z = 1.0f;

            index = 3 * i + 3;

            // wedge top
            vertexBuffer.put(index, x * radius);
            vertexBuffer.put(index + 1, y * radius);
            vertexBuffer.put(index + 2, z);

            index += 3;     // add 3 for the second center point

            // wedge bottom
            vertexBuffer.put(index + 3 * (slices + 1), x * radius);
            vertexBuffer.put(index + 3 * (slices + 1) + 1, y * radius);
            vertexBuffer.put(index + 3 * (slices + 1) + 2, -z);

            index = 3 * (2 * slices + 4 + 2 * i);

            // core upper rim
            vertexBuffer.put(index, x * radius);
            vertexBuffer.put(index + 1, y * radius);
            vertexBuffer.put(index + 2, z);

            // core lower rim
            vertexBuffer.put(index + 3, x * radius);
            vertexBuffer.put(index + 4, y * radius);
            vertexBuffer.put(index + 5, -z);
        }

        // wedge sides
        for (i = 0; i < 2; i++)
        {
            x = (float) Math.sin(i * angle.getRadians());
            y = (float) Math.cos(i * angle.getRadians());
            z = 1.0f;

            index = 3 * (4 * (slices + 1 + i) + 2);

            // inner points
            vertexBuffer.put(index, 0);
            vertexBuffer.put(index + 1, 0);
            vertexBuffer.put(index + 2, z);

            vertexBuffer.put(index + 3, 0);
            vertexBuffer.put(index + 4, 0);
            vertexBuffer.put(index + 5, -z);

            // outer points
            vertexBuffer.put(index + 6, x * radius);
            vertexBuffer.put(index + 7, y * radius);
            vertexBuffer.put(index + 8, z);

            vertexBuffer.put(index + 9, x * radius);
            vertexBuffer.put(index + 10, y * radius);
            vertexBuffer.put(index + 11, -z);
        }

        // INDICES

        int coreIndex = 2 * (slices + 1) + 2;

        for (i = 0; i < slices; i++)
        {
            // wedge top
            index = 3 * i;

            indexBuffer.put(index, 0);                          // center point
            indexBuffer.put(index + 1, i + 2);
            indexBuffer.put(index + 2, i + 1);

            // wedge bottom
            index = 3 * (slices + i);

            indexBuffer.put(index, (slices + 2));               // center point
            indexBuffer.put(index + 1, (slices + 2) + i + 2);
            indexBuffer.put(index + 2, (slices + 2) + i + 1);

            // wedge core
            index = 6 * (slices + i);

            indexBuffer.put(index + 0, coreIndex + 0);
            indexBuffer.put(index + 1, coreIndex + 1);
            indexBuffer.put(index + 2, coreIndex + 2);

            indexBuffer.put(index + 3, coreIndex + 2);
            indexBuffer.put(index + 4, coreIndex + 1);
            indexBuffer.put(index + 5, coreIndex + 3);

            coreIndex += 2;
        }

        // wedge sides
        for (i = 0; i < 2; i++)
        {
            index = 3 * (4 * slices) + 6 * i;
            coreIndex = 4 * (slices + 1) + 2 + i * 4;

            indexBuffer.put(index + 0, coreIndex + 0);
            indexBuffer.put(index + 1, coreIndex + 2);
            indexBuffer.put(index + 2, coreIndex + 1);

            indexBuffer.put(index + 3, coreIndex + 1);
            indexBuffer.put(index + 4, coreIndex + 2);
            indexBuffer.put(index + 5, coreIndex + 3);
        }

        // The static wedge tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (index = 0; index < wedgeIndexCount; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a triangular tessellated wedge.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            wedgeIndexCount, indexBuffer, wedgeVertexCount, vertexBuffer);

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    public IndexedTriangleBuffer tessellateWedgeBuffer(int face, float radius, int subdivisions, Angle angle)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (angle.getRadians() < 0 || angle.getRadians() > 2 * Math.PI)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "angle < 0 or angle > 2 PI");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, index;
        float x, y, z, a;

        // face 0 = top
        // face 1 = bottom
        // face 2 = round core wall
        // face 3 = first wedge side
        // face 4 = second wedge side

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = (float) angle.getRadians() / slices;

        int wedgeIndexCount = 6;
        int wedgeVertexCount = 4;

        if (face == 0 || face == 1)
        {
            wedgeIndexCount = 3 * slices;
            wedgeVertexCount = slices + 2;
        }
        else if (face == 2)
        {
            wedgeIndexCount = 6 * slices;
            wedgeVertexCount = 2 * slices + 2;
        }

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(wedgeIndexCount);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * wedgeVertexCount);

        // VERTICES

        if (face == 0 || face == 1)      // wedge top or bottom
        {

            int isTop = 1;
            if (face == 1)
                isTop = -1;

            // center point
            vertexBuffer.put(0, 0f);
            vertexBuffer.put(1, 0f);
            vertexBuffer.put(2, isTop * 1.0f);

            // rim points
            for (i = 0; i <= slices; i++)
            {
                a = i * da;
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                z = 1.0f;

                index = 3 * i + 3;

                // wedge top
                vertexBuffer.put(index, x * radius);
                vertexBuffer.put(index + 1, y * radius);
                vertexBuffer.put(index + 2, isTop * z);
            }
        }
        else if (face == 2)              // round core wall
        {
            // rim points
            for (i = 0; i <= slices; i++)
            {
                a = i * da;
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                z = 1.0f;

                index = 3 * (2 * i);

                // core upper rim
                vertexBuffer.put(index, x * radius);
                vertexBuffer.put(index + 1, y * radius);
                vertexBuffer.put(index + 2, z);

                // core lower rim
                vertexBuffer.put(index + 3, x * radius);
                vertexBuffer.put(index + 4, y * radius);
                vertexBuffer.put(index + 5, -z);
            }
        }
        else if (face == 3 || face == 4)
        {
            // wedge side
            i = face - 3;

            x = (float) Math.sin(i * angle.getRadians());
            y = (float) Math.cos(i * angle.getRadians());
            z = 1.0f;

            index = 0;

            // inner points
            vertexBuffer.put(index, 0);
            vertexBuffer.put(index + 1, 0);
            vertexBuffer.put(index + 2, z);

            vertexBuffer.put(index + 3, 0);
            vertexBuffer.put(index + 4, 0);
            vertexBuffer.put(index + 5, -z);

            // outer points
            vertexBuffer.put(index + 6, x * radius);
            vertexBuffer.put(index + 7, y * radius);
            vertexBuffer.put(index + 8, z);

            vertexBuffer.put(index + 9, x * radius);
            vertexBuffer.put(index + 10, y * radius);
            vertexBuffer.put(index + 11, -z);
        }

        // INDICES

        if (face == 0 || face == 1)      // top or bottom
        {
            for (i = 0; i < slices; i++)
            {
                // wedge top
                index = 3 * i;

                indexBuffer.put(index, 0);                          // center point
                indexBuffer.put(index + 1, i + 2);
                indexBuffer.put(index + 2, i + 1);
            }
        }
        else if (face == 2)
        {
            int coreIndex = 0;

            for (i = 0; i < slices; i++)
            {
                // wedge core
                index = 6 * i;

                indexBuffer.put(index + 0, coreIndex + 0);
                indexBuffer.put(index + 1, coreIndex + 1);
                indexBuffer.put(index + 2, coreIndex + 2);

                indexBuffer.put(index + 3, coreIndex + 2);
                indexBuffer.put(index + 4, coreIndex + 1);
                indexBuffer.put(index + 5, coreIndex + 3);

                coreIndex += 2;
            }
        }
        else if (face == 3 || face == 4)
        {
            // wedge side
            indexBuffer.put(0, 0);
            indexBuffer.put(1, 2);
            indexBuffer.put(2, 1);

            indexBuffer.put(3, 1);
            indexBuffer.put(4, 2);
            indexBuffer.put(5, 3);
        }

        // The static wedge tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (index = 0; index < wedgeIndexCount; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a triangular tessellated wedge.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            wedgeIndexCount, indexBuffer, wedgeVertexCount, vertexBuffer);

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    //**************************************************************//
    //*********************         Cone         *******************//
    //**************************************************************//

    public IndexedTriangleBuffer tessellateConeBuffer(float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, index;
        float x, y, z, a;

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = 2.0f * (float) Math.PI / (float) slices;

        int coneIndexCount = 12 * slices;
        int coneVertexCount = 4 * slices + 4;

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(coneIndexCount);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * coneVertexCount);

        // VERTICES

        // bottom center point
        vertexBuffer.put(0, 0f);
        vertexBuffer.put(1, 0f);
        vertexBuffer.put(2, -1.0f);

        // rim points
        for (i = 0; i < slices; i++)
        {
            a = i * da;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            z = 1.0f;

            index = 3 * i + 3;

            // cone bottom
            vertexBuffer.put(index, x * radius);
            vertexBuffer.put(index + 1, y * radius);
            vertexBuffer.put(index + 2, -z);

            index += 3 * slices + 3 * i;

            // core upper rim - all points are at same location
            vertexBuffer.put(index, 0);
            vertexBuffer.put(index + 1, 0);
            vertexBuffer.put(index + 2, z);

            // core lower rim
            vertexBuffer.put(index + 3, x * radius);
            vertexBuffer.put(index + 4, y * radius);
            vertexBuffer.put(index + 5, -z);
        }

        // extra vertices for seamless texture mapping

        int wrapIndex = 3 * (3 * slices + 1);
        x = (float) Math.sin(0);
        y = (float) Math.cos(0);
        z = 1.0f;

        vertexBuffer.put(wrapIndex, 0);
        vertexBuffer.put(wrapIndex + 1, 0);
        vertexBuffer.put(wrapIndex + 2, z);

        vertexBuffer.put(wrapIndex + 3, x * radius);
        vertexBuffer.put(wrapIndex + 4, y * radius);
        vertexBuffer.put(wrapIndex + 5, -z);

        // INDICES

        int coreIndex = slices + 1;
        int centerPoint = 0;

        for (i = 0; i < slices; i++)
        {
            index = 3 * i;

            // cone bottom
            indexBuffer.put(index, 0);             // center point
            indexBuffer.put(index + 1, (i < slices - 1) ? i + 2 : 1);
            indexBuffer.put(index + 2, i + 1);

            // cone core
            index += 3 * (slices + i);

            indexBuffer.put(index, coreIndex);
            indexBuffer.put(index + 1, coreIndex + 1);
            indexBuffer.put(index + 2, coreIndex + 2);

            indexBuffer.put(index + 3, coreIndex + 2);
            indexBuffer.put(index + 4, coreIndex + 1);
            indexBuffer.put(index + 5, coreIndex + 3);

            coreIndex += 2;
        }

        // The static cone tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (index = 0; index < coneIndexCount; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a triangular tessellated cone.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            coneIndexCount, indexBuffer, coneVertexCount, vertexBuffer);

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    public IndexedTriangleBuffer tessellateConeBuffer(int face, float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // face 0 = base
        // face 1 = core

        int i, index;
        float x, y, z, a;

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = 2.0f * (float) Math.PI / (float) slices;

        int coneIndexCount = 3 * slices;
        int coneVertexCount = slices + 1;

        if (face == 1)   // cone core
        {
            coneIndexCount = 6 * slices;
            coneVertexCount = 2 * slices + 2;
        }

        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(coneIndexCount);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(3 * coneVertexCount);

        // VERTICES

        if (face == 0)     // cone base
        {
            // base center point
            vertexBuffer.put(0, 0f);
            vertexBuffer.put(1, 0f);
            vertexBuffer.put(2, -1.0f);

            // base rim points
            for (i = 0; i < slices; i++)
            {
                a = i * da;
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                z = 1.0f;

                index = 3 * i + 3;

                vertexBuffer.put(index, x * radius);
                vertexBuffer.put(index + 1, y * radius);
                vertexBuffer.put(index + 2, -z);
            }
        }
        else if (face == 1)     // cone core
        {
            // rim points
            for (i = 0; i < slices; i++)
            {
                a = i * da;
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                z = 1.0f;

                index = 6 * i;

                // core upper rim
                vertexBuffer.put(index, 0);
                vertexBuffer.put(index + 1, 0);
                vertexBuffer.put(index + 2, z);

                // core lower rim
                vertexBuffer.put(index + 3, x * radius);
                vertexBuffer.put(index + 4, y * radius);
                vertexBuffer.put(index + 5, -z);
            }

            // extra vertices for seamless texture mapping

            int wrapIndex = 3 * (2 * slices);
            x = (float) Math.sin(0);
            y = (float) Math.cos(0);
            z = 1.0f;

            vertexBuffer.put(wrapIndex, 0);
            vertexBuffer.put(wrapIndex + 1, 0);
            vertexBuffer.put(wrapIndex + 2, z);

            vertexBuffer.put(wrapIndex + 3, x * radius);
            vertexBuffer.put(wrapIndex + 4, y * radius);
            vertexBuffer.put(wrapIndex + 5, -z);
        }

        // INDICES

        int centerPoint = 0;

        if (face == 0)      // cone base
        {
            for (i = 0; i < slices; i++)
            {
                index = 3 * i;

                indexBuffer.put(index, 0);                  // center point
                indexBuffer.put(index + 1, (i < slices - 1) ? i + 2 : 1);
                indexBuffer.put(index + 2, i + 1);
            }
        }
        else if (face == 1)     // cone core
        {
            int coreIndex = 0;

            for (i = 0; i < slices; i++)
            {
                index = 6 * i;

                indexBuffer.put(index, coreIndex);
                indexBuffer.put(index + 1, coreIndex + 1);
                indexBuffer.put(index + 2, coreIndex + 2);

                indexBuffer.put(index + 3, coreIndex + 2);
                indexBuffer.put(index + 4, coreIndex + 1);
                indexBuffer.put(index + 5, coreIndex + 3);

                coreIndex += 2;
            }
        }

        // The static cone tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (index = 0; index < coneIndexCount; index += 3)
            {
                int tmp = indexBuffer.get(index);
                indexBuffer.put(index, indexBuffer.get(index + 2));
                indexBuffer.put(index + 2, tmp);
            }
        }

        // Start with a triangular tessellated cone.
        IndexedTriangleBuffer itb = new IndexedTriangleBuffer(
            coneIndexCount, indexBuffer, coneVertexCount, vertexBuffer);

        // Scale each vertex by the specified radius.
        if (radius != 1)
        {
            vertexBuffer = itb.getVertices();
            for (int vertex = 0; vertex < itb.vertexCount; vertex++)
            {
                mul3AndSet(vertexBuffer, 3 * vertex, radius);
            }
        }

        itb.vertices.rewind();
        itb.indices.rewind();

        return itb;
    }

    //**************************************************************//
    //********************       Cylinder        *******************//
    //**************************************************************//

    public int getCylinderVertexCount(int slices, int stacks)
    {
        return slices * (stacks + 1);
    }

    public int getCylinderIndexCount(int slices, int stacks)
    {
        return stacks * 2 * (slices + 1) + 2 * (stacks - 1);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getCylinderOutlineIndexCount(int slices, int stacks)
    {
        return slices * 4;
    }

    public int getCylinderDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public int getCylinderOutlineDrawMode()
    {
        return GL.GL_LINES;
    }

    public LatLon[] makeCylinderLocations(Globe globe, LatLon center, double radius, int slices)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (slices < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = 2.0 * Math.PI / slices;
        double r = radius / globe.getRadius();
        LatLon[] dest = new LatLon[slices];

        for (int i = 0; i < slices; i++)
        {
            double a = i * da;
            dest[i] = LatLon.greatCircleEndPosition(center, a, r);
        }

        return dest;
    }

    public LatLon[] makeCylinderLocations(Globe globe, LatLon center, double minorRadius, double majorRadius,
        Angle heading, int slices)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (slices < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = 2.0 * Math.PI / slices;
        LatLon[] dest = new LatLon[slices];

        for (int i = 0; i < slices; i++)
        {
            double a = i * da;
            double cosA = Math.cos(a);
            double sinA = Math.sin(a);
            double bCosA = minorRadius * cosA;
            double aSinA = majorRadius * sinA;
            double r = (minorRadius * majorRadius) / Math.sqrt(bCosA * bCosA + aSinA * aSinA);
            dest[i] = LatLon.greatCircleEndPosition(center, a + heading.radians, r / globe.getRadius());
        }

        return dest;
    }

    public void makeCylinderVertices(Terrain terrain, LatLon center, double radius, double[] altitudes,
        boolean[] terrainConformant, int slices, int stacks, Vec4 refPoint, float[] dest)
    {
        int numPoints = this.getCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (terrain == null)
        {
            String message = Logging.getMessage("nullValue.TerrainIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (refPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = 2.0 * Math.PI / slices;
        double r = radius / terrain.getGlobe().getRadius();
        FloatBuffer destBuffer = FloatBuffer.wrap(dest);

        for (int i = 0; i < slices; i++)
        {
            double a = i * da;
            LatLon ll = LatLon.greatCircleEndPosition(center, a, r);

            for (int j = 0; j <= stacks; j++)
            {
                this.append(terrain, ll, altitudes[j], terrainConformant[j], refPoint, destBuffer);
            }
        }
    }

    public void makeCylinderVertices(Terrain terrain, LatLon center, double minorRadius, double majorRadius,
        Angle heading, double[] altitudes,
        boolean[] terrainConformant, int slices, int stacks, Vec4 refPoint, float[] dest)
    {
        int numPoints = this.getCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (terrain == null)
        {
            String message = Logging.getMessage("nullValue.TerrainIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (refPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = 2.0 * Math.PI / (slices - 1);
        double globeRadius = terrain.getGlobe().getRadius();
        FloatBuffer destBuffer = FloatBuffer.wrap(dest);

        for (int i = 0; i < slices; i++)
        {
            double angle = (i != slices - 1) ? i * da : 0;
            double yLength = majorRadius * Math.cos(angle);
            double xLength = minorRadius * Math.sin(angle);
            double distance = Math.sqrt(xLength * xLength + yLength * yLength);
            // azimuth runs positive clockwise from north and through 360 degrees.
            double azimuth = (Math.PI / 2.0) - (Math.acos(xLength / distance) * Math.signum(yLength) - heading.radians);

            LatLon ll = LatLon.greatCircleEndPosition(center, azimuth, distance / globeRadius);

            for (int j = 0; j <= stacks; j++)
            {
                this.append(terrain, ll, altitudes[j], terrainConformant[j], refPoint, destBuffer);
            }
        }
    }

    public void makeCylinderVertices(float radius, float height, int slices, int stacks, float[] dest)
    {
        int numPoints = this.getCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y, z;
        float a;
        float dz, da;
        int i, j;
        int index;

        if (stacks != 0.0f)
            dz = height / (float) stacks;
        else
            dz = 0.0f;
        da = 2.0f * (float) Math.PI / (float) slices;

        for (i = 0; i < slices; i++)
        {
            a = i * da;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            z = 0.0f;
            for (j = 0; j <= stacks; j++)
            {
                index = j + i * (stacks + 1);
                index = 3 * index;
                dest[index] = x * radius;
                dest[index + 1] = y * radius;
                dest[index + 2] = z;
                z += dz;
            }
        }
    }

    public void makeCylinderNormals(int slices, int stacks, float[] dest)
    {
        int numPoints = this.getCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a;
        float da;
        float nsign;
        int i, j;
        int index;
        float[] norm;

        da = 2.0f * (float) Math.PI / (float) slices;
        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];

        for (i = 0; i < slices; i++)
        {
            a = i * da;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            norm[0] = x * nsign;
            norm[1] = y * nsign;
            norm[2] = 0.0f;
            this.norm3AndSet(norm, 0);

            for (j = 0; j <= stacks; j++)
            {
                index = j + i * (stacks + 1);
                index = 3 * index;
                System.arraycopy(norm, 0, dest, index, 3);
            }
        }
    }

    public void makeEllipticalCylinderNormals(int slices, int stacks, double minorRadius, double majorRadius,
        Angle heading, float[] dest)
    {
        int numPoints = this.getCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double x, y;
        double a;
        double da;
        double nsign;
        int i, j;
        int index;
        float[] norm;
        double a2 = majorRadius * majorRadius;
        double b2 = minorRadius * minorRadius;
        double d;

        da = 2.0f * (float) Math.PI / (float) slices;
        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];

        for (i = 0; i < slices; i++)
        {
            a = i * da + heading.radians;
            x = majorRadius * Math.sin(a) / a2;
            y = minorRadius * Math.cos(a) / b2;
            d = Math.sqrt(x * x + y * y);
            norm[0] = (float) ((x / d) * nsign);
            norm[1] = (float) ((y / d) * nsign);
            norm[2] = 0.0f;
            this.norm3AndSet(norm, 0);

            for (j = 0; j <= stacks; j++)
            {
                index = j + i * (stacks + 1);
                index = 3 * index;
                System.arraycopy(norm, 0, dest, index, 3);
            }
        }
    }

    public void makeCylinderIndices(int slices, int stacks, int[] dest)
    {
        int numIndices = this.getCylinderIndexCount(slices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, j;
        int vertex, index;

        index = 0;
        for (j = 0; j < stacks; j++)
        {
            if (j != 0)
            {
                if (this.orientation == INSIDE)
                    vertex = j + 1;
                else // (this.orientation == OUTSIDE)
                    vertex = j;
                dest[index++] = vertex;
                dest[index++] = vertex;
            }
            for (i = 0; i <= slices; i++)
            {
                if (i == slices)
                    vertex = j;
                else
                    vertex = j + i * (stacks + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex + 1;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + 1;
                }
            }
        }
    }

    public void makeCylinderOutlineIndices(int slices, int stacks, int[] dest)
    {
        int numIndices = this.getCylinderOutlineIndexCount(slices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i;
        int vertex, index;

        index = 0;
        // Bottom ring
        for (i = 0; i < slices; i++)
        {
            vertex = i * (stacks + 1);
            dest[index++] = vertex;
            dest[index++] = (i != slices - 1) ? vertex + stacks + 1 : 0;
        }
        // Top ring
        for (i = 0; i < slices; i++)
        {
            vertex = i * (stacks + 1) + stacks;
            dest[index++] = vertex;
            dest[index++] = (i != slices - 1) ? vertex + stacks + 1 : stacks;
        }
//        // Vertical edges
//        for (i = 0; i < slices; i++)
//        {
//            vertex = i * (stacks + 1);
//            dest[index++] = vertex;
//            dest[index++] = vertex + stacks;
//        }
    }

    //**************************************************************//
    //********************  Partial Cylinder    ********************//
    //**************************************************************//

    public int getPartialCylinderVertexCount(int slices, int stacks)
    {
        return (slices + 1) * (stacks + 1);
    }

    public int getPartialCylinderIndexCount(int slices, int stacks)
    {
        return stacks * 2 * (slices + 1) + 2 * (stacks - 1);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getPartialCylinderOutlineIndexCount(int slices, int stacks)
    {
        return slices * 4;
    }

    public int getPartialCylinderDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public int getPartialCylinderOutlineDrawMode()
    {
        return GL.GL_LINES;
    }

    public LatLon[] makePartialCylinderLocations(Globe globe, LatLon center, double radius, int slices, double start,
        double sweep)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (slices < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = sweep / slices;
        double r = radius / globe.getRadius();
        LatLon[] dest = new LatLon[slices + 1];

        for (int i = 0; i <= slices; i++)
        {
            double a = i * da + start;
            dest[i] = LatLon.greatCircleEndPosition(center, a, r);
        }

        return dest;
    }

    public void makePartialCylinderVertices(Terrain terrain, LatLon center, double radius, double[] altitudes,
        boolean[] terrainConformant, int slices, int stacks, double start, double sweep, Vec4 refPoint, float[] dest)
    {
        int numPoints = this.getPartialCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (terrain == null)
        {
            String message = Logging.getMessage("nullValue.TerrainIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (refPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = sweep / slices;
        double r = radius / terrain.getGlobe().getRadius();
        FloatBuffer destBuffer = FloatBuffer.wrap(dest);

        for (int i = 0; i <= slices; i++)
        {
            double a = i * da + start;
            LatLon ll = LatLon.greatCircleEndPosition(center, a, r);

            for (int j = 0; j <= stacks; j++)
            {
                this.append(terrain, ll, altitudes[j], terrainConformant[j], refPoint, destBuffer);
            }
        }
    }

    public void makePartialCylinderVertices(float radius, float height, int slices, int stacks,
        float start, float sweep, float[] dest)
    {
        int numPoints = this.getPartialCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y, z;
        float a;
        float dz, da;
        int i, j;
        int index;

        if (stacks != 0.0f)
            dz = height / (float) stacks;
        else
            dz = 0.0f;
        da = sweep / (float) slices;

        for (i = 0; i <= slices; i++)
        {
            a = i * da + start;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            z = 0.0f;
            for (j = 0; j <= stacks; j++)
            {
                index = j + i * (stacks + 1);
                index = 3 * index;
                dest[index] = x * radius;
                dest[index + 1] = y * radius;
                dest[index + 2] = z;
                z += dz;
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void makePartialCylinderNormals(float radius, float height, int slices, int stacks,
        float start, float sweep, float[] dest)
    {
        int numPoints = this.getPartialCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a;
        float da;
        float nsign;
        int i, j;
        int index;
        float[] norm;

        da = sweep / (float) slices;
        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];

        for (i = 0; i <= slices; i++)
        {
            a = i * da + start;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            norm[0] = x * nsign;
            norm[1] = y * nsign;
            norm[2] = 0.0f;
            this.norm3AndSet(norm, 0);

            for (j = 0; j <= stacks; j++)
            {
                index = j + i * (stacks + 1);
                index = 3 * index;
                System.arraycopy(norm, 0, dest, index, 3);
            }
        }
    }

    public void makePartialCylinderIndices(int slices, int stacks, int[] dest)
    {
        int numIndices = this.getPartialCylinderIndexCount(slices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, j;
        int vertex, index;

        index = 0;
        for (j = 0; j < stacks; j++)
        {
            if (j != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = j + slices * (stacks + 1);
                    dest[index++] = vertex - 1;
                    vertex = j + 1;
                    dest[index++] = vertex;
                }
                else //(this.orientation == OUTSIDE)
                {
                    vertex = j + slices * (stacks + 1);
                    dest[index++] = vertex;
                    vertex = j;
                    dest[index++] = vertex;
                }
            }
            for (i = 0; i <= slices; i++)
            {
                vertex = j + i * (stacks + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex + 1;
                    dest[index++] = vertex;
                }
                else //(this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + 1;
                }
            }
        }
    }

    public void makePartialCylinderOutlineIndices(int slices, int stacks, int[] dest)
    {
        int numIndices = this.getPartialCylinderOutlineIndexCount(slices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i;
        int vertex, index;

        index = 0;
        // Bottom ring
        for (i = 0; i < slices; i++)
        {
            vertex = i * (stacks + 1);
            dest[index++] = vertex;
            dest[index++] = vertex + stacks + 1;
        }
        // Top ring
        for (i = 0; i < slices; i++)
        {
            vertex = i * (stacks + 1) + stacks;
            dest[index++] = vertex;
            dest[index++] = vertex + stacks + 1;
        }
    }

    //**************************************************************//
    //********************  Disk                ********************//
    //**************************************************************//

    public int getDiskVertexCount(int slices, int loops)
    {
        return slices * (loops + 1);
    }

    public int getDiskIndexCount(int slices, int loops)
    {
        return loops * 2 * (slices + 1) + 2 * (loops - 1);
    }

    public int getDiskDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public LatLon[] makeDiskLocations(Globe globe, LatLon center, double innerRadius, double outerRadius, int slices,
        int loops)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (slices < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (loops < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "loops < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = 2.0 * Math.PI / slices;
        double dr = (outerRadius - innerRadius) / loops;
        LatLon[] dest = new LatLon[slices * (loops + 1)];
        int index = 0;

        for (int s = 0; s < slices; s++)
        {
            double a = s * da;

            for (int l = 0; l <= loops; l++)
            {
                double r = (innerRadius + l * dr) / globe.getRadius();
                dest[index++] = LatLon.greatCircleEndPosition(center, a, r);
            }
        }

        return dest;
    }

    public LatLon[] makeDiskLocations(Globe globe, LatLon center, double[] radii, Angle heading, int slices, int loops)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (slices < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (loops < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "loops < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double innerMinorRadius = radii[0];
        double innerMajorRadius = radii[1];
        double outerMinorRadius = radii[2];
        double outerMajorRadius = radii[3];
        double da = 2.0 * Math.PI / slices;
        double dMinor = (outerMinorRadius - innerMinorRadius) / loops;
        double dMajor = (outerMajorRadius - innerMajorRadius) / loops;
        LatLon[] dest = new LatLon[slices * (loops + 1)];
        int index = 0;

        for (int s = 0; s < slices; s++)
        {
            double a = s * da;
            double cosA = Math.cos(a);
            double sinA = Math.sin(a);

            for (int l = 0; l <= loops; l++)
            {
                double minorRadius = (innerMinorRadius + l * dMinor);
                double majorRadius = (innerMajorRadius + l * dMajor);
                double bCosA = minorRadius * cosA;
                double aSinA = majorRadius * sinA;
                double r = (minorRadius * majorRadius) / Math.sqrt(bCosA * bCosA + aSinA * aSinA);
                dest[index++] = LatLon.greatCircleEndPosition(center, a + heading.radians, r / globe.getRadius());
            }
        }

        return dest;
    }

    public void makeDiskVertices(Terrain terrain, LatLon center, double innerRadius, double outerRadius,
        double altitude, boolean terrainConformant, int slices, int loops, Vec4 refPoint, float[] dest)
    {
        int numPoints = this.getDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (terrain == null)
        {
            String message = Logging.getMessage("nullValue.TerrainIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (refPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = 2.0 * Math.PI / slices;
        double dr = (outerRadius - innerRadius) / loops;
        double globeRadius = terrain.getGlobe().getRadius();
        FloatBuffer destBuffer = FloatBuffer.wrap(dest);

        for (int s = 0; s < slices; s++)
        {
            double a = s * da;

            for (int l = 0; l <= loops; l++)
            {
                double r = (innerRadius + l * dr) / globeRadius;
                LatLon ll = LatLon.greatCircleEndPosition(center, a, r);
                this.append(terrain, ll, altitude, terrainConformant, refPoint, destBuffer);
            }
        }
    }

    public void makeDiskVertices(Terrain terrain, LatLon center, double[] radii, Angle heading,
        double altitude, boolean terrainConformant, int slices, int loops, Vec4 refPoint, float[] dest)
    {
        int numPoints = this.getDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (terrain == null)
        {
            String message = Logging.getMessage("nullValue.TerrainIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (refPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double innerMinorRadius = radii[0];
        double innerMajorRadius = radii[1];
        double outerMinorRadius = radii[2];
        double outerMajorRadius = radii[3];
        double da = 2.0 * Math.PI / (slices - 1);
        double dMinor = (outerMinorRadius - innerMinorRadius) / loops;
        double dMajor = (outerMajorRadius - innerMajorRadius) / loops;
        double globeRadius = terrain.getGlobe().getRadius();
        FloatBuffer destBuffer = FloatBuffer.wrap(dest);

        for (int s = 0; s < slices; s++)
        {
            double a = (s != slices - 1) ? s * da : 0;
            double cosA = Math.cos(a);
            double sinA = Math.sin(a);

            for (int l = 0; l <= loops; l++)
            {
                double minorRadius = (innerMinorRadius + l * dMinor);
                double majorRadius = (innerMajorRadius + l * dMajor);
                double yLength = majorRadius * cosA;
                double xLength = minorRadius * sinA;
                double r = Math.sqrt(xLength * xLength + yLength * yLength);
                double azimuth = (Math.PI / 2) - (Math.acos(xLength / r) * Math.signum(yLength) - heading.radians);
                LatLon ll = LatLon.greatCircleEndPosition(center, azimuth, r / globeRadius);
                this.append(terrain, ll, altitude, terrainConformant, refPoint, destBuffer);
            }
        }
    }

    public void makeDiskVertices(float innerRadius, float outerRadius, int slices, int loops, float[] dest)
    {
        int numPoints = this.getDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a, r;
        float da, dr;
        int s, l;
        int index;

        da = 2.0f * (float) Math.PI / (float) slices;
        dr = (outerRadius - innerRadius) / (float) loops;

        for (s = 0; s < slices; s++)
        {
            a = s * da;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            for (l = 0; l <= loops; l++)
            {
                index = l + s * (loops + 1);
                index = 3 * index;
                r = innerRadius + l * dr;
                dest[index] = r * x;
                dest[index + 1] = r * y;
                dest[index + 2] = 0.0f;
            }
        }
    }

    public void makeDiskNormals(int slices, int loops, float[] dest)
    {
        int numPoints = this.getDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int index;
        float nsign;
        float[] normal;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        normal = new float[3];
        normal[0] = 0.0f;
        normal[1] = 0.0f;
        //noinspection PointlessArithmeticExpression
        normal[2] = 1.0f * nsign;

        for (s = 0; s < slices; s++)
        {
            for (l = 0; l <= loops; l++)
            {
                index = l + s * (loops + 1);
                index = 3 * index;
                System.arraycopy(normal, 0, dest, index, 3);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void makeDiskVertexNormals(double innerMinorRadius, double outerMinorRadius, int slices, int loops,
        float[] srcVerts, float[] dest)
    {
        int numPoints = this.getDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (srcVerts == null)
        {
            String message = "nullValue.SourceVertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int index;
        float nsign;
        float[] norm, zero, tmp;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        zero = new float[3];
        tmp = new float[3];

        for (l = 0; l <= loops; l++)
        {
            // Normal vectors for first and last loops require a special case.
            if (l == 0 || l == loops)
            {
                // Closed disk: all slices share a common center point.
                if (l == 0 && (innerMinorRadius == 0.0f || outerMinorRadius == 0))
                {
                    // Compute common center point normal.
                    int nextSlice;
                    int adjacentLoop;
                    System.arraycopy(zero, 0, norm, 0, 3);
                    for (s = 0; s < slices; s++)
                    {
                        index = l + s * (loops + 1);
                        nextSlice = l + (s + 1) * (loops + 1);
                        if (s == slices - 1)
                            nextSlice = l;
                        adjacentLoop = index + 1;
                        this.facenorm(srcVerts, index, nextSlice + 1, adjacentLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                    this.mul3AndSet(norm, 0, nsign);
                    this.norm3AndSet(norm, 0);
                    // Copy common normal to the first point of each slice.
                    for (s = 0; s < slices; s++)
                    {
                        index = l + s * (loops + 1);
                        System.arraycopy(norm, 0, dest, 3 * index, 3);
                    }
                }
                // Open disk: each slice has a unique starting point.
                else
                {
                    for (s = 0; s < slices; s++)
                    {
                        int prevSlice, nextSlice;
                        int adjacentLoop;
                        index = l + s * (loops + 1);
                        prevSlice = l + (s - 1) * (loops + 1);
                        nextSlice = l + (s + 1) * (loops + 1);

                        if (s == 0)
                            prevSlice = l + (slices - 1) * (loops + 1);
                        else if (s == slices - 1)
                            nextSlice = l;

                        if (l == 0)
                            adjacentLoop = index + 1;
                        else
                            adjacentLoop = index - 1;

                        System.arraycopy(zero, 0, norm, 0, 3);

                        // Add clockwise adjacent face.
                        if (l == 0)
                            this.facenorm(srcVerts, index, nextSlice, adjacentLoop, tmp);
                        else
                            this.facenorm(srcVerts, index, adjacentLoop, nextSlice, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        // Add counter-clockwise adjacent face.
                        if (l == 0)
                            this.facenorm(srcVerts, index, adjacentLoop, prevSlice, tmp);
                        else
                            this.facenorm(srcVerts, index, prevSlice, adjacentLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);

                        // Normalize and place in output.
                        this.mul3AndSet(norm, 0, nsign);
                        this.norm3AndSet(norm, 0);
                        System.arraycopy(norm, 0, dest, 3 * index, 3);
                    }
                }
            }
            // Normal vectors for internal loops.
            else
            {
                for (s = 0; s < slices; s++)
                {
                    int prevSlice, nextSlice;
                    int prevLoop, nextLoop;
                    index = l + s * (loops + 1);
                    prevSlice = l + (s - 1) * (loops + 1);
                    nextSlice = l + (s + 1) * (loops + 1);

                    if (s == 0)
                        prevSlice = l + (slices - 1) * (loops + 1);
                    else if (s == slices - 1)
                        nextSlice = l;

                    prevLoop = index - 1;
                    nextLoop = index + 1;

                    System.arraycopy(zero, 0, norm, 0, 3);

                    // Add lower-left adjacent face.
                    this.facenorm(srcVerts, index, prevSlice, prevSlice - 1, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, prevSlice - 1, prevLoop, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add lower-right adjacent face.
                    this.facenorm(srcVerts, index, prevLoop, nextSlice - 1, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, nextSlice - 1, nextSlice, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add upper-right adjacent face.
                    this.facenorm(srcVerts, index, nextSlice, nextSlice + 1, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, nextSlice + 1, nextLoop, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add upper-left adjacent face.
                    this.facenorm(srcVerts, index, nextLoop, prevSlice + 1, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, prevSlice + 1, prevSlice, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);

                    // Normalize and place in output.
                    this.mul3AndSet(norm, 0, nsign);
                    this.norm3AndSet(norm, 0);
                    System.arraycopy(norm, 0, dest, 3 * index, 3);
                }
            }
        }
    }

    public void makeDiskIndices(int slices, int loops, int[] dest)
    {
        int numIndices = this.getDiskIndexCount(slices, loops);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int vertex, index;

        index = 0;
        for (l = 0; l < loops; l++)
        {
            if (l != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = l;
                    dest[index++] = vertex;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = l - 1;
                    dest[index++] = vertex;
                    vertex = l + 1;
                    dest[index++] = vertex;
                }
            }
            for (s = 0; s <= slices; s++)
            {
                if (s == slices)
                    vertex = l;
                else
                    vertex = l + s * (loops + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + 1;
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex + 1;
                    dest[index++] = vertex;
                }
            }
        }
    }

    //**************************************************************//
    //********************  Partial Disk        ********************//
    //**************************************************************//

    public int getPartialDiskVertexCount(int slices, int loops)
    {
        return (slices + 1) * (loops + 1);
    }

    public int getPartialDiskIndexCount(int slices, int loops)
    {
        return loops * 2 * (slices + 1) + 2 * (loops - 1);
    }

    public int getPartialDiskDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public LatLon[] makePartialDiskLocations(Globe globe, LatLon center, double innerRadius, double outerRadius,
        int slices, int loops, double start, double sweep)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (slices < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (loops < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "loops < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = sweep / slices;
        double dr = (outerRadius - innerRadius) / loops;
        int count = this.getPartialDiskVertexCount(slices, loops);
        LatLon[] dest = new LatLon[count];
        int index = 0;

        for (int s = 0; s <= slices; s++)
        {
            double a = s * da + start;

            for (int l = 0; l <= loops; l++)
            {
                double r = (innerRadius + l * dr) / globe.getRadius();
                dest[index++] = LatLon.greatCircleEndPosition(center, a, r);
            }
        }

        return dest;
    }

    public void makePartialDiskVertices(Terrain terrain, LatLon center, double innerRadius, double outerRadius,
        double altitude, boolean terrainConformant, int slices, int loops, double start, double sweep, Vec4 refPoint,
        float[] dest)
    {
        int numPoints = this.getPartialDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (terrain == null)
        {
            String message = Logging.getMessage("nullValue.TerrainIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (refPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = sweep / slices;
        double dr = (outerRadius - innerRadius) / loops;
        double globeRadius = terrain.getGlobe().getRadius();
        FloatBuffer destBuffer = FloatBuffer.wrap(dest);

        for (int s = 0; s <= slices; s++)
        {
            double a = s * da + start;

            for (int l = 0; l <= loops; l++)
            {
                double r = (innerRadius + l * dr) / globeRadius;
                LatLon ll = LatLon.greatCircleEndPosition(center, a, r);
                this.append(terrain, ll, altitude, terrainConformant, refPoint, destBuffer);
            }
        }
    }

    public void makePartialDiskVertices(float innerRadius, float outerRadius, int slices, int loops,
        float start, float sweep, float[] dest)
    {
        int numPoints = this.getPartialDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a, r;
        float da, dr;
        int s, l;
        int index;

        da = sweep / (float) slices;
        dr = (outerRadius - innerRadius) / (float) loops;

        for (s = 0; s <= slices; s++)
        {
            a = s * da + start;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            for (l = 0; l <= loops; l++)
            {
                index = l + s * (loops + 1);
                index = 3 * index;
                r = innerRadius + l * dr;
                dest[index] = r * x;
                dest[index + 1] = r * y;
                dest[index + 2] = 0.0f;
            }
        }
    }

    public void makePartialDiskNormals(int slices, int loops, float[] dest)
    {
        int numPoints = this.getPartialDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int index;
        float nsign;
        float[] normal;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        normal = new float[3];
        normal[0] = 0.0f;
        normal[1] = 0.0f;
        //noinspection PointlessArithmeticExpression
        normal[2] = 1.0f * nsign;

        for (s = 0; s <= slices; s++)
        {
            for (l = 0; l <= loops; l++)
            {
                index = l + s * (loops + 1);
                index = 3 * index;
                System.arraycopy(normal, 0, dest, index, 3);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void makePartialDiskVertexNormals(float innerRadius, float outerRadius, int slices, int loops,
        float start, float sweep, float[] srcVerts, float[] dest)
    {
        int numPoints = this.getPartialDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (srcVerts == null)
        {
            String message = "nullValue.SourceVertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int index;
        float nsign;
        float[] norm, zero, tmp;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        zero = new float[3];
        tmp = new float[3];

        for (l = 0; l <= loops; l++)
        {
            // Normal vectors for first and last loops require a special case.
            if (l == 0 || l == loops)
            {
                // Closed disk: all slices share a common center point.
                if (l == 0 && innerRadius == 0.0f)
                {
                    // Compute common center point normal.
                    int nextSlice;
                    int adjacentLoop;
                    System.arraycopy(zero, 0, norm, 0, 3);
                    for (s = 0; s < slices; s++)
                    {
                        index = l + s * (loops + 1);
                        nextSlice = l + (s + 1) * (loops + 1);
                        adjacentLoop = index + 1;
                        this.facenorm(srcVerts, index, nextSlice + 1, adjacentLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                    this.mul3AndSet(norm, 0, nsign);
                    this.norm3AndSet(norm, 0);
                    // Copy common normal to the first point of each slice.
                    for (s = 0; s <= slices; s++)
                    {
                        index = l + s * (loops + 1);
                        System.arraycopy(norm, 0, dest, 3 * index, 3);
                    }
                }
                // Open disk: each slice has a unique starting point.
                else
                {
                    for (s = 0; s <= slices; s++)
                    {
                        int prevSlice, nextSlice;
                        int adjacentLoop;
                        index = l + s * (loops + 1);

                        if (l == 0)
                            adjacentLoop = index + 1;
                        else
                            adjacentLoop = index - 1;

                        System.arraycopy(zero, 0, norm, 0, 3);

                        if (s > 0)
                        {
                            prevSlice = l + (s - 1) * (loops + 1);
                            // Add counter-clockwise adjacent face.
                            if (l == 0)
                                this.facenorm(srcVerts, index, adjacentLoop, prevSlice, tmp);
                            else
                                this.facenorm(srcVerts, index, prevSlice, adjacentLoop, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                        }
                        if (s < slices)
                        {
                            nextSlice = l + (s + 1) * (loops + 1);
                            // Add clockwise adjacent face.
                            if (l == 0)
                                this.facenorm(srcVerts, index, nextSlice, adjacentLoop, tmp);
                            else
                                this.facenorm(srcVerts, index, adjacentLoop, nextSlice, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                        }

                        // Normalize and place in output.
                        this.mul3AndSet(norm, 0, nsign);
                        this.norm3AndSet(norm, 0);
                        System.arraycopy(norm, 0, dest, 3 * index, 3);
                    }
                }
            }
            // Normal vectors for internal loops.
            else
            {
                for (s = 0; s <= slices; s++)
                {
                    int prevSlice, nextSlice;
                    int prevLoop, nextLoop;
                    index = l + s * (loops + 1);
                    prevLoop = index - 1;
                    nextLoop = index + 1;

                    System.arraycopy(zero, 0, norm, 0, 3);
                    if (s > 0)
                    {
                        prevSlice = l + (s - 1) * (loops + 1);
                        // Add lower-left adjacent face.
                        this.facenorm(srcVerts, index, prevSlice, prevSlice - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, prevSlice - 1, prevLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        // Add upper-left adjacent face.
                        this.facenorm(srcVerts, index, nextLoop, prevSlice + 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, prevSlice + 1, prevSlice, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                    if (s < slices)
                    {
                        nextSlice = l + (s + 1) * (loops + 1);
                        // Add lower-right adjacent face.
                        this.facenorm(srcVerts, index, prevLoop, nextSlice - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, nextSlice - 1, nextSlice, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        // Add upper-right adjacent face.
                        this.facenorm(srcVerts, index, nextSlice, nextSlice + 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, nextSlice + 1, nextLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }

                    // Normalize and place in output.
                    this.mul3AndSet(norm, 0, nsign);
                    this.norm3AndSet(norm, 0);
                    System.arraycopy(norm, 0, dest, 3 * index, 3);
                }
            }
        }
    }

    public void makePartialDiskIndices(int slices, int loops, int[] dest)
    {
        int numIndices = this.getPartialDiskIndexCount(slices, loops);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int vertex, index;

        index = 0;
        for (l = 0; l < loops; l++)
        {
            if (l != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = l + slices * (loops + 1);
                    dest[index++] = vertex;
                    vertex = l;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = (l - 1) + slices * (loops + 1);
                    dest[index++] = vertex;
                    vertex = l;
                    dest[index++] = vertex + 1;
                }
            }
            for (s = 0; s <= slices; s++)
            {
                vertex = l + s * (loops + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + 1;
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex + 1;
                    dest[index++] = vertex;
                }
            }
        }
    }

    //**************************************************************//
    //********************  Radial Wall         ********************//
    //**************************************************************//

    public int getRadialWallVertexCount(int pillars, int stacks)
    {
        return (pillars + 1) * (stacks + 1);
    }

    public int getRadialWallIndexCount(int pillars, int stacks)
    {
        return stacks * 2 * (pillars + 1) + 2 * (stacks - 1);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getRadialWallOutlineIndexCount(int pillars, int stacks)
    {
        return pillars * 4;
    }

    public int getRadialWallDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public int getRadialWallOutlineDrawMode()
    {
        return GL.GL_LINES;
    }

    public void makeRadialWallVertices(Terrain terrain, LatLon center, double innerRadius, double outerRadius,
        double angle, double[] altitudes, boolean[] terrainConformant, int pillars, int stacks, Vec4 refPoint,
        float[] dest)
    {
        int numPoints = this.getRadialWallVertexCount(pillars, stacks);
        int numCoords = 3 * numPoints;

        if (terrain == null)
        {
            String message = Logging.getMessage("nullValue.TerrainIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pillars=" + pillars
                + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (refPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double a = angle;
        double dr = (outerRadius - innerRadius) / pillars;
        double globeRadius = terrain.getGlobe().getRadius();
        FloatBuffer destBuffer = FloatBuffer.wrap(dest);

        for (int s = 0; s <= stacks; s++)
        {
            for (int p = 0; p <= pillars; p++)
            {
                double r = (innerRadius + p * dr) / globeRadius;
                LatLon ll = LatLon.greatCircleEndPosition(center, a, r);
                this.append(terrain, ll, altitudes[s], terrainConformant[s], refPoint, destBuffer);
            }
        }
    }

    public void makeRadialWallVertices(float innerRadius, float outerRadius, float height, float angle,
        int pillars, int stacks, float[] dest)
    {
        int numPoints = this.getRadialWallVertexCount(pillars, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pillars=" + pillars
                + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y, z;
        float a, r;
        float dz, dr;
        int s, p;
        int index;

        a = angle;
        x = (float) Math.sin(a);
        y = (float) Math.cos(a);
        z = 0.0f;

        if (stacks != 0.0f)
            dz = height / (float) stacks;
        else
            dz = 0.0f;
        dr = (outerRadius - innerRadius) / (float) pillars;

        for (s = 0; s <= stacks; s++)
        {
            for (p = 0; p <= pillars; p++)
            {
                index = p + s * (pillars + 1);
                index = 3 * index;
                r = innerRadius + p * dr;
                dest[index] = r * x;
                dest[index + 1] = r * y;
                dest[index + 2] = z;
            }
            z += dz;
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void makeRadialWallNormals(float innerRadius, float outerRadius, float height, float angle,
        int pillars, int stacks, float[] dest)
    {
        int numPoints = this.getRadialWallVertexCount(pillars, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pillars=" + pillars
                + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a;
        int s, p;
        int index;
        float nsign;
        float[] norm;

        a = angle;
        x = (float) Math.cos(a);
        y = (float) -Math.sin(a);

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        norm[0] = x * nsign;
        norm[1] = y * nsign;
        norm[2] = 0.0f;
        this.norm3AndSet(norm, 0);

        for (s = 0; s <= stacks; s++)
        {
            for (p = 0; p <= pillars; p++)
            {
                index = p + s * (pillars + 1);
                index = 3 * index;
                System.arraycopy(norm, 0, dest, index, 3);
            }
        }
    }

    public void makeRadialWallIndices(int pillars, int stacks, int[] dest)
    {
        int numIndices = this.getRadialWallIndexCount(pillars, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pillars=" + pillars
                + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int p, s;
        int vertex, index;

        index = 0;
        for (s = 0; s < stacks; s++)
        {
            if (s != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = pillars + s * (pillars + 1);
                    dest[index++] = vertex;
                    vertex = s * (pillars + 1);
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = pillars + (s - 1) * (pillars + 1);
                    dest[index++] = vertex;
                    vertex = (s + 1) * (pillars + 1);
                    dest[index++] = vertex;
                }
            }
            for (p = 0; p <= pillars; p++)
            {
                vertex = p + s * (pillars + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + (pillars + 1);
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex + (pillars + 1);
                    dest[index++] = vertex;
                }
            }
        }
    }

    public void makeRadialWallOutlineIndices(int pillars, int stacks, int[] dest)
    {
        int numIndices = this.getRadialWallOutlineIndexCount(pillars, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pillars=" + pillars
                + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int vertex;
        int index = 0;
        // Bottom
        for (int i = 0; i < pillars; i++)
        {
            vertex = i;
            dest[index++] = vertex;
            dest[index++] = vertex + 1;
        }
        // Top
        for (int i = 0; i < pillars; i++)
        {
            vertex = i + stacks * (pillars + 1);
            dest[index++] = vertex;
            dest[index++] = vertex + 1;
        }
    }

    //**************************************************************//
    //********************  Long Cylinder       ********************//
    //**************************************************************//

    public int getLongCylinderVertexCount(int arcSlices, int lengthSlices, int stacks)
    {
        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        return slices * (stacks + 1);
    }

    public int getLongCylinderIndexCount(int arcSlices, int lengthSlices, int stacks)
    {
        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        return stacks * 2 * (slices + 1) + 2 * (stacks - 1);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getLongCylinderOutlineIndexCount(int arcSlices, int lengthSlices, int stacks)
    {
        return (arcSlices + lengthSlices) * 2 * 4;
    }

    public int getLongCylinderDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public int getLongCylinderOutlineDrawMode()
    {
        return GL.GL_LINES;
    }

    public LatLon[] makeLongCylinderLocations(Globe globe, LatLon center1, LatLon center2, double radius, int arcSlices,
        int lengthSlices)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center1 == null || center2 == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (arcSlices < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (lengthSlices < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "lengthSlices < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double az1 = LatLon.greatCircleAzimuth(center1, center2).radians;
        double az2 = LatLon.greatCircleAzimuth(center2, center1).radians;
        double len = LatLon.greatCircleDistance(center1, center2).radians;
        double r = radius / globe.getRadius();
        double da = Math.PI / arcSlices;
        double ds = len / lengthSlices;

        LatLon[] locations = new LatLon[lengthSlices];
        double[] azimuths = new double[lengthSlices];
        for (int i = 1; i < lengthSlices; i++)
        {
            double s = i * ds;
            locations[i] = LatLon.greatCircleEndPosition(center1, az1, s);
            azimuths[i] = LatLon.greatCircleAzimuth(locations[i], center1).radians;
        }

        int count = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        int index = 0;
        LatLon[] dest = new LatLon[count];

        for (int i = 0; i <= arcSlices; i++) // top arc
        {
            double a = i * da + az1 + (Math.PI / 2);
            dest[index++] = LatLon.greatCircleEndPosition(center1, a, r);
        }

        for (int i = 1; i < lengthSlices; i++) // right side
        {
            double a = azimuths[i] + (Math.PI / 2);
            dest[index++] = LatLon.greatCircleEndPosition(locations[i], a, r);
        }

        for (int i = 0; i <= arcSlices; i++) // bottom arc
        {
            double a = i * da + az2 + (Math.PI / 2);
            dest[index++] = LatLon.greatCircleEndPosition(center2, a, r);
        }

        for (int i = lengthSlices - 1; i >= 1; i--) // left side
        {
            double a = azimuths[i] - (Math.PI / 2);
            dest[index++] = LatLon.greatCircleEndPosition(locations[i], a, r);
        }

        return dest;
    }

    public void makeLongCylinderVertices(Terrain terrain, LatLon center1, LatLon center2, double radius,
        double[] altitudes, boolean[] terrainConformant, int arcSlices, int lengthSlices, int stacks,
        Vec4 refPoint, float[] dest)
    {
        int numPoints = this.getLongCylinderVertexCount(arcSlices, lengthSlices, stacks);
        int numCoords = 3 * numPoints;

        if (terrain == null)
        {
            String message = Logging.getMessage("nullValue.TerrainIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center1 == null || center2 == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                + " lengthSlices=" + lengthSlices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (refPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double az1 = LatLon.greatCircleAzimuth(center1, center2).radians;
        double az2 = LatLon.greatCircleAzimuth(center2, center1).radians;
        double len = LatLon.greatCircleDistance(center1, center2).radians;
        double r = radius / terrain.getGlobe().getRadius();
        double da = Math.PI / arcSlices;
        double ds = len / lengthSlices;
        FloatBuffer destBuffer = FloatBuffer.wrap(dest);

        LatLon[] locations = new LatLon[lengthSlices];
        double[] azimuths = new double[lengthSlices];
        for (int i = 1; i < lengthSlices; i++)
        {
            double s = i * ds;
            locations[i] = LatLon.greatCircleEndPosition(center1, az1, s);
            azimuths[i] = LatLon.greatCircleAzimuth(locations[i], center1).radians;
        }

        for (int j = 0; j <= stacks; j++)
        {
            for (int i = 0; i <= arcSlices; i++) // top arc
            {
                double a = i * da + az1 + (Math.PI / 2);
                LatLon ll = LatLon.greatCircleEndPosition(center1, a, r);
                this.append(terrain, ll, altitudes[j], terrainConformant[j], refPoint, destBuffer);
            }

            for (int i = 1; i < lengthSlices; i++) // right side
            {
                double a = azimuths[i] + (Math.PI / 2);
                LatLon ll = LatLon.greatCircleEndPosition(locations[i], a, r);
                this.append(terrain, ll, altitudes[j], terrainConformant[j], refPoint, destBuffer);
            }

            for (int i = 0; i <= arcSlices; i++) // bottom arc
            {
                double a = i * da + az2 + (Math.PI / 2);
                LatLon ll = LatLon.greatCircleEndPosition(center2, a, r);
                this.append(terrain, ll, altitudes[j], terrainConformant[j], refPoint, destBuffer);
            }

            for (int i = lengthSlices - 1; i >= 1; i--) // left side
            {
                double a = azimuths[i] - (Math.PI / 2);
                LatLon ll = LatLon.greatCircleEndPosition(locations[i], a, r);
                this.append(terrain, ll, altitudes[j], terrainConformant[j], refPoint, destBuffer);
            }
        }
    }

    public void makeLongCylinderVertices(float radius, float length, float height,
        int arcSlices, int lengthSlices, int stacks, float[] dest)
    {
        int numPoints = this.getLongCylinderVertexCount(arcSlices, lengthSlices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                + " lengthSlices=" + lengthSlices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y, z;
        float a;
        float dy, dz, da;
        int i, j;
        int index;

        da = (float) Math.PI / (float) arcSlices;
        dy = length / (float) lengthSlices;
        if (stacks != 0.0f)
            dz = height / (float) stacks;
        else
            dz = 0.0f;
        z = 0.0f;
        index = 0;

        for (j = 0; j <= stacks; j++)
        {
            // Top arc
            for (i = 0; i <= arcSlices; i++)
            {
                a = i * da + (3.0f * (float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * radius;
                dest[index++] = y * radius + length;
                dest[index++] = z;
            }
            // Right side.
            for (i = lengthSlices - 1; i >= 1; i--)
            {
                dest[index++] = radius;
                dest[index++] = i * dy;
                dest[index++] = z;
            }
            // Bottom arc
            for (i = 0; i <= arcSlices; i++)
            {
                a = i * da + ((float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * radius;
                dest[index++] = y * radius;
                dest[index++] = z;
            }
            // Left side.
            for (i = 1; i < lengthSlices; i++)
            {
                dest[index++] = -radius;
                dest[index++] = i * dy;
                dest[index++] = z;
            }
            z += dz;
        }
    }

    public void makeLongCylinderNormals(int arcSlices, int lengthSlices, int stacks, float[] dest)
    {
        int numPoints = this.getLongCylinderVertexCount(arcSlices, lengthSlices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                + " lengthSlices=" + lengthSlices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a, da;
        float nsign;
        int i, j;
        int index;

        da = (float) Math.PI / (float) arcSlices;
        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        index = 0;

        for (j = 0; j <= stacks; j++)
        {
            // Top arc
            for (i = 0; i <= arcSlices; i++)
            {
                a = i * da + (3.0f * (float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * nsign;
                dest[index++] = y * nsign;
                dest[index++] = 0.0f;
            }
            // Right side.
            for (i = lengthSlices - 1; i >= 1; i--)
            {
                //noinspection PointlessArithmeticExpression
                dest[index++] = 1.0f * nsign;
                dest[index++] = 0.0f;
                dest[index++] = 0.0f;
            }
            // Bottom arc
            for (i = 0; i <= arcSlices; i++)
            {
                a = i * da + ((float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * nsign;
                dest[index++] = y * nsign;
                dest[index++] = 0.0f;
            }
            // Left side.
            for (i = 1; i < lengthSlices; i++)
            {
                dest[index++] = -1.0f * nsign;
                dest[index++] = 0.0f;
                dest[index++] = 0.0f;
            }
        }
    }

    public void makeLongCylinderIndices(int arcSlices, int lengthSlices, int stacks, int[] dest)
    {
        int numIndices = this.getLongCylinderIndexCount(arcSlices, lengthSlices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                + " lengthSlices=" + lengthSlices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int slices;
        int i, j;
        int vertex, index;

        slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        index = 0;

        for (j = 0; j < stacks; j++)
        {
            if (j != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = (j - 1) * slices;
                    dest[index++] = vertex;
                    vertex = j * slices;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = (j - 1) * slices;
                    dest[index++] = vertex + slices;
                    vertex = (j - 1) * slices;
                    dest[index++] = vertex;
                }
            }
            for (i = 0; i <= slices; i++)
            {
                if (i == slices)
                    vertex = j * slices;
                else
                    vertex = i + j * slices;
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex + slices;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + slices;
                }
            }
        }
    }

    public void makeLongCylinderOutlineIndices(int arcSlices, int lengthSlices, int stacks, int[] dest)
    {
        int numIndices = this.getLongCylinderOutlineIndexCount(arcSlices, lengthSlices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                + " lengthSlices=" + lengthSlices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        int i;
        int vertex, index;

        index = 0;
        // Bottom ring
        for (i = 0; i < slices; i++)
        {
            vertex = i;
            dest[index++] = vertex;
            dest[index++] = (i != slices - 1) ? vertex + 1 : 0;
        }
        // Top ring
        for (i = 0; i < slices; i++)
        {
            vertex = i + slices * stacks;
            dest[index++] = vertex;
            dest[index++] = (i != slices - 1) ? vertex + 1 : slices * stacks;
        }
    }

    //**************************************************************//
    //********************  Long Disk           ********************//
    //**************************************************************//

    public int getLongDiskVertexCount(int arcSlices, int lengthSlices, int loops)
    {
        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        return slices * (loops + 1);
    }

    public int getLongDiskIndexCount(int arcSlices, int lengthSlices, int loops)
    {
        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        return loops * 2 * (slices + 1) + 2 * (loops - 1);
    }

    public int getLongDiskDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public LatLon[] makeLongDiskLocations(Globe globe, LatLon center1, LatLon center2, double innerRadius,
        double outerRadius, int arcSlices, int lengthSlices, int loops)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center1 == null || center2 == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (arcSlices < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (lengthSlices < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "lengthSlices < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (loops < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "loops < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double az1 = LatLon.greatCircleAzimuth(center1, center2).radians;
        double az2 = LatLon.greatCircleAzimuth(center2, center1).radians;
        double len = LatLon.greatCircleDistance(center1, center2).radians;
        double da = Math.PI / arcSlices;
        double ds = len / lengthSlices;
        double dr = (outerRadius - innerRadius) / loops;
        double globeRadius = globe.getRadius();

        LatLon[] locations = new LatLon[lengthSlices];
        double[] azimuths = new double[lengthSlices];
        for (int i = 1; i < lengthSlices; i++)
        {
            double s = i * ds;
            locations[i] = LatLon.greatCircleEndPosition(center1, az1, s);
            azimuths[i] = LatLon.greatCircleAzimuth(locations[i], center1).radians;
        }

        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        int count = slices * (loops + 1);
        int index = 0;
        LatLon[] dest = new LatLon[count];

        for (int l = 0; l <= loops; l++)
        {
            double r = (innerRadius + l * dr) / globeRadius;

            for (int i = 0; i <= arcSlices; i++) // top arc
            {
                double a = i * da + az1 + (Math.PI / 2);
                dest[index++] = LatLon.greatCircleEndPosition(center1, a, r);
            }

            for (int i = 1; i < lengthSlices; i++) // right side
            {
                double a = azimuths[i] + (Math.PI / 2);
                dest[index++] = LatLon.greatCircleEndPosition(locations[i], a, r);
            }

            for (int i = 0; i <= arcSlices; i++) // bottom arc
            {
                double a = i * da + az2 + (Math.PI / 2);
                dest[index++] = LatLon.greatCircleEndPosition(center2, a, r);
            }

            for (int i = lengthSlices - 1; i >= 1; i--) // left side
            {
                double a = azimuths[i] - (Math.PI / 2);
                dest[index++] = LatLon.greatCircleEndPosition(locations[i], a, r);
            }
        }

        return dest;
    }

    public void makeLongDiskVertices(Terrain terrain, LatLon center1, LatLon center2, double innerRadius,
        double outerRadius, double altitude, boolean terrainConformant, int arcSlices, int lengthSlices, int loops,
        Vec4 refPoint, float[] dest)
    {
        int numPoints = this.getLongDiskVertexCount(arcSlices, lengthSlices, loops);
        int numCoords = 3 * numPoints;

        if (terrain == null)
        {
            String message = Logging.getMessage("nullValue.TerrainIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center1 == null || center2 == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                + " lengthSlices=" + lengthSlices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (refPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double az1 = LatLon.greatCircleAzimuth(center1, center2).radians;
        double az2 = LatLon.greatCircleAzimuth(center2, center1).radians;
        double len = LatLon.greatCircleDistance(center1, center2).radians;
        double da = Math.PI / arcSlices;
        double ds = len / lengthSlices;
        double dr = (outerRadius - innerRadius) / loops;
        double globeRadius = terrain.getGlobe().getRadius();
        FloatBuffer destBuffer = FloatBuffer.wrap(dest);

        LatLon[] locations = new LatLon[lengthSlices];
        double[] azimuths = new double[lengthSlices];
        for (int i = 1; i < lengthSlices; i++)
        {
            double s = i * ds;
            locations[i] = LatLon.greatCircleEndPosition(center1, az1, s);
            azimuths[i] = LatLon.greatCircleAzimuth(locations[i], center1).radians;
        }

        for (int l = 0; l <= loops; l++)
        {
            double r = (innerRadius + l * dr) / globeRadius;

            for (int i = 0; i <= arcSlices; i++) // top arc
            {
                double a = i * da + az1 + (Math.PI / 2);
                LatLon ll = LatLon.greatCircleEndPosition(center1, a, r);
                this.append(terrain, ll, altitude, terrainConformant, refPoint, destBuffer);
            }

            for (int i = 1; i < lengthSlices; i++) // right side
            {
                double a = azimuths[i] + (Math.PI / 2);
                LatLon ll = LatLon.greatCircleEndPosition(locations[i], a, r);
                this.append(terrain, ll, altitude, terrainConformant, refPoint, destBuffer);
            }

            for (int i = 0; i <= arcSlices; i++) // bottom arc
            {
                double a = i * da + az2 + (Math.PI / 2);
                LatLon ll = LatLon.greatCircleEndPosition(center2, a, r);
                this.append(terrain, ll, altitude, terrainConformant, refPoint, destBuffer);
            }

            for (int i = lengthSlices - 1; i >= 1; i--) // left side
            {
                double a = azimuths[i] - (Math.PI / 2);
                LatLon ll = LatLon.greatCircleEndPosition(locations[i], a, r);
                this.append(terrain, ll, altitude, terrainConformant, refPoint, destBuffer);
            }
        }
    }

    public void makeLongDiskVertices(float innerRadius, float outerRadius, float length,
        int arcSlices, int lengthSlices, int loops, float[] dest)
    {
        int numPoints = this.getLongDiskVertexCount(arcSlices, lengthSlices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                + " lengthSlices=" + lengthSlices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a, r;
        float dy, da, dr;
        int s, l;
        int index;

        dy = length / (float) lengthSlices;
        da = (float) Math.PI / (float) arcSlices;
        dr = (outerRadius - innerRadius) / (float) loops;
        index = 0;

        for (l = 0; l <= loops; l++)
        {
            r = innerRadius + l * dr;
            // Top arc.
            for (s = 0; s <= arcSlices; s++)
            {
                a = s * da + (3.0f * (float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * r;
                dest[index++] = y * r + length;
                dest[index++] = 0.0f;
            }
            // Right side.
            for (s = lengthSlices - 1; s >= 1; s--)
            {
                dest[index++] = r;
                dest[index++] = s * dy;
                dest[index++] = 0.0f;
            }
            // Bottom arc.
            for (s = 0; s <= arcSlices; s++)
            {
                a = s * da + ((float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * r;
                dest[index++] = y * r;
                dest[index++] = 0.0f;
            }
            // Left side.
            for (s = 1; s < lengthSlices; s++)
            {
                dest[index++] = -r;
                dest[index++] = s * dy;
                dest[index++] = 0.0f;
            }
        }
    }

    public void makeLongDiskNormals(int arcSlices, int lengthSlices, int loops, float[] dest)
    {
        int numPoints = this.getLongDiskVertexCount(arcSlices, lengthSlices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                + " lengthSlices=" + lengthSlices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int slices;
        int s, l;
        int index;
        float nsign;
        float[] normal;

        slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        normal = new float[3];
        normal[0] = 0.0f;
        normal[1] = 0.0f;
        //noinspection PointlessArithmeticExpression
        normal[2] = 1.0f * nsign;

        for (l = 0; l <= loops; l++)
        {
            for (s = 0; s < slices; s++)
            {
                index = s + l * slices;
                index = 3 * index;
                System.arraycopy(normal, 0, dest, index, 3);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void makeLongDiskVertexNormals(float innerRadius, float outerRadius, float length,
        int arcSlices, int lengthSlices, int loops,
        float[] srcVerts, float[] dest)
    {
        int numPoints = this.getLongDiskVertexCount(arcSlices, lengthSlices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                + " lengthSlices=" + lengthSlices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (srcVerts == null)
        {
            String message = "nullValue.SourceVertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int slices;
        int s, l;
        int index;
        float nsign;
        float[] norm, zero, tmp;

        slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        zero = new float[3];
        tmp = new float[3];

        for (l = 0; l <= loops; l++)
        {
            // Normal vectors for first and last loops require a special case.
            if (l == 0 || l == loops)
            {
                // Closed disk: slices are collapsed.
                if (l == 0 && innerRadius == 0.0f)
                {
                    // Top arc.
                    {
                        // Compute common normal.
                        System.arraycopy(zero, 0, norm, 0, 3);
                        for (s = 0; s <= arcSlices; s++)
                        {
                            index = s;
                            this.facenorm(srcVerts, index, index + slices + 1, index + slices, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                        }
                        index = arcSlices;
                        this.facenorm(srcVerts, index, index + 1, index + slices, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        index = 0;
                        this.facenorm(srcVerts, index, index + slices, index + slices - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.mul3AndSet(norm, 0, nsign);
                        this.norm3AndSet(norm, 0);
                        // Copy common normal to the first point of each slice.
                        for (s = 0; s <= arcSlices; s++)
                        {
                            index = s;
                            System.arraycopy(norm, 0, dest, 3 * index, 3);
                        }
                    }
                    // Right and left sides.
                    {
                        int leftSideIndex;
                        for (s = 1; s < lengthSlices; s++)
                        {
                            // Compute common normal.
                            index = s + arcSlices;
                            leftSideIndex = slices - s;
                            System.arraycopy(zero, 0, norm, 0, 3);
                            this.facenorm(srcVerts, index, index + slices, index - 1, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                            this.facenorm(srcVerts, index, index + 1, index + slices, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                            if (s == 1)
                                this.facenorm(srcVerts, leftSideIndex, leftSideIndex - slices + 1,
                                    leftSideIndex + slices, tmp);
                            else
                                this.facenorm(srcVerts, leftSideIndex, leftSideIndex + 1, leftSideIndex + slices, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                            this.facenorm(srcVerts, leftSideIndex, leftSideIndex + slices, leftSideIndex - 1, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                            this.mul3AndSet(norm, 0, nsign);
                            this.norm3AndSet(norm, 0);
                            // Copy common normal to the first point of each slice.
                            System.arraycopy(norm, 0, dest, 3 * index, 3);
                            System.arraycopy(norm, 0, dest, 3 * leftSideIndex, 3);
                        }
                    }
                    // Bottom arc.
                    {
                        // Compute common normal.
                        System.arraycopy(zero, 0, norm, 0, 3);
                        for (s = 0; s <= arcSlices; s++)
                        {
                            index = s + arcSlices + lengthSlices;
                            this.facenorm(srcVerts, index, index + slices + 1, index + slices, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                        }
                        index = arcSlices + lengthSlices;
                        this.facenorm(srcVerts, index, index + slices, index - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        index = (2 * arcSlices) + lengthSlices;
                        this.facenorm(srcVerts, index, index + 1, index + slices, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.mul3AndSet(norm, 0, nsign);
                        this.norm3AndSet(norm, 0);
                        // Copy common normal to the first point of each slice.
                        for (s = 0; s <= arcSlices; s++)
                        {
                            index = s + arcSlices + lengthSlices;
                            System.arraycopy(norm, 0, dest, 3 * index, 3);
                        }
                    }
                }
                // Open disk: each slice has a unique starting point.
                else
                {
                    for (s = 0; s < slices; s++)
                    {
                        int prevSlice, nextSlice;
                        int adjacentLoop;
                        index = s + l * slices;
                        prevSlice = index - 1;
                        nextSlice = index + 1;

                        if (s == 0)
                            prevSlice = l * slices;
                        else if (s == slices - 1)
                            nextSlice = l;

                        if (l == 0)
                            adjacentLoop = index + slices;
                        else
                            adjacentLoop = index - slices;

                        System.arraycopy(zero, 0, norm, 0, 3);

                        // Add clockwise adjacent face.
                        if (l == 0)
                            this.facenorm(srcVerts, index, nextSlice, adjacentLoop, tmp);
                        else
                            this.facenorm(srcVerts, index, adjacentLoop, nextSlice, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        // Add counter-clockwise adjacent face.
                        if (l == 0)
                            this.facenorm(srcVerts, index, adjacentLoop, prevSlice, tmp);
                        else
                            this.facenorm(srcVerts, index, prevSlice, adjacentLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);

                        // Normalize and place in output.
                        this.mul3AndSet(norm, 0, nsign);
                        this.norm3AndSet(norm, 0);
                        System.arraycopy(norm, 0, dest, 3 * index, 3);
                    }
                }
            }
            // Normal vectors for internal loops.
            else
            {
                for (s = 0; s < slices; s++)
                {
                    int prevSlice, nextSlice;
                    int prevLoop, nextLoop;
                    index = s + l * slices;
                    prevSlice = index - 1;
                    nextSlice = index + 1;

                    if (s == 0)
                        prevSlice = (slices - 1) + l * slices;
                    else if (s == slices - 1)
                        nextSlice = l * slices;

                    prevLoop = index - slices;
                    nextLoop = index + slices;

                    System.arraycopy(zero, 0, norm, 0, 3);

                    // Add lower-left adjacent face.
                    this.facenorm(srcVerts, index, prevSlice, prevSlice - slices, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, prevSlice - slices, prevLoop, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add lower-right adjacent face.
                    this.facenorm(srcVerts, index, prevLoop, nextSlice - slices, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, nextSlice - slices, nextSlice, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add upper-right adjacent face.
                    this.facenorm(srcVerts, index, nextSlice, nextSlice + slices, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, nextSlice + slices, nextLoop, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add upper-left adjacent face.
                    this.facenorm(srcVerts, index, nextLoop, prevSlice + slices, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, prevSlice + slices, prevSlice, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);

                    // Normalize and place in output.
                    this.mul3AndSet(norm, 0, nsign);
                    this.norm3AndSet(norm, 0);
                    System.arraycopy(norm, 0, dest, 3 * index, 3);
                }
            }
        }
    }

    public void makeLongDiskIndices(int arcSlices, int lengthSlices, int loops, int[] dest)
    {
        int numIndices = this.getLongDiskIndexCount(arcSlices, lengthSlices, loops);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                + " lengthSlices=" + lengthSlices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int slices;
        int s, l;
        int vertex, index;

        slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        index = 0;

        for (l = 0; l < loops; l++)
        {
            if (l != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = (l - 1) * slices;
                    dest[index++] = vertex + slices;
                    vertex = (l - 1) * slices;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = (l - 1) * slices;
                    dest[index++] = vertex;
                    vertex = l * slices;
                    dest[index++] = vertex;
                }
            }
            for (s = 0; s <= slices; s++)
            {
                if (s == slices)
                    vertex = l * slices;
                else
                    vertex = s + l * slices;
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + slices;
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex + slices;
                    dest[index++] = vertex;
                }
            }
        }
    }

    //**************************************************************//
    //********************  Polygon                 ****************//
    //**************************************************************//

    public int computePolygonWindingOrder2(int pos, int count, Vec4[] points)
    {
        float area;
        int order;

        area = this.computePolygonArea2(pos, count, points);
        if (area < 0.0f)
            order = CLOCKWISE;
        else
            order = COUNTER_CLOCKWISE;

        return order;
    }

    public float computePolygonArea2(int pos, int count, Vec4[] points)
    {
        if (pos < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pos=" + pos);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (count < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "count=" + count);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (points == null)
        {
            String message = "nullValue.PointsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (points.length < (pos + count))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "points.length < " + (pos + count));
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float area;
        int i;
        int coord, nextCoord;

        area = 0.0f;
        for (i = 0; i < count; i++)
        {
            coord = pos + i;
            nextCoord = (i == count - 1) ? (pos) : (pos + i + 1);
            area += points[coord].x * points[nextCoord].y;
            area -= points[nextCoord].x * points[coord].y;
        }
        area /= 2.0f;

        return area;
    }

    public IndexedTriangleArray tessellatePolygon(int pos, int count, float[] vertices, Vec4 normal)
    {
        if (count < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "count=" + count);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.length < (pos + count))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        TessellatorCallback cb;
        GLUTessellatorSupport glts;
        double[] dvertices = new double[3 * count];
        int i;
        int srcIndex, destIndex;

        if (normal == null)
            normal = Vec4.UNIT_Z;

        cb = new TessellatorCallback(this, count, vertices);
        glts = new GLUTessellatorSupport();
        glts.beginTessellation(cb, normal);
        try
        {
            GLU.gluTessBeginPolygon(glts.getGLUtessellator(), null);
            GLU.gluTessBeginContour(glts.getGLUtessellator());
            for (i = 0; i < count; i++)
            {
                srcIndex = 3 * (pos + i);
                destIndex = 3 * i;
                dvertices[destIndex] = vertices[srcIndex];
                dvertices[destIndex + 1] = vertices[srcIndex + 1];
                dvertices[destIndex + 2] = vertices[srcIndex + 2];
                GLU.gluTessVertex(glts.getGLUtessellator(), dvertices, destIndex, pos + i);
            }
            GLU.gluTessEndContour(glts.getGLUtessellator());
            GLU.gluTessEndPolygon(glts.getGLUtessellator());
        }
        finally
        {
            glts.endTessellation();
        }

        return new IndexedTriangleArray(
            cb.getIndexCount(), cb.getIndices(),
            cb.getVertexCount(), cb.getVertices());
    }

    public IndexedTriangleArray tessellatePolygon2(int pos, int count, float[] vertices)
    {
        if (count < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "count=" + count);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.length < (pos + count))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.tessellatePolygon(pos, count, vertices, Vec4.UNIT_Z);
    }

    private static class TessellatorCallback extends GLUtessellatorCallbackAdapter
    {
        private GeometryBuilder gb;
        private int type;
        private int indexCount;
        private int primIndexCount;
        private int vertexCount;
        private int[] indices;
        private int[] primIndices;
        private float[] vertices;

        private TessellatorCallback(GeometryBuilder gb, int vertexCount, float[] vertices)
        {
            this.gb = gb;
            this.indexCount = 0;
            this.primIndexCount = 0;
            this.vertexCount = vertexCount;

            int initialCapacity = this.gb.nextPowerOfTwo(3 * vertexCount);
            this.indices = new int[initialCapacity];
            this.primIndices = new int[initialCapacity];
            this.vertices = this.gb.copyOf(vertices, initialCapacity);
        }

        public int getIndexCount()
        {
            return this.indexCount;
        }

        public int[] getIndices()
        {
            return this.indices;
        }

        public int getVertexCount()
        {
            return this.vertexCount;
        }

        public float[] getVertices()
        {
            return this.vertices;
        }

        protected void addTriangle(int i1, int i2, int i3)
        {
            // Triangle indices will be specified in counter-clockwise order. To reverse the ordering, we
            // swap the indices.

            int minCapacity, oldCapacity, newCapacity;

            minCapacity = this.indexCount + 3;
            oldCapacity = this.indices.length;
            while (minCapacity > oldCapacity)
            {
                newCapacity = 2 * oldCapacity;
                this.indices = this.gb.copyOf(this.indices, newCapacity);
                oldCapacity = minCapacity;
            }

            if (this.gb.orientation == GeometryBuilder.INSIDE)
            {
                this.indices[this.indexCount++] = this.primIndices[i1];
                this.indices[this.indexCount++] = this.primIndices[i3];
                this.indices[this.indexCount++] = this.primIndices[i2];
            }
            else // (this.gb.orientation == GeometryBuilder.OUTSIDE)
            {
                this.indices[this.indexCount++] = this.primIndices[i1];
                this.indices[this.indexCount++] = this.primIndices[i2];
                this.indices[this.indexCount++] = this.primIndices[i3];
            }
        }

        public void begin(int type)
        {
            this.type = type;
            this.primIndexCount = 0;
        }

        public void vertex(Object vertexData)
        {
            int minCapacity, oldCapacity, newCapacity;

            oldCapacity = this.primIndices.length;
            minCapacity = this.primIndexCount + 1;
            while (minCapacity > oldCapacity)
            {
                newCapacity = 2 * oldCapacity;
                this.primIndices = this.gb.copyOf(this.primIndices, newCapacity);
                oldCapacity = newCapacity;
            }

            int index = (Integer) vertexData;
            this.primIndices[this.primIndexCount++] = index;
        }

        public void end()
        {
            int i;

            if (this.type == GL.GL_TRIANGLES)
            {
                for (i = 2; i < this.primIndexCount; i++)
                {
                    if (((i + 1) % 3) == 0)
                        this.addTriangle(i - 2, i - 1, i);
                }
            }
            else if (this.type == GL.GL_TRIANGLE_STRIP)
            {
                for (i = 2; i < this.primIndexCount; i++)
                {
                    if ((i % 2) == 0)
                        this.addTriangle(i - 2, i - 1, i);
                    else
                        this.addTriangle(i - 1, i - 2, i);
                }
            }
            else if (this.type == GL.GL_TRIANGLE_FAN)
            {
                for (i = 2; i < this.primIndexCount; i++)
                {
                    this.addTriangle(0, i - 1, i);
                }
            }
        }

        public void combine(double[] coords, Object[] data, float[] weight, Object[] outData)
        {
            outData[0] = data[0];
        }
    }

    //**************************************************************//
    //********************  Indexed Triangle Buffer  ***************//
    //**************************************************************//

    public int getIndexedTriangleBufferDrawMode()
    {
        return GL.GL_TRIANGLES;
    }

    public static class IndexedTriangleBuffer
    {
        private IntBuffer indices;
        private FloatBuffer vertices;
        private int indexCount;
        private int vertexCount;

        public IndexedTriangleBuffer(int indexCount, IntBuffer indices, int vertexCount, FloatBuffer vertices)
        {
            this.indices = indices;
            this.vertices = vertices;
            this.indexCount = indexCount;
            this.vertexCount = vertexCount;
        }

        public int getIndexCount()
        {
            return this.indexCount;
        }

        public IntBuffer getIndices()
        {
            return this.indices;
        }

        public int getVertexCount()
        {
            return this.vertexCount;
        }

        public FloatBuffer getVertices()
        {
            return this.vertices;
        }
    }

    public void subdivideIndexedTriangleBuffer(IndexedTriangleBuffer itb)
    {
        if (itb == null)
        {
            String message = "nullValue.IndexedTriangleArray";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int indexCount;
        int a, b, c;
        int ab, bc, ca;
        int i, j;
        HashMap<Edge, Integer> edgeMap;
        Edge e;
        Integer split;

        indexCount = itb.getIndexCount();
        edgeMap = new HashMap<Edge, Integer>();

        // Iterate over each triangle, and split the edge of each triangle. Each edge is split exactly once. The
        // index of the new vertex created by a split is stored in edgeMap.
        for (i = 0; i < indexCount; i += 3)
        {
            for (j = 0; j < 3; j++)
            {
                a = itb.indices.get(i + j);
                b = itb.indices.get((j < 2) ? (i + j + 1) : i);
                e = new Edge(a, b);
                split = edgeMap.get(e);
                if (split == null)
                {
                    split = this.splitVertex(itb, a, b);
                    edgeMap.put(e, split);
                }
            }
        }

        // Iterate over each triangle, and create indices for four new triangles, replacing indices of the original
        // triangle.
        for (i = 0; i < indexCount; i += 3)
        {
            a = itb.indices.get(i);
            b = itb.indices.get(i + 1);
            c = itb.indices.get(i + 2);
            ab = edgeMap.get(new Edge(a, b));
            bc = edgeMap.get(new Edge(b, c));
            ca = edgeMap.get(new Edge(c, a));
            this.indexSplitTriangle(itb, i, a, b, c, ab, bc, ca);
        }
    }

    public void makeIndexedTriangleBufferNormals(IndexedTriangleBuffer itb, FloatBuffer dest)
    {
        if (itb == null)
        {
            String message = "nullValue.IndexedTriangleArray";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numCoords = 3 * itb.vertexCount;

        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.capacity() < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.capacity();
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.makeIndexedTriangleBufferNormals(0, itb.getIndexCount(), itb.getIndices(),
            0, itb.getVertexCount(), itb.getVertices(), dest);
    }

    public void makeIndexedTriangleBufferNormals(int indexPos, int indexCount, IntBuffer indices,
        int vertexPos, int vertexCount, FloatBuffer vertices,
        FloatBuffer dest)
    {
        if (indices == null)
        {
            String message = "nullValue.IndexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (indices.capacity() < (indexPos + indexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "indices.length=" + indices.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.capacity() < (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.capacity() < (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + dest.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, v;
        int index;
        float nsign;
        float[] norm;
        int[] faceIndices;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        faceIndices = new int[3];

        // Compute the normal for each face, contributing that normal to each vertex of the face.
        for (i = 0; i < indexCount; i += 3)
        {
            faceIndices[0] = indices.get(indexPos + i);
            faceIndices[1] = indices.get(indexPos + i + 1);
            faceIndices[2] = indices.get(indexPos + i + 2);
            // Compute the normal for this face.
            this.facenorm(vertices, faceIndices[0], faceIndices[1], faceIndices[2], norm);
            // Add this face normal to the normal at each vertex.
            for (v = 0; v < 3; v++)
            {
                index = 3 * faceIndices[v];
                this.add3AndSet(dest, index, norm, 0);
            }
        }

        // Scale and normalize each vertex normal.
        for (v = 0; v < vertexCount; v++)
        {
            index = 3 * (vertexPos + v);
            this.mul3AndSet(dest, index, nsign);
            this.norm3AndSet(dest, index);
        }

        dest.rewind();
    }

    private int splitVertex(IndexedTriangleBuffer itb, int a, int b)
    {
        int minCapacity, oldCapacity, newCapacity;

        oldCapacity = itb.vertices.capacity();
        minCapacity = 3 * (itb.getVertexCount() + 1);
        while (minCapacity > oldCapacity)
        {
            newCapacity = 2 * oldCapacity;
            itb.vertices = this.copyOf(itb.vertices, newCapacity);
            oldCapacity = newCapacity;
        }

        int s = itb.getVertexCount();
        int is = 3 * s;
        int ia = 3 * a;
        int ib = 3 * b;
        itb.vertices.put(is, (itb.vertices.get(ia) + itb.vertices.get(ib)) / 2.0f);
        itb.vertices.put(is + 1, (itb.vertices.get(ia + 1) + itb.vertices.get(ib + 1)) / 2.0f);
        itb.vertices.put(is + 2, (itb.vertices.get(ia + 2) + itb.vertices.get(ib + 2)) / 2.0f);
        itb.vertexCount++;

        return s;
    }

    public void makeEllipsoidNormals(IndexedTriangleBuffer itb, FloatBuffer dest)
    {
        if (itb == null)
        {
            String message = "nullValue.IndexedTriangleArray";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numCoords = 3 * itb.vertexCount;

        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.capacity() < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.capacity();
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.makeEllipsoidNormals(0, itb.getIndexCount(), itb.getIndices(),
            0, itb.getVertexCount(), itb.getVertices(), dest);
    }

    public void makeEllipsoidNormals(int indexPos, int indexCount, IntBuffer indices,
        int vertexPos, int vertexCount, FloatBuffer vertices,
        FloatBuffer dest)
    {
        if (indices == null)
        {
            String message = "nullValue.IndexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (indices.capacity() < (indexPos + indexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "indices.length=" + indices.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.capacity() < (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.capacity() < (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + dest.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, v;
        int index;
        float nsign;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;

        // for a sphere, normals are just the normalized vectors of the vertex positions

        // first copy all the vertices to the normals buffer
        for (i = 0; i < 3 * vertexCount; i++)
        {
            dest.put(i, vertices.get(i));
        }

        // Scale and normalize each vertex normal.
        for (v = 0; v < vertexCount; v++)
        {
            index = 3 * (vertexPos + v);
            this.mul3AndSet(dest, index, nsign);
            this.norm3AndSet(dest, index);
        }

        dest.rewind();
    }

    public void makeCylinderNormals(IndexedTriangleBuffer itb, FloatBuffer dest)
    {
        if (itb == null)
        {
            String message = "nullValue.IndexedTriangleArray";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numCoords = 3 * itb.vertexCount;

        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.capacity() < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.capacity();
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.makeCylinderNormals(0, itb.getIndexCount(), itb.getIndices(),
            0, itb.getVertexCount(), itb.getVertices(), dest);
    }

    public void makeCylinderNormals(int indexPos, int indexCount, IntBuffer indices,
        int vertexPos, int vertexCount, FloatBuffer vertices,
        FloatBuffer dest)
    {
        if (indices == null)
        {
            String message = "nullValue.IndexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (indices.capacity() < (indexPos + indexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "indices.length=" + indices.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.capacity() < (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.capacity() < (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + dest.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, v;
        int index;
        float nsign;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;

        // for a cylinder, normals are just the normalized vectors of the (x, y) coords of the vertex positions

        // first copy all the vertices to the normals buffer
        for (i = 0; i < 3 * vertexCount; i++)
        {
            if (i % 3 == 2)    // set z coord to zero
                dest.put(i, 0);
            else
                dest.put(i, -vertices.get(i));
        }

        // Scale and normalize each vertex normal.
        for (v = 0; v < vertexCount; v++)
        {
            index = 3 * (vertexPos + v);
            this.mul3AndSet(dest, index, nsign);
            this.norm3AndSet(dest, index);
        }

        dest.rewind();
    }

    private void indexSplitTriangle(IndexedTriangleBuffer itb, int original, int a, int b, int c, int ab, int bc,
        int ca)
    {
        int minCapacity, oldCapacity, newCapacity;

        // One of the new triangles will overwrite the original triangles, so we only need enough space to index
        // three new triangles.
        oldCapacity = itb.indices.capacity();
        minCapacity = itb.getIndexCount() + 9;
        while (minCapacity > oldCapacity)
        {
            newCapacity = 2 * oldCapacity;
            itb.indices = this.copyOf(itb.indices, newCapacity);
            oldCapacity = newCapacity;
        }

        // Lower-left triangle.
        // This triangle replaces the original.
        itb.indices.put(original, a);
        itb.indices.put(original + 1, ab);
        itb.indices.put(original + 2, ca);

        // Center triangle.
        itb.indices.put(itb.indexCount++, ab);
        itb.indices.put(itb.indexCount++, bc);
        itb.indices.put(itb.indexCount++, ca);

        // Lower-right triangle.
        itb.indices.put(itb.indexCount++, ab);
        itb.indices.put(itb.indexCount++, b);
        itb.indices.put(itb.indexCount++, bc);

        // Upper triangle.
        itb.indices.put(itb.indexCount++, ca);
        itb.indices.put(itb.indexCount++, bc);
        itb.indices.put(itb.indexCount++, c);
    }

    // This method finds triangles of the sphere mesh which span the discontinuity at 2 * PI radians.
    // It then creates a duplicate of the vertex in each of these triangles that is out of range, and uses this
    // duplicate to form the face (replacing the original vertex).  This way, the original vertex is used only
    // in faces that do not span the discontinuity, while faces that do span the discontinuity use the
    // duplicate instead.  When it comes time for texture mapping, a different texture coordinate can be
    // mapped to the duplicate vertex than to the original, each one falling in the correct range for the face(s) it
    // comprises.

    public void fixSphereSeam(IndexedTriangleBuffer itb, float wrapThreshold)
    {
        int vertex0, vertex1, vertex2;  // indices of the three vertices of the current face
        double x0, y0, x1, y1, x2, y2;   // actual x and y point values of those vertices
        double phi0, phi1, phi2;

        Integer newVertex, wrapVertex;
        int wrapIndex = -1;     //  track index of the vertex that will be replaced by a new "wrapped" vertex
        // keep track of newly created duplicate vertices:
        Map<Integer, Integer> duplicates = new HashMap<Integer, Integer>();

        // for each indexed triangle, determine if phi (longitude) of any of the vertices is on the
        // opposite side of 2PI from others (the "wrap" vertex)
        int indexCount = itb.getIndexCount();
        for (int i = 0; i < indexCount; i += 3)
        {
            vertex0 = itb.indices.get(i);
            vertex1 = itb.indices.get(i + 1);
            vertex2 = itb.indices.get(i + 2);

            x0 = itb.vertices.get(3 * vertex0);
            y0 = itb.vertices.get(3 * vertex0 + 1);
            x1 = itb.vertices.get(3 * vertex1);
            y1 = itb.vertices.get(3 * vertex1 + 1);
            x2 = itb.vertices.get(3 * vertex2);
            y2 = itb.vertices.get(3 * vertex2 + 1);

            // compute phi of each of the three vertices of the face
            phi0 = Math.atan2(y0, x0);
            if (phi0 < 0.0d)
                phi0 += 2.0d * Math.PI;

            phi1 = Math.atan2(y1, x1);
            if (phi1 < 0.0d)
                phi1 += 2.0d * Math.PI;

            phi2 = Math.atan2(y2, x2);
            if (phi2 < 0.0d)
                phi2 += 2.0d * Math.PI;

            // check if face spans phi = 0 (the texture seam), and determine which is the "wrapped" vertex
            if (Math.abs(phi0 - phi1) > wrapThreshold)
            {
                if (Math.abs(phi0 - phi2) > wrapThreshold)
                    wrapIndex = i;    // vertex0 is the wrapped vertex
                else
                    wrapIndex = i + 1;   // vertex1 is the wrapped vertex
            }
            else if (Math.abs(phi1 - phi2) > wrapThreshold)
                wrapIndex = i + 2;   // vertex2 is the wrapped vertex

            if (wrapIndex >= 0)  // check if one of the vertices on this face wrapped across 2PI
            {
                wrapVertex = itb.indices.get(wrapIndex);
                //look to see if this vertex has been duplicated already
                newVertex = duplicates.get(wrapVertex);
                if (newVertex != null)
                    itb.indices.put(wrapIndex, newVertex);   // replace the old vertex with the duplicate
                else
                {
                    // create a duplicate of the wrapIndex vertex and get its index newVertex
                    newVertex = duplicateVertex(itb, wrapVertex);
                    // place the new vertex in the duplicates structure
                    duplicates.put(wrapVertex, newVertex);
                    // now replace the index at the wrapIndex with the index of the new duplicate
                    itb.indices.put(wrapIndex, newVertex);
                }
                wrapIndex = -1;     // reset the wrapVertex
            }
        }
    }

    // append copy of vertex at sourceIndex to end of vertices buffer

    private int duplicateVertex(IndexedTriangleBuffer itb, int sourceIndex)
    {
        if (itb == null)
        {
            String message = Logging.getMessage("nullValue.IndexedTriangleBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (sourceIndex >= itb.vertexCount)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "sourceIndex > vertexCount");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // ensure there is room in the vertex buffer for one new vertex
        int minCapacity, oldCapacity, newCapacity;

        oldCapacity = itb.vertices.capacity();
        minCapacity = 3 * itb.getVertexCount() + 3;
        while (minCapacity > oldCapacity)
        {
            newCapacity = 2 * oldCapacity;
            itb.vertices = this.copyOf(itb.vertices, newCapacity);
            oldCapacity = newCapacity;
        }

        int destIndex = itb.getVertexCount();

        // append vertex data to vertices buffer
        itb.vertices.put(3 * destIndex, itb.vertices.get(3 * sourceIndex));
        itb.vertices.put(3 * destIndex + 1, itb.vertices.get(3 * sourceIndex + 1));
        itb.vertices.put(3 * destIndex + 2, itb.vertices.get(3 * sourceIndex + 2));

        itb.vertexCount++;
        // return index of the newly created vertex
        return itb.vertexCount - 1;
    }

    public void makeUnitSphereTextureCoordinates(IndexedTriangleBuffer itb, FloatBuffer texCoords)
    {
        if (itb == null)
        {
            String message = "nullValue.IndexedTriangleArray";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numCoords = 2 * itb.vertexCount;

        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (texCoords.capacity() < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + texCoords.capacity();
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.makeUnitSphereTextureCoordinates(itb.getVertexCount(), itb.getVertices(), texCoords, -1);
    }

    // allow for correction of seam caused by triangles that wrap across tecture bounds

    public void makeUnitSphereTextureCoordinates(IndexedTriangleBuffer itb, FloatBuffer texCoords,
        int seamVerticesIndex)
    {
        if (itb == null)
        {
            String message = "nullValue.IndexedTriangleArray";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numCoords = 2 * itb.vertexCount;

        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (texCoords.capacity() < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + texCoords.capacity();
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.makeUnitSphereTextureCoordinates(itb.getVertexCount(), itb.getVertices(),
            texCoords, seamVerticesIndex);
    }

    public void makeUnitSphereTextureCoordinates(int vertexCount, FloatBuffer vertices,
        FloatBuffer texCoords, int seamVerticesIndex)
    {
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.capacity() < 3 * vertexCount)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (texCoords.capacity() < 2 * vertexCount)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + texCoords.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i;
        double x, y, z;
        double theta, phi, u, v;

        // compute uv texture coordinates for each vertex and place them in the texCoords buffer.
        for (i = 0; i < vertexCount; i++)
        {
            x = vertices.get(3 * i);
            y = vertices.get(3 * i + 1);
            z = vertices.get(3 * i + 2);

            phi = Math.atan2(y, x);
            theta = Math.acos(z);

            if (phi < 0.0d)
                phi += 2.0d * Math.PI;  // shift phi to be in [0, 2*PI]

            u = phi / (2.0d * Math.PI);
            v = (Math.PI - theta) / Math.PI;

            texCoords.put(2 * i, (float) u);
            texCoords.put(2 * i + 1, (float) v);
        }

        if (seamVerticesIndex > 0)          // if the seam of the sphere was fixed
        {
            for (i = seamVerticesIndex; i < vertexCount; i++)
            {
                // wrap u (phi) texCoord for all the duplicated vertices
                u = texCoords.get(2 * i);
                if (u < 0.5)
                    texCoords.put(2 * i, (float) u + 1);
                else
                    texCoords.put(2 * i, (float) u - 1);
            }
        }
        texCoords.rewind();
    }

    // single texture version
    public void makeUnitBoxTextureCoordinates(FloatBuffer texCoords, int vertexCount)
    {
        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (texCoords.capacity() < 2 * vertexCount)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + texCoords.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // create uv texture coordinates for each of the 6 box faces and place them in the texCoords buffer.
        for (int i = 0; i < vertexCount; i += 4)
        {
            // V0 (upper left)
            texCoords.put(2 * i, 0);
            texCoords.put(2 * i + 1, 1);
            // V1 (upper right)
            texCoords.put(2 * i + 2, 1);
            texCoords.put(2 * i + 3, 1);
            // V2 (lower left)
            texCoords.put(2 * i + 4, 0);
            texCoords.put(2 * i + 5, 0);
            // V3 (lower right)
            texCoords.put(2 * i + 6, 1);
            texCoords.put(2 * i + 7, 0);
        }

        texCoords.rewind();
    }

    // multi-texture version
    public void makeUnitBoxTextureCoordinates(int index, FloatBuffer texCoords, int vertexCount)
    {
        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (texCoords.capacity() < 2 * vertexCount)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + texCoords.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // create uv texture coordinates for each of the 6 box faces and place them in the texCoords buffer.
        for (int i = 0; i < vertexCount; i += 4)
        {
            // V0 (upper left)
            texCoords.put(2 * i, 0);
            texCoords.put(2 * i + 1, 1);
            // V1 (upper right)
            texCoords.put(2 * i + 2, 1);
            texCoords.put(2 * i + 3, 1);
            // V2 (lower left)
            texCoords.put(2 * i + 4, 0);
            texCoords.put(2 * i + 5, 0);
            // V3 (lower right)
            texCoords.put(2 * i + 6, 1);
            texCoords.put(2 * i + 7, 0);
        }

        texCoords.rewind();
    }

    public void makeUnitPyramidTextureCoordinates(FloatBuffer texCoords, int vertexCount)
    {
        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (texCoords.capacity() < 2 * vertexCount)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + texCoords.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // create uv texture coordinates for each of the 4 pyramid faces and for the base, and place them
        // in the texCoords buffer.

        int i;
        for (i = 0; i < vertexCount - 4; i += 3) // create texture coords for the 4 sides of the pyramid first
        {
            // V0 (point)
            texCoords.put(2 * i, 0.5f);
            texCoords.put(2 * i + 1, 1);
            // V1 (lower left)
            texCoords.put(2 * i + 2, 0);
            texCoords.put(2 * i + 3, 0);
            // V2 (lower right)
            texCoords.put(2 * i + 4, 1);
            texCoords.put(2 * i + 5, 0);
        }

        // then create coords for the base

        // V0 (upper left)
        texCoords.put(2 * i, 0);
        texCoords.put(2 * i + 1, 1);
        // V1 (upper right)
        texCoords.put(2 * i + 2, 1);
        texCoords.put(2 * i + 3, 1);
        // V2 (lower left)
        texCoords.put(2 * i + 4, 0);
        texCoords.put(2 * i + 5, 0);
        // V3 (lower right)
        texCoords.put(2 * i + 6, 1);
        texCoords.put(2 * i + 7, 0);

        texCoords.rewind();
    }

    public void makeUnitPyramidTextureCoordinates(int index, FloatBuffer texCoords, int vertexCount)
    {
        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (texCoords.capacity() < 2 * vertexCount)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + texCoords.capacity());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // create uv texture coordinates for either one of the 4 pyramid faces or for the base, and place them
        // in the texCoords buffer.

        int i = 0;
        if (index == 4)  // pyramid base
        {
            // V0 (upper left)
            texCoords.put(2 * i, 0);
            texCoords.put(2 * i + 1, 1);
            // V1 (upper right)
            texCoords.put(2 * i + 2, 1);
            texCoords.put(2 * i + 3, 1);
            // V2 (lower left)
            texCoords.put(2 * i + 4, 0);
            texCoords.put(2 * i + 5, 0);
            // V3 (lower right)
            texCoords.put(2 * i + 6, 1);
            texCoords.put(2 * i + 7, 0);
        }
        else    // pyramid side
        {
            for (i = 0; i < vertexCount; i += 3) // create texture coords for the 4 sides of the pyramid first
            {
                // V0 (point)
                texCoords.put(2 * i, 0.5f);
                texCoords.put(2 * i + 1, 1);
                // V1 (lower left)
                texCoords.put(2 * i + 2, 0);
                texCoords.put(2 * i + 3, 0);
                // V2 (lower right)
                texCoords.put(2 * i + 4, 1);
                texCoords.put(2 * i + 5, 0);
            }
        }

        texCoords.rewind();
    }

    public void makeUnitCylinderTextureCoordinates(int face, FloatBuffer texCoords, int subdivisions)
    {
        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // create uv texture coordinates for the cylinder top, bottom and core, and place them
        // in the texCoords buffer.

        int i, index;
        float x, y, z, u, v, a, phi;

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = 2.0f * (float) Math.PI / (float) slices;

        if (face == 2)      // cylinder core
        {
            int coreTexIndex = 0;

            for (i = 0; i < slices; i++)
            {
                a = i * da;

                // cylinder core top rim
                u = 1 - a / (float) (2 * Math.PI);

                texCoords.put(coreTexIndex, u);
                texCoords.put(coreTexIndex + 1, 1);

                // cylinder core bottom rim
                texCoords.put(coreTexIndex + 2, u);
                texCoords.put(coreTexIndex + 3, 0);

                coreTexIndex += 4;
            }

            // close the texture seam
            texCoords.put(coreTexIndex, 0);
            texCoords.put(coreTexIndex + 1, 1);

            texCoords.put(coreTexIndex + 2, 0);
            texCoords.put(coreTexIndex + 3, 0);
        }
        else                // cylinder top or bottom
        {
            // center point
            texCoords.put(0, 0.5f);
            texCoords.put(1, 0.5f);

            // perimeter points
            for (i = 0; i < slices; i++)
            {
                a = i * da;
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);

                u = x / 2 + 0.5f;
                v = y / 2 + 0.5f;

                if (face == 1)   // Cylinder bottom
                    u = 1 - u;

                texCoords.put(2 * (i + 1), u);
                texCoords.put(2 * (i + 1) + 1, v);
            }
        }

        texCoords.rewind();
    }

    public void makeWedgeTextureCoordinates(FloatBuffer texCoords, int subdivisions, Angle angle)
    {
        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // create uv texture coordinates for the wedge top, bottom, core and sides, and place them
        // in the texCoords buffer.

        int i, index;
        float x, y, u, v, a;

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = (float) angle.getRadians() / slices;
        int coreTexIndex = 4 * (slices + 2);

        // center points
        texCoords.put(0, 0.5f);
        texCoords.put(1, 0.5f);

        texCoords.put(2 * (slices + 2), 0.5f);
        texCoords.put(2 * (slices + 2) + 1, 0.5f);

        for (i = 0; i <= slices; i++)
        {
            a = i * da;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);

            u = x / 2 + 0.5f;
            v = y / 2 + 0.5f;

            // wedge top
            texCoords.put(2 * (i + 1), u);
            texCoords.put(2 * (i + 1) + 1, v);

            // wedge bottom
            texCoords.put(2 * (slices + i + 3), 1 - u);
            texCoords.put(2 * (slices + i + 3) + 1, v);

            // wedge core top rim
            u = 1 - a / (float) (2 * Math.PI);

            texCoords.put(coreTexIndex, u);
            texCoords.put(coreTexIndex + 1, 1);

            // wedge core bottom rim
            texCoords.put(coreTexIndex + 2, u);
            texCoords.put(coreTexIndex + 3, 0);

            coreTexIndex += 4;
        }

        // wedge sides
        for (i = 0; i < 2; i++)
        {
            index = 2 * (4 * (slices + 1 + i) + 2);

            // inner points
            texCoords.put(index, 0);
            texCoords.put(index + 1, 1);

            texCoords.put(index + 2, 0);
            texCoords.put(index + 3, 0);

            // outer points
            texCoords.put(index + 4, 1);
            texCoords.put(index + 5, 1);

            texCoords.put(index + 6, 1);
            texCoords.put(index + 7, 0);
        }

        texCoords.rewind();
    }

    public void makeUnitWedgeTextureCoordinates(int face, FloatBuffer texCoords, int subdivisions, Angle angle)
    {
        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // create uv texture coordinates for the wedge top, bottom, core and sides, and place them
        // in the texCoords buffer.

        int i, index;
        float x, y, u, v, a;

        // face 0 = top
        // face 1 = bottom
        // face 2 = round core wall
        // face 3 = first wedge side
        // face 4 = second wedge side

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = (float) angle.getRadians() / slices;

        if (face == 0 || face == 1)   // wedge top or bottom
        {
            // center point
            texCoords.put(0, 0.5f);
            texCoords.put(1, 0.5f);

            for (i = 0; i <= slices; i++)
            {
                a = i * da;
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);

                u = x / 2 + 0.5f;
                v = y / 2 + 0.5f;

                if (face == 1)   // wedge bottom
                    u = 1 - u;

                // rim point
                texCoords.put(2 * (i + 1), u);
                texCoords.put(2 * (i + 1) + 1, v);
            }
        }
        else if (face == 2)   // wedge core
        {
            int coreTexIndex = 0;

            for (i = 0; i <= slices; i++)
            {
                a = i * da;

                // cylinder core top rim
                u = 1 - a / (float) (2 * Math.PI);

                texCoords.put(coreTexIndex, u);
                texCoords.put(coreTexIndex + 1, 1);

                // cylinder core bottom rim
                texCoords.put(coreTexIndex + 2, u);
                texCoords.put(coreTexIndex + 3, 0);

                coreTexIndex += 4;
            }
        }
        else if (face == 3)     // west-facing wedge side
        {
            // inner points
            texCoords.put(0, 1);
            texCoords.put(1, 1);

            texCoords.put(2, 1);
            texCoords.put(3, 0);

            // outer points
            texCoords.put(4, 0);
            texCoords.put(5, 1);

            texCoords.put(6, 0);
            texCoords.put(7, 0);
        }
        else if (face == 4)     // adjustable wedge side
        {
            // inner points
            texCoords.put(0, 0);
            texCoords.put(1, 1);

            texCoords.put(2, 0);
            texCoords.put(3, 0);

            // outer points
            texCoords.put(4, 1);
            texCoords.put(5, 1);

            texCoords.put(6, 1);
            texCoords.put(7, 0);
        }

        texCoords.rewind();
    }

    public void makeUnitConeTextureCoordinates(FloatBuffer texCoords, int subdivisions)
    {
        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // create uv texture coordinates for the cone bottom and core, and place them
        // in the texCoords buffer.

        int i, index;
        float x, y, z, u, v, a, phi;

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = 2.0f * (float) Math.PI / (float) slices;
        int coreTexIndex = 2 * (slices + 1);

        // center point
        texCoords.put(0, 0.5f);
        texCoords.put(1, 0.5f);

        for (i = 0; i < slices; i++)
        {
            a = i * da;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);

            u = x / 2 + 0.5f;
            v = y / 2 + 0.5f;

            // cone bottom
            texCoords.put(2 * (i + 1), 1 - u);
            texCoords.put(2 * (i + 1) + 1, v);

            // cone core top rim
            u = 1 - a / (float) (2 * Math.PI);

            texCoords.put(coreTexIndex, u);
            texCoords.put(coreTexIndex + 1, 1);

            // cone core bottom rim
            texCoords.put(coreTexIndex + 2, u);
            texCoords.put(coreTexIndex + 3, 0);

            coreTexIndex += 4;
        }

        // close the texture seam
        texCoords.put(coreTexIndex, 0);
        texCoords.put(coreTexIndex + 1, 1);

        texCoords.put(coreTexIndex + 2, 0);
        texCoords.put(coreTexIndex + 3, 0);

        texCoords.rewind();
    }

    public void makeUnitConeTextureCoordinates(int face, FloatBuffer texCoords, int subdivisions)
    {
        if (texCoords == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // create uv texture coordinates for the cone base and core, and place them
        // in the texCoords buffer.

        int i, index;
        float x, y, z, u, v, a, phi;

        int slices = (int) Math.pow(2, 2 + subdivisions);
        float da = 2.0f * (float) Math.PI / (float) slices;

        if (face == 1)      // cone core
        {
            int coreTexIndex = 0;

            for (i = 0; i < slices; i++)
            {
                a = i * da;

                // cone core top rim
                u = 1 - a / (float) (2 * Math.PI);

                texCoords.put(coreTexIndex, u);
                texCoords.put(coreTexIndex + 1, 1);

                // core bottom rim
                texCoords.put(coreTexIndex + 2, u);
                texCoords.put(coreTexIndex + 3, 0);

                coreTexIndex += 4;
            }

            // close the texture seam
            texCoords.put(coreTexIndex, 0);
            texCoords.put(coreTexIndex + 1, 1);

            texCoords.put(coreTexIndex + 2, 0);
            texCoords.put(coreTexIndex + 3, 0);
        }
        else if (face == 0)               // cone base
        {
            // center point
            texCoords.put(0, 0.5f);
            texCoords.put(1, 0.5f);

            // perimeter points
            for (i = 0; i < slices; i++)
            {
                a = i * da;
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);

                u = x / 2 + 0.5f;
                v = y / 2 + 0.5f;

                texCoords.put(2 * (i + 1), 1 - u);
                texCoords.put(2 * (i + 1) + 1, v);
            }
        }

        texCoords.rewind();
    }

    //**************************************************************//
    //********************  Indexed Triangle Array  ****************//
    //**************************************************************//

    public int getIndexedTriangleArrayDrawMode()
    {
        return GL.GL_TRIANGLES;
    }

    public static class IndexedTriangleArray
    {
        private int indexCount;
        private int vertexCount;
        private int[] indices;
        private float[] vertices;

        public IndexedTriangleArray(int indexCount, int[] indices, int vertexCount, float[] vertices)
        {
            this.indexCount = indexCount;
            this.indices = indices;
            this.vertexCount = vertexCount;
            this.vertices = vertices;
        }

        public int getIndexCount()
        {
            return this.indexCount;
        }

        public int[] getIndices()
        {
            return this.indices;
        }

        public int getVertexCount()
        {
            return this.vertexCount;
        }

        public float[] getVertices()
        {
            return this.vertices;
        }
    }

    public void subdivideIndexedTriangleArray(IndexedTriangleArray ita)
    {
        if (ita == null)
        {
            String message = "nullValue.IndexedTriangleArray";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int indexCount;
        int a, b, c;
        int ab, bc, ca;
        int i, j;
        HashMap<Edge, Integer> edgeMap;
        Edge e;
        Integer split;

        indexCount = ita.indexCount;
        edgeMap = new HashMap<Edge, Integer>();

        // Iterate over each triangle, and split the edge of each triangle. Each edge is split exactly once. The
        // index of the new vertex created by a split is stored in edgeMap.
        for (i = 0; i < indexCount; i += 3)
        {
            for (j = 0; j < 3; j++)
            {
                a = ita.indices[i + j];
                b = ita.indices[(j < 2) ? (i + j + 1) : i];
                e = new Edge(a, b);
                split = edgeMap.get(e);
                if (split == null)
                {
                    split = this.splitVertex(ita, a, b);
                    edgeMap.put(e, split);
                }
            }
        }

        // Iterate over each triangle, and create indices for four new triangles, replacing indices of the original
        // triangle.
        for (i = 0; i < indexCount; i += 3)
        {
            a = ita.indices[i];
            b = ita.indices[i + 1];
            c = ita.indices[i + 2];
            ab = edgeMap.get(new Edge(a, b));
            bc = edgeMap.get(new Edge(b, c));
            ca = edgeMap.get(new Edge(c, a));
            this.indexSplitTriangle(ita, i, a, b, c, ab, bc, ca);
        }
    }

    public IndexedTriangleArray subdivideIndexedTriangles(int indexCount, int[] indices,
        int vertexCount, float[] vertices)
    {
        int numCoords = 3 * vertexCount;

        if (indices == null)
        {
            String message = "nullValue.IndexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (indices.length < indexCount)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "indices.length=" + indices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.length < numCoords)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        IndexedTriangleArray ita = new IndexedTriangleArray(indexCount, indices, vertexCount, vertices);
        this.subdivideIndexedTriangleArray(ita);

        return ita;
    }

    public void makeIndexedTriangleArrayNormals(IndexedTriangleArray ita, float[] dest)
    {
        if (ita == null)
        {
            String message = "nullValue.IndexedTriangleArray";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numCoords = 3 * ita.vertexCount;

        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.makeIndexedTriangleArrayNormals(0, ita.indexCount, ita.indices, 0, ita.vertexCount, ita.vertices, dest);
    }

    public void makeIndexedTriangleArrayNormals(int indexPos, int indexCount, int[] indices,
        int vertexPos, int vertexCount, float[] vertices,
        float[] dest)
    {
        if (indices == null)
        {
            String message = "nullValue.IndexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (indices.length < (indexPos + indexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "indices.length=" + indices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.length < (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + dest.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, v;
        int index;
        float nsign;
        float[] norm;
        int[] faceIndices;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        faceIndices = new int[3];

        // Compute the normal for each face, contributing that normal to each vertex of the face.
        for (i = 0; i < indexCount; i += 3)
        {
            faceIndices[0] = indices[indexPos + i];
            faceIndices[1] = indices[indexPos + i + 1];
            faceIndices[2] = indices[indexPos + i + 2];
            // Compute the normal for this face.
            this.facenorm(vertices, faceIndices[0], faceIndices[1], faceIndices[2], norm);
            // Add this face normal to the normal at each vertex.
            for (v = 0; v < 3; v++)
            {
                index = 3 * faceIndices[v];
                this.add3AndSet(dest, index, norm, 0);
            }
        }

        // Scale and normalize each vertex normal.
        for (v = 0; v < vertexCount; v++)
        {
            index = 3 * (vertexPos + v);
            this.mul3AndSet(dest, index, nsign);
            this.norm3AndSet(dest, index);
        }
    }

    public void makeIndexedTriangleStripNormals(int indexPos, int indexCount, int[] indices,
        int vertexPos, int vertexCount, float[] vertices,
        float[] dest)
    {
        if (indices == null)
        {
            String message = "nullValue.IndexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (indices.length < indexPos + indexCount)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "indices.length=" + indices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.length < 3 * (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < 3 * (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + dest.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, v;
        int index;
        float nsign;
        float[] norm;
        int[] faceIndices;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        faceIndices = new int[3];

        // Compute the normal for each face, contributing that normal to each vertex of the face.
        for (i = 2; i < indexCount; i++)
        {
            if ((i % 2) == 0)
            {
                faceIndices[0] = indices[indexPos + i - 2];
                faceIndices[1] = indices[indexPos + i - 1];
                faceIndices[2] = indices[indexPos + i];
            }
            else
            {
                faceIndices[0] = indices[indexPos + i - 1];
                faceIndices[1] = indices[indexPos + i - 2];
                faceIndices[2] = indices[indexPos + i];
            }
            // Compute the normal for this face.
            this.facenorm(vertices, faceIndices[0], faceIndices[1], faceIndices[2], norm);
            // Add this face normal to the normal at each vertex.
            for (v = 0; v < 3; v++)
            {
                index = 3 * faceIndices[v];
                this.add3AndSet(dest, index, norm, 0);
            }
        }

        // Scale and normalize each vertex normal.
        for (v = 0; v < vertexCount; v++)
        {
            index = 3 * (vertexPos + v);
            this.mul3AndSet(dest, index, nsign);
            this.norm3AndSet(dest, index);
        }
    }

    private int splitVertex(IndexedTriangleArray ita, int a, int b)
    {
        int minCapacity, oldCapacity, newCapacity;

        oldCapacity = ita.vertices.length;
        minCapacity = 3 * (ita.vertexCount + 1);
        while (minCapacity > oldCapacity)
        {
            newCapacity = 2 * oldCapacity;
            ita.vertices = this.copyOf(ita.vertices, newCapacity);
            oldCapacity = newCapacity;
        }

        int s = ita.vertexCount;
        int is = 3 * s;
        int ia = 3 * a;
        int ib = 3 * b;
        ita.vertices[is] = (ita.vertices[ia] + ita.vertices[ib]) / 2.0f;
        ita.vertices[is + 1] = (ita.vertices[ia + 1] + ita.vertices[ib + 1]) / 2.0f;
        ita.vertices[is + 2] = (ita.vertices[ia + 2] + ita.vertices[ib + 2]) / 2.0f;
        ita.vertexCount++;

        return s;
    }

    private void indexSplitTriangle(IndexedTriangleArray ita, int original, int a, int b, int c, int ab, int bc, int ca)
    {
        int minCapacity, oldCapacity, newCapacity;

        // One of the new triangles will overwrite the original triangles, so we only need enough space to index
        // three new triangles.
        oldCapacity = ita.indices.length;
        minCapacity = ita.indexCount + 9;
        while (minCapacity > oldCapacity)
        {
            newCapacity = 2 * oldCapacity;
            ita.indices = this.copyOf(ita.indices, newCapacity);
            oldCapacity = newCapacity;
        }

        // Lower-left triangle.
        // This triangle replaces the original.
        ita.indices[original] = a;
        ita.indices[original + 1] = ab;
        ita.indices[original + 2] = ca;

        // Center triangle.
        ita.indices[ita.indexCount++] = ab;
        ita.indices[ita.indexCount++] = bc;
        ita.indices[ita.indexCount++] = ca;

        // Lower-right triangle.
        ita.indices[ita.indexCount++] = ab;
        ita.indices[ita.indexCount++] = b;
        ita.indices[ita.indexCount++] = bc;

        // Upper triangle.
        ita.indices[ita.indexCount++] = ca;
        ita.indices[ita.indexCount++] = bc;
        ita.indices[ita.indexCount++] = c;
    }

    private static class Edge
    {
        public final int a;
        public final int b;

        public Edge(int a, int b)
        {
            this.a = a;
            this.b = b;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            // Compares a non directed edge between two points. Therefore we must treat edge equivalence as
            // edge(ab)=edge(ab) OR edge(ab)=edge(ba).
            Edge that = (Edge) o;
            return (this.a == that.a && this.b == that.b)
                || (this.a == that.b && this.b == that.a);
        }

        public int hashCode()
        {
            // Represents the hash for a a non directed edge between two points. Therefore we use a non-commutative
            // hash so that hash(ab)=hash(ba).
            return this.a + this.b;
        }
    }

    //**************************************************************//
    //********************  Subdivision Points  ********************//
    //**************************************************************//

    public int getSubdivisionPointsVertexCount(int subdivisions)
    {
        return (1 << subdivisions) + 1;
    }

    public void makeSubdivisionPoints(float x1, float y1, float z1, float x2, float y2, float z2,
        int subdivisions, float[] dest)
    {
        int numPoints = this.getSubdivisionPointsVertexCount(subdivisions);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions=" + subdivisions);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int first, last;
        int index;

        first = 0;
        last = numPoints - 1;

        index = 3 * first;
        dest[index] = x1;
        dest[index + 1] = y1;
        dest[index + 2] = z1;

        index = 3 * last;
        dest[index] = x2;
        dest[index + 1] = y2;
        dest[index + 2] = z2;

        this.subdivide(x1, y1, z1, x2, y2, z2, subdivisions, dest, first, last);
    }

    private void subdivide(float x1, float y1, float z1, float x2, float y2, float z2, int subdivisions,
        float[] dest, int first, int last)
    {
        float x, y, z;
        int mid, index;

        if (subdivisions <= 0)
            return;

        x = (x1 + x2) / 2.0f;
        y = (y1 + y2) / 2.0f;
        z = (z1 + z2) / 2.0f;

        mid = (first + last) / 2;
        index = mid * 3;
        dest[index] = x;
        dest[index + 1] = y;
        dest[index + 2] = z;

        if (subdivisions > 1)
        {
            this.subdivide(x1, y1, z1, x, y, z, subdivisions - 1, dest, first, mid);
            this.subdivide(x, y, z, x2, y2, z2, subdivisions - 1, dest, mid, last);
        }
    }

    //**************************************************************//
    //********************  Bilinear Surface ********************//
    //**************************************************************//

    public int getBilinearSurfaceFillIndexCount(int uStacks, int vStacks)
    {
        return vStacks * 2 * (uStacks + 1) + 2 * (vStacks - 1);
    }

    public int getBilinearSurfaceOutlineIndexCount(int uStacks, int vStacks, int mask)
    {
        int count = 0;
        if ((mask & TOP) != 0)
            count += 2 * uStacks;
        if ((mask & BOTTOM) != 0)
            count += 2 * uStacks;
        if ((mask & LEFT) != 0)
            count += 2 * vStacks;
        if ((mask & RIGHT) != 0)
            count += 2 * vStacks;

        return count;
    }

    public int getBilinearSurfaceVertexCount(int uStacks, int vStacks)
    {
        return (uStacks + 1) * (vStacks + 1);
    }

    public int getBilinearSurfaceFillDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public int getBilinearSurfaceOutlineDrawMode()
    {
        return GL.GL_LINES;
    }

    public void makeBilinearSurfaceFillIndices(int vertexPos, int uStacks, int vStacks, int destPos, int[] dest)
    {
        int numIndices = this.getBilinearSurfaceFillIndexCount(uStacks, vStacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "uStacks=" + uStacks
                + " vStacks=" + vStacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < (numIndices + destPos))
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int ui, vi;
        int vertex, index;

        index = destPos;
        for (vi = 0; vi < vStacks; vi++)
        {
            if (vi != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = uStacks + vi * (uStacks + 1);
                    dest[index++] = vertexPos + vertex;
                    vertex = vi * (uStacks + 1);
                    dest[index++] = vertexPos + vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = uStacks + (vi - 1) * (uStacks + 1);
                    dest[index++] = vertexPos + vertex;
                    vertex = vi * (uStacks + 1) + (uStacks + 1);
                    dest[index++] = vertexPos + vertex;
                }
            }
            for (ui = 0; ui <= uStacks; ui++)
            {
                vertex = ui + vi * (uStacks + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertexPos + vertex;
                    dest[index++] = vertexPos + vertex + (uStacks + 1);
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertexPos + vertex + (uStacks + 1);
                    dest[index++] = vertexPos + vertex;
                }
            }
        }
    }

    public void makeBilinearSurfaceOutlineIndices(int vertexPos, int uStacks, int vStacks, int mask, int destPos,
        int[] dest)
    {
        int numIndices = this.getBilinearSurfaceOutlineIndexCount(uStacks, vStacks, mask);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "uStacks=" + uStacks
                + " vStacks=" + vStacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < (numIndices + destPos))
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int ui, vi;
        int vertex, index;

        index = destPos;
        // Bottom row.
        if ((mask & BOTTOM) != 0)
        {
            for (ui = 0; ui < uStacks; ui++)
            {
                vertex = ui;
                dest[index++] = vertexPos + vertex;
                vertex = ui + 1;
                dest[index++] = vertexPos + vertex;
            }
        }
        // Right side.
        if ((mask & RIGHT) != 0)
        {
            for (vi = 0; vi < vStacks; vi++)
            {
                vertex = uStacks + vi * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
                vertex = uStacks + (vi + 1) * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
            }
        }
        // Top side.
        if ((mask & TOP) != 0)
        {
            for (ui = uStacks; ui > 0; ui--)
            {
                vertex = ui + vStacks * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
                vertex = (ui - 1) + vStacks * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
            }
        }
        // Left side.
        if ((mask & LEFT) != 0)
        {
            for (vi = vStacks; vi > 0; vi--)
            {
                vertex = vi * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
                vertex = (vi - 1) * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
            }
        }
    }

    public void makeBilinearSurfaceVertices(float[] control, int destPos, int uStacks, int vStacks, float[] dest)
    {
        int numPoints = this.getBilinearSurfaceVertexCount(uStacks, vStacks);
        int numCoords = 3 * numPoints;

        if (control == null)
        {
            String message = "nullValue.ControlPointArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (control.length < 12)
        {
            String message = "generic.ControlPointArrayInvalidLength " + control.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "uStacks=" + uStacks
                + " vStacks=" + vStacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < (numCoords + 3 * destPos))
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y, z;
        float u, v;
        float du, dv;
        float oneMinusU, oneMinusV;
        int ui, vi;
        int index;

        du = 1.0f / (float) uStacks;
        dv = 1.0f / (float) vStacks;

        for (vi = 0; vi <= vStacks; vi++)
        {
            v = vi * dv;
            oneMinusV = 1.0f - v;
            for (ui = 0; ui <= uStacks; ui++)
            {
                u = ui * du;
                oneMinusU = 1.0f - u;
                index = ui + vi * (uStacks + 1);
                index = 3 * (destPos + index);
                x = oneMinusU * oneMinusV * control[0]  // Lower left control point
                    + u * oneMinusV * control[3]  // Lower right control point
                    + u * v * control[6]  // Upper right control point
                    + oneMinusU * v * control[9]; // Upper left control point
                y = oneMinusU * oneMinusV * control[1]
                    + u * oneMinusV * control[4]
                    + u * v * control[7]
                    + oneMinusU * v * control[10];
                z = oneMinusU * oneMinusV * control[2]
                    + u * oneMinusV * control[5]
                    + u * v * control[8]
                    + oneMinusU * v * control[11];
                dest[index] = x;
                dest[index + 1] = y;
                dest[index + 2] = z;
            }
        }
    }

    public void makeBilinearSurfaceVertexNormals(int srcPos, int uStacks, int vStacks, float[] srcVerts,
        int destPos, float dest[])
    {
        int numPoints = this.getBilinearSurfaceVertexCount(uStacks, vStacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "uStacks=" + uStacks
                + " vStacks=" + vStacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (srcVerts == null)
        {
            String message = "nullValue.SourceVertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < (numCoords + 3 * destPos))
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int ui, vi;
        int index;
        int vprev, vnext;
        float nsign;
        float[] norm, zero, tmp;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        zero = new float[3];
        tmp = new float[3];

        for (vi = 0; vi <= vStacks; vi++)
        {
            for (ui = 0; ui <= uStacks; ui++)
            {
                index = ui + vi * (uStacks + 1);
                index = srcPos + index;
                vprev = index - (uStacks + 1);
                vnext = index + (uStacks + 1);

                System.arraycopy(zero, 0, norm, 0, 3);

                // Adjacent faces below.
                if (vi > 0)
                {
                    // Adjacent faces below and to the left.
                    if (ui > 0)
                    {
                        this.facenorm(srcVerts, index, index - 1, vprev - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, vprev - 1, vprev, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                    // Adjacent faces below and to the right.
                    if (ui < uStacks)
                    {
                        this.facenorm(srcVerts, index, vprev, vprev + 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, vprev + 1, index + 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                }

                // Adjacent faces above.
                if (vi < vStacks)
                {
                    // Adjacent faces above and to the left.
                    if (ui > 0)
                    {
                        this.facenorm(srcVerts, index, vnext, vnext - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, vnext - 1, index - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                    // Adjacent faces above and to the right.
                    if (ui < uStacks)
                    {
                        this.facenorm(srcVerts, index, index + 1, vnext + 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, vnext + 1, vnext, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                }

                // Normalize and place in output.
                this.mul3AndSet(norm, 0, nsign);
                this.norm3AndSet(norm, 0);
                System.arraycopy(norm, 0, dest, 3 * index, 3);
            }
        }
    }

    //**************************************************************//
    //********************  2D Shapes  *****************************//
    //**************************************************************//

    /**
     * Creates a vertex buffer for a two-dimensional ellipse centered at the specified location and with the specified
     * radii. The ellipse's center is placed at <code>(x, y)</code>, it has a width of <code>2 * majorRadius</code>, and
     * a height of <code>2 * minorRadius</code>.
     * <p/>
     * If the specified <code>slices</code> is greater than 1 this returns a buffer with vertices evenly spaced along
     * the circumference of the ellipse. Otherwise this returns a buffer with one vertex.
     * <p/>
     * The returned buffer contains pairs of xy coordinates representing the location of each vertex in the ellipse in a
     * counter-clockwise winding order relative to the z axis. The buffer may be rendered in OpenGL as either a triangle
     * fan or a line loop.
     *
     * @param x           the x-coordinate of the ellipse's center.
     * @param y           the y-coordinate of the ellipse's center.
     * @param majorRadius the ellipse's radius along the x axis.
     * @param minorRadius the ellipse's radius along the y axis.
     * @param slices      the number of slices in the ellipse.
     *
     * @return a buffer containing the ellipse's x and y locations.
     *
     * @throws IllegalArgumentException if any of <code>majorRadius</code>, <code>minorRadius</code>, or
     *                                  <code>slices</code> are less than zero.
     */
    public FloatBuffer makeEllipse(float x, float y, float majorRadius, float minorRadius, int slices)
    {
        if (majorRadius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (minorRadius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (slices < 0)
        {
            String message = Logging.getMessage("generic.NumSlicesIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Return a buffer with only the first point at angle 0 if the number of slices is zero or one.
        if (slices <= 1)
        {
            // The buffer contains one coordinate pair.
            FloatBuffer buffer = Buffers.newDirectFloatBuffer(2);
            buffer.put(x + majorRadius);
            buffer.put(y);
            buffer.rewind();
            return buffer;
        }

        float step = (float) Math.PI * 2f / (float) slices;
        float angle = 0;

        // The buffer contains one coordinate pair per slice.
        FloatBuffer buffer = Buffers.newDirectFloatBuffer(2 * slices);

        // Add each vertex on the circumference of the ellipse, starting at zero and ending one step before 360.
        for (int i = 0; i < slices; i++, angle += step)
        {
            buffer.put(x + (float) Math.cos(angle) * majorRadius);
            buffer.put(y + (float) Math.sin(angle) * minorRadius);
        }

        // Rewind and return.
        buffer.rewind();
        return buffer;
    }

    /**
     * Creates a vertex buffer for a two-dimensional ellipse centered at the specified location and with the specified
     * radii. The ellipse's center is placed at <code>(x, y)</code>, it has a width of <code>2 * majorRadius</code>, and
     * a height of <code>2 * minorRadius</code>.
     * <p/>
     * If the specified <code>slices</code> is greater than 1 this returns a buffer with vertices evenly spaced along
     * the circumference of the ellipse. Otherwise this returns a buffer with one vertex.
     * <p/>
     * If the specified <code>leaderWidth</code> is greater than zero and the location <code>(leaderX, leaderY)</code>
     * is outside of the rectangle that encloses the ellipse, the ellipse has a triangle attached to one side with with
     * its top pointing at <code>(leaderX, leaderY)</code>. Otherwise this returns an ellipse with no leader and is
     * equivalent to calling <code>{@link #makeEllipse(float, float, float, float, int)}</code>. The leader is attached
     * at the center of either the top, bottom, left, or right side, depending on the leader's location relative to the
     * ellipse. The leader width is limited in size by the side it is attached to. For example, if the leader is
     * attached to the ellipse's bottom, its width is limited by the ellipse's major radius.
     *
     * @param x           the x-coordinate of the ellipse's center.
     * @param y           the y-coordinate of the ellipse's center.
     * @param majorRadius the ellipse's radius along the x axis.
     * @param minorRadius the ellipse's radius along the y axis.
     * @param slices      the number of slices in the ellipse.
     * @param leaderX     the x-coordinate the leader points to.
     * @param leaderY     the y-coordinate the leader points to.
     * @param leaderWidth the leader triangle's width.
     *
     * @return a buffer containing the ellipse's x and y locations.
     *
     * @throws IllegalArgumentException if any of <code>majorRadius</code>, <code>minorRadius</code>,
     *                                  <code>slices</code>, or <code>leaderWidth</code> are less than zero.
     */
    public FloatBuffer makeEllipseWithLeader(float x, float y, float majorRadius, float minorRadius, int slices,
        float leaderX, float leaderY, float leaderWidth)
    {
        if (majorRadius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (minorRadius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (slices < 0)
        {
            String message = Logging.getMessage("generic.NumSlicesIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (leaderWidth < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Return an ellipse without a leader if the leader width is zero.
        if (leaderWidth == 0)
            return this.makeEllipse(x, y, majorRadius, minorRadius, slices);

        int leaderCode = this.computeLeaderLocationCode(x - majorRadius, y - minorRadius, x + majorRadius,
            y + minorRadius, leaderX, leaderY);

        // Return an ellipse without a leader if the leader point is inside the rectangle.
        if (leaderCode == LEADER_LOCATION_INSIDE)
            return this.makeEllipse(x, y, majorRadius, minorRadius, slices);

        // Return a buffer with only the first point at angle 0 if the number of slices is zero or one.
        if (slices <= 1)
        {
            // The buffer contains one coordinate pair.
            FloatBuffer buffer = Buffers.newDirectFloatBuffer(2);
            buffer.put(x + majorRadius);
            buffer.put(y);
            buffer.rewind();
            return buffer;
        }

        // Determine the leader's size in radians and the starting angle according to the leader's location relative to
        // the ellipse.
        float leaderAngle;
        float startAngle;

        if ((leaderCode & LEADER_LOCATION_BOTTOM) != 0)
        {
            // Limit the leader's width by the ellipse's major radius.
            float maxLeaderWidth = 2f * majorRadius;
            if (leaderWidth > maxLeaderWidth)
                leaderWidth = maxLeaderWidth;

            leaderAngle = leaderWidth / majorRadius;
            startAngle = 3f * (float) Math.PI / 2f;
        }
        else if ((leaderCode & LEADER_LOCATION_TOP) != 0)
        {
            // Limit the leader's width by the ellipse's major radius.
            float maxLeaderWidth = 2f * majorRadius;
            if (leaderWidth > maxLeaderWidth)
                leaderWidth = maxLeaderWidth;

            leaderAngle = leaderWidth / majorRadius;
            startAngle = (float) Math.PI / 2f;
        }
        else if ((leaderCode & LEADER_LOCATION_LEFT) != 0)
        {
            // Limit the leader's width by the ellipse's minor radius.
            float maxLeaderWidth = 2f * minorRadius;
            if (leaderWidth > maxLeaderWidth)
                leaderWidth = maxLeaderWidth;

            leaderAngle = leaderWidth / minorRadius;
            startAngle = (float) Math.PI;
        }
        else if ((leaderCode & LEADER_LOCATION_RIGHT) != 0)
        {
            // Limit the leader's width by the ellipse's minor radius.
            float maxLeaderWidth = 2f * minorRadius;
            if (leaderWidth > maxLeaderWidth)
                leaderWidth = maxLeaderWidth;

            leaderAngle = leaderWidth / minorRadius;
            startAngle = 0f;
        }
        else
        {
            // Return an ellipse without a leader if the leader location code is unrecognized. This should never happen,
            // but we check anyway.
            return this.makeEllipse(x, y, majorRadius, minorRadius, slices);
        }

        float step = (float) (Math.PI * 2f - leaderAngle) / (float) slices;
        float angle = startAngle + leaderAngle / 2f;

        // The buffer contains one coordinate pair per slice, and three coordinate pairs for the leader.
        FloatBuffer buffer = Buffers.newDirectFloatBuffer(2 * slices + 6);
        // Start in the leader right corner to ensure the vertices can be drawn as a triangle fan.
        buffer.put(x + (float) Math.cos(startAngle + leaderAngle / 2f) * majorRadius);
        buffer.put(y + (float) Math.sin(startAngle + leaderAngle / 2f) * minorRadius);

        // Add each vertex on the circumference of the ellipse, starting at the right side of the leader, and ending at
        // the left side of the leader.
        for (int i = 0; i < slices; i++, angle += step)
        {
            buffer.put(x + (float) Math.cos(angle) * majorRadius);
            buffer.put(y + (float) Math.sin(angle) * minorRadius);
        }

        // Leader left corner.
        buffer.put(x + (float) Math.cos(startAngle - leaderAngle / 2f) * majorRadius);
        buffer.put(y + (float) Math.sin(startAngle - leaderAngle / 2f) * minorRadius);
        // Leader point.
        buffer.put(leaderX);
        buffer.put(leaderY);

        // Rewind and return.
        buffer.rewind();
        return buffer;
    }

    /**
     * Creates a vertex buffer for a two-dimensional rectangle at the specified location, and with the specified size.
     * The rectangle's lower left corner is placed at <code>(x, y)</code>, and its upper right corner is placed at
     * <code>(x + width, y + height)</code>.
     * <p/>
     * The returned buffer contains pairs of xy coordinates representing the location of each vertex in the rectangle in
     * a counter-clockwise winding order relative to the z axis. The buffer may be rendered in OpenGL as either a
     * triangle fan or a line loop.
     *
     * @param x      the x-coordinate of the rectangle's lower left corner.
     * @param y      the y-coordinate of the rectangle's lower left corner.
     * @param width  the rectangle's width.
     * @param height the rectangle's height.
     *
     * @return a buffer containing the rectangle's x and y locations.
     *
     * @throws IllegalArgumentException if either <code>width</code> or <code>height</code> are less than zero.
     */
    public FloatBuffer makeRectangle(float x, float y, float width, float height)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height < 0)
        {
            String message = Logging.getMessage("Geom.HeightIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // The buffer contains eight coordinate pairs: two pairs for each corner.
        FloatBuffer buffer = Buffers.newDirectFloatBuffer(8);
        // Lower left corner.
        buffer.put(x);
        buffer.put(y);
        // Lower right corner.
        buffer.put(x + width);
        buffer.put(y);
        // Upper right corner.
        buffer.put(x + width);
        buffer.put(y + height);
        // Upper left corner.
        buffer.put(x);
        buffer.put(y + height);
        // Rewind and return.
        buffer.rewind();
        return buffer;
    }

    /**
     * Creates a vertex buffer for a two-dimensional rectangle at the specified location, with the specified size, and
     * with optionally rounded corners. The rectangle's lower left corner is placed at the <code>(x, y)</code>, and its
     * upper right corner is placed at <code>(x + width, y + height)</code>.
     * <p/>
     * If the specified <code>cornerRadius</code> and <code>cornerSlices</code> are greater than 0, the rectangle's
     * corners have a rounded appearance. The radius specifies the size of a rounded corner, and the slices specifies
     * the number of segments that make a rounded corner. If either <code>cornerRadius</code> or
     * <code>cornerSlices</code> are 0, this returns a rectangle with sharp corners and is equivalent to calling
     * <code>{@link #makeRectangle(float, float, float, float)}</code>. The <code>cornerRadius</code> is limited by the
     * rectangle's width and height. For example, if the corner radius is 100 and the width and height are 50 and 100,
     * the actual corner radius used is 25 - half of the rectangle's smallest dimension.
     * <p/>
     * The returned buffer contains pairs of xy coordinates representing the location of each vertex in the rectangle in
     * a counter-clockwise winding order relative to the z axis. The buffer may be rendered in OpenGL as either a
     * triangle fan or a line loop.
     *
     * @param x            the x-coordinate of the rectangle's lower left corner.
     * @param y            the y-coordinate of the rectangle's lower left corner.
     * @param width        the rectangle's width.
     * @param height       the rectangle's height.
     * @param cornerRadius the rectangle's rounded corner radius, or 0 to disable rounded corners.
     * @param cornerSlices the number of slices in each rounded corner, or 0 to disable rounded corners.
     *
     * @return a buffer containing the rectangle's x and y locations.
     *
     * @throws IllegalArgumentException if any of <code>width</code>, <code>height</code>, <code>cornerRadius</code>, or
     *                                  <code>cornerSlices</code> are less than zero.
     */
    public FloatBuffer makeRectangle(float x, float y, float width, float height, float cornerRadius, int cornerSlices)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height < 0)
        {
            String message = Logging.getMessage("Geom.HeightIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (cornerRadius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (cornerSlices < 0)
        {
            String message = Logging.getMessage("generic.NumSlicesIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Limit the corner radius to half of the rectangles width or height, whichever is smaller.
        float maxCornerRadius = Math.min(width, height) / 2f;
        if (cornerRadius > maxCornerRadius)
            cornerRadius = maxCornerRadius;

        // Create a rectangle with sharp corners if either the corner radius or the number of corner slices is 0.
        if (cornerRadius == 0f || cornerSlices == 0)
            return this.makeRectangle(x, y, width, height);

        float piOver2 = (float) Math.PI / 2f;

        // The buffer contains four coordinate pairs for each corner, and two coordinate pairs per corner vertex.
        FloatBuffer buffer = Buffers.newDirectFloatBuffer(16 + 8 * (cornerSlices - 1));
        // Lower left corner.
        buffer.put(x);
        buffer.put(y + cornerRadius);
        this.addRectangleRoundedCorner(x + cornerRadius, x + cornerRadius, cornerRadius, (float) Math.PI, piOver2,
            cornerSlices, buffer);
        buffer.put(x + cornerRadius);
        buffer.put(y);
        // Lower right corner.
        buffer.put(x + width - cornerRadius);
        buffer.put(y);
        this.addRectangleRoundedCorner(x + width - cornerRadius, y + cornerRadius, cornerRadius, -piOver2, piOver2,
            cornerSlices, buffer);
        buffer.put(x + width);
        buffer.put(y + cornerRadius);
        // Upper right corner.
        buffer.put(x + width);
        buffer.put(y + height - cornerRadius);
        this.addRectangleRoundedCorner(x + width - cornerRadius, y + height - cornerRadius, cornerRadius, 0f, piOver2,
            cornerSlices, buffer);
        buffer.put(x + width - cornerRadius);
        buffer.put(y + height);
        // Upper left corner.
        buffer.put(x + cornerRadius);
        buffer.put(y + height);
        this.addRectangleRoundedCorner(x + cornerRadius, y + height - cornerRadius, cornerRadius, piOver2, piOver2,
            cornerSlices, buffer);
        buffer.put(x);
        buffer.put(y + height - cornerRadius);
        // Rewind and return.
        buffer.rewind();
        return buffer;
    }

    /**
     * Creates a vertex buffer for a two-dimensional rectangle at the specified location, with the specified size, and
     * with an optional leader pointing to the specified leader location. The rectangle's lower left corner is placed at
     * <code>(x, y)</code>, and its upper right corner is placed at <code>(x + width, y + height)</code>.
     * <p/>
     * If the specified <code>leaderWidth</code> is greater than zero and the location <code>(leaderX, leaderY)</code>
     * is outside of the rectangle, the rectangle has a triangle attached to one side with with its top pointing at
     * <code>(leaderX, leaderY)</code>. Otherwise this returns a rectangle with no leader and is equivalent to calling
     * <code>{@link #makeRectangle(float, float, float, float)}</code>. The leader is attached at the center of either
     * the top, bottom, left, or right side, depending on the leader's location relative to the rectangle. The leader
     * width is limited in size by the side it is attached to. For example, if the leader is attached to the rectangle's
     * bottom, its width is limited by the rectangle's width.
     * <p/>
     * The returned buffer contains pairs of xy coordinates representing the location of each vertex in the rectangle in
     * a counter-clockwise winding order relative to the z axis. The buffer may be rendered in OpenGL as either a
     * triangle fan or a line loop.
     *
     * @param x           the x-coordinate of the rectangle's lower left corner.
     * @param y           the y-coordinate of the rectangle's lower left corner.
     * @param width       the rectangle's width.
     * @param height      the rectangle's height.
     * @param leaderX     the x-coordinate the leader points to.
     * @param leaderY     the y-coordinate the leader points to.
     * @param leaderWidth the leader triangle's width.
     *
     * @return a buffer containing the rectangle's x and y locations.
     *
     * @throws IllegalArgumentException if any of <code>width</code>, <code>height</code>, or <code>leaderWidth</code>
     *                                  are less than zero.
     */
    @SuppressWarnings({"SuspiciousNameCombination"})
    public FloatBuffer makeRectangleWithLeader(float x, float y, float width, float height, float leaderX,
        float leaderY, float leaderWidth)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height < 0)
        {
            String message = Logging.getMessage("Geom.HeightIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (leaderWidth < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Return a rectangle without a leader if the leader width is zero.
        if (leaderWidth == 0)
            return this.makeRectangle(x, y, width, height);

        int leaderCode = this.computeLeaderLocationCode(x, y, x + width, y + height, leaderX, leaderY);

        // Return a rectangle without a leader if the leader point is inside the rectangle.
        if (leaderCode == LEADER_LOCATION_INSIDE)
            return this.makeRectangle(x, y, width, height);

        if ((leaderCode & LEADER_LOCATION_BOTTOM) != 0)
        {
            // Limit the leader's width by the rectangle's width.
            if (leaderWidth > width)
                leaderWidth = width;

            // The buffer contains seven xy coordinate pairs: two pairs for each corner and three pairs for the leader.
            FloatBuffer buffer = Buffers.newDirectFloatBuffer(14);
            // Start in the leader right corner to ensure the vertices can be drawn as a triangle fan.
            buffer.put(x + width / 2f + leaderWidth / 2f);
            buffer.put(y);
            // Lower right corner.
            buffer.put(x + width);
            buffer.put(y);
            // Upper right corner.
            buffer.put(x + width);
            buffer.put(y + height);
            // Upper left corner.
            buffer.put(x);
            buffer.put(y + height);
            // Lower left corner.
            buffer.put(x);
            buffer.put(y);
            // Leader left corner.
            buffer.put(x + width / 2f - leaderWidth / 2f);
            buffer.put(y);
            // Leader point.
            buffer.put(leaderX);
            buffer.put(leaderY);
            // Rewind and return.
            buffer.rewind();
            return buffer;
        }
        else if ((leaderCode & LEADER_LOCATION_TOP) != 0)
        {
            // Limit the leader's width by the rectangle's width.
            if (leaderWidth > width)
                leaderWidth = width;

            // The buffer contains seven xy coordinate pairs: two pairs for each corner and three pairs for the leader.
            FloatBuffer buffer = Buffers.newDirectFloatBuffer(14);
            // Start in the leader left corner to ensure the vertices can be drawn as a triangle fan.
            buffer.put(x + width / 2f - leaderWidth / 2f);
            buffer.put(y + height);
            // Upper left corner.
            buffer.put(x);
            buffer.put(y + height);
            // Lower left corner.
            buffer.put(x);
            buffer.put(y);
            // Lower right corner.
            buffer.put(x + width);
            buffer.put(y);
            // Upper right corner.
            buffer.put(x + width);
            buffer.put(y + height);
            // Leader right corner.
            buffer.put(x + width / 2f + leaderWidth / 2f);
            buffer.put(y + height);
            // Leader point.
            buffer.put(leaderX);
            buffer.put(leaderY);
            // Rewind and return.
            buffer.rewind();
            return buffer;
        }
        else if ((leaderCode & LEADER_LOCATION_LEFT) != 0)
        {
            // Limit the leader's width by the rectangle's height.
            if (leaderWidth > height)
            {
                //noinspection SuspiciousNameCombination
                leaderWidth = height;
            }

            // The buffer contains seven xy coordinate pairs: two pairs for each corner and three pairs for the leader.
            FloatBuffer buffer = Buffers.newDirectFloatBuffer(14);
            // Start in the leader bottom corner to ensure the vertices can be drawn as a triangle fan.
            buffer.put(x);
            buffer.put(y + height / 2f - leaderWidth / 2f);
            // Lower left corner.
            buffer.put(x);
            buffer.put(y);
            // Lower right corner.
            buffer.put(x + width);
            buffer.put(y);
            // Upper right corner.
            buffer.put(x + width);
            buffer.put(y + height);
            // Upper left corner.
            buffer.put(x);
            buffer.put(y + height);
            // Leader top corner.
            buffer.put(x);
            buffer.put(y + height / 2f + leaderWidth / 2f);
            // Leader point.
            buffer.put(leaderX);
            buffer.put(leaderY);
            // Rewind and return.
            buffer.rewind();
            return buffer;
        }
        else if ((leaderCode & LEADER_LOCATION_RIGHT) != 0)
        {
            // Limit the leader's width by the rectangle's height.
            if (leaderWidth > height)
            {
                //noinspection SuspiciousNameCombination
                leaderWidth = height;
            }

            // The buffer contains seven xy coordinate pairs: two pairs for each corner and three pairs for the leader.
            FloatBuffer buffer = Buffers.newDirectFloatBuffer(14);
            // Start in the leader top corner to ensure the vertices can be drawn as a triangle fan.
            buffer.put(x + width);
            buffer.put(y + height / 2f + leaderWidth / 2f);
            // Upper right corner.
            buffer.put(x + width);
            buffer.put(y + height);
            // Upper left corner.
            buffer.put(x);
            buffer.put(y + height);
            // Lower left corner.
            buffer.put(x);
            buffer.put(y);
            // Lower right corner.
            buffer.put(x + width);
            buffer.put(y);
            // Leader bottom corner.
            buffer.put(x + width);
            buffer.put(y + height / 2f - leaderWidth / 2f);
            // Leader point.
            buffer.put(leaderX);
            buffer.put(leaderY);
            // Rewind and return.
            buffer.rewind();
            return buffer;
        }
        else
        {
            // Return a rectangle without a leader if the leader location code is unrecognized. This should never
            // happen, but we check anyway.
            return this.makeRectangle(x, y, width, height);
        }
    }

    /**
     * Creates a vertex buffer for a two-dimensional rectangle at the specified location, with the specified size, and
     * with optionally rounded corners. The rectangle's lower left corner is placed at the <code>(x, y)</code>, and its
     * upper right corner is placed at <code>(x + width, y + height)</code>.
     * <p/>
     * If the specified <code>cornerRadius</code> and <code>cornerSlices</code> are greater than 0, the rectangle's
     * corners have a rounded appearance. The radius specifies the size of a rounded corner, and the slices specifies
     * the number of segments that make a rounded corner. If either <code>cornerRadius</code> or
     * <code>cornerSlices</code> are 0, this returns a rectangle with sharp corners and is equivalent to calling
     * <code>{@link #makeRectangleWithLeader(float, float, float, float, float, float, float)} </code>. The
     * <code>cornerRadius</code> is limited by the rectangle's width and height. For example, if the corner radius is
     * 100 and the width and height are 50 and 100, the actual corner radius used is 25 - half of the rectangle's
     * smallest dimension.
     * <p/>
     * If the specified <code>leaderWidth</code> is greater than zero and the location <code>(leaderX, leaderY)</code>
     * is outside of the rectangle, the rectangle has a triangle attached to one side with with its top pointing at
     * <code>(leaderX, leaderY)</code>. Otherwise this returns a rectangle with no leader and is equivalent to calling
     * <code>{@link #makeRectangle(float, float, float, float, float, int)}</code>. The leader is attached at the center
     * of either the top, bottom, left, or right side, depending on the leader's location relative to the rectangle. The
     * leader width is limited in size by the side it is attached to. For example, if the leader is attached to the
     * rectangle's bottom, its width is limited by the rectangle's width minus any area used by the rounded corners.
     * <p/>
     * The returned buffer contains pairs of xy coordinates representing the location of each vertex in the rectangle in
     * a counter-clockwise winding order relative to the z axis. The buffer may be rendered in OpenGL as either a
     * triangle fan or a line loop.
     *
     * @param x            the x-coordinate of the rectangle's lower left corner.
     * @param y            the y-coordinate of the rectangle's lower left corner.
     * @param width        the rectangle's width.
     * @param height       the rectangle's height.
     * @param cornerRadius the rectangle's rounded corner radius, or 0 to disable rounded corners.
     * @param cornerSlices the number of slices in each rounded corner, or 0 to disable rounded corners.
     * @param leaderX      the x-coordinate the leader points to.
     * @param leaderY      the y-coordinate the leader points to.
     * @param leaderWidth  the leader triangle's width.
     *
     * @return a buffer containing the rectangle's x and y locations.
     *
     * @throws IllegalArgumentException if any of <code>width</code>, <code>height</code>, <code>cornerRadius</code>,
     *                                  <code>cornerSlices</code>, or <code>leaderWidth</code> are less than zero.
     */
    public FloatBuffer makeRectangleWithLeader(float x, float y, float width, float height, float cornerRadius,
        int cornerSlices, float leaderX, float leaderY, float leaderWidth)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height < 0)
        {
            String message = Logging.getMessage("Geom.HeightIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (cornerRadius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (cornerSlices < 0)
        {
            String message = Logging.getMessage("generic.NumSlicesIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (leaderWidth < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Limit the corner radius to half of the rectangles width or height, whichever is smaller.
        float maxCornerRadius = Math.min(width, height) / 2f;
        if (cornerRadius > maxCornerRadius)
            cornerRadius = maxCornerRadius;

        // Create a rectangle with sharp corners if either the corner radius or the number of corner slices is 0.
        if (cornerRadius == 0f || cornerSlices == 0)
            return this.makeRectangleWithLeader(x, y, width, height, leaderX, leaderY, leaderWidth);

        // Return a rectangle without a leader if the leader width is zero.
        if (leaderWidth == 0)
            return this.makeRectangle(x, y, width, height, cornerRadius, cornerSlices);

        int leaderCode = this.computeLeaderLocationCode(x, y, x + width, y + height, leaderX, leaderY);

        // Return a rectangle without a leader if the leader point is inside the rectangle.
        if (leaderCode == LEADER_LOCATION_INSIDE)
            return this.makeRectangle(x, y, width, height, cornerRadius, cornerSlices);

        float piOver2 = (float) Math.PI / 2f;

        if ((leaderCode & LEADER_LOCATION_BOTTOM) != 0)
        {
            // Limit the leader width by the rectangle's width minus any width used by the rounded corners.
            float maxLeaderWidth = width - 2f * cornerRadius;
            if (leaderWidth > maxLeaderWidth)
                leaderWidth = maxLeaderWidth;

            // The buffer contains two coordinate pairs for each corner, three coordinate pairs for the leader, and two
            // coordinate pairs per corner vertex.
            FloatBuffer buffer = Buffers.newDirectFloatBuffer(22 + 8 * (cornerSlices - 1));
            // Start in the leader right corner to ensure the vertices can be drawn as a triangle fan.
            buffer.put(x + width / 2f + leaderWidth / 2f);
            buffer.put(y);
            // Lower right corner.
            buffer.put(x + width - cornerRadius);
            buffer.put(y);
            this.addRectangleRoundedCorner(x + width - cornerRadius, y + cornerRadius, cornerRadius, -piOver2, piOver2,
                cornerSlices, buffer);
            buffer.put(x + width);
            buffer.put(y + cornerRadius);
            // Upper right corner.
            buffer.put(x + width);
            buffer.put(y + height - cornerRadius);
            this.addRectangleRoundedCorner(x + width - cornerRadius, y + height - cornerRadius, cornerRadius, 0f,
                piOver2,
                cornerSlices, buffer);
            buffer.put(x + width - cornerRadius);
            buffer.put(y + height);
            // Upper left corner.
            buffer.put(x + cornerRadius);
            buffer.put(y + height);
            this.addRectangleRoundedCorner(x + cornerRadius, y + height - cornerRadius, cornerRadius, piOver2, piOver2,
                cornerSlices, buffer);
            buffer.put(x);
            buffer.put(y + height - cornerRadius);
            // Lower left corner.
            buffer.put(x);
            buffer.put(y + cornerRadius);
            this.addRectangleRoundedCorner(x + cornerRadius, x + cornerRadius, cornerRadius, (float) Math.PI, piOver2,
                cornerSlices, buffer);
            buffer.put(x + cornerRadius);
            buffer.put(y);
            // Leader left corner.
            buffer.put(x + width / 2f - leaderWidth / 2f);
            buffer.put(y);
            // Leader point.
            buffer.put(leaderX);
            buffer.put(leaderY);
            // Rewind and return.
            buffer.rewind();
            return buffer;
        }
        else if ((leaderCode & LEADER_LOCATION_TOP) != 0)
        {
            // Limit the leader width by the rectangle's width minus any width used by the rounded corners.
            float maxLeaderWidth = width - 2f * cornerRadius;
            if (leaderWidth > maxLeaderWidth)
                leaderWidth = maxLeaderWidth;

            // The buffer contains two coordinate pairs for each corner, three coordinate pairs for the leader, and two
            // coordinate pairs per corner vertex.
            FloatBuffer buffer = Buffers.newDirectFloatBuffer(22 + 8 * (cornerSlices - 1));
            // Start in the leader left corner to ensure the vertices can be drawn as a triangle fan.
            buffer.put(x + width / 2f - leaderWidth / 2f);
            buffer.put(y + height);
            // Upper left corner.
            buffer.put(x + cornerRadius);
            buffer.put(y + height);
            this.addRectangleRoundedCorner(x + cornerRadius, y + height - cornerRadius, cornerRadius, piOver2, piOver2,
                cornerSlices, buffer);
            buffer.put(x);
            buffer.put(y + height - cornerRadius);
            // Lower left corner.
            buffer.put(x);
            buffer.put(y + cornerRadius);
            this.addRectangleRoundedCorner(x + cornerRadius, x + cornerRadius, cornerRadius, (float) Math.PI, piOver2,
                cornerSlices, buffer);
            buffer.put(x + cornerRadius);
            buffer.put(y);
            // Lower right corner.
            buffer.put(x + width - cornerRadius);
            buffer.put(y);
            this.addRectangleRoundedCorner(x + width - cornerRadius, y + cornerRadius, cornerRadius, -piOver2, piOver2,
                cornerSlices, buffer);
            buffer.put(x + width);
            buffer.put(y + cornerRadius);
            // Upper right corner.
            buffer.put(x + width);
            buffer.put(y + height - cornerRadius);
            this.addRectangleRoundedCorner(x + width - cornerRadius, y + height - cornerRadius, cornerRadius, 0f,
                piOver2,
                cornerSlices, buffer);
            buffer.put(x + width - cornerRadius);
            buffer.put(y + height);
            // Leader right corner.
            buffer.put(x + width / 2f + leaderWidth / 2f);
            buffer.put(y + height);
            // Leader point.
            buffer.put(leaderX);
            buffer.put(leaderY);
            // Rewind and return.
            buffer.rewind();
            return buffer;
        }
        else if ((leaderCode & LEADER_LOCATION_LEFT) != 0)
        {
            // Limit the leader width by the rectangle's height minus any width used by the rounded corners.
            float maxLeaderWidth = height - 2f * cornerRadius;
            if (leaderWidth > maxLeaderWidth)
                leaderWidth = maxLeaderWidth;

            // The buffer contains two coordinate pairs for each corner, three coordinate pairs for the leader, and two
            // coordinate pairs per corner vertex.
            FloatBuffer buffer = Buffers.newDirectFloatBuffer(22 + 8 * (cornerSlices - 1));
            // Start in the leader bottom corner to ensure the vertices can be drawn as a triangle fan.
            buffer.put(x);
            buffer.put(y + height / 2f - leaderWidth / 2f);
            // Lower left corner.
            buffer.put(x);
            buffer.put(y + cornerRadius);
            this.addRectangleRoundedCorner(x + cornerRadius, x + cornerRadius, cornerRadius, (float) Math.PI, piOver2,
                cornerSlices, buffer);
            buffer.put(x + cornerRadius);
            buffer.put(y);
            // Lower right corner.
            buffer.put(x + width - cornerRadius);
            buffer.put(y);
            this.addRectangleRoundedCorner(x + width - cornerRadius, y + cornerRadius, cornerRadius, -piOver2, piOver2,
                cornerSlices, buffer);
            buffer.put(x + width);
            buffer.put(y + cornerRadius);
            // Upper right corner.
            buffer.put(x + width);
            buffer.put(y + height - cornerRadius);
            this.addRectangleRoundedCorner(x + width - cornerRadius, y + height - cornerRadius, cornerRadius, 0f,
                piOver2,
                cornerSlices, buffer);
            buffer.put(x + width - cornerRadius);
            buffer.put(y + height);
            // Upper left corner.
            buffer.put(x + cornerRadius);
            buffer.put(y + height);
            this.addRectangleRoundedCorner(x + cornerRadius, y + height - cornerRadius, cornerRadius, piOver2, piOver2,
                cornerSlices, buffer);
            buffer.put(x);
            buffer.put(y + height - cornerRadius);
            // Leader top corner.
            buffer.put(x);
            buffer.put(y + height / 2f + leaderWidth / 2f);
            // Leader point.
            buffer.put(leaderX);
            buffer.put(leaderY);
            // Rewind and return.
            buffer.rewind();
            return buffer;
        }
        else if ((leaderCode & LEADER_LOCATION_RIGHT) != 0)
        {
            // Limit the leader width by the rectangle's height minus any width used by the rounded corners.
            float maxLeaderWidth = height - 2f * cornerRadius;
            if (leaderWidth > maxLeaderWidth)
                leaderWidth = maxLeaderWidth;

            // The buffer contains two coordinate pairs for each corner, three coordinate pairs for the leader, and two
            // coordinate pairs per corner vertex.
            FloatBuffer buffer = Buffers.newDirectFloatBuffer(22 + 8 * (cornerSlices - 1));
            // Start in the leader top corner to ensure the vertices can be drawn as a triangle fan.
            buffer.put(x + width);
            buffer.put(y + height / 2f + leaderWidth / 2f);
            // Upper right corner.
            buffer.put(x + width);
            buffer.put(y + height - cornerRadius);
            this.addRectangleRoundedCorner(x + width - cornerRadius, y + height - cornerRadius, cornerRadius, 0f,
                piOver2,
                cornerSlices, buffer);
            buffer.put(x + width - cornerRadius);
            buffer.put(y + height);
            // Upper left corner.
            buffer.put(x + cornerRadius);
            buffer.put(y + height);
            this.addRectangleRoundedCorner(x + cornerRadius, y + height - cornerRadius, cornerRadius, piOver2, piOver2,
                cornerSlices, buffer);
            buffer.put(x);
            buffer.put(y + height - cornerRadius);
            // Lower left corner.
            buffer.put(x);
            buffer.put(y + cornerRadius);
            this.addRectangleRoundedCorner(x + cornerRadius, x + cornerRadius, cornerRadius, (float) Math.PI, piOver2,
                cornerSlices, buffer);
            buffer.put(x + cornerRadius);
            buffer.put(y);
            // Lower right corner.
            buffer.put(x + width - cornerRadius);
            buffer.put(y);
            this.addRectangleRoundedCorner(x + width - cornerRadius, y + cornerRadius, cornerRadius, -piOver2, piOver2,
                cornerSlices, buffer);
            buffer.put(x + width);
            buffer.put(y + cornerRadius);
            // Leader bottom corner.
            buffer.put(x + width);
            buffer.put(y + height / 2f - leaderWidth / 2f);
            // Leader point.
            buffer.put(leaderX);
            buffer.put(leaderY);
            // Rewind and return.
            buffer.rewind();
            return buffer;
        }
        else
        {
            // Return a rectangle without a leader if the leader location code is unrecognized. This should never
            // happen, but we check anyway.
            return this.makeRectangle(x, y, width, height, cornerRadius, cornerSlices);
        }
    }

    /**
     * Adds the vertices for one rounded corner of a two-dimensional rectangular to the specified <code>buffer</code>.
     * This assumes that the first and last vertices of each corner are created by the caller, so this adds only the
     * intermediate vertices. The number of intermediate vertices is equal to <code>slices - 2</code>. This does nothing
     * if <code>slices</code> is one or zero.
     *
     * @param x      the x-coordinate of the corner's origin.
     * @param y      the y-coordinate of the corner's origin.
     * @param radius the corner's radius.
     * @param start  the corner's starting angle, in radians.
     * @param sweep  the corner's angular distance, in radians.
     * @param slices the number of slices in the corner.
     * @param buffer the buffer the corner's xy coordinates are added to.
     */
    protected void addRectangleRoundedCorner(float x, float y, float radius, float start, float sweep, int slices,
        FloatBuffer buffer)
    {
        if (slices == 0f)
            return;

        float step = sweep / (float) slices;
        float angle = start + step;

        for (int i = 1; i < slices; i++, angle += step)
        {
            buffer.put(x + (float) Math.cos(angle) * radius);
            buffer.put(y + (float) Math.sin(angle) * radius);
        }
    }

    /**
     * Returns a four bit code indicating the leader's location within the specified rectangle. The rectangle's lower
     * left corner is located at <code>(x1, y1)</code> and its upper right corner is located at <code>(x2, y2)</code>.
     * The returned code includes the bit for any of <code>LEADER_LOCATION_LEFT</code>,
     * <code>LEADER_LOCATION_RIGHT</code>, <code>LEADER_LOCATION_BOTTOM</code>, and <code>LEADER_LOCATION_TOP</code>,
     * depending on whether the leader is located to the left, right, bottom, or top of the rectangle. If the leader is
     * inside the rectangle, this returns <code>LEADER_LOCATION_INSIDE</code>.
     *
     * @param x1      the rectangle's minimum x-coordinate.
     * @param y1      the rectangle's maximum x-coordinate.
     * @param x2      the rectangle's minimum y-coordinate.
     * @param y2      the rectangle's maximum y-coordinate.
     * @param leaderX the leader's x-coordinate.
     * @param leaderY the leader's y-coordinate.
     *
     * @return a four bit code indicating the leader's location relative to the rectangle.
     */
    protected int computeLeaderLocationCode(float x1, float y1, float x2, float y2, float leaderX, float leaderY)
    {
        return (leaderY > y2 ? LEADER_LOCATION_TOP : 0)   // bit 0: top
            | (leaderY < y1 ? LEADER_LOCATION_BOTTOM : 0) // bit 1: bottom
            | (leaderX > x2 ? LEADER_LOCATION_RIGHT : 0)  // bit 2: right
            | (leaderX < x1 ? LEADER_LOCATION_LEFT : 0);  // bit 3: left
    }

    //**************************************************************//
    //********************  Geometry Support    ********************//
    //**************************************************************//

    public <T> void reversePoints(int pos, int count, T[] points)
    {
        if (pos < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pos=" + pos);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (count < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "count=" + count);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (points == null)
        {
            String message = "nullValue.PointsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (points.length < (pos + count))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "points.length < " + (pos + count));
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        T tmp;
        int i, j, mid;

        for (i = 0, mid = count >> 1, j = count - 1; i < mid; i++, j--)
        {
            tmp = points[pos + i];
            points[pos + i] = points[pos + j];
            points[pos + j] = tmp;
        }
    }

    private int[] copyOf(int[] original, int newLength)
    {
        int[] copy;

        copy = new int[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));

        return copy;
    }

    private float[] copyOf(float[] original, int newLength)
    {
        float[] copy;

        copy = new float[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));

        return copy;
    }

    private IntBuffer copyOf(IntBuffer original, int newLength)
    {
        IntBuffer copy;

        copy = Buffers.newDirectIntBuffer(newLength);
        original.rewind();
        copy.put(original);

        return copy;
    }

    private FloatBuffer copyOf(FloatBuffer original, int newLength)
    {
        FloatBuffer copy;

        copy = Buffers.newDirectFloatBuffer(newLength);
        original.rewind();
        copy.put(original);

        return copy;
    }

    private void facenorm(float[] srcVerts, int vertA, int vertB, int vertC, float[] dest)
    {
        int ia, ib, ic;
        float[] ab, ac;

        ia = 3 * vertA;
        ib = 3 * vertB;
        ic = 3 * vertC;
        ab = new float[3];
        ac = new float[3];

        this.sub3(srcVerts, ib, srcVerts, ia, ab, 0);
        this.sub3(srcVerts, ic, srcVerts, ia, ac, 0);
        this.cross3(ab, ac, dest);
        this.norm3AndSet(dest, 0);
    }

    private void facenorm(FloatBuffer srcVerts, int vertA, int vertB, int vertC, float[] dest)
    {
        int ia, ib, ic;
        float[] ab, ac;

        ia = 3 * vertA;
        ib = 3 * vertB;
        ic = 3 * vertC;
        ab = new float[3];
        ac = new float[3];

        this.sub3(srcVerts, ib, srcVerts, ia, ab, 0);
        this.sub3(srcVerts, ic, srcVerts, ia, ac, 0);
        this.cross3(ab, ac, dest);
        this.norm3AndSet(dest, 0);
    }

    private void add3AndSet(float[] a, int aPos, float[] b, int bPos)
    {
        a[aPos] = a[aPos] + b[bPos];
        a[aPos + 1] = a[aPos + 1] + b[bPos + 1];
        a[aPos + 2] = a[aPos + 2] + b[bPos + 2];
    }

    private void add3AndSet(FloatBuffer a, int aPos, float[] b, int bPos)
    {
        a.put(aPos, a.get(aPos) + b[bPos]);
        a.put(aPos + 1, a.get(aPos + 1) + b[bPos + 1]);
        a.put(aPos + 2, a.get(aPos + 2) + b[bPos + 2]);
    }

    private void sub3(float[] a, int aPos, float[] b, int bPos, float[] dest, int destPos)
    {
        dest[destPos] = a[aPos] - b[bPos];
        dest[destPos + 1] = a[aPos + 1] - b[bPos + 1];
        dest[destPos + 2] = a[aPos + 2] - b[bPos + 2];
    }

    private void sub3(FloatBuffer a, int aPos, FloatBuffer b, int bPos, float[] dest, int destPos)
    {
        dest[destPos] = a.get(aPos) - b.get(bPos);
        dest[destPos + 1] = a.get(aPos + 1) - b.get(bPos + 1);
        dest[destPos + 2] = a.get(aPos + 2) - b.get(bPos + 2);
    }

    private void cross3(float[] a, float[] b, float[] dest)
    {
        dest[0] = a[1] * b[2] - a[2] * b[1];
        dest[1] = a[2] * b[0] - a[0] * b[2];
        dest[2] = a[0] * b[1] - a[1] * b[0];
    }

    private void mul3AndSet(float[] src, int srcPos, float c)
    {
        src[srcPos] *= c;
        src[srcPos + 1] *= c;
        src[srcPos + 2] *= c;
    }

    private void mul3AndSet(FloatBuffer src, int srcPos, float c)
    {
        src.put(srcPos, src.get(srcPos) * c);
        src.put(srcPos + 1, src.get(srcPos + 1) * c);
        src.put(srcPos + 2, src.get(srcPos + 2) * c);
    }

    private void mulAndSet(FloatBuffer src, int srcPos, float b, int offset)
    {
        src.put(srcPos + offset, src.get(srcPos + offset) * b);
    }

    private void norm3AndSet(float[] src, int srcPos)
    {
        float len;

        len = src[srcPos] * src[srcPos] + src[srcPos + 1] * src[srcPos + 1] + src[srcPos + 2] * src[srcPos + 2];
        if (len != 0.0f)
        {
            len = (float) Math.sqrt(len);
            src[srcPos] /= len;
            src[srcPos + 1] /= len;
            src[srcPos + 2] /= len;
        }
    }

    private void norm3AndSet(FloatBuffer src, int srcPos)
    {
        float len;

        len = src.get(srcPos) * src.get(srcPos)
            + src.get(srcPos + 1) * src.get(srcPos + 1)
            + src.get(srcPos + 2) * src.get(srcPos + 2);
        if (len != 0.0f)
        {
            len = (float) Math.sqrt(len);
            src.put(srcPos, src.get(srcPos) / len);
            src.put(srcPos + 1, src.get(srcPos + 1) / len);
            src.put(srcPos + 2, src.get(srcPos + 2) / len);
        }
    }

    private int nextPowerOfTwo(int n)
    {
        int i = 1;
        while (i < n)
        {
            i <<= 1;
        }
        return i;
    }

    private void append(Terrain terrain, LatLon ll, double altitude, boolean terrainConformant, Vec4 refPoint,
        FloatBuffer dest)
    {
        Vec4 point = terrainConformant ?
            terrain.getSurfacePoint(ll.latitude, ll.longitude, altitude) :
            terrain.getGlobe().computePointFromPosition(ll.latitude, ll.longitude, altitude);

        coord[0] = (float) (point.x - refPoint.x);
        coord[1] = (float) (point.y - refPoint.y);
        coord[2] = (float) (point.z - refPoint.z);
        dest.put(coord);
    }
}

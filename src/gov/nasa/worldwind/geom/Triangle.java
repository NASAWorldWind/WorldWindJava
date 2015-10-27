/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.nio.*;
import java.util.*;

/**
 * Provides operations on triangles.
 *
 * @author Eric Dalgliesh 30/11/2006
 * @version $Id: Triangle.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Triangle
{
    private static final double EPSILON = 0.0000001; // used in intersects method

    private final Vec4 a;
    private final Vec4 b;
    private final Vec4 c;

    /**
     * Construct a triangle from three counter-clockwise ordered vertices. The front face of the triangle is determined
     * by the right-hand rule.
     *
     * @param a the first vertex.
     * @param b the second vertex.
     * @param c the third vertex.
     *
     * @throws IllegalArgumentException if any vertex is null.
     */
    public Triangle(Vec4 a, Vec4 b, Vec4 c)
    {
        if (a == null || b == null || c == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Returns the first vertex.
     *
     * @return the first vertex.
     */
    public Vec4 getA()
    {
        return this.a;
    }

    /**
     * Returns the second vertex.
     *
     * @return the second vertex.
     */
    public Vec4 getB()
    {
        return this.b;
    }

    /**
     * Returns the third vertex.
     *
     * @return the third vertex.
     */
    public Vec4 getC()
    {
        return this.c;
    }

//    private Plane getPlane()
//    {
//        Vector ab, ac;
//        ab = new Vector(this.b.subtract(this.a)).normalize();
//        ac = new Vector(this.c.subtract(this.a)).normalize();
//
//        Vector n = new Vector(new Point(ab.x(), ab.y(), ab.z(), ab.w()).cross(new Point(ac.x(), ac.y(), ac.z(), ac.w())));
//
//        return new gov.nasa.worldwind.geom.Plane(n);
//    }

//    private Point temporaryIntersectPlaneAndLine(Line line, Plane plane)
//    {
//        Vector n = line.getDirection();
//        Point v0 = Point.fromOriginAndDirection(plane.getDistance(), plane.getNormal(), Point.ZERO);
//        Point p0 = line.getPointAt(0);
//        Point p1 = line.getPointAt(1);
//
//        double r1 = n.dot(v0.subtract(p0))/n.dot(p1.subtract(p0));
//        if(r1 >= 0)
//            return line.getPointAt(r1);
//        return null;
//    }
//
//    private Triangle divide(double d)
//    {
//        d  = 1/d;
//        return new Triangle(this.a.multiply(d), this.b.multiply(d), this.c.multiply(d));
//    }

    /**
     * Indicates whether a specified point is on the triangle.
     *
     * @param p the point to test. If null, the method returns false.
     *
     * @return true if the point is on the triangle, otherwise false.
     */
    public boolean contains(Vec4 p)
    {
        if (p == null)
            return false;

        // Compute vectors
        Vec4 v0 = this.c.subtract3(this.a);
        Vec4 v1 = this.b.subtract3(this.a);
        Vec4 v2 = p.subtract3(this.a);

        // Compute dot products
        double dot00 = v0.dotSelf3();
        double dot01 = v0.dot3(v1);
        double dot02 = v0.dot3(v2);
        double dot11 = v1.dotSelf3();
        double dot12 = v1.dot3(v2);

        // Compute barycentric coordinates
        double det = (dot00 * dot11 - dot01 * dot01);

        double detInv = 1 / det;
        double u = (dot11 * dot02 - dot01 * dot12) * detInv;
        double v = (dot00 * dot12 - dot01 * dot02) * detInv;

        // Check if point is contained in triangle (including edges and vertices)
        return (u >= 0d) && (v >= 0d) && (u + v <= 1d);

        // Check if point is contained inside triangle (NOT including edges or vertices)
//        return (u > 0d) && (v > 0d) && (u + v < 1d);
    }

    /**
     * Determine the intersection of the triangle with a specified line.
     *
     * @param line the line to test.
     *
     * @return the point of intersection if the line intersects the triangle, otherwise null.
     *
     * @throws IllegalArgumentException if the line is null.
     */
    public Vec4 intersect(Line line)
    {
        Intersection intersection = intersect(line, this.a, this.b, this.c);

        return intersection != null ? intersection.getIntersectionPoint() : null;
    }

    /**
     * Determines the intersection of a specified line with a specified triangle. The triangle is specified by three
     * points ordered counterclockwise. The triangle's front face is determined by the right-hand rule.
     *
     * @param line the line to test.
     * @param a    the first vertex of the triangle.
     * @param b    the second vertex of the triangle.
     * @param c    the third vertex of the triangle.
     *
     * @return the point of intersection if the line intersects the triangle, otherwise null.
     *
     * @throws IllegalArgumentException if the line or any of the triangle vertices is null.
     */
    public static Intersection intersect(Line line, Vec4 a, Vec4 b, Vec4 c)
    {
        return intersect(line, a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z);
    }

    /**
     * Determines the intersection of a specified line with a triangle specified by individual coordinates.
     *
     * @param line the line to test.
     * @param vax  the X coordinate of the first vertex of the triangle.
     * @param vay  the Y coordinate of the first vertex of the triangle.
     * @param vaz  the Z coordinate of the first vertex of the triangle.
     * @param vbx  the X coordinate of the second vertex of the triangle.
     * @param vby  the Y coordinate of the second vertex of the triangle.
     * @param vbz  the Z coordinate of the second vertex of the triangle.
     * @param vcx  the X coordinate of the third vertex of the triangle.
     * @param vcy  the Y coordinate of the third vertex of the triangle.
     * @param vcz  the Z coordinate of the third vertex of the triangle.
     *
     * @return the point of intersection if the line intersects the triangle, otherwise null.
     */
    public static Intersection intersect(Line line,
        double vax, double vay, double vaz, double vbx, double vby, double vbz, double vcx, double vcy, double vcz)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // taken from Moller and Trumbore
        // http://www.cs.virginia.edu/~gfx/Courses/2003/ImageSynthesis/papers/Acceleration/
        // Fast%20MinimumStorage%20RayTriangle%20Intersection.pdf

        Vec4 origin = line.getOrigin();
        Vec4 dir = line.getDirection();

        // find vectors for two edges sharing Point a: vb - va and vc - va
        double edge1x = vbx - vax;
        double edge1y = vby - vay;
        double edge1z = vbz - vaz;

        double edge2x = vcx - vax;
        double edge2y = vcy - vay;
        double edge2z = vcz - vaz;

        // Start calculating determinant. Compute cross product of line direction and edge2.
        double pvecx = (dir.y * edge2z) - (dir.z * edge2y);
        double pvecy = (dir.z * edge2x) - (dir.x * edge2z);
        double pvecz = (dir.x * edge2y) - (dir.y * edge2x);

        // Get determinant.
        double det = edge1x * pvecx + edge1y * pvecy + edge1z * pvecz; // edge1 dot pvec

        if (det > -EPSILON && det < EPSILON) // If det is near zero, then ray lies on plane of triangle
            return null;

        double detInv = 1d / det;

        // Distance from vertA to ray origin: origin - va
        double tvecx = origin.x - vax;
        double tvecy = origin.y - vay;
        double tvecz = origin.z - vaz;

        // Calculate u parameter and test bounds: 1/det * tvec dot pvec
        double u = detInv * (tvecx * pvecx + tvecy * pvecy + tvecz * pvecz);
        if (u < 0 || u > 1)
            return null;

        // Prepare to test v parameter: tvec cross edge1
        double qvecx = (tvecy * edge1z) - (tvecz * edge1y);
        double qvecy = (tvecz * edge1x) - (tvecx * edge1z);
        double qvecz = (tvecx * edge1y) - (tvecy * edge1x);

        // Calculate v parameter and test bounds: 1/det * dir dot qvec
        double v = detInv * (dir.x * qvecx + dir.y * qvecy + dir.z * qvecz);
        if (v < 0 || u + v > 1)
            return null;

        // Calculate the point of intersection on the line: t = 1/det * edge2 dot qvec;
        double t = detInv * (edge2x * qvecx + edge2y * qvecy + edge2z * qvecz);
        if (t < 0)
            return null;

        return new Intersection(line.getPointAt(t), t, false);
    }

    /**
     * Compute the intersections of a line with a triangle strip.
     *
     * @param line     the line to intersect.
     * @param vertices the tri-strip vertices.
     * @param indices  the indices forming the tri-strip.
     *
     * @return the list of intersections with the line and the tri-strip, or null if there are no intersections.
     *
     * @throws IllegalArgumentException if the line, vertex buffer or index buffer is null.
     */
    public static List<Intersection> intersectTriStrip(final Line line, FloatBuffer vertices, IntBuffer indices)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (vertices == null || indices == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        List<Intersection> intersections = null;

        for (int n = indices.position(); n < indices.limit() - 2; n++)
        {
            Intersection intersection;

            int i = indices.get(n) * 3;
            int j = indices.get(n + 1) * 3;
            int k = indices.get(n + 2) * 3;

            // The triangle intersect method detects front and back face intersections so there's no reason to
            // order the vertices.
            intersection = intersect(line,
                vertices.get(i), vertices.get(i + 1), vertices.get(i + 2),
                vertices.get(j), vertices.get(j + 1), vertices.get(j + 2),
                vertices.get(k), vertices.get(k + 1), vertices.get(k + 2));

            if (intersection != null)
            {
                if (intersections == null)
                    intersections = new ArrayList<Intersection>();
                intersections.add(intersection);
            }
        }

        return intersections;
    }

    /**
     * Compute the intersections of a line with a triangle strip.
     *
     * @param line     the line to intersect.
     * @param vertices the tri-strip vertices.
     * @param indices  the indices forming the tri-strip.
     *
     * @return the list of intersections with the line and the triangle strip, or null if there are no intersections.
     *
     * @throws IllegalArgumentException if the line, vertex array or index buffer is null.
     */
    public static List<Intersection> intersectTriStrip(final Line line, Vec4[] vertices, IntBuffer indices)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (vertices == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (indices == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        List<Intersection> intersections = null;

        for (int n = indices.position(); n < indices.limit() - 1; n++)
        {
            Intersection intersection;

            int i = indices.get(n) * 3;
            int j = indices.get(n + 1) * 3;
            int k = indices.get(n + 2) * 3;

            // The triangle intersect method detects front and back face intersections so there's no reason to
            // order the vertices.
            intersection = intersect(line, vertices[i], vertices[j], vertices[k]);

            if (intersection != null)
            {
                if (intersections == null)
                    intersections = new ArrayList<Intersection>();
                intersections.add(intersection);
            }
        }

        return intersections;
    }

    /**
     * Compute the intersections of a line with a triangle fan.
     *
     * @param line     the line to intersect.
     * @param vertices the tri-fan vertices.
     * @param indices  the indices forming the tri-fan.
     *
     * @return the list of intersections with the line and the triangle fan, or null if there are no intersections.
     *
     * @throws IllegalArgumentException if the line, vertex buffer or index buffer is null.
     */
    public static List<Intersection> intersectTriFan(final Line line, FloatBuffer vertices, IntBuffer indices)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (vertices == null || indices == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        List<Intersection> intersections = null;

        // Get the index and then the values of the constant vertex.
        int k = indices.get(); // note that this increments the index buffer position

        float v0x = vertices.get(k * 3);
        float v0y = vertices.get(k * 3 + 1);
        float v0z = vertices.get(k * 3 + 2);

        // Starting with the second position in the index buffer, get subsequent indices and vertices.
        for (int n = indices.position(); n < indices.limit() - 1; n++)
        {
            Intersection intersection;

            int i = indices.get(n) * 3;
            int j = indices.get(n + 1) * 3;

            // The triangle intersect method detects front and back face intersections so there's no reason to
            // order the vertices.
            intersection = intersect(line,
                v0x, v0y, v0z,
                vertices.get(i), vertices.get(i + 1), vertices.get(i + 2),
                vertices.get(j), vertices.get(j + 1), vertices.get(j + 2));

            if (intersection != null)
            {
                if (intersections == null)
                    intersections = new ArrayList<Intersection>();
                intersections.add(intersection);
            }
        }

        return intersections;
    }

    /**
     * Compute the intersections of a line with a triangle fan.
     *
     * @param line     the line to intersect.
     * @param vertices the tri-fan vertices.
     * @param indices  the indices forming the tri-fan.
     *
     * @return the list of intersections with the line and the triangle fan, or null if there are no intersections.
     *
     * @throws IllegalArgumentException if the line, vertex array or index buffer is null.
     */
    public static List<Intersection> intersectTriFan(final Line line, Vec4[] vertices, IntBuffer indices)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (vertices == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (indices == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        List<Intersection> intersections = null;

        Vec4 v0 = vertices[0];

        for (int n = indices.position() + 1; n < indices.limit() - 1; n++)
        {
            Intersection intersection;

            Vec4 v1 = vertices[indices.get(n)];
            Vec4 v2 = vertices[indices.get(n + 1)];

            // The triangle intersect method detects front and back face intersections so there's no reason to
            // order the vertices.
            intersection = intersect(line, v0, v1, v2);
            if (intersection != null)
            {
                if (intersections == null)
                    intersections = new ArrayList<Intersection>();
                intersections.add(intersection);
            }
        }

        return intersections;
    }

    /**
     * Compute the intersections of a line with a collection of triangles.
     *
     * @param line     the line to intersect.
     * @param vertices the triangles, arranged in a buffer as GL_TRIANGLES (9 floats per triangle).
     *
     * @return the list of intersections with the line and the triangles, or null if there are no intersections.
     *
     * @throws IllegalArgumentException if the line or vertex buffer is null.
     */
    public static List<Intersection> intersectTriangles(final Line line, FloatBuffer vertices)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (vertices == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        List<Intersection> intersections = null;

        vertices.rewind();

        while (vertices.limit() - vertices.position() >= 9)
        {
            Intersection intersection = intersect(line,
                vertices.get(), vertices.get(), vertices.get(),
                vertices.get(), vertices.get(), vertices.get(),
                vertices.get(), vertices.get(), vertices.get());

            if (intersection != null)
            {
                if (intersections == null)
                    intersections = new ArrayList<Intersection>();
                intersections.add(intersection);
            }
        }

        return intersections;
    }

    /**
     * Compute the intersections of a line with a collection of triangles.
     *
     * @param line     the line to intersect.
     * @param vertices the triangles, arranged in a buffer as GL_TRIANGLES (9 floats per triangle).
     * @param indices  the indices forming the triangles.
     *
     * @return the list of intersections with the line and the triangle fan, or null if there are no intersections.
     *
     * @throws IllegalArgumentException if the line, vertex buffer or index buffer is null.
     */
    public static List<Intersection> intersectTriangles(final Line line, FloatBuffer vertices, IntBuffer indices)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (vertices == null || indices == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        List<Intersection> intersections = null;

        for (int n = indices.position(); n < indices.limit(); n += 3)
        {
            Intersection intersection;

            int i = indices.get(n) * 3;
            int j = indices.get(n + 1) * 3;
            int k = indices.get(n + 2) * 3;

            intersection = intersect(line,
                vertices.get(i), vertices.get(i + 1), vertices.get(i + 2),
                vertices.get(j), vertices.get(j + 1), vertices.get(j + 2),
                vertices.get(k), vertices.get(k + 1), vertices.get(k + 2));

            if (intersection != null)
            {
                if (intersections == null)
                    intersections = new ArrayList<Intersection>();
                intersections.add(intersection);
            }
        }

        return intersections;
    }

    /**
     * Compute the intersections of a line with a triangle collection.
     *
     * @param line         the line to intersect.
     * @param vertices     the tri-fan vertices, in the order x, y, z, x, y, z, ...
     * @param indices      the indices forming the tri-fan.
     * @param triangleType the type of triangle collection, either GL.GL_TRIANGLE_STRIP or GL.GL_TRIANGLE_FAN.
     *
     * @return the list of intersections with the line and the triangle fan, or null if there are no intersections.
     */
    public static List<Intersection> intersectTriangleTypes(final Line line, FloatBuffer vertices, IntBuffer indices,
        int triangleType)
    {
        if (triangleType == GL.GL_TRIANGLES)
            return Triangle.intersectTriangles(line, vertices, indices);
        else if (triangleType == GL.GL_TRIANGLE_STRIP)
            return Triangle.intersectTriStrip(line, vertices, indices);
        else if (triangleType == GL.GL_TRIANGLE_FAN)
            return Triangle.intersectTriFan(line, vertices, indices);

        return null;
    }

    /**
     * Expands a buffer of indexed triangle vertices to a buffer of non-indexed triangle vertices.
     *
     * @param indices the triangle indices.
     * @param inBuf   the vertex buffer the indices refer to, in the order x, y, z, x, y, z, ...
     * @param outBuf  the buffer in which to place the expanded triangle vertices. The buffer must have a limit
     *                sufficient to hold the output vertices.
     *
     * @throws IllegalArgumentException if the index list or the input or output buffer is null, or if the output buffer
     *                                  size is insufficient.
     */
    public static void expandTriangles(List<Integer> indices, FloatBuffer inBuf, FloatBuffer outBuf)
    {
        if (indices == null)
        {
            String msg = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (inBuf == null || outBuf == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int nunTriangles = indices.size() / 3;
        if (nunTriangles * 3 * 3 > outBuf.limit() - outBuf.position())
        {
            String msg = Logging.getMessage("generic.BufferSize", outBuf.limit() - outBuf.position());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        for (int i = 0; i < indices.size(); i += 3)
        {
            int k = indices.get(i) * 3;
            outBuf.put(inBuf.get(k)).put(inBuf.get(k + 1)).put(inBuf.get(k + 2));

            k = indices.get(i + 1) * 3;
            outBuf.put(inBuf.get(k)).put(inBuf.get(k + 1)).put(inBuf.get(k + 2));

            k = indices.get(i + 2) * 3;
            outBuf.put(inBuf.get(k)).put(inBuf.get(k + 1)).put(inBuf.get(k + 2));
        }
    }

    /**
     * Expands a buffer of indexed triangle fan vertices to a buffer of non-indexed general-triangle vertices.
     *
     * @param indices the triangle indices.
     * @param inBuf   the vertex buffer the indices refer to, in the order x, y, z, x, y, z, ...
     * @param outBuf  the buffer in which to place the expanded triangle vertices. The buffer must have a limit
     *                sufficient to hold the output vertices.
     *
     * @throws IllegalArgumentException if the index list or the input or output buffer is null, or if the output buffer
     *                                  size is insufficient.
     */
    public static void expandTriangleFan(List<Integer> indices, FloatBuffer inBuf, FloatBuffer outBuf)
    {
        if (indices == null)
        {
            String msg = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (inBuf == null || outBuf == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int nunTriangles = indices.size() - 2;
        if (nunTriangles * 3 * 3 > outBuf.limit() - outBuf.position())
        {
            String msg = Logging.getMessage("generic.BufferSize", outBuf.limit() - outBuf.position());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int k = indices.get(0) * 3;
        float v0x = inBuf.get(k);
        float v0y = inBuf.get(k + 1);
        float v0z = inBuf.get(k + 2);

        for (int i = 1; i < indices.size() - 1; i++)
        {
            outBuf.put(v0x).put(v0y).put(v0z);

            k = indices.get(i) * 3;
            outBuf.put(inBuf.get(k)).put(inBuf.get(k + 1)).put(inBuf.get(k + 2));

            k = indices.get(i + 1) * 3;
            outBuf.put(inBuf.get(k)).put(inBuf.get(k + 1)).put(inBuf.get(k + 2));
        }
    }

    /**
     * Expands a buffer of indexed triangle strip vertices to a buffer of non-indexed general-triangle vertices.
     *
     * @param indices the triangle indices.
     * @param inBuf   the vertex buffer the indices refer to, in the order x, y, z, x, y, z, ...
     * @param outBuf  the buffer in which to place the expanded triangle vertices. The buffer must have a limit
     *                sufficient to hold the output vertices.
     *
     * @throws IllegalArgumentException if the index list or the input or output buffer is null, or if the output buffer
     *                                  size is insufficient.
     */
    public static void expandTriangleStrip(List<Integer> indices, FloatBuffer inBuf, FloatBuffer outBuf)
    {
        if (indices == null)
        {
            String msg = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (inBuf == null || outBuf == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int nunTriangles = indices.size() - 2;
        if (nunTriangles * 3 * 3 > outBuf.limit() - outBuf.position())
        {
            String msg = Logging.getMessage("generic.BufferSize", outBuf.limit() - outBuf.position());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        for (int i = 2; i < indices.size(); i++)
        {
            int k = indices.get(i - 2) * 3;
            outBuf.put(inBuf.get(k)).put(inBuf.get(k + 1)).put(inBuf.get(k + 2));

            k = indices.get(i % 2 == 0 ? i : i - 1) * 3;
            outBuf.put(inBuf.get(k)).put(inBuf.get(k + 1)).put(inBuf.get(k + 2));

            k = indices.get(i % 2 == 0 ? i - 1 : i) * 3;
            outBuf.put(inBuf.get(k)).put(inBuf.get(k + 1)).put(inBuf.get(k + 2));
        }
    }

    public static void expandTriangles(List<Integer> indices, IntBuffer outBuf)
    {
        if (indices == null)
        {
            String msg = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (outBuf == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int numTriangles = indices.size() / 3;
        if (numTriangles * 3 > outBuf.limit() - outBuf.position())
        {
            String msg = Logging.getMessage("generic.BufferSize", outBuf.limit() - outBuf.position());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        for (int i = 0; i < indices.size(); i++)
        {
            outBuf.put(indices.get(i));
        }
    }

    public static void expandTriangleFan(List<Integer> indices, IntBuffer outBuf)
    {
        if (indices == null)
        {
            String msg = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (outBuf == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int nunTriangles = indices.size() - 2;
        if (nunTriangles * 3 > outBuf.limit() - outBuf.position())
        {
            String msg = Logging.getMessage("generic.BufferSize", outBuf.limit() - outBuf.position());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int k0 = indices.get(0);

        for (int i = 1; i < indices.size() - 1; i++)
        {
            outBuf.put(k0);
            outBuf.put(indices.get(i));
            outBuf.put(indices.get(i + 1));
        }
    }

    public static void expandTriangleStrip(List<Integer> indices, IntBuffer outBuf)
    {
        if (indices == null)
        {
            String msg = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (outBuf == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int nunTriangles = indices.size() - 2;
        if (nunTriangles * 3 > outBuf.limit() - outBuf.position())
        {
            String msg = Logging.getMessage("generic.BufferSize", outBuf.limit() - outBuf.position());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        for (int i = 2; i < indices.size(); i++)
        {
            outBuf.put(indices.get(i - 2));
            outBuf.put(indices.get(i % 2 == 0 ? i - 1 : i));
            outBuf.put(indices.get(i % 2 == 0 ? i : i - 1));
        }
    }

    /**
     * Defines a line segment representing the intersection of a line with and in the plane of a triangle. Used only
     * within {@link #intersectTriangles}.
     */
    protected static class TriangleIntersection
    {
        public Vec4 p0; // the first point of the line
        public Vec4 p1; // the second point of the line
        public double s0; // the distance along the line to the first intersection with the triangle
        public double s1; // the distance along the line to the second intersection with the triangle
    }

    /**
     * Intersects two triangles and returns their intersection vertices.
     *
     * @param v                    the Cartesian coordinates of the first triangle.
     * @param u                    the Cartesian coordinates of the second triangle.
     * @param intersectionVertices a pre-allocated two-element array in which the intersection vertices, if any, are
     *                             returned.
     *
     * @return -1 if there is no intersection, 1 if there is an intersection, or 0 if the triangles are co-planar.
     */
    public static int intersectTriangles(Vec4[] v, Vec4[] u, Vec4[] intersectionVertices)
    {
        // Taken from http://jgt.akpeters.com/papers/Moller97/tritri.html#ISECTLINE

        // Compute plane equation of first triangle: n1 * x + d1 = 0.
        double e1x = v[1].x - v[0].x;
        double e1y = v[1].y - v[0].y;
        double e1z = v[1].z - v[0].z;
        double e2x = v[2].x - v[0].x;
        double e2y = v[2].y - v[0].y;
        double e2z = v[2].z - v[0].z;
        Vec4 n1 = new Vec4(e1y * e2z - e1z * e2y, e1z * e2x - e1x * e2z, e1x * e2y - e1y * e2x);
        double d1 = -n1.dot3(v[0]);

        // Evaluate second triangle with plane equation 1 to determine signed distances to the plane.
        double du0 = n1.dot3(u[0]) + d1;
        double du1 = n1.dot3(u[1]) + d1;
        double du2 = n1.dot3(u[2]) + d1;

        // Coplanarity robustness check.
        if (Math.abs(du0) < EPSILON)
            du0 = 0;
        if (Math.abs(du1) < EPSILON)
            du1 = 0;
        if (Math.abs(du2) < EPSILON)
            du2 = 0;

        double du0du1 = du0 * du1;
        double du0du2 = du0 * du2;

        if (du0du1 > 0 && du0du2 > 0) // same sign on all of them + != 0 ==> no intersection
            return -1;

        // Compute plane equation of second triangle: n2 * x + d2 = 0
        e1x = u[1].x - u[0].x;
        e1y = u[1].y - u[0].y;
        e1z = u[1].z - u[0].z;
        e2x = u[2].x - u[0].x;
        e2y = u[2].y - u[0].y;
        e2z = u[2].z - u[0].z;
        Vec4 n2 = new Vec4(e1y * e2z - e1z * e2y, e1z * e2x - e1x * e2z, e1x * e2y - e1y * e2x);
        double d2 = -n2.dot3(u[0]);

        // Evaluate first triangle with plane equation 2 to determine signed distances to the plane.
        double dv0 = n2.dot3(v[0]) + d2;
        double dv1 = n2.dot3(v[1]) + d2;
        double dv2 = n2.dot3(v[2]) + d2;

        // Coplanarity robustness check.
        if (Math.abs(dv0) < EPSILON)
            dv0 = 0;
        if (Math.abs(dv1) < EPSILON)
            dv1 = 0;
        if (Math.abs(dv2) < EPSILON)
            dv2 = 0;

        double dv0dv1 = dv0 * dv1;
        double dv0dv2 = dv0 * dv2;

        if (dv0dv1 > 0 && dv0dv2 > 0) // same sign on all of them + != 0 ==> no intersection
            return -1;

        // Compute direction of intersection line.
        Vec4 ld = n1.cross3(n2);

        // Compute an index to the largest component of line direction.
        double max = Math.abs(ld.x);
        int index = 0;
        double b = Math.abs(ld.y);
        double c = Math.abs(ld.z);
        if (b > max)
        {
            max = b;
            index = 1;
        }
        if (c > max)
        {
            index = 2;
        }

        // This is the simplified projection onto the line of intersection.
        double vp0 = v[0].x;
        double vp1 = v[1].x;
        double vp2 = v[2].x;

        double up0 = u[0].x;
        double up1 = u[1].x;
        double up2 = u[2].x;
        if (index == 1)
        {
            vp0 = v[0].y;
            vp1 = v[1].y;
            vp2 = v[2].y;

            up0 = u[0].y;
            up1 = u[1].y;
            up2 = u[2].y;
        }
        else if (index == 2)
        {
            vp0 = v[0].z;
            vp1 = v[1].z;
            vp2 = v[2].z;

            up0 = u[0].z;
            up1 = u[1].z;
            up2 = u[2].z;
        }

        // Compute interval for triangle 1.
        TriangleIntersection isectA = compute_intervals_isectline(v, vp0, vp1, vp2, dv0, dv1, dv2, dv0dv1, dv0dv2);

        if (isectA == null)
            return coplanarTriangles(n1, v, u) ? 0 : -1;

        int smallest1 = 0;
        if (isectA.s0 > isectA.s1)
        {
            double cc = isectA.s0;
            isectA.s0 = isectA.s1;
            isectA.s1 = cc;
            smallest1 = 1;
        }

        // Compute interval for triangle 2.
        TriangleIntersection isectB = compute_intervals_isectline(u, up0, up1, up2, du0, du1, du2, du0du1, du0du2);

        int smallest2 = 0;
        if (isectB.s0 > isectB.s1)
        {
            double cc = isectB.s0;
            isectB.s0 = isectB.s1;
            isectB.s1 = cc;
            smallest2 = 1;
        }

        if (isectA.s1 < isectB.s0 || isectB.s1 < isectA.s0)
            return -1;

        // At this point we know that the triangles intersect: there's an intersection line, the triangles are not
        // coplanar, and they overlap.

        if (isectB.s0 < isectA.s0)
        {
            if (smallest1 == 0)
                intersectionVertices[0] = isectA.p0;
            else
                intersectionVertices[0] = isectA.p1;

            if (isectB.s1 < isectA.s1)
            {
                if (smallest2 == 0)
                    intersectionVertices[1] = isectB.p1;
                else
                    intersectionVertices[1] = isectB.p0;
            }
            else
            {
                if (smallest1 == 0)
                    intersectionVertices[1] = isectA.p1;
                else
                    intersectionVertices[1] = isectA.p0;
            }
        }
        else
        {
            if (smallest2 == 0)
                intersectionVertices[0] = isectB.p0;
            else
                intersectionVertices[0] = isectB.p1;

            if (isectB.s1 > isectA.s1)
            {
                if (smallest1 == 0)
                    intersectionVertices[1] = isectA.p1;
                else
                    intersectionVertices[1] = isectA.p0;
            }
            else
            {
                if (smallest2 == 0)
                    intersectionVertices[1] = isectB.p1;
                else
                    intersectionVertices[1] = isectB.p0;
            }
        }

        return 1;
    }

    protected static TriangleIntersection compute_intervals_isectline(Vec4[] v, double vv0, double vv1, double vv2,
        double d0, double d1, double d2,
        double d0d1, double d0d2)
    {
        if (d0d1 > 0) // D0, D1 are on the same side, D2 on the other or on the plane
            return intersect(v[2], v[0], v[1], vv2, vv0, vv1, d2, d0, d1);
        else if (d0d2 > 0)
            return intersect(v[1], v[0], v[2], vv1, vv0, vv2, d1, d0, d2);
        else if (d1 * d2 > 0 || d0 != 0)
            return intersect(v[0], v[1], v[2], vv0, vv1, vv2, d0, d1, d2);
        else if (d1 != 0)
            return intersect(v[1], v[0], v[2], vv1, vv0, vv2, d1, d0, d2);
        else if (d2 != 0)
            return intersect(v[2], v[0], v[1], vv2, vv0, vv1, d2, d0, d1);
        else
            return null; // triangles are coplanar
    }

    protected static TriangleIntersection intersect(Vec4 v0, Vec4 v1, Vec4 v2, double vv0, double vv1, double vv2,
        double d0, double d1, double d2)
    {
        TriangleIntersection intersection = new TriangleIntersection();

        double tmp = d0 / (d0 - d1);
        intersection.s0 = vv0 + (vv1 - vv0) * tmp;
        Vec4 diff = v1.subtract3(v0);
        diff = diff.multiply3(tmp);
        intersection.p0 = diff.add3(v0);

        tmp = d0 / (d0 - d2);
        intersection.s1 = vv0 + (vv2 - vv0) * tmp;
        diff = v2.subtract3(v0);
        diff = diff.multiply3(tmp);
        intersection.p1 = diff.add3(v0);

        return intersection;
    }

    protected static boolean coplanarTriangles(Vec4 n, Vec4[] v, Vec4[] u)
    {
        // First project onto an axis-aligned plane that maximizes the are of the triangles.
        int i0;
        int i1;

        double[] a = new double[] {Math.abs(n.x), Math.abs(n.y), Math.abs(n.z)};
        if (a[0] > a[1]) // X > Y
        {
            if (a[0] > a[2])
            { // X is greatest
                i0 = 1;
                i1 = 2;
            }
            else
            { // Z is greatest
                i0 = 0;
                i1 = 1;
            }
        }
        else // X < Y
        {
            if (a[2] > a[1])
            { // Z is greatest
                i0 = 0;
                i1 = 1;
            }
            else
            { // Y is greatest
                i0 = 0;
                i1 = 2;
            }
        }

        // Test all edges of triangle 1 against the edges of triangle 2.
        double[] v0 = new double[] {v[0].x, v[0].y, v[0].z};
        double[] v1 = new double[] {v[1].x, v[1].y, v[1].z};
        double[] v2 = new double[] {v[2].x, v[2].y, v[2].z};

        double[] u0 = new double[] {u[0].x, u[0].y, u[0].z};
        double[] u1 = new double[] {u[1].x, u[1].y, u[1].z};
        double[] u2 = new double[] {u[2].x, u[2].y, u[2].z};

        boolean tf = triangleEdgeTest(v0, v1, u0, u1, u2, i0, i1);
        if (tf)
            return true;

        tf = triangleEdgeTest(v1, v2, u0, u1, u2, i0, i1);
        if (tf)
            return true;

        tf = triangleEdgeTest(v2, v0, u0, u1, u2, i0, i1);
        if (tf)
            return true;

        // Finally, test whether one triangle is contained in the other one.
        tf = pointInTri(v0, u0, u1, u2, i0, i1);
        if (tf)
            return true;

        return pointInTri(u0, v0, v1, v2, i0, i1);
    }

    protected static boolean triangleEdgeTest(double[] v0, double[] v1, double[] u0, double[] u1, double[] u2, int i0,
        int i1)
    {
        double ax = v1[i0] - v0[i0];
        double ay = v1[i1] - v0[i1];

        // Test edge u0:u1 against v0:v1
        boolean tf = edgeEdgeTest(v0, u0, u1, i0, i1, ax, ay);
        if (tf)
            return true;

        // Test edge u1:u2 against v0:v1
        tf = edgeEdgeTest(v0, u1, u2, i0, i1, ax, ay);
        if (tf)
            return true;

        // Test edge u2:u0 against v0:v1
        return edgeEdgeTest(v0, u2, u0, i0, i1, ax, ay);
    }

    protected static boolean edgeEdgeTest(double[] v0, double[] u0, double[] u1, int i0, int i1, double ax, double ay)
    {
        double bx = u0[i0] - u1[i0];
        double by = u0[i1] - u1[i1];
        double cx = v0[i0] - u0[i0];
        double cy = v0[i1] - u0[i1];

        double f = ay * bx - ax * by;
        double d = by * cx - bx * cy;

        if ((f > 0 && d >= 0 && d <= f) || (f < 0 && d <= 0 && d >= f))
        {
            double e = ax * cy - ay * cx;
            if (f > 0)
            {
                if (e >= 0 && e <= f)
                    return true;
            }
            else
            {
                if (e <= 0 && e >= f)
                    return true;
            }
        }

        return false;
    }

    protected static boolean pointInTri(double[] v0, double[] u0, double[] u1, double[] u2, int i0, int i1)
    {
        double a = u1[i1] - u0[i1];
        double b = -(u1[i0] - u0[i0]);
        double c = -a * u0[i0] - b * u0[i1];
        double d0 = a * v0[i0] + b * v0[i1] + c;

        a = u2[i1] - u1[i1];
        b = -(u2[i0] - u1[i0]);
        c = -a * u1[i0] - b * u1[i1];
        double d1 = a * v0[i0] + b * v0[i1] + c;

        a = u0[i1] - u2[i1];
        b = -(u0[i0] - u2[i0]);
        c = -a * u2[i0] - b * u2[i1];
        double d2 = a * v0[i0] + b * v0[i1] + c;

        return d0 * d1 > 0 && d0 * d2 > 0;
    }

    public String toString()
    {
        return "Triangle (" + a + ", " + b + ", " + c + ")";
    }
}

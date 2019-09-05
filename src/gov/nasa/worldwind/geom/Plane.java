/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

/**
 * Represents a <code>Plane</code> in Cartesian coordinates, defined by a normal vector to the plane and a signed scalar
 * value proportional to the distance of the plane from the origin. The sign of the value is relative to the direction
 * of the plane normal.
 *
 * @author Tom Gaskins
 * @version $Id: Plane.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public final class Plane
{
    private final Vec4 n; // the plane normal and proportional distance. The vector is not necessarily a unit vector.

    /**
     * Constructs a plane from a 4-D vector giving the plane normal vector and distance.
     *
     * @param vec a 4-D vector indicating the plane's normal vector and distance. The normal need not be unit length.
     *
     * @throws IllegalArgumentException if the vector is null.
     */
    public Plane(Vec4 vec)
    {
        if (vec == null)
        {
            String message = Logging.getMessage("nullValue.VectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (vec.getLengthSquared3() == 0.0)
        {
            String message = Logging.getMessage("Geom.Plane.VectorIsZero");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.n = vec;
    }

    /**
     * Constructs a plane from four values giving the plane normal vector and distance.
     *
     * @param nx the X component of the plane normal vector.
     * @param ny the Y component of the plane normal vector.
     * @param nz the Z component of the plane normal vector.
     * @param d  the plane distance.
     *
     * @throws IllegalArgumentException if the normal vector components define the zero vector (all values are zero).
     */
    public Plane(double nx, double ny, double nz, double d)
    {
        if (nx == 0.0 && ny == 0.0 && nz == 0.0)
        {
            String message = Logging.getMessage("Geom.Plane.VectorIsZero");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.n = new Vec4(nx, ny, nz, d);
    }

    /**
     * Returns the plane that passes through the specified three points. The plane's normal is the cross product of the
     * two vectors from <code>pb</code> to <code>pa</code> and <code>pc</code> to <code>pa</code>, respectively. The
     * returned plane is undefined if any of the specified points are colinear.
     *
     * @param pa the first point.
     * @param pb the second point.
     * @param pc the third point.
     *
     * @return a <code>Plane</code> passing through the specified points.
     *
     * @throws IllegalArgumentException if <code>pa</code>, <code>pb</code>, or <code>pc</code> is <code>null</code>.
     */
    public static Plane fromPoints(Vec4 pa, Vec4 pb, Vec4 pc)
    {
        if (pa == null || pb == null || pc == null)
        {
            String message = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 vab = pb.subtract3(pa);
        Vec4 vac = pc.subtract3(pa);
        Vec4 n = vab.cross3(vac);
        double d = -n.dot3(pa);

        return new Plane(n.x, n.y, n.z, d);
    }

    /**
     * Returns the plane's normal vector.
     *
     * @return the plane's normal vector.
     */
    public final Vec4 getNormal()
    {
        return this.n;//new Vec4(this.n.x, this.n.y, this.n.z);
    }

    /**
     * Returns the plane distance.
     *
     * @return the plane distance.
     */
    public final double getDistance()
    {
        return this.n.w;
    }

    /**
     * Returns a 4-D vector holding the plane's normal and distance.
     *
     * @return a 4-D vector indicating the plane's normal vector and distance.
     */
    public final Vec4 getVector()
    {
        return this.n;
    }

    /**
     * Returns a normalized version of this plane. The normalized plane's normal vector is unit length and its distance
     * is D/|N| where |N| is the length of this plane's normal vector.
     *
     * @return a normalized copy of this Plane.
     */
    public final Plane normalize()
    {
        double length = this.n.getLength3();
        if (length == 0) // should not happen, but check to be sure.
            return this;

        return new Plane(new Vec4(
            this.n.x / length,
            this.n.y / length,
            this.n.z / length,
            this.n.w / length));
    }

    /**
     * Calculates the 4-D dot product of this plane with a vector.
     *
     * @param p the vector.
     *
     * @return the dot product of the plane and the vector.
     *
     * @throws IllegalArgumentException if the vector is null.
     */
    public final double dot(Vec4 p)
    {
        if (p == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.n.x * p.x + this.n.y * p.y + this.n.z * p.z + this.n.w * p.w;
    }

    /**
     * Determine the intersection point of a line with this plane.
     *
     * @param line the line to intersect.
     *
     * @return the intersection point if the line intersects the plane, otherwise null.
     *
     * @throws IllegalArgumentException if the line is null.
     */
    public Vec4 intersect(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double t = this.intersectDistance(line);

        if (Double.isNaN(t))
            return null;

        if (Double.isInfinite(t))
            return line.getOrigin();

        return line.getPointAt(t);
    }

    /**
     * Determine the parametric point of intersection of a line with this plane.
     *
     * @param line the line to test
     *
     * @return The parametric value of the point on the line at which it intersects the plane. {@link Double#NaN} is
     *         returned if the line does not intersect the plane. {@link Double#POSITIVE_INFINITY} is returned if the
     *         line is coincident with the plane.
     *
     * @throws IllegalArgumentException if the line is null.
     */
    public double intersectDistance(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double ldotv = this.n.dot3(line.getDirection());
        if (ldotv == 0) // are line and plane parallel
        {
            double ldots = this.n.dot4(line.getOrigin());
            if (ldots == 0)
                return Double.POSITIVE_INFINITY; // line is coincident with the plane
            else
                return Double.NaN; // line is not coincident with the plane
        }

        return -this.n.dot4(line.getOrigin()) / ldotv; // ldots / ldotv
    }

    /**
     * Test a line segment for intersection with this plane. If it intersects, return the point of intersection.
     *
     * @param pa the first point of the line segment.
     * @param pb the second point of the line segment.
     *
     * @return The point of intersection with the plane. Null is returned if the segment does not instersect this plane.
     *         {@link gov.nasa.worldwind.geom.Vec4#INFINITY} coincident with the plane.
     *
     * @throws IllegalArgumentException if either input point is null.
     */
    public Vec4 intersect(Vec4 pa, Vec4 pb)
    {
        if (pa == null || pb == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            // Test if line segment is in fact a point
            if (pa.equals(pb))
            {
                double d = this.distanceTo(pa);
                if (d == 0)
                    return pa;
                else
                    return null;
            }

            Line l = Line.fromSegment(pa, pb);
            double t = this.intersectDistance(l);

            if (Double.isInfinite(t))
                return Vec4.INFINITY;

            if (Double.isNaN(t) || t < 0 || t > 1)
                return null;

            return l.getPointAt(t);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    /**
     * Clip a line segment to this plane.
     *
     * @param pa the first point of the segment.
     * @param pb the second point of the segment.
     *
     * @return An array of two points both on the positive side of the plane. If the direction of the line formed by the
     *         two points is positive with respect to this plane's normal vector, the first point in the array will be
     *         the intersection point on the plane, and the second point will be the original segment end point. If the
     *         direction of the line is negative with respect to this plane's normal vector, the first point in the
     *         array will be the original segment's begin point, and the second point will be the intersection point on
     *         the plane. If the segment does not intersect the plane, null is returned. If the segment is coincident
     *         with the plane, the input points are returned, in their input order.
     *
     * @throws IllegalArgumentException if either input point is null.
     */
    public Vec4[] clip(Vec4 pa, Vec4 pb)
    {
        if (pa == null || pb == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (pa.equals(pb))
            return null;

        // Get the projection of the segment onto the plane.
        Line line = Line.fromSegment(pa, pb);
        double ldotv = this.n.dot3(line.getDirection());

        // Are the line and plane parallel?
        if (ldotv == 0) // line and plane are parallel and maybe coincident
        {
            double ldots = this.n.dot4(line.getOrigin());
            if (ldots == 0)
                return new Vec4[] {pa, pb}; // line is coincident with the plane
            else
                return null; // line is not coincident with the plane
        }

        // Not parallel so the line intersects. But does the segment intersect?
        double t = -this.n.dot4(line.getOrigin()) / ldotv; // ldots / ldotv
        if (t < 0 || t > 1) // segment does not intersect
            return null;

        Vec4 p = line.getPointAt(t);
        if (ldotv > 0)
            return new Vec4[] {p, pb};
        else
            return new Vec4[] {pa, p};
    }

    public double distanceTo(Vec4 p)
    {
        return this.n.dot4(p);
    }

    /**
     * Determines whether two points are on the same side of a plane.
     *
     * @param pa the first point.
     * @param pb the second point.
     *
     * @return true if the points are on the same side of the plane, otherwise false.
     *
     * @throws IllegalArgumentException if either point is null.
     */
    public int onSameSide(Vec4 pa, Vec4 pb)
    {
        if (pa == null || pb == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double da = this.distanceTo(pa);
        double db = this.distanceTo(pb);

        if (da < 0 && db < 0)
            return -1;

        if (da > 0 && db > 0)
            return 1;

        return 0;
    }

    /**
     * Determines whether multiple points are on the same side of a plane.
     *
     * @param pts the array of points.
     *
     * @return true if the points are on the same side of the plane, otherwise false.
     *
     * @throws IllegalArgumentException if the points array is null or any point within it is null.
     */
    public int onSameSide(Vec4[] pts)
    {
        if (pts == null)
        {
            String message = Logging.getMessage("nullValue.PointsArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double d = this.distanceTo(pts[0]);
        int side = d < 0 ? -1 : d > 0 ? 1 : 0;
        if (side == 0)
            return 0;

        for (int i = 1; i < pts.length; i++)
        {
            if (pts[i] == null)
            {
                String message = Logging.getMessage("nullValue.PointIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            d = this.distanceTo(pts[i]);
            if ((side == -1 && d < 0) || (side == 1 && d > 0))
                continue;

            return 0; // point is not on same side as the others
        }

        return side;
    }

    /**
     * Compute the intersection of three planes.
     *
     * @param pa the first plane.
     * @param pb the second plane.
     * @param pc the third plane.
     *
     * @return the Cartesian coordinates of the intersection, or null if the three planes to not intersect at a point.
     *
     * @throws IllegalArgumentException if any of the planes are null.
     */
    public static Vec4 intersect(Plane pa, Plane pb, Plane pc)
    {
        if (pa == null || pb == null || pc == null)
        {
            String message = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 na = pa.getNormal();
        Vec4 nb = pb.getNormal();
        Vec4 nc = pc.getNormal();

        Matrix m = new Matrix(
            na.x, na.y, na.z, 0,
            nb.x, nb.y, nb.z, 0,
            nc.x, nc.y, nc.z, 0,
            0, 0, 0, 1, true
        );

        Matrix mInverse = m.getInverse();

        Vec4 D = new Vec4(-pa.getDistance(), -pb.getDistance(), -pc.getDistance());

        return D.transformBy3(mInverse);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Plane))
            return false;

        Plane plane = (Plane) o;

        return !(n != null ? !n.equals(plane.n) : plane.n != null);
    }

    @Override
    public int hashCode()
    {
        return n != null ? n.hashCode() : 0;
    }

    @Override
    public final String toString()
    {
        return this.n.toString();
    }
}

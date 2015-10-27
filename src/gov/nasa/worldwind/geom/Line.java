/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

/**
 * @author Tom Gaskins
 * @version $Id: Line.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public final class Line// Instances are immutable
{
    private final Vec4 origin;
    private final Vec4 direction;

    /**
     * Create the line containing a line segement between two points.
     *
     * @param pa the first point of the line segment.
     * @param pb the second point of the line segment.
     *
     * @return The line containing the two points.
     *
     * @throws IllegalArgumentException if either point is null or they are coincident.
     */
    public static Line fromSegment(Vec4 pa, Vec4 pb)
    {
        return new Line(pa, new Vec4(pb.x - pa.x, pb.y - pa.y, pb.z - pa.z, 0));
    }

    /**
     * @param origin    the origin of the line being constructed
     * @param direction the direction of the line being constructed
     *
     * @throws IllegalArgumentException if <code>origin</code> is null, or <code>direction</code> is null or has zero
     *                                  length
     */
    public Line(Vec4 origin, Vec4 direction)
    {
        String message = null;
        if (origin == null)
            message = "nullValue.OriginIsNull";
        else if (direction == null)
            message = "nullValue.DirectionIsNull";
        else if (direction.getLength3() <= 0)
            message = "Geom.Line.DirectionIsZeroVector";
        if (message != null)
        {
            message = Logging.getMessage(message);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.origin = origin;
        this.direction = direction;
    }

    public final Vec4 getDirection()
    {
        return direction;
    }

    public final Vec4 getOrigin()
    {
        return origin;
    }

    public final Vec4 getPointAt(double t)
    {
        return Vec4.fromLine3(this.origin, t, this.direction);
    }

    public final double selfDot()
    {
        return this.origin.dot3(this.direction);
    }

    /**
     * Performs a comparison to test whether this Object is internally identical to the other Object <code>o</code>.
     * This method takes into account both direction and origin, so two lines which may be equivalent may not be
     * considered equal.
     *
     * @param o the object to be compared against.
     *
     * @return true if these two objects are equal, false otherwise
     */
    @Override
    public final boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final gov.nasa.worldwind.geom.Line line = (gov.nasa.worldwind.geom.Line) o;

        if (!direction.equals(line.direction))
            return false;
        //noinspection RedundantIfStatement
        if (!line.origin.equals(origin))
            return false;

        return true;
    }

    @Override
    public final int hashCode()
    {
        int result;
        result = origin.hashCode();
        result = 29 * result + direction.hashCode();
        return result;
    }

    public String toString()
    {
        return "Origin: " + this.origin + ", Direction: " + this.direction;
    }
//
//    public final double distanceToOld(Vec4 p)
//    {
//        if (p == null)
//        {
//            String message = Logging.getMessage("nullValue.PointIsNull");
//            Logging.logger().severe(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        Vec4 origin = this.origin;
//        Vec4 sideB = origin.subtract3(p); // really a vector
//
//        double distanceToOrigin = sideB.dot3(this.direction);
//        double divisor = distanceToOrigin / this.direction.getLengthSquared3();
//
//        Vec4 sideA = this.direction.multiply3(divisor);
//
//        double aSquared = sideA.getLengthSquared3();
//        double bSquared = sideB.getLengthSquared3();
//
//        return Math.sqrt(bSquared - aSquared);
//    }

    public final Vec4 nearestPointTo(Vec4 p)
    {
        Vec4 w = p.subtract3(this.origin);

        double c1 = w.dot3(this.direction);
        double c2 = this.direction.dot3(this.direction);

        return this.origin.add3(this.direction.multiply3(c1 / c2));
    }

    /**
     * Calculate the shortests distance between this line and a specified <code>Vec4</code>. This method returns a
     * positive distance.
     *
     * @param p the <code>Vec4</code> whose distance from this <code>Line</code> will be calculated
     *
     * @return the distance between this <code>Line</code> and the specified <code>Vec4</code>
     *
     * @throws IllegalArgumentException if <code>p</code> is null
     */
    public final double distanceTo(Vec4 p)
    {
        return p.distanceTo3(this.nearestPointTo(p));
    }

    /**
     * Finds the closest point to a third point of a segment defined by two points.
     *
     * @param p0 The first endpoint of the segment.
     * @param p1 The second endpoint of the segment.
     * @param p  The point outside the segment whose closest point on the segment is desired.
     *
     * @return The closest point on (p0, p1) to p. Note that this will be p0 or p1 themselves whenever the closest
     *         point on the <em>line</em> defined by p0 and p1 is outside the segment (i.e., the results are bounded by
     *         the segment endpoints).
     */
    public static Vec4 nearestPointOnSegment(Vec4 p0, Vec4 p1, Vec4 p)
    {
        Vec4 v = p1.subtract3(p0);
        Vec4 w = p.subtract3(p0);

        double c1 = w.dot3(v);
        double c2 = v.dot3(v);

        if (c1 <= 0)
            return p0;
        if (c2 <= c1)
            return p1;

        return p0.add3(v.multiply3(c1 / c2));
    }

    public static double distanceToSegment(Vec4 p0, Vec4 p1, Vec4 p)
    {
        Vec4 pb = nearestPointOnSegment(p0, p1, p);

        return p.distanceTo3(pb);
    }

    /**
     * Clip a line segment to a frustum, returning the end points of the portion of the segment that is within the
     * frustum.
     *
     * @param pa      the first point of the segment.
     * @param pb      the second point of the segment.
     * @param frustum the frustum.
     *
     * @return The two points at which the segment intersects the frustum, or null if the segment does not intersect and
     *         the frustum does not fully contain it. If the segment is coincident with a plane of the frustum, the
     *         returned segment is the portion of the original segment on that plane, clipped to the other frustum
     *         planes.
     */
    public static Vec4[] clipToFrustum(Vec4 pa, Vec4 pb, Frustum frustum)
    {
        return clipToFrustum(pa, pb, frustum, 1);
    }

    private static Vec4[] clipToFrustum(Vec4 pa, Vec4 pb, Frustum frustum, int maxRecursionCount)
    {
        if (pa == null || pb == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (frustum == null)
        {
            String message = Logging.getMessage("nullValue.FrustumIsNull");

            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // First do a trivial accept test.
        if (frustum.contains(pa) && frustum.contains(pb))
            return new Vec4[] {pa, pb};

        Vec4[] segment = new Vec4[] {pa, pb};
        Vec4[] ipts;

        for (Plane p : frustum.getAllPlanes())
        {
            // See if both points are behind the plane and therefore not in the frustum.
            if (p.onSameSide(segment[0], segment[1]) < 0)
                return null;

            // Clip the segment to the plane if they intersect.
            ipts = p.clip(segment[0], segment[1]);
            if (ipts != null)
            {
                segment = ipts;
            }
        }

        // If one of the initial points was in the frustum then the segment must have been clipped.
        if (frustum.contains(pa) || frustum.contains(pb))
            return segment;

        // The segment was clipped by an infinite frustum plane but may still lie outside the frustum.
        // So recurse using the clipped segment.
        if (maxRecursionCount > 0)
            return clipToFrustum(segment[0], segment[1], frustum, --maxRecursionCount);
        else
            return segment;
    }

    /**
     * Determine if a point is behind the <code>Line</code>'s origin.
     *
     * @param point The point to test.
     *
     * @return true if <code>point</code> is behind this <code>Line</code>'s origin, false otherwise.
     */
    public boolean isPointBehindLineOrigin(Vec4 point)
    {
        double dot = point.subtract3(this.getOrigin()).dot3(this.getDirection());
        return dot < 0.0;
    }

    public Vec4 nearestIntersectionPoint(Intersection[] intersections)
    {
        Vec4 intersectionPoint = null;

        // Find the nearest intersection that's in front of the ray origin.
        double nearestDistance = Double.MAX_VALUE;
        for (Intersection intersection : intersections)
        {
            // Ignore any intersections behind the line origin.
            if (!this.isPointBehindLineOrigin(intersection.getIntersectionPoint()))
            {
                double d = intersection.getIntersectionPoint().distanceTo3(this.getOrigin());
                if (d < nearestDistance)
                {
                    intersectionPoint = intersection.getIntersectionPoint();
                    nearestDistance = d;
                }
            }
        }

        return intersectionPoint;
    }
}

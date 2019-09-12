/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import java.util.*;

/**
 * Represents a geometric cylinder, most often used as a bounding volume. <code>Cylinder</code>s are immutable.
 *
 * @author Tom Gaskins
 * @version $Id: Cylinder.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Cylinder implements Extent, Renderable
{
    protected final Vec4 bottomCenter; // point at center of cylinder base
    protected final Vec4 topCenter; // point at center of cylinder top
    protected final Vec4 axisUnitDirection; // axis as unit vector from bottomCenter to topCenter
    protected final double cylinderRadius;
    protected final double cylinderHeight;

    /**
     * Create a Cylinder from two points and a radius.
     *
     * @param bottomCenter   the center point of of the cylinder's base.
     * @param topCenter      the center point of the cylinders top.
     * @param cylinderRadius the cylinder's radius.
     *
     * @throws IllegalArgumentException if the radius is zero or the top or bottom point is null or they are
     *                                  coincident.
     */
    public Cylinder(Vec4 bottomCenter, Vec4 topCenter, double cylinderRadius)
    {
        if (bottomCenter == null || topCenter == null || bottomCenter.equals(topCenter))
        {
            String message = Logging.getMessage(
                bottomCenter == null || topCenter == null ? "nullValue.EndPointIsNull" : "generic.EndPointsCoincident");

            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (cylinderRadius <= 0)
        {
            String message = Logging.getMessage("Geom.Cylinder.RadiusIsZeroOrNegative", cylinderRadius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Convert the bottom center and top center points to points in four-dimensional homogeneous coordinates to
        // ensure that their w-coordinates are 1. Cylinder's intersection tests compute a dot product between these
        // points and each frustum plane, which depends on a w-coordinate of 1. We convert each point at construction to
        // avoid the additional overhead of converting them during every intersection test.
        this.bottomCenter = bottomCenter.toHomogeneousPoint3();
        this.topCenter = topCenter.toHomogeneousPoint3();
        this.cylinderHeight = this.bottomCenter.distanceTo3(this.topCenter);
        this.cylinderRadius = cylinderRadius;
        this.axisUnitDirection = this.topCenter.subtract3(this.bottomCenter).normalize3();
    }

    /**
     * Create a Cylinder from two points, a radius and an axis direction. Provided for use when unit axis is know and
     * computation of it can be avoided.
     *
     * @param bottomCenter   the center point of of the cylinder's base.
     * @param topCenter      the center point of the cylinders top.
     * @param cylinderRadius the cylinder's radius.
     * @param unitDirection  the unit-length axis of the cylinder.
     *
     * @throws IllegalArgumentException if the radius is zero or the top or bottom point is null or they are
     *                                  coincident.
     */
    public Cylinder(Vec4 bottomCenter, Vec4 topCenter, double cylinderRadius, Vec4 unitDirection)
    {
        if (bottomCenter == null || topCenter == null || bottomCenter.equals(topCenter))
        {
            String message = Logging.getMessage(
                bottomCenter == null || topCenter == null ? "nullValue.EndPointIsNull" : "generic.EndPointsCoincident");

            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (cylinderRadius <= 0)
        {
            String message = Logging.getMessage("Geom.Cylinder.RadiusIsZeroOrNegative", cylinderRadius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Convert the bottom center and top center points to points in four-dimensional homogeneous coordinates to
        // ensure that their w-coordinates are 1. Cylinder's intersection tests compute a dot product between these
        // points and each frustum plane, which depends on a w-coordinate of 1. We convert each point at construction to
        // avoid the additional overhead of converting them during every intersection test.
        this.bottomCenter = bottomCenter.toHomogeneousPoint3();
        this.topCenter = topCenter.toHomogeneousPoint3();
        this.cylinderHeight = this.bottomCenter.distanceTo3(this.topCenter);
        this.cylinderRadius = cylinderRadius;
        this.axisUnitDirection = unitDirection;
    }

    /**
     * Returns the unit-length axis of this cylinder.
     *
     * @return the unit-length axis of this cylinder.
     */
    public Vec4 getAxisUnitDirection()
    {
        return axisUnitDirection;
    }

    /**
     * Returns the this cylinder's bottom-center point.
     *
     * @return this cylinder's bottom-center point.
     */
    public Vec4 getBottomCenter()
    {
        return bottomCenter;
    }

    /**
     * Returns the this cylinder's top-center point.
     *
     * @return this cylinder's top-center point.
     */
    public Vec4 getTopCenter()
    {
        return topCenter;
    }

    /**
     * Returns this cylinder's radius.
     *
     * @return this cylinder's radius.
     */
    public double getCylinderRadius()
    {
        return cylinderRadius;
    }

    /**
     * Returns this cylinder's height.
     *
     * @return this cylinder's height.
     */
    public double getCylinderHeight()
    {
        return cylinderHeight;
    }

    /**
     * Return this cylinder's center point.
     *
     * @return this cylinder's center point.
     */
    public Vec4 getCenter()
    {
        Vec4 b = this.bottomCenter;
        Vec4 t = this.topCenter;
        return new Vec4(
            (b.x + t.x) / 2.0,
            (b.y + t.y) / 2.0,
            (b.z + t.z) / 2.0);
    }

    /** {@inheritDoc} */
    public double getDiameter()
    {
        return 2 * this.getRadius();
    }

    /** {@inheritDoc} */
    public double getRadius()
    {
        // return the radius of the enclosing sphere
        double halfHeight = this.bottomCenter.distanceTo3(this.topCenter) / 2.0;
        return Math.sqrt(halfHeight * halfHeight + this.cylinderRadius * this.cylinderRadius);
    }

    /**
     * Return this cylinder's volume.
     *
     * @return this cylinder's volume.
     */
    public double getVolume()
    {
        return Math.PI * this.cylinderRadius * this.cylinderRadius * this.cylinderHeight;
    }

    /**
     * Compute a bounding cylinder for a collection of points.
     *
     * @param points the points to compute a bounding cylinder for.
     *
     * @return a cylinder bounding all the points. The axis of the cylinder is the longest principal axis of the
     *         collection. (See {@link WWMath#computePrincipalAxes(Iterable)}.
     *
     * @throws IllegalArgumentException if the point list is null or empty.
     * @see #computeVerticalBoundingCylinder(gov.nasa.worldwind.globes.Globe, double, Sector)
     */
    public static Cylinder computeBoundingCylinder(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String message = Logging.getMessage("nullValue.PointListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4[] axes = WWMath.computePrincipalAxes(points);
        if (axes == null)
        {
            String message = Logging.getMessage("generic.ListIsEmpty");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 r = axes[0];
        Vec4 s = axes[1];

        List<Vec4> sPlanePoints = new ArrayList<Vec4>();
        double minDotR = Double.MAX_VALUE;
        double maxDotR = -minDotR;

        for (Vec4 p : points)
        {
            double pdr = p.dot3(r);
            sPlanePoints.add(p.subtract3(r.multiply3(p.dot3(r))));

            if (pdr < minDotR)
                minDotR = pdr;
            if (pdr > maxDotR)
                maxDotR = pdr;
        }

        Vec4 minPoint = sPlanePoints.get(0);
        Vec4 maxPoint = minPoint;
        double minDotS = Double.MAX_VALUE;
        double maxDotS = -minDotS;
        for (Vec4 p : sPlanePoints)
        {
            double d = p.dot3(s);
            if (d < minDotS)
            {
                minPoint = p;
                minDotS = d;
            }
            if (d > maxDotS)
            {
                maxPoint = p;
                maxDotS = d;
            }
        }

        Vec4 center = minPoint.add3(maxPoint).divide3(2);
        double radius = center.distanceTo3(minPoint);

        for (Vec4 h : sPlanePoints)
        {
            Vec4 hq = h.subtract3(center);
            double d = hq.getLength3();
            if (d > radius)
            {
                Vec4 g = center.subtract3(hq.normalize3().multiply3(radius));
                center = g.add3(h).divide3(2);
                radius = d;
            }
        }

        Vec4 bottomCenter = center.add3(r.multiply3(minDotR));
        Vec4 topCenter = center.add3((r.multiply3(maxDotR)));

        if (radius == 0)
            radius = 1;

        if (bottomCenter.equals(topCenter))
            topCenter = bottomCenter.add3(new Vec4(1, 0, 0));

        return new Cylinder(bottomCenter, topCenter, radius);
    }

    /** {@inheritDoc} */
    public Intersection[] intersect(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] tVals = new double[2];
        if (!intcyl(line.getOrigin(), line.getDirection(), this.bottomCenter, this.axisUnitDirection,
            this.cylinderRadius, tVals))
            return null;

        if (!clipcyl(line.getOrigin(), line.getDirection(), this.bottomCenter, this.topCenter,
            this.axisUnitDirection, tVals))
            return null;

        if (!Double.isInfinite(tVals[0]) && !Double.isInfinite(tVals[1]) && tVals[0] >= 0.0 && tVals[1] >= 0.0)
            return new Intersection[] {new Intersection(line.getPointAt(tVals[0]), false),
                new Intersection(line.getPointAt(tVals[1]), false)};
        if (!Double.isInfinite(tVals[0]) && tVals[0] >= 0.0)
            return new Intersection[] {new Intersection(line.getPointAt(tVals[0]), false)};
        if (!Double.isInfinite(tVals[1]) && tVals[1] >= 0.0)
            return new Intersection[] {new Intersection(line.getPointAt(tVals[1]), false)};
        return null;
    }

    /** {@inheritDoc} */
    public boolean intersects(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return intersect(line) != null;
    }

    // Taken from "Graphics Gems IV", Section V.2, page 356.

    protected boolean intcyl(Vec4 raybase, Vec4 raycos, Vec4 base, Vec4 axis, double radius, double[] tVals)
    {
        boolean hit; // True if ray intersects cyl
        Vec4 RC; // Ray base to cylinder base
        double d; // Shortest distance between the ray and the cylinder
        double t, s; // Distances along the ray
        Vec4 n, D, O;
        double ln;

        RC = raybase.subtract3(base);
        n = raycos.cross3(axis);

        // Ray is parallel to the cylinder's axis.
        if ((ln = n.getLength3()) == 0.0)
        {
            d = RC.dot3(axis);
            D = RC.subtract3(axis.multiply3(d));
            d = D.getLength3();
            tVals[0] = Double.NEGATIVE_INFINITY;
            tVals[1] = Double.POSITIVE_INFINITY;
            // True if ray is in cylinder.
            return d <= radius;
        }

        n = n.normalize3();
        d = Math.abs(RC.dot3(n)); // Shortest distance.
        hit = (d <= radius);

        // If ray hits cylinder.
        if (hit)
        {
            O = RC.cross3(axis);
            t = -O.dot3(n) / ln;
            O = n.cross3(axis);
            O = O.normalize3();
            s = Math.abs(Math.sqrt(radius * radius - d * d) / raycos.dot3(O));
            tVals[0] = t - s; // Entering distance.
            tVals[1] = t + s; // Exiting distance.
        }

        return hit;
    }

    // Taken from "Graphics Gems IV", Section V.2, page 356.

    protected boolean clipcyl(Vec4 raybase, Vec4 raycos, Vec4 bot, Vec4 top, Vec4 axis, double[] tVals)
    {
        double dc, dwb, dwt, tb, tt;
        double in, out; // Object intersection distances.

        in = tVals[0];
        out = tVals[1];

        dc = axis.dot3(raycos);
        dwb = axis.dot3(raybase) - axis.dot3(bot);
        dwt = axis.dot3(raybase) - axis.dot3(top);

        // Ray is parallel to the cylinder end-caps.
        if (dc == 0.0)
        {
            if (dwb <= 0.0)
                return false;
            if (dwt >= 0.0)
                return false;
        }
        else
        {
            // Intersect the ray with the bottom end-cap.
            tb = -dwb / dc;
            // Intersect the ray with the top end-cap.
            tt = -dwt / dc;

            // Bottom is near cap, top is far cap.
            if (dc >= 0.0)
            {
                if (tb > out)
                    return false;
                if (tt < in)
                    return false;
                if (tb > in && tb < out)
                    in = tb;
                if (tt > in && tt < out)
                    out = tt;
            }
            // Bottom is far cap, top is near cap.
            else
            {
                if (tb < in)
                    return false;
                if (tt > out)
                    return false;
                if (tb > in && tb < out)
                    out = tb;
                if (tt > in && tt < out)
                    in = tt;
            }
        }

        tVals[0] = in;
        tVals[1] = out;
        return in < out;
    }

    protected double intersects(Plane plane, double effectiveRadius)
    {
        // Test the distance from the first cylinder end-point. Assumes that bottomCenter's w-coordinate is 1.
        double dq1 = plane.dot(this.bottomCenter);
        boolean bq1 = dq1 <= -effectiveRadius;

        // Test the distance from the top of the cylinder. Assumes that topCenter's w-coordinate is 1.
        double dq2 = plane.dot(this.topCenter);
        boolean bq2 = dq2 <= -effectiveRadius;

        if (bq1 && bq2) // both beyond effective radius; cylinder is on negative side of plane
            return -1;

        if (bq1 == bq2) // both within effective radius; can't draw any conclusions
            return 0;

        return 1; // Cylinder almost certainly intersects
    }

    protected double intersectsAt(Plane plane, double effectiveRadius, Vec4[] endpoints)
    {
        // Test the distance from the first end-point. Assumes that the first end-point's w-coordinate is 1.
        double dq1 = plane.dot(endpoints[0]);
        boolean bq1 = dq1 <= -effectiveRadius;

        // Test the distance from the possibly reduced second cylinder end-point. Assumes that the second end-point's
        // w-coordinate is 1.
        double dq2 = plane.dot(endpoints[1]);
        boolean bq2 = dq2 <= -effectiveRadius;

        if (bq1 && bq2) // endpoints more distant from plane than effective radius; cylinder is on neg. side of plane
            return -1;

        if (bq1 == bq2) // endpoints less distant from plane than effective radius; can't draw any conclusions
            return 0;

        // Compute and return the endpoints of the cylinder on the positive side of the plane.
        double t = (effectiveRadius + dq1) / plane.getNormal().dot3(endpoints[0].subtract3(endpoints[1]));

        Vec4 newEndPoint = endpoints[0].add3(endpoints[1].subtract3(endpoints[0]).multiply3(t));
        if (bq1) // Truncate the lower end of the cylinder
            endpoints[0] = newEndPoint;
        else // Truncate the upper end of the cylinder
            endpoints[1] = newEndPoint;

        return t;
    }

    /** {@inheritDoc} */
    public double getEffectiveRadius(Plane plane)
    {
        if (plane == null)
            return 0;

        // Determine the effective radius of the cylinder axis relative to the plane.
        double dot = plane.getNormal().dot3(this.axisUnitDirection);
        double scale = 1d - dot * dot;
        if (scale <= 0)
            return 0;
        else
            return this.cylinderRadius * Math.sqrt(scale);
    }

    /** {@inheritDoc} */
    public boolean intersects(Plane plane)
    {
        if (plane == null)
        {
            String message = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double effectiveRadius = this.getEffectiveRadius(plane);
        return this.intersects(plane, effectiveRadius) >= 0;
    }

    /** {@inheritDoc} */
    public boolean intersects(Frustum frustum)
    {
        if (frustum == null)
        {
            String message = Logging.getMessage("nullValue.FrustumIsNull");

            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double intersectionPoint;
        Vec4[] endPoints = new Vec4[] {this.bottomCenter, this.topCenter};

        double effectiveRadius = this.getEffectiveRadius(frustum.getNear());
        intersectionPoint = this.intersectsAt(frustum.getNear(), effectiveRadius, endPoints);
        if (intersectionPoint < 0)
            return false;

        // Near and far have the same effective radius.
        intersectionPoint = this.intersectsAt(frustum.getFar(), effectiveRadius, endPoints);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getLeft());
        intersectionPoint = this.intersectsAt(frustum.getLeft(), effectiveRadius, endPoints);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getRight());
        intersectionPoint = this.intersectsAt(frustum.getRight(), effectiveRadius, endPoints);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getTop());
        intersectionPoint = this.intersectsAt(frustum.getTop(), effectiveRadius, endPoints);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getBottom());
        intersectionPoint = this.intersectsAt(frustum.getBottom(), effectiveRadius, endPoints);
        return intersectionPoint >= 0;
    }

    /** {@inheritDoc} */
    public double getProjectedArea(View view)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO: compute a more exact projected screen area for Cylinder.
        return WWMath.computeSphereProjectedArea(view, this.getCenter(), this.getRadius());
    }

    /**
     * Returns a cylinder that minimally surrounds the specified minimum and maximum elevations in the sector at a
     * specified vertical exaggeration, and is oriented such that the cylinder axis is perpendicular to the globe's
     * surface.
     *
     * @param globe                The globe associated with the sector.
     * @param verticalExaggeration the vertical exaggeration to apply to the minimum and maximum elevations when
     *                             computing the cylinder.
     * @param sector               the sector to return the bounding cylinder for.
     *
     * @return The minimal bounding cylinder in Cartesian coordinates.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null
     * @see #computeBoundingCylinder(Iterable)
     */
    static public Cylinder computeVerticalBoundingCylinder(Globe globe, double verticalExaggeration, Sector sector)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double[] minAndMaxElevations = globe.getMinAndMaxElevations(sector);
        return computeVerticalBoundingCylinder(globe, verticalExaggeration, sector,
            minAndMaxElevations[0], minAndMaxElevations[1]);
    }

    /**
     * Returns a cylinder that minimally surrounds the specified minimum and maximum elevations in the sector at a
     * specified vertical exaggeration, and is oriented such that the cylinder axis is perpendicular to the globe's
     * surface.
     *
     * @param globe                The globe associated with the sector.
     * @param verticalExaggeration the vertical exaggeration to apply to the minimum and maximum elevations when
     *                             computing the cylinder.
     * @param sector               the sector to return the bounding cylinder for.
     * @param minElevation         the minimum elevation of the bounding cylinder.
     * @param maxElevation         the maximum elevation of the bounding cylinder.
     *
     * @return The minimal bounding cylinder in Cartesian coordinates.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null
     * @see #computeBoundingCylinder(Iterable)
     */
    public static Cylinder computeVerticalBoundingCylinder(Globe globe, double verticalExaggeration, Sector sector,
        double minElevation, double maxElevation)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute the exaggerated minimum and maximum heights.
        double minHeight = minElevation * verticalExaggeration;
        double maxHeight = maxElevation * verticalExaggeration;

        if (minHeight == maxHeight)
            maxHeight = minHeight + 1; // ensure the top and bottom of the cylinder won't be coincident

        // If the sector spans both poles in latitude, or spans greater than 180 degrees in longitude, we cannot use the
        // sector's Cartesian quadrilateral to compute a bounding cylinde. This is because the quadrilateral is either
        // smaller than the geometry defined by the sector (when deltaLon >= 180), or the quadrilateral degenerates to
        // two points (when deltaLat >= 180). So we compute a bounging cylinder that spans the equator and covers the
        // sector's latitude range. In some cases this cylinder may be too large, but we're typically not interested
        // in culling these cylinders since the sector will span most of the globe.
        if (sector.getDeltaLatDegrees() >= 180d || sector.getDeltaLonDegrees() >= 180d)
        {
            return computeVerticalBoundsFromSectorLatitudeRange(globe, sector, minHeight, maxHeight);
        }
        // Otherwise, create a standard bounding cylinder that minimally surrounds the specified sector and elevations.
        else
        {
            return computeVerticalBoundsFromSectorQuadrilateral(globe, sector, minHeight, maxHeight);
        }
    }

    /**
     * Compute the Cylinder that surrounds the equator, and has height defined by the sector's minumum and maximum
     * latitudes (including maxHeight).
     *
     * @param globe     The globe associated with the sector.
     * @param sector    the sector to return the bounding cylinder for.
     * @param minHeight the minimum height to include in the bounding cylinder.
     * @param maxHeight the maximum height to include in the bounding cylinder.
     *
     * @return the minimal bounding cylinder in Cartesianl coordinates.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected static Cylinder computeVerticalBoundsFromSectorLatitudeRange(Globe globe, Sector sector, double minHeight,
        double maxHeight)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 centerPoint = Vec4.ZERO;
        Vec4 axis = Vec4.UNIT_Y;
        double radius = globe.getEquatorialRadius() + maxHeight;

        // Compute the sector's lowest projection along the cylinder axis. This will be a point of minimum latitude
        // with maxHeight.
        Vec4 extremePoint = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(),
            maxHeight);
        double minProj = extremePoint.subtract3(centerPoint).dot3(axis);
        // Compute the sector's lowest highest along the cylinder axis. This will be a point of maximum latitude
        // with maxHeight.
        extremePoint = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), maxHeight);
        double maxProj = extremePoint.subtract3(centerPoint).dot3(axis);

        Vec4 bottomCenterPoint = axis.multiply3(minProj).add3(centerPoint);
        Vec4 topCenterPoint = axis.multiply3(maxProj).add3(centerPoint);

        if (radius == 0)
            radius = 1;

        if (bottomCenterPoint.equals(topCenterPoint))
            topCenterPoint = bottomCenterPoint.add3(new Vec4(1, 0, 0));

        return new Cylinder(bottomCenterPoint, topCenterPoint, radius);
    }

    /**
     * Returns a cylinder that minimally surrounds the specified height range in the sector.
     *
     * @param globe     The globe associated with the sector.
     * @param sector    the sector to return the bounding cylinder for.
     * @param minHeight the minimum height to include in the bounding cylinder.
     * @param maxHeight the maximum height to include in the bounding cylinder.
     *
     * @return The minimal bounding cylinder in Cartesian coordinates.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null
     */
    protected static Cylinder computeVerticalBoundsFromSectorQuadrilateral(Globe globe, Sector sector, double minHeight,
        double maxHeight)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Get three non-coincident points on the sector's quadrilateral. We choose the north or south pair that is
        // closest to the equator, then choose a third point from the opposite pair. We use maxHeight as elevation
        // because we want to bound the largest potential quadrilateral for the sector.
        Vec4 p0, p1, p2;
        if (Math.abs(sector.getMinLatitude().degrees) <= Math.abs(sector.getMaxLatitude().degrees))
        {
            p0 = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMaxLongitude(), maxHeight); // SE
            p1 = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), maxHeight); // SW
            p2 = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMinLongitude(), maxHeight); // NW
        }
        else
        {
            p0 = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMinLongitude(), maxHeight); // NW
            p1 = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), maxHeight); // NE
            p2 = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), maxHeight); // SW
        }

        // Compute the center, axis, and radius of the circle that circumscribes the three points.
        // This circle is guaranteed to circumscribe all four points of the sector's Cartesian quadrilateral.
        Vec4[] centerOut = new Vec4[1];
        Vec4[] axisOut = new Vec4[1];
        double[] radiusOut = new double[1];
        if (!WWMath.computeCircleThroughPoints(p0, p1, p2, centerOut, axisOut, radiusOut))
        {
            // If the computation failed, then two of the points are coincident. Fall back to creating a bounding
            // cylinder based on the vertices of the sector. This bounding cylinder won't be as tight a fit, but
            // it will be correct.
            return computeVerticalBoundsFromSectorVertices(globe, sector, minHeight, maxHeight);
        }
        Vec4 centerPoint = centerOut[0];
        Vec4 axis = axisOut[0];
        double radius = radiusOut[0];

        // Compute the sector's lowest projection along the cylinder axis. We test opposite corners of the sector
        // using minHeight. One of these will be the lowest point in the sector.
        Vec4 extremePoint = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(),
            minHeight);
        double minProj = extremePoint.subtract3(centerPoint).dot3(axis);
        extremePoint = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), minHeight);
        minProj = Math.min(minProj, extremePoint.subtract3(centerPoint).dot3(axis));
        // Compute the sector's highest projection along the cylinder axis. We only need to use the point at the
        // sector's centroid with maxHeight. This point is guaranteed to be the highest point in the sector.
        LatLon centroid = sector.getCentroid();
        extremePoint = globe.computePointFromPosition(centroid.getLatitude(), centroid.getLongitude(), maxHeight);
        double maxProj = extremePoint.subtract3(centerPoint).dot3(axis);

        Vec4 bottomCenterPoint = axis.multiply3(minProj).add3(centerPoint);
        Vec4 topCenterPoint = axis.multiply3(maxProj).add3(centerPoint);

        if (radius == 0)
            radius = 1;

        if (bottomCenterPoint.equals(topCenterPoint))
            topCenterPoint = bottomCenterPoint.add3(new Vec4(1, 0, 0));

        return new Cylinder(bottomCenterPoint, topCenterPoint, radius);
    }

    /**
     * Returns a cylinder that surrounds the specified height range in the zero-area sector. The returned cylinder won't
     * be as tight a fit as <code>computeBoundsFromSectorQuadrilateral</code>.
     *
     * @param globe     The globe associated with the sector.
     * @param sector    the sector to return the bounding cylinder for.
     * @param minHeight the minimum height to include in the bounding cylinder.
     * @param maxHeight the maximum height to include in the bounding cylinder.
     *
     * @return The minimal bounding cylinder in Cartesian coordinates.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null
     */
    protected static Cylinder computeVerticalBoundsFromSectorVertices(Globe globe, Sector sector, double minHeight,
        double maxHeight)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute the top center point as the surface point with maxHeight at the sector's centroid.
        LatLon centroid = sector.getCentroid();
        Vec4 topCenterPoint = globe.computePointFromPosition(centroid.getLatitude(), centroid.getLongitude(),
            maxHeight);
        // Compute the axis as the surface normal at the sector's centroid.
        Vec4 axis = globe.computeSurfaceNormalAtPoint(topCenterPoint);

        // Compute the four corner points of the sector with minHeight.
        Vec4 southwest = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), minHeight);
        Vec4 southeast = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMaxLongitude(), minHeight);
        Vec4 northeast = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), minHeight);
        Vec4 northwest = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMinLongitude(), minHeight);

        // Compute the bottom center point as the lowest projection along the axis.
        double minProj = southwest.subtract3(topCenterPoint).dot3(axis);
        minProj = Math.min(minProj, southeast.subtract3(topCenterPoint).dot3(axis));
        minProj = Math.min(minProj, northeast.subtract3(topCenterPoint).dot3(axis));
        minProj = Math.min(minProj, northwest.subtract3(topCenterPoint).dot3(axis));
        Vec4 bottomCenterPoint = axis.multiply3(minProj).add3(topCenterPoint);

        // Compute the radius as the maximum distance from the top center point to any of the corner points.
        double radius = topCenterPoint.distanceTo3(southwest);
        radius = Math.max(radius, topCenterPoint.distanceTo3(southeast));
        radius = Math.max(radius, topCenterPoint.distanceTo3(northeast));
        radius = Math.max(radius, topCenterPoint.distanceTo3(northwest));

        if (radius == 0)
            radius = 1;

        if (bottomCenterPoint.equals(topCenterPoint))
            topCenterPoint = bottomCenterPoint.add3(new Vec4(1, 0, 0));

        return new Cylinder(bottomCenterPoint, topCenterPoint, radius);
    }

    /**
     * Display the cylinder.
     *
     * @param dc the current draw context.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute a matrix that will transform world coordinates to cylinder coordinates. The negative z-axis
        // will point from the cylinder's bottomCenter to its topCenter. The y-axis will be a vector that is
        // perpendicular to the cylinder's axisUnitDirection. Because the cylinder is symmetric, it does not matter
        // in what direction the y-axis points, as long as it is perpendicular to the z-axis.
        double tolerance = 1e-6;
        Vec4 upVector = (this.axisUnitDirection.cross3(Vec4.UNIT_Y).getLength3() <= tolerance) ?
            Vec4.UNIT_NEGATIVE_Z : Vec4.UNIT_Y;
        Matrix transformMatrix = Matrix.fromModelLookAt(this.bottomCenter, this.topCenter, upVector);
        double[] matrixArray = new double[16];
        transformMatrix.toArray(matrixArray, 0, false);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        OGLStackHandler ogsh = new OGLStackHandler();
        ogsh.pushAttrib(gl, GL2.GL_CURRENT_BIT | GL2.GL_ENABLE_BIT | GL2.GL_TRANSFORM_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        try
        {
            // The cylinder is drawn with as a wireframe plus a center axis. It's drawn in two passes in order to
            // visualize the portions of the cylinder above and below an intersecting surface.

            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);
            gl.glEnable(GL.GL_DEPTH_TEST);

            // Draw the axis
            gl.glDepthFunc(GL.GL_LEQUAL); // draw the part that would normally be visible
            gl.glColor4f(1f, 1f, 1f, 0.4f);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3d(this.bottomCenter.x, this.bottomCenter.y, this.bottomCenter.z);
            gl.glVertex3d(this.topCenter.x, this.topCenter.y, this.topCenter.z);
            gl.glEnd();

            gl.glDepthFunc(GL.GL_GREATER); // draw the part that is behind an intersecting surface
            gl.glColor4f(1f, 0f, 1f, 0.4f);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3d(this.bottomCenter.x, this.bottomCenter.y, this.bottomCenter.z);
            gl.glVertex3d(this.topCenter.x, this.topCenter.y, this.topCenter.z);
            gl.glEnd();

            // Draw the exterior wireframe
            ogsh.pushModelview(gl);
            gl.glMultMatrixd(matrixArray, 0);

            GLUquadric quadric = dc.getGLU().gluNewQuadric();
            dc.getGLU().gluQuadricDrawStyle(quadric, GLU.GLU_LINE);

            gl.glDepthFunc(GL.GL_LEQUAL);
            gl.glColor4f(1f, 1f, 1f, 0.5f);
            dc.getGLU().gluCylinder(quadric, this.cylinderRadius, this.cylinderRadius, this.cylinderHeight, 30, 30);

            gl.glDepthFunc(GL.GL_GREATER);
            gl.glColor4f(1f, 0f, 1f, 0.4f);
            dc.getGLU().gluCylinder(quadric, this.cylinderRadius, this.cylinderRadius, this.cylinderHeight, 30, 30);

            dc.getGLU().gluDeleteQuadric(quadric);
        }
        finally
        {
            ogsh.pop(gl);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Cylinder))
            return false;

        Cylinder cylinder = (Cylinder) o;

        if (Double.compare(cylinder.cylinderHeight, cylinderHeight) != 0)
            return false;
        if (Double.compare(cylinder.cylinderRadius, cylinderRadius) != 0)
            return false;
        if (axisUnitDirection != null ? !axisUnitDirection.equals(cylinder.axisUnitDirection)
            : cylinder.axisUnitDirection != null)
            return false;
        if (bottomCenter != null ? !bottomCenter.equals(cylinder.bottomCenter) : cylinder.bottomCenter != null)
            return false;
        //noinspection RedundantIfStatement
        if (topCenter != null ? !topCenter.equals(cylinder.topCenter) : cylinder.topCenter != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = bottomCenter != null ? bottomCenter.hashCode() : 0;
        result = 31 * result + (topCenter != null ? topCenter.hashCode() : 0);
        result = 31 * result + (axisUnitDirection != null ? axisUnitDirection.hashCode() : 0);
        temp = cylinderRadius != +0.0d ? Double.doubleToLongBits(cylinderRadius) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = cylinderHeight != +0.0d ? Double.doubleToLongBits(cylinderHeight) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public String toString()
    {
        return this.cylinderRadius + ", " + this.bottomCenter.toString() + ", " + this.topCenter.toString() + ", "
            + this.axisUnitDirection.toString();
    }
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.*;

/**
 * Represents a sphere in three dimensional space. <p/> Instances of <code>Sphere</code> are immutable. </p>
 *
 * @author Tom Gaskins
 * @version $Id: Sphere.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public final class Sphere implements Extent, Renderable
{
    public final static Sphere UNIT_SPHERE = new Sphere(Vec4.ZERO, 1);

    protected final Vec4 center;
    protected final double radius;

    /**
     * Creates a sphere that completely contains a set of points.
     *
     * @param points the <code>Vec4</code>s to be enclosed by the new Sphere
     *
     * @return a <code>Sphere</code> encompassing the given array of <code>Vec4</code>s
     *
     * @throws IllegalArgumentException if <code>points</code> is null or empty
     */
    public static Sphere createBoundingSphere(Vec4 points[])
    {
        if (points == null)
        {
            String message = Logging.getMessage("nullValue.PointsArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (points.length < 1)
        {
            String message = Logging.getMessage("Geom.Sphere.NoPointsSpecified");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Creates the sphere around the axis aligned bounding box of the input points.
        Vec4[] extrema = Vec4.computeExtrema(points);
        Vec4 center = new Vec4(
            (extrema[0].x + extrema[1].x) / 2.0,
            (extrema[0].y + extrema[1].y) / 2.0,
            (extrema[0].z + extrema[1].z) / 2.0);
        double radius = extrema[0].distanceTo3(extrema[1]) / 2.0;

        return new Sphere(center, radius);
    }

    /**
     * Creates a sphere that completely contains a set of points.
     *
     * @param buffer the Cartesian coordinates to be enclosed by the new Sphere.
     *
     * @return a <code>Sphere</code> encompassing the given coordinates.
     *
     * @throws IllegalArgumentException if <code>buffer</code> is null or contains fewer than three values.
     */
    public static Sphere createBoundingSphere(BufferWrapper buffer)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer.getBackingBuffer().position() > buffer.getBackingBuffer().limit() - 3)
        {
            String message = Logging.getMessage("Geom.Sphere.NoPointsSpecified");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Creates the sphere around the axis aligned bounding box of the input points.
        Vec4[] extrema = Vec4.computeExtrema(buffer);
        Vec4 center = new Vec4(
            (extrema[0].x + extrema[1].x) / 2.0,
            (extrema[0].y + extrema[1].y) / 2.0,
            (extrema[0].z + extrema[1].z) / 2.0);
        double radius = extrema[0].distanceTo3(extrema[1]) / 2.0;

        return new Sphere(center, radius);
    }

    /**
     * Creates a sphere that completely contains a set of Extents. This returns null if the specified Iterable is empty
     * or contains only null elements. Note that this does not compute an optimal bounding sphere. The returned sphere
     * is computed as follows: the center is the mean of all center points in the specified set of Extents, and the
     * radius is smallest value which defines a sphere originating at the computed center point and containing all the
     * specified Extents.
     *
     * @param extents the extends to be enclosed by the new Sphere.
     *
     * @return a new Sphere encompassing the given Extents.
     *
     * @throws IllegalArgumentException if the Iterable is null.
     */
    public static Sphere createBoundingSphere(Iterable<? extends Extent> extents)
    {
        if (extents == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 center = null;
        double radius = 0;
        int count = 0;

        // Compute the mean center point of the specified extents.
        for (Extent e : extents)
        {
            if (e == null)
                continue;

            center = (center != null) ? e.getCenter().add3(center) : e.getCenter();
            count++;
        }

        // If the accumulated center point is null, then the specified Iterable is empty or contains only null elements.
        // We cannot compute an enclosing extent, so just return null.
        if (center == null)
            return null;

        center = center.divide3(count);

        // Compute the maximum distance from the mean center point to the outermost point on each extent. This is
        // the radius of the enclosing extent.
        for (Extent e : extents)
        {
            if (e == null)
                continue;

            double distance = e.getCenter().distanceTo3(center) + e.getRadius();
            if (radius < distance)
                radius = distance;
        }

        return new Sphere(center, radius);
    }

    /**
     * Creates a new <code>Sphere</code> from a given center and radius. <code>radius</code> must be positive (that is,
     * greater than zero), and <code>center</code> may not be null.
     *
     * @param center the center of the new sphere
     * @param radius the radius of the new sphere
     *
     * @throws IllegalArgumentException if <code>center</code> is null or if <code>radius</code> is non-positive
     */
    public Sphere(Vec4 center, double radius)
    {
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (radius <= 0)
        {
            String message = Logging.getMessage("Geom.Sphere.RadiusIsZeroOrNegative", radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.center = center;
        this.radius = radius;
    }

    /**
     * Obtains the radius of this <code>Sphere</code>. The radus is the distance from the center to the surface. If an
     * object's distance to this sphere's center is less than or equal to the radius, then that object is at least
     * partially within this <code>Sphere</code>.
     *
     * @return the radius of this sphere
     */
    public final double getRadius()
    {
        return this.radius;
    }

    /**
     * Obtains the diameter of this <code>Sphere</code>. The diameter is twice the radius.
     *
     * @return the diameter of this <code>Sphere</code>
     */
    public final double getDiameter()
    {
        return 2 * this.radius;
    }

    /**
     * Obtains the center of this <code>Sphere</code>.
     *
     * @return the <code>Vec4</code> situated at the center of this <code>Sphere</code>
     */
    public final Vec4 getCenter()
    {
        return this.center;
    }

    /** {@inheritDoc} */
    public double getEffectiveRadius(Plane plane)
    {
        return this.getRadius();
    }

    /**
     * Computes a point on the sphere corresponding to a specified location.
     *
     * @param location the location to compute the point for.
     *
     * @return the Cartesian coordinates of the corresponding location on the sphere.
     *
     * @throws IllegalArgumentException if the location is null.
     */
    public Vec4 getPointOnSphere(LatLon location)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double sinLat = location.getLatitude().sin();

        double x = this.center.x + this.getRadius() * sinLat * location.getLongitude().cos();
        double y = this.center.y + this.getRadius() * sinLat * location.getLongitude().sin();
        double z = this.center.z + this.getRadius() * location.getLatitude().cos();

        return new Vec4(x, y, z);
    }

    /**
     * Obtains the intersections of this sphere with a line. The returned array may be either null or of zero length if
     * no intersections are discovered. It does not contain null elements and will have a size of 2 at most. Tangential
     * intersections are marked as such. <code>line</code> is considered to have infinite length in both directions.
     *
     * @param line the <code>Line</code> with which to intersect this <code>Sphere</code>
     *
     * @return an array containing all the intersections of this <code>Sphere</code> and <code>line</code>
     *
     * @throws IllegalArgumentException if <code>line</code> is null
     */
    public final Intersection[] intersect(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double a = line.getDirection().getLengthSquared3();
        double b = 2 * line.selfDot();
        double c = line.getOrigin().getLengthSquared3() - this.radius * this.radius;

        double discriminant = Sphere.discriminant(a, b, c);
        if (discriminant < 0)
            return null;

        double discriminantRoot = Math.sqrt(discriminant);
        if (discriminant == 0)
        {
            Vec4 p = line.getPointAt((-b - discriminantRoot) / (2 * a));
            return new Intersection[] {new Intersection(p, true)};
        }
        else // (discriminant > 0)
        {
            Vec4 near = line.getPointAt((-b - discriminantRoot) / (2 * a));
            Vec4 far = line.getPointAt((-b + discriminantRoot) / (2 * a));
            return new Intersection[] {new Intersection(near, false), new Intersection(far, false)};
        }
    }

    /**
     * Calculates a discriminant. A discriminant is useful to determine the number of roots to a quadratic equation. If
     * the discriminant is less than zero, there are no roots. If it equals zero, there is one root. If it is greater
     * than zero, there are two roots.
     *
     * @param a the coefficient of the second order pronumeral
     * @param b the coefficient of the first order pronumeral
     * @param c the constant parameter in the quadratic equation
     *
     * @return the discriminant "b squared minus 4ac"
     */
    private static double discriminant(double a, double b, double c)
    {
        return b * b - 4 * a * c;
    }

    /**
     * Indicates whether a specified {@link Frustum} intersects this sphere.
     *
     * @param frustum the frustum to test.
     *
     * @return true if the specified frustum intersects this sphere, otherwise false.
     *
     * @throws IllegalArgumentException if the frustum is null.
     */
    public final boolean intersects(Frustum frustum)
    {
        if (frustum == null)
        {
            String message = Logging.getMessage("nullValue.FrustumIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // See if the extent's bounding sphere is within or intersects the frustum. The dot product of the extent's
        // center point with each plane's vector provides a distance to each plane. If this distance is less than
        // -radius, the extent is completely clipped by that plane and therefore does not intersect the space enclosed
        // by this Frustum.

        Vec4 c = this.getCenter();
        double nr = -this.getRadius();

        if (frustum.getFar().dot(c) <= nr)
            return false;
        if (frustum.getLeft().dot(c) <= nr)
            return false;
        if (frustum.getRight().dot(c) <= nr)
            return false;
        if (frustum.getTop().dot(c) <= nr)
            return false;
        if (frustum.getBottom().dot(c) <= nr)
            return false;
        //noinspection RedundantIfStatement
        if (frustum.getNear().dot(c) <= nr)
            return false;

        return true;
    }

    /**
     * Tests for intersection with a <code>Line</code>.
     *
     * @param line the <code>Line</code> with which to test for intersection
     *
     * @return true if <code>line</code> intersects or makes a tangent with the surface of this <code>Sphere</code>
     *
     * @throws IllegalArgumentException if <code>line</code> is null
     */
    public boolean intersects(Line line)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        return line.distanceTo(this.center) <= this.radius;
    }

    /**
     * Tests for intersection with a <code>Plane</code>.
     *
     * @param plane the <code>Plane</code> with which to test for intersection
     *
     * @return true if <code>plane</code> intersects or makes a tangent with the surface of this <code>Sphere</code>
     *
     * @throws IllegalArgumentException if <code>plane</code> is null
     */
    public boolean intersects(Plane plane)
    {
        if (plane == null)
        {
            String msg = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double dq1 = plane.dot(this.center);
        return dq1 <= this.radius;
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

        return WWMath.computeSphereProjectedArea(view, this.getCenter(), this.getRadius());
    }

    /**
     * Causes this <code>Sphere</code> to render itself using the <code>DrawContext</code> provided. <code>dc</code> may
     * not be null.
     *
     * @param dc the <code>DrawContext</code> to be used
     *
     * @throws IllegalArgumentException if <code>dc</code> is null
     */
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_CURRENT_BIT);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glTranslated(this.center.x, this.center.y, this.center.z);
        GLUquadric quadric = dc.getGLU().gluNewQuadric();
        dc.getGLU().gluQuadricDrawStyle(quadric, GLU.GLU_LINE);
        dc.getGLU().gluSphere(quadric, this.radius, 10, 10);
        gl.glPopMatrix();
        dc.getGLU().gluDeleteQuadric(quadric);

        gl.glPopAttrib();
    }

    @Override
    public String toString()
    {
        return "Sphere: center = " + this.center.toString() + " radius = " + Double.toString(this.radius);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final gov.nasa.worldwind.geom.Sphere sphere = (gov.nasa.worldwind.geom.Sphere) o;

        if (Double.compare(sphere.radius, radius) != 0)
            return false;
        //noinspection RedundantIfStatement
        if (!center.equals(sphere.center))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = center.hashCode();
        temp = radius != +0.0d ? Double.doubleToLongBits(radius) : 0L;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

//    public final boolean intersects(Line line)
//    {
//        if (line == null)
//        {
//            String message = WorldWind.retrieveErrMsg("nullValue.LineIsNull");
//            WorldWind.logger().logger(Level.SEVERE, message);
//            throw new IllegalArgumentException(message);
//        }
//
//        double a = line.getDirection().getLengthSquared();
//        double b = 2 * line.selfDot();
//        double c = line.getOrigin().selfDot() - this.radius * this.radius;
//
//        double discriminant = Sphere.discriminant(a, b, c);
//        if (discriminant < 0)
//        {
//            return false;
//        }
//
//        return true;
//
//    }
}

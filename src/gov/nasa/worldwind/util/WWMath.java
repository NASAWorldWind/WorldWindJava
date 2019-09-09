/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.*;
import java.nio.*;
import java.util.*;
import java.util.List;

/**
 * A collection of useful math methods, all static.
 *
 * @author tag
 * @version $Id: WWMath.java 2191 2014-08-01 23:28:20Z dcollins $
 */
public class WWMath
{
    public static final double SECOND_TO_MILLIS = 1000.0;
    public static final double MINUTE_TO_MILLIS = 60.0 * SECOND_TO_MILLIS;
    public static final double HOUR_TO_MILLIS = 60.0 * MINUTE_TO_MILLIS;
    public static final double DAY_TO_MILLIS = 24.0 * HOUR_TO_MILLIS;

    public static final double METERS_TO_KILOMETERS = 1e-3;
    public static final double METERS_TO_MILES = 0.000621371192;
    public static final double METERS_TO_NAUTICAL_MILES = 0.000539956803;
    public static final double METERS_TO_YARDS = 1.0936133;
    public static final double METERS_TO_FEET = 3.280839895;

    public static final double SQUARE_METERS_TO_SQUARE_KILOMETERS = 1e-6;
    public static final double SQUARE_METERS_TO_SQUARE_MILES = 3.86102159e-7;
    public static final double SQUARE_METERS_TO_SQUARE_YARDS = 1.19599005;
    public static final double SQUARE_METERS_TO_SQUARE_FEET = 10.7639104;
    public static final double SQUARE_METERS_TO_HECTARES = 1e-4;
    public static final double SQUARE_METERS_TO_ACRES = 0.000247105381;

    public static final LatLon LONGITUDE_OFFSET_180 = LatLon.fromDegrees(0, 180);

    /**
     * Convenience method to compute the log base 2 of a value.
     *
     * @param value the value to take the log of.
     *
     * @return the log base 2 of the specified value.
     */
    public static double logBase2(double value)
    {
        return Math.log(value) / Math.log(2d);
    }

    /**
     * Convenience method for testing whether a value is a power of two.
     *
     * @param value the value to test for power of 2
     *
     * @return true if power of 2, else false
     */
    public static boolean isPowerOfTwo(int value)
    {
        return (value == powerOfTwoCeiling(value));
    }

    /**
     * Returns the value that is the nearest power of 2 greater than or equal to the given value.
     *
     * @param reference the reference value. The power of 2 returned is greater than or equal to this value.
     *
     * @return the value that is the nearest power of 2 greater than or equal to the reference value
     */
    public static int powerOfTwoCeiling(int reference)
    {
        int power = (int) Math.ceil(Math.log(reference) / Math.log(2d));
        return (int) Math.pow(2d, power);
    }

    /**
     * Returns the value that is the nearest power of 2 less than or equal to the given value.
     *
     * @param reference the reference value. The power of 2 returned is less than or equal to this value.
     *
     * @return the value that is the nearest power of 2 less than or equal to the reference value
     */
    public static int powerOfTwoFloor(int reference)
    {
        int power = (int) Math.floor(Math.log(reference) / Math.log(2d));
        return (int) Math.pow(2d, power);
    }

    /**
     * Populate an array with the successive powers of a number.
     *
     * @param base      the number whose powers to compute.
     * @param numPowers the number of powers to compute.
     *
     * @return an array containing the requested values. Each element contains the value b^i, where b is the base and i
     *         is the element number (0, 1, etc.).
     */
    protected static int[] computePowers(int base, int numPowers)
    {
        int[] powers = new int[numPowers];

        powers[0] = 1;
        for (int i = 1; i < numPowers; i++)
        {
            powers[i] += base * powers[i - 1];
        }

        return powers;
    }

    /**
     * Clamps a value to a given range.
     *
     * @param v   the value to clamp.
     * @param min the floor.
     * @param max the ceiling
     *
     * @return the nearest value such that min &lt;= v &lt;= max.
     */
    public static double clamp(double v, double min, double max)
    {
        return v < min ? min : v > max ? max : v;
    }

    /**
     * Clamps an integer value to a given range.
     *
     * @param v   the value to clamp.
     * @param min the floor.
     * @param max the ceiling
     *
     * @return the nearest value such that min &lt;= v &lt;= max.
     */
    public static int clamp(int v, int min, int max)
    {
        return v < min ? min : v > max ? max : v;
    }

    /**
     * Returns a number between 0.0 and 1.0 indicating whether a specified floating point value is before, between or
     * after the specified min and max. Returns a linear interpolation of min and max when the value is between the
     * two.
     * <p>
     * The returned number is undefined if min &gt; max. Otherwise, the returned number is equivalent to the following:
     * <ul> <li>0.0 - If value &lt; min</li> <li>1.0 - If value &gt; max</li> <li>Linear interpolation of min and max - If min
     * &lt;= value &lt;= max</li> </ul>
     *
     * @param value the value to compare to the minimum and maximum.
     * @param min   the minimum value.
     * @param max   the maximum value.
     *
     * @return a floating point number between 0.0 and 1.0, inclusive.
     */
    public static double stepValue(double value, double min, double max)
    {
        // Note: when min==max this returns 0 if the value is on or before the min, and 1 if the value is after the max.
        // The case that would cause a divide by zero error is never evaluated. The value is always less than, equal to,
        // or greater than the min/max.

        if (value <= min)
        {
            return 0;
        }
        else if (value >= max)
        {
            return 1;
        }
        else
        {
            return (value - min) / (max - min);
        }
    }

    /**
     * Returns a number between 0.0 and 1.0 indicating whether a specified floating point value is before, between or
     * after the specified min and max. Returns a smooth interpolation of min and max when the value is between the
     * two.
     * <p>
     * This method's smooth interpolation is similar to the interpolation performed by {@link #stepValue(double, double,
     * double)}, except that the first derivative of the returned number approaches zero as the value approaches the
     * minimum or maximum. This causes the returned number to ease-in and ease-out as the value travels between the
     * minimum and maximum.
     * <p>
     * The returned number is undefined if min &gt; max. Otherwise, the returned number is equivalent to the following:
     * <ul> <li>0.0 - If value &lt; min</li> <li>1.0 - If value &gt; max</li> <li>Smooth interpolation of min and max - If min
     * &lt;= value &lt;= max</li> </ul>
     *
     * @param value the value to compare to the minimum and maximum.
     * @param min   the minimum value.
     * @param max   the maximum value.
     *
     * @return a floating point number between 0.0 and 1.0, inclusive.
     */
    public static double smoothStepValue(double value, double min, double max)
    {
        // When the min and max are equivalent this cannot distinguish between the two. In this case, this returns 0 if
        // the value is on or before the min, and 1 if the value is after the max. The case that would cause a divide by
        // zero error is never evaluated. The value is always less than, equal to, or greater than the min/max.

        if (value <= min)
        {
            return 0;
        }
        else if (value >= max)
        {
            return 1;
        }
        else
        {
            double step = (value - min) / (max - min);
            return step * step * (3 - 2 * step);
        }
    }

    /**
     * Returns the interpolation factor for <code>v</code> given the specified range <code>[x, y]</code>. The
     * interpolation factor is a number between 0 and 1 (inclusive), representing the value's relative position between
     * <code>x</code> and <code>y</code>. For example, 0 corresponds to <code>x</code>, 1 corresponds to <code>y</code>,
     * and anything in between corresponds to a linear combination of <code>x</code> and <code>y</code>.
     *
     * @param v the value to compute the interpolation factor for.
     * @param x the first value.
     * @param y the second value.
     *
     * @return the interpolation factor for <code>v</code> given the specified range <code>[x, y]</code>
     */
    public static double computeInterpolationFactor(double v, double x, double y)
    {
        return clamp((v - x) / (y - x), 0d, 1d);
    }

    /**
     * Returns the linear interpolation of <code>x</code> and <code>y</code> according to the function: <code>(1 - a) *
     * x + a * y</code>. The interpolation factor <code>a</code> defines the weight given to each value, and is clamped
     * to the range [0, 1]. If <code>a</code> is 0 or less, this returns x. If <code>a</code> is 1 or more, this returns
     * <code>y</code>. Otherwise, this returns the linear interpolation of <code>x</code> and <code>y</code>. For
     * example, when <code>a</code> is <code>0.5</code> this returns <code>(x + y)/2</code>.
     *
     * @param a the interpolation factor.
     * @param x the first value.
     * @param y the second value.
     *
     * @return the linear interpolation of <code>x</code> and <code>y</code>.
     */
    public static double mix(double a, double x, double y)
    {
        double t = clamp(a, 0d, 1d);
        return x + t * (y - x);
    }

    /**
     * Returns the smooth hermite interpolation of <code>x</code> and <code>y</code> according to the function: <code>(1
     * - t) * x + t * y</code>, where <code>t = a * a * (3 - 2 * a)</code>. The interpolation factor <code>a</code>
     * defines the weight given to each value, and is clamped to the range [0, 1]. If <code>a</code> is 0 or less, this
     * returns <code>x</code>. If <code>a</code> is 1 or more, this returns <code>y</code>. Otherwise, this returns the
     * smooth hermite interpolation of <code>x</code> and <code>y</code>. Like the linear function {@link #mix(double,
     * double, double)}, when <code>a</code> is <code>0.5</code> this returns <code>(x + y)/2</code>. But unlike the
     * linear function, the hermite function's slope gradually increases when <code>a</code> is near 0, then gradually
     * decreases when <code>a</code> is near 1. This is a useful property where a more gradual transition from
     * <code>x</code> to <code>y</code> is desired.
     *
     * @param a the interpolation factor.
     * @param x the first value.
     * @param y the second value.
     *
     * @return the smooth hermite interpolation of <code>x</code> and <code>y</code>.
     */
    public static double mixSmooth(double a, double x, double y)
    {
        double t = clamp(a, 0d, 1d);
        t = t * t * (3d - 2d * t);
        return x + t * (y - x);
    }

    /**
     * converts meters to feet.
     *
     * @param meters the value in meters.
     *
     * @return the value converted to feet.
     */
    public static double convertMetersToFeet(double meters)
    {
        return (meters * METERS_TO_FEET);
    }

    /**
     * converts meters to miles.
     *
     * @param meters the value in meters.
     *
     * @return the value converted to miles.
     */
    public static double convertMetersToMiles(double meters)
    {
        return (meters * METERS_TO_MILES);
    }

    /**
     * Converts distance in feet to distance in meters.
     *
     * @param feet the distance in feet.
     *
     * @return the distance converted to meters.
     */
    public static double convertFeetToMeters(double feet)
    {
        return (feet / METERS_TO_FEET);
    }

    /**
     * Converts time in seconds to time in milliseconds.
     *
     * @param seconds time in seconds.
     *
     * @return time in milliseconds.
     */
    public static double convertSecondsToMillis(double seconds)
    {
        return (seconds * SECOND_TO_MILLIS);
    }

    /**
     * Converts time in milliseconds to time in seconds.
     *
     * @param millis time in milliseconds.
     *
     * @return time in seconds.
     */
    public static double convertMillisToSeconds(double millis)
    {
        return millis / SECOND_TO_MILLIS;
    }

    /**
     * Converts time in minutes to time in milliseconds.
     *
     * @param minutes time in minutes.
     *
     * @return time in milliseconds.
     */
    public static double convertMinutesToMillis(double minutes)
    {
        return (minutes * MINUTE_TO_MILLIS);
    }

    /**
     * Converts time in milliseconds to time in minutes.
     *
     * @param millis time in milliseconds.
     *
     * @return time in minutes.
     */
    public static double convertMillisToMinutes(double millis)
    {
        return millis / MINUTE_TO_MILLIS;
    }

    /**
     * Converts time in hours to time in milliseconds.
     *
     * @param hours time in hours.
     *
     * @return time in milliseconds.
     */
    public static double convertHoursToMillis(double hours)
    {
        return (hours * HOUR_TO_MILLIS);
    }

    /**
     * Converts time in milliseconds to time in hours.
     *
     * @param mills time in milliseconds.
     *
     * @return time in hours.
     */
    public static double convertMillisToHours(double mills)
    {
        return mills / HOUR_TO_MILLIS;
    }

    /**
     * Convert time in days to time in milliseconds.
     *
     * @param millis time in days.
     *
     * @return time in milliseconds.
     */
    public static double convertDaysToMillis(double millis)
    {
        return millis * DAY_TO_MILLIS;
    }

    /**
     * Convert time in milliseconds to time in days.
     *
     * @param millis time in milliseconds.
     *
     * @return time in days.
     */
    public static double convertMillisToDays(double millis)
    {
        return millis / DAY_TO_MILLIS;
    }

    /**
     * Returns the distance in model coordinates from the {@link gov.nasa.worldwind.View} eye point to the specified
     * {@link gov.nasa.worldwind.geom.Extent}. If the View eye point is inside the extent, this returns 0.
     *
     * @param dc     the {@link gov.nasa.worldwind.render.DrawContext} which the View eye point is obtained from.
     * @param extent the extent to compute the distance from.
     *
     * @return the distance from the View eye point to the extent, in model coordinates.
     *
     * @throws IllegalArgumentException if either the DrawContext or the extent is null.
     */
    public static double computeDistanceFromEye(DrawContext dc, Extent extent)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (extent == null)
        {
            String message = Logging.getMessage("nullValue.ExtentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double distance = dc.getView().getEyePoint().distanceTo3(extent.getCenter()) - extent.getRadius();
        return (distance < 0d) ? 0d : distance;
    }

    /**
     * Returns the size in window coordinates of the specified {@link gov.nasa.worldwind.geom.Extent} from the current
     * {@link gov.nasa.worldwind.View}. The returned size is an estimate of the Extent's diameter in window
     * coordinates.
     *
     * @param dc     the current draw context, from which the View is obtained from.
     * @param extent the extent to compute the window size for.
     *
     * @return size of the specified Extent from the specified View, in window coordinates (screen pixels).
     *
     * @throws IllegalArgumentException if either the DrawContext or the extent is null.
     */
    public static double computeSizeInWindowCoordinates(DrawContext dc, Extent extent)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (extent == null)
        {
            String message = Logging.getMessage("nullValue.ExtentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Estimate the size in window coordinates W from as follows. Given
        // R, the extent radius in meters
        // D, the distance from eye point to extent center, in meters
        //
        // compute S, the size of screen pixel at D, in meters/pixels. S is an estimate based on the viewport
        // size, the view projection, and D.
        //
        // Finally, estimate W, the extent's diameter in window coordinates (screen pixels).
        //
        // W : 2R * 1/S = (2R meters / S meters) * 1 pixels = 2R/S pixels

        double distance = dc.getView().getEyePoint().distanceTo3(extent.getCenter());
        double pixelSize = dc.getView().computePixelSizeAtDistance(distance);
        return 2d * extent.getRadius() / pixelSize;
    }

    /**
     * Computes the area in square pixels of a sphere after it is projected into the specified <code>view's</code>
     * viewport. The returned value is the screen area that the sphere covers in the infinite plane defined by the
     * <code>view's</code> viewport. This area is not limited to the size of the <code>view's</code> viewport, and
     * portions of the sphere are not clipped by the <code>view's</code> frustum.
     * <p>
     * This returns zero if the specified <code>radius</code> is zero.
     *
     * @param view   the <code>View</code> for which to compute a projected screen area.
     * @param center the sphere's center point, in model coordinates.
     * @param radius the sphere's radius, in meters.
     *
     * @return the projected screen area of the sphere in square pixels.
     *
     * @throws IllegalArgumentException if the <code>view</code> is <code>null</code>, if <code>center</code> is
     *                                  <code>null</code>, or if <code>radius</code> is less than zero.
     */
    public static double computeSphereProjectedArea(View view, Vec4 center, double radius)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (radius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative", radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (radius == 0)
            return 0;

        // Compute the sphere's area by scaling its radius based on the sphere's depth in eye coordinates. This provides
        // a good approximation of the sphere's projected area, but does not provide an exact value: the perspective
        // projection of a sphere is an ellipse.

        // Compute the sphere's depth in eye coordinates by transforming the center point into eye coordinates and using
        // its absolute z-value as the depth value. Then compute the radius in pixels by dividing the radius in meters
        // by the number of meters per pixel at the sphere's depth.
        double depth = Math.abs(center.transformBy4(view.getModelviewMatrix()).z);
        double radiusInPixels = radius / view.computePixelSizeAtDistance(depth);

        return Math.PI * radiusInPixels * radiusInPixels;
    }

    /**
     * Computes a unit-length normal vector for a buffer of coordinate triples. The normal vector is computed from the
     * first three non-colinear points in the buffer.
     *
     * @param coords the coordinates. This method returns null if this argument is null.
     * @param stride the number of floats between successive points. 0 indicates that the points are arranged one
     *               immediately after the other.
     *
     * @return the computed unit-length normal vector, or null if a normal vector could not be computed.
     */
    public static Vec4 computeBufferNormal(FloatBuffer coords, int stride)
    {
        Vec4[] verts = WWMath.findThreeIndependentVertices(coords, stride);
        return verts != null ? WWMath.computeTriangleNormal(verts[0], verts[1], verts[2]) : null;
    }

    /**
     * Computes a unit-length normal vector for an array of coordinates. The normal vector is computed from the first
     * three non-colinear points in the array.
     *
     * @param coords the coordinates. This method returns null if this argument is null.
     *
     * @return the computed unit-length normal vector, or null if a normal vector could not be computed.
     */
    public static Vec4 computeArrayNormal(Vec4[] coords)
    {
        Vec4[] verts = WWMath.findThreeIndependentVertices(coords);
        return verts != null ? WWMath.computeTriangleNormal(verts[0], verts[1], verts[2]) : null;
    }

    /**
     * Finds three non-colinear points in a buffer.
     *
     * @param coords the coordinates. This method returns null if this argument is null.
     * @param stride the number of floats between successive points. 0 indicates that the points are arranged one
     *               immediately after the other.
     *
     * @return an array of three points, or null if three non-colinear points could not be found.
     */
    public static Vec4[] findThreeIndependentVertices(FloatBuffer coords, int stride)
    {
        int xstride = stride > 0 ? stride : 3;

        if (coords == null || coords.limit() < 3 * xstride)
            return null;

        Vec4 a = new Vec4(coords.get(0), coords.get(1), coords.get(2));
        Vec4 b = null;
        Vec4 c = null;

        int k = xstride;
        for (; k < coords.limit(); k += xstride)
        {
            b = new Vec4(coords.get(k), coords.get(k + 1), coords.get(k + 2));
            if (!(b.x == a.x && b.y == a.y && b.z == a.z))
                break;
            b = null;
        }

        if (b == null)
            return null;

        for (k += xstride; k < coords.limit(); k += xstride)
        {
            c = new Vec4(coords.get(k), coords.get(k + 1), coords.get(k + 2));

            // if c is not coincident with a or b, and the vectors ab and bc are not colinear, break and return a, b, c
            if (!((c.x == a.x && c.y == a.y && c.z == a.z) || (c.x == b.x && c.y == b.y && c.z == b.z)))
            {
                if (!Vec4.areColinear(a, b, c))
                    break;
            }

            c = null; // reset c to signal failure to return statement below
        }

        return c != null ? new Vec4[] {a, b, c} : null;
    }

    /**
     * Finds three non-colinear points in an array of points.
     *
     * @param coords the coordinates. This method returns null if this argument is null.
     *
     * @return an array of three points, or null if three non-colinear points could not be found.
     */
    public static Vec4[] findThreeIndependentVertices(Vec4[] coords)
    {
        if (coords == null || coords.length < 3)
            return null;

        Vec4 a = coords[0];
        Vec4 b = null;
        Vec4 c = null;

        int k = 1;
        for (; k < coords.length; k++)
        {
            b = coords[k];
            if (!(b.x == a.x && b.y == a.y && b.z == a.z))
                break;
            b = null;
        }

        if (b == null)
            return null;

        for (; k < coords.length; k++)
        {
            c = coords[k];

            // if c is not coincident with a or b, and the vectors ab and bc are not colinear, break and return a, b, c
            if (!((c.x == a.x && c.y == a.y && c.z == a.z) || (c.x == b.x && c.y == b.y && c.z == b.z)))
            {
                if (!Vec4.areColinear(a, b, c))
                    break;
            }

            c = null; // reset c to signal failure to return statement below
        }

        return c != null ? new Vec4[] {a, b, c} : null;
    }

    /**
     * Returns the normal vector corresponding to the triangle defined by three vertices (a, b, c).
     *
     * @param a the triangle's first vertex.
     * @param b the triangle's second vertex.
     * @param c the triangle's third vertex.
     *
     * @return the triangle's unit-length normal vector.
     *
     * @throws IllegalArgumentException if any of the specified vertices are null.
     */
    public static Vec4 computeTriangleNormal(Vec4 a, Vec4 b, Vec4 c)
    {
        if (a == null || b == null || c == null)
        {
            String message = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double x = ((b.y - a.y) * (c.z - a.z)) - ((b.z - a.z) * (c.y - a.y));
        double y = ((b.z - a.z) * (c.x - a.x)) - ((b.x - a.x) * (c.z - a.z));
        double z = ((b.x - a.x) * (c.y - a.y)) - ((b.y - a.y) * (c.x - a.x));

        double length = (x * x) + (y * y) + (z * z);
        if (length == 0d)
            return new Vec4(x, y, z);

        length = Math.sqrt(length);
        return new Vec4(x / length, y / length, z / length);
    }

    /**
     * Returns the area enclosed by the specified (x, y) points (the z and w coordinates are ignored). If the specified
     * points do not define a closed loop, then the loop is automatically closed by simulating appending the first point
     * to the last point.
     *
     * @param points the (x, y) points which define the 2D polygon.
     *
     * @return the area enclosed by the specified coordinates.
     *
     * @throws IllegalArgumentException if points is null.
     */
    public static double computePolygonAreaFromVertices(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.util.Iterator<? extends Vec4> iter = points.iterator();
        if (!iter.hasNext())
        {
            return 0;
        }

        double area = 0;
        Vec4 firstPoint = iter.next();
        Vec4 point = firstPoint;

        while (iter.hasNext())
        {
            Vec4 nextLocation = iter.next();

            area += point.x * nextLocation.y;
            area -= nextLocation.x * point.y;

            point = nextLocation;
        }

        // Include the area connecting the last point to the first point, if they're not already equal.
        if (!point.equals(firstPoint))
        {
            area += point.x * firstPoint.y;
            area -= firstPoint.x * point.y;
        }

        area /= 2.0;
        return area;
    }

    /**
     * Returns the winding order of the polygon described by the specified locations, with respect to an axis
     * perpendicular to the (lat, lon) coordinates, and pointing in the direction of "positive elevation".
     *
     * @param locations the locations defining the geographic polygon.
     *
     * @return {@link AVKey#CLOCKWISE} if the polygon has clockwise winding order, and {@link AVKey#COUNTER_CLOCKWISE}
     *         otherwise.
     */
    public static String computeWindingOrderOfLocations(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.util.Iterator<? extends LatLon> iter = locations.iterator();
        if (!iter.hasNext())
            return AVKey.COUNTER_CLOCKWISE;

        if (LatLon.locationsCrossDateLine(locations))
            iter = LatLon.makeDatelineCrossingLocationsPositive(locations).iterator();

        double area = 0;
        LatLon firstLocation = iter.next();
        LatLon location = firstLocation;

        while (iter.hasNext())
        {
            LatLon nextLocation = iter.next();

            area += location.getLongitude().degrees * nextLocation.getLatitude().degrees;
            area -= nextLocation.getLongitude().degrees * location.getLatitude().degrees;

            location = nextLocation;
        }

        // Include the area connecting the last point to the first point, if they're not already equal.
        if (!location.equals(firstLocation))
        {
            area += location.getLongitude().degrees * firstLocation.getLatitude().degrees;
            area -= firstLocation.getLongitude().degrees * location.getLatitude().degrees;
        }

        return (area < 0) ? AVKey.CLOCKWISE : AVKey.COUNTER_CLOCKWISE;
    }

    /**
     * Returns the winding order of the 2D polygon described by the specified (x, y) points (z and w coordinates are
     * ignored), with respect to the positive z axis.
     *
     * @param points the (x, y) points which define the 2D polygon.
     *
     * @return AVKey.CLOCKWISE if the polygon has clockwise winding order about the positive z axis, and
     *         AVKey.COUNTER_CLOCKWISE otherwise.
     */
    public static String computeWindingOrderOfVertices(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double area = computePolygonAreaFromVertices(points);

        return (area < 0) ? AVKey.CLOCKWISE : AVKey.COUNTER_CLOCKWISE;
    }

    /**
     * Returns an array of normalized vectors defining the three principal axes of the x-, y-, and z-coordinates from
     * the specified points Iterable, sorted from the most prominent axis to the least prominent. This returns null if
     * the points Iterable is empty, or if all of the points are null. The returned array contains three normalized
     * orthogonal vectors defining a coordinate system which best fits the distribution of the points Iterable about its
     * arithmetic mean.
     *
     * @param points the Iterable of points for which to compute the principal axes.
     *
     * @return the normalized principal axes of the points Iterable, sorted from the most prominent axis to the least
     *         prominent.
     *
     * @throws IllegalArgumentException if the points Iterable is null.
     */
    public static Vec4[] computePrincipalAxes(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Compute the covariance matrix of the specified points Iterable. Note that Matrix.fromCovarianceOfVertices
        // returns null if the points Iterable is empty, or if all of the points are null.
        Matrix covariance = Matrix.fromCovarianceOfVertices(points);
        if (covariance == null)
            return null;

        // Compute the eigenvalues and eigenvectors of the covariance matrix. Since the covariance matrix is symmetric
        // by definition, we can safely use the method Matrix.computeEigensystemFromSymmetricMatrix3().
        final double[] eigenValues = new double[3];
        final Vec4[] eigenVectors = new Vec4[3];
        Matrix.computeEigensystemFromSymmetricMatrix3(covariance, eigenValues, eigenVectors);

        // Compute an index array who's entries define the order in which the eigenValues array can be sorted in
        // ascending order.
        Integer[] indexArray = {0, 1, 2};
        Arrays.sort(indexArray, new Comparator<Integer>()
        {
            public int compare(Integer a, Integer b)
            {
                return Double.compare(eigenValues[a], eigenValues[b]);
            }
        });

        // Return the normalized eigenvectors in order of decreasing eigenvalue. This has the effect of returning three
        // normalized orthognal vectors defining a coordinate system, which are sorted from the most prominent axis to
        // the least prominent.
        return new Vec4[]
            {
                eigenVectors[indexArray[2]].normalize3(),
                eigenVectors[indexArray[1]].normalize3(),
                eigenVectors[indexArray[0]].normalize3()
            };
    }

    /**
     * Returns an array of normalized vectors defining the three principal axes of the x-, y-, and z-coordinates from
     * the specified buffer of points, sorted from the most prominent axis to the least prominent. This returns null if
     * the buffer is empty. The returned array contains three normalized orthogonal vectors defining a coordinate system
     * which best fits the distribution of the points about its arithmetic mean.
     * <p>
     * The buffer must contain XYZ coordinate tuples which are either tightly packed or offset by the specified stride.
     * The stride specifies the number of buffer elements between the first coordinate of consecutive tuples. For
     * example, a stride of 3 specifies that each tuple is tightly packed as XYZXYZXYZ, whereas a stride of 5 specifies
     * that there are two elements between each tuple as XYZabXYZab (the elements "a" and "b" are ignored). The stride
     * must be at least 3. If the buffer's length is not evenly divisible into stride-sized tuples, this ignores the
     * remaining elements that follow the last complete tuple.
     *
     * @param coordinates the buffer containing the point coordinates for which to compute the principal axes.
     * @param stride      the number of elements between the first coordinate of consecutive points. If stride is 3,
     *                    this interprets the buffer has having tightly packed XYZ coordinate tuples.
     *
     * @return the normalized principal axes of the points, sorted from the most prominent axis to the least prominent.
     *
     * @throws IllegalArgumentException if the buffer is null, or if the stride is less than three.
     */
    public static Vec4[] computePrincipalAxes(BufferWrapper coordinates, int stride)
    {
        if (coordinates == null)
        {
            String message = Logging.getMessage("nullValue.CoordinatesAreNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (stride < 3)
        {
            String msg = Logging.getMessage("generic.StrideIsInvalid");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute the covariance matrix of the specified points Iterable. Note that Matrix.fromCovarianceOfVertices
        // returns null if the points Iterable is empty, or if all of the points are null.
        Matrix covariance = Matrix.fromCovarianceOfVertices(coordinates, stride);
        if (covariance == null)
            return null;

        // Compute the eigenvalues and eigenvectors of the covariance matrix. Since the covariance matrix is symmetric
        // by definition, we can safely use the method Matrix.computeEigensystemFromSymmetricMatrix3().
        final double[] eigenValues = new double[3];
        final Vec4[] eigenVectors = new Vec4[3];
        Matrix.computeEigensystemFromSymmetricMatrix3(covariance, eigenValues, eigenVectors);

        // Compute an index array who's entries define the order in which the eigenValues array can be sorted in
        // ascending order.
        Integer[] indexArray = {0, 1, 2};
        Arrays.sort(indexArray, new Comparator<Integer>()
        {
            public int compare(Integer a, Integer b)
            {
                return Double.compare(eigenValues[a], eigenValues[b]);
            }
        });

        // Return the normalized eigenvectors in order of decreasing eigenvalue. This has the effect of returning three
        // normalized orthognal vectors defining a coordinate system, which are sorted from the most prominent axis to
        // the least prominent.
        return new Vec4[]
            {
                eigenVectors[indexArray[2]].normalize3(),
                eigenVectors[indexArray[1]].normalize3(),
                eigenVectors[indexArray[0]].normalize3()
            };
    }

    /**
     * Returns whether the geographic polygon described by the specified locations defines a closed loop. If the
     * iterable holds fewer than two points, this always returns false. Therefore a polygon consisting of a single point
     * and the empty polygon are not considered closed loops.
     *
     * @param locations the locations which define the geographic polygon.
     *
     * @return true if the polygon defines a closed loop, and false otherwise.
     *
     * @throws IllegalArgumentException if the locations are null.
     */
    public static boolean isPolygonClosed(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.util.Iterator<? extends LatLon> iter = locations.iterator();
        if (!iter.hasNext())
        {
            return false;
        }

        LatLon firstLocation = iter.next();
        LatLon lastLocation = null;

        while (iter.hasNext())
        {
            lastLocation = iter.next();
        }

        return (lastLocation != null) && lastLocation.equals(firstLocation);
    }

    /**
     * Returns whether the 2D polygon described by the specified (x, y) points defines a closed loop (z and w
     * coordinates are ignored). If the iterable holds fewer than two points, this always returns false. Therefore a
     * polygon consisting of a single point and the empty polygon are not considered closed loops.
     *
     * @param points the (x, y) points which define the 2D polygon.
     *
     * @return true if the polygon defines a closed loop, and false otherwise.
     *
     * @throws IllegalArgumentException if the points are null.
     */
    public static boolean isPolygonClosed2(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.util.Iterator<? extends Vec4> iter = points.iterator();
        if (!iter.hasNext())
        {
            return false;
        }

        Vec4 firstPoint = iter.next();
        Vec4 lastPoint = null;

        while (iter.hasNext())
        {
            lastPoint = iter.next();
        }

        return (lastPoint != null) && (lastPoint.x == firstPoint.x) && (lastPoint.y == firstPoint.y);
    }

    // TODO: this is only valid for linear path type

    /**
     * Determines whether a {@link LatLon} location is located inside a given polygon.
     *
     * @param location  the location
     * @param locations the list of positions describing the polygon. Last one should be the same as the first one.
     *
     * @return true if the location is inside the polygon.
     */
    public static boolean isLocationInside(LatLon location, Iterable<? extends LatLon> locations)
    {
        if (location == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.util.Iterator<? extends LatLon> iter = locations.iterator();
        if (!iter.hasNext())
        {
            return false;
        }

        // Test for even/odd number of intersections with a constant latitude line going through the given location.
        boolean result = false;
        LatLon p1 = iter.next();
        while (iter.hasNext())
        {
            LatLon p2 = iter.next();

// Developped for clarity
//            double lat = location.getLatitude().degrees;
//            double lon = location.getLongitude().degrees;
//            double lat1 = p1.getLatitude().degrees;
//            double lon1 = p1.getLongitude().degrees;
//            double lat2 = p2.getLatitude().degrees;
//            double lon2 = p2.getLongitude().degrees;
//            if ( ((lat2 <= lat && lat < lat1) || (lat1 <= lat && lat < lat2))
//                    && (lon < (lon1 - lon2) * (lat - lat2) / (lat1 - lat2) + lon2) )
//                result = !result;

            if (((p2.getLatitude().degrees <= location.getLatitude().degrees
                && location.getLatitude().degrees < p1.getLatitude().degrees) ||
                (p1.getLatitude().degrees <= location.getLatitude().degrees
                    && location.getLatitude().degrees < p2.getLatitude().degrees))
                && (location.getLongitude().degrees < (p1.getLongitude().degrees - p2.getLongitude().degrees)
                * (location.getLatitude().degrees - p2.getLatitude().degrees)
                / (p1.getLatitude().degrees - p2.getLatitude().degrees) + p2.getLongitude().degrees))
                result = !result;

            p1 = p2;
        }
        return result;
    }

    /**
     * Computes the center, axis, and radius of the circle that circumscribes the specified points. If the points are
     * oriented in a clockwise winding order, the circle's axis will point toward the viewer. Otherwise the axis will
     * point away from the viewer. Values are returned in the first element of centerOut, axisOut, and radiusOut. The
     * caller must provide a preallocted arrays of length one or greater for each of these values.
     *
     * @param p0        the first point.
     * @param p1        the second point.
     * @param p2        the third point.
     * @param centerOut preallocated array to hold the circle's center.
     * @param axisOut   preallocated array to hold the circle's axis.
     * @param radiusOut preallocated array to hold the circle's radius.
     *
     * @return true if the computation was successful; false otherwise.
     *
     * @throws IllegalArgumentException if <code>p0</code>, <code>p1</code>, or <code>p2</code> is null
     */
    public static boolean computeCircleThroughPoints(Vec4 p0, Vec4 p1, Vec4 p2, Vec4[] centerOut, Vec4[] axisOut,
        double[] radiusOut)
    {
        if (p0 == null || p1 == null || p2 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 v0 = p1.subtract3(p0);
        Vec4 v1 = p2.subtract3(p1);
        Vec4 v2 = p2.subtract3(p0);

        double d0 = v0.dot3(v2);
        double d1 = -v0.dot3(v1);
        double d2 = v1.dot3(v2);

        double t0 = d1 + d2;
        double t1 = d0 + d2;
        double t2 = d0 + d1;

        double e0 = d0 * t0;
        double e1 = d1 * t1;
        double e2 = d2 * t2;

        double max_e = Math.max(Math.max(e0, e1), e2);
        double min_e = Math.min(Math.min(e0, e1), e2);

        double E = e0 + e1 + e2;

        double tolerance = 1e-6;
        if (Math.abs(E) <= tolerance * (max_e - min_e))
            return false;

        double radiusSquared = 0.5d * t0 * t1 * t2 / E;
        // the three points are collinear -- no circle with finite radius is possible
        if (radiusSquared < 0d)
            return false;

        double radius = Math.sqrt(radiusSquared);

        Vec4 center = p0.multiply3(e0 / E);
        center = center.add3(p1.multiply3(e1 / E));
        center = center.add3(p2.multiply3(e2 / E));

        Vec4 axis = v2.cross3(v0);
        axis = axis.normalize3();

        if (centerOut != null)
            centerOut[0] = center;
        if (axisOut != null)
            axisOut[0] = axis;
        if (radiusOut != null)
            radiusOut[0] = radius;
        return true;
    }

    /**
     * Intersect a line with a convex polytope and return the intersection points.
     * <p>
     * See "3-D Computer Graphics" by Samuel R. Buss, 2005, Section X.1.4.
     *
     * @param line   the line to intersect with the polytope.
     * @param planes the planes defining the polytope. Each plane's normal must point away from the the polytope, i.e.
     *               each plane's positive halfspace is outside the polytope. (Note: This is the opposite convention
     *               from that of a view frustum.)
     *
     * @return the points of intersection, or null if the line does not intersect the polytope. Two points are returned
     *         if the line both enters and exits the polytope. One point is retured if the line origin is within the
     *         polytope.
     *
     * @throws IllegalArgumentException if the line is null or ill-formed, the planes array is null or there are fewer
     *                                  than three planes.
     */
    public static Intersection[] polytopeIntersect(Line line, Plane[] planes)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Algorithm from "3-D Computer Graphics" by Samuel R. Buss, 2005, Section X.1.4.

        // Determine intersection with each plane and categorize the intersections as "front" if the line intersects
        // the front side of the plane (dot product of line direction with plane normal is negative) and "back" if the
        // line intersects the back side of the plane (dot product of line direction with plane normal is positive).

        double fMax = -Double.MAX_VALUE;
        double bMin = Double.MAX_VALUE;
        boolean isTangent = false;

        Vec4 u = line.getDirection();
        Vec4 p = line.getOrigin();

        for (Plane plane : planes)
        {
            Vec4 n = plane.getNormal();
            double d = -plane.getDistance();

            double s = u.dot3(n);
            if (s == 0) // line is parallel to plane
            {
                double pdn = p.dot3(n);
                if (pdn > d) // is line in positive halfspace (in front of) of the plane?
                    return null; // no intersection
                else
                {
                    if (pdn == d)
                        isTangent = true; // line coincident with plane
                    continue; // line is in negative halfspace; possible intersection; check other planes
                }
            }

            // Determine whether front or back intersection.
            double a = (d - p.dot3(n)) / s;
            if (u.dot3(n) < 0) // line intersects front face and therefore entering polytope
            {
                if (a > fMax)
                {
                    if (a > bMin)
                        return null;
                    fMax = a;
                }
            }
            else // line intersects back face and therefore leaving polytope
            {
                if (a < bMin)
                {
                    if (a < 0 || a < fMax)
                        return null;
                    bMin = a;
                }
            }
        }

        // Compute the Cartesian intersection points. There will be no more than two.
        if (fMax >= 0) // intersects frontface and backface; point origin is outside the polytope
            return new Intersection[]
                {
                    new Intersection(p.add3(u.multiply3(fMax)), isTangent),
                    new Intersection(p.add3(u.multiply3(bMin)), isTangent)
                };
        else // intersects backface only; point origin is within the polytope
            return new Intersection[] {new Intersection(p.add3(u.multiply3(bMin)), isTangent)};
    }

    //**************************************************************//
    //********************  Geometry Construction  ******************//
    //**************************************************************//

    /**
     * Computes an index buffer in the system native byte order that tessellates the interior of a vertex grid as a
     * triangle strip. The returned buffer may be used as the source <code>buffer</code> in a call to {@link
     * com.jogamp.opengl.GL2#glDrawElements(int, int, int, java.nio.Buffer)}, where <code>mode</code> is {@link
     * com.jogamp.opengl.GL#GL_TRIANGLE_STRIP}, <code>count</code> is the number of elements remaining in the buffer,
     * and <code>type</code> is {@link com.jogamp.opengl.GL#GL_UNSIGNED_INT}.
     * <p>
     * For details the drawing OpenGL primitives, see <a href="http://www.glprogramming.com/red/chapter02.html#name14">http://www.glprogramming.com/red/chapter02.html#name14</a>.
     *
     * @param width  the patch width, in vertices.
     * @param height the patch height, in vertices.
     *
     * @return an index buffer that tessellate's the grid interior as a triangle strip.
     *
     * @throws IllegalArgumentException if either the width or height are less than or equal to zero.
     */
    public static IntBuffer computeIndicesForGridInterior(int width, int height)
    {
        if (width <= 0)
        {
            String message = Logging.getMessage("Geom.WidthInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("Geom.HeightInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numIndices = (height - 1) * (2 * width) + (2 * (height - 2));
        IntBuffer buffer = Buffers.newDirectIntBuffer(numIndices);

        int pos;
        for (int y = 0; y < height - 1; y++)
        {
            if (y != 0)
            {
                buffer.put((width - 1) + (y - 1) * width);
                buffer.put(width + y * width);
            }

            for (int x = 0; x < width; x++)
            {
                pos = x + y * width;
                buffer.put(pos + width);
                buffer.put(pos);
            }
        }

        buffer.rewind();
        return buffer;
    }

    /**
     * Computes an index buffer in the system native byte order that tessellates the outline of a vertex grid as a line
     * strip. The returned buffer may be used as the source <code>buffer</code> in a call to {@link
     * com.jogamp.opengl.GL2#glDrawElements(int, int, int, java.nio.Buffer)}, where <code>mode</code> is {@link
     * com.jogamp.opengl.GL#GL_LINE_STRIP}, <code>count</code> is the number of elements remaining in the buffer, and
     * <code>type</code> is {@link com.jogamp.opengl.GL#GL_UNSIGNED_INT}.
     * <p>
     * For details the drawing OpenGL primitives, see <a href="http://www.glprogramming.com/red/chapter02.html#name14">http://www.glprogramming.com/red/chapter02.html#name14</a>.
     *
     * @param width  the patch width, in vertices.
     * @param height the patch height, in vertices.
     *
     * @return an index buffer that tessellates the grid outline as a line strip.
     *
     * @throws IllegalArgumentException if either the width or height are less than or equal to zero.
     */
    public static IntBuffer computeIndicesForGridOutline(int width, int height)
    {
        if (width <= 0)
        {
            String message = Logging.getMessage("Geom.WidthInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("Geom.HeightInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numIndices = 2 * (width + height - 2);
        IntBuffer buffer = Buffers.newDirectIntBuffer(numIndices);

        for (int x = 0; x < width; x++)
        {
            buffer.put(x);
        }

        for (int y = 1; y < height - 1; y++)
        {
            buffer.put((width - 1) + y * width);
        }

        for (int x = width - 1; x >= 0; x--)
        {
            buffer.put(x + (height - 1) * width);
        }

        for (int y = height - 2; y >= 1; y--)
        {
            buffer.put(y * width);
        }

        buffer.rewind();
        return buffer;
    }

    /**
     * Computes per-vertex normals of an indexed triangle strip, storing the normal coordinates in the specified normal
     * buffer. If the normal buffer is null, this creates a new buffer in the system native byte order with the capacity
     * to store the same number of vertices as the vertex buffer. The vertex buffer is assumed to contain tightly packed
     * 3-coordinate tuples. The 3-coordinate normal for each vertex is stored in the normal buffer at the same position
     * each vertex appears in the vertex buffer.
     * <p>
     * For details the drawing OpenGL primitives, see <a href="http://www.glprogramming.com/red/chapter02.html#name14">http://www.glprogramming.com/red/chapter02.html#name14</a>.
     *
     * @param indices  indices into the vertex buffer defining a triangle strip.
     * @param vertices buffer of vertex coordinate tuples used to compute the normal coordinates.
     * @param normals  buffer of normal coordinate tuples that receives the normal coordinates, or null to create a new
     *                 buffer to hold the normal coordinates.
     *
     * @return buffer of normal coordinate tuples.
     *
     * @throws IllegalArgumentException if either the index buffer or the vertex buffer is null.
     */
    public static FloatBuffer computeNormalsForIndexedTriangleStrip(IntBuffer indices, FloatBuffer vertices,
        FloatBuffer normals)
    {
        if (indices == null)
        {
            String message = Logging.getMessage("nullValue.IndexBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (vertices == null)
        {
            String message = Logging.getMessage("nullValue.VertexBufferNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numIndices = indices.remaining();
        int numVertices = vertices.remaining() / 3;

        // If the normal buffer is null, create a new one with the capacity to store the same number of vertices as
        // the vertex buffer. Otherwise, initialize the normal buffer by setting all normal coordinate to zero.
        if (normals == null)
            normals = Buffers.newDirectFloatBuffer(3 * numVertices);
        else
        {
            for (int i = 0; i < numVertices; i++)
            {
                normals.put(0);
                normals.put(0);
                normals.put(0);
            }
            normals.rewind();
        }

        // Compute the normal for each face, then add that normal to each individual vertex of the face. After this step
        // each normal contains the accumulated normal values from each face it contributes to.
        int[] triangle = new int[3];
        for (int i = 2; i < numIndices; i++)
        {
            indices.position(i - 2);
            indices.get(triangle);

            // Triangle strips alternate between counter-clockwise and clockwise winding order each triangle. Triangles
            // starting with an even index are clockwise, while those starting with an odd index are counter-clockwise.
            // Reverse the face's vertex order for even triangles to ensure that all faces have counter-clockwise
            // ordering.
            if ((i % 2) != 0)
            {
                int tmp = triangle[0];
                triangle[0] = triangle[1];
                triangle[1] = tmp;
            }

            addTriangleNormal(3 * triangle[0], 3 * triangle[1], 3 * triangle[2], vertices, normals);
        }

        indices.rewind();
        vertices.rewind();
        normals.rewind();

        // Normalize each tuple. Each normal contains the accumulated normal values from each face it contributes to
        // Normalizing the tuple averages the accumulated normals from each face, and ensures that the normal has unit
        // length.
        for (int i = 0; i < numVertices; i++)
        {
            // Normalizes the tuple and the buffer's position then advances to the next tuple.
            normalize3(normals);
        }

        normals.rewind();

        return normals;
    }

    /**
     * Computes a triangle normal given the starting position of three tuples in the specified vertex buffer, and stores
     * adds the result to three tuples in the specified normal buffer with the same positions.
     *
     * @param a        the first tuple's starting position.
     * @param b        the second tuple's starting position.
     * @param c        the third tuple's starting position.
     * @param vertices buffer of vertex coordinate tuples used to compute the normal coordinates.
     * @param normals  buffer of normal coordinate tuples that receives the normal coordinates.
     */
    protected static void addTriangleNormal(int a, int b, int c, FloatBuffer vertices, FloatBuffer normals)
    {
        vertices.position(a);
        float ax = vertices.get();
        float ay = vertices.get();
        float az = vertices.get();

        vertices.position(b);
        float bx = vertices.get();
        float by = vertices.get();
        float bz = vertices.get();

        vertices.position(c);
        float cx = vertices.get();
        float cy = vertices.get();
        float cz = vertices.get();

        float x = ((by - ay) * (cz - az)) - ((bz - az) * (cy - ay));
        float y = ((bz - az) * (cx - ax)) - ((bx - ax) * (cz - az));
        float z = ((bx - ax) * (cy - ay)) - ((by - ay) * (cx - ax));

        float length = (x * x) + (y * y) + (z * z);
        if (length > 0d)
        {
            length = (float) Math.sqrt(length);
            x /= length;
            y /= length;
            z /= length;
        }

        normals.position(a);
        float nx = normals.get();
        float ny = normals.get();
        float nz = normals.get();
        normals.position(a);
        normals.put(nx + x);
        normals.put(ny + y);
        normals.put(nz + z);

        normals.position(b);
        nx = normals.get();
        ny = normals.get();
        nz = normals.get();
        normals.position(b);
        normals.put(nx + x);
        normals.put(ny + y);
        normals.put(nz + z);

        normals.position(c);
        nx = normals.get();
        ny = normals.get();
        nz = normals.get();
        normals.position(c);
        normals.put(nx + x);
        normals.put(ny + y);
        normals.put(nz + z);
    }

    /**
     * Normalizes the 3-coordinate tuple starting at the buffer's position, then advances the buffer's position to the
     * end of the tuple.
     *
     * @param buffer the buffer to normalize.
     *
     * @throws NullPointerException if the buffer is null.
     */
    protected static void normalize3(FloatBuffer buffer)
    {
        int pos = buffer.position();

        float x = buffer.get();
        float y = buffer.get();
        float z = buffer.get();

        float length = (x * x) + (y * y) + (z * z);
        if (length > 0d)
        {
            length = (float) Math.sqrt(length);
            x /= length;
            y /= length;
            z /= length;
        }

        buffer.position(pos);
        buffer.put(x);
        buffer.put(y);
        buffer.put(z);
    }

    /**
     * Computes a line between two integer-coordinate points using Bresenham's algorithm.
     *
     * @param x0 the x coordinate of the first point.
     * @param y0 the y coordinate of the first point, relative to an upper-left origin.
     * @param x1 the x coordinate of the second point.
     * @param y1 the y coordinate of the second point, relative to an upper-left origin.
     *
     * @return a list of points defining a line between the two input points.
     */
    public static List<Point> bresenham(int x0, int y0, int x1, int y1)
    {
        List<Point> points = new ArrayList<Point>(Math.abs(x1 - x0 + 1));

        boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
        if (steep)
        {
            int t = x0;
            //noinspection SuspiciousNameCombination
            x0 = y0;
            y0 = t;
            t = x1;
            //noinspection SuspiciousNameCombination
            x1 = y1;
            y1 = t;
        }

        if (x0 > x1)
        {
            int t = x0;
            x0 = x1;
            x1 = t;
            t = y0;
            y0 = y1;
            y1 = t;
        }

        int deltax = x1 - x0;
        int deltay = Math.abs(y1 - y0);
        int error = deltax / 2;
        int ystep = y0 < y1 ? 1 : -1;
        int y = y0;

        for (int x = x0; x <= x1; x += 1)
        {
            if (steep)
                points.add(new Point(y, x));
            else
                points.add(new Point(x, y));
            error -= deltay;
            if (error < 0)
            {
                y += ystep;
                error += deltax;
            }
        }

        return points;
    }

    /**
     * Create positions that describe lines parallel to a control line.
     *
     * @param controlPositions List of positions along the control line. Must be greater than 1.
     * @param leftPositions    List to receive positions on the left line.
     * @param rightPositions   List to receive positions on the right line.
     * @param distance         Distance from the center line to the left and right lines.
     * @param globe            Globe used to compute positions.
     *
     * @throws IllegalArgumentException if any of the lists are null, the number of control positions is less than 2, or
     *                                  the globe is null.
     */
    public static void generateParallelLines(List<Position> controlPositions, List<Position> leftPositions,
        List<Position> rightPositions, double distance, Globe globe)
    {
        if (controlPositions == null || leftPositions == null || rightPositions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (controlPositions.size() < 2)
        {
            String message = Logging.getMessage("generic.LengthIsInvalid");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Starting at the start of the line, take points three at a time. B is the current control point, A is the next
        // point in the line, and C is the previous point. We need to a find a vector that bisects angle ABC.
        //       B
        //       ---------> C
        //      /
        //     /
        //    /
        // A /

        Iterator<? extends Position> iterator = controlPositions.iterator();

        Position posB = iterator.next();
        Position posA = iterator.next();

        // Compute points, ignoring elevation. We will project the path onto the surface of the globe, compute the
        // parallel points on the surface, and then raise the points to the correct elevation.
        Vec4 ptA = globe.computePointFromLocation(posA);
        Vec4 ptB = globe.computePointFromLocation(posB);
        Vec4 ptC;

        // We'll keep track of the offset used to compute parallel points as we go through the list. We need this
        // to handle cases where the position list contains sequential co-located points.
        Vec4 prevOffset = null;

        // Compute side points at the start of the line.
        prevOffset = generateParallelPoints(ptB, null, ptA, leftPositions, rightPositions, distance,
            posB.getElevation(), globe, prevOffset);

        double prevElevation;
        while (iterator.hasNext())
        {
            prevElevation = posA.getElevation();
            posA = iterator.next();

            ptC = ptB;
            ptB = ptA;
            ptA = globe.computePointFromLocation(posA);

            prevOffset = generateParallelPoints(ptB, ptC, ptA, leftPositions, rightPositions, distance,
                prevElevation, globe, prevOffset);
        }

        // Compute side points at the end of the line.
        generateParallelPoints(ptA, ptB, null, leftPositions, rightPositions, distance, posA.getElevation(),
            globe, prevOffset);
    }

    /**
     * Compute points on either side of a line segment. This method requires a point on the line, and either a next
     * point, previous point, or both.
     *
     * @param point          Center point about which to compute side points.
     * @param prev           Previous point on the line. May be null if {@code next} is non-null.
     * @param next           Next point on the line. May be null if {@code prev} is non-null.
     * @param leftPositions  Left position will be added to this list.
     * @param rightPositions Right position will be added to this list.
     * @param distance       Distance from the center line to the left and right lines.
     * @param elevation      Elevation at which to place the generated positions.
     * @param globe          Globe used to compute positions.
     * @param previousOffset Offset vector from a previous call to this method. May be null.
     *
     * @return Offset vector that should be passed back to this method on the next call for a list of positions. (Used
     *         to generate parallel points when a position list contains sequential co-located positions.)
     *
     * @throws IllegalArgumentException if the necessary point, previous or next references are null, either the left or
     *                                  right position list is null, or the globe is null.
     */
    public static Vec4 generateParallelPoints(Vec4 point, Vec4 prev, Vec4 next, List<Position> leftPositions,
        List<Position> rightPositions, double distance, double elevation, Globe globe, Vec4 previousOffset)
    {
        if ((point == null) || (prev == null && next == null))
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (leftPositions == null || rightPositions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 offset;
        Vec4 normal = globe.computeSurfaceNormalAtPoint(point);

        // Compute vector in the direction backward along the line.
        Vec4 backward = (prev != null) ? prev.subtract3(point) : point.subtract3(next);

        // Compute a vector perpendicular to segment BC, and the globe normal vector.
        Vec4 perpendicular = backward.cross3(normal);

        // If the current point is co-located with either the next or prev points, then reuse the previously computed offset.
        if (point.equals(prev) || (point.equals(next)) && previousOffset != null)
        {
            offset = previousOffset;
        }
        // If both next and previous points are supplied then calculate the angle that bisects the angle current, next, prev.
        else if (next != null && prev != null && !Vec4.areColinear(prev, point, next))
        {
            // Compute vector in the forward direction.
            Vec4 forward = next.subtract3(point);

            // Calculate the vector that bisects angle ABC.
            offset = forward.normalize3().add3(backward.normalize3());
            offset = offset.normalize3();

            // Determine the length of the offset vector that will keep the left and right lines parallel to the control
            // line.
            Angle theta = backward.angleBetween3(offset);

            // If the angle is less than 1/10 of a degree than treat this segment as if it were linear.
            double length;
            if (theta.degrees > 0.1)
                length = distance / theta.sin();
            else
                length = distance;

            // Compute the scalar triple product of the vector BC, the normal vector, and the offset vector to
            // determine if the offset points to the left or the right of the control line.
            double tripleProduct = perpendicular.dot3(offset);
            if (tripleProduct < 0)
            {
                offset = offset.multiply3(-1);
            }

            offset = offset.multiply3(length);
        }
        else
        {
            offset = perpendicular.normalize3();
            offset = offset.multiply3(distance);
        }

        // Determine the left and right points by applying the offset.
        Vec4 ptRight = point.add3(offset);
        Vec4 ptLeft = point.subtract3(offset);

        // Convert cartesian points to geographic.
        Position posLeft = new Position(globe.computePositionFromPoint(ptLeft), elevation);
        Position posRight = new Position(globe.computePositionFromPoint(ptRight), elevation);

        leftPositions.add(posLeft);
        rightPositions.add(posRight);

        return offset;
    }
}

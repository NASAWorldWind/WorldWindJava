/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.animation;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

/**
 * @author jym
 * @version $Id: AnimationSupport.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AnimationSupport
{

    /**
     * Calcualte a scaled time based on the great circle distance between two points.  The time is calulated by
     * interpolating between the <code>minLengthMillis</code> and the <code>maxLengthMillis</code> using the ratio
     * of the spherical distance between the given positions over 180 degrees.
     *
     * @param beginLatLon The first geographic position
     * @param endLatLon The second geographio position
     * @param minTimeMillis The minimum length of tine
     * @param maxTimeMillis The maximum length of time
     * @return The scaled time in milliseconds.
     */
    public static long getScaledTimeMillisecs(
            LatLon beginLatLon, LatLon endLatLon,
            long minTimeMillis, long maxTimeMillis)
    {
        Angle sphericalDistance = LatLon.greatCircleDistance(beginLatLon, endLatLon);
        double scaleFactor = angularRatio(sphericalDistance, Angle.POS180);
        return (long) mixDouble(scaleFactor, minTimeMillis, maxTimeMillis);
    }

    /**
     * Calculate a scaled tiome based on the ratio of the angular distance between the <code>begin</code> and
     * <code>end</code> angles over the max value.
     *
     * @param begin the begin angle
     * @param end the end angle
     * @param max the maximun number of degrees
     * @param minTimeMillisecs the minimum length of time
     * @param maxTimeMillisecs the maximum length of time
     * @return the scaled time in milliseconds
     */
    public static long getScaledTimeMillisecs(
        Angle begin, Angle end, Angle max,
        long minTimeMillisecs, long maxTimeMillisecs)
    {
        Angle angularDistance = begin.angularDistanceTo(end);
        double scaleFactor = angularRatio(angularDistance, max);
        return (long) mixDouble(scaleFactor, minTimeMillisecs, maxTimeMillisecs);
    }

    /**
     * Calculate a scaled time based on the ratio of the distance between the <code>beginZoom</code> and
     * <code>endZoom</code> distances over the larger of the beginZoom and endZoom.
     *
     * @param beginZoom the begin zoom value
     * @param endZoom the end zoom value
     * @param minTimeMillisecs the minimum length of time
     * @param maxTimeMillisecs the maximum length of time
     * @return the scaled time in milliseconds
     */
    public static long getScaledTimeMillisecs(
        double beginZoom, double endZoom,
        long minTimeMillisecs, long maxTimeMillisecs)
    {
        double scaleFactor = Math.abs(endZoom - beginZoom) / Math.max(endZoom, beginZoom);
        // Clamp scaleFactor to range [0, 1].
        scaleFactor = clampDouble(scaleFactor, 0.0, 1.0);
        // Iteration time is interpolated value between minumum and maximum lengths.
        return (long) mixDouble(scaleFactor, minTimeMillisecs, maxTimeMillisecs);
    }

    /**
     * Calculate the angular ratio between two angles
     * @param x The numerator
     * @param y The denominator
     * @return The angular ratio of <code>x/y</code>
     */
    public static double angularRatio(Angle x, Angle y)
    {
        if (x == null || y == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double unclampedRatio = x.divide(y);
        return clampDouble(unclampedRatio, 0, 1);
    }

    /**
     * Calculate the linear interpolation between two values
     *
     * @param amount The interpolant, a number between 0 and 1.
     * @param value1 The minimum value of the range
     * @param value2 The maximum value of the range
     * @return the interpolated value
     */
    public static double mixDouble(double amount, double value1, double value2)
    {
        if (amount < 0)
            return value1;
        else if (amount > 1)
            return value2;
        return value1 * (1.0 - amount) + value2 * amount;
    }

    /**
     * Clamps a value between a minimum and maximum
     *
     * @param value the value to clamo
     * @param min the minimum
     * @param max the maximum
     * @return the clamped value
     */
    public static double clampDouble(double value, double min, double max)
    {
        return value < min ? min : (value > max ? max : value);
    }

    /**
     * Normalize an interpolant value
     *
     * @param amount The value to normalize
     * @param startAmount The lower end of the range
     * @param stopAmount  The upper end of the range
     * @return the normalized interpolant
     */
    public static double interpolantNormalized(double amount, double startAmount,
        double stopAmount)
    {
        if (amount < startAmount)
            return 0.0;
        else if (amount > stopAmount)
            return 1.0;
        if ((stopAmount - startAmount) == 0)
        {
            return(1.0);
        }
        return (amount - startAmount) / (stopAmount - startAmount);
    }

    /**
     * Smooth an interpolant value using hermite smoothing
     *
     * @param interpolant The interpolant
     * @param smoothingIterations the number of smoothing iterations
     * @return the smoothed interpolant
     */
    public static double interpolantSmoothed(double interpolant, int smoothingIterations)
    {
        // Apply iterative hermite smoothing.
        double smoothed = interpolant;
        for (int i = 0; i < smoothingIterations; i++)
        {
            smoothed = smoothed * smoothed * (3.0 - 2.0 * smoothed);
        }
        return smoothed;
    }

    /**
     * Calculate a normalized, smoothed interpolant
     * @param interpolant the unsmoothed, unnormalized interpolant
     * @param startInterpolant the lower end of interpolant range
     * @param stopInterpolant the higher end of the interpolant range
     * @param maxSmoothing the numver of iterations to smooth.
     * @return the normalized, smoothed interpolant
     */
    public static double basicInterpolant(double interpolant, double startInterpolant, double stopInterpolant,
        int maxSmoothing)
    {
        double normalizedInterpolant = interpolantNormalized(interpolant, startInterpolant, stopInterpolant);
        return interpolantSmoothed(normalizedInterpolant, maxSmoothing);
    }
}

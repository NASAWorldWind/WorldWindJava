/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Utility methods for working with tactical graphics.
 *
 * @author pabercrombie
 * @version $Id: TacticalGraphicUtil.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TacticalGraphicUtil
{
    /**
     * Convert a list of cartesian points to Positions.
     *
     * @param globe  Globe used to convert points to positions.
     * @param points Points to convert.
     *
     * @return List of positions computed from cartesian points.
     */
    public static List<Position> asPositionList(Globe globe, Vec4... points)
    {
        List<Position> positions = new ArrayList<Position>(points.length);
        for (Vec4 point : points)
        {
            positions.add(globe.computePositionFromPoint(point));
        }
        return positions;
    }

    /**
     * Get the date range from a graphic's modifiers. This method looks at the value of the <code>AVKey.DATE_TIME</code>
     * modifier, and returns the results as a two element array. If the value of the modifier is an
     * <code>Iterable</code>, then this method returns the first two values of the iteration. If the value of the
     * modifier is a single object, this method returns an array containing that object and <code>null</code>.
     *
     * @param graphic Graphic from which to retrieve dates.
     *
     * @return A two element array containing the altitude modifiers. One or both elements may be null.
     */
    public static Object[] getDateRange(TacticalGraphic graphic)
    {
        Object date1 = null;
        Object date2 = null;

        Object o = graphic.getModifier(SymbologyConstants.DATE_TIME_GROUP);
        if (o instanceof Iterable)
        {
            Iterator iterator = ((Iterable) o).iterator();
            if (iterator.hasNext())
            {
                date1 = iterator.next();
            }

            if (iterator.hasNext())
            {
                date2 = iterator.next();
            }
        }
        else
        {
            date1 = o;
        }

        return new Object[] {date1, date2};
    }

    /**
     * Get the altitude range from the graphic's modifiers. This method looks at the value of the
     * <code>AVKey.ALTITUDE</code> modifier, and returns the results as a two element array. If the value of the
     * modifier is an <code>Iterable</code>, then this method returns the first two values of the iteration. If the
     * value of the modifier is a single object, this method returns an array containing that object and
     * <code>null</code>.
     *
     * @param graphic Graphic from which to retrieve dates.
     *
     * @return A two element array containing the altitude modifiers. One or both elements may be null.
     */
    public static Object[] getAltitudeRange(TacticalGraphic graphic)
    {
        Object alt1 = null;
        Object alt2 = null;

        Object o = graphic.getModifier(SymbologyConstants.ALTITUDE_DEPTH);
        if (o instanceof Iterable)
        {
            Iterator iterator = ((Iterable) o).iterator();
            if (iterator.hasNext())
            {
                alt1 = iterator.next();
            }

            if (iterator.hasNext())
            {
                alt2 = iterator.next();
            }
        }
        else
        {
            alt1 = o;
        }

        return new Object[] {alt1, alt2};
    }

    /**
     * Position one or two labels some distance along the path. Top and bottom labels are often positioned above and
     * below the same point, so this method supports positioning a pair of labels at the same point. The label offsets
     * determine how the labels draw in relation to the line.
     *
     * @param dc        Current draw context.
     * @param positions Positions that describe the path.
     * @param label1    First label to position.
     * @param label2    Second label to position. (May be null.)
     * @param distance  Distance along the path at which to position the labels.
     */
    public static void placeLabelsOnPath(DrawContext dc, Iterable<? extends Position> positions,
        TacticalGraphicLabel label1,
        TacticalGraphicLabel label2, double distance)
    {
        Iterator<? extends Position> iterator = positions.iterator();
        Globe globe = dc.getGlobe();

        Position pos1 = null;
        Position pos2;
        Vec4 pt1, pt2;

        double length = 0;
        double thisDistance = 0;

        pos2 = iterator.next();
        pt2 = globe.computePointFromLocation(pos2);

        while (iterator.hasNext() && length < distance)
        {
            pos1 = pos2;
            pt1 = pt2;

            pos2 = iterator.next();
            pt2 = globe.computePointFromLocation(pos2);

            thisDistance = pt2.distanceTo2(pt1);
            length += thisDistance;
        }

        if (pos1 != null && pos2 != null && thisDistance > 0)
        {
            double delta = length - distance;
            LatLon ll = LatLon.interpolateGreatCircle(delta / thisDistance, pos1, pos2);
            pos1 = new Position(ll, 0);

            label1.setPosition(pos1);
            label1.setOrientationPosition(pos2);

            if (label2 != null)
            {
                label2.setPosition(pos1);
                label2.setOrientationPosition(pos2);
            }
        }
    }

    /**
     * Compute a point along a Bezier curve defined by a list of control points. The first and last points should mark
     * the start and end of the curve.
     * <p/>
     * This function implements the Bezier curve equation from "Mathematics for 3D Game Programming and Computer
     * Graphics, Second Edition" by Eric Lengyel (equation 15.16, pg. 458).
     * <p/>
     * A typical usage looks like this:
     * <pre>
     * Vec4[] controlPoints = ... // Determine control points appropriate for your curve
     *
     * List<Position> curvePositions = new ArrayList<Position>();
     * int[] coefficients = new int[controlPoints.length];
     *
     * int intervals = 32;
     * double delta = 1.0 / intervals;
     * for (int i = 0; i < intervals; i++)
     * {
     *     double t = i * delta;
     *     Vec4 pt = TacticalGraphicUtil.bezierCurve(controlPoints, t, coefficients);
     *     Position pos = globe.computePositionFromPoint(p);
     *     curvePositions.add(pos);
     * }
     * </pre>
     *
     * @param controlPoints Control points for the curve.
     * @param t             Interpolation parameter in the range [0..1].
     * @param coefficients  Array to store binomial coefficients between invocations of this function. On the first
     *                      invocation, pass an int[] with length equal to the controlPoints array. bezierCurve will
     *                      populate the array on the first invocation, and reuse the computed values on subsequent
     *                      invocations.
     *
     * @return A point along the curve.
     */
    public static Vec4 bezierCurve(Vec4[] controlPoints, double t, int[] coefficients)
    {
        if (coefficients == null || controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (coefficients.length != controlPoints.length)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", coefficients.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (coefficients[0] != 1)
        {
            binomial(coefficients.length - 1, coefficients);
        }

        int n = controlPoints.length - 1;

        Vec4 r = Vec4.ZERO;
        for (int k = 0; k <= n; k++)
        {
            double c = coefficients[k] * Math.pow(t, k) * Math.pow(1 - t, n - k);
            r = r.add3(controlPoints[k].multiply3(c));
        }

        return r;
    }

    /**
     * Compute binomial coefficients for a polynomial of order n. Stated another way, computes the nth row of Pascal's
     * triangle.
     *
     * @param n            Order of polynomial for which to calculate coefficients.
     * @param coefficients Array to receive coefficients. The length of this array must be n + 1.
     */
    protected static void binomial(int n, int[] coefficients)
    {
        if (coefficients == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (coefficients.length != n + 1)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", coefficients.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Algorithm from "Data Structures and Algorithms with Object-Oriented Design Patterns in Java" by Bruno R.
        // Preiss (http://www.brpreiss.com/books/opus5/html/page460.html)
        for (int i = 0; i <= n; i++)
        {
            coefficients[i] = 1;
            for (int j = i - 1; j > 0; j--)
            {
                coefficients[j] += coefficients[j - 1];
            }
        }
    }
}

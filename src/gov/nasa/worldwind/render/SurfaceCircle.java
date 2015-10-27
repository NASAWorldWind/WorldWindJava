/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: SurfaceCircle.java 2302 2014-09-08 20:40:47Z tgaskins $
 */
public class SurfaceCircle extends SurfaceEllipse
{
    /** Constructs a new surface circle with the default attributes, default center location and default radius. */
    public SurfaceCircle()
    {
    }

    /**
     * Creates a shallow copy of the specified source shape.
     *
     * @param source the shape to copy.
     */
    public SurfaceCircle(SurfaceCircle source)
    {
        super(source);
    }

    /**
     * Constructs a new surface circle with the specified normal (as opposed to highlight) attributes, default center
     * location, and default radius. Modifying the attribute reference after calling this constructor causes this
     * shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     */
    public SurfaceCircle(ShapeAttributes normalAttrs)
    {
        super(normalAttrs);
    }

    /**
     * Constructs a new surface circle with the default attributes, the specified center location and radius (in
     * meters).
     *
     * @param center the circle's center location.
     * @param radius the circle's radius, in meters.
     *
     * @throws IllegalArgumentException if the center is null, or if the radius is negative.
     */
    public SurfaceCircle(LatLon center, double radius)
    {
        super(center, radius, radius);
    }

    /**
     * Constructs a new surface circle with the default attributes, the specified center location, radius (in meters),
     * and initial number of geometry intervals.
     *
     * @param center    the circle's center location.
     * @param radius    the circle's radius, in meters.
     * @param intervals the initial number of intervals (or slices) defining the circle's geometry.
     *
     * @throws IllegalArgumentException if the center is null, if the radius is negative, or if the number of intervals
     *                                  is less than 8.
     */
    public SurfaceCircle(LatLon center, double radius, int intervals)
    {
        super(center, radius, radius, Angle.ZERO, intervals);
    }

    /**
     * Constructs a new surface circle with the specified normal (as opposed to highlight) attributes, the specified
     * center location, and radius (in meters). Modifying the attribute reference after calling this constructor causes
     * this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     * @param center      the circle's center location.
     * @param radius      the circle's radius, in meters.
     *
     * @throws IllegalArgumentException if the center is null, or if the radius is negative.
     */
    public SurfaceCircle(ShapeAttributes normalAttrs, LatLon center, double radius)
    {
        super(normalAttrs, center, radius, radius);
    }

    /**
     * Constructs a new surface circle with the specified normal (as opposed to highlight) attributes, the specified
     * center location,  radius (in meters), and initial number of geometry intervals. Modifying the attribute reference
     * after calling this constructor causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     * @param center      the circle's center location.
     * @param radius      the circle's radius, in meters.
     * @param intervals   the initial number of intervals (or slices) defining the circle's geometry.
     *
     * @throws IllegalArgumentException if the center is null, if the radius is negative, or if the number of intervals
     *                                  is less than 8.
     */
    public SurfaceCircle(ShapeAttributes normalAttrs, LatLon center, double radius, int intervals)
    {
        super(normalAttrs, center, radius, radius, Angle.ZERO, intervals);
    }

    public double getRadius()
    {
        return this.getMajorRadius();
    }

    public void setRadius(double radius)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative", radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setRadii(radius, radius);
    }
}

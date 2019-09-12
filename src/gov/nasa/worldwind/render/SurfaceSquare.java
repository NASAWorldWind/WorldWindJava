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
 * @version $Id: SurfaceSquare.java 2302 2014-09-08 20:40:47Z tgaskins $
 */
public class SurfaceSquare extends SurfaceQuad
{
    /**
     * Constructs a new surface square with the default attributes, default center location, default size, and default
     * heading.
     */
    public SurfaceSquare()
    {
    }

    /**
     * Creates a shallow copy of the specified source shape.
     *
     * @param source the shape to copy.
     */
    public SurfaceSquare(SurfaceSquare source)
    {
        super(source);
    }

    /**
     * Constructs a new surface square with the specified normal (as opposed to highlight) attributes, default center
     * location, default size, and default heading. Modifying the attribute reference after calling this constructor
     * causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     */
    public SurfaceSquare(ShapeAttributes normalAttrs)
    {
        super(normalAttrs);
    }

    /**
     * Constructs a new surface square with the default attributes, the specified center location and size (in meters).
     *
     * @param center the square's center location.
     * @param size   the square's width and height, in meters.
     *
     * @throws IllegalArgumentException if the center is null, or if the size is negative.
     */
    public SurfaceSquare(LatLon center, double size)
    {
        super(center, size, size);
    }

    /**
     * Constructs a new surface square with the default attributes, the specified center location, size (in meters), and
     * heading clockwise from North.
     *
     * @param center  the square's center location.
     * @param size    the square's width and height, in meters.
     * @param heading the square's heading, clockwise from North.
     *
     * @throws IllegalArgumentException if the center or heading are null, or if the size is negative.
     */
    public SurfaceSquare(LatLon center, double size, Angle heading)
    {
        super(center, size, size, heading);
    }

    /**
     * Constructs a new surface square with the specified normal (as opposed to highlight) attributes, the specified
     * center location and size (in meters). Modifying the attribute reference after calling this constructor causes
     * this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     * @param center      the square's center location.
     * @param size        the square's width and height, in meters.
     *
     * @throws IllegalArgumentException if the center is null, or if the size is negative.
     */
    public SurfaceSquare(ShapeAttributes normalAttrs, LatLon center, double size)
    {
        super(normalAttrs, center, size, size);
    }

    /**
     * Constructs a new surface square with the specified normal (as opposed to highlight) attributes, the specified
     * center location and dimensions (in meters). Modifying the attribute reference after calling this constructor
     * causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     * @param center      the square's center location.
     * @param size        the square's width and height, in meters.
     * @param heading     the square's heading, clockwise from North.
     *
     * @throws IllegalArgumentException if the center or heading are null, or if the size is negative.
     */
    public SurfaceSquare(ShapeAttributes normalAttrs, LatLon center, double size, Angle heading)
    {
        super(normalAttrs, center, size, size, heading);
    }

    public double getSize()
    {
        return this.getWidth();
    }

    public void setSize(double size)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("Geom.SizeIsNegative", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setSize(size, size);
    }
}

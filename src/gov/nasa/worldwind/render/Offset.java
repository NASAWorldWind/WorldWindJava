/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.*;

import java.awt.*;

/**
 * Defines the relationship of an image, label or other screen-space item relative to another screen-space item. An
 * offset contains an X coordinate, a Y coordinate, and for each of these a separate "units" string indicating the
 * coordinate units.
 * <p>
 * Recognized "units" values are {@link AVKey#PIXELS}, which indicates pixel units relative to the lower left corner of
 * the screen-space item, {@link AVKey#FRACTION}, which indicates that the units are fractions of the screen-space
 * item's width and height, relative to its lower left corner, and {@link AVKey#INSET_PIXELS}, which indicates units of
 * pixels but with origin at the screen-space item's upper right.
 * <p>
 * This class implements the functionality of a KML <i>Offset</i>.
 *
 * @author tag
 * @version $Id: Offset.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Offset
{
    public static final Offset CENTER = Offset.fromFraction(0.5, 0.5);
    public static final Offset BOTTOM_CENTER = Offset.fromFraction(0.5, 0.0);
    public static final Offset TOP_CENTER = Offset.fromFraction(0.5, 1.0);
    public static final Offset LEFT_CENTER = Offset.fromFraction(0.0, 0.5);
    public static final Offset RIGHT_CENTER = Offset.fromFraction(1.0, 0.5);

    protected Double x;
    protected Double y;
    protected String xUnits;
    protected String yUnits;

    public Offset(Double x, Double y, String xUnits, String yUnits)
    {
        this.x = x;
        this.y = y;
        this.xUnits = xUnits;
        this.yUnits = yUnits;
    }

    /**
     * Creates a new offset from explicit fraction coordinates.
     *
     * @param xFraction the offset's X coordinate as a fraction of the containing rectangle.
     * @param yFraction the offset's Y coordinate as a fraction of the containing rectangle.
     *
     * @return a new offset with the specified X and Y coordinates.
     */
    public static Offset fromFraction(double xFraction, double yFraction)
    {
        return new Offset(xFraction, yFraction, AVKey.FRACTION, AVKey.FRACTION);
    }

    /**
     * Returns the hot spot's X coordinate. See {@link #setXUnits(String)} for a description of the hot spot.
     *
     * @return the hot spot's X coordinate.
     */
    public Double getX()
    {
        return x;
    }

    /**
     * Specifies the hot spot's X coordinate, in units specified by {@link #setXUnits(String)}.
     *
     * @param x the hot spot's X coordinate. May be null, in which case 0 is used during rendering.
     */
    public void setX(Double x)
    {
        this.x = x;
    }

    /**
     * Returns the hot spot's Y coordinate. See {@link #setYUnits(String)} for a description of the hot spot.
     *
     * @return the hot spot's Y coordinate.
     */
    public Double getY()
    {
        return y;
    }

    /**
     * Specifies the hot spot's Y coordinate, in units specified by {@link #setYUnits(String)}.
     *
     * @param y the hot spot's Y coordinate. May be null, in which case 0 is used during rendering.
     */
    public void setY(Double y)
    {
        this.y = y;
    }

    /**
     * Returns the units of the offset X value. See {@link #setXUnits(String)} for a description of the recognized
     * values.
     *
     * @return the units of the offset X value, or null.
     */
    public String getXUnits()
    {
        return xUnits;
    }

    /**
     * Specifies the units of the offset X value. Recognized values are {@link AVKey#PIXELS}, which indicates pixel
     * units relative to the lower left corner of the placemark image, {@link AVKey#FRACTION}, which indicates the units
     * are fractions of the placemark image width and height, and {@link AVKey#INSET_PIXELS}, which indicates units of
     * pixels but with origin in the upper left.
     *
     * @param units the units of the offset X value. If null, {@link AVKey#PIXELS} is used during rendering.
     */
    public void setXUnits(String units)
    {
        this.xUnits = units;
    }

    /**
     * Returns the units of the offset Y value. See {@link #setYUnits(String)} for a description of the recognized
     * values.
     *
     * @return the units of the offset Y value, or null.
     */
    public String getYUnits()
    {
        return yUnits;
    }

    /**
     * Specifies the units of the offset Y value. Recognized values are {@link AVKey#PIXELS}, which indicates pixel
     * units relative to the lower left corner of the placemark image, {@link AVKey#FRACTION}, which indicates the units
     * are fractions of the placemark image width and height, and {@link AVKey#INSET_PIXELS}, which indicates units of
     * pixels but with origin in the upper left.
     *
     * @param units the units of the offset Y value. If null, {@link AVKey#PIXELS} is used during rendering.
     */
    public void setYUnits(String units)
    {
        this.yUnits = units;
    }

    /**
     * Computes the X and Y offset specified by this offset applied to a specified rectangle.
     *
     * @param width  the rectangle width.
     * @param height the rectangle height.
     * @param xScale an optional scale to apply to the X coordinate of the offset. May be null.
     * @param yScale an optional scale to apply to the Y coordinate of the offset. May be null.
     *
     * @return the result of applying this offset to the specified rectangle and incorporating the optional scales.
     */
    public Point.Double computeOffset(double width, double height, Double xScale, Double yScale)
    {
        double dx = 0;
        double dy = 0;

        if (this.getX() != null)
        {
            String units = this.getXUnits();
            if (AVKey.PIXELS.equals(units))
                dx = this.getX();
            else if (AVKey.INSET_PIXELS.equals(units))
                dx = width - this.getX();
            else if (AVKey.FRACTION.equals(units))
                dx = (width * this.getX());
            else
                dx = this.getX(); // treat as pixels
        }

        if (this.getY() != null)
        {
            String units = this.getYUnits();
            if (AVKey.PIXELS.equals(units))
                dy = this.getY();
            else if (AVKey.INSET_PIXELS.equals(units))
                dy = height - this.getY();
            else if (AVKey.FRACTION.equals(units))
                dy = (height * this.getY());
            else
                dy = this.getY(); // treat as pixels
        }

        if (xScale != null)
            dx *= xScale;

        if (yScale != null)
            dy *= yScale;

        return new Point.Double(dx, dy);
    }

    /**
     * Saves the offset's current state in the specified <code>restorableSupport</code>. If <code>context</code> is not
     * <code>null</code>, the state is appended to it.  Otherwise the state is added to the
     * <code>RestorableSupport</code> root. This state can be restored later by calling {@link
     * #restoreState(gov.nasa.worldwind.util.RestorableSupport, gov.nasa.worldwind.util.RestorableSupport.StateObject)}.
     *
     * @param restorableSupport the <code>RestorableSupport</code> that receives the offset's state.
     * @param context           the <code>StateObject</code> the state is appended to, if not <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>restorableSupport</code> is <code>null</code>.
     */
    public void getRestorableState(RestorableSupport restorableSupport, RestorableSupport.StateObject context)
    {
        if (restorableSupport == null)
        {
            String message = Logging.getMessage("nullValue.RestorableSupportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.getX() != null)
            restorableSupport.addStateValueAsDouble(context, "x", this.getX());

        if (this.getY() != null)
            restorableSupport.addStateValueAsDouble(context, "y", this.getY());

        if (this.getXUnits() != null)
            restorableSupport.addStateValueAsString(context, "xUnits", this.getXUnits());

        if (this.getYUnits() != null)
            restorableSupport.addStateValueAsString(context, "yUnits", this.getYUnits());
    }

    /**
     * Restores the state of any offset parameters contained in the specified <code>RestorableSupport</code>. If the
     * <code>StateObject</code> is not <code>null</code> it's searched for state values, otherwise the
     * <code>RestorableSupport</code> root is searched.
     *
     * @param restorableSupport the <code>RestorableSupport</code> that contains the offset's state.
     * @param context           the <code>StateObject</code> to search for state values, if not <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>restorableSupport</code> is <code>null</code>.
     */
    public void restoreState(RestorableSupport restorableSupport, RestorableSupport.StateObject context)
    {
        if (restorableSupport == null)
        {
            String message = Logging.getMessage("nullValue.RestorableSupportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Double d = restorableSupport.getStateValueAsDouble(context, "x");
        if (d != null)
            this.setX(d);

        d = restorableSupport.getStateValueAsDouble(context, "y");
        if (d != null)
            this.setY(d);

        String s = restorableSupport.getStateValueAsString(context, "xUnits");
        if (s != null)
            this.setXUnits(s);

        s = restorableSupport.getStateValueAsString(context, "yUnits");
        if (s != null)
            this.setYUnits(s);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Offset that = (Offset) o;

        if (this.x != null ? !this.x.equals(that.x) : that.x != null)
            return false;
        if (this.y != null ? !this.y.equals(that.y) : that.y != null)
            return false;
        if (this.xUnits != null ? !this.xUnits.equals(that.xUnits) : that.xUnits != null)
            return false;
        //noinspection RedundantIfStatement
        if (this.yUnits != null ? !this.yUnits.equals(that.yUnits) : that.yUnits != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = this.x != null ? this.x.hashCode() : 0;
        result = 31 * result + (this.y != null ? this.y.hashCode() : 0);
        result = 31 * result + (this.xUnits != null ? this.xUnits.hashCode() : 0);
        result = 31 * result + (this.yUnits != null ? this.yUnits.hashCode() : 0);
        return result;
    }
}

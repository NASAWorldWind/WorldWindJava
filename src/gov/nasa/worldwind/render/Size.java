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
 * Defines the dimensions of an image, label or other screen-space item relative to a container (for example, the
 * viewport). A size contains a width, a height, a width size mode, a height size mode, and for each of these a "units"
 * string indicating the coordinate units.
 * <p>
 * The possible size modes are: <ul> <li> {@link #NATIVE_DIMENSION} - Maintain the native dimensions.</li> <li> {@link
 * #MAINTAIN_ASPECT_RATIO} - Maintain the aspect ratio of the image when one dimension is specified and the other is
 * not.</li> <li> {@link #EXPLICIT_DIMENSION} - Use an explicit dimension. This dimension may be either an absolute
 * pixel value, or a fraction of the container.</li></ul>
 * <p>
 * Recognized units are {@link AVKey#PIXELS}, which indicates pixel units relative to the lower left corner of the
 * image, or {@link AVKey#FRACTION}, which indicates the units are fractions of the image width and height.
 * <p>
 * Examples:
 * <pre>
 * Width mode      Height mode      Width (Units)      Height (Units)        Result
 * --------------------------------------------------------------------------------------------------------------------
 * Native          Native           N/A                N/A                   Keep native dimensions
 * Aspect ratio    Explicit         N/A                100 (pix)             Scale image so that height is 100 pixels,
 *                                                                           but maintain aspect ratio
 * Explicit        Aspect ratio     0.5 (fraction)     N/A                   Make the width half of the container, and
 *                                                                           scale height to maintain aspect ratio
 * Explicit        Native           1.0 (fraction)     N/A                   Stretch the image to fill the width of the
 *                                                                           container, but do not scale the height.
 * </pre>
 *
 * This class implements the functionality of a KML <i>size</i>.
 *
 * @author pabercrombie
 * @version $Id: Size.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Size
{
    /** Size mode to indicate that the content's native dimension must be used. */
    public static final String NATIVE_DIMENSION = "gov.nasa.worldwind.render.Size.NativeDimension";

    /** Size mode to indicate that the content's aspect ratio must be maintained. */
    public static final String MAINTAIN_ASPECT_RATIO = "gov.nasa.worldwind.render.Size.MaintainAspectRatio";

    /**
     * Size mode to indicate that the size parameter indicates an explicit dimension measured either in pixels, or as a
     * fraction of the container.
     */
    public static final String EXPLICIT_DIMENSION = "gov.nasa.worldwind.render.Size.ExplicitDimension";

    /**
     * Size mode for width. May be one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     * #EXPLICIT_DIMENSION}.
     */
    protected String widthMode = NATIVE_DIMENSION;
    /** Units of width. */
    protected String widthUnits = AVKey.PIXELS;
    /** Width size parameter. */
    protected double widthParam;

    /**
     * Size mode for height. May be one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     * #EXPLICIT_DIMENSION}.
     */
    protected String heightMode = NATIVE_DIMENSION;
    /** Units of height. */
    protected String heightUnits = AVKey.PIXELS;
    /** Height size parameter. */
    protected double heightParam;

    /** Create a Size object that will preserve native dimensions. */
    public Size()
    {
    }

    /**
     * Create a Size with specified dimensions.
     *
     * @param widthMode   Width mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *                    #EXPLICIT_DIMENSION}.
     * @param widthParam  The width (applies only to {@link #EXPLICIT_DIMENSION} mode).
     * @param widthUnits  Units of {@code width}. Either {@link AVKey#PIXELS} or {@link AVKey#PIXELS}.
     * @param heightMode  height mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *                    #EXPLICIT_DIMENSION}.
     * @param heightParam The height (applies only to {@link #EXPLICIT_DIMENSION} mode).
     * @param heightUnits Units of {@code height}. Either {@link AVKey#PIXELS} or {@link AVKey#PIXELS}.
     *
     * @see #setWidth(String, double, String)
     * @see #setHeight(String, double, String)
     */
    public Size(String widthMode, double widthParam, String widthUnits, String heightMode, double heightParam,
        String heightUnits)
    {
        this.setWidth(widthMode, widthParam, widthUnits);
        this.setHeight(heightMode, heightParam, heightUnits);
    }

    /**
     * Create a size from explicit pixel dimensions.
     *
     * @param widthInPixels  Width of rectangle in pixels.
     * @param heightInPixels Height of rectangle in pixels.
     *
     * @return New size object.
     */
    public static Size fromPixels(int widthInPixels, int heightInPixels)
    {
        return new Size(EXPLICIT_DIMENSION, widthInPixels, AVKey.PIXELS,
            EXPLICIT_DIMENSION, heightInPixels, AVKey.PIXELS);
    }

    /**
     * Creates a new size from explicit fraction dimensions.
     *
     * @param widthFraction  the size's width as a fraction of the containing rectangle.
     * @param heightFraction the size's height as a fraction of the containing rectangle.
     *
     * @return a new size with the specified width and height.
     */
    public static Size fromFraction(double widthFraction, double heightFraction)
    {
        return new Size(EXPLICIT_DIMENSION, widthFraction, AVKey.FRACTION,
            EXPLICIT_DIMENSION, heightFraction, AVKey.FRACTION);
    }

    /**
     * Set the width.
     *
     * @param mode  Width mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *              #EXPLICIT_DIMENSION}.
     * @param width The width (applies only to {@link #EXPLICIT_DIMENSION} mode).
     * @param units Units of {@code width}. Either {@link AVKey#PIXELS} or {@link AVKey#PIXELS}.
     *
     * @throws IllegalArgumentException if {@code mode} is null.
     */
    public void setWidth(String mode, double width, String units)
    {
        if (mode == null)
        {
            String message = Logging.getMessage("nullValue.SizeModeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.widthMode = mode;
        this.widthParam = width;
        this.widthUnits = units;
    }

    /**
     * Set the height.
     *
     * @param mode   Width mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *               #EXPLICIT_DIMENSION}.
     * @param height The width (applies only to {@link #EXPLICIT_DIMENSION} mode).
     * @param units  Units of {@code width}. Either {@link AVKey#PIXELS} or {@link AVKey#FRACTION}.
     *
     * @throws IllegalArgumentException if {@code mode} is null.
     */
    public void setHeight(String mode, double height, String units)
    {
        if (mode == null)
        {
            String message = Logging.getMessage("nullValue.SizeModeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.heightMode = mode;
        this.heightParam = height;
        this.heightUnits = units;
    }

    /**
     * Returns the units of the offset X value. See {@link #setWidth(String, double, String)} for a description of the
     * recognized values.
     *
     * @return the units of the offset X value, or null.
     */
    public String getWidthUnits()
    {
        return widthUnits;
    }

    /**
     * Returns the units of the offset Y value. See {@link #setHeight(String, double, String)} for a description of the
     * recognized values.
     *
     * @return the units of the offset Y value, or null.
     */
    public String getHeightUnits()
    {
        return heightUnits;
    }

    /**
     * Get the mode of the width dimension.
     *
     * @return Width mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *         #EXPLICIT_DIMENSION}.
     */
    public String getWidthMode()
    {
        return this.widthMode;
    }

    /**
     * Get the mode of the height dimension.
     *
     * @return Height mode, one of {@link #NATIVE_DIMENSION}, {@link #MAINTAIN_ASPECT_RATIO}, or {@link
     *         #EXPLICIT_DIMENSION}.
     */
    public String getHeightMode()
    {
        return this.heightMode;
    }

    /**
     * Get the unscaled width.
     *
     * @return Unscaled width. The units of this value depend on the current height units.
     *
     * @see #getWidthMode()
     * @see #getWidthUnits()
     */
    public double getWidth()
    {
        return widthParam;
    }

    /**
     * Get the unscaled height.
     *
     * @return Unscaled height. The units of this value depend on the current height units.
     *
     * @see #getHeightMode()
     * @see #getHeightUnits()
     */
    public double getHeight()
    {
        return heightParam;
    }

    /**
     * Computes the width and height of a rectangle within a container rectangle.
     *
     * @param rectWidth       The width of the rectangle to size.
     * @param rectHeight      The height of the rectangle to size.
     * @param containerWidth  The width of the container.
     * @param containerHeight The height of the container.
     *
     * @return The desired image dimensions.
     */
    public Dimension compute(int rectWidth, int rectHeight, int containerWidth, int containerHeight)
    {
        if (rectWidth < 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", rectWidth);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (rectHeight < 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", rectHeight);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (containerWidth < 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", containerWidth);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (containerHeight < 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", containerHeight);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double aspectRatio;
        if (rectHeight != 0)
            aspectRatio = (double) rectWidth / rectHeight;
        else
            aspectRatio = 0;

        String xMode = this.getWidthMode();
        String yMode = this.getHeightMode();

        double width, height;

        if (NATIVE_DIMENSION.equals(xMode) && NATIVE_DIMENSION.equals(yMode)
            || NATIVE_DIMENSION.equals(xMode) && MAINTAIN_ASPECT_RATIO.equals(yMode)
            || MAINTAIN_ASPECT_RATIO.equals(xMode) && NATIVE_DIMENSION.equals(yMode)
            || MAINTAIN_ASPECT_RATIO.equals(xMode) && MAINTAIN_ASPECT_RATIO.equals(yMode))
        {
            // Keep original dimensions
            width = rectWidth;
            height = rectHeight;
        }
        else if (MAINTAIN_ASPECT_RATIO.equals(xMode))
        {
            // y dimension is specified, scale x to maintain aspect ratio
            height = computeSize(this.heightParam, this.heightUnits, containerHeight);
            width = height * aspectRatio;
        }
        else if (MAINTAIN_ASPECT_RATIO.equals(yMode))
        {
            // x dimension is specified, scale y to maintain aspect ratio
            width = computeSize(this.widthParam, this.widthUnits, containerWidth);
            if (aspectRatio != 0)
                height = width / aspectRatio;
            else
                height = 0;
        }
        else
        {
            if (NATIVE_DIMENSION.equals(xMode))
                width = rectWidth;
            else
                width = computeSize(this.widthParam, this.widthUnits, containerWidth);

            if (NATIVE_DIMENSION.equals(yMode))
                height = rectHeight;
            else
                height = computeSize(this.heightParam, this.heightUnits, containerHeight);
        }

        return new Dimension((int) width, (int) height);
    }

    /**
     * Compute a dimension taking into account the units of the dimension.
     *
     * @param size               The size parameter.
     * @param units              One of {@link AVKey#PIXELS} or {@link AVKey#FRACTION}. If the {@code units} value is
     *                           not one of the expected options, {@link AVKey#PIXELS} is used as the default.
     * @param containerDimension The viewport dimension.
     *
     * @return Size in pixels
     */
    protected double computeSize(double size, String units, double containerDimension)
    {
        if (AVKey.FRACTION.equals(units))
            return size * containerDimension;
        else  // Default to pixel
            return size;
    }

    /**
     * Saves the size's current state in the specified <code>restorableSupport</code>. If <code>context</code> is not
     * <code>null</code>, the state is appended to it.  Otherwise the state is added to the
     * <code>RestorableSupport</code> root. This state can be restored later by calling {@link
     * #restoreState(gov.nasa.worldwind.util.RestorableSupport, gov.nasa.worldwind.util.RestorableSupport.StateObject)}.
     *
     * @param restorableSupport the <code>RestorableSupport</code> that receives the size's state.
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

        RestorableSupport.StateObject so = restorableSupport.addStateObject(context, "width");
        if (so != null)
        {
            restorableSupport.addStateValueAsString(so, "mode", this.getWidthMode());
            restorableSupport.addStateValueAsDouble(so, "param", this.getWidth());

            if (this.getWidthUnits() != null)
                restorableSupport.addStateValueAsString(so, "units", this.getWidthUnits());
        }

        so = restorableSupport.addStateObject(context, "height");
        if (so != null)
        {
            restorableSupport.addStateValueAsString(so, "mode", this.getHeightMode());
            restorableSupport.addStateValueAsDouble(so, "param", this.getHeight());

            if (this.getHeightUnits() != null)
                restorableSupport.addStateValueAsString(so, "units", this.getHeightUnits());
        }
    }

    /**
     * Restores the state of any size parameters contained in the specified <code>RestorableSupport</code>. If the
     * <code>StateObject</code> is not <code>null</code> it's searched for state values, otherwise the
     * <code>RestorableSupport</code> root is searched.
     *
     * @param restorableSupport the <code>RestorableSupport</code> that contains the size's state.
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

        RestorableSupport.StateObject so = restorableSupport.getStateObject(context, "width");
        if (so != null)
        {
            String mode = restorableSupport.getStateValueAsString(so, "mode");
            mode = convertLegacyModeString(mode);

            Double param = restorableSupport.getStateValueAsDouble(so, "param");
            String units = restorableSupport.getStateValueAsString(so, "units");

            // Restore the width only when the mode and param are specified. null is an acceptable value for units.
            if (mode != null && param != null)
                this.setWidth(mode, param, units);
        }

        so = restorableSupport.getStateObject(context, "height");
        if (so != null)
        {
            String mode = restorableSupport.getStateValueAsString(so, "mode");
            mode = convertLegacyModeString(mode);

            Double param = restorableSupport.getStateValueAsDouble(so, "param");
            String units = restorableSupport.getStateValueAsString(so, "units");

            // Restore the height only when the mode and param are specified. null is an acceptable value for units.
            if (mode != null && param != null)
                this.setHeight(mode, param, units);
        }
    }

    /**
     * Converts a legacy size mode <code>string</code> ("NativeDimension", "MaintainAspectRatio", "ExplicitDimension"),
     * into one of the mode constants (<code>NATIVE_DIMENSION</code>, <code>MAINTAIN_ASPECT_RATIO</code>, or
     * <code>EXPLICIT_DIMENSION</code>). Returns the input string unmodified if the input does not match a legacy size
     * mode.
     *
     * @param string the legacy size mode <code>String</code> to convert to a size mode.
     *
     * @return a size mode constant, or the input string if <code>string</code> is not a legacy size mode.
     */
    protected String convertLegacyModeString(String string)
    {
        if ("NativeDimension".equals(string))
            return NATIVE_DIMENSION;
        else if ("MaintainAspectRatio".equals(string))
            return MAINTAIN_ASPECT_RATIO;
        else if ("ExplicitDimension".equals(string))
            return EXPLICIT_DIMENSION;
        else
            return string;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        Size that = (Size) o;

        if (Double.compare(this.widthParam, that.widthParam) != 0)
            return false;
        if (Double.compare(this.heightParam, that.heightParam) != 0)
            return false;
        if (this.widthUnits != null ? !this.widthUnits.equals(that.widthUnits) : that.widthUnits != null)
            return false;
        if (this.heightUnits != null ? !this.heightUnits.equals(that.heightUnits) : that.heightUnits != null)
            return false;
        if (this.widthMode != null ? !this.widthMode.equals(that.widthMode) : that.widthMode != null)
            return false;
        //noinspection RedundantIfStatement
        if (this.heightMode != null ? !this.heightMode.equals(that.heightMode) : that.heightMode != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        temp = this.widthParam != +0.0d ? Double.doubleToLongBits(this.widthParam) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = this.heightParam != +0.0d ? Double.doubleToLongBits(this.heightParam) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (this.widthUnits != null ? this.widthUnits.hashCode() : 0);
        result = 31 * result + (this.heightUnits != null ? this.heightUnits.hashCode() : 0);
        result = 31 * result + (this.widthMode != null ? this.widthMode.hashCode() : 0);
        result = 31 * result + (this.heightMode != null ? this.heightMode.hashCode() : 0);
        return result;
    }
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.util.*;

/**
 * Basic implementation of the {@link gov.nasa.worldwind.render.BalloonAttributes} interface. Extends
 * <code>BasicShapeAttributes</code> to include attributes for World Wind {@link gov.nasa.worldwind.render.Balloon}
 * shapes.
 *
 * @author pabercrombie
 * @version $Id: BasicBalloonAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicBalloonAttributes extends BasicShapeAttributes implements BalloonAttributes
{
    /** Indicates the width and height of the balloon's shape in the viewport. Initially <code>null</code>. */
    protected Size size;
    /** Indicates the maximum width and height of the balloon's shape in the viewport. Initially <code>null</code>. */
    protected Size maxSize;
    /** Indicates the location at which the balloon's lower left corner is aligned. Initially <code>null</code>. */
    protected Offset offset;
    /** Indicates the padding between the balloon's content and its frame, in pixels. Initially <code>null</code>. */
    protected Insets insets;
    /** Indicates the shape of the balloon's frame. Initially <code>null</code>. */
    protected String balloonShape;
    /** Indicates the shape of the balloon's leader. Initially <code>null</code>. */
    protected String leaderShape;
    /** Indicates the width of the balloon's leader, in pixels. Initially 0. */
    protected int leaderWidth;
    /** Indicates the radius of each rounded corner on the balloon's rectangular frame, in pixels. Initially 0. */
    protected int cornerRadius;
    /** Indicates the font used to display the balloon's text. Initially <code>null</code>. */
    protected Font font;
    /** Indicates the color used to display the balloon's text. Initially <code>null</code>. */
    protected Color textColor;
    /** Indicates the location of the image source in pixels. Initially <code>null</code>. */
    protected Point imageOffset;
    /** Indicates the balloon texture's opacity as a floating-point value from 0.0 to 1.0. Initially 0.0. */
    protected double imageOpacity;
    /** Specifies the balloon texture's horizontal and vertical repeat mode. Initially <code>null</code>. */
    protected String imageRepeat;

    /**
     * Creates a new <code>BasicBalloonAttributes</code> with the default attributes. The default attributes are as
     * follows:
     * <p/>
     * <table> <tr><th>Attribute</th><th>Default Value</th></tr> <tr><td>unresolved</td><td><code>true</code></td></tr>
     * <tr><td>drawInterior</td><td><code>true</code></td></tr> <tr><td>drawOutline</td><td><code>true</code></td></tr>
     * <tr><td>enableAntialiasing</td><td><code>true</code></td></tr> <tr><td>enableLighting</td><td><code>false</code></td></tr>
     * <tr><td>interiorMaterial</td><td>{@link gov.nasa.worldwind.render.Material#WHITE}</td></tr>
     * <tr><td>outlineMaterial</td><td><code>171, 171, 171 (red, green, blue)</code></td></tr>
     * <tr><td>interiorOpacity</td><td>1.0</td></tr> <tr><td>outlineOpacity</td><td>1.0</td></tr>
     * <tr><td>outlineWidth</td><td>1.0</td></tr> <tr><td>outlineStippleFactor</td><td>0</td></tr>
     * <tr><td>outlineStipplePattern</td><td>0xF0F0</td></tr> <tr><td>interiorImageSource</td><td><code>null</code></td></tr>
     * <tr><td>interiorImageScale</td><td>1.0</td></tr> <tr><td>size</td><td>350x350 pixels (width x height)</td></tr>
     * <tr><td>maximumSize</td><td><code>null</code></td></tr> <tr><td>offset</td><td>40,60 pixels (x,
     * y)</code></td></tr> <tr><td>insets</td><td>30,15,15,15 (top, left, bottom, right)</td></tr>
     * <tr><td>balloonShape</td><td>{@link AVKey#SHAPE_RECTANGLE}</td></tr> <tr><td>leaderShape</td><td>{@link
     * AVKey#SHAPE_TRIANGLE}</td></tr> <tr><td>leaderWidth</td><td>40.0</td></tr> <tr><td>cornerRadius</td><td>20.0</td></tr>
     * <tr><td>font</td><td>Arial Plain 12</td></tr> <tr><td>textColor</td><td>{@link java.awt.Color#BLACK}</td></tr>
     * <tr><td>imageOffset</td><td>0,0 (x, y)</code></td></tr> <tr><td>imageOpacity</td><td>1</td></tr>
     * <tr><td>imageRepeat</td><td>{@link gov.nasa.worldwind.avlist.AVKey#REPEAT_XY}</td></tr> </table>
     */
    public BasicBalloonAttributes()
    {
        // Note: update the above constructor comment if these defaults change.

        // Common shape attributes.
        this.setOutlineMaterial(new Material(new Color(171, 171, 171)));

        // Balloon-specific attributes.
        this.setSize(Size.fromPixels(350, 350));
        this.setOffset(new Offset(0.45, -60.0, AVKey.FRACTION, AVKey.PIXELS));
        this.setInsets(new Insets(30, 15, 15, 15));
        this.setBalloonShape(AVKey.SHAPE_RECTANGLE);
        this.setLeaderShape(AVKey.SHAPE_TRIANGLE);
        this.setLeaderWidth(40);
        this.setCornerRadius(20);
        this.setFont(Font.decode("Arial-PLAIN-12"));
        this.setTextColor(Color.BLACK);
        this.setImageOffset(new Point(0, 0));
        this.setImageOpacity(1);
        this.setImageRepeat(AVKey.REPEAT_XY);
    }

    /**
     * Creates a new <code>BasicBalloonAttributes</code> configured with the specified <code>attributes</code>.
     *
     * @param attributes the attributes to configure the new <code>BasicBalloonAttributes</code> with.
     *
     * @throws IllegalArgumentException if <code>attributes</code> is <code>null</code>.
     */
    public BasicBalloonAttributes(BalloonAttributes attributes)
    {
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Copies both common attributes inherited from ShapeAttributes and balloon-specific attributes.
        this.copy(attributes);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overrides the superclass' behavior to return a new <code>BasicBalloonAttributes</code>.
     */
    public ShapeAttributes copy()
    {
        return new BasicBalloonAttributes(this);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Extends the superclass' behavior to copy <code>BalloonAttributes</code> if the specified <code>attributes</code>
     * is an instance of <code>BalloonAttributes</code>.
     */
    public void copy(ShapeAttributes attributes)
    {
        // Copy the common attributes inherited from ShapeAttributes.
        super.copy(attributes);

        // Copy the balloon-specific attributes.
        if (attributes instanceof BalloonAttributes)
        {
            BalloonAttributes balloonAttrs = (BalloonAttributes) attributes;
            this.size = balloonAttrs.getSize();
            this.maxSize = balloonAttrs.getMaximumSize();
            this.offset = balloonAttrs.getOffset();
            this.insets = balloonAttrs.getInsets();
            this.balloonShape = balloonAttrs.getBalloonShape();
            this.leaderShape = balloonAttrs.getLeaderShape();
            this.leaderWidth = balloonAttrs.getLeaderWidth();
            this.cornerRadius = balloonAttrs.getCornerRadius();
            this.font = balloonAttrs.getFont();
            this.textColor = balloonAttrs.getTextColor();
            this.imageOffset = balloonAttrs.getImageOffset();
            this.imageOpacity = balloonAttrs.getImageOpacity();
            this.imageRepeat = balloonAttrs.getImageRepeat();
        }
    }

    /** {@inheritDoc} */
    public String getBalloonShape()
    {
        return this.balloonShape;
    }

    /** {@inheritDoc} */
    public void setBalloonShape(String shape)
    {
        if (shape == null)
        {
            String message = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.balloonShape = shape;
    }

    /** {@inheritDoc} */
    public Size getSize()
    {
        return this.size;
    }

    /** {@inheritDoc} */
    public void setSize(Size size)
    {
        if (size == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.size = size;
    }

    /** {@inheritDoc} */
    public Size getMaximumSize()
    {
        return this.maxSize;
    }

    /** {@inheritDoc} */
    public void setMaximumSize(Size maxSize)
    {
        this.maxSize = maxSize;
    }

    /** {@inheritDoc} */
    public String getLeaderShape()
    {
        return this.leaderShape;
    }

    /** {@inheritDoc} */
    public void setLeaderShape(String shape)
    {
        if (shape == null)
        {
            String message = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.leaderShape = shape;
    }

    /** {@inheritDoc} */
    public int getLeaderWidth()
    {
        return this.leaderWidth;
    }

    /** {@inheritDoc} */
    public void setLeaderWidth(int width)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.leaderWidth = width;
    }

    /** {@inheritDoc} */
    public int getCornerRadius()
    {
        return this.cornerRadius;
    }

    /** {@inheritDoc} */
    public void setCornerRadius(int radius)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative", radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.cornerRadius = radius;
    }

    /** {@inheritDoc} */
    public Offset getOffset()
    {
        return this.offset;
    }

    /** {@inheritDoc} */
    public void setOffset(Offset offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.OffsetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.offset = offset;
    }

    /** {@inheritDoc} */
    public Insets getInsets()
    {
        return this.insets;
    }

    /** {@inheritDoc} */
    public void setInsets(Insets insets)
    {
        if (insets == null)
        {
            String message = Logging.getMessage("nullValue.InsetsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.insets = insets;
    }

    /** {@inheritDoc} */
    public Font getFont()
    {
        return this.font;
    }

    /** {@inheritDoc} */
    public void setFont(Font font)
    {
        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.font = font;
    }

    /** {@inheritDoc} */
    public Color getTextColor()
    {
        return this.textColor;
    }

    /** {@inheritDoc} */
    public void setTextColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.textColor = color;
    }

    /** {@inheritDoc} */
    public Point getImageOffset()
    {
        return this.imageOffset;
    }

    /** {@inheritDoc} */
    public void setImageOffset(Point offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.OffsetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageOffset = offset;
    }

    /** {@inheritDoc} */
    public double getImageOpacity()
    {
        return this.imageOpacity;
    }

    /** {@inheritDoc} */
    public void setImageOpacity(double opacity)
    {
        if (opacity < 0 || opacity > 1)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageOpacity = opacity;
    }

    /** {@inheritDoc} */
    public String getImageRepeat()
    {
        return this.imageRepeat;
    }

    /** {@inheritDoc} */
    public void setImageRepeat(String repeat)
    {
        if (repeat == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageRepeat = repeat;
    }

    /** {@inheritDoc} */
    public void getRestorableState(RestorableSupport restorableSupport, RestorableSupport.StateObject context)
    {
        if (restorableSupport == null)
        {
            String message = Logging.getMessage("nullValue.RestorableSupportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.getRestorableState(restorableSupport, context);

        RestorableSupport.StateObject so = restorableSupport.addStateObject(context, "size");
        if (so != null)
            this.getSize().getRestorableState(restorableSupport, so);

        Size maxSize = this.getMaximumSize();
        if (maxSize != null)
        {
            so = restorableSupport.addStateObject(context, "maxSize");
            if (so != null)
                this.getMaximumSize().getRestorableState(restorableSupport, so);
        }

        so = restorableSupport.addStateObject(context, "drawOffset");
        if (so != null)
            this.getOffset().getRestorableState(restorableSupport, so);

        so = restorableSupport.addStateObject(context, "insets");
        if (so != null)
        {
            restorableSupport.addStateValueAsInteger(so, "top", this.getInsets().top);
            restorableSupport.addStateValueAsInteger(so, "left", this.getInsets().left);
            restorableSupport.addStateValueAsInteger(so, "bottom", this.getInsets().bottom);
            restorableSupport.addStateValueAsInteger(so, "right", this.getInsets().right);
        }

        restorableSupport.addStateValueAsString(context, "balloonShape", this.getBalloonShape());

        restorableSupport.addStateValueAsString(context, "leader", this.getLeaderShape());
        restorableSupport.addStateValueAsInteger(context, "leaderGapWidth", this.getLeaderWidth());
        restorableSupport.addStateValueAsInteger(context, "cornerRadius", this.getCornerRadius());

        // Save the name, style, and size of the font. These will be used to restore the font using the
        // constructor: new Font(name, style, size).
        so = restorableSupport.addStateObject(context, "font");
        if (so != null)
        {
            restorableSupport.addStateValueAsString(so, "name", this.getFont().getName());
            restorableSupport.addStateValueAsInteger(so, "style", this.getFont().getStyle());
            restorableSupport.addStateValueAsInteger(so, "size", this.getFont().getSize());
        }

        String encodedColor = RestorableSupport.encodeColor(this.getTextColor());
        if (encodedColor != null)
            restorableSupport.addStateValueAsString(context, "textColor", encodedColor);

        so = restorableSupport.addStateObject(context, "imageOffset");
        if (so != null)
        {
            restorableSupport.addStateValueAsDouble(so, "x", this.getImageOffset().getX());
            restorableSupport.addStateValueAsDouble(so, "y", this.getImageOffset().getY());
        }

        restorableSupport.addStateValueAsDouble(context, "imageOpacity", this.getImageOpacity());
        restorableSupport.addStateValueAsString(context, "imageRepeat", this.getImageRepeat());
    }

    /** {@inheritDoc} */
    public void restoreState(RestorableSupport restorableSupport, RestorableSupport.StateObject context)
    {
        if (restorableSupport == null)
        {
            String message = Logging.getMessage("nullValue.RestorableSupportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.restoreState(restorableSupport, context);

        // Map legacy restorable state values using FrameFactory constants to the new AVKey constants.
        Map<String, String> legacySupport = new HashMap<String, String>();
        legacySupport.put("Render.FrameFactory.ShapeRectangle", AVKey.SHAPE_RECTANGLE);
        legacySupport.put("Render.FrameFactory.ShapeEllipse", AVKey.SHAPE_ELLIPSE);
        legacySupport.put("Render.FrameFactory.ShapeNone", AVKey.SHAPE_NONE);
        legacySupport.put("Render.FrameFactory.LeaderTriangle", AVKey.SHAPE_TRIANGLE);
        legacySupport.put("Render.FrameFactory.LeaderNone", AVKey.SHAPE_NONE);

        RestorableSupport.StateObject so = restorableSupport.getStateObject(context, "size");
        if (so != null)
            this.getSize().restoreState(restorableSupport, so);

        so = restorableSupport.getStateObject(context, "maxSize");
        if (so != null)
        {
            Size maxSize = new Size();
            maxSize.restoreState(restorableSupport, so);
            this.setMaximumSize(maxSize);
        }

        so = restorableSupport.getStateObject(context, "drawOffset");
        if (so != null)
            this.getOffset().restoreState(restorableSupport, so);

        // Restore the insets property only if all parts are available.
        // We will not restore a partial insets (for example, just the top value).
        so = restorableSupport.getStateObject(context, "insets");
        if (so != null)
        {
            Integer topState = restorableSupport.getStateValueAsInteger(so, "top");
            Integer leftState = restorableSupport.getStateValueAsInteger(so, "left");
            Integer bottomState = restorableSupport.getStateValueAsInteger(so, "bottom");
            Integer rightState = restorableSupport.getStateValueAsInteger(so, "right");
            if (topState != null && leftState != null && bottomState != null && rightState != null)
                this.setInsets(new Insets(topState, leftState, bottomState, rightState));
        }

        String s = restorableSupport.getStateValueAsString(context, "balloonShape");
        if (s != null)
        {
            // Map legacy versions using the FrameFactory constants to new AVKey constants.
            String updatedValue = legacySupport.get(s);
            if (updatedValue != null)
                s = updatedValue;

            this.setBalloonShape(s);
        }

        s = restorableSupport.getStateValueAsString(context, "leader");
        if (s != null)
        {
            // Map legacy versions using the FrameFactory constants to new AVKey constants.
            String updatedValue = legacySupport.get(s);
            if (updatedValue != null)
                s = updatedValue;

            this.setLeaderShape(s);
        }

        Integer i = restorableSupport.getStateValueAsInteger(context, "leaderGapWidth");
        if (i != null)
            this.setLeaderWidth(i);

        i = restorableSupport.getStateValueAsInteger(context, "cornerRadius");
        if (i != null)
            this.setCornerRadius(i);

        // Restore the font property only if all parts are available.
        // We will not restore a partial font (for example, just the size).
        so = restorableSupport.getStateObject(context, "font");
        if (so != null)
        {
            // The "font name" of toolTipFont.
            String name = restorableSupport.getStateValueAsString(so, "name");
            // The style attributes.
            Integer style = restorableSupport.getStateValueAsInteger(so, "style");
            // The simple font size.
            Integer size = restorableSupport.getStateValueAsInteger(so, "size");
            if (name != null && style != null && size != null)
                this.setFont(new Font(name, style, size));
        }

        s = restorableSupport.getStateValueAsString(context, "textColor");
        if (s != null)
        {
            Color color = RestorableSupport.decodeColor(s);
            if (color != null)
                this.setTextColor(color);
        }

        // Restore the imageOffset property only if all parts are available.
        // We will not restore a partial imageOffset (for example, just the x value).
        so = restorableSupport.getStateObject(context, "imageOffset");
        if (so != null)
        {
            Double x = restorableSupport.getStateValueAsDouble(so, "x");
            Double y = restorableSupport.getStateValueAsDouble(so, "y");
            if (x != null && y != null)
                this.setImageOffset(new Point(x.intValue(), y.intValue()));
        }

        Double d = restorableSupport.getStateValueAsDouble(context, "imageOpacity");
        if (d != null)
            this.setImageOpacity(d);

        s = restorableSupport.getStateValueAsString(context, "imageRepeat");
        if (s != null)
            this.setImageRepeat(s);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        BasicBalloonAttributes that = (BasicBalloonAttributes) o;

        if (this.size != null ? !this.size.equals(that.size) : that.size != null)
            return false;
        if (this.maxSize != null ? !this.maxSize.equals(that.maxSize) : that.maxSize != null)
            return false;
        if (this.offset != null ? !this.offset.equals(that.offset) : that.offset != null)
            return false;
        if (this.insets != null ? !this.insets.equals(that.insets) : that.insets != null)
            return false;
        if (this.balloonShape != null ? !this.balloonShape.equals(that.balloonShape) : that.balloonShape != null)
            return false;
        if (this.leaderShape != null ? !this.leaderShape.equals(that.leaderShape) : that.leaderShape != null)
            return false;
        if (this.leaderWidth != that.leaderWidth)
            return false;
        if (this.cornerRadius != that.cornerRadius)
            return false;
        if (this.font != null ? !this.font.equals(that.font) : that.font != null)
            return false;
        if (this.textColor != null ? !this.textColor.equals(that.textColor) : that.textColor != null)
            return false;
        if (this.imageOffset != null ? !this.imageOffset.equals(that.imageOffset) : that.imageOffset != null)
            return false;
        if (Double.compare(this.imageOpacity, that.imageOpacity) != 0)
            return false;
        //noinspection RedundantIfStatement
        if (this.imageRepeat != null ? !this.imageRepeat.equals(that.imageRepeat) : that.imageRepeat != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();

        result = 31 * result + (this.size != null ? this.size.hashCode() : 0);
        result = 31 * result + (this.maxSize != null ? this.maxSize.hashCode() : 0);
        result = 31 * result + (this.offset != null ? this.offset.hashCode() : 0);
        result = 31 * result + (this.insets != null ? this.insets.hashCode() : 0);
        result = 31 * result + (this.balloonShape != null ? this.balloonShape.hashCode() : 0);
        result = 31 * result + (this.leaderShape != null ? this.leaderShape.hashCode() : 0);
        result = 31 * result + this.leaderWidth;
        result = 31 * result + this.cornerRadius;
        result = 31 * result + (this.font != null ? this.font.hashCode() : 0);
        result = 31 * result + (this.textColor != null ? this.textColor.hashCode() : 0);
        result = 31 * result + (this.imageOffset != null ? this.imageOffset.hashCode() : 0);
        long temp = this.imageOpacity != +0.0d ? Double.doubleToLongBits(this.imageOpacity) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (this.imageRepeat != null ? this.imageRepeat.hashCode() : 0);

        return result;
    }
}

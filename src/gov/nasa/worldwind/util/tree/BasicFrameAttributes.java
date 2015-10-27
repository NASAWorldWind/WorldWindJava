/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * Basic implementation of {@link FrameAttributes} set.
 *
 * @author pabercrombie
 * @version $Id: BasicFrameAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicFrameAttributes implements FrameAttributes
{
    protected double backgroundOpacity;
    protected Color frameColor1;
    protected Color frameColor2;

    protected Color titleBarColor1;
    protected Color titleBarColor2;

    protected Color scrollBarColor1;
    protected Color scrollBarColor2;

    protected Color minimizeButtonColor;

    protected double foregroundOpacity;

    protected Color foregroundColor;

    protected Color textColor;
    protected Font font;

    protected Dimension iconSize;
    protected int iconSpace;

    protected int cornerRadius;

    public BasicFrameAttributes()
    {
        this.backgroundOpacity = 0.8;
        this.frameColor1 = Color.WHITE;
        this.frameColor2 = new Color(0xC8D2DE);

        this.titleBarColor1 = new Color(29, 78, 169);
        this.titleBarColor2 = new Color(93, 158, 223);

        this.scrollBarColor1 = new Color(29, 78, 169);
        this.scrollBarColor2 = new Color(93, 158, 223);

        this.minimizeButtonColor = new Color(0xEB9BA4);
        this.foregroundOpacity = 1.0;
        this.foregroundColor = Color.BLACK;
        this.font = Font.decode("Arial-BOLD-14");
        this.textColor = Color.WHITE;

        this.iconSize = new Dimension(16, 16);
        this.iconSpace = 5;

        this.cornerRadius = 5;
    }

    /**
     * Create a new attributes object with the same configuration as an existing attributes object.
     *
     * @param attributes Object to copy configuration from.
     */
    public BasicFrameAttributes(BasicFrameAttributes attributes)
    {
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.copy(attributes);
    }

    /** {@inheritDoc} */
    public Color getForegroundColor()
    {
        return this.foregroundColor;
    }

    /** {@inheritDoc} */
    public void setForegroundColor(Color textColor)
    {
        if (textColor == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.foregroundColor = textColor;
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
            String msg = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.font = font;
    }

    /** {@inheritDoc} */
    public Color getTextColor()
    {
        return textColor;
    }

    /** {@inheritDoc} */
    public void setTextColor(Color textColor)
    {
        if (textColor == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.textColor = textColor;
    }

    /** {@inheritDoc} */
    public Dimension getIconSize()
    {
        return this.iconSize;
    }

    /** {@inheritDoc} */
    public void setIconSize(Dimension iconSize)
    {
        if (iconSize == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.iconSize = iconSize;
    }

    /** {@inheritDoc} */
    public int getIconSpace()
    {
        return this.iconSpace;
    }

    /** {@inheritDoc} */
    public void setIconSpace(int iconSpace)
    {
        this.iconSpace = iconSpace;
    }

    /** {@inheritDoc} */
    public double getForegroundOpacity()
    {
        return foregroundOpacity;
    }

    /** {@inheritDoc} */
    public void setForegroundOpacity(double textOpacity)
    {
        this.foregroundOpacity = textOpacity;
    }

    /** {@inheritDoc} */
    public double getBackgroundOpacity()
    {
        return this.backgroundOpacity;
    }

    /** {@inheritDoc} */
    public void setBackgroundOpacity(double frameOpacity)
    {
        this.backgroundOpacity = frameOpacity;
    }

    /** {@inheritDoc} */
    public Color[] getBackgroundColor()
    {
        return new Color[] {this.frameColor1, this.frameColor2};
    }

    /** {@inheritDoc} */
    public void setTitleBarColor(Color color1, Color color2)
    {
        if (color1 == null || color2 == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.titleBarColor1 = color1;
        this.titleBarColor2 = color2;
    }

    /** {@inheritDoc} */
    public Color[] getTitleBarColor()
    {
        return new Color[] {this.titleBarColor1, this.titleBarColor2};
    }

    /** {@inheritDoc} */
    public Color[] getScrollBarColor()
    {
        return new Color[] {this.scrollBarColor1, this.scrollBarColor2};
    }

    /** {@inheritDoc} */
    public void setScrollBarColor(Color color1, Color color2)
    {
        if (color1 == null || color2 == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.scrollBarColor1 = color1;
        this.scrollBarColor2 = color2;
    }

    /** {@inheritDoc} */
    public Color getMinimizeButtonColor()
    {
        return minimizeButtonColor;
    }

    /** {@inheritDoc} */
    public void setMinimizeButtonColor(Color minimizeButtonColor)
    {
        if (minimizeButtonColor == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.minimizeButtonColor = minimizeButtonColor;
    }

    /** {@inheritDoc} */
    public void setBackgroundColor(Color frameColor1, Color frameColor2)
    {
        if (frameColor1 == null || frameColor2 == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.frameColor1 = frameColor1;
        this.frameColor2 = frameColor2;
    }

    /** {@inheritDoc} */
    public int getCornerRadius()
    {
        return this.cornerRadius;
    }

    /** {@inheritDoc} */
    public void setCornerRadius(int cornerRadius)
    {
        this.cornerRadius = cornerRadius;
    }

    /** {@inheritDoc} */
    public BasicFrameAttributes copy()
    {
        return new BasicFrameAttributes(this);
    }

    /** {@inheritDoc} */
    public void copy(FrameAttributes attributes)
    {
        if (attributes != null)
        {
            this.backgroundOpacity = attributes.getBackgroundOpacity();
            Color[] colorArray = attributes.getBackgroundColor();
            this.frameColor1 = colorArray[0];
            this.frameColor2 = colorArray[1];

            colorArray = attributes.getTitleBarColor();
            this.titleBarColor1 = colorArray[0];
            this.titleBarColor2 = colorArray[1];

            colorArray = attributes.getTitleBarColor();
            this.scrollBarColor1 = colorArray[0];
            this.scrollBarColor2 = colorArray[1];

            this.minimizeButtonColor = attributes.getMinimizeButtonColor();
            this.foregroundOpacity = attributes.getForegroundOpacity();
            this.foregroundColor = attributes.getForegroundColor();
            this.font = attributes.getFont();
            this.textColor = attributes.getTextColor();

            this.iconSize = attributes.getIconSize();
            this.iconSpace = attributes.getIconSpace();

            this.cornerRadius = attributes.getCornerRadius();
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        BasicFrameAttributes that = (BasicFrameAttributes) o;

        if (this.backgroundOpacity != that.backgroundOpacity)
            return false;
        if (this.frameColor1 != null ? !this.frameColor1.equals(that.frameColor1) : that.frameColor1 != null)
            return false;
        if (this.frameColor2 != null ? !this.frameColor2.equals(that.frameColor2) : that.frameColor2 != null)
            return false;
        if (this.titleBarColor1 != null ? !this.titleBarColor1.equals(that.titleBarColor1)
            : that.titleBarColor1 != null)
            return false;
        if (this.titleBarColor2 != null ? !this.titleBarColor2.equals(that.titleBarColor2)
            : that.titleBarColor2 != null)
            return false;
        if (this.scrollBarColor1 != null ? !this.scrollBarColor1.equals(that.scrollBarColor1)
            : that.scrollBarColor1 != null)
            return false;
        if (this.scrollBarColor2 != null ? !this.scrollBarColor2.equals(that.scrollBarColor2)
            : that.scrollBarColor2 != null)
            return false;
        if (this.minimizeButtonColor != null ? !this.minimizeButtonColor.equals(that.minimizeButtonColor)
            : that.minimizeButtonColor != null)
            return false;
        if (this.foregroundOpacity != that.foregroundOpacity)
            return false;
        if (this.foregroundColor != null ? !this.foregroundColor.equals(that.foregroundColor)
            : that.foregroundColor != null)
            return false;
        if (this.font != null ? !this.font.equals(that.font) : that.font != null)
            return false;
        if (this.textColor != null ? !this.textColor.equals(that.textColor) : that.textColor != null)
            return false;
        if (this.iconSpace != that.iconSpace)
            return false;
        if (this.iconSize != null ? !this.iconSize.equals(that.iconSize) : that.iconSize != null)
            return false;
        //noinspection RedundantIfStatement
        if (this.cornerRadius != that.cornerRadius)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = 12; // Arbitrary non-zero constant
        long temp;

        temp = this.backgroundOpacity != +0.0d ? Double.doubleToLongBits(this.backgroundOpacity) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (this.frameColor1 != null ? this.frameColor1.hashCode() : 0);
        result = 31 * result + (this.frameColor2 != null ? this.frameColor2.hashCode() : 0);
        result = 31 * result + (this.titleBarColor1 != null ? this.titleBarColor1.hashCode() : 0);
        result = 31 * result + (this.titleBarColor2 != null ? this.titleBarColor2.hashCode() : 0);
        result = 31 * result + (this.scrollBarColor1 != null ? this.scrollBarColor1.hashCode() : 0);
        result = 31 * result + (this.scrollBarColor2 != null ? this.scrollBarColor2.hashCode() : 0);
        result = 31 * result + (this.minimizeButtonColor != null ? this.minimizeButtonColor.hashCode() : 0);
        temp = this.foregroundOpacity != +0.0d ? Double.doubleToLongBits(this.foregroundOpacity) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (this.foregroundColor != null ? this.foregroundColor.hashCode() : 0);
        result = 31 * result + (this.font != null ? this.font.hashCode() : 0);
        result = 31 * result + (this.textColor != null ? this.textColor.hashCode() : 0);
        result = 31 * result + this.iconSpace;
        result = 31 * result + (this.iconSize != null ? this.iconSize.hashCode() : 0);
        result = 31 * result + this.cornerRadius;

        return result;
    }
}

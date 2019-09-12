/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * Basic implementation of {@link TreeAttributes} set.
 *
 * @author pabercrombie
 * @version $Id: BasicTreeAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicTreeAttributes implements TreeAttributes
{
    protected boolean rootVisible;

    protected Color textColor;
    protected Font font;
    protected Font descriptionFont;
    protected int rowSpacing; // Spacing between rows in the tree

    protected Color checkBoxColor1;
    protected Color checkBoxColor2;

    protected Dimension iconSize;
    protected int iconSpace;

    public BasicTreeAttributes()
    {
        this.rootVisible = true;
        this.textColor = Color.BLACK;
        this.font = Font.decode("Arial-BOLD-14");
        this.descriptionFont = Font.decode("Arial-12");
        this.rowSpacing = 8; // Spacing between rows in the tree

        this.checkBoxColor1 = new Color(29, 78, 169);
        this.checkBoxColor2 = new Color(93, 158, 223);

        this.iconSize = new Dimension(16, 16);
        this.iconSpace = 5;
    }

    /**
     * Create a new attributes object with the same configuration as an existing attributes object.
     *
     * @param attributes Object to copy configuration from.
     */
    public BasicTreeAttributes(TreeAttributes attributes)
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
    public boolean isRootVisible()
    {
        return this.rootVisible;
    }

    /** {@inheritDoc} */
    public void setRootVisible(boolean visible)
    {
        this.rootVisible = visible;
    }

    /** {@inheritDoc} */
    public Color getColor()
    {
        return this.textColor;
    }

    /** {@inheritDoc} */
    public void setColor(Color textColor)
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
    public Font getFont()
    {
        return this.font;
    }

    /** {@inheritDoc} */
    public Font getDescriptionFont()
    {
        return this.descriptionFont;
    }

    /** {@inheritDoc} */
    public void setDescriptionFont(Font font)
    {
        if (font == null)
        {
            String msg = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.descriptionFont = font;
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
    public int getRowSpacing()
    {
        return this.rowSpacing;
    }

    /** {@inheritDoc} */
    public void setRowSpacing(int spacing)
    {
        this.rowSpacing = spacing;
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
    public Color[] getCheckBoxColor()
    {
        return new Color[] {this.checkBoxColor1, this.checkBoxColor2};
    }

    /** {@inheritDoc} */
    public void setCheckBoxColor(Color color1, Color color2)
    {
        if (color1 == null || color2 == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.checkBoxColor1 = color1;
        this.checkBoxColor2 = color2;
    }

    /** {@inheritDoc} */
    public BasicTreeAttributes copy()
    {
        return new BasicTreeAttributes(this);
    }

    /** {@inheritDoc} */
    public void copy(TreeAttributes attributes)
    {
        if (attributes != null)
        {
            this.rootVisible = attributes.isRootVisible();
            this.textColor = attributes.getColor();
            this.font = attributes.getFont();
            this.descriptionFont = attributes.getDescriptionFont();
            this.rowSpacing = attributes.getRowSpacing();
            Color[] colors = attributes.getCheckBoxColor();
            this.checkBoxColor1 = colors[0];
            this.checkBoxColor2 = colors[1];
            this.iconSize = attributes.getIconSize();
            this.iconSpace = attributes.getIconSpace();
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        BasicTreeAttributes that = (BasicTreeAttributes) o;

        if (this.rootVisible != that.rootVisible)
            return false;
        if (this.textColor != null ? !this.textColor.equals(that.textColor) : that.textColor != null)
            return false;
        if (this.font != null ? !this.font.equals(that.font) : that.font != null)
            return false;
        if (this.descriptionFont != null ? !this.descriptionFont.equals(that.descriptionFont)
            : that.descriptionFont != null)
            return false;
        if (this.rowSpacing != that.rowSpacing)
            return false;
        if (!this.iconSize.equals(that.iconSize))
            return false;
        if (this.checkBoxColor1 != null ? !this.checkBoxColor1.equals(that.checkBoxColor1)
            : that.checkBoxColor1 != null)
            return false;
        if (this.checkBoxColor2 != null ? !this.checkBoxColor2.equals(that.checkBoxColor2)
            : that.checkBoxColor2 != null)
            return false;
        //noinspection RedundantIfStatement
        if (this.iconSpace != that.iconSpace)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;

        result = (this.rootVisible ? 1 : 0);
        result = 31 * result + (this.textColor != null ? this.textColor.hashCode() : 0);
        result = 31 * result + (this.font != null ? this.font.hashCode() : 0);
        result = 31 * result + (this.descriptionFont != null ? this.descriptionFont.hashCode() : 0);
        result = 31 * result + this.rowSpacing;
        result = 31 * result + (this.iconSize != null ? this.iconSize.hashCode() : 0);
        result = 31 * result + this.iconSpace;

        return result;
    }
}

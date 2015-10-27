/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: UserFacingText.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class UserFacingText implements GeographicText
{
    private CharSequence text;
    private Position textPosition;
    private Font textFont; // Can be null to indicate the default font.
    private Color textColor; // Can be null to indicate the default text color.
    private Color textBackgroundColor; // Can be null to indicate no background color.
    private boolean isVisible = true;
    double priority;  //used for label culling

    public UserFacingText(CharSequence text, Position textPosition)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.CharSequenceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.text = text;
        this.textPosition = textPosition;
    }

    public CharSequence getText()
    {
        return this.text;
    }

    public void setText(CharSequence text)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.CharSequenceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.text = text;
    }

    public double getPriority()
    {
        return priority;
    }

    public void setPriority(double priority)
    {
        this.priority = priority;
    }

    public Position getPosition()
    {
        return this.textPosition;
    }

    public void setPosition(Position position)
    {
        this.textPosition = position;
    }

    public Font getFont()
    {
        return this.textFont;
    }

    public void setFont(Font font)
    {
        this.textFont = font;
    }

    public Color getColor()
    {
        return this.textColor;
    }

    public void setColor(Color color)
    {
        this.textColor = color;
    }

    public Color getBackgroundColor()
    {
        return this.textBackgroundColor;
    }

    public void setBackgroundColor(Color background)
    {
        this.textBackgroundColor = background;
    }

    public boolean isVisible()
    {
        return this.isVisible;
    }

    public void setVisible(boolean visible)
    {
        this.isVisible = visible;
    }
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;

import java.awt.*;

/**
 * A piece of text that is drawn at a geographic location.
 *
 * @author dcollins
 * @version $Id: GeographicText.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface GeographicText
{
    /**
     * Indicates the text contained in this object.
     *
     * @return The current text.
     */
    CharSequence getText();

    /**
     * Specifies the text.
     *
     * @param text New text.
     */
    void setText(CharSequence text);

    /**
     * Indicates the geographic position of the text.
     *
     * @return The text position.
     */
    Position getPosition();

    /**
     * Specifies the geographic position of the text.
     *
     * @param position New text position.
     */
    void setPosition(Position position);

    /**
     * Indicates the font used to draw the text.
     *
     * @return Current font.
     */
    Font getFont();

    /**
     * Specifies the font used to draw the text.
     *
     * @param font New font.
     */
    void setFont(Font font);

    /**
     * Indicates the color used to draw the text.
     *
     * @return Current text color.
     */
    Color getColor();

    /**
     * Specifies the color used to draw the text.
     *
     * @param color New color.
     */
    void setColor(Color color);

    /**
     * Indicates the background color used to draw the text.
     *
     * @return Current background color.
     */
    Color getBackgroundColor();

    /**
     * Specifies the background color used to draw the text.
     *
     * @param background New background color.
     */
    void setBackgroundColor(Color background);

    /**
     * Indicates whether or not the text is visible. The text will not be drawn when the visibility is set to {@code
     * false}.
     *
     * @return {@code true} if the text is visible, otherwise {@code false}.
     */
    boolean isVisible();

    /**
     * Specifies whether or not the text is visible. The text will not be drawn when the visibility is set to {@code
     * false}.
     *
     * @param visible {@code true} if the text should be visible. {@code false} if not.
     */
    void setVisible(boolean visible);

    /**
     * Indicates the text priority. The priority can be used to implement text culling.
     *
     * @return The text priority.
     */
    double getPriority();

    /**
     * Specifies the text priority. The priority can be used to implement text culling.
     *
     * @param d New priority.
     */
    void setPriority(double d);
}

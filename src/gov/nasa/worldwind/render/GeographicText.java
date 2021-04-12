/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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

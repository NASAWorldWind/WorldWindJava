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

package gov.nasa.worldwind.util.tree;

import java.awt.*;

/**
 * Attributes to control how a {@link Tree} is rendered. The class captures a set of attributes found in a typical tree
 * layout, but some layouts may not use all of these properties.
 *
 * @author pabercrombie
 * @version $Id: TreeAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see TreeLayout
 */
public interface TreeAttributes
{
    /**
     * Returns a new TreeAttributes instance of the same type as this TreeAttributes, who's properties are
     * configured exactly as this TreeAttributes.
     *
     * @return a copy of this TreeAttributes.
     */
    TreeAttributes copy();

    /**
     * Copies the specified TreeAttributes' properties into this object's properties. This does nothing if the
     * specified attributes is null.
     *
     * @param attributes the attributes to copy.
     */
    void copy(TreeAttributes attributes);

    /**
     * Should be root node be drawn?
     *
     * @return True if the root node should be drawn.
     *
     * @see #setRootVisible(boolean)
     */
    boolean isRootVisible();

    /**
     * Set the root node to visibile or not visible.
     *
     * @param visible True if the root node should be drawn.
     *
     * @see #isRootVisible()
     */
    void setRootVisible(boolean visible);

    /**
     * Get the color of the text in the tree.
     *
     * @return Text color.
     *
     * @see #setColor(java.awt.Color)
     */
    Color getColor();

    /**
     * Set the color of the text in the tree.
     *
     * @param textColor New text color.
     *
     * @see #getColor()
     */
    void setColor(Color textColor);

    /**
     * Get the color of filled checkboxes that indicate if nodes are selected. The checkboxes are drawn as a gradient
     * of two colors.
     *
     * @return two element array of the colors that make up the checkbox gradient.
     */
    Color[] getCheckBoxColor();

    /**
     * Set the color of filled checkboxes that indicate if a node is selected. The checkboxes are drawn as a gradient
     * of two colors.
     *
     * @param color1 first color in the checkbox gradient.
     * @param color2 second color in the checkbox gradient.
     */
    void setCheckBoxColor(Color color1, Color color2);

    /**
     * Get the font used to render text.
     *
     * @return Tree font.
     *
     * @see #setFont(java.awt.Font)
     */
    Font getFont();

    /**
     * Get the font used to render the node description.
     *
     * @return Font for node description.
     */
    Font getDescriptionFont();

    /**
     * Set the font used to render the node descriptions.
     *
     * @param font New font for descriptions.
     */
    void setDescriptionFont(Font font);

    /**
     * Set the font used to render text.
     *
     * @param font New tree font.
     *
     * @see #getFont()
     */
    void setFont(Font font);

    /**
     * Get the space, in pixels, to leave between rows in the tree.
     *
     * @return Space in pixels between rows.
     *
     * @see #setRowSpacing(int)
     */
    int getRowSpacing();

    /**
     * Set the space, in pixels, to leave between rows in the tree.
     *
     * @param spacing Row spacing.
     *
     * @see #getRowSpacing()
     */
    void setRowSpacing(int spacing);

    /**
     * Get the size of each icon in the tree. If the icon images do not match this size, they will be scaled to fit.
     *
     * @return Icon size.
     *
     * @see #setIconSize(java.awt.Dimension)
     */
    Dimension getIconSize();

    /**
     * Set the size of each icon in the tree.
     *
     * @param size New size.
     *
     * @see #getIconSize()
     */
    void setIconSize(Dimension size);

    /**
     * Get the amount of space, in pixels, to leave between an icon in the tree and surrounding text and shapes.
     *
     * @return Icon space in pixels.
     *
     * @see #setIconSpace(int)
     */
    int getIconSpace();

    /**
     * Set the amount of space, in pixels, to leave between an icon in the tree and surrounding text and shapes.
     *
     * @param iconSpace Icon space in pixels.
     *
     * @see #getIconSpace()
     */
    void setIconSpace(int iconSpace);
}

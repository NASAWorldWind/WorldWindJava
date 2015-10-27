/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.*;

import java.awt.*;

/**
 * Provides a general interface for icons rendered by World Wind. Icons have a source image and optionally a background
 * image. They may also have an associated tool tip. An icon has a geographic position. The indication of that position
 * is determined by implementations. The simplest implementation is to center the icon at the position, but association
 * by leader lines and other mechanisms are appropriate.
 *
 * @author tag
 * @version $Id: WWIcon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface WWIcon extends AVList, Restorable
{
    /**
     * Specifies the source image for the icon. Implementations of this interface determine the allowed source types,
     * but generally allow at least a {@link String} file path and a {@link java.awt.image.BufferedImage}.
     *
     * @param imageSource the image source.
     *
     * @throws IllegalArgumentException if <code>imageSource</code> is null or the source is not an allowed type.
     */
    void setImageSource(Object imageSource);

    /**
     * Returns the icon's image source.
     *
     * @return the icon's image source.
     */
    Object getImageSource();

    /**
     * Returns the icon's geographic position.
     *
     * @return the icon's geographic position.
     */
    Position getPosition();

    /**
     * Sets the icon's geographic position.
     *
     * @param iconPosition the icon's geographic position. May be null to indicate that the icon has no current position
     *                     and therefore should not be displayed.
     */
    void setPosition(Position iconPosition);

    /**
     * Indicates whether the icon should be drawn in its highlighted state. The representation of the highlighted state
     * is implementation dependent.
     *
     * @return true if the icon should be rendered as highlighted, otherwise false.
     */
    boolean isHighlighted();

    /**
     * Specifies whether the icon should be drawn in its highlighted state. The representation of the highlighted state
     * is implementation dependent.
     *
     * @param highlighted true if the icon should be rendered as highlighted, otherwise false.
     */
    void setHighlighted(boolean highlighted);

    /**
     * Returns the icon's specified screen size.
     *
     * @return the icon's specified screen size. The default value is false.
     *
     * @see #setSize(java.awt.Dimension)
     */
    Dimension getSize();

    /**
     * Sets the desired screen size of the icon. When rendered, the icon is scaled to this size if it's specified. If a
     * screen size is not specified, the icon is displayed at the size of its source image.
     * <p/>
     * This size is not related to the icon's image source size. Whatever the source size is, it's scaled to display at
     * the specified screen size, if any.
     *
     * @param size the desired screen size, or null if the icon should be displayed at its source image size.
     */
    void setSize(Dimension size);

    /**
     * Returns the state of the visibility flag.
     *
     * @return the visibility flag. The default value is true.
     */
    boolean isVisible();

    /**
     * Specifies whether the icon is drawn.
     *
     * @param visible true if the icon is drawn, otherwise false. The default is true.
     */
    void setVisible(boolean visible);

    /**
     * Returns the icon's highlight scale, which indicates the degree of expansion or shrinkage applied to the icon when
     * it's drawn in its highlighted state.
     *
     * @return the icon's highlight scale.
     */
    double getHighlightScale();

    /**
     * Specifies the relative screen size of the icon when it's highlighted. The scale indicates the degree of expansion
     * or shrinkage from the icon's base size.
     *
     * @param highlightScale the highlight scale.
     */
    void setHighlightScale(double highlightScale);

    /**
     * Returns the icon's current tool tip text.
     *
     * @return the current tool tip text.
     */
    String getToolTipText();

    /**
     * Specifies the text string to display as the icon's tool tip.
     *
     * @param toolTipText the tool tip text. May be null, the default, to indicate no tool tip is displaye.
     */
    void setToolTipText(String toolTipText);

    /**
     * Returns the font used to render an icon's tool tip, if any.
     *
     * @return the tool tip font. The default is null.
     */
    Font getToolTipFont();

    /**
     * Specifies the font to use when displaying the icon's tool tip, if any.
     *
     * @param toolTipFont the tool tip font. If null, an implementation dependent font is used.
     */
    void setToolTipFont(Font toolTipFont);

    /**
     * Indicates whether the icon's tool tip, if any, is displayed with the icon.
     *
     * @return true if the tool tip is displayed when the icon is rendered, otherwise false, the default.
     */
    boolean isShowToolTip();

    /**
     * Indicates whether the icon's tool tip, if any, is displayed with the icon.
     *
     * @param showToolTip true if the tool tip is displayed when the icon is rendered, otherwise false, the default.
     */
    void setShowToolTip(boolean showToolTip);

    /**
     * Indicates the color in which the icon's tool tip, if any, is drawn.
     *
     * @return the tool tip's text color. The default value is null, in which case an implementation dependent color is
     *         used.
     */
    Color getToolTipTextColor();

    /**
     * Specifies the color in which to display the icon's tool tip text, if any.
     *
     * @param textColor the tool tip text color. The default is null, in which case an implementation dependent color is
     *                  used.
     */
    void setToolTipTextColor(Color textColor);

    /**
     * Indicates whether the icon is always to be displayed "on top" of all other ordered renderables.
     *
     * @return true if the icon has visual priority, otherwise false, the default.
     */
    boolean isAlwaysOnTop();

    /**
     * Indicates whether the icon is always to be displayed "on top" of all other ordered renderables.
     *
     * @param alwaysOnTop true if the icon has visual priority, otherwise false, the default.
     */
    void setAlwaysOnTop(boolean alwaysOnTop);

    /**
     * Returns the icon's background image source, if any.
     *
     * @return the icon's background image source. The default is null.
     */
    Object getBackgroundImage();

    /**
     * Specifies the icon's background image source. If non-null, the image is displayed centered and behind the icon.
     *
     * @param background the background image source. The default is null.
     */
    void setBackgroundImage(Object background);

    /**
     * Indicates the relative screen size of the background image, if specified. The scale indicates the degree of
     * expansion or shrinkage from the image's source size.
     *
     * @return the background image scale.
     */
    double getBackgroundScale();

    /**
     * Indicates the relative screen size of the background image, if specified. The scale indicates the degree of
     * expansion or shrinkage from the image's source size.
     *
     * @param backgroundScale the background image scale.
     */
    void setBackgroundScale(double backgroundScale);

    /**
     * Indicates the offset in screen coordinates at which to place the lower left corner of the icon tool tip's text
     * box. If the offset is null, the tool tip is drawn at the icon's position.
     *
     * @return the tool tip offset. The default is null.
     */
    Vec4 getToolTipOffset();

    /**
     * Indicates the offset in screen coordinates at which to place the lower left corner of the icon tool tip's text
     * box. If the offset is null, the tool tip is drawn at the icon's position.
     *
     * @param toolTipOffset the tool tip offset. The default is null.
     */
    void setToolTipOffset(Vec4 toolTipOffset);

    /**
     * Returns the {@link WWTexture} used to represent the icon.
     *
     * @return the icon's texture.
     */
    BasicWWTexture getImageTexture();

    /**
     * Returns the {@link WWTexture} used to represent the icon's background texture.
     *
     * @return the icon's background texture.
     */
    BasicWWTexture getBackgroundTexture();
}

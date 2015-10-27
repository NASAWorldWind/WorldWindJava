/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import java.awt.*;

/**
 * Holds attributes for World Wind {@link gov.nasa.worldwind.render.Balloon} shapes. Changes made to the attributes are
 * applied to the balloon when the <code>WorldWindow</code> renders the next frame. Instances of
 * <code>BalloonAttributes</code> may be shared by many balloons, thereby reducing the memory normally required to store
 * attributes for each balloon.
 *
 * @author pabercrombie
 * @version $Id: BalloonAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see Balloon
 */
public interface BalloonAttributes extends ShapeAttributes
{
    /**
     * Indicates the width and height of the balloon's shape in the viewport. If the balloon's shape is
     * <code>AVKey.SHAPE_RECTANGLE</code>, then the returned <code>Size</code> indicates the rectangle's width and
     * height. If the balloon's shape is <code>AVKey.SHAPE_ELLIPSE</code>, then the returned <code>Size</code> indicates
     * the ellipse's x- and y-radii.
     *
     * @return the width and height of the balloon's shape in the viewport
     */
    public Size getSize();

    /**
     * Specifies the width and height of the balloon's shape in the viewport. If the balloon's shape is
     * <code>AVKey.SHAPE_RECTANGLE</code>, then <code>size</code> specifies the rectangle's width and height. If the
     * balloon's shape is <code>AVKey.SHAPE_ELLIPSE</code>, then <code>size</code> specifies the ellipse's x- and
     * y-radii.
     * <p/>
     * The balloon's content area is the rectangle obtained by taking the balloon's <code>size</code> and shrinking it
     * by the balloon's insets.
     *
     * @param size the desired width and height of the balloon's shape in the viewport.
     *
     * @throws IllegalArgumentException if <code>size</code> is <code>null</code>.
     * @see #getSize()
     * @see #getMaximumSize()
     * @see #setInsets(java.awt.Insets)
     */
    public void setSize(Size size);

    /**
     * Indicates the maximum width and height of the balloon's shape in the viewport. If the balloon's shape is
     * <code>AVKey.SHAPE_RECTANGLE</code>, then the returned <code>Size</code> indicates the rectangle's maximum width
     * and height. If the balloon's shape is <code>AVKey.SHAPE_ELLIPSE</code>, then the returned <code>Size</code>
     * indicates the ellipse's maximum x- and y-radii. This returns <code>null</code> if the balloon has no maximum
     * size.
     *
     * @return the maximum width and height of the balloon's shape in the viewport, or <code>null</code> if the balloon
     *         has no maximum size.
     */
    Size getMaximumSize();

    /**
     * Specifies the maximum width and height of the balloon's shape in the viewport. If the balloon's shape is
     * <code>AVKey.SHAPE_RECTANGLE</code>, then <code>size</code> specifies the rectangle's maximum width and height. If
     * the balloon's shape is <code>AVKey.SHAPE_ELLIPSE</code>, then <code>size</code> specifies the ellipse's maximum
     * x- and y-radii. Specifying a <code>null</code> size causes the balloon to have no maximum size.
     *
     * @param size the desired maximum width and height of the balloon's shape in the viewport, or <code>null</code> if
     *             the balloon should have no maximum size.
     */
    void setMaximumSize(Size size);

    /**
     * Indicates the location relative to the balloon's screen reference point at which the balloon's frame shape is
     * aligned. The balloon's lower left corner begins at the returned <code>offset</code>.
     *
     * @return the location at which the balloon's lower left corner is aligned.
     *
     * @see #setOffset(Offset)
     */
    public Offset getOffset();

    /**
     * Specifies a location relative to the balloon's screen reference point at which to align the balloon's frame
     * shape. The balloon's frame shape begins at the point indicated by the <code>offset</code>. An <code>offset</code>
     * of (0, 0) pixels causes the balloon's lower left corner to be placed at the screen reference point. An
     * <code>offset</code> of (1, 1) in fraction units causes the balloon's upper right corner to be placed at the
     * screen reference point.
     * <p/>
     * If the balloon is attached to the globe, the screen reference point is the projection of its geographic position
     * into the viewport. If the balloon is attached to the screen, the screen reference point is the balloon's screen
     * point.
     * <p/>
     * If the balloon has a leader shape, the leader extends from one side of the balloon's frame and points to the
     * screen reference point.
     *
     * @param offset a location at which to align the balloon's lower left corner.
     *
     * @throws IllegalArgumentException if <code>offset</code> is <code>null</code>.
     * @see #getOffset()
     */
    public void setOffset(Offset offset);

    /**
     * Indicates the amount of space between the balloon's content and its frame, in pixels. The balloon's content area
     * decreases to account for the returned space.
     *
     * @return the padding between the balloon's content and its frame, in pixels.
     *
     * @see #setInsets(java.awt.Insets)
     */
    public Insets getInsets();

    /**
     * Specifies the amount of space (in pixels) between the balloon's content and the edges of the balloon's frame. The
     * balloon's content area decreases to account for the specified <code>insets</code>. If the balloon's size and
     * insets cause the content width or height to become less than 1, then the balloon's content is not displayed.
     * <p/>
     * If the balloon's shape is <code>AVKey.SHAPE_RECTANGLE</code>, <code>insets</code> specifies the padding between
     * the balloon's content area and the rectangle's top, left, bottom, and right.
     * <p/>
     * If the balloon's shape is <code>AVKey.SHAPE_ELLIPSE</code>, <code>insets</code> specifies the padding between the
     * balloon's content area and the ellipse's top, left, bottom, and right apexes.
     *
     * @param insets the desired padding between the balloon's content and its frame, in pixels.
     *
     * @throws IllegalArgumentException if <code>insets</code> is <code>null</code>.
     * @see #getInsets()
     */
    public void setInsets(Insets insets);

    /**
     * Indicates the shape of the balloon's frame.
     *
     * @return the balloon frame's shape, either {@link gov.nasa.worldwind.avlist.AVKey#SHAPE_NONE}, {@link
     *         gov.nasa.worldwind.avlist.AVKey#SHAPE_RECTANGLE}, or {@link gov.nasa.worldwind.avlist.AVKey#SHAPE_ELLIPSE}.
     *
     * @see #setBalloonShape(String)
     */
    public String getBalloonShape();

    /**
     * Specifies the shape of the balloon's frame. The <code>shape</code> may be one of the following: <ul> <li>{@link
     * gov.nasa.worldwind.avlist.AVKey#SHAPE_NONE}</li> <li>{@link gov.nasa.worldwind.avlist.AVKey#SHAPE_RECTANGLE}</li>
     * <li>{@link gov.nasa.worldwind.avlist.AVKey#SHAPE_ELLIPSE}</li> </ul>
     * <p/>
     * If the <code>shape</code> is <code>AVKey.SHAPE_NONE</code>, the balloon's content is displayed in a rectangle in
     * the viewport without any decoration. The rectangle's dimension in the viewport are specified by calling {@link
     * #setSize(Size)}.
     * <p/>
     * If the <code>shape</code> is <code>AVKey.SHAPE_RECTANGLE</code>, the balloon is displayed as a rectangle in the
     * viewport with optionally rounded corners. The rectangle's dimension in the viewport are specified by calling
     * {@link #setSize(Size)}. The rectangle's corner radius in pixels is specified by calling {@link
     * #setCornerRadius(int)}.
     * <p/>
     * If the <code>shape</code> is <code>AVKey.SHAPE_ELLIPSE</code>, the balloon is displayed as an ellipse in the
     * viewport. The ellipse's x- and y-radii are specified by calling {@link #setSize(Size)}. The balloon's corner
     * radius attribute is ignored.
     *
     * @param shape the frame shape to use, either <code>AVKey.SHAPE_NONE</code> <code>AVKey.SHAPE_RECTANGLE</code> or
     *              <code>AVKey.SHAPE_ELLIPSE</code>.
     *
     * @throws IllegalArgumentException if <code>shape</code> is <code>null</code>.
     * @see #getBalloonShape()
     */
    public void setBalloonShape(String shape);

    /**
     * Indicates the shape of the balloon's leader.
     *
     * @return the balloon leader's shape, either {@link gov.nasa.worldwind.avlist.AVKey#SHAPE_NONE} or {@link
     *         gov.nasa.worldwind.avlist.AVKey#SHAPE_TRIANGLE}.
     *
     * @see #setLeaderShape(String)
     */
    public String getLeaderShape();

    /**
     * Specifies the shape of the balloon's leader. The <code>shape</code> may be one of the following: <ul> <li>{@link
     * gov.nasa.worldwind.avlist.AVKey#SHAPE_NONE}</li> <li>{@link gov.nasa.worldwind.avlist.AVKey#SHAPE_TRIANGLE}</li>
     * </ul>
     * <p/>
     * If the <code>shape</code> is <code>AVKey.SHAPE_NONE</code>, the leader is disabled and does not display.
     * <p/>
     * If the <code>shape</code> is <code>AVKey.SHAPE_TRIANGLE</code>, the leader extends from one side of the balloon's
     * frame and points to the balloon's screen reference point. The width of the leader (in pixels) where it intersects
     * the balloon's frame is specified by calling {@link #setLeaderWidth(int)}.
     *
     * @param shape the leader shape to use, either <code>AVKey.SHAPE_NONE</code> or <code>AVKey.SHAPE_TRIANGLE</code>.
     *
     * @throws IllegalArgumentException if <code>shape</code> is <code>null</code>.
     * @see #getLeaderShape()
     * @see #setLeaderWidth(int)
     */
    public void setLeaderShape(String shape);

    /**
     * Indicates the width of the balloon's leader, in pixels. The returned value is either zero or a positive integer.
     *
     * @return the width of the balloon's leader (in pixels) where it intersects the balloon's frame.
     *
     * @see #setLeaderWidth(int)
     */
    public int getLeaderWidth();

    /**
     * Specifies the width of the balloon's leader, in pixels. The specified <code>width</code> must be zero or a
     * positive integer. Specifying a <code>width</code> of zero disables the balloon's leader.
     * <p/>
     * This does nothing if the balloon's leader shape is <code>AVKey.SHAPE_NONE</code>.
     * <p/>
     * If the balloon's leader shape is <code>AVKey.SHAPE_TRIANGLE</code>, this specifies the size of the leader where
     * it intersects the balloon's frame.
     *
     * @param width the desired leader width, in pixels.
     *
     * @throws IllegalArgumentException if <code>width</code> is less than zero.
     * @see #getLeaderWidth()
     */
    public void setLeaderWidth(int width);

    /**
     * Indicates the radius of each rounded corner on the balloon's rectangular frame, in pixels. The returned value is
     * either zero or a positive integer. If the returned value is zero the balloon's rectangular frame has sharp
     * corners. This value is ignored if the balloon's shape is <code>AVKey.SHAPE_ELLIPSE</code>.
     *
     * @return the radius of the rounded corner's on the balloon's on the balloon's rectangular frame, in pixels.
     *
     * @see #setCornerRadius(int)
     */
    public int getCornerRadius();

    /**
     * Specifies the radius of the rounded corner's on the balloon's rectangular frame in pixels. The specified
     * <code>radius</code> must be zero or a positive integer. Specifying a <code>radius</code> of zero causes the
     * shape's rectangular frame to have sharp corners. This does nothing if the balloon's shape is
     * <code>AVKey.SHAPE_ELLIPSE</code>.
     *
     * @param radius the desired radius, in pixels.
     *
     * @throws IllegalArgumentException if <code>radius</code> is less than zero.
     * @see #getCornerRadius()
     */
    public void setCornerRadius(int radius);

    /**
     * Indicates the font used to display the balloon's text. This value may be ignored if the balloon's text contains
     * HTML.
     *
     * @return the balloon's text font.
     *
     * @see #setFont(java.awt.Font)
     */
    public Font getFont();

    /**
     * Specifies the font in which to display the balloon's text. The specified <code>font</code> may be ignored if the
     * balloon's text contains HTML.
     *
     * @param font the font to use for the balloon's text.
     *
     * @throws IllegalArgumentException if <code>font</code> is <code>null</code>.
     * @see #getFont()
     */
    public void setFont(Font font);

    /**
     * Indicates the color used to display the balloon's text. This value may be ignored if the balloon's text contains
     * HTML.
     *
     * @return the balloon's text color.
     *
     * @see #setTextColor(java.awt.Color)
     */
    public Color getTextColor();

    /**
     * Specifies the color in which to display the balloon's text. The specified <code>color</code> may be ignored if
     * the balloon's text contains HTML.
     *
     * @param color the color to use for the balloon's text.
     *
     * @throws IllegalArgumentException if <code>color</code> is <code>null</code>.
     * @see #getTextColor()
     */
    public void setTextColor(Color color);

    /**
     * Indicates the location of the balloon's texture (in pixels) relative to the balloon's upper left corner.
     *
     * @return the location of the balloon's texture in pixels.
     *
     * @see #setImageOffset(java.awt.Point)
     */
    public Point getImageOffset();

    /**
     * Specifies the location of the balloon's texture (in pixels) relative to the balloon's upper left corner. The
     * location is applied to the balloon's texture before the balloon's texture scale is applied.
     *
     * @param offset the location of the balloon's texture source in pixels.
     *
     * @throws IllegalArgumentException if <code>offset</code> is <code>null</code>.
     * @see #getImageOffset()
     */
    public void setImageOffset(Point offset);

    /**
     * Indicates the opacity of the balloon's texture as a floating-point value in the range 0.0 to 1.0.
     *
     * @return the balloon texture's opacity as a floating-point value from 0.0 to 1.0.
     *
     * @see #setImageOpacity(double)
     */
    public double getImageOpacity();

    /**
     * Specifies the opacity of the balloon's texture as a floating-point value in the range 0.0 to 1.0. The specified
     * <code>opacity</code> is multiplied by the shape's interior opacity to determine the texture's final opacity. A
     * value of 1.0 specifies a completely opaque texture, and 0.0 specifies a completely transparent texture. Values in
     * between specify a partially transparent texture.
     *
     * @param opacity the balloon texture's opacity as a floating-point value from 0.0 to 1.0.
     *
     * @throws IllegalArgumentException if <code>opacity</code> is less than 0.0 or greater than 1.0.
     * @see #getImageOpacity()
     */
    public void setImageOpacity(double opacity);

    /**
     * Indicates the balloon texture's horizontal and vertical repeat mode.
     *
     * @return the balloon texture's repeat mode, one of {@link gov.nasa.worldwind.avlist.AVKey#REPEAT_NONE}, {@link
     *         gov.nasa.worldwind.avlist.AVKey#REPEAT_X}, {@link gov.nasa.worldwind.avlist.AVKey#REPEAT_Y}, or {@link
     *         gov.nasa.worldwind.avlist.AVKey#REPEAT_XY}
     *
     * @see #setImageRepeat(String)
     */
    public String getImageRepeat();

    /**
     * Specifies the balloon texture's horizontal and vertical repeat mode. The <code>repeat</code> may be one of the
     * following: <ul> <li>{@link gov.nasa.worldwind.avlist.AVKey#REPEAT_NONE}</li> <li>{@link
     * gov.nasa.worldwind.avlist.AVKey#REPEAT_X}</li> <li>{@link gov.nasa.worldwind.avlist.AVKey#REPEAT_Y}</li>
     * <li>{@link gov.nasa.worldwind.avlist.AVKey#REPEAT_XY}</li> </ul>
     * <p/>
     * If <code>repeat</code> is <code>AVKey.REPEAT_NONE</code>, the balloon's texture is displayed according to its
     * offset and scale without any repeating pattern.
     * <p/>
     * If <code>repeat</code> is <code>AVKey.REPEAT_X</code>, <code>AVKey.REPEAT_Y</code>, or
     * <code>AVKey.REPEAT_XY</code>, the balloon's texture is repeated along the X axis, along the Y axis, or along both
     * the X and Y axes, respectively. The texture is repeated after its offset and scale are applied.
     *
     * @param repeat the texture's repeat mode to use, one of <code>AVKey.REPEAT_NONE</code>,
     *               <code>AVKey.REPEAT_X</code>, <code>AVKey.REPEAT_Y</code>, or <code>AVKey.REPEAT_XY</code>.
     *
     * @throws IllegalArgumentException if <code>repeat</code> is <code>null</code>.
     * @see #getImageRepeat()
     */
    public void setImageRepeat(String repeat);
}

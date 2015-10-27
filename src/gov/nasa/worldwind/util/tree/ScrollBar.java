/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A scrollbar component. The scrollable range is defined by four values: min, max, value, and extent. {@code value} is
 * the current position of the scroll bar. {@code extent} represents the visible region. The four values must always
 * satisfy this relationship:
 * <p/>
 * <pre>
 *   min &lt;= value &lt;= value + extent &lt;= max
 * </pre>
 *
 * @author pabercrombie
 * @version $Id: ScrollBar.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScrollBar implements Renderable
{
    /** Scroll increment for one unit up. */
    public static final String UNIT_UP = "gov.nasa.util.ScrollBar.UnitUp";
    /** Scroll increment for one unit down. */
    public static final String UNIT_DOWN = "gov.nasa.util.ScrollBar.UnitDown";
    /** Scroll increment for one page up. */
    public static final String BLOCK_UP = "gov.nasa.util.ScrollBar.BlockUp";
    /** Scroll increment for one page down. */
    public static final String BLOCK_DOWN = "gov.nasa.util.ScrollBar.BlockDown";

    /** Default scroll range minimum value. */
    protected static final int DEFAULT_MIN_VALUE = 0;
    /** Default scroll range maximum value. */
    protected static final int DEFAULT_MAX_VALUE = 100;
    /** Default unit increment. */
    protected static final int DEFAULT_UNIT_INCREMENT = 5;
    /** Default minimum size, in pixels, of the scroll knob. */
    protected static final int DEFAULT_MIN_SCROLL_KNOB_SIZE = 10;
    /** Default delay, in milliseconds, between auto scroll steps. */
    protected static final int DEFAULT_AUTO_SCROLL_DELAY = 20;
    /** Default insets that position the triangle of the scroll arrow in its box. */
    protected static final Insets DEFAULT_ARROW_INSETS = new Insets(2, 2, 2, 2);
    /** Default opacity. */
    protected static final double DEFAULT_OPACITY = 1.0;
    /** Default color used to draw lines in the scroll bar. */
    protected static final Color DEFAULT_LINE_COLOR = Color.BLACK;
    /** Default first color in the scroll knob gradient. */
    protected static final Color DEFAULT_SCROLL_KNOB_COLOR1 = new Color(29, 78, 169);
    /** Default second color in the scroll knob gradient. */
    protected static final Color DEFAULT_SCROLL_KNOB_COLOR2 = new Color(93, 158, 223);

    /** Minimum value in the scroll range. */
    protected int minValue = DEFAULT_MIN_VALUE;
    /** Maximum value in the scroll range. */
    protected int maxValue = DEFAULT_MAX_VALUE;
    /** Current scroll bar value. */
    protected int value;
    /** The amount of the scroll region that is visible in the frame. */
    protected int extent;

    /** Amount that the scroll bar scrolls when the up or down arrow is clicked. */
    protected int unitIncrement = DEFAULT_UNIT_INCREMENT;
    /** Size, in pixels, of the scroll arrow square. */
    protected int scrollArrowSize;

    /**
     * The minimum size of the scroll knob. The size of the knob will adjust to the scroll extent, but will never get
     * smaller than this size. This prevents the knob from shrinking to a single pixels if the scroll range is very
     * large.
     */
    protected int minScrollKnobSize = DEFAULT_MIN_SCROLL_KNOB_SIZE;

    /** Support for setting up and restoring picking state, and resolving the picked object. */
    protected PickSupport pickSupport = new PickSupport();

    /** Full bounds of the scroll bar. */
    protected Rectangle bounds = new Rectangle();
    /**
     * Bounds of the scroll track part of the scroll bar. This is the region in which the scroll knob moves, and
     * excludes the scroll up and down arrows.
     */
    protected Rectangle scrollBounds = new Rectangle();

    /** Insets used to position the triangle in the scroll arrow box. */
    protected Insets arrowInsets = DEFAULT_ARROW_INSETS;

    /** Scroll bar orientation, either {@link AVKey#HORIZONTAL} or {@link AVKey#VERTICAL}. */
    protected String orientation;

    /** Opacity of the scroll bar knob. */
    protected double opacity = DEFAULT_OPACITY;
    /** Color applied to lines in the scroll bar. */
    protected Color lineColor = DEFAULT_LINE_COLOR;

    /** First color of the gradient used to fill the scroll knob. */
    protected Color knobColor1 = DEFAULT_SCROLL_KNOB_COLOR1;
    /** Second color of the gradient used to fill the scroll knob. */
    protected Color knobColor2 = DEFAULT_SCROLL_KNOB_COLOR2;

    // Support for long-running scroll operations
    /**
     * Delay in milliseconds between each step in auto-scroll operation is effect. When auto-scrolling the bar will
     * scroll by the {@link #autoScrollIncrement} every {@code autoScrollDelay} milliseconds.
     */
    protected int autoScrollDelay = DEFAULT_AUTO_SCROLL_DELAY;
    /**
     * Indicates whether an auto-scroll operation is active (such as when the user has clicked and held the mouse on the
     * scroll arrow).
     */
    protected boolean autoScrolling;
    /**
     * The amount and direction that the bar scrolls while auto-scrolling. One of {@link #UNIT_UP}, {@link #UNIT_DOWN},
     * {@link #BLOCK_UP}, {@link #BLOCK_DOWN}.
     */
    protected String autoScrollIncrement;

    // UI controls
    /** HotSpot to handle input on the scroll up control. */
    protected HotSpot scrollUpControl;
    /** HotSpot to handle input on the scroll down control. */
    protected HotSpot scrollDownControl;
    /** HotSpot to handle input on page up control. */
    protected HotSpot scrollUpBlockControl;
    /** HotSpot to handle input on page down control. */
    protected HotSpot scrollDownBlockControl;
    /** HotSpot to handle input on the scroll knob. */
    protected ScrollKnob scrollKnobControl;

    // Values computed once per frame and reused during the frame as needed.
    /** Identifies frame used to calculate per-frame values. */
    protected long frameNumber = -1;
    /** Bounds of the "up arrow" control. */
    protected Rectangle scrollUpControlBounds;
    /** Bounds of the "down arrow" control. */
    protected Rectangle scrollDownControlBounds;
    /** Bounds of the scroll knob. */
    protected Rectangle scrollKnobBounds;
    /** Bounds of the scroll bar area above the knob. */
    protected Rectangle scrollUpBarBounds;
    /** Bounds of the scroll bar area below the knob. */
    protected Rectangle scrollDownBarBounds;
    /** Time at which the scrollbar should automatically update itself. */
    protected long nextAutoScroll;

    /**
     * Create a scroll bar in the vertical orientation.
     *
     * @param parent The screen component that contains the scroll bar. Input events that cannot be handled by the
     *               scroll bar will be passed to this component. May be null.
     */
    public ScrollBar(HotSpot parent)
    {
        this.setOrientation(AVKey.VERTICAL);
        this.initializeUIControls(parent);
    }

    /**
     * Create a scroll bar with an orientation.
     *
     * @param orientation Either {@link AVKey#VERTICAL} or {@link AVKey#HORIZONTAL}.
     * @param parent      The screen component that contains the scroll bar. Input events that cannot be handled by the
     *                    scroll bar will be passed to this component. May be null.
     */
    public ScrollBar(HotSpot parent, String orientation)
    {
        this.setOrientation(orientation);
        this.initializeUIControls(parent);
    }

    /**
     * Initialize the objects that represent the UI controls.
     *
     * @param parent The screen component that contains the scroll bar. Input events that cannot be handled by the
     *               scroll bar will be passed to this component. May be null.
     */
    protected void initializeUIControls(HotSpot parent)
    {
        this.scrollKnobControl = new ScrollKnob(parent, this);
        this.scrollUpControl = new ScrollControl(parent, this, UNIT_UP);
        this.scrollDownControl = new ScrollControl(parent, this, UNIT_DOWN);
        this.scrollUpBlockControl = new ScrollControl(parent, this, BLOCK_UP);
        this.scrollDownBlockControl = new ScrollControl(parent, this, BLOCK_DOWN);
    }

    /**
     * Get the bounds of the scroll bar.
     *
     * @return Scroll bar bounds.
     */
    public Rectangle getBounds()
    {
        return bounds;
    }

    /**
     * Set the bounds of the scroll bar.
     *
     * @param bounds New bounds.
     */
    public void setBounds(Rectangle bounds)
    {
        this.bounds = bounds;

        if (AVKey.VERTICAL.equals(this.getOrientation()))
            this.scrollArrowSize = bounds.width;
        else
            this.scrollArrowSize = bounds.height;

        this.scrollBounds = new Rectangle(bounds.x, bounds.y + this.scrollArrowSize, bounds.width,
            bounds.height - 2 * this.scrollArrowSize);
    }

    /**
     * Get the minimum value in the scroll range.
     *
     * @return Minimum value.
     */
    public int getMinValue()
    {
        return minValue;
    }

    /**
     * Set the minimum value in the scroll range.
     *
     * @param minValue New minimum.
     */
    public void setMinValue(int minValue)
    {
        if (minValue < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "minValue < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.minValue = minValue;

        if (this.getValue() < this.minValue)
            this.setValue(this.minValue);
    }

    /**
     * Get the maximum value in the scroll range.
     *
     * @return Maximum value.
     *
     * @see #getMinValue()
     * @see #setMaxValue(int)
     */
    public int getMaxValue()
    {
        return maxValue;
    }

    /**
     * Set the maximum value in the scroll range.
     *
     * @param maxValue New maximum.
     */
    public void setMaxValue(int maxValue)
    {
        if (maxValue < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "maxValue < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.maxValue = maxValue;

        if (this.getValue() > this.maxValue)
            this.setValue(this.maxValue);
    }

    /**
     * Get the current value of the scroll bar.
     *
     * @return Current value. The value is clamped to the range [minValue : maxValue - extent].
     */
    public int getValue()
    {
        return this.value;
    }

    /**
     * Set the value of the scroll bar. The value is clamped to the range [minValue : maxValue - extent].
     *
     * @param value New value.
     */
    public void setValue(int value)
    {
        this.value = WWMath.clamp(value, this.getMinValue(), this.getMaxValue() - this.getExtent());
    }

    /**
     * Get the unit increment. This is the amount that the scroll bar scrolls by when one of the arrow controls is
     * clicked.
     *
     * @return Unit increment.
     *
     * @see #setUnitIncrement(int)
     */
    public int getUnitIncrement()
    {
        return this.unitIncrement;
    }

    /**
     * Set the unit increment.
     *
     * @param unitIncrement New unit increment. Must be a positive number.
     *
     * @see #getUnitIncrement()
     */
    public void setUnitIncrement(int unitIncrement)
    {
        if (unitIncrement <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "unitIncrement <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.unitIncrement = unitIncrement;
    }

    /**
     * Get the block increment. This is the amount that the scroll bar scrolls by when the bar is clicked above or below
     * the knob.
     *
     * @return The block increment. This implementation returns the extent, so the scroll bar will adjust by a full
     *         visible page.
     */
    public int getBlockIncrement()
    {
        return this.extent;
    }

    /**
     * Get the scroll bar orientation.
     *
     * @return The scroll bar orientation, either {@link AVKey#VERTICAL} or {@link AVKey#HORIZONTAL}.
     */
    public String getOrientation()
    {
        return this.orientation;
    }

    /**
     * Set the scroll bar orientation.
     *
     * @param orientation The scroll bar orientation, either {@link AVKey#VERTICAL} or {@link AVKey#HORIZONTAL}.
     */
    public void setOrientation(String orientation)
    {
        if (orientation == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.orientation = orientation;
    }

    /**
     * Get the extent. The extent the amount of the scrollable region that is visible.
     *
     * @return The extent.
     *
     * @see #setExtent(int)
     */
    public int getExtent()
    {
        return this.extent;
    }

    /**
     * Set the extent. The extent the amount of the scrollable region that is visible. This method may change the value
     * of the scroll bar to maintain the relationship:
     * <pre>
     *   min &lt;= value &lt;= value + extent &lt;= max
     * </pre>
     *
     * @param extent New extent. If {@code extent} is greater than the range of the scroll bar (max - min), then the
     *               extent will be set to the maximum valid value.
     *
     * @see #getExtent()
     */
    public void setExtent(int extent)
    {
        this.extent = Math.min(extent, this.getMaxValue() - this.getMinValue());
        if (this.getValue() + this.getExtent() > this.getMaxValue())
            this.setValue(this.getMaxValue() - this.getExtent());
    }

    /**
     * Get the value as a percentage of the scroll range.
     *
     * @return Current value as percentage.
     */
    public double getValueAsPercentage()
    {
        return (double) this.getValue() / (this.getMaxValue() - this.getMinValue());
    }

    /**
     * Indicates the minimum size of the scrollbar knob, in pixels.
     *
     * @return Minimum size of the knob in pixels.
     */
    public int getMinScrollKnobSize()
    {
        return this.minScrollKnobSize;
    }

    /**
     * Specifies the minimum size of the scrollbar knob, in pixels.
     *
     * @param minSize Minimum size of the knob in pixels.
     */
    public void setMinScrollKnobSize(int minSize)
    {
        this.minScrollKnobSize = minSize;
    }

    /**
     * Get the size of the scroll knob, in pixels.
     *
     * @param scrollAreaSize The size of the scroll area, in pixels.
     *
     * @return Size of the scroll knob, in pixels.
     */
    protected int getKnobSize(int scrollAreaSize)
    {
        return (int) Math.max((scrollAreaSize * ((double) this.getExtent() / (this.getMaxValue() - this.minValue))),
            this.getMinScrollKnobSize());
    }

    /**
     * Get the height of the scroll arrow controls at the top and bottom of the scroll bar.
     *
     * @return Height of arrow control, in pixels.
     */
    protected int getScrollArrowSize()
    {
        return this.scrollArrowSize;
    }

    /**
     * Get the color used to draw the lines of the scroll bar boundary and the scroll arrows.
     *
     * @return Color used for the scroll bar lines.
     *
     * @see #setLineColor(java.awt.Color)
     * @see #getKnobColor()
     */
    public Color getLineColor()
    {
        return lineColor;
    }

    /**
     * Set the color of the lines of the scroll bar boundary. This color is also used for the arrows in the scroll
     * controls.
     *
     * @param color Color for lines and scroll arrows.
     *
     * @see #getLineColor()
     * @see #setKnobColor(java.awt.Color, java.awt.Color)
     */
    public void setLineColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.lineColor = color;
    }

    /**
     * Set the color of scroll knob. The knob is drawn with a gradient made up of two colors.
     *
     * @param color1 First color in the gradient.
     * @param color2 Second color in the gradient.
     *
     * @see #getKnobColor()
     * @see #setLineColor(java.awt.Color)
     */
    public void setKnobColor(Color color1, Color color2)
    {
        if (color1 == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (color2 == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.knobColor1 = color1;
        this.knobColor2 = color2;
    }

    /**
     * Get the color of scroll knob. The knob is drawn with a gradient made up of two colors.
     *
     * @return Two element array containing the two colors that form the gradient.
     *
     * @see #setKnobColor(java.awt.Color, java.awt.Color)
     * @see #getLineColor()
     */
    public Color[] getKnobColor()
    {
        return new Color[] {this.knobColor1, this.knobColor2};
    }

    /**
     * Indicates the opacity of the scroll knob.
     *
     * @return Scroll knob opacity.
     */
    public double getOpacity()
    {
        return this.opacity;
    }

    /**
     * Specifies the opacity of the scroll knob.
     *
     * @param opacity New opacity.
     */
    public void setOpacity(double opacity)
    {
        this.opacity = opacity;
    }

    /**
     * Indicates how frequently the scrollbar updates while one of the scroll arrows is pressed.
     *
     * @return The delay, in milliseconds, between scrollbar updates.
     */
    public int getAutoScrollDelay()
    {
        return this.autoScrollDelay;
    }

    /**
     * Specifies how often the scrollbar will update itself when one of the scroll arrows is pressed.
     *
     * @param delay Delay in milliseconds between scrollbar updates. A smaller number makes the scrollbar scroll faster,
     *              a larger number makes it scroll slower. The delay may not be negative.
     */
    public void setAutoScrollDelay(int delay)
    {
        if (delay < 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", delay);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.autoScrollDelay = delay;
    }

    //**************************************************************//
    //********  Methods for setting the scroll position  ***********//
    //**************************************************************//

    /**
     * Adjust the scroll value.
     *
     * @param amount Amount to add to the current value. A positive value indicates a scroll down; a negative value
     *               indicates a scroll up.
     */
    public void scroll(int amount)
    {
        this.setValue(this.getValue() + amount);
    }

    /**
     * Adjust the scroll bar by the unit amount or the block amount.
     *
     * @param amount One of {@link #UNIT_UP}, {@link #UNIT_DOWN}, {@link #BLOCK_UP}, or {@link #BLOCK_DOWN}.
     */
    public void scroll(String amount)
    {
        if (UNIT_UP.equals(amount))
            this.scroll(-this.getUnitIncrement());
        else if (UNIT_DOWN.equals(amount))
            this.scroll(this.getUnitIncrement());
        else if (BLOCK_UP.equals(amount))
            this.scroll(-this.getBlockIncrement());
        else if (BLOCK_DOWN.equals(amount))
            this.scroll(this.getBlockIncrement());
    }

    /**
     * Start an auto-scroll operation. During auto-scroll, the scroll bar will adjust its value and repaint continuously
     * until the auto-scroll is stopped.
     *
     * @param increment Amount to adjust scroll bar each time. One of {@link #UNIT_UP}, {@link #UNIT_DOWN}, {@link
     *                  #BLOCK_UP}, or {@link #BLOCK_DOWN}.
     *
     * @see #stopAutoScroll()
     * @see #isAutoScrolling()
     * @see #scroll(String)
     */
    public void startAutoScroll(String increment)
    {
        this.autoScrolling = true;
        this.autoScrollIncrement = increment;
    }

    /**
     * Stop an auto-scroll operation.
     *
     * @see #startAutoScroll(String)
     * @see #isAutoScrolling()
     */
    public void stopAutoScroll()
    {
        this.autoScrolling = false;
    }

    /**
     * Is the scroll bar auto-scrolling?
     *
     * @return True if an auto-scroll operation is in progress.
     *
     * @see #startAutoScroll(String)
     * @see #stopAutoScroll()
     */
    public boolean isAutoScrolling()
    {
        return this.autoScrolling;
    }

    //**************************************************************//
    //**********************  Rendering ****************************//
    //**************************************************************//

    /**
     * Draw the scroll bar. The scroll will not draw its bounds are too small to draw without distortion.
     *
     * @param dc the <code>DrawContext</code> to be used
     */
    public void render(DrawContext dc)
    {
        if (dc.getFrameTimeStamp() != this.frameNumber)
        {
            // If an auto-scroll operation is in progress, adjust the scroll value and request that the scene be repainted
            // and a delay so that the next scroll value can be applied.
            if (this.isAutoScrolling())
            {
                // Only scroll if the autoscroll delay has elapsed since the last scroll
                long now = System.currentTimeMillis();
                if (now > this.nextAutoScroll)
                {
                    int delay = this.getAutoScrollDelay();
                    this.scroll(this.autoScrollIncrement);
                    dc.setRedrawRequested(delay);
                    this.nextAutoScroll = now + delay;
                }
            }

            this.computeBounds();
            this.frameNumber = dc.getFrameTimeStamp();
        }

        // Don't draw the scrollbar if the bounds are too small to draw without distortion
        if (!this.canDrawInBounds())
            return;

        if (dc.isPickingMode())
        {
            this.doPick(dc);
        }
        else
        {
            this.draw(dc);
        }
    }

    /**
     * Determines if the scrollbar is able to draw within its bounds.
     *
     * @return {@code true} if the scroll bar is able to draw within the bounds, or {@code false} if the bounds are too
     *         small to draw without distortion.
     */
    protected boolean canDrawInBounds()
    {
        int arrowSize = this.getScrollArrowSize();
        String orientation = this.getOrientation();

        if (AVKey.VERTICAL.equals(orientation))
            return this.bounds.height >= (arrowSize * 2 + this.getMinScrollKnobSize())
                && this.bounds.width >= arrowSize;
        else
            return this.bounds.width >= (arrowSize * 2 + this.getMinScrollKnobSize())
                && this.bounds.height >= arrowSize;
    }

    /** Compute the bounds of the scroll bar. */
    protected void computeBounds()
    {
        int x1 = this.bounds.x;
        int y1 = this.bounds.y;

        int x2 = this.bounds.x + this.bounds.width;
        int y2 = this.bounds.y + this.bounds.height;

        int scrollControlSize = this.getScrollArrowSize();

        if (AVKey.VERTICAL.equals(this.getOrientation()))
        {
            this.scrollDownControlBounds = new Rectangle(x1, y1, scrollControlSize, scrollControlSize);
            this.scrollUpControlBounds = new Rectangle(x1, y2 - scrollControlSize, scrollControlSize,
                scrollControlSize);

            int scrollAreaHeight = this.bounds.height - 2 * scrollControlSize;
            int position = (int) (scrollAreaHeight * this.getValueAsPercentage());

            int knobEnd = y2 - scrollControlSize - position - this.getKnobSize(scrollAreaHeight);

            // Make sure the knob doesn't overlap the scroll down control
            if (knobEnd < y1 + scrollControlSize)
                knobEnd = y1 + scrollControlSize;

            this.scrollKnobBounds = new Rectangle(x1, knobEnd - 1, scrollControlSize,
                this.getKnobSize(scrollAreaHeight) + 1);

            this.scrollDownBarBounds = new Rectangle(x1, y1 + scrollControlSize, scrollControlSize,
                knobEnd - y1 - scrollControlSize);
            int knobStart = (int) this.scrollKnobBounds.getMaxY();
            this.scrollUpBarBounds = new Rectangle(x1, knobStart, scrollControlSize,
                this.scrollUpControlBounds.y - knobStart);
        }
        else
        {
            this.scrollUpControlBounds = new Rectangle(x1, y1, scrollControlSize, scrollControlSize);
            this.scrollDownControlBounds = new Rectangle(x2 - scrollControlSize, y1, scrollControlSize,
                scrollControlSize);

            int scrollAreaWidth = this.bounds.width - 2 * scrollControlSize;
            int position = (int) (scrollAreaWidth * this.getValueAsPercentage());

            int knobStart = x1 + scrollControlSize + position;
            int knobSize = this.getKnobSize(scrollAreaWidth);
            this.scrollKnobBounds = new Rectangle(knobStart, y1, knobSize + 1, scrollControlSize);

            // Make sure the knob doesn't overlap the scroll right control
            if (this.scrollKnobBounds.getMaxX() > x2 - scrollControlSize)
                this.scrollKnobBounds.x = x2 - scrollControlSize - knobSize;

            this.scrollUpBarBounds = new Rectangle(x1 + scrollControlSize, y1,
                this.scrollKnobBounds.x - scrollControlSize - x1, scrollControlSize);
            int knobEnd = (int) this.scrollKnobBounds.getMaxX();
            this.scrollDownBarBounds = new Rectangle(knobEnd, y1, this.scrollDownControlBounds.x - knobEnd,
                scrollControlSize);
        }
    }

    /**
     * Draw the scroll bar.
     *
     * @param dc Current draw context.
     */
    protected void draw(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler oglStack = new OGLStackHandler();
        try
        {
            oglStack.pushAttrib(gl,
                GL2.GL_COLOR_BUFFER_BIT
                    | GL2.GL_CURRENT_BIT
                    | GL2.GL_LINE_BIT
                    | GL2.GL_POLYGON_BIT);

            gl.glLineWidth(1f);
            OGLUtil.applyColor(gl, this.getLineColor(), this.getOpacity(), false);

            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);

            try
            {
                gl.glBegin(GL2.GL_QUADS);
                // Draw scroll bar frame
                this.drawQuad(dc, this.bounds);

                // Draw boxes for up and down arrows
                this.drawQuad(dc, this.scrollDownControlBounds);
                this.drawQuad(dc, this.scrollUpControlBounds);
            }
            finally
            {
                gl.glEnd();
            }

            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

            // Draw background gradient
            String gradientDirection;
            if (AVKey.VERTICAL.equals(this.getOrientation()))
                gradientDirection = AVKey.HORIZONTAL;
            else
                gradientDirection = AVKey.VERTICAL;
            TreeUtil.drawRectWithGradient(gl, this.scrollKnobBounds, this.knobColor2, this.knobColor1,
                this.getOpacity(), gradientDirection);

            // Draw a border around the knob
            OGLUtil.applyColor(gl, this.getLineColor(), this.getOpacity(), false);

            gl.glBegin(GL2.GL_LINE_LOOP);
            this.drawQuad(dc, this.scrollKnobBounds);
            gl.glEnd();

            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            if (AVKey.VERTICAL.equals(this.getOrientation()))
            {
                this.drawTriangle(dc, 90, this.scrollUpControlBounds, arrowInsets);
                this.drawTriangle(dc, -90, this.scrollDownControlBounds, arrowInsets);
            }
            else
            {
                this.drawTriangle(dc, 180, this.scrollUpControlBounds, arrowInsets);
                this.drawTriangle(dc, 0, this.scrollDownControlBounds, arrowInsets);
            }
        }
        finally
        {
            oglStack.pop(gl);
        }
    }

    /**
     * Pick the scroll bar.
     *
     * @param dc Current draw context.
     */
    protected void doPick(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        try
        {
            this.pickSupport.clearPickList();
            this.pickSupport.beginPicking(dc);

            gl.glBegin(GL2.GL_QUADS);

            this.drawPickableQuad(dc, this.scrollDownControl, this.scrollDownControlBounds);
            this.drawPickableQuad(dc, this.scrollUpControl, this.scrollUpControlBounds);
            this.drawPickableQuad(dc, this.scrollDownBlockControl, this.scrollDownBarBounds);
            this.drawPickableQuad(dc, this.scrollUpBlockControl, this.scrollUpBarBounds);

            // The knob, for dragging
            this.drawPickableQuad(dc, this.scrollKnobControl, this.scrollKnobBounds);
        }
        finally
        {
            gl.glEnd();

            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, dc.getPickPoint(), dc.getCurrentLayer());
        }
    }

    /**
     * Draw a filled quad in a unique pick color. This method must be called between {@code glBegin(GL.GL_QUADS)} and
     * {@code glEnd()}.
     *
     * @param dc         Current draw context.
     * @param pickObject User object to attach to the picked object.
     * @param bounds     Bounds of the quad.
     */
    protected void drawPickableQuad(DrawContext dc, Object pickObject, Rectangle bounds)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        Color color = dc.getUniquePickColor();
        int colorCode = color.getRGB();
        this.pickSupport.addPickableObject(colorCode, pickObject);
        gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

        this.drawQuad(dc, bounds);
    }

    /**
     * Draw the vertices of a quadrilateral. This method must be called between {@code glBegin(GL.GL_QUADS)} and {@code
     * glEnd()}.
     *
     * @param dc     Current draw context.
     * @param bounds Bounds of the quad.
     */
    protected void drawQuad(DrawContext dc, Rectangle bounds)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        int minX = (int) bounds.getMinX();
        int minY = (int) bounds.getMinY();
        int maxX = (int) bounds.getMaxX();
        int maxY = (int) bounds.getMaxY();

        gl.glVertex2i(minX, minY);
        gl.glVertex2i(maxX, minY);
        gl.glVertex2i(maxX, maxY);
        gl.glVertex2i(minX, maxY);
    }

    /**
     * Draw a triangle for one of the scroll bar controls.
     *
     * @param dc       Draw context.
     * @param rotation Rotation to apply to the triangle. 0 rotation produces a triangle pointing to the right. Rotation
     *                 must be one of: 0, 90, -90, or 180.
     * @param bounds   The bounds of the scroll control. The arrow must be drawn within this rectangle.
     * @param insets   Insets to apply to the bounds.
     */
    protected void drawTriangle(DrawContext dc, float rotation, Rectangle bounds, Insets insets)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        try
        {
            gl.glPushMatrix();

            // Apply the inset to the bounds. 
            Rectangle insetBounds = new Rectangle(bounds.x + insets.left, bounds.y + insets.bottom,
                bounds.width - insets.left - insets.right,
                bounds.height - insets.top - insets.bottom);

            float halfHeight = insetBounds.height / 2.0f;
            float halfWidth = insetBounds.width / 2.0f;

            float adjustX = 0;
            float adjustY = 0;
            if (rotation == 90)
            {
                adjustX = halfWidth;
                adjustY = (insetBounds.height - halfWidth) / 2.0f;
            }
            else if (rotation == -90)
            {
                adjustX = halfWidth;
                adjustY = (insetBounds.height - halfWidth) / 2.0f + halfWidth;
            }
            else if (rotation == 0)
            {
                adjustX = (insetBounds.width - halfWidth) / 2.0f;
                adjustY = halfHeight;
            }
            else if (rotation == 180)
            {
                adjustX = (insetBounds.width - halfWidth) / 2.0f + halfWidth;
                adjustY = halfHeight;
            }

            gl.glTranslated(insetBounds.x + adjustX, insetBounds.y + adjustY, 1.0);
            gl.glRotatef(rotation, 0, 0, 1);

            gl.glBegin(GL2.GL_TRIANGLES);
            gl.glVertex2f(0, halfHeight);
            gl.glVertex2f(halfWidth, 0);
            gl.glVertex2f(0, -halfHeight);
            gl.glEnd();
        }
        finally
        {
            gl.glPopMatrix();
        }
    }

    //**************************************************************//
    //*****************  User input handling ***********************//
    //**************************************************************//

    /** Control for the scroll arrows and areas of the scroll bar above and below the knob. */
    public class ScrollControl extends TreeHotSpot
    {
        protected ScrollBar scrollBar;
        protected String adjustment;

        public ScrollControl(HotSpot parent, ScrollBar owner, String adjustment)
        {
            super(parent);
            this.scrollBar = owner;
            this.adjustment = adjustment;
        }

        @Override
        public void mousePressed(MouseEvent event)
        {
            if (event == null || event.isConsumed())
                return;

            if (event.getButton() == MouseEvent.BUTTON1)
                scrollBar.startAutoScroll(this.adjustment);
        }

        @Override
        public void mouseReleased(MouseEvent event)
        {
            if (event == null || event.isConsumed())
                return;

            if (event.getButton() == MouseEvent.BUTTON1)
                this.scrollBar.stopAutoScroll();
        }

        @Override
        public void selected(SelectEvent event)
        {
            // Overridden to prevent the super class passing the event to a parent component

            if (event == null || event.isConsumed())
                return;

            // Consume drag events to prevent the globe from panning in response to the drag.
            if (event.isDrag())
                event.consume();
        }

        @Override
        public void mouseClicked(MouseEvent event)
        {
            // Don't let super class pass this event to parent component
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Overridden to stop autoscroll operations when the scrollbar becomes inactive.
         *
         * @param active {@code true} if the scrollbar is being activated, {@code false} if the scrollbar is being
         *               deactivated.
         */
        @Override
        public void setActive(boolean active)
        {
            // If the scrollbar is being deactivated, stop any autoscroll operations that are in progress. When the
            // scrollbar is inactive it will not receive mouse events, so it will not be able to stop the scroll
            // operation when the mouse is released.
            if (!active)
                this.scrollBar.stopAutoScroll();
            super.setActive(active);
        }
    }

    /** Control for dragging the scroll knob. */
    public class ScrollKnob extends DragControl
    {
        protected ScrollBar scrollBar;
        protected int dragRefValue;

        public ScrollKnob(HotSpot parent, ScrollBar owner)
        {
            super(parent);
            this.scrollBar = owner;
        }

        @Override
        public void mouseClicked(MouseEvent event)
        {
            // Don't let super class pass this event to parent component
        }

        @Override
        protected void beginDrag(Point point)
        {
            super.beginDrag(point);
            this.dragRefValue = this.scrollBar.getValue();
        }

        protected void drag(Point point)
        {
            int delta;
            int adjustment;
            int screenDimension;

            if (AVKey.VERTICAL.equals(scrollBar.getOrientation()))
            {
                delta = point.y - this.dragRefPoint.y;
                screenDimension = this.scrollBar.scrollBounds.height - this.scrollBar.getMinScrollKnobSize();
            }
            else
            {
                delta = point.x - this.dragRefPoint.x;
                screenDimension = this.scrollBar.scrollBounds.width;
            }

            int scrollRange = this.scrollBar.getMaxValue() - this.scrollBar.getMinValue();
            adjustment = (int) (((double) delta / screenDimension) * scrollRange);

            this.scrollBar.setValue(this.dragRefValue + adjustment);
        }
    }
}

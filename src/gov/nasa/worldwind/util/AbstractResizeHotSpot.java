/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;

import java.awt.*;
import java.awt.event.*;

/**
 * A HotSpot for resizing a frame or window. This class handles the resize input events, but does does not actually draw
 * the resize controls. The HotSpot is defined by a direction, for example, {@link AVKey#NORTH} indicates that the
 * HotSpot resizes the frame vertically from the north edge (the user clicks the top edge of the frame and drags
 * vertically).
 * <p/>
 * An instance of this class should be added to the picked object when the edge or corner of the frame is picked.
 * <p/>
 * Subclasses must the implement {#getSize}, {#setSize}, {#getScreenPoint}, and {#setScreenPoint} to manipulate the
 * frame that they want to resize.
 *
 * @author pabercrombie
 * @version $Id: AbstractResizeHotSpot.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractResizeHotSpot extends AbstractHotSpot
{
    protected static final int NORTH = 1;
    protected static final int SOUTH = 2;
    protected static final int EAST = 4;
    protected static final int WEST = 8;
    protected static final int NORTHWEST = NORTH + WEST;
    protected static final int NORTHEAST = NORTH + EAST;
    protected static final int SOUTHWEST = SOUTH + WEST;
    protected static final int SOUTHEAST = SOUTH + EAST;

    protected boolean dragging;

    protected Point dragRefPoint;
    protected Dimension refSize;
    protected Point refLocation;

    protected boolean allowVerticalResize = true;
    protected boolean allowHorizontalResize = true;

    /**
     * True if the window needs to be moved in the X direction as it is resized. For example, if the upper left corner
     * is being dragged, the window should move to keep that corner under the cursor.
     */
    protected boolean adjustLocationX;
    /** True if the window needs to be moved in the Y direction as it is resized. */
    protected boolean adjustLocationY;

    protected int xSign = 1;
    protected int ySign = 1;

    protected int cursor;

    protected void setDirection(String direction)
    {
        int dir = 0;
        if (AVKey.NORTH.equals(direction))
            dir = NORTH;
        else if (AVKey.SOUTH.equals(direction))
            dir = SOUTH;
        else if (AVKey.EAST.equals(direction))
            dir = EAST;
        else if (AVKey.WEST.equals(direction))
            dir = WEST;
        else if (AVKey.NORTHEAST.equals(direction))
            dir = NORTHEAST;
        else if (AVKey.NORTHWEST.equals(direction))
            dir = NORTHWEST;
        else if (AVKey.SOUTHEAST.equals(direction))
            dir = SOUTHEAST;
        else if (AVKey.SOUTHWEST.equals(direction))
            dir = SOUTHWEST;

        this.setDirection(dir);
    }

    protected void setDirection(int direction)
    {
        this.adjustLocationX =
            NORTH == direction
                || WEST == direction
                || SOUTHWEST == direction
                || NORTHWEST == direction;
        this.adjustLocationY =
            NORTH == direction
                || WEST == direction
                || NORTHWEST == direction
                || NORTHEAST == direction;

        if (NORTH == direction || SOUTH == direction)
        {
            this.allowVerticalResize = true;
            this.allowHorizontalResize = false;
        }
        else if (EAST == direction || WEST == direction)
        {
            this.allowVerticalResize = false;
            this.allowHorizontalResize = true;
        }
        else
        {
            this.allowVerticalResize = true;
            this.allowHorizontalResize = true;
        }

        if (WEST == direction || SOUTHWEST == direction || NORTHWEST == direction)
            this.xSign = -1;
        else
            this.xSign = 1;

        if (NORTH == direction || NORTHEAST == direction || NORTHWEST == direction)
            this.ySign = -1;
        else
            this.ySign = 1;

        if (NORTH == direction)
            this.cursor = Cursor.N_RESIZE_CURSOR;
        else if (SOUTH == direction)
            this.cursor = Cursor.S_RESIZE_CURSOR;
        else if (EAST == direction)
            this.cursor = Cursor.E_RESIZE_CURSOR;
        else if (WEST == direction)
            this.cursor = Cursor.W_RESIZE_CURSOR;
        else if (NORTHEAST == direction)
            this.cursor = Cursor.NE_RESIZE_CURSOR;
        else if (SOUTHEAST == direction)
            this.cursor = Cursor.SE_RESIZE_CURSOR;
        else if (SOUTHWEST == direction)
            this.cursor = Cursor.SW_RESIZE_CURSOR;
        else if (NORTHWEST == direction)
            this.cursor = Cursor.NW_RESIZE_CURSOR;
    }

    /**
     * Set the resize direction based on which point on the frame was picked (if a point on the left of the frame is
     * picked, the resize direction is west, if a point on the top edge is picked the resize direction is north, etc).
     *
     * @param pickPoint The point on the frame that was picked.
     */
    protected void setDirectionFromPoint(Point pickPoint)
    {
        Point topLeft = this.getScreenPoint();
        Dimension size = this.getSize();

        if (topLeft == null || size == null)
            return;

        // Find the center of the frame
        Point center = new Point(topLeft.x + size.width / 2, topLeft.y + size.height / 2);

        // Find horizontal and vertical distance from pick point to center point
        int dy = center.y - pickPoint.y;
        int dx = pickPoint.x - center.x;

        // Use the sign of dx and dy to determine if we are resizing up or down, left or right
        int vdir = (dy > 0) ? NORTH : SOUTH;
        int hdir = (dx > 0) ? EAST : WEST;

        // Compare the aspect ratio of the frame to the aspect ratio of the rectangle formed by the pick point and the
        // center point. If the aspect ratios are close to equal, resize both horizontally and vertically. Otherwise,
        // resize only horizontally or only vertically.
        double frameAspectRatio = (double) size.width / size.height;
        double pickAspectRatio = Math.abs((double) dx / dy);

        int dir;

        double tolerance = frameAspectRatio * 0.1;
        if (Math.abs(pickAspectRatio - frameAspectRatio) < tolerance)
            dir = hdir + vdir;
        else if (pickAspectRatio < frameAspectRatio)
            dir = vdir;
        else
            dir = hdir;

        this.setDirection(dir);
    }

    /**
     * Is the control currently dragging?
     *
     * @return True if the control is dragging.
     */
    public boolean isDragging()
    {
        return this.dragging;
    }

    /**
     * Handle a {@link gov.nasa.worldwind.event.SelectEvent} and call {@link #beginDrag}, {@link #drag}, {@link
     * #endDrag} as appropriate. Subclasses may override this method if they need to handle events other than drag
     * events.
     *
     * @param event Select event.
     */
    @Override
    public void selected(SelectEvent event)
    {
        if (event == null || this.isConsumed(event))
            return;

        Point pickPoint = event.getPickPoint();
        if (pickPoint != null)
        {
            if (event.isDrag())
            {
                if (!this.isDragging())
                {
                    this.dragging = true;
                    this.beginDrag(pickPoint);
                }

                this.drag(pickPoint);

                event.consume();
            }
        }

        if (event.isDragEnd())
        {
            this.dragging = false;
            this.endDrag();

            event.consume();
        }
    }

    /**
     * Update the resize cursor when the mouse moves.
     *
     * @param e Mouse event.
     */
    @Override
    public void mouseMoved(MouseEvent e)
    {
        if (e == null || e.isConsumed())
            return;
        
        this.setDirectionFromPoint(e.getPoint());
    }

    protected void beginDrag(Point point)
    {
        this.dragRefPoint = point;
        this.refSize = this.getSize();
        this.refLocation = this.getScreenPoint();
    }

    public void drag(Point point)
    {
        int deltaX = 0;
        int deltaY = 0;

        if (this.refLocation == null || this.refSize == null)
            return;

        if (this.allowHorizontalResize)
            deltaX = (point.x - this.dragRefPoint.x) * this.xSign;
        if (this.allowVerticalResize)
            deltaY = (point.y - this.dragRefPoint.y) * this.ySign;

        int width = this.refSize.width + deltaX;
        int height = this.refSize.height + deltaY;

        if (this.isValidSize(width, height))
        {
            this.setSize(new Dimension(width, height));

            if (this.adjustLocationX || this.adjustLocationY)
            {
                double x = this.refLocation.x - (this.adjustLocationX ? deltaX : 0);
                double y = this.refLocation.y - (this.adjustLocationY ? deltaY : 0);
                this.setScreenPoint(new Point((int) x, (int) y));
            }
        }
    }

    /** Called when a drag action ends. This implementation sets {@link #dragRefPoint} to null. */
    protected void endDrag()
    {
        this.dragRefPoint = null;
    }

    /**
     * Get a cursor for the type of resize that this hotspot handles.
     *
     * @return New cursor.
     */
    @Override
    public Cursor getCursor()
    {
        return Cursor.getPredefinedCursor(this.cursor);
    }

    /**
     * {@inheritDoc}
     *
     * Overridden to reset state when the mouse leaves the resize area.
     *
     * @param active {@code true} if the HotSpot is being activated, {@code false} if it is being deactivated.
     */
    @Override
    public void setActive(boolean active)
    {
        // If the resize area is being deactivated, reset the cursor so that the next time the HotSpot becomes active
        // we won't show the wrong cursor.
        if (!active)
            this.cursor = Cursor.DEFAULT_CURSOR;
        super.setActive(active);
    }

    /**
     * Is a frame size valid? This method is called before attempting to resize the frame. If this method returns false,
     * the resize operation is not attempted. This implementation ensures that the proposed frame size is greater than
     * or equal to the minimum frame size.
     *
     * @param width  Frame width.
     * @param height Frame height.
     *
     * @return True if this frame size is valid.
     *
     * @see #getMinimumSize()
     */
    protected boolean isValidSize(int width, int height)
    {
        Dimension minSize = this.getMinimumSize();
        return width >= minSize.width && height >= minSize.height;
    }

    /**
     * Get the minimum size of the frame. The user is not allowed to resize the frame to be smaller than this size. This
     * implementation returns 0, 0.
     *
     * @return Minimum frame size.
     *
     * @see #isValidSize(int, int)
     */
    protected Dimension getMinimumSize()
    {
        return new Dimension(0, 0);
    }

    /**
     * Get the size of the frame.
     *
     * @return Frame size in pixels.
     */
    protected abstract Dimension getSize();

    /**
     * Set the size of the frame.
     *
     * @param newSize New frame size in pixels.
     */
    protected abstract void setSize(Dimension newSize);

    /**
     * Get the screen point of the upper left corner of the frame.
     *
     * @return Screen point measured from upper left corner of the screen (AWT coordinates).
     */
    protected abstract Point getScreenPoint();

    /**
     * Set the screen point of the upper left corner of the frame.
     *
     * @param newPoint New screen point measured from upper left corner of the screen (AWT coordinates).
     */
    protected abstract void setScreenPoint(Point newPoint);
}

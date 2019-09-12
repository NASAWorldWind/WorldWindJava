/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.AbstractResizeHotSpot;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * A screen control for resizing a frame. This class handles the resize input events, but does does not actually draw
 * the resize control.
 *
 * @author pabercrombie
 * @version $Id: FrameResizeControl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FrameResizeControl extends AbstractResizeHotSpot
{
    protected ScrollFrame frame;

    /**
     * Create a resize control.
     *
     * @param frame     Frame to resize.
     */
    public FrameResizeControl(ScrollFrame frame)
    {
        this.frame = frame;
    }

    /** {@inheritDoc} */
    @Override
    protected void beginDrag(Point point)
    {
        super.beginDrag(point);
    }

    /** {@inheritDoc} */
    @Override
    protected void endDrag()
    {
        super.endDrag();
    }

    /** {@inheritDoc} */
    @Override
    protected Dimension getSize()
    {
        return this.frame.getCurrentSize();
    }

    /** {@inheritDoc} */
    @Override
    protected void setSize(Dimension newSize)
    {
        this.frame.setSize(Size.fromPixels(newSize.width, newSize.height));
    }

    /** {@inheritDoc} */
    @Override
    protected Point getScreenPoint()
    {
        Point2D point2D = this.frame.getScreenPoint();
        return new Point((int)point2D.getX(), (int)point2D.getY());
    }

    /** {@inheritDoc} */
    @Override
    protected void setScreenPoint(Point newPoint)
    {
        this.frame.setScreenLocation(new Offset(newPoint.getX(), newPoint.getY(), AVKey.PIXELS, AVKey.INSET_PIXELS));
    }

    /** {@inheritDoc} */
    @Override
    protected Dimension getMinimumSize()
    {
        return this.frame.getMinimumSize();
    }

    /**
     * Forwards mouse wheel events to the frame, so that the contents can be scrolled when the mouse is over the resize
     * area.
     *
     * @param event The event to handle.
     */
    public void mouseWheelMoved(MouseWheelEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        this.frame.mouseWheelMoved(event);
    }
}

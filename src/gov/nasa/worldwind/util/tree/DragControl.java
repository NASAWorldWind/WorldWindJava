/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.util.HotSpot;

import java.awt.*;

/**
 * A {@link TreeHotSpot} that can handle drag events.
 *
 * @author pabercrombie
 * @version $Id: DragControl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class DragControl extends TreeHotSpot
{
    protected boolean dragging;
    protected Point dragRefPoint;

    /**
     * Create a drag control.
     *
     * @param parent The screen area that contains this drag control. Input events that cannot be handled by this object
     *               will be passed to the parent. May be null.
     */
    public DragControl(HotSpot parent)
    {
        super(parent);
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
     * Handle a {@link SelectEvent} and call {@link #beginDrag(java.awt.Point)}, {@link #drag(java.awt.Point)},
     * {@link #endDrag} as appropriate. Subclasses may override this method if they need to handle events other than
     * drag events.
     *
     * @param event Select event.
     */
    @Override
    public void selected(SelectEvent event)
    {
        if (event == null || this.isConsumed(event))
            return;

        Point pickPoint = event.getPickPoint();
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
        else if (event.isDragEnd())
        {
            this.dragging = false;
            this.endDrag();
            event.consume();
        }
    }

    /**
     * Called when a drag begins. This implementation saves the first drag point to {@link #dragRefPoint}.
     *
     * @param point Point at which dragging started.
     */
    protected void beginDrag(Point point)
    {
        this.dragRefPoint = point;
    }

    /**
     * Called for each point within a drag action.
     *
     * @param point Current drag point.
     */
    protected abstract void drag(Point point);

    /**
     * Called when a drag action ends. This implementation sets {@link #dragRefPoint} to null.
     */
    protected void endDrag()
    {
        this.dragRefPoint = null;
    }
}

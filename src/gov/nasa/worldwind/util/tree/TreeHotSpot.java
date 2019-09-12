/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.util.*;

import java.awt.event.*;

/**
 * An area of a {@link gov.nasa.worldwind.util.tree.Tree} that can receive select and mouse events. The TreeHotSpot's
 * default behavior is to forward events to its parent HotSpot. Subclasses must override methods for events they can
 * react to, and all other events are handled by the parent.
 *
 * @author pabercrombie
 * @version $Id: TreeHotSpot.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TreeHotSpot extends AbstractHotSpot
{
    /** The parent HotSpot, or null if this TreeHotSpot has no parent. */
    protected HotSpot parent;

    /**
     * Create a hot spot.
     *
     * @param parent The screen area that contains this hot spot. Input events that cannot be handled by this object
     *               will be passed to the parent. May be null.
     */
    public TreeHotSpot(HotSpot parent)
    {
        this.parent = parent;
    }

    /**
     * Forwards the event to the parent HotSpot if the parent is non-null. Otherwise does nothing. Override this method
     * to handle key released events.
     *
     * @param event The event to handle.
     */
    public void selected(SelectEvent event)
    {
        if (event == null || this.isConsumed(event))
            return;

        if (this.parent != null)
            this.parent.selected(event);
    }

    /**
     * Forwards the event to the parent HotSpot if the parent is non-null. Otherwise does nothing. Override this method
     * to handle mouse click events.
     *
     * @param event The event to handle.
     */
    public void mouseClicked(MouseEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        if (this.parent != null)
            this.parent.mouseClicked(event);
    }

    /**
     * Forwards the event to the parent HotSpot if the parent is non-null. Otherwise does nothing. Override this method
     * to handle mouse pressed events.
     *
     * @param event The event to handle.
     */
    public void mousePressed(MouseEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        if (this.parent != null)
            this.parent.mousePressed(event);
    }

    /**
     * Forwards the event to the parent HotSpot if the parent is non-null. Otherwise does nothing. Override this method
     * to handle mouse released events.
     *
     * @param event The event to handle.
     */
    public void mouseReleased(MouseEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        if (this.parent != null)
            this.parent.mouseReleased(event);
    }

    /**
     * Forwards the event to the parent HotSpot if the parent is non-null. Otherwise does nothing. Override this method
     * to handle mouse wheel events.
     *
     * @param event The event to handle.
     */
    public void mouseWheelMoved(MouseWheelEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        if (this.parent != null)
            this.parent.mouseWheelMoved(event);
    }
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.event;

import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * This class signals that an object or terrain is under the cursor and identifies that object and the operation that
 * caused the signal. See the <em>Field Summary</em> for a description of the possible operations. When a SelectEvent
 * occurs, all select event listeners registered with the associated {@link gov.nasa.worldwind.WorldWindow} are called.
 * Select event listeners are registered by calling {@link gov.nasa.worldwind.WorldWindow#addSelectListener(SelectListener)}.
 * <p/>
 * A <code>ROLLOVER</code> SelectEvent is generated every frame when the cursor is over a visible object either because
 * the user moved it there or because the World Window was repainted and a visible object was found to be under the
 * cursor. A <code>ROLLOVER</code> SelectEvent is also generated when there are no longer any objects under the cursor.
 * Select events generated for objects under the cursor have a non-null pickPoint, and contain the top-most visible
 * object of all objects at the cursor position.
 * <p/>
 * A <code>BOX_ROLLOVER</code> SelectEvent is generated every frame when the selection box intersects a visible object
 * either because the user moved or expanded it or because the World Window was repainted and a visible object was found
 * to intersect the box. A <code>BOX_ROLLOVER</code> SelectEvent is also generated when there are no longer any objects
 * intersecting the selection box. Select events generated for objects intersecting the selection box have a non-null
 * pickRectangle, and contain all top-most visible objects of all objects intersecting the selection box.
 * <p/>
 * If a select listener performs some action in response to a select event, it should call the event's {@link
 * #consume()} method in order to indicate to subsequently called listeners that the event has been responded to and no
 * further action should be taken. Left press select events should not be consumed unless it is necessary to do so.
 * Consuming left press events prevents the WorldWindow from gaining focus, thereby preventing it from receiving key
 * events.
 * <p/>
 * If no object is under the cursor but the cursor is over terrain, the select event will identify the terrain as the
 * picked object and will include the corresponding geographic position. See {@link
 * gov.nasa.worldwind.pick.PickedObject#isTerrain()}.
 *
 * @author tag
 * @version $Id: SelectEvent.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings({"StringEquality"})
public class SelectEvent extends WWEvent
{
    /** The user clicked the left mouse button while the cursor was over picked object. */
    public static final String LEFT_CLICK = "gov.nasa.worldwind.SelectEvent.LeftClick";
    /** The user double-clicked the left mouse button while the cursor was over picked object. */
    public static final String LEFT_DOUBLE_CLICK = "gov.nasa.worldwind.SelectEvent.LeftDoubleClick";
    /** The user clicked the right mouse button while the cursor was over picked object. */
    public static final String RIGHT_CLICK = "gov.nasa.worldwind.SelectEvent.RightClick";
    /** The user pressed the left mouse button while the cursor was over picked object. */
    public static final String LEFT_PRESS = "gov.nasa.worldwind.SelectEvent.LeftPress";
    /** The user pressed the right mouse button while the cursor was over picked object. */
    public static final String RIGHT_PRESS = "gov.nasa.worldwind.SelectEvent.RightPress";
    /**
     * The cursor has moved over the picked object and become stationary, or has moved off the object of the most recent
     * <code>HOVER</code> event. In the latter case, the picked object will be null.
     */
    public static final String HOVER = "gov.nasa.worldwind.SelectEvent.Hover";
    /**
     * The cursor has moved over the object or has moved off the object most recently rolled over. In the latter case
     * the picked object will be null.
     */
    public static final String ROLLOVER = "gov.nasa.worldwind.SelectEvent.Rollover";
    /** The user is attempting to drag the picked object. */
    public static final String DRAG = "gov.nasa.worldwind.SelectEvent.Drag";
    /** The user has stopped dragging the picked object. */
    public static final String DRAG_END = "gov.nasa.worldwind.SelectEvent.DragEnd";
    /**
     * The user has selected one or more of objects using a selection box. A box rollover event is generated every frame
     * if one or more objects intersect the box, in which case the event's pickedObjects list contain the selected
     * objects. A box rollover event is generated once when the selection becomes empty, in which case the event's
     * pickedObjects is <code>null</code>. In either case, the event's pickRect contains the selection box bounds in AWT
     * screen coordinates.
     */
    public static final String BOX_ROLLOVER = "gov.nasa.worldwind.SelectEvent.BoxRollover";

    private final String eventAction;
    private final Point pickPoint;
    private final Rectangle pickRect;
    private final MouseEvent mouseEvent;
    private final PickedObjectList pickedObjects;

    public SelectEvent(Object source, String eventAction, MouseEvent mouseEvent, PickedObjectList pickedObjects)
    {
        super(source);
        this.eventAction = eventAction;
        this.pickPoint = mouseEvent != null ? mouseEvent.getPoint() : null;
        this.pickRect = null;
        this.mouseEvent = mouseEvent;
        this.pickedObjects = pickedObjects;
    }

    public SelectEvent(Object source, String eventAction, Point pickPoint, PickedObjectList pickedObjects)
    {
        super(source);
        this.eventAction = eventAction;
        this.pickPoint = pickPoint;
        this.pickRect = null;
        this.mouseEvent = null;
        this.pickedObjects = pickedObjects;
    }

    public SelectEvent(Object source, String eventAction, Rectangle pickRectangle, PickedObjectList pickedObjects)
    {
        super(source);
        this.eventAction = eventAction;
        this.pickPoint = null;
        this.pickRect = pickRectangle;
        this.mouseEvent = null;
        this.pickedObjects = pickedObjects;
    }

    @Override
    public void consume()
    {
        super.consume();

        if (this.getMouseEvent() != null)
            this.getMouseEvent().consume();
    }

    public String getEventAction()
    {
        return this.eventAction != null ? this.eventAction : "gov.nasa.worldwind.SelectEvent.UnknownEventAction";
    }

    public Point getPickPoint()
    {
        return this.pickPoint;
    }

    public Rectangle getPickRectangle()
    {
        return this.pickRect;
    }

    public MouseEvent getMouseEvent()
    {
        return this.mouseEvent;
    }

    public boolean hasObjects()
    {
        return this.pickedObjects != null && this.pickedObjects.size() > 0;
    }

    public PickedObjectList getObjects()
    {
        return this.pickedObjects;
    }

    public PickedObject getTopPickedObject()
    {
        return this.hasObjects() ? this.pickedObjects.getTopPickedObject() : null;
    }

    public Object getTopObject()
    {
        PickedObject tpo = this.getTopPickedObject();
        return tpo != null ? tpo.getObject() : null;
    }

    /**
     * Returns a list of all picked objects in this event's picked object list who's onTop flag is set to true. This
     * returns <code>null</code> if this event's picked object list is empty, or does not contain any picked objects
     * marked as on top.
     *
     * @return a new list of the picked objects marked as on top, or <code>null</code> if nothing is marked as on top.
     */
    public List<PickedObject> getAllTopPickedObjects()
    {
        return this.hasObjects() ? this.pickedObjects.getAllTopPickedObjects() : null;
    }

    /**
     * Returns a list of all objects associated with a picked object in this event's picked object list who's onTop flag
     * is set to true. This returns <code>null</code> if this event's picked object list is empty, or does not contain
     * any picked objects marked as on top.
     *
     * @return a new list of the objects associated with a picked object marked as on top, or <code>null</code> if
     *         nothing is marked as on top.
     */
    public List<?> getAllTopObjects()
    {
        return this.hasObjects() ? this.pickedObjects.getAllTopObjects() : null;
    }

    public boolean isRollover()
    {
        return this.getEventAction() == ROLLOVER;
    }

    public boolean isHover()
    {
        return this.getEventAction() == HOVER;
    }

    public boolean isDragEnd()
    {
        return this.getEventAction() == DRAG_END;
    }

    public boolean isDrag()
    {
        return this.getEventAction() == DRAG;
    }

    public boolean isRightPress()
    {
        return this.getEventAction() == RIGHT_PRESS;
    }

    public boolean isRightClick()
    {
        return this.getEventAction() == RIGHT_CLICK;
    }

    public boolean isLeftDoubleClick()
    {
        return this.getEventAction() == LEFT_DOUBLE_CLICK;
    }

    public boolean isLeftClick()
    {
        return this.getEventAction() == LEFT_CLICK;
    }

    public boolean isLeftPress()
    {
        return this.getEventAction() == LEFT_PRESS;
    }

    public boolean isBoxSelect()
    {
        return this.getEventAction() == BOX_ROLLOVER;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(this.getClass().getName() + " "
            + (this.eventAction != null ? this.eventAction : Logging.getMessage("generic.Unknown")));
        if (this.pickedObjects != null && this.pickedObjects.getTopObject() != null)
            sb.append(", ").append(this.pickedObjects.getTopObject().getClass().getName());

        return sb.toString();
    }
}

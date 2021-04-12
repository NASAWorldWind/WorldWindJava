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
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.event.SelectEvent;

import java.awt.*;
import java.awt.event.*;

/**
 * AbstractHotSpot is an abstract base class for the {@link gov.nasa.worldwind.util.HotSpot} interface. The methods in
 * AbstractHotSpot are empty or simply return {@code null}. This is a convenience class for that enables a subclass to
 * override only the events its interested in.
 *
 * @author dcollins
 * @version $Id: AbstractHotSpot.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractHotSpot extends AVListImpl implements HotSpot
{
    /** Indicates whether or not this HotSpot is active. */
    protected boolean active;

    /** Creates a new AbstractHotSpot, but otherwise does nothing. */
    public AbstractHotSpot()
    {
    }

    /** {@inheritDoc} */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    /** {@inheritDoc} */
    public boolean isActive()
    {
        return this.active;
    }

    /**
     * Called when the HotSpot is selected in the WorldWindow. The default implementation does nothing. Override this
     * method to handle select events.
     *
     * @param event The event to handle.
     */
    public void selected(SelectEvent event)
    {
    }

    /**
     * Called when a key is typed and the HotSpot is active. The default implementation does nothing. Override this
     * method to handle key typed events.
     *
     * @param event The event to handle.
     */
    public void keyTyped(KeyEvent event)
    {
    }

    /**
     * Called when a key is pressed and the HotSpot is active. The default implementation does nothing. Override this
     * method to handle key pressed events.
     *
     * @param event The event to handle.
     */
    public void keyPressed(KeyEvent event)
    {
    }

    /**
     * Called when a key is released and the HotSpot is active. The default implementation does nothing. Override this
     * method to handle key released events.
     *
     * @param event The event to handle.
     */
    public void keyReleased(KeyEvent event)
    {
    }

    /**
     * Called when the mouse is clicked on the HotSpot in the WorldWindow. The default implementation does nothing.
     * Override this method to handle mouse click events.
     *
     * @param event The event to handle.
     */
    public void mouseClicked(MouseEvent event)
    {
    }

    /**
     * Called when the mouse is pressed over the HotSpot in the WorldWindow. The default implementation does nothing.
     * Override this method to handle mouse pressed events.
     *
     * @param event The event to handle.
     */
    public void mousePressed(MouseEvent event)
    {
    }

    /**
     * Called when the mouse is released over the HotSpot in the WorldWindow. The default implementation does nothing.
     * Override this method to handle mouse released events.
     *
     * @param event The event to handle.
     */
    public void mouseReleased(MouseEvent event)
    {
    }

    /**
     * Called when the mouse enters the WorldWindow and the HotSpot is active. The default implementation does nothing.
     * Override this method to handle mouse enter events.
     *
     * @param event The event to handle.
     */
    public void mouseEntered(MouseEvent event)
    {
    }

    /**
     * Called when the mouse exits the WorldWindow and the HotSpot is active. The default implementation does nothing.
     * Override this method to handle mouse exit events.
     *
     * @param event The event to handle.
     */
    public void mouseExited(MouseEvent event)
    {
    }

    /**
     * Called when the mouse is dragged in the WorldWindow and the HotSpot is active. The default implementation does
     * nothing. Override this method to handle mouse dragged events.
     *
     * @param event The event to handle.
     */
    public void mouseDragged(MouseEvent event)
    {
    }

    /**
     * Called when the cursor moves over the HotSpot in the WorldWindow. The default implementation does nothing.
     * Override this method to handle mouse move events.
     *
     * @param event The event to handle.
     */
    public void mouseMoved(MouseEvent event)
    {
    }

    /**
     * Called when the mouse wheel is moved in the WorldWindow and HotSpot is active. The default implementation does
     * nothing. Override this method to handle mouse wheel events.
     *
     * @param event The event to handle.
     */
    public void mouseWheelMoved(MouseWheelEvent event)
    {
    }

    /**
     * Returns a {@code null} Cursor, indicating the default cursor should be used when the HotSpot is active.
     *
     * @return A {@code null} Cursor.
     */
    public Cursor getCursor()
    {
        return null;
    }

    /**
     * Determine if a select event, or the mouse event that generated the select event, has been consumed.
     *
     * @param event Event to test.
     *
     * @return {@code true} if {@code event} has been consumed, or if {@code event} was triggered by a mouse event, and
     *         that mouse event has been consumed.
     */
    protected boolean isConsumed(SelectEvent event)
    {
        return event.isConsumed() || (event.getMouseEvent() != null && event.getMouseEvent().isConsumed());
    }
}

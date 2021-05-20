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

import gov.nasa.worldwind.event.*;

import java.awt.*;
import java.awt.event.*;

/**
 * HotSpot is an interface for forwarding select, keyboard, and mouse events to picked objects in the {@link
 * gov.nasa.worldwind.WorldWindow}. When the HotSpot is active it receives input events that occur in the WorldWindow.
 *
 * @author dcollins
 * @version $Id: HotSpot.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface HotSpot extends SelectListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
    /**
     * Called when this HotSpot is activated or deactivated. The HotSpot only receives input events when it is active.
     *
     * @param active {@code true} if this HotSpot is being activated. {@code false} if this HotSpot is being deactivated.
     */
    void setActive(boolean active);

    /**
     * Indicates whether or not this HotSpot is active.
     *
     * @return {@code true} if this HotSpot is active, {@code false} if not.
     */
    boolean isActive();

    /**
     * Called when the HotSpot is selected in the WorldWindow.
     *
     * @param event The event to handle.
     */
    void selected(SelectEvent event);

    /**
     * Called when a key is typed and the HotSpot is active.
     *
     * @param event The event to handle.
     */
    void keyTyped(KeyEvent event);

    /**
     * Called when a key is pressed and the HotSpot is active.
     *
     * @param event The event to handle.
     */
    void keyPressed(KeyEvent event);

    /**
     * Called when a key is released and the HotSpot is active.
     *
     * @param event The event to handle.
     */
    void keyReleased(KeyEvent event);

    /**
     * Called when the mouse is clicked on the HotSpot in the WorldWindow.
     *
     * @param event The event to handle.
     */
    void mouseClicked(MouseEvent event);

    /**
     * Called when the mouse is pressed over the HotSpot in the WorldWindow.
     *
     * @param event The event to handle.
     */
    void mousePressed(MouseEvent event);

    /**
     * Called when the mouse is released over the HotSpot in the WorldWindow.
     *
     * @param event The event to handle.
     */
    void mouseReleased(MouseEvent event);

    /**
     * Called when the mouse enters the WorldWindow and the HotSpot is active.
     *
     * @param event The event to handle.
     */
    void mouseEntered(MouseEvent event);

    /**
     * Called when the mouse exits the WorldWindow and the HotSpot is active.
     *
     * @param event The event to handle.
     */
    void mouseExited(MouseEvent event);

    /**
     * Called when the mouse is dragged in the WorldWindow and the HotSpot is active.
     *
     * @param event The event to handle.
     */
    void mouseDragged(MouseEvent event);

    /**
     * Called when the cursor moves over the HotSpot in the WorldWindow.
     *
     * @param event The event to handle.
     */
    void mouseMoved(MouseEvent event);

    /**
     * Called when the mouse wheel is moved in the WorldWindow and HotSpot is active.
     *
     * @param event The event to handle.
     */
    void mouseWheelMoved(MouseWheelEvent event);

    /**
     * Returns the AWT {@link java.awt.Cursor} representation to display when the HotSpot is active, or {@code null} to
     * use the default Cursor.
     *
     * @return The Cursor representation associated with the HotSpot. May return {@code null}.
     */
    Cursor getCursor();
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
     * Called when the HotSpot is selected in the World Window.
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
     * Called when the mouse is clicked on the HotSpot in the World Window.
     *
     * @param event The event to handle.
     */
    void mouseClicked(MouseEvent event);

    /**
     * Called when the mouse is pressed over the HotSpot in the World Window.
     *
     * @param event The event to handle.
     */
    void mousePressed(MouseEvent event);

    /**
     * Called when the mouse is released over the HotSpot in the World Window.
     *
     * @param event The event to handle.
     */
    void mouseReleased(MouseEvent event);

    /**
     * Called when the mouse enters the World Window and the HotSpot is active.
     *
     * @param event The event to handle.
     */
    void mouseEntered(MouseEvent event);

    /**
     * Called when the mouse exits the World Window and the HotSpot is active.
     *
     * @param event The event to handle.
     */
    void mouseExited(MouseEvent event);

    /**
     * Called when the mouse is dragged in the World Window and the HotSpot is active.
     *
     * @param event The event to handle.
     */
    void mouseDragged(MouseEvent event);

    /**
     * Called when the cursor moves over the HotSpot in the World Window.
     *
     * @param event The event to handle.
     */
    void mouseMoved(MouseEvent event);

    /**
     * Called when the mouse wheel is moved in the World Window and HotSpot is active.
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

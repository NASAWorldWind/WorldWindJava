/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.event;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;

import java.awt.event.*;

/**
 * @author tag
 * @version $Id: InputHandler.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface InputHandler extends AVList, java.beans.PropertyChangeListener
{
    void setEventSource(WorldWindow newWorldWindow);

    WorldWindow getEventSource();

    void setHoverDelay(int delay);

    int getHoverDelay();

    void addSelectListener(SelectListener listener);

    void removeSelectListener(SelectListener listener);

    void addKeyListener(KeyListener listener);

    void removeKeyListener(KeyListener listener);

    void addMouseListener(MouseListener listener);

    void removeMouseListener(MouseListener listener);

    void addMouseMotionListener(MouseMotionListener listener);

    void removeMouseMotionListener(MouseMotionListener listener);

    void addMouseWheelListener(MouseWheelListener listener);

    void removeMouseWheelListener(MouseWheelListener listener);

    void dispose();

    /**
     * Indicates whether a redraw is forced when the a mouse button is pressed. Touch screen devices require this so
     * that the current position and selection are updated when the button is pressed. The update occurs naturally on
     * non-touch screen devices because the motion of the mouse prior to the press causes the current position and
     * selection to be updated.
     *
     * @return true if a redraw is forced when a button is pressed, otherwise false.
     */
    boolean isForceRedrawOnMousePressed();

    /**
     * Specifies whether a redraw is forced when the a mouse button is pressed. Touch screen devices require this so
     * that the current position and selection are updated when the button is pressed. The update occurs naturally on
     * non-touch screen devices because the motion of the mouse prior to the press causes the current position and
     * selection to be updated.
     *
     * @param forceRedrawOnMousePressed true to force a redraw on button press, otherwise false, the default.
     */
    void setForceRedrawOnMousePressed(boolean forceRedrawOnMousePressed);
}

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

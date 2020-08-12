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
package gov.nasa.worldwind.util.dashboard;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: DashboardController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DashboardController implements MouseListener, Disposable {

    private DashboardDialog dialog;
    private Component component;
    private WorldWindow wwd;

    public DashboardController(WorldWindow wwd, Component component) {
        if (wwd == null) {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
        this.component = component;
        wwd.getInputHandler().addMouseListener(this);
    }

    @Override
    public void dispose() {
        if (this.dialog != null) {
            this.dialog.dispose();
            this.dialog = null;
        }

        if (this.wwd.getInputHandler() != null) {
            this.wwd.getInputHandler().removeMouseListener(this);
        }
        this.wwd = null;

        this.component = null;
    }

    public void raiseDialog() {
        if (this.dialog == null) {
            this.dialog = new DashboardDialog(getParentFrame(this.component), wwd);
        }

        this.dialog.raiseDialog();
    }

    public void lowerDialog() {
        if (this.dialog != null) {
            this.dialog.lowerDialog();
        }
    }

    private Frame getParentFrame(Component comp) {
        return comp != null ? (Frame) SwingUtilities.getAncestorOfClass(Frame.class, comp) : null;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if ((event.getButton() == MouseEvent.BUTTON1
                && (event.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0
                && (event.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0
                && (event.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)) {
            raiseDialog();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}

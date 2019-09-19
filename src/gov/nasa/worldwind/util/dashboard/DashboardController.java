/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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

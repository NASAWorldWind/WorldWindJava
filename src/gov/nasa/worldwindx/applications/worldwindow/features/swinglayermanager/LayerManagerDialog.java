/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager;

import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.features.AbstractFeature;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import javax.swing.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: LayerManagerDialog.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LayerManagerDialog extends AbstractFeature
{
    private static final String ICON_PATH
        = "gov/nasa/worldwindx/applications/worldwindow/images/layer-manager-64x64.png";

    protected JDialog dialog;
    protected boolean positionInitialized = false;

    public LayerManagerDialog(Registry registry)
    {
        super("Layer Manager", Constants.FEATURE_LAYER_MANAGER_DIALOG, ICON_PATH, registry);
        setEnabled(true);
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);

        this.dialog = new JDialog(this.controller.getFrame());
        this.dialog.setPreferredSize(new Dimension(350, 700));
        this.dialog.getContentPane().setLayout(new BorderLayout());
        this.dialog.setResizable(true);
        this.dialog.setModal(false);
        this.dialog.setTitle("Layer Manager");

        ControlsPanel controlsPanel = (ControlsPanel) controller.getRegisteredObject(Constants.CONTROLS_PANEL);
        if (controlsPanel != null)
            this.dialog.getContentPane().add(controlsPanel.getJPanel(), BorderLayout.CENTER);
        else
            Util.getLogger().severe("Control panel is not registered.");

        this.dialog.pack();

        this.addToToolBar();
    }

    @Override
    public boolean isTwoState()
    {
        return true;
    }

    public boolean isOn()
    {
        return this.dialog.isVisible();
    }

    @Override
    public void turnOn(boolean tf)
    {
        if (tf && !this.positionInitialized)
        {
            Util.positionDialogInContainer(this.dialog, this.controller.getAppPanel().getJPanel(),
                SwingConstants.WEST, SwingConstants.NORTH);
            this.positionInitialized = true;

            // Make the default layer group visible the first time the dialog is raised.
            this.controller.getLayerManager().scrollToLayer(null);
        }

        this.setVisible(tf);
    }

    protected void setVisible(boolean tf)
    {
        this.dialog.setVisible(tf);
    }
}
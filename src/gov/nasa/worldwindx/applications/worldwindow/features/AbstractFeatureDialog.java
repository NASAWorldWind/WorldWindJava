/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: AbstractFeatureDialog.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AbstractFeatureDialog extends AbstractFeature implements WWODialog
{
    protected JDialog dialog;
    protected JPanel dialogPanel;
    protected JPanel leftButtonPanel;
    protected JPanel rightButtonPanel;
    protected JPanel centerButtonPanel;
    protected JButton closeButton;
    protected boolean positionInitialized = false;

    private int horizontalLocation = SwingConstants.CENTER;
    private int verticalLocation = SwingConstants.CENTER;

    protected AbstractFeatureDialog(String name, String featureID, Registry registry)
    {
        super(name, featureID, registry);
    }

    protected AbstractFeatureDialog(String name, String featureID, String largeIconPath, Registry registry)
    {
        super(name, featureID, largeIconPath, registry);
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.dialog = new JDialog(this.controller.getFrame());
        this.dialog.setResizable(false);
        this.dialog.setModal(false);

        this.dialogPanel = this.createButtonPanel();
        this.dialog.getContentPane().add(this.dialogPanel, BorderLayout.SOUTH);

        this.setTitle(this.getName());
    }

    public JDialog getJDialog()
    {
        return this.dialog;
    }

    public void setTitle(String title)
    {
        this.dialog.setTitle(title != null ? title : "");
    }

    protected void setTaskPanel(FeaturePanel panel)
    {
        this.setTaskComponent(panel.getJPanel());
    }

    protected void setTaskPanel(String featureID)
    {
        FeaturePanel panel = (FeaturePanel) this.controller.getRegisteredObject(featureID);
        if (panel == null)
        {
            Util.getLogger().warning("Registrado ning\u00fan objeto para caracter\u00edstica " + featureID);
            return;
        }

        this.setTaskComponent(panel.getJPanel());

        JComponent[] dialogControls = panel.getDialogControls();
        if (dialogControls != null)
        {
            for (JComponent c : dialogControls)
            {
                this.insertDialogComponent(c);
            }
        }
    }

    // Specifies the main component of the dialog, typically the main panel.
    protected void setTaskComponent(JComponent component)
    {
        this.dialog.getContentPane().add(component, BorderLayout.CENTER);
    }

    protected void insertLeftDialogComponent(JComponent component)
    {
        int n = this.leftButtonPanel.getComponentCount();
        this.leftButtonPanel.add(component,
            n == 0 ? BorderLayout.WEST : n == 1 ? BorderLayout.CENTER : BorderLayout.EAST);
    }

    protected void insertRightDialogComponent(JComponent component)
    {
        int n = this.rightButtonPanel.getComponentCount();
        this.rightButtonPanel.add(component,
            n == 0 ? BorderLayout.EAST : n == 1 ? BorderLayout.CENTER : BorderLayout.WEST);
    }

    protected void insertCenterDialogComponent(JComponent component)
    {
        int n = this.centerButtonPanel.getComponentCount();
        this.centerButtonPanel.add(component,
            n == 0 ? BorderLayout.CENTER : n == 1 ? BorderLayout.EAST : BorderLayout.WEST);
    }

    protected void insertDialogComponent(JComponent component)
    {
        this.insertRightDialogComponent(component);
    }

    protected void setLocation(int horizontal, int vertical)
    {
        this.horizontalLocation = horizontal;
        this.verticalLocation = vertical;
        this.positionInitialized = false;
    }

    @Override
    public void turnOn(boolean tf)
    {
        this.setVisible(tf);
    }

    public void setVisible(boolean tf)
    {
        if (tf)
            this.dialog.pack();

        if (tf && !this.positionInitialized)
        {
            Util.positionDialogInContainer(this.dialog, this.controller.getAppPanel().getJPanel(),
                this.horizontalLocation, this.verticalLocation);
            this.positionInitialized = true;
        }

        this.dialog.setVisible(tf);
    }

    private JPanel createButtonPanel()
    {
        this.leftButtonPanel = new JPanel(new BorderLayout(10, 5));
        this.rightButtonPanel = new JPanel(new BorderLayout(10, 5));
        this.centerButtonPanel = new JPanel(new BorderLayout(10, 5));

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));
        panel.add(this.leftButtonPanel, BorderLayout.WEST);
        panel.add(this.rightButtonPanel, BorderLayout.EAST);
        panel.add(this.centerButtonPanel, BorderLayout.CENTER);

        this.closeButton = new JButton("Close");
        this.closeButton.setToolTipText("Close dialog");
        this.closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                setVisible(false);
            }
        });

        this.rightButtonPanel.add(this.closeButton, BorderLayout.EAST);

        return panel;
    }
}

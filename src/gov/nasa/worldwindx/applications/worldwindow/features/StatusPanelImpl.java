/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.util.ShadedPanel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.Date;

/**
 * @author tag
 * @version $Id: StatusPanelImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class StatusPanelImpl extends AbstractFeature implements StatusPanel, SelectListener
{
    private ShadedPanel panel;
    private Color panelTextColor = new Color(0x184984);
    private JLabel statusMessageLabel;

    public StatusPanelImpl(Registry registry)
    {
        super("Status Panel", Constants.STATUS_PANEL, registry);

        this.panel = new ShadedPanel(new BorderLayout());
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);

        this.panel.setOpaque(false);
        this.panel.setBorder(new EmptyBorder(4, 5, 4, 5));

        JPanel leftPanel = createLeftComponents();
        JPanel centerPanel = createCenterComponents();
        JPanel rightPanel = createRightComponents();

        if (leftPanel != null)
            this.panel.add(leftPanel, BorderLayout.WEST);

        if (centerPanel != null)
            this.panel.add(centerPanel, BorderLayout.CENTER);

        if (rightPanel != null)
            this.panel.add(rightPanel, BorderLayout.EAST);

        this.controller.getWWd().addSelectListener(this); // to handle status bar messages for picked items
    }

    public JPanel getJPanel()
    {
        return this.panel;
    }

    public JComponent[] getDialogControls()
    {
        return null;
    }

    public String setStatusMessage(String message)
    {
        String oldMessage = this.statusMessageLabel.getText();

        if (WWUtil.isEmpty(message))
            this.statusMessageLabel.setText("");
        else
            this.statusMessageLabel.setText(message);

        return WWUtil.isEmpty(oldMessage) ? null : oldMessage;
    }

    // Creates components on the left-side group of the status panel.
    private JPanel createLeftComponents()
    {
        JPanel p = new JPanel(new BorderLayout(10, 5));
        p.setOpaque(false);

        Object o = this.controller.getRegisteredObject(Constants.FEATURE_GAZETTEER_PANEL);
        if (o != null && o instanceof FeaturePanel)
            p.add(((FeaturePanel) o).getJPanel(), BorderLayout.WEST);

        return p;
    }

    // Creates components in the center group of the status panel.
    private JPanel createCenterComponents()
    {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 10, 0, 0));

        this.statusMessageLabel = new JLabel("");
        this.statusMessageLabel.setOpaque(false);
        this.statusMessageLabel.setForeground(this.panelTextColor);
        p.add(this.statusMessageLabel, BorderLayout.WEST);

        return p;
    }

    // Creates components in the right-side group of the status panel.
    private JPanel createRightComponents()
    {
        JPanel p = new JPanel(new BorderLayout(20, 5));
        p.setOpaque(false);

        p.add(makeClock(), BorderLayout.EAST);
        p.add(makeNetworkLabel(), BorderLayout.WEST);

        return p;
    }

    private JLabel makeNetworkLabel()
    {
        NetworkActivitySignal nas = this.controller.getNetworkActivitySignal();
        return nas != null ? nas.getLabel() : new JLabel("");
    }

    private JComponent makeClock()
    {
        final JLabel label =
            new JLabel(SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date()));

        label.setOpaque(false);
        label.setForeground(this.panelTextColor);
        label.setToolTipText("Time on this computer");

        Timer timer = new Timer(1000, new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                label.setText(
                    SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date()));
            }
        });
        timer.start();

        return label;
    }

    protected Object lastSelectedObject;

    // Monitors World Wind select events and if the object selected has a status-bar message attached to it, displays
    // that message in the status bar. If there is no status-bark message attached to the selected object but there is
    // an external link attached, displays the external-link string.
    public void selected(SelectEvent event)
    {
        if (event.isRollover())
        {
            if (this.lastSelectedObject == event.getTopObject())
                return; // same thing selected

            // Clear status area
            if (this.lastSelectedObject != null)
            {
                this.controller.setStatusMessage(null);
                this.lastSelectedObject = null;
            }

            // Show message in status area if a message is specified
            if (event.getTopObject() != null && event.getTopObject() instanceof AVList)
            {
                String statusMessage = (((AVList) event.getTopObject()).getStringValue(Constants.STATUS_BAR_MESSAGE));
                if (statusMessage == null)
                    statusMessage = (((AVList) event.getTopObject()).getStringValue(AVKey.EXTERNAL_LINK));

                if (statusMessage != null)
                {
                    this.lastSelectedObject = event.getTopObject();
                    this.controller.setStatusMessage(statusMessage);
                }
            }
        }
    }
}

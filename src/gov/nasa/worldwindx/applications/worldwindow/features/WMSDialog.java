/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.applications.worldwindow.core.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.URISyntaxException;

/**
 * @author tag
 * @version $Id: WMSDialog.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSDialog extends AbstractFeatureDialog
{
    protected static final String FEATURE_TITLE = "WMS Servers...";
    protected static final String ICON_PATH = "gov/nasa/worldwindx/applications/worldwindow/images/wms-64x64.png";

    protected JTabbedPane tabbedPane = new JTabbedPane();

    public WMSDialog(Registry registry)
    {
        super(FEATURE_TITLE, Constants.FEATURE_WMS_DIALOG, ICON_PATH, registry);
    }

    @Override
    public boolean isTwoState()
    {
        return true;
    }

    @Override
    public boolean isOn()
    {
        return this.dialog != null && this.dialog.isVisible();
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);

        WWMenu fileMenu = (WWMenu) this.getController().getRegisteredObject(Constants.FILE_MENU);
        if (fileMenu != null)
            fileMenu.addMenu(this.getFeatureID());

        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.setOpaque(false);

        this.tabbedPane.add(new JPanel());
        this.tabbedPane.setTitleAt(0, "+"); // this tab is just a button for adding servers/panels
        this.tabbedPane.setToolTipTextAt(0, "Connect to WMS Server");

        this.tabbedPane.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent changeEvent)
            {
                if (tabbedPane.getSelectedIndex() == 0)
                {
                    addNewPanel(tabbedPane); // Add new panel when '+' is selected
                }
            }
        });

        // Add an initial panel to the tabbed pane
        this.addNewPanel(this.tabbedPane);
        tabbedPane.setSelectedIndex(1);

        this.setTaskComponent(this.tabbedPane);
        this.setLocation(SwingConstants.CENTER, SwingConstants.CENTER);
        this.getJDialog().setResizable(true);

        JButton deleteButton = new JButton(
            ImageLibrary.getIcon("gov/nasa/worldwindx/applications/worldwindow/images/delete-20x20.png"));
        deleteButton.setToolTipText("Remove Server");
        deleteButton.setOpaque(false);
        deleteButton.setBackground(new Color(0, 0, 0, 0));
        deleteButton.setBorderPainted(false);
        deleteButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                deleteCurrentPanel();
            }
        });
        deleteButton.setEnabled(true);
        this.insertLeftDialogComponent(deleteButton);
//
//        JButton cancelButton = new JButton("Cancel");
//        cancelButton.setToolTipText("Cancel capabilities retrieval from server");
//        cancelButton.setOpaque(false);
//        cancelButton.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent actionEvent)
//            {
//                cancelCurrentRetrieval();
//            }
//        });
//        this.insertRightDialogComponent(cancelButton);

        this.setTitle("WMS Servers");
        this.dialog.validate();
        this.dialog.pack();
    }
//
//    protected void cancelCurrentRetrieval()
//    {
//        JComponent tabPane = (JComponent) tabbedPane.getSelectedComponent();
//        if (tabPane == null)
//            return;
//
//        WMSPanel wmsPanel = (WMSPanel) tabPane.getClientProperty(Constants.FEATURE_OWNER_PROPERTY);
//        if (wmsPanel != null)
//            wmsPanel.cancel();
//    }

    protected void deleteCurrentPanel()
    {
        JComponent tabPane = (JComponent) tabbedPane.getSelectedComponent();
        if (tabPane == null)
            return;

        WMSPanel wmsPanel = (WMSPanel) tabPane.getClientProperty(Constants.FEATURE_OWNER_PROPERTY);

        if (tabbedPane.getTabCount() > 2) // actually remove the tab only if there is more than one (plus the "+" tab)
            tabbedPane.remove(tabPane);
        else
            tabbedPane.setTitleAt(1, "New Server");

        if (wmsPanel != null)
            wmsPanel.clearPanel();
    }

    protected WMSPanel addNewPanel(final JTabbedPane tabPane)
    {
        final WMSPanel wmsPanel = new WMSPanel(null); // the null indicates not to register the panel
        wmsPanel.initialize(this.controller);
        wmsPanel.getJPanel().putClientProperty("WMS_PANEL", wmsPanel);
        tabPane.putClientProperty(wmsPanel.getURLString(), wmsPanel);

        tabPane.addTab("New Server", wmsPanel.getJPanel());
        tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
        tabPane.setToolTipTextAt(tabbedPane.getSelectedIndex(), "Server WMS Contents");

        wmsPanel.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (evt.getPropertyName().equals("NewServer"))
                {
                    String serverLocation = (String) evt.getNewValue();

                    if (WWUtil.isEmpty(serverLocation))
                        return;
//
//                    // Check to see if it's already open.
//                    for (int i = 0; i < tabbedPane.getTabCount(); i++)
//                    {
//                        JPanel jp = (JPanel) tabbedPane.getTabComponentAt(i);
//                        if (jp != null)
//                        {
//                            WMSPanel wp = (WMSPanel) jp.getClientProperty("WMS_PANEL");
//                            if (wp != null && wp.getURLString().equalsIgnoreCase(serverLocation))
//                            {
//                                tabbedPane.setSelectedIndex(i); // make it the visible one
//                                return;
//                            }
//                        }
//                    }

                    try
                    {

                        addNewPanel(tabPane).contactWMSServer(serverLocation);
                    }
                    catch (URISyntaxException e)
                    {
                        e.printStackTrace(); // TODO
                    }
                }
            }
        });

        return wmsPanel;
    }
}

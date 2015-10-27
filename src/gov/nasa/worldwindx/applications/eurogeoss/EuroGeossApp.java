/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.eurogeoss;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.*;
import gov.nasa.worldwindx.examples.layermanager.LayerAndElevationManagerPanel;
import gov.nasa.worldwindx.examples.util.*;

import javax.swing.*;
import java.awt.*;

/**
 * An app that demonstrates searching and displaying WMS server contents from the EuroGEOSS data broker catalog at
 * http://eurogeoss.eu
 *
 * @author dcollins
 * @version $Id: EuroGeossApp.java 1586 2013-09-06 18:03:47Z dcollins $
 */
public class EuroGeossApp
{
    // Most of this code was taken from ApplicationTemplate.

    protected static final String EUROGEOSS_SERVICE_TITLE = "EuroGEOSS Catalog";
    protected static final String EUROGEOSS_SERVICE_URL = "http://23.21.170.207/geodab-dswg/services/cswisogeo";
    protected static final String NEO_SERVICE_TITLE = "NASA Earth Observations (NEO) WMS";
    protected static final String NEO_SERVICE_URL = "http://neowms.sci.gsfc.nasa.gov/wms/wms";

    public static class AppPanel extends JPanel
    {
        protected WorldWindow wwd;
        protected StatusBar statusBar;
        protected ToolTipController toolTipController;
        protected HighlightController highlightController;

        public AppPanel()
        {
            super(new BorderLayout());

            this.wwd = new WorldWindowGLCanvas();
            ((Component) this.wwd).setPreferredSize(new Dimension(1200, 800));

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            // Setup a select listener for the worldmap click-and-go feature
            this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));

            this.add((Component) this.wwd, BorderLayout.CENTER);
            this.statusBar = new StatusBar();
            this.add(statusBar, BorderLayout.PAGE_END);
            this.statusBar.setEventSource(wwd);

            // Add controllers to manage highlighting and tool tips.
            this.toolTipController = new ToolTipController(this.wwd, AVKey.DISPLAY_NAME, null);
            this.highlightController = new HighlightController(this.wwd, SelectEvent.ROLLOVER);
        }
    }

    // This is the application's main frame.
    public static class AppFrame extends JFrame
    {
        protected AppPanel wwjPanel;
        protected JTabbedPane tabbedPane;
        protected CatalogPanel catalogPanel;
        protected LayerAndElevationManagerPanel layerManagerPanel;

        public AppFrame()
        {
            initialize();

            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void initialize()
        {
            // Create the WorldWindow.
            this.wwjPanel = new AppPanel();
            this.tabbedPane = new JTabbedPane();
            this.catalogPanel = new CatalogPanel(EUROGEOSS_SERVICE_URL, getWwd());
            this.layerManagerPanel = new LayerAndElevationManagerPanel(getWwd());

            this.tabbedPane = new JTabbedPane();
            this.tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // top, left, bottom, right
            this.tabbedPane.add(EUROGEOSS_SERVICE_TITLE, this.catalogPanel);
            this.addWMSLayersTab(NEO_SERVICE_TITLE, NEO_SERVICE_URL);
            this.tabbedPane.add("Layers", this.layerManagerPanel);
            this.getContentPane().setLayout(new BorderLayout(0, 0)); // hgap, vgap
            this.getContentPane().add(this.tabbedPane, BorderLayout.WEST);
            this.getContentPane().add(this.wwjPanel, BorderLayout.CENTER);

            // Create and install the view controls layer and register a controller for it with the World Window.
            ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
            ApplicationTemplate.insertBeforeCompass(getWwd(), viewControlsLayer);
            this.getWwd().addSelectListener(new ViewControlsSelectListener(this.getWwd(), viewControlsLayer));

            // Search the layer list for layers that are also select listeners and register them with the World
            // Window. This enables interactive layers to be included without specific knowledge of them here.
            for (Layer layer : this.getWwd().getModel().getLayers())
            {
                if (layer instanceof SelectListener)
                {
                    this.getWwd().addSelectListener((SelectListener) layer);
                }
            }

            this.pack();

            // Center the application on the screen.
            WWUtil.alignComponent(null, this, AVKey.CENTER);
            this.setResizable(true);
        }

        protected void addWMSLayersTab(String title, String serviceUrl)
        {
            try
            {
                WMSLayersPanel panel = new WMSLayersPanel(getWwd(), serviceUrl, new Dimension(500, 0));
                this.tabbedPane.add(title, panel);
            }
            catch (Exception e)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE, "Unable to add WMS server " + serviceUrl, e);
            }
        }

        public WorldWindow getWwd()
        {
            return this.wwjPanel.wwd;
        }
    }

    static
    {
        System.setProperty("java.net.useSystemProxies", "true");
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
        else if (Configuration.isWindowsOS())
        {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }

    public static AppFrame start(String appName, Class appFrameClass)
    {
        if (Configuration.isMacOS() && appName != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try
        {
            final AppFrame frame = (AppFrame) appFrameClass.newInstance();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                }
            });

            return frame;
        }
        catch (Exception e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, "Exception at application start", e);
            return null;
        }
    }

    public static void main(String[] args)
    {
        start("World Wind EuroGEOSS Client", AppFrame.class);
    }
}

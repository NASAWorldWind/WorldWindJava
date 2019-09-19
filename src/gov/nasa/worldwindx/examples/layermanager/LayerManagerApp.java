/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.layermanager;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;

/**
 * Shows how to instantiate the new layer manager. This class merely instantiates and positions it. The layer manager,
 * itself, handles all the logic of identifying which layers are visible and enabling changes to their Z order.
 *
 * @author tag
 * @version $Id: LayerManagerApp.java 1179 2013-02-15 17:47:37Z tgaskins $
 */
public class LayerManagerApp
{
    // Most of this code was taken from ApplicationTemplate.

    public static class AppPanel extends JPanel
    {
        protected WorldWindow wwd;
        protected StatusBar statusBar;

        public AppPanel()
        {
            super(new BorderLayout());

            this.wwd = new WorldWindowGLCanvas();
            ((Component) this.wwd).setPreferredSize(new Dimension(1000, 600));

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            this.add((Component) this.wwd, BorderLayout.CENTER);
            this.statusBar = new StatusBar();
            this.add(statusBar, BorderLayout.PAGE_END);
            this.statusBar.setEventSource(wwd);
        }
    }

    // This is the application's main frame.
    public static class AppFrame extends JFrame
    {
        protected AppPanel wwjPanel;

        public AppFrame()
        {
            initialize();

            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void initialize()
        {
            // Create the WorldWindow.
            this.wwjPanel = new AppPanel();
            this.getContentPane().add(wwjPanel, BorderLayout.CENTER);

            // Instantiate and position the layer manager panel. This is really the only layer-manager specific code
            // in this example.
            LayerAndElevationManagerPanel layerManagerPanel = new LayerAndElevationManagerPanel(this.getWwd());
            JPanel outerPanel = new JPanel(new BorderLayout(10, 10));
            outerPanel.add(layerManagerPanel, BorderLayout.CENTER);
            this.getContentPane().add(outerPanel, BorderLayout.WEST);

            this.pack();

            // Center the application on the screen.
            WWUtil.alignComponent(null, this, AVKey.CENTER);
            this.setResizable(true);
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
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
        else if (Configuration.isWindowsOS())
        {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }

    public static AppFrame start(String appName, Class<?> appFrameClass)
    {
        if (Configuration.isMacOS() && appName != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try
        {
            final AppFrame frame = (AppFrame) appFrameClass.getConstructor().newInstance();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        start("Layer Manager", AppFrame.class);
    }
}

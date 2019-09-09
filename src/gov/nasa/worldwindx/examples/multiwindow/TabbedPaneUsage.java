/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.multiwindow;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;

/**
 * This class illustrates how to use multiple WorldWind windows with a {@link JTabbedPane}.
 * <p>
 * Applications using multiple WorldWind windows simultaneously should instruct WorldWind to share OpenGL and other
 * resources among those windows. Most WorldWind classes are designed to be shared across {@link WorldWindow} objects
 * and will be shared automatically. But OpenGL resources are not automatically shared. To share them, a reference to a
 * previously created WorldWindow must be specified as a constructor argument for subsequently created WorldWindows.
 * <p>
 * Most WorldWind {@link gov.nasa.worldwind.globes.Globe} and {@link gov.nasa.worldwind.layers.Layer} objects can be shared among WorldWindows. Those that cannot be shared
 * have an operational dependency on the WorldWindow they're associated with. An example is the {@link
 * gov.nasa.worldwind.layers.ViewControlsLayer} layer for on-screen navigation. Because this layer responds to input events within a specific
 * WorldWindow, it is not sharable. Refer to the WorldWind Overview page for a list of layers that cannot be shared.
 * // TODO: include the reference to overview.html.
 *
 * @version $Id: TabbedPaneUsage.java 1853 2014-02-28 19:28:23Z tgaskins $
 */
public class TabbedPaneUsage extends JFrame
{
    private static class WWPanel extends JPanel
    {
        WorldWindowGLCanvas wwd;

        public WWPanel(WorldWindowGLCanvas shareWith, int width, int height)
        {
            // To share resources among WorldWindows, pass the first WorldWindow to the constructor of the other
            // WorldWindows.
            this.wwd = shareWith != null ? new WorldWindowGLCanvas(shareWith) : new WorldWindowGLCanvas();
            this.wwd.setSize(new java.awt.Dimension(width, height));

            this.setLayout(new BorderLayout(5, 5));
            this.add(this.wwd, BorderLayout.CENTER);
            this.setOpaque(false);

            StatusBar statusBar = new StatusBar();
            statusBar.setEventSource(wwd);
            this.add(statusBar, BorderLayout.SOUTH);
        }
    }

    public TabbedPaneUsage()
    {
        try
        {
            // Create the application frame and the tabbed pane and add the pane to the frame.
            JTabbedPane tabbedPanel = new JTabbedPane();
            this.add(tabbedPanel, BorderLayout.CENTER);

            // Create the first WorldWindow and add it to the tabbed panel.
            WWPanel wwpA = new WWPanel(null, 600, 600);
            tabbedPanel.add(wwpA, "WorldWindow A");

            // Create the Model, starting with the Globe.
            Globe earth = new Earth();

            // Create layers that both WorldWindows can share.
            Layer[] layers = new Layer[]
                {
                    new StarsLayer(),
                    new CompassLayer(),
                    new BMNGWMSLayer(),
                    new LandsatI3WMSLayer(),
                };

            // Create two models and pass them the shared layers.
            Model modelForWindowA = new BasicModel();
            modelForWindowA.setGlobe(earth);
            modelForWindowA.setLayers(new LayerList(layers));
            wwpA.wwd.setModel(modelForWindowA);

            Model modelForWindowB = new BasicModel();
            modelForWindowB.setGlobe(new Earth());
            modelForWindowB.setLayers(new LayerList(layers));

            // Add view control layers, which the WorldWindows cannot share.
            ViewControlsLayer viewControlsA = new ViewControlsLayer();
            wwpA.wwd.getModel().getLayers().add(viewControlsA);
            wwpA.wwd.addSelectListener(new ViewControlsSelectListener(wwpA.wwd, viewControlsA));

            ViewControlsLayer viewControlsB = new ViewControlsLayer();

            // Add the tabbed panel to the frame.
            this.add(tabbedPanel, BorderLayout.CENTER);

            // Position and display the frame. It's essential to do this before creating the second WorldWindow. This
            // first one must be visible in order for the second one to share its OpenGL resources.
            this.setTitle("WorldWind Multi-Window Tabbed Pane");
            this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER); // Center the application on the screen.
            this.setResizable(true);
            this.setVisible(true);

            // Now that the first WorldWindow is visible, create the second one.
            WWPanel wwpB = new WWPanel(wwpA.wwd, wwpA.getWidth(), wwpA.getHeight());
            tabbedPanel.add(wwpB, "WorldWindow B");
            wwpB.wwd.setModel(modelForWindowB);
            wwpB.wwd.getModel().getLayers().add(viewControlsB);
            wwpB.wwd.addSelectListener(new ViewControlsSelectListener(wwpB.wwd, viewControlsB));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                new TabbedPaneUsage();
            }
        });
    }
}

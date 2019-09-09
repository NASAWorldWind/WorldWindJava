/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.multiwindow;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;

/**
 * This class illustrates how to display a globe, and in a separate window display another globe with a visualization of
 * the view volume in the main globe window.
 * <p>
 * Applications using multiple WorldWind windows simultaneously should instruct WorldWind to share OpenGL and other
 * resources among those windows. Most WorldWind classes are designed to be shared across {@link WorldWindow} objects
 * and will be shared automatically. But OpenGL resources are not automatically shared. To share them, a reference to a
 * previously created WorldWindow must be specified as a constructor argument for subsequently created WorldWindows.
 * <p>
 * Most WorldWind {@link gov.nasa.worldwind.globes.Globe} and {@link gov.nasa.worldwind.layers.Layer} objects can be
 * shared among WorldWindows. Those that cannot be shared have an operational dependency on the WorldWindow they're
 * associated with. An example is the {@link gov.nasa.worldwind.layers.ViewControlsLayer} layer for on-screen
 * navigation. Because this layer responds to input events within a specific WorldWindow, it is not sharable. Refer to
 * the WorldWind Overview page for a list of layers that cannot be shared.
 *
 * @author tag
 * @version $Id: ViewViewVolume.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ViewViewVolume extends JFrame
{
    static
    {
        if (gov.nasa.worldwind.Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Multi-Window Analysis");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }

    protected WWPanel wwp;

    public ViewViewVolume()
    {
        this.getContentPane().setLayout(new BorderLayout(5, 5));

        this.wwp = new WWPanel(new Dimension(650, 500));
        this.getContentPane().add(wwp);

        this.pack();
        this.setResizable(true);

        WWUtil.alignComponent(null, this, AVKey.CENTER);

        this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }

    protected static class WWPanel extends JPanel
    {
        WorldWindowGLCanvas wwd;

        public WWPanel(Dimension size)
        {
            this.wwd = new WorldWindowGLCanvas();
            this.wwd.setSize(size);

            this.wwd.setModel((Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME));

            this.setLayout(new BorderLayout(5, 5));
            this.add(this.wwd, BorderLayout.CENTER);

            StatusBar statusBar = new StatusBar();
            statusBar.setEventSource(wwd);
            this.add(statusBar, BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                // Make a WorldWindow to observe
                ViewViewVolume vvv = new ViewViewVolume();
                vvv.setVisible(true);

                // Make the observer
                ViewVolumeViewer vvViewer = new ViewVolumeViewer(vvv.wwp.wwd, new Dimension(500, 500));
                Point p = vvv.getLocation();
                vvViewer.setLocation(p.x + vvv.getWidth() + 20, p.y);
                vvViewer.setVisible(true);
            }
        });
    }
}

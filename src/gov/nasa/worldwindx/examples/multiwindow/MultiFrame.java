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

package gov.nasa.worldwindx.examples.multiwindow;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.util.StatusBar;

import javax.swing.*;
import java.awt.*;

/**
 * This example shows how to create two WorldWindows, each in its own JFrame. The WorldWindows share a globe and some
 * layers.
 * <p>
 * Applications using multiple WorldWind windows simultaneously should instruct WorldWind to share OpenGL and other
 * resources among those windows. Most WorldWind classes are designed to be shared across {@link WorldWindow} objects
 * and are shared automatically. But OpenGL resources are not automatically shared. To share them, a reference to a
 * previously created WorldWindow must be specified as a constructor argument for subsequently created WorldWindows.
 * <p>
 * Most WorldWind {@link gov.nasa.worldwind.globes.Globe} and {@link gov.nasa.worldwind.layers.Layer} objects can be
 * shared among WorldWindows. Those that cannot be shared have an operational dependency on the WorldWindow they're
 * associated with. An example is the {@link gov.nasa.worldwind.layers.ViewControlsLayer} layer for on-screen
 * navigation. Because this layer responds to input events within a specific WorldWindow, it is not sharable. Refer to
 * the WorldWind Overview page for a list of layers that cannot be shared.
 * // TODO: include the reference to overview.html.
 *
 * @author tag
 * @version $Id: MultiFrame.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MultiFrame
{
    // A panel to hold a WorldWindow and status bar.
    private static class WWPanel extends JPanel
    {
        private WorldWindowGLCanvas wwd;

        public WWPanel(WorldWindowGLCanvas shareWith, int width, int height, Model model)
        {
            // To share resources among WorldWindows, pass the first WorldWindow to the constructor of the other
            // WorldWindows.
            this.wwd = shareWith != null ? new WorldWindowGLCanvas(shareWith) : new WorldWindowGLCanvas();
            this.wwd.setSize(new java.awt.Dimension(width, height));
            this.wwd.setModel(model);

            this.setLayout(new BorderLayout(5, 5));
            this.add(this.wwd, BorderLayout.CENTER);

            StatusBar statusBar = new StatusBar();
            statusBar.setEventSource(wwd);
            this.add(statusBar, BorderLayout.SOUTH);
        }
    }

    // A JFrame to hold one WorldWindow panel. Multiple of these are created in main below.
    private static class CanvasFrame extends javax.swing.JFrame
    {
        private WWPanel wwp;

        public CanvasFrame(WorldWindow shareWith, Model model, String side)
        {
            this.getContentPane().setLayout(new BorderLayout(5, 5));

            this.wwp = new WWPanel((WorldWindowGLCanvas) shareWith, 500, 500, model);
            this.getContentPane().add(wwp, BorderLayout.CENTER);

            this.pack();

            java.awt.Dimension wwSize = this.getPreferredSize();
            wwSize.setSize(wwSize.getWidth(), 1.1 * wwSize.getHeight());
            this.setSize(wwSize);

            // Position the windows side-by-side.
            java.awt.Dimension parentSize;
            java.awt.Point parentLocation = new java.awt.Point(0, 0);
            parentSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            int x = parentLocation.x + (parentSize.width / 2 + (side.equals("left") ? -wwSize.width : 20));
            int y = parentLocation.y + (parentSize.height - wwSize.height) / 2;
            this.setLocation(x, y);
            this.setResizable(true);
        }
    }

    public static void main(String[] args)
    {
        try
        {
            // Create a Model for each window, starting with the Globe they share.
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

            Model modelForWindowB = new BasicModel();
            modelForWindowB.setGlobe(earth);
            modelForWindowB.setLayers(new LayerList(layers));

            // Create two frames and give each their own model.
            CanvasFrame frameA = new CanvasFrame(null, modelForWindowA, "left");
            frameA.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frameA.setTitle("Frame A");
            frameA.wwp.wwd.setModel(modelForWindowA);
            frameA.setVisible(true);

            // When creating the second frame, specify resource sharing with the first one.
            CanvasFrame frameB = new CanvasFrame(frameA.wwp.wwd, modelForWindowB, "right");
            frameB.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frameB.setTitle("Frame B");
            frameB.wwp.wwd.setModel(modelForWindowB);
            frameB.setVisible(true);

            // Add view control layers, which the WorldWindows cannnot share.
            ViewControlsLayer viewControlsA = new ViewControlsLayer();
            frameA.wwp.wwd.getModel().getLayers().add(viewControlsA);
            frameA.wwp.wwd.addSelectListener(new ViewControlsSelectListener(frameA.wwp.wwd, viewControlsA));

            ViewControlsLayer viewControlsB = new ViewControlsLayer();
            frameB.wwp.wwd.getModel().getLayers().add(viewControlsB);
            frameB.wwp.wwd.addSelectListener(new ViewControlsSelectListener(frameB.wwp.wwd, viewControlsB));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.multiwindow;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This class illustrates how to use multiple WorldWind windows with a {@link CardLayout} layer manager.
 * <p>
 * Applications using multiple WorldWind windows simultaneously should instruct WorldWind to share OpenGL and other
 * resources among those windows. Most WorldWind classes are designed to be shared across {@link WorldWindow} objects
 * and will be shared automatically. But OpenGL resources are not automatically shared. To share them, a reference to a
 * previously created WorldWindow must be specified as a constructor argument for subsequently created WorldWindows.
 * <p>
 * Most WorldWind {@link Globe} and {@link Layer} objects can be shared among WorldWindows. Those that cannot be shared
 * have an operational dependency on the WorldWindow they're associated with. An example is the {@link
 * ViewControlsLayer} layer for on-screen navigation. Because this layer responds to input events within a specific
 * WorldWindow, it is not sharable. Refer to the WorldWind Overview page for a list of layers that cannot be shared.
 * // TODO: include the reference to overview.html.
 *
 * @version $Id: CardLayoutUsage.java 1853 2014-02-28 19:28:23Z tgaskins $
 */
public class CardLayoutUsage extends JFrame
{
    private static class WWPanel extends JPanel // A class to encapsulate a WorldWindow that shares resources.
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

            StatusBar statusBar = new StatusBar();
            statusBar.setBorder(new EmptyBorder(5, 5, 5, 5));
            statusBar.setEventSource(wwd);
            this.add(statusBar, BorderLayout.SOUTH);
        }
    }

    private WWPanel wwpA;
    private WWPanel wwpB;

    public CardLayoutUsage()
    {
        try
        {
            // Create an inner panel and the CardLayout manager.
            JPanel cardPanel = new JPanel();
            cardPanel.setLayout(new CardLayout());

            // Create the first WorldWindow and add it to the card panel.
            this.wwpA = new WWPanel(null, 600, 600);

            // Add the first WorldWindow to the card panel.
            cardPanel.add(wwpA, "WorldWindow A");

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
            modelForWindowB.setGlobe(earth);
            modelForWindowB.setLayers(new LayerList(layers));

            // Add view control layers, which the WorldWindows cannot share.
            ViewControlsLayer viewControlsA = new ViewControlsLayer();
            wwpA.wwd.getModel().getLayers().add(viewControlsA);
            wwpA.wwd.addSelectListener(new ViewControlsSelectListener(wwpA.wwd, viewControlsA));

            ViewControlsLayer viewControlsB = new ViewControlsLayer();

            // Add the card panel to the frame.
            this.add(cardPanel, BorderLayout.CENTER);
            this.add(this.makeControlPanel((CardLayout) cardPanel.getLayout(), cardPanel), BorderLayout.SOUTH);

            // Position and display the frame. It's essential to do this before creating the second WorldWindow. This
            // first one must be visible in order for the second one to share its OpenGL resources.
            this.setTitle("WorldWind Multi-Window CardLayout");
            this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER); // Center the application on the screen.
            this.setResizable(true);
            this.setVisible(true);

            // Now that the first WorldWindow is visible, create the second one.
            this.wwpB = new WWPanel(wwpA.wwd, wwpA.getWidth(), wwpA.getHeight());
            cardPanel.add(wwpB, "WorldWindow B");
            wwpB.wwd.setModel(modelForWindowB);
            wwpB.wwd.getModel().getLayers().add(viewControlsB);
            wwpB.wwd.addSelectListener(new ViewControlsSelectListener(wwpB.wwd, viewControlsB));

            wwpA.wwd.redraw();
            wwpB.wwd.redraw();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private JPanel makeControlPanel(final CardLayout cardLayout, final JPanel cardLayoutParent)
    {
        final JButton buttonA = new JButton("WorldWindow A");
        final JButton buttonB = new JButton(" WorldWindow B");

        buttonA.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                cardLayout.show(cardLayoutParent, "WorldWindow A");
                buttonA.setEnabled(false);
                buttonB.setEnabled(true);
                wwpA.wwd.redraw();
            }
        });

        buttonB.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                cardLayout.show(cardLayoutParent, "WorldWindow B");
                buttonA.setEnabled(true);
                buttonB.setEnabled(false);
                wwpB.wwd.redraw();
            }
        });

        buttonA.setEnabled(false);

        JPanel panel = new JPanel(new GridLayout(1, 0));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(buttonA);
        panel.add(buttonB);

        return panel;
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                new CardLayoutUsage();
            }
        });
    }
}

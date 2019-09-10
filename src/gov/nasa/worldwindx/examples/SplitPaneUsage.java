/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.util.StatusBar;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Illustrates how to use WorldWind within a Swing JSplitPane. Doing so is mostly straightforward, but in order to work
 * around a Swing bug the WorldWindow must be placed within a JPanel and that JPanel's minimum preferred size must be
 * set to zero (both width and height). See the code that does this in the first few lines of the AppPanel constructor
 * below.
 * <p>
 * This example also illustrates another bug in Swing that does not have a known workaround: the WorldWindow does not
 * resize when a vertical split-pane's one-touch-expand widget is clicked if that split-pane contains a horizontal
 * split-plane that contains the WorldWindow. If the one-touch widget is clicked on the bottom pane of this example,
 * that pane will expand to the full height of the window but the WorldWindow will not change size and will display on
 * top of the expanded pane. (The horizontal split pane's one-touch behavior works correctly.) If the panes are
 * rearranged so that the WorldWindow and the bottom panel are in one vertical split pane, and that split pane is the
 * right component of the horizontal split pane containing the layer panel, then the one-touch widgets work correctly
 * for both JSplitPanes. This bug is related only to the one-touch widget. Moving the vertical split-pane interactively
 * via the split-pane's handle works correctly.
 *
 * @author tag
 * @version $Id: SplitPaneUsage.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SplitPaneUsage
{
    public static class AppPanel extends JPanel
    {
        private WorldWindowGLCanvas wwd;

        // Constructs a JPanel to hold the WorldWindow
        public AppPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            super(new BorderLayout());

            // Create the WorldWindow and set its preferred size.
            this.wwd = new WorldWindowGLCanvas();
            this.wwd.setPreferredSize(canvasSize);

            // THIS IS THE TRICK: Set the panel's minimum size to (0,0);
            this.setMinimumSize(new Dimension(0, 0));

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            // Setup a select listener for the worldmap click-and-go feature
            this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));

            // Add the WorldWindow to this JPanel.
            this.add(this.wwd, BorderLayout.CENTER);

            // Add the status bar if desired.
            if (includeStatusBar)
            {
                StatusBar statusBar = new StatusBar();
                this.add(statusBar, BorderLayout.PAGE_END);
                statusBar.setEventSource(wwd);
            }
        }
    }

    private static class AppFrame extends JFrame
    {
        private Dimension canvasSize = new Dimension(800, 600); // the desired WorldWindow size

        public AppFrame()
        {
            // Create the WorldWindow.
            final AppPanel wwjPanel = new AppPanel(this.canvasSize, true);
            LayerPanel layerPanel = new LayerPanel(wwjPanel.wwd);

            // Create a horizontal split pane containing the layer panel and the WorldWindow panel.
            JSplitPane horizontalSplitPane = new JSplitPane();
            horizontalSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            horizontalSplitPane.setLeftComponent(layerPanel);
            horizontalSplitPane.setRightComponent(wwjPanel);
            horizontalSplitPane.setOneTouchExpandable(true);
            horizontalSplitPane.setContinuousLayout(true); // prevents the pane's being obscured when expanding right

            // Create a panel for the bottom component of a vertical split-pane.
            JPanel bottomPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Bottom Panel");
            label.setBorder(new EmptyBorder(10, 10, 10, 10));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            bottomPanel.add(label, BorderLayout.CENTER);

            // Create a vertical split-pane containing the horizontal split plane and the button panel.
            JSplitPane verticalSplitPane = new JSplitPane();
            verticalSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            verticalSplitPane.setTopComponent(horizontalSplitPane);
            verticalSplitPane.setBottomComponent(bottomPanel);
            verticalSplitPane.setOneTouchExpandable(true);
            verticalSplitPane.setContinuousLayout(true);
            verticalSplitPane.setResizeWeight(1);

            // Add the vertical split-pane to the frame.
            this.getContentPane().add(verticalSplitPane, BorderLayout.CENTER);
            this.pack();

            // Center the application on the screen.
            Dimension prefSize = this.getPreferredSize();
            Dimension parentSize;
            java.awt.Point parentLocation = new java.awt.Point(0, 0);
            parentSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
            int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
            this.setLocation(x, y);
            this.setResizable(true);
        }
    }

    public static void main(String[] args)
    {
        start("WorldWind Split Pane Usage");
    }

    public static void start(String appName)
    {
        if (Configuration.isMacOS() && appName != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try
        {
            final AppFrame frame = new AppFrame();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

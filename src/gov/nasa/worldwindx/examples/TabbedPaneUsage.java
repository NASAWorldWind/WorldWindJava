/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * This example demonstrates the use of tabbed panes.
 *
 * @author tag
 * @version $Id: TabbedPaneUsage.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TabbedPaneUsage
{
    static
    {
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Tabbed Pane Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }

    public static class WWJPanel extends JPanel
    {
        protected WorldWindow wwd;
        protected StatusBar statusBar;

        public WWJPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            super(new BorderLayout());

            this.wwd = new WorldWindowGLCanvas();
            ((Component) this.wwd).setPreferredSize(canvasSize);

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);
            this.addMarkers();
            this.addShapes();

            this.add(((Component) this.wwd), BorderLayout.CENTER);
            if (includeStatusBar)
            {
                this.statusBar = new StatusBar();
                this.add(statusBar, BorderLayout.PAGE_END);
                this.statusBar.setEventSource(wwd);
            }
        }

        protected void addMarkers()
        {
            ArrayList<Marker> markers = new ArrayList<Marker>();

            MarkerAttributes attrs = new BasicMarkerAttributes(Material.YELLOW, BasicMarkerShape.CONE, 1d, 10, 5);
            Position position = Position.fromDegrees(40, -120);
            Marker marker = new BasicMarker(position, attrs);
            markers.add(marker);

            final MarkerLayer layer = new MarkerLayer();
            layer.setOverrideMarkerElevation(true);
            layer.setKeepSeparated(false);
            layer.setElevation(1000d);
            layer.setMarkers(markers);
            ApplicationTemplate.insertBeforePlacenames(this.wwd, layer);
        }

        protected void addShapes()
        {
            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.GREEN);
            attrs.setInteriorOpacity(0.7);
            attrs.setEnableLighting(true);
            attrs.setDrawInterior(true);
            attrs.setDrawOutline(false);

            // ********* sample  Cones  *******************

            // Cone with equal axes, ABSOLUTE altitude mode
            Cone cone3 = new Cone(Position.fromDegrees(42, -118, 80000), 100000, 50000);
            cone3.setAltitudeMode(WorldWind.ABSOLUTE);
            cone3.setAttributes(attrs);
            cone3.setVisible(true);
            cone3.setValue(AVKey.DISPLAY_NAME, "Cone with equal axes, ABSOLUTE altitude mode");
            layer.addRenderable(cone3);

            // Cone with equal axes, RELATIVE_TO_GROUND
            Cone cone4 = new Cone(Position.fromDegrees(37.5, -115, 50000), 50000, 50000, 50000);
            cone4.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            cone4.setAttributes(attrs);
            cone4.setVisible(true);
            cone4.setValue(AVKey.DISPLAY_NAME, "Cone with equal axes, RELATIVE_TO_GROUND altitude mode");
            layer.addRenderable(cone4);

            ApplicationTemplate.insertBeforePlacenames(this.wwd, layer);
        }
    }

    protected static int wwjPaneNumber = 1;

    public static void main(String[] args)
    {
        try
        {
            JFrame mainFrame = new JFrame();

            mainFrame.setTitle("WorldWind Tabbed Pane");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final JTabbedPane tabbedPane = new JTabbedPane();
            final WWJPanel wwjPanel = new WWJPanel(new Dimension(800, 600), true);
            final JPanel controlPanel = new JPanel();

            JButton detachButton = new JButton("Detach");
            detachButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    System.out.print("Removing tab...");
                    tabbedPane.removeTabAt(0);
                    System.out.println("Tab removed");
                }
            });

            JButton attachButton = new JButton("Attach");
            attachButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    System.out.print("Adding tab...");
                    tabbedPane.insertTab("WWJ Pane " + ++wwjPaneNumber, null, wwjPanel, "Reattach", 0);
                    System.out.println("Tab added");
                }
            });

            controlPanel.add(detachButton);
            controlPanel.add(attachButton);

            tabbedPane.add("WWJ Pane 1", wwjPanel);
            tabbedPane.add("Dummy Pane", controlPanel);

            mainFrame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
            mainFrame.pack();
            WWUtil.alignComponent(null, mainFrame, AVKey.CENTER);
            mainFrame.setVisible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

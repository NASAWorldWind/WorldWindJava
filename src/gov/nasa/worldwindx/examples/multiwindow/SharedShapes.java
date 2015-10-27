/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.multiwindow;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.render.airspaces.Box;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

/**
 * This example shows how to share World Wind shapes between two {@link WorldWindow} instances. In this example the two
 * World Wind windows share imagery layers and the layers representing each type of shape. Though this example uses a
 * different globe for each window, it is possible for the two windows to share the same {@link Globe} as is done in the
 * {@link MultiFrame} example.
 * <p/>
 * Applications using multiple World Wind windows simultaneously should instruct World Wind to share OpenGL and other
 * resources among those windows. Most World Wind classes are designed to be shared across WorldWindow objects and are
 * shared automatically. But OpenGL resources are not automatically shared. To share them, a reference to a previously
 * created WorldWindow must be specified as a constructor argument for subsequently created WorldWindows.
 * <p/>
 * Most World Wind Globe and {@link gov.nasa.worldwind.layers.Layer} objects can be shared among World Windows. Those
 * that cannot be shared have an operational dependency on the World Window they're associated with. An example is the
 * {@link gov.nasa.worldwind.layers.ViewControlsLayer} layer for on-screen navigation. Because this layer responds to
 * input events within a specific World Window, it is not sharable. Refer to the World Wind Overview page for a list of
 * layers that cannot be shared.
 *
 * @author dcollins
 * @version $Id: SharedShapes.java 2326 2014-09-17 22:35:45Z dcollins $
 */
public class SharedShapes
{
    protected static class WWPanel extends JPanel
    {
        protected WorldWindowGLCanvas wwd;
        protected HighlightController highlightController;

        public WWPanel(WorldWindow shareWith, Model model, Dimension canvasSize)
        {
            super(new BorderLayout(5, 5));

            this.wwd = shareWith != null ? new WorldWindowGLCanvas(shareWith) : new WorldWindowGLCanvas();
            if (canvasSize != null)
                this.wwd.setPreferredSize(canvasSize);
            this.wwd.setModel(model);
            this.add(this.wwd, BorderLayout.CENTER);

            StatusBar statusBar = new StatusBar();
            statusBar.setEventSource(this.wwd);
            this.add(statusBar, BorderLayout.SOUTH);

            this.highlightController = new HighlightController(this.wwd, SelectEvent.ROLLOVER);
        }

        public WorldWindow getWwd()
        {
            return this.wwd;
        }
    }

    protected static class SharedLayerPanel extends JPanel
    {
        protected JComponent layersComponent;

        public SharedLayerPanel(String title, Dimension preferredSize, Iterable<? extends Layer> layersIterable)
        {
            this.setLayout(new BorderLayout());
            this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createTitledBorder(title)));

            // Create a box that holds the controls for each layer.
            this.layersComponent = javax.swing.Box.createVerticalBox();
            this.layersComponent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            this.update(layersIterable);

            // Put the layer box in a scroll panel. We put the layer box in a dummy container to prevent the scroll
            // panel from stretching the vertical spacing between the layer controls.
            JPanel dummyPanel = new JPanel(new BorderLayout());
            dummyPanel.add(this.layersComponent, BorderLayout.NORTH);
            JScrollPane scrollPane = new JScrollPane(dummyPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            if (preferredSize != null)
                scrollPane.setPreferredSize(preferredSize);
            this.add(scrollPane, BorderLayout.CENTER);
        }

        public void update(Iterable<? extends Layer> layersIterable)
        {
            this.layersComponent.removeAll();

            if (layersIterable != null)
            {
                for (Layer layer : layersIterable)
                {
                    this.addLayer(layer);
                }
            }

            this.revalidate();
        }

        protected void addLayer(final Layer layer)
        {
            final JCheckBox jcb = new JCheckBox(layer.getName(), layer.isEnabled());
            jcb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    layer.setEnabled(jcb.isSelected());
                    layer.firePropertyChange(AVKey.LAYER, null, layer);
                }
            });

            this.layersComponent.add(jcb);
            this.layersComponent.add(javax.swing.Box.createVerticalStrut(5));
        }
    }

    protected static Layer makeAirspaceLayer()
    {
        RenderableLayer layer = new RenderableLayer();
        layer.setName("Airspaces");

        RandomShapeAttributes randomAttrs = new RandomShapeAttributes();
        AirspaceAttributes attrs = randomAttrs.nextAttributes().asAirspaceAttributes();

        Airspace airspace = new Orbit(LatLon.fromDegrees(37.5, -120), LatLon.fromDegrees(42.5, -120),
            Orbit.OrbitType.CENTER, 100000);
        airspace.setAltitudes(10000, 20000);
        airspace.setTerrainConforming(true);
        airspace.setAttributes(attrs);
        layer.addRenderable(airspace);

        airspace = new Curtain(Arrays.asList(
            LatLon.fromDegrees(37.5, -112.5), LatLon.fromDegrees(42.5, -112.5), LatLon.fromDegrees(37.5, -107.5)));
        airspace.setAltitudes(10000, 20000);
        airspace.setTerrainConforming(true);
        airspace.setAttributes(attrs);
        layer.addRenderable(airspace);

        airspace = new Polygon(Arrays.asList(
            LatLon.fromDegrees(37.5, -102.5), LatLon.fromDegrees(42.5, -102.5), LatLon.fromDegrees(37.5, -97.5)));
        airspace.setAltitudes(10000, 20000);
        airspace.setTerrainConforming(true);
        airspace.setAttributes(attrs);
        layer.addRenderable(airspace);

        airspace = new PolyArc(Arrays.asList(
            LatLon.fromDegrees(37.5, -92.5), LatLon.fromDegrees(42.5, -92.5), LatLon.fromDegrees(37.5, -87.5)),
            200000, Angle.fromDegrees(0), Angle.fromDegrees(90));
        airspace.setAltitudes(10000, 20000);
        airspace.setTerrainConforming(true);
        airspace.setAttributes(attrs);
        layer.addRenderable(airspace);

        airspace = new Cake(Arrays.asList(
            new Cake.Layer(LatLon.fromDegrees(40, -80), 100000, Angle.ZERO, Angle.ZERO, 10000, 20000),
            new Cake.Layer(LatLon.fromDegrees(40, -80), 50000, Angle.ZERO, Angle.ZERO, 20000, 30000),
            new Cake.Layer(LatLon.fromDegrees(40, -80), 25000, Angle.ZERO, Angle.ZERO, 30000, 40000)));
        airspace.setTerrainConforming(true);
        airspace.setAttributes(attrs);
        layer.addRenderable(airspace);

        airspace = new CappedCylinder(LatLon.fromDegrees(30, -120), 100000);
        airspace.setAltitudes(10000, 20000);
        airspace.setTerrainConforming(true);
        airspace.setAttributes(attrs);
        layer.addRenderable(airspace);

        airspace = new PartialCappedCylinder(LatLon.fromDegrees(30, -110), 100000,
            Angle.fromDegrees(30), Angle.fromDegrees(330));
        airspace.setAltitudes(10000, 20000);
        airspace.setTerrainConforming(true);
        airspace.setAttributes(attrs);
        layer.addRenderable(airspace);

        airspace = new SphereAirspace(LatLon.fromDegrees(30, -100), 100000);
        airspace.setAltitudes(10000, 20000);
        airspace.setTerrainConforming(true);
        airspace.setAttributes(attrs);
        layer.addRenderable(airspace);

        airspace = new TrackAirspace(Arrays.asList(
            new Box(LatLon.fromDegrees(27.5, -92.5), LatLon.fromDegrees(32.5, -92.5), 100000, 100000),
            new Box(LatLon.fromDegrees(32.5, -92.5), LatLon.fromDegrees(27.5, -87.5), 100000, 100000)));
        ((TrackAirspace) airspace).getLegs().get(0).setAltitudes(10000, 20000);
        ((TrackAirspace) airspace).getLegs().get(1).setAltitudes(20000, 30000);
        airspace.setTerrainConforming(true);
        airspace.setAttributes(attrs);
        layer.addRenderable(airspace);

        airspace = new Route(Arrays.asList(
            LatLon.fromDegrees(27.5, -82.5), LatLon.fromDegrees(32.5, -82.5), LatLon.fromDegrees(27.5, -77.5)),
            200000);
        airspace.setAltitudes(10000, 20000);
        airspace.setTerrainConforming(true);
        airspace.setAttributes(attrs);
        layer.addRenderable(airspace);

        return layer;
    }

    public static void main(String[] args)
    {
        Layer[] basicLayers = new Layer[]
            {
                new StarsLayer(),
                new CompassLayer(),
                new BMNGOneImage(),
                new BMNGWMSLayer(),
                new LandsatI3WMSLayer(),
            };

        Layer[] shapeLayers = new Layer[]
            {
                makeAirspaceLayer()
            };

        // Create the shared World Wind layers.
        Layer[] layers = new Layer[basicLayers.length + shapeLayers.length];
        System.arraycopy(basicLayers, 0, layers, 0, basicLayers.length);
        System.arraycopy(shapeLayers, 0, layers, basicLayers.length, shapeLayers.length);

        // Create separate models for each World Window.
        Model modelForWindowA = new BasicModel(new Earth(), new LayerList(layers));
        Model modelForWindowB = new BasicModel(new EarthFlat(), new LayerList(layers));

        // Create the first World Window.
        WWPanel panelA = new WWPanel(null, modelForWindowA, new Dimension(900, 900));

        // Create a layer panel that displays the layer list shared by both WorldWindows.
        SharedLayerPanel layerPanel = new SharedLayerPanel("Shared Shapes", new Dimension(200, 0),
            Arrays.asList(shapeLayers));

        // Create a box that arranges the layer panel and the World Wind windows horizontally, and assigns each the
        // appropriate amount of space for its preferred size.
        javax.swing.Box box = javax.swing.Box.createHorizontalBox();
        box.add(layerPanel);
        box.add(javax.swing.Box.createHorizontalStrut(5));
        box.add(panelA);

        // Create an application frame to display the two World Wind windows and the shared layer panel.
        JFrame appFrame = new JFrame("World Wind Shared Shapes");
        appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        appFrame.getContentPane().add(box, BorderLayout.CENTER);

        // Make the first World Window visible. This is essential in order to share OpenGL resources with the second
        // World Window created below.
        appFrame.setVisible(true);

        // Make the second World Window and tell it to share OpenGL resources with the first World Window.
        WWPanel panelB = new WWPanel(panelA.getWwd(), modelForWindowB, new Dimension(900, 900));
        box.add(javax.swing.Box.createHorizontalStrut(5));
        box.add(panelB);
        appFrame.pack();

        // Center the application frame on the screen and make it visible.
        WWUtil.alignComponent(null, appFrame, AVKey.CENTER);
    }
}

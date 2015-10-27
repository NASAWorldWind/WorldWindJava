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
import gov.nasa.worldwind.globes.projections.ProjectionSinusoidal;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.Earth.BMNGOneImage;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * This class illustrates how to display round and flat globes side by side.
 * <p/>
 * Applications using multiple World Wind windows simultaneously should instruct World Wind to share OpenGL and other
 * resources among those windows. Most World Wind classes are designed to be shared across {@link WorldWindow} objects
 * and will be shared automatically. But OpenGL resources are not automatically shared. To share them, a reference to a
 * previously created WorldWindow must be specified as a constructor argument for subsequently created WorldWindows.
 * <p/>
 * Most World Wind {@link Globe} and {@link Layer} objects can be shared among WorldWindows. Those that cannot be shared
 * have an operational dependency on the WorldWindow they're associated with. An example is the {@link
 * ViewControlsLayer} layer for on-screen navigation. Because this layer responds to input events within a specific
 * WorldWindow, it is not sharable. Refer to the World Wind Overview page for a list of layers that cannot be shared.
 *
 * @author tag
 * @version $Id: FlatAndRoundGlobes.java 2219 2014-08-11 21:39:44Z dcollins $
 */
public class FlatAndRoundGlobes
{
    public FlatAndRoundGlobes()
    {
        LayerList layers = this.makeCommonLayers();
        
        Model roundModel = this.makeModel(new Earth(), layers);
        Model flatModel = this.makeModel(new EarthFlat(), layers);
        ((EarthFlat) flatModel.getGlobe()).setProjection(new ProjectionSinusoidal());

        WWFrame roundFrame = new WWFrame(null, roundModel, "Round Globe", AVKey.LEFT_OF_CENTER);
//        WWFrame flatFrame = new WWFrame(null, flatModel, "Flat Globe", AVKey.RIGHT_OF_CENTER);
        WWFrame flatFrame = new WWFrame(roundFrame.wwPanel.wwd, flatModel, "Flat Globe", AVKey.RIGHT_OF_CENTER);

        this.addViewControlLayer(roundFrame);
        this.addViewControlLayer(flatFrame);

        roundFrame.wwPanel.wwd.getView().setEyePosition(new Position(START_LOCATION, 3e6));
        flatFrame.wwPanel.wwd.getView().setEyePosition(new Position(START_LOCATION, 3e6));

        roundFrame.setVisible(true);
        flatFrame.setVisible(true);
    }

    protected LayerList makeCommonLayers()
    {
        LayerList layerList = new LayerList();

        layerList.add(new BMNGOneImage());

        Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
        Layer layer = (Layer) factory.createFromConfigSource("config/Earth/BMNGWMSLayer2.xml", null);
        layer.setEnabled(true);
        layerList.add(layer);

        layerList.add(makePathLayer());
        layerList.add(makePolygonLayer());
        layerList.add(makeExtrudedPolygonLayer());

        return layerList;
    }

    protected Model makeModel(Globe globe, LayerList layers)
    {
        Model model = new BasicModel(globe, new LayerList(layers));

        // Add per-window layers
        model.getLayers().add(new CompassLayer());
        model.getLayers().add(new WorldMapLayer());
        model.getLayers().add(new ScalebarLayer());

        return model;
    }

    protected void addViewControlLayer(WWFrame wwf)
    {
        ViewControlsLayer layer = new ViewControlsLayer();
        wwf.wwPanel.wwd.getModel().getLayers().add(layer);
        wwf.wwPanel.wwd.addSelectListener(new ViewControlsSelectListener(wwf.wwPanel.wwd, layer));
    }

    protected static class WWFrame extends JFrame
    {
        protected Dimension canvasSize = new Dimension(800, 600);
        protected WWPanel wwPanel;

        public WWFrame(WorldWindowGLCanvas shareWith, Model model, String displayName, String position)
        {
            this.getContentPane().setLayout(new BorderLayout(5, 5));
            this.wwPanel = new WWPanel(shareWith, canvasSize, model);
            this.getContentPane().add(this.wwPanel, BorderLayout.CENTER);

            this.setTitle(displayName);
            WWUtil.alignComponent(null, this, position);
            this.setResizable(true);
            this.pack();

            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

    protected static class WWPanel extends JPanel
    {
        protected WorldWindowGLCanvas wwd;
        protected HighlightController highlightController;

        public WWPanel(WorldWindowGLCanvas shareWith, Dimension size, Model model)
        {
            this.wwd = shareWith != null ? new WorldWindowGLCanvas(shareWith) : new WorldWindowGLCanvas();
            this.wwd.setSize(size);
            this.wwd.setModel(model);

            this.setLayout(new BorderLayout(5, 5));
            this.add(this.wwd, BorderLayout.CENTER);

            StatusBar statusBar = new StatusBar();
            statusBar.setEventSource(wwd);
            this.add(statusBar, BorderLayout.SOUTH);

            this.highlightController = new HighlightController(this.wwd, SelectEvent.ROLLOVER);
//
//            wwd.getSceneController().getGLRuntimeCapabilities().setVertexBufferObjectEnabled(true);
        }
    }

    protected static final int NUM_PATHS = 200;
    protected static final int NUM_POSITIONS = 200;
    protected static final Angle PATH_LENGTH = Angle.fromDegrees(5);
    protected static final double PATH_HEIGHT = 1e3;
    protected static final LatLon START_LOCATION = LatLon.fromDegrees(48.86, 2.33);
    protected static final int ALTITUDE_MODE = WorldWind.RELATIVE_TO_GROUND;
    protected static final double LINE_WIDTH = 1d;

    protected Layer makePathLayer()
    {
        RenderableLayer layer = new RenderableLayer();
        layer.setName("Paths");
        this.makePaths(layer, new Position(START_LOCATION, PATH_HEIGHT), NUM_PATHS, PATH_LENGTH, NUM_POSITIONS);

        return layer;
    }

    protected void makePaths(RenderableLayer layer, Position origin, int numPaths, Angle length, int numPositions)
    {
        double dAngle = 360d / numPaths;

        for (int i = 0; i < numPaths; i++)
        {
            Angle heading = Angle.fromDegrees(i * dAngle);
            layer.addRenderable(this.makePath(origin, heading, length, numPositions));
        }

        System.out.printf("%d paths, each with %d positions\n", NUM_PATHS, NUM_POSITIONS);
    }

    protected Path makePath(Position startPosition, Angle heading, Angle length, int numPositions)
    {
        double dLength = length.radians / (numPositions - 1);
        java.util.List<Position> positions = new ArrayList<Position>(numPositions);

        for (int i = 0; i < numPositions - 1; i++)
        {
            LatLon ll = Position.greatCircleEndPosition(startPosition, heading, Angle.fromRadians(i * dLength));
            positions.add(new Position(ll, PATH_HEIGHT));
        }

        LatLon ll = Position.greatCircleEndPosition(startPosition, heading, length);
        positions.add(new Position(ll, PATH_HEIGHT));

        Path path = new Path(positions);
        path.setAltitudeMode(ALTITUDE_MODE);
        path.setExtrude(true);
        path.setDrawVerticals(true);

        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setOutlineMaterial(new Material(WWUtil.makeRandomColor(null)));
        attrs.setInteriorMaterial(attrs.getOutlineMaterial());
        attrs.setInteriorOpacity(0.5);
        attrs.setDrawOutline(true);
        attrs.setDrawInterior(true);
        attrs.setOutlineWidth(LINE_WIDTH);
        path.setAttributes(attrs);

        return path;
    }

    protected Layer makePolygonLayer()
    {
        RenderableLayer layer = new RenderableLayer();
        layer.setName("Polygons");

        ShapeAttributes normalAttributes = new BasicShapeAttributes();
        normalAttributes.setInteriorMaterial(Material.YELLOW);
        normalAttributes.setOutlineOpacity(0.5);
        normalAttributes.setInteriorOpacity(0.8);
        normalAttributes.setOutlineMaterial(Material.GREEN);
        normalAttributes.setOutlineWidth(2);
        normalAttributes.setDrawOutline(true);
        normalAttributes.setDrawInterior(true);
        normalAttributes.setEnableLighting(true);

        ShapeAttributes highlightAttributes = new BasicShapeAttributes(normalAttributes);
        highlightAttributes.setOutlineMaterial(Material.WHITE);
        highlightAttributes.setOutlineOpacity(1);

        // Create a polygon, set some of its properties and set its attributes.
        ArrayList<Position> pathPositions = new ArrayList<Position>();
        pathPositions.add(Position.fromDegrees(28, -106, 3e4));
        pathPositions.add(Position.fromDegrees(35, -104, 3e4));
        pathPositions.add(Position.fromDegrees(35, -107, 9e4));
        pathPositions.add(Position.fromDegrees(28, -107, 9e4));
        pathPositions.add(Position.fromDegrees(28, -106, 3e4));
        Polygon pgon = new Polygon(pathPositions);
        pgon.setValue(AVKey.DISPLAY_NAME, "Has a hole\nRotated -170\u00b0");

        pathPositions.clear();
        pathPositions.add(Position.fromDegrees(29, -106.4, 4e4));
        pathPositions.add(Position.fromDegrees(30, -106.4, 4e4));
        pathPositions.add(Position.fromDegrees(29, -106.8, 7e4));
        pathPositions.add(Position.fromDegrees(29, -106.4, 4e4));
        pgon.addInnerBoundary(pathPositions);
        pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        pgon.setAttributes(normalAttributes);
        pgon.setHighlightAttributes(highlightAttributes);
        pgon.setRotation(-90d);
        layer.addRenderable(pgon);

        ArrayList<Position> pathLocations = new ArrayList<Position>();
        pathLocations.add(Position.fromDegrees(28, -110, 5e4));
        pathLocations.add(Position.fromDegrees(35, -108, 5e4));
        pathLocations.add(Position.fromDegrees(35, -111, 5e4));
        pathLocations.add(Position.fromDegrees(28, -111, 5e4));
        pathLocations.add(Position.fromDegrees(28, -110, 5e4));
        pgon = new Polygon(pathLocations);
        pgon.setValue(AVKey.DISPLAY_NAME, "Has an image");
        normalAttributes = new BasicShapeAttributes(normalAttributes);
        normalAttributes.setDrawInterior(true);
        normalAttributes.setInteriorMaterial(Material.WHITE);
        normalAttributes.setInteriorOpacity(1);
        pgon.setAttributes(normalAttributes);
        pgon.setHighlightAttributes(highlightAttributes);
        float[] texCoords = new float[] {0, 0, 1, 0, 1, 1, 0, 1, 0, 0};
        pgon.setTextureImageSource("images/32x32-icon-nasa.png", texCoords, 5);
        layer.addRenderable(pgon);

        return layer;
    }

    protected Layer makeExtrudedPolygonLayer()
    {
        RenderableLayer layer = new RenderableLayer();
        layer.setName("Extruded Polygons");

        // Create and set an attribute bundle.
        ShapeAttributes sideAttributes = new BasicShapeAttributes();
        sideAttributes.setInteriorMaterial(Material.MAGENTA);
        sideAttributes.setOutlineOpacity(0.5);
        sideAttributes.setInteriorOpacity(0.5);
        sideAttributes.setOutlineMaterial(Material.GREEN);
        sideAttributes.setOutlineWidth(2);
        sideAttributes.setDrawOutline(true);
        sideAttributes.setDrawInterior(true);
        sideAttributes.setEnableLighting(true);

        ShapeAttributes sideHighlightAttributes = new BasicShapeAttributes(sideAttributes);
        sideHighlightAttributes.setOutlineMaterial(Material.WHITE);
        sideHighlightAttributes.setOutlineOpacity(1);

        ShapeAttributes capAttributes = new BasicShapeAttributes(sideAttributes);
        capAttributes.setInteriorMaterial(Material.YELLOW);
        capAttributes.setInteriorOpacity(0.8);
        capAttributes.setDrawInterior(true);
        capAttributes.setEnableLighting(true);

        // Create a path, set some of its properties and set its attributes.
        ArrayList<Position> pathPositions = new ArrayList<Position>();
        pathPositions.add(Position.fromDegrees(36, -106, 3e4));
        pathPositions.add(Position.fromDegrees(43, -104, 3e4));
        pathPositions.add(Position.fromDegrees(43, -107, 9e4));
        pathPositions.add(Position.fromDegrees(36, -107, 9e4));
        pathPositions.add(Position.fromDegrees(36, -106, 3e4));
        ExtrudedPolygon pgon = new ExtrudedPolygon(pathPositions);

        pathPositions.clear();
        pathPositions.add(Position.fromDegrees(37, -106.4, 4e4));
        pathPositions.add(Position.fromDegrees(38, -106.4, 4e4));
        pathPositions.add(Position.fromDegrees(37, -106.8, 7e4));
        pathPositions.add(Position.fromDegrees(37, -106.4, 4e4));
        pgon.addInnerBoundary(pathPositions);
        pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        pgon.setSideAttributes(sideAttributes);
        pgon.setSideHighlightAttributes(sideHighlightAttributes);
        pgon.setCapAttributes(capAttributes);
        layer.addRenderable(pgon);

        ArrayList<LatLon> pathLocations = new ArrayList<LatLon>();
        pathLocations.add(LatLon.fromDegrees(36, -110));
        pathLocations.add(LatLon.fromDegrees(43, -108));
        pathLocations.add(LatLon.fromDegrees(43, -111));
        pathLocations.add(LatLon.fromDegrees(36, -111));
        pathLocations.add(LatLon.fromDegrees(36, -110));
        pgon = new ExtrudedPolygon(pathLocations, 6e4);
        pgon.setSideAttributes(sideAttributes);
        pgon.setSideHighlightAttributes(sideHighlightAttributes);
        pgon.setCapAttributes(capAttributes);
        layer.addRenderable(pgon);

        return layer;
    }

    public static void main(String[] args)
    {
        String appName = "World Wind MultiGlobe";

        if (Configuration.isMacOS())
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);

        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new FlatAndRoundGlobes();
            }
        });
    }
}

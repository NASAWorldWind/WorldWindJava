/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.util.Arrays;

/**
 * Illustrates how to enable shape dragging in World Wind by using a <code>{@link gov.nasa.worldwind.util.BasicDragger}</code>. This creates
 * multiple shapes on the surface terrain that can be dragged to a new location on the terrain. The shapes retain their
 * form when dragged.
 *
 * @author dcollins
 * @version $Id: DraggingShapes.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class DraggingShapes extends ApplicationTemplate
{
    protected static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected static final String SURFACE_POLYGON_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/georss.png";

        public AppFrame()
        {
            // Add a basic dragger to the World Window's select listeners to enable shape dragging.
            this.getWwd().addSelectListener(new BasicDragger(this.getWwd()));

            // Create a layer of shapes to drag.
            this.makeShapes();
        }

        protected void makeShapes()
        {
            RenderableLayer layer = new RenderableLayer();
            layer.setName("Surface Shapes");

            // Surface polygon over Florida.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.WHITE);
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);
            attrs.setImageSource(SURFACE_POLYGON_IMAGE_PATH);
            attrs.setImageScale(0.5);

            double originLat = 28;
            double originLon = -82;
            Iterable<LatLon> locations = Arrays.asList(
                LatLon.fromDegrees(originLat + 5.0, originLon + 2.5),
                LatLon.fromDegrees(originLat + 5.0, originLon - 2.5),
                LatLon.fromDegrees(originLat + 2.5, originLon - 5.0),
                LatLon.fromDegrees(originLat - 2.5, originLon - 5.0),
                LatLon.fromDegrees(originLat - 5.0, originLon - 2.5),
                LatLon.fromDegrees(originLat - 5.0, originLon + 2.5),
                LatLon.fromDegrees(originLat - 2.5, originLon + 5.0),
                LatLon.fromDegrees(originLat + 2.5, originLon + 5.0),
                LatLon.fromDegrees(originLat + 5.0, originLon + 2.5));
            SurfaceShape shape = new SurfacePolygon(locations);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface polygon spanning the international dateline.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.RED);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.RED)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            locations = Arrays.asList(
                LatLon.fromDegrees(20, -170),
                LatLon.fromDegrees(15, 170),
                LatLon.fromDegrees(10, -175),
                LatLon.fromDegrees(5, 170),
                LatLon.fromDegrees(0, -170),
                LatLon.fromDegrees(20, -170));
            shape = new SurfacePolygon(locations);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface ellipse over the center of the United States.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.CYAN);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.CYAN)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceEllipse(LatLon.fromDegrees(38, -104), 1.5e5, 1e5, Angle.fromDegrees(15));
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface circle over the center of the United states.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.GREEN);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.GREEN)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceCircle(LatLon.fromDegrees(36, -104), 1e5);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface circle over the North Pole
            shape = new SurfaceCircle(LatLon.fromDegrees(90, 0), 1e6);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface quadrilateral over the center of the United States.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.ORANGE);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.ORANGE)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceQuad(LatLon.fromDegrees(42, -104), 1e5, 1.3e5, Angle.fromDegrees(20));
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface square over the center of the United states.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.MAGENTA);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.MAGENTA)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceSquare(LatLon.fromDegrees(45, -104), 1e5);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface sector over Mt. Shasta.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.YELLOW);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.YELLOW)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceSector(new Sector(
                Angle.fromDegrees(41.0), Angle.fromDegrees(41.6),
                Angle.fromDegrees(-122.5), Angle.fromDegrees(-121.7)));
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface sector over Lake Tahoe.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.CYAN);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.CYAN)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceSector(new Sector(
                Angle.fromDegrees(38.9), Angle.fromDegrees(39.3),
                Angle.fromDegrees(-120.2), Angle.fromDegrees(-119.9)));
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface polyline spanning the international dateline.
            attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(3);
            attrs.setOutlineStippleFactor(2);

            locations = Arrays.asList(
                LatLon.fromDegrees(-10, 170),
                LatLon.fromDegrees(-10, -170));
            shape = new SurfacePolyline(locations);
            shape.setAttributes(attrs);
            ((SurfacePolyline) shape).setClosed(false);
            layer.addRenderable(shape);

            // Add the layer to the model and update the layer panel.
            insertBeforeCompass(this.getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Dragging Shapes", AppFrame.class);
    }
}

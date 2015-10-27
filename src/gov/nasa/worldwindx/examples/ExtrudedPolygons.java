/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.BasicDragger;

import java.util.ArrayList;

/**
 * Shows how to use {@link ExtrudedPolygon}.
 *
 * @author tag
 * @version $Id: ExtrudedPolygons.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ExtrudedPolygons extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Add a dragger to enable shape dragging
            this.getWwd().addSelectListener(new BasicDragger(this.getWwd()));

            RenderableLayer layer = new RenderableLayer();

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
            pathPositions.add(Position.fromDegrees(28, -106, 3e4));
            pathPositions.add(Position.fromDegrees(35, -104, 3e4));
            pathPositions.add(Position.fromDegrees(35, -107, 9e4));
            pathPositions.add(Position.fromDegrees(28, -107, 9e4));
            pathPositions.add(Position.fromDegrees(28, -106, 3e4));
            ExtrudedPolygon pgon = new ExtrudedPolygon(pathPositions);

            pathPositions.clear();
            pathPositions.add(Position.fromDegrees(29, -106.4, 4e4));
            pathPositions.add(Position.fromDegrees(30, -106.4, 4e4));
            pathPositions.add(Position.fromDegrees(29, -106.8, 7e4));
            pathPositions.add(Position.fromDegrees(29, -106.4, 4e4));
            pgon.addInnerBoundary(pathPositions);
            pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pgon.setSideAttributes(sideAttributes);
            pgon.setSideHighlightAttributes(sideHighlightAttributes);
            pgon.setCapAttributes(capAttributes);
            layer.addRenderable(pgon);

            ArrayList<LatLon> pathLocations = new ArrayList<LatLon>();
            pathLocations.add(LatLon.fromDegrees(28, -110));
            pathLocations.add(LatLon.fromDegrees(35, -108));
            pathLocations.add(LatLon.fromDegrees(35, -111));
            pathLocations.add(LatLon.fromDegrees(28, -111));
            pathLocations.add(LatLon.fromDegrees(28, -110));
            pgon = new ExtrudedPolygon(pathLocations, 6e4);
            pgon.setSideAttributes(sideAttributes);
            pgon.setSideHighlightAttributes(sideHighlightAttributes);
            pgon.setCapAttributes(capAttributes);
            layer.addRenderable(pgon);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Extruded Polygons", AppFrame.class);
    }
}

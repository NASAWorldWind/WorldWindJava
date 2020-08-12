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

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.util.BasicDragger;

import java.util.*;

/**
 * Example of {@link Polygon} usage. Sets material, opacity and other attributes. Sets rotation and other properties.
 * Adds an image for texturing. Shows a dateline-spanning Polygon.
 *
 * @author tag
 * @version $Id: Polygons.java 2291 2014-08-30 21:38:47Z tgaskins $
 */
public class Polygons extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Add a dragger to enable shape dragging
            this.getWwd().addSelectListener(new BasicDragger(this.getWwd()));

            RenderableLayer layer = new RenderableLayer();
            layer.setName("Polygons");

            // Create and set an attribute bundle.
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
            pgon.setRotation(-170d);
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

            pathLocations.clear();
            pathLocations.add(Position.fromDegrees(28, -170, 29e4));
            pathLocations.add(Position.fromDegrees(35, -174, 29e4));
            pathLocations.add(Position.fromDegrees(35, 174, 29e4));
            pathLocations.add(Position.fromDegrees(28, 170, 29e4));
            pathLocations.add(Position.fromDegrees(28, -170, 29e4));
            pgon = new Polygon(pathLocations);
            pgon.setValue(AVKey.DISPLAY_NAME, "Spans dateline\nRotated -45\u00b0");
            normalAttributes = new BasicShapeAttributes(normalAttributes);
            normalAttributes.setDrawInterior(true);
            pgon.setAttributes(normalAttributes);
            pgon.setHighlightAttributes(highlightAttributes);
            pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pgon.setRotation(-45d);
            layer.addRenderable(pgon);

            // Polygon over the north pole
            pathLocations.clear();
            pathLocations.add(Position.fromDegrees(80, 0, 100e3));
            pathLocations.add(Position.fromDegrees(80, 90, 100e3));
            pathLocations.add(Position.fromDegrees(80, 180, 100e3));
            pathLocations.add(Position.fromDegrees(80, -90, 100e3));
            pathLocations.add(Position.fromDegrees(80, 0, 100e3));
            pgon = new Polygon(pathLocations);
            pgon.setValue(AVKey.DISPLAY_NAME, "Surrounds the north pole");
            normalAttributes = new BasicShapeAttributes(normalAttributes);
            normalAttributes.setDrawInterior(true);
            normalAttributes.setInteriorMaterial(Material.RED);
            pgon.setAttributes(normalAttributes);
            pgon.setHighlightAttributes(highlightAttributes);
            pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            layer.addRenderable(pgon);

            List<Marker> markers = new ArrayList<Marker>(1);
            markers.add(new BasicMarker(Position.fromDegrees(90, 0), new BasicMarkerAttributes()));
            MarkerLayer markerLayer = new MarkerLayer();
            markerLayer.setMarkers(markers);
            insertBeforeCompass(getWwd(), markerLayer);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Polygons", AppFrame.class);
    }
}

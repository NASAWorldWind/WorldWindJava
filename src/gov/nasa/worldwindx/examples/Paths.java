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
import gov.nasa.worldwind.util.*;
import java.awt.Color;

import java.util.*;

/**
 * Example of {@link Path} usage. A Path is a line or curve between positions. The path may follow terrain, and may be
 * turned into a curtain by extruding the path to the ground.
 *
 * @author tag
 * @version $Id: Paths.java 2292 2014-09-02 21:13:05Z tgaskins $
 */
public class Paths extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            super(true, true, false);

            // Add a dragger to enable shape dragging
            this.getWwd().addSelectListener(new BasicDragger(this.getWwd()));

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(Color.YELLOW));
            attrs.setOutlineWidth(2d);

            // Create a path, set some of its properties and set its attributes.
            ArrayList<Position> pathPositions = new ArrayList<>();
            pathPositions.add(Position.fromDegrees(28, -102, 1e4));
            pathPositions.add(Position.fromDegrees(35, -100, 1e4));
            Path path = new Path(pathPositions);
            path.setAttributes(attrs);
            path.setVisible(true);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setPathType(AVKey.GREAT_CIRCLE);
            layer.addRenderable(path);

            // Create a path that follows the terrain
            path = new Path(pathPositions);
            path.setAttributes(attrs);
            path.setVisible(true);
            path.setSurfacePath(true);
            layer.addRenderable(path);

            // Create a path that uses all default values.
            pathPositions = new ArrayList<>();
            pathPositions.add(Position.fromDegrees(28, -104, 1e4));
            pathPositions.add(Position.fromDegrees(35, -102, 1e4));
            path = new Path(pathPositions);
            layer.addRenderable(path);

            // Create a path with more than two positions and closed.
            pathPositions = new ArrayList<>();
            pathPositions.add(Position.fromDegrees(28, -106, 4e4));
            pathPositions.add(Position.fromDegrees(35, -104, 4e4));
            pathPositions.add(Position.fromDegrees(35, -107, 4e4));
            pathPositions.add(Position.fromDegrees(28, -107, 4e4));
            path = new Path(pathPositions);
            path.setAltitudeMode(WorldWind.ABSOLUTE);
            path.setExtrude(true);
            path.setPathType(AVKey.LINEAR);

            attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(Color.BLUE));
            attrs.setInteriorMaterial(new Material(Color.RED));
            attrs.setOutlineWidth(2);
            path.setAttributes(attrs);

            layer.addRenderable(path);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

            List<Marker> markers = new ArrayList<>(1);
            markers.add(new BasicMarker(Position.fromDegrees(90, 0), new BasicMarkerAttributes()));
            MarkerLayer markerLayer = new MarkerLayer();
            markerLayer.setMarkers(markers);
            insertBeforeCompass(getWwd(), markerLayer);
        }
    }

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Paths", AppFrame.class);
    }
}

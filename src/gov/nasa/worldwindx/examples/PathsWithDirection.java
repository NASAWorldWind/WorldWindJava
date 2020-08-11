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

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.util.DirectedPath;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.util.ArrayList;

/**
 * Shows how to draw a {@link Path} with direction arrowheads between the points.
 *
 * @author pabercrombie
 * @version $Id: PathsWithDirection.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class PathsWithDirection extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(2d);

            // Create a path, set some of its properties and set its attributes.
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(49.01653274909177, -122.7349081128505, 1));
            pathPositions.add(Position.fromDegrees(49.01715024535254, -122.7596194200486, 10));
            pathPositions.add(Position.fromDegrees(49.02781845803761, -122.7651733463364, 100));
            pathPositions.add(Position.fromDegrees(49.05312411976134, -122.7926787136435, 1000));
            pathPositions.add(Position.fromDegrees(49.0747697644625, -122.8224152286015, 1000));
            pathPositions.add(Position.fromDegrees(49.09727187849899, -122.8187118695457, 1000));
            pathPositions.add(Position.fromDegrees(49.1002974270654, -122.7348314826556, 100));
            pathPositions.add(Position.fromDegrees(49.11190305133165, -122.7345541413842, 100));
            pathPositions.add(Position.fromDegrees(49.11101764617014, -122.7455553490629, 10));
            pathPositions.add(Position.fromDegrees(49.11509767012883, -122.7459193678911, 10));
            pathPositions.add(Position.fromDegrees(49.11467371318521, -122.7563706291131, 10));

            Path path = new DirectedPath(pathPositions);

            // To ensure that the arrowheads resize smoothly, refresh each time the path is drawn.
            path.setAttributes(attrs);
            path.setVisible(true);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setPathType(AVKey.GREAT_CIRCLE);
            layer.addRenderable(path);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 49.06);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -122.77);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 22000);

        ApplicationTemplate.start("WorldWind Paths With Direction", AppFrame.class);
    }
}

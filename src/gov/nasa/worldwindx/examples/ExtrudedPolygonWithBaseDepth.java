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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.util.ArrayList;

/**
 * Shows how to use {@link ExtrudedPolygon} with a specified base depth that places the extruded polygon's base vertices
 * below the terrain. You might want to do this if the extruded polygon spans a valley and the polygon boundary is not
 * sampled in sufficient detail to capture the valley. Specifying a base depth can fill the gap between the base and the
 * valley floor.
 *
 * @author tag
 * @version $Id: ExtrudedPolygonWithBaseDepth.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ExtrudedPolygonWithBaseDepth extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes sideAttributes = new BasicShapeAttributes();
            sideAttributes.setInteriorMaterial(Material.MAGENTA);
            sideAttributes.setOutlineOpacity(0.5);
            sideAttributes.setInteriorOpacity(0.25);
            sideAttributes.setOutlineMaterial(Material.GREEN);
            sideAttributes.setOutlineWidth(1);
            sideAttributes.setDrawOutline(true);
            sideAttributes.setDrawInterior(true);

            ShapeAttributes capAttributes = new BasicShapeAttributes(sideAttributes);
            capAttributes.setInteriorMaterial(Material.YELLOW);
            capAttributes.setInteriorOpacity(0.25);
            capAttributes.setDrawInterior(true);

            // Create a path, set some of its properties and set its attributes.
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(43.84344, -114.63673, 20));
            pathPositions.add(Position.fromDegrees(43.84343, -114.63468, 20));
            pathPositions.add(Position.fromDegrees(43.84316, -114.63468, 20));
            pathPositions.add(Position.fromDegrees(43.84314, -114.63675, 20));
            pathPositions.add(Position.fromDegrees(43.84344, -114.63673, 20));
            ExtrudedPolygon pgon = new ExtrudedPolygon(pathPositions);

            pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pgon.setSideAttributes(sideAttributes);
            pgon.setCapAttributes(capAttributes);
            pgon.setBaseDepth(20); // Set the base depth to the extruded polygon's height.
            layer.addRenderable(pgon);

            Path path = new Path(Position.fromDegrees(43.8425, -114.6355, 0),
                Position.fromDegrees(43.8442, -114.6356, 0));

            ShapeAttributes pathAttributes = new BasicShapeAttributes();
            pathAttributes.setOutlineOpacity(1);
            pathAttributes.setOutlineMaterial(Material.GREEN);
            pathAttributes.setOutlineWidth(4);
            path.setAttributes(pathAttributes);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            layer.addRenderable(path);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

            getWwd().getView().setEyePosition(
                Position.fromDegrees(43.843162670564354, -114.63551647988652, 2652.865781935775));
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Extruded Polygon with Base Depth", AppFrame.class);
    }
}

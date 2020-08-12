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

package gov.nasa.worldwindx.performance;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: PolygonsEverywhere.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class PolygonsEverywhere extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            makeMany();
        }

        protected void makeMany()
        {
            int altitudeMode = WorldWind.ABSOLUTE;

            double minLat = -50, maxLat = 50, minLon = -140, maxLon = -10;
            double delta = 1.5;
            double intervals = 5;
            double dLat = 1 / intervals;
            double dLon = 1 / intervals;

            ArrayList<Position> positions = new ArrayList<Position>();

            RenderableLayer layer = new RenderableLayer();
            layer.setPickEnabled(false);

            int count = 0;
            for (double lat = minLat; lat <= maxLat; lat += delta)
            {
                for (double lon = minLon; lon <= maxLon; lon += delta)
                {
                    positions.clear();
                    double innerLat = lat;
                    double innerLon = lon;

                    for (int i = 0; i <= intervals; i++)
                    {
                        innerLon += dLon;
                        positions.add(Position.fromDegrees(innerLat, innerLon, 5e4));
                    }

                    for (int i = 0; i <= intervals; i++)
                    {
                        innerLat += dLat;
                        positions.add(Position.fromDegrees(innerLat, innerLon, 5e4));
                    }

                    for (int i = 0; i <= intervals; i++)
                    {
                        innerLon -= dLon;
                        positions.add(Position.fromDegrees(innerLat, innerLon, 5e4));
                    }

                    for (int i = 0; i <= intervals; i++)
                    {
                        innerLat -= dLat;
                        positions.add(Position.fromDegrees(innerLat, innerLon, 5e4));
                    }

                    Polygon pgon = new Polygon(positions);
                    pgon.setAltitudeMode(altitudeMode);
                    ShapeAttributes attrs = new BasicShapeAttributes();
                    attrs.setDrawOutline(false);
                    attrs.setInteriorMaterial(Material.RED);
                    attrs.setEnableLighting(true);
                    pgon.setAttributes(attrs);
                    layer.addRenderable(pgon);
                    ++count;
                }
            }
            System.out.printf("%d Polygons, %d positions each, Altitude mode = %s\n", count, positions.size(),
                altitudeMode == WorldWind.RELATIVE_TO_GROUND ? "RELATIVE_TO_GROUND" : "ABSOLUTE");

            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Very Many Polygons", AppFrame.class);
    }
}

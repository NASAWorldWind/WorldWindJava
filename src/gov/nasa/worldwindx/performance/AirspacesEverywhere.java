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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.airspaces.Polygon;

import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: AirspacesEverywhere.java 2231 2014-08-15 19:03:12Z dcollins $
 */
public class AirspacesEverywhere extends ApplicationTemplate
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
            double minLat = -50, maxLat = 50, minLon = -140, maxLon = -10;
            double delta = 5;
            double intervals = 100;
            double dLat = 1 / intervals;
            double dLon = 1 / intervals;

            ArrayList<LatLon> positions = new ArrayList<LatLon>();

            RenderableLayer layer = new RenderableLayer();

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
                        positions.add(LatLon.fromDegrees(innerLat, innerLon));
                    }

                    for (int i = 0; i <= intervals; i++)
                    {
                        innerLat += dLat;
                        positions.add(LatLon.fromDegrees(innerLat, innerLon));
                    }

                    for (int i = 0; i <= intervals; i++)
                    {
                        innerLon -= dLon;
                        positions.add(LatLon.fromDegrees(innerLat, innerLon));
                    }

                    for (int i = 0; i <= intervals; i++)
                    {
                        innerLat -= dLat;
                        positions.add(LatLon.fromDegrees(innerLat, innerLon));
                    }

                    Polygon pgon = new Polygon(positions);
                    pgon.setAltitudes(1e3, 1e4);
                    pgon.setAltitudeDatum(AVKey.ABOVE_MEAN_SEA_LEVEL, AVKey.ABOVE_MEAN_SEA_LEVEL);
                    layer.addRenderable(pgon);
                    ++count;
                }
            }
            System.out.printf("%d Polygons, %d positions\n", count, positions.size());

            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Very Many Airspaces", AppFrame.class);
    }
}

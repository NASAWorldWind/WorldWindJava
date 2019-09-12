/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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

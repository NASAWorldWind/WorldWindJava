/*
 * Copyright (C) 2015 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.performance;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: SurfacePolygonsEverywhere.java 3235 2015-06-22 20:52:18Z tgaskins $
 */
public class SurfacePolygonsEverywhere extends ApplicationTemplate
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
            double delta = 1.5;
            double intervals = 5;
            double dLat = 1 / intervals;
            double dLon = 1 / intervals;

            ArrayList<LatLon> positions = null;

            RenderableLayer layer = new RenderableLayer();
            layer.setPickEnabled(false);

            int count = 0;
            for (double lat = minLat; lat <= maxLat; lat += delta)
            {
                for (double lon = minLon; lon <= maxLon; lon += delta)
                {
                    positions = new ArrayList<LatLon>();
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

                    SurfacePolygon pgon = new SurfacePolygon(positions);
                    ShapeAttributes attrs = new BasicShapeAttributes();
                    attrs.setDrawOutline(true);
                    attrs.setInteriorMaterial(Material.RED);
                    attrs.setEnableLighting(true);
                    pgon.setAttributes(attrs);
                    layer.addRenderable(pgon);
                    ++count;
                }
            }
            System.out.printf("%d Polygons, %d positions each\n", count, positions.size());

            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Very Many Surface Polygons", AppFrame.class);
    }
}

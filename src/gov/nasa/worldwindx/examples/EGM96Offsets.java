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

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.util.EGM96;
import java.awt.Dimension;

import java.io.IOException;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

/**
 * Shows how to apply EGM96 offsets to the Earth.
 *
 * @author tag
 * @version $Id: EGM96Offsets.java 1501 2013-07-11 15:59:11Z tgaskins $
 */
public class EGM96Offsets extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        /**
         * Attempt to retrieve the best elevations for a specified list of locations. The elevations returned are the best currently
         * available for the data set and the area bounding the locations. Since the necessary elevation data might not
         * be in memory at the time of the call, this method iterates until the necessary elevation data is in memory
         * and can be used to determine the locations elevations.
         *
         * @param locations a list of locations to determine elevations for
         */
        public void loadBestElevations(ArrayList<LatLon> locations)
        {
            Globe globe = this.getWwd().getModel().getGlobe();
            ArrayList<Sector> sectors = new ArrayList<>();
            ArrayList<ArrayList<LatLon>> locationsList = new ArrayList<>();
            double delta = 0.0001;
            for (LatLon ll : locations)
            {
                double lat = ll.latitude.degrees;
                double lon = ll.longitude.degrees;
                sectors.add(Sector.fromDegrees(lat, lat + delta, lon, lon + delta));
                ArrayList<LatLon> sectorLocations = new ArrayList<>();
                sectorLocations.add(ll);
                sectorLocations.add(LatLon.fromDegrees(lat + delta, lon + delta));
                locationsList.add(sectorLocations);
            }

            double[] targetResolutions = new double[sectors.size()];
            double[] actualResolutions = new double[sectors.size()];
            for (int i = 0, len = sectors.size(); i < len; i++)
            {
                targetResolutions[i] = globe.getElevationModel().getBestResolution(sectors.get(i));
            }
            boolean resolutionsAchieved = false;
            double[] elevations = new double[2];
            while (!resolutionsAchieved)
            {
                for (int i = 0, len = sectors.size(); i < len; i++)
                {
                    actualResolutions[i] = globe.getElevations(sectors.get(i), locationsList.get(i), targetResolutions[i], elevations);
                }

                resolutionsAchieved = true;
                for (int i = 0, len = actualResolutions.length; i < len && resolutionsAchieved; i++)
                {
                    resolutionsAchieved = actualResolutions[i] <= targetResolutions[i];
                }
                if (!resolutionsAchieved)
                {
                    try
                    {
                        Thread.sleep(200);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        public AppFrame()
        {
            Model m = this.wwjPanel.getWwd().getModel();
            Earth earth = (Earth) m.getGlobe();
            try
            {
                earth.applyEGMA96Offsets("config/EGM96.dat");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            final RenderableLayer layer = new RenderableLayer();
            double[] locations = new double[]
            {
                37.0, -119.0,
                36.0, -117.016667,
                89.0, 0.0,
                -80.0, 0.0,
                -90.0, 0.0
            };
            Thread t = new Thread(() ->
            {
                ArrayList<LatLon> elevLocations = new ArrayList<>();
                for (int i = 0; i < locations.length; i += 2)
                {
                    elevLocations.add(LatLon.fromDegrees(locations[i], locations[i + 1]));
                }

                loadBestElevations(elevLocations);

                EGM96 egm96Offsets = earth.getEGM96();
                for (int i = 0; i < locations.length; i += 2)
                {
                    Position pos = Position.fromDegrees(locations[i], locations[i + 1], 0);
                    PointPlacemark placemark = new PointPlacemark(pos);
                    String label = String.format("lat: %7.4f, lon: %7.4f", locations[i], locations[i + 1]);
                    placemark.setValue(AVKey.DISPLAY_NAME, String.format("EGM96 Offset: %7.4f\nEGM96 Adjusted elevation: %7.4f",
                        egm96Offsets.getOffset(pos.latitude, pos.longitude),
                        earth.getElevation(pos.latitude, pos.longitude)));
                    placemark.setLabelText(label);
                    layer.addRenderable(placemark);
                }
                SwingUtilities.invokeLater(() ->
                {
                    System.out.println("Elevations retrieved");
                    getWwd().redraw();
                });
            });
            t.start();

            this.wwjPanel.toolTipController.setAnnotationSize(new Dimension(500, 0));
            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind EGM96 Offsets", AppFrame.class);
    }
}

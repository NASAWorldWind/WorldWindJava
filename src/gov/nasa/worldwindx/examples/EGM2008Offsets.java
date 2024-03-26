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
import gov.nasa.worldwind.util.EGM2008;
import java.awt.Dimension;

import java.io.IOException;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

/**
 * Shows how to apply EGM2008 offsets to Earth elevations.
 *
 * This EGM2008 data file is not included in the SDK due to its size. The data may be downloaded here:
 * https://builds.worldwind.arc.nasa.gov/artifactory/EGM2008-Data/egm2008_25.dat
 *
 * This example looks for the EGM2008 data in the WorldWind src/config folder by default.
 */
public class EGM2008Offsets extends ApplicationTemplate
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
            final RenderableLayer layer = new RenderableLayer();
            double[] locations = new double[]
            {
                37.0, -119.0,
                36.0, -117.016667,
                89.0, 0.0,
                -80.0, 0.0,
                -90.0, 0.0
            };
            EGM2008 egm2008Offsets = new EGM2008("config/egm2008_25.dat");
            boolean egmAvailable = egm2008Offsets.isEGMDataAvailable();
            if (!egmAvailable)
            {
                System.out.println("*** EGM 2008 data not available.");
            }

            // Run the elevation query in a separate thread to avoid locking up the user interface
            Thread t = new Thread(() ->
            {
                ArrayList<LatLon> elevLocations = new ArrayList<>();
                for (int i = 0; i < locations.length; i += 2)
                {
                    elevLocations.add(LatLon.fromDegrees(locations[i], locations[i + 1]));
                }

                loadBestElevations(elevLocations);

                try
                {
                    for (int i = 0; i < locations.length; i += 2)
                    {
                        Position pos = Position.fromDegrees(locations[i], locations[i + 1], 0);
                        PointPlacemark placemark = new PointPlacemark(pos);
                        String label = String.format("lat: %7.4f, lon: %7.4f", locations[i], locations[i + 1]);
                        float egmOffset = egm2008Offsets.getOffset(pos.latitude, pos.longitude);
                        double elevation = earth.getElevation(pos.latitude, pos.longitude);
                        if (egmAvailable)
                        {
                            placemark.setValue(AVKey.DISPLAY_NAME, String.format("EGM2008 Offset: %7.4f\nEllipsoid elevation:%7.4f\nEGM2008 Adjusted elevation: %7.4f",
                                egmOffset, elevation, elevation - egmOffset));
                        }
                        else
                        {
                            placemark.setValue(AVKey.DISPLAY_NAME, String.format("EGM2008 Offset: N/A\nEllipsoid elevation:%7.4f\nEGM2008 Adjusted elevation: N/A",
                                elevation));
                        }
                        placemark.setLabelText(label);
                        layer.addRenderable(placemark);
                    }
                }
                catch (IOException iex)
                {
                    iex.printStackTrace();
                }

                SwingUtilities.invokeLater(() ->
                {
                    System.out.println("Elevations retrieved");
                    getWwd().redraw();
                });
            });
            t.start();

            try
            {
                // Test offsets for some coordinates
                float lat = 47;
                float lon = -94;
                System.out.println(lat + "," + lon + "," + egm2008Offsets.getOffset(lat, lon));

                lat = 37;
                lon = -119;
                System.out.println(lat + "," + lon + "," + egm2008Offsets.getOffset(lat, lon));

                // Try previous coordinates to verify caching
                lat = 47;
                lon = -94;
                System.out.println(lat + "," + lon + "," + egm2008Offsets.getOffset(lat, lon));

                lat = 37;
                lon = -119;
                System.out.println(lat + "," + lon + "," + egm2008Offsets.getOffset(lat, lon));

                lat = 47.02f;
                lon = -94.02f;
                System.out.println(lat + "," + lon + "," + egm2008Offsets.getOffset(lat, lon));

                float gridResolution = (float) EGM2008.GRID_RESOLUTION;
                lat = 47 + gridResolution;
                lon = -94 - gridResolution;
                System.out.println(lat + "," + lon + "," + egm2008Offsets.getOffset(lat, lon));

                lat = 36.0f;
                lon = -117.0f;
                System.out.println(lat + "," + lon + "," + egm2008Offsets.getOffset(lat, lon));

                lat = 36.0f;
                lon = -117.041666666667f;
                System.out.println(lat + "," + lon + "," + egm2008Offsets.getOffset(lat, lon));

                System.out.println();
            }
            catch (IOException iex)
            {
                iex.printStackTrace();
            }

            this.wwjPanel.toolTipController.setAnnotationSize(new Dimension(500, 0));
            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind EGM2008 Offsets", AppFrame.class);
    }
}

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

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.*;

import javax.swing.*;

/**
 * Shows how to make extruded shapes from an ESRI Shapefile containing per-shape height attributes.
 */
public class ExtrudedPolygonsFromShapefile extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            // Construct a factory that loads Shapefiles on a background thread.
            ShapefileLayerFactory factory = new ShapefileLayerFactory();

            // Load a Shapefile in the San Francisco bay area containing per-shape height attributes.
            factory.createFromShapefileSource("testData/shapefiles/BayArea.shp",
                new ShapefileLayerFactory.CompletionCallback()
                {
                    @Override
                    public void completion(Object result)
                    {
                        final Layer layer = (Layer) result; // the result is the layer the factory created
                        layer.setName(WWIO.getFilename(layer.getName()));

                        // Add the layer to the WorldWindow's layer list on the Event Dispatch Thread.
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                getWwd().getModel().getLayers().add(layer);
                                getWwd().redraw();
                            }
                        });
                    }

                    @Override
                    public void exception(Exception e)
                    {
                        Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
                    }
                });
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 37.419833280894515);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -122.08426559929343);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 1000.0);
        Configuration.setValue(AVKey.INITIAL_PITCH, 60.0);

        ApplicationTemplate.start("Extruded Polygons from Shapefile", AppFrame.class);
    }
}

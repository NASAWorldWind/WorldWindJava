/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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

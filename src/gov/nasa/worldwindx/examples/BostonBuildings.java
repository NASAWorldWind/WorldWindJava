/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.util.RandomShapeAttributes;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.geom.*;
import javax.swing.*;

/**
 * Illustrates how to import ESRI Shapefiles into WorldWind. This uses a <code>{@link ShapefileLayerFactory}</code> to
 * parse a Shapefile's contents and convert the shapefile into an equivalent WorldWind shape.
 *
 * @version $Id: Shapefiles.java 3212 2015-06-18 02:45:56Z tgaskins $
 */
public class BostonBuildings extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            ShapefileLayerFactory factory = new ShapefileLayerFactory();

            // Specify an attribute delegate to assign random attributes to each shapefile record.
            final RandomShapeAttributes randomAttrs = new RandomShapeAttributes();
            factory.setAttributeDelegate(new ShapefileRenderable.AttributeDelegate() {
                @Override
                public void assignAttributes(ShapefileRecord shapefileRecord,
                        ShapefileRenderable.Record renderableRecord) {
                    renderableRecord.setAttributes(randomAttrs.nextAttributes().asShapeAttributes());
                }
            });

//            try {
//                // 42.3636, -71.1
//                Position eyePos = new Position(Angle.fromDegreesLatitude(42.3636), Angle.fromDegreesLongitude(-71.1), 25000.0); // Boston
//                this.getWwd().getView().setEyePosition(eyePos);
//                KMLRoot root = KMLRoot.createAndParse("/home/mpeterson/d/temp/boston/boston4236.kml");
//                RenderableLayer layer = new RenderableLayer();
//                this.getWwd().getModel().getLayers().add(layer);
//                layer.addRenderable(new KMLController(root));
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
            // Load the shapefile. Define the completion callback.
            Position eyePos = new Position(Angle.fromDegreesLatitude(42.3638), Angle.fromDegreesLongitude(-71.0607), 3000.0); // Boston
//            Position eyePos = new Position(Angle.fromDegreesLatitude(2.5), Angle.fromDegreesLongitude(2.5), 25000.0);
            this.getWwd().getView().setEyePosition(eyePos);
//            factory.createFromShapefileSource("/home/mpeterson/d/temp/multi.shp",
            factory.createFromShapefileSource("/home/mpeterson/d/temp/boston/boston4236.shp",
                    new ShapefileLayerFactory.CompletionCallback() {
                @Override
                public void completion(Object result) {
                    final Layer layer = (Layer) result; // the result is the layer the factory created
                    layer.setName(WWIO.getFilename(layer.getName()));

                    // Add the layer to the WorldWindow's layer list on the Event Dispatch Thread.
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            AppFrame.this.getWwd().getModel().getLayers().add(layer);
                        }
                    });
                }

                @Override
                public void exception(Exception e) {
                    Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
                }
            });
        }
    }

    public static void main(String[] args) {
        start("WorldWind Shapefiles", AppFrame.class);
    }
}

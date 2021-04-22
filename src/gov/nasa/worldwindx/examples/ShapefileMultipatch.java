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
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cone;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.util.RandomShapeAttributes;

import javax.swing.*;

/**
 * Illustrates how to import ESRI Shapefiles containing 3D multi patches into
 * WorldWind. This uses a <code>{@link ShapefileLayerFactory}</code> to parse a
 * Shapefile's contents and convert the shapefile into an equivalent WorldWind
 * shape.
 */
public class ShapefileMultipatch extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            ShapefileLayerFactory factory = new ShapefileLayerFactory();
            BasicShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setEnableLighting(true);
            attrs.setInteriorMaterial(Material.RED);
            factory.setNormalShapeAttributes(attrs);
            BasicShapeAttributes highlightAttrs = new BasicShapeAttributes(attrs);
            highlightAttrs.setInteriorMaterial(Material.WHITE);
            factory.setHighlightShapeAttributes(highlightAttrs);

            // Load the shapefile. Define the completion callback.
            factory.createFromShapefileSource("testData/shapefiles/multipatch.shp",
                    new ShapefileLayerFactory.CompletionCallback() {
                @Override
                public void completion(Object result) {
                    final RenderableLayer layer = (RenderableLayer) result; // the result is the layer the factory created
                    layer.setName(WWIO.getFilename(layer.getName()));

//                    Cylinder cyl = new Cylinder(Position.fromDegrees(43, -97, 50000), 5000, 5000);
//                    cyl.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
//                    cyl.setAttributes(attrs);
//                    cyl.setVisible(true);
//                    cyl.setValue(AVKey.DISPLAY_NAME, "Cone with equal axes, CLAMP_TO_GROUND altitude mode");
//                    layer.addRenderable(cyl);
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
//        Configuration.setValue(AVKey.INITIAL_LATITUDE, 40.009993372683);
//        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -105.27284091410579);
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 43);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -97);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 21000);
        start("WorldWind Multi Patch Shapefiles", AppFrame.class);
    }
}

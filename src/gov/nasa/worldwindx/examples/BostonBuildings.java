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

import java.util.*;
import java.io.*;
import javax.swing.*;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.util.RandomShapeAttributes;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.meshes.*;

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

//            Earth testGlobe = new Earth();
//            Position[] positions = new Position[]{Position.fromDegrees(0, 0, 0), Position.fromDegrees(90, 0, 0), Position.fromDegrees(-90, 0, 0),
//                Position.fromDegrees(180, 0, 0), Position.fromDegrees(0, 90, 0), Position.fromDegrees(0, -90, 0)};
//            for (Position p : positions) {
//                System.out.println(p + " => " + testGlobe.computePointFromPosition(p));
//            }
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
           for (Layer layer : this.getWwd().getModel().getLayers()) {
                if (layer.getName().toLowerCase().contains("bing")) {
                    layer.setEnabled(true);
                }
            }
            ShapeAttributes normalAttributes = new BasicShapeAttributes();
            normalAttributes.setInteriorMaterial(Material.LIGHT_GRAY);
//            normalAttributes.setOutlineOpacity(0.5);
//            normalAttributes.setInteriorOpacity(0.8);
            normalAttributes.setOutlineMaterial(Material.GREEN);
            normalAttributes.setOutlineWidth(2);
            normalAttributes.setDrawOutline(true);
            normalAttributes.setDrawInterior(true);
            normalAttributes.setEnableLighting(true);
            Position eyePos = new Position(Angle.fromDegreesLatitude(42.3638), Angle.fromDegreesLongitude(-71.0607), 3000.0); // Boston
//             Position eyePos = new Position(Angle.fromDegreesLatitude(0.0025), Angle.fromDegreesLongitude(0.0025), 2500.0);
            this.getWwd().getView().setEyePosition(eyePos);
            double delta = 0.004;
            factory.setAOIFilter(new Sector(eyePos.latitude.subtractDegrees(delta), eyePos.latitude.addDegrees(delta),
                    eyePos.longitude.subtractDegrees(delta), eyePos.longitude.addDegrees(delta)));
//            factory.createFromShapefileSource("/home/mpeterson/d/temp/multi.shp",
            factory.createFromShapefileSource("/home/mpeterson/d/temp/boston/boston4236.shp",
                    new ShapefileLayerFactory.CompletionCallback() {
                @Override
                public void completion(Object result) {
                    final RenderableLayer layer = (RenderableLayer) result; // the result is the layer the factory created
                    layer.setName(WWIO.getFilename(layer.getName()));

                    // Add the layer to the WorldWindow's layer list on the Event Dispatch Thread.
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            for (Renderable r:layer.getRenderables()) {
                                if (r instanceof Mesh3D) {
                                    Mesh3D mesh=(Mesh3D) r;
                                    mesh.setAttributes(normalAttributes);
                                }
                            }
                            AppFrame.this.getWwd().getModel().getLayers().add(layer);
//                            ArrayList<Polygon> polygons = testPolygons();
//                            for (Polygon p : polygons) {
//                                layer.addRenderable(p);
//                            }
                        }
                    });
                }

                @Override
                public void exception(Exception e) {
                    Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
                }
            });
        }

        private ArrayList<Polygon> testPolygons() {
            ArrayList<Polygon> polygons = new ArrayList<>();
            ShapeAttributes normalAttributes = new BasicShapeAttributes();
            normalAttributes.setInteriorMaterial(Material.RED);
//            normalAttributes.setOutlineOpacity(0.5);
//            normalAttributes.setInteriorOpacity(0.8);
            normalAttributes.setOutlineMaterial(Material.GREEN);
            normalAttributes.setOutlineWidth(2);
            normalAttributes.setDrawOutline(true);
            normalAttributes.setDrawInterior(true);
            normalAttributes.setEnableLighting(true);
            try {
                try (BufferedReader infoReader = new BufferedReader(new FileReader("/home/mpeterson/d/temp/polygons.txt"))) {
                    String line = infoReader.readLine();
                    ArrayList<Position> positions = new ArrayList<>();
                    while (line != null) {
                        if (line.toLowerCase().startsWith("part")) {
                            if (!positions.isEmpty()) {
                                Polygon newPoly = new Polygon(positions);
                                newPoly.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                                newPoly.setAttributes(normalAttributes);
                                polygons.add(newPoly);
                                positions.clear();
                            }
                        } else if (line.startsWith("(")) {
                            StringTokenizer st = new StringTokenizer(line, ",()");
                            double lat = Double.valueOf(st.nextToken());
                            double lon = Double.valueOf(st.nextToken());
                            double elev = Double.valueOf(st.nextToken());
                            positions.add(Position.fromDegrees(lat, lon, elev));

                        }
                        line = infoReader.readLine();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return polygons;
        }
    }

    public static void main(String[] args) {
        start("WorldWind Shapefiles", AppFrame.class);
    }
}

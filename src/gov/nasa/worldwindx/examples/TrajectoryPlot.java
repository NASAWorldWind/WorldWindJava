/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import java.io.*;
import java.util.*;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;

/**
 * Example of plotting flight trajectories from a CSV file.
 */
public class TrajectoryPlot extends ApplicationTemplate {

    private static String TRAJECTORY_FILE_NAME = "testData/ExampleTrajectories.csv";

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            super(true, true, false);

            importTrajectories();
        }

        private void importTrajectories() {
            try {
                HashMap<String, ArrayList<Position>> flightMaps = new HashMap<>();
                try (BufferedReader csvReader = new BufferedReader(new FileReader(TRAJECTORY_FILE_NAME))) {
                    csvReader.readLine(); // skip header
                    String line = csvReader.readLine();
                    while (line != null) {
                        StringTokenizer st = new StringTokenizer(line, ",");
                        if (st.countTokens() != 4) {
                            System.out.println("Malformed line: " + line);
                        } else {
                            String ckey = st.nextToken();
                            double lat = Double.valueOf(st.nextToken());
                            double lon = Double.valueOf(st.nextToken());
                            double alt = Double.valueOf(st.nextToken());
                            if (!flightMaps.containsKey(ckey)) {
                                flightMaps.put(ckey, new ArrayList<>());
                            }
                            flightMaps.get(ckey).add(Position.fromDegrees(lat, lon, alt));
                        }
                        line = csvReader.readLine();
                    }
                }
                ShapeAttributes markerAttrs = new BasicShapeAttributes();
                markerAttrs.setInteriorMaterial(Material.RED);
                markerAttrs.setInteriorOpacity(0.7);
                markerAttrs.setEnableLighting(true);
                markerAttrs.setDrawInterior(true);
                markerAttrs.setDrawOutline(false);

                BasicShapeAttributes pathAttrs = new BasicShapeAttributes();
                pathAttrs.setOutlineMaterial(Material.YELLOW);

                RenderableLayer markerLayer = new RenderableLayer();
                markerLayer.setName("Flight Markers");
                insertBeforePlacenames(this.getWwd(), markerLayer);

                RenderableLayer labelLayer = new RenderableLayer();
                labelLayer.setName("Flight Labels");
                insertBeforePlacenames(this.getWwd(), labelLayer);
                flightMaps.keySet().forEach((ckey) -> {
                    ArrayList<Position> flightPositions = flightMaps.get(ckey);
                    if (!flightPositions.isEmpty()) {
                        labelLayer.addRenderable(new GlobeAnnotation(ckey, flightPositions.get(0)));
                    }

                    RenderableLayer flightLayer = new RenderableLayer();
                    flightLayer.setName(ckey);
                    insertBeforePlacenames(this.wwjPanel.getWwd(), flightLayer);

                    Path flightPath = new Path(flightPositions);
                    flightPath.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                    flightPath.setAttributes(pathAttrs);
                    flightLayer.addRenderable(flightPath);

                    flightPositions.forEach((p) -> {
                        Ellipsoid marker = new Ellipsoid(p, 10, 10, 10);
                        marker.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                        marker.setAttributes(markerAttrs);
                        marker.setVisible(true);
                        markerLayer.addRenderable(marker);

                    });
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            TRAJECTORY_FILE_NAME = args[0];
        }
        ApplicationTemplate.start("WorldWind Paths", AppFrame.class);
    }
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.render.ScreenAnnotation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Retrieve the highest-resolution elevations available for the current elevation model, drawing them from the server if
 * necessary. Shift-click on the globe to retrieve the elevation of a location.
 * <p>
 * Note: The {@link gov.nasa.worldwind.terrain.HighResolutionTerrain} class may be more appropriate to your needs than
 * this example.
 *
 * @author tag
 * @version $Id: GetBestElevations.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see gov.nasa.worldwind.terrain.HighResolutionTerrain
 */
public class GetBestElevations extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        /**
         * Retrieve elevations for a specified list of locations. The elevations returned are the best currently
         * available for the dataset and the area bounding the locations. Since the necessary elevation data might not
         * be in memory at the time of the call, this method iterates until the necessary elevation data is in memory
         * and can be used to determine the locations' elevations.
         * <p>
         * The locations must have a bounding sector, so more than one location is required. If the bounding region is
         * large, a huge amount of data must be retrieved from the server. Be aware that you are overriding the system's
         * resolution selection mechanism by requesting the highest resolution data, which could easily be gigabytes.
         * Requesting data for a large region will take a long time, will dump a lot of data into the local disk cache,
         * and may cause the server to throttle your access.
         *
         * @param locations a list of locations to determine elevations for
         *
         * @return the resolution actually achieved.
         */
        public double[] getBestElevations(List<LatLon> locations) {
            Globe globe = this.getWwd().getModel().getGlobe();
            Sector sector = Sector.boundingSector(locations);
            double[] elevations = new double[locations.size()];

            // Iterate until the best resolution is achieved. Use the elevation model to determine the best elevation.
            double targetResolution = globe.getElevationModel().getBestResolution(sector);
            double actualResolution = Double.MAX_VALUE;
            while (actualResolution > targetResolution) {
                actualResolution = globe.getElevations(sector, locations, targetResolution, elevations);
                // Uncomment the two lines below if you want to watch the resolution converge
//                System.out.printf("Target resolution = %s, Actual resolution = %s\n",
//                    Double.toString(targetResolution), Double.toString(actualResolution));
                try {
                    Thread.sleep(200); // give the system a chance to retrieve data from the disk cache or the server
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return elevations;
        }

        public AppFrame() {
            super(true, true, false);

            final ScreenAnnotation annotation = new ScreenAnnotation("Shift-click to select a location",
                    new Point(100, 50));
            AnnotationLayer layer = new AnnotationLayer();
            layer.addAnnotation(annotation);
            insertBeforeCompass(this.getWwd(), layer);

            this.getWwd().getInputHandler().addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    if ((mouseEvent.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0) {
                        return;
                    }
                    mouseEvent.consume();

                    final Position pos = getWwd().getCurrentPosition();
                    if (pos == null) {
                        return;
                    }

                    annotation.setText(String.format("Elevation = "));
                    getWwd().redraw();

                    // Run the elevation query in a separate thread to avoid locking up the user interface
                    Thread t = new Thread(() -> {
                        // We want elevation for only one location, so add a second location that's very near the
                        // desired one. This causes fewer requests to the disk or server, and causes faster
                        // convergence.
                        List<LatLon> locations = Arrays.asList(pos, pos.add(LatLon.fromDegrees(0.00001, 0.00001)));
                        final double[] elevations = getBestElevations(locations);
                        SwingUtilities.invokeLater(() -> {
                            annotation.setText(String.format("Elevation = %d m", (int) elevations[0]));
                            getWwd().redraw();
                        });
                    });
                    t.start();
                }

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {
                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                }

                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {
                }
            });
        }
    }

    public static void main(String[] args) {
        // Adjust configuration values before instantiation
        ApplicationTemplate.start("WorldWind Get Best Elevations", AppFrame.class);
    }
}

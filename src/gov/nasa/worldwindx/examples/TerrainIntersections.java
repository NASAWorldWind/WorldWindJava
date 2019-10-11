/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Shows how to compute terrain intersections using the highest resolution terrain data available from a globe's
 * elevation model.
 * <p>
 * To generate and show intersections, Shift + LeftClick anywhere on the globe. The program forms a grid of locations
 * around the selected location. The grid points are shown in yellow. It then determines whether a line between the
 * selected location and each grid point intersects the terrain. If it does, the intersection nearest the selected
 * location is shown in cyan and a line is drawn from the selected location to the intersection. If there is no
 * intersection, a line is drawn from the selected location to the grid position.
 * <p>
 * If the highest resolution terrain is not available for the area around the selected location, it is retrieved from
 * the elevation model's source, which is most likely a remote server. Since the high-res data must be retrieved and
 * then loaded from the local disk cache, it will take some time to compute and show the intersections.
 * <p>
 * This example uses a {@link gov.nasa.worldwind.terrain.Terrain} object to perform the terrain retrieval, generation
 * and intersection calculations.s
 *
 * @author tag
 * @version $Id: TerrainIntersections.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class TerrainIntersections extends ApplicationTemplate {

    /**
     * The width and height in degrees of the grid used to calculate intersections.
     */
    protected static final Angle GRID_RADIUS = Angle.fromDegrees(0.05);

    /**
     * The number of cells along each edge of the grid.
     */
    protected static final int GRID_DIMENSION = 10; // cells per side

    /**
     * The desired terrain resolution to use in the intersection calculations.
     */
    protected static final Double TARGET_RESOLUTION = 10d; // meters, or null for globe's highest resolution

    protected static final int NUM_THREADS = 4; // set to 1 to run synchronously

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private static final Cursor WaitCursor = new Cursor(Cursor.WAIT_CURSOR);

        protected HighResolutionTerrain terrain;
        protected RenderableLayer gridLayer;
        protected RenderableLayer intersectionsLayer;
        protected RenderableLayer sightLinesLayer;
        protected RenderableLayer tilesLayer;
        protected Thread calculationDispatchThread;
        protected JProgressBar progressBar;
        protected ThreadPoolExecutor threadPool;
        protected List<Position> grid;
        protected int numGridPoints; // used to monitor percentage progress
        protected long startTime, endTime; // for reporting calculation duration
        protected Position previousCurrentPosition;

        public AppFrame() {
            super(true, true, false);

            // Create a thread pool.
            this.threadPool = new ThreadPoolExecutor(NUM_THREADS, NUM_THREADS, 200, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>());

            // Display a progress bar.
            this.progressBar = new JProgressBar(0, 100);
            this.progressBar.setBorder(new EmptyBorder(0, 10, 0, 10));
            this.progressBar.setBorderPainted(false);
            this.progressBar.setStringPainted(true);
            this.layerPanel.add(this.progressBar, BorderLayout.SOUTH);

            // Be sure to re-use the Terrain object to take advantage of its caching.
            this.terrain = new HighResolutionTerrain(getWwd().getModel().getGlobe(), TARGET_RESOLUTION);

            this.gridLayer = new RenderableLayer();
            this.gridLayer.setName("Grid");
            this.getWwd().getModel().getLayers().add(this.gridLayer);

            this.intersectionsLayer = new RenderableLayer();
            this.intersectionsLayer.setName("Intersections");
            this.getWwd().getModel().getLayers().add(this.intersectionsLayer);

            this.sightLinesLayer = new RenderableLayer();
            this.sightLinesLayer.setName("Sight Lines");
            this.getWwd().getModel().getLayers().add(this.sightLinesLayer);

            // Set up a mouse handler to generate a grid and start intersection calculations when the user shift-clicks.
            this.getWwd().getInputHandler().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    // Control-Click cancels any currently running operation.
                    if ((mouseEvent.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                        if (calculationDispatchThread != null && calculationDispatchThread.isAlive()) {
                            calculationDispatchThread.interrupt();
                        }
                        return;
                    }

                    // Alt-Click repeats the most recent calculations.
                    if ((mouseEvent.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0) {
                        if (previousCurrentPosition == null) {
                            return;
                        }

                        mouseEvent.consume(); // tell the rest of WW that this event has been processed

                        computeAndShowIntersections(previousCurrentPosition);
                        return;
                    }

                    // Perform the intersection tests in response to Shift-Click.
                    if ((mouseEvent.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0) {
                        return;
                    }

                    mouseEvent.consume(); // tell the rest of WW that this event has been processed

                    final Position pos = getWwd().getCurrentPosition();
                    if (pos == null) {
                        return;
                    }

                    computeAndShowIntersections(pos);
                }
            });
        }

        protected void computeAndShowIntersections(final Position curPos) {
            this.previousCurrentPosition = curPos;

            SwingUtilities.invokeLater(() -> {
                setCursor(WaitCursor);
            });

            // Dispatch the calculation threads in a separate thread to avoid locking up the user interface.
            this.calculationDispatchThread = new Thread(() -> {
                try {
                    performIntersectionTests(curPos);
                } catch (InterruptedException e) {
                    System.out.println("Operation was interrupted");
                }
            });

            this.calculationDispatchThread.start();
        }

        // Create containers to hold the intersection points and the lines emanating from the center.
        protected List<Position> firstIntersectionPositions = new ArrayList<>();
        protected List<Position[]> sightLines = new ArrayList<>(GRID_DIMENSION * GRID_DIMENSION);

        // Make the picked location's position and model-coordinate point available to all methods.
        protected Position referencePosition;
        protected Vec4 referencePoint;

        // This is a collection of synchronized accessors to the list updated during the calculations.
        protected synchronized void addIntersectionPosition(Position position) {
            this.firstIntersectionPositions.add(position);
        }

        protected synchronized void addSightLine(Position positionA, Position positionB) {
            this.sightLines.add(new Position[]{positionA, positionB});
        }

        protected synchronized int getSightlinesSize() {
            return this.sightLines.size();
        }

        private long lastTime = System.currentTimeMillis();

        /**
         * Keeps the progress meter current. When calculations are complete, displays the results.
         */
        protected synchronized void updateProgress() {
            // Update the progress bar only once every 250 milliseconds to avoid stealing time from calculations.
            if (this.sightLines.size() >= this.numGridPoints) {
                endTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() < this.lastTime + 250) {
                return;
            }
            this.lastTime = System.currentTimeMillis();

            // On the EDT, update the progress bar and if calculations are complete, update the WorldWindow.
            SwingUtilities.invokeLater(() -> {
                int progress = (int) (100d * getSightlinesSize() / (double) numGridPoints);
                progressBar.setValue(progress);

                if (progress >= 100) {
                    setCursor(Cursor.getDefaultCursor());
                    progressBar.setString((endTime - startTime) + " ms");
                    showResults();
                    System.out.printf("Calculation time %d milliseconds\n", endTime - startTime);
                }
            });
        }

        /**
         * Updates the WorldWind model with the new intersection locations and sight lines.
         */
        protected void showResults() {
            this.showIntersections(firstIntersectionPositions);
            this.showSightLines(sightLines);
//            this.showIntersectingTiles(this.grid, this.referencePosition);
            this.getWwd().redraw();
        }

        protected void performIntersectionTests(final Position curPos) throws InterruptedException {
            // Clear the results lists when the user selects a new location.
            this.firstIntersectionPositions.clear();
            this.sightLines.clear();

            // Raise the selected location and the grid points a little above ground just to show we can.
            final double height = 5; // meters

            // Form the grid.
            double gridRadius = GRID_RADIUS.degrees;
            Sector sector = Sector.fromDegrees(
                    curPos.getLatitude().degrees - gridRadius, curPos.getLatitude().degrees + gridRadius,
                    curPos.getLongitude().degrees - gridRadius, curPos.getLongitude().degrees + gridRadius);

            this.grid = buildGrid(sector, height, GRID_DIMENSION, GRID_DIMENSION);
            this.numGridPoints = grid.size();

            // Compute the position of the selected location (incorporate its height).
            this.referencePosition = new Position(curPos.getLatitude(), curPos.getLongitude(), height);
            this.referencePoint = terrain.getSurfacePoint(curPos.getLatitude(), curPos.getLongitude(), height);

//            // Pre-caching is unnecessary and is useful only when it occurs before the intersection
//            // calculations. It will incur extra overhead otherwise. The normal intersection calculations
//            // cause the same caching, making subsequent calculations on the same area faster.
//            this.preCache(grid, this.referencePosition);
            // On the EDT, show the grid.
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(0);
                progressBar.setString(null);
                clearLayers();
                showGrid(grid, referencePosition);
                getWwd().redraw();
            });

            // Perform the intersection calculations.
            this.startTime = System.currentTimeMillis();
            for (Position gridPos : this.grid) // for each grid point.
            {
                //noinspection ConstantConditions
                if (NUM_THREADS > 0) {
                    this.threadPool.execute(new Intersector(gridPos));
                } else {
                    performIntersection(gridPos);
                }
            }
        }

        /**
         * Performs one line of sight calculation between the reference position and a specified grid position.
         *
         * @param gridPosition the grid position.
         *
         * @throws InterruptedException if the operation is interrupted.
         */
        protected void performIntersection(Position gridPosition) throws InterruptedException {
            // Intersect the line between this grid point and the selected position.
            Intersection[] intersections = this.terrain.intersect(this.referencePosition, gridPosition);
            if (intersections == null || intersections.length == 0) {
                // No intersection, so the line goes from the center to the grid point.
                this.sightLines.add(new Position[]{this.referencePosition, gridPosition});
                return;
            }

            // Only the first intersection is shown.
            Vec4 iPoint = intersections[0].getIntersectionPoint();
            Vec4 gPoint = terrain.getSurfacePoint(gridPosition.getLatitude(), gridPosition.getLongitude(),
                    gridPosition.getAltitude());

            // Check to see whether the intersection is beyond the grid point.
            if (iPoint.distanceTo3(this.referencePoint) >= gPoint.distanceTo3(this.referencePoint)) {
                // Intersection is beyond the grid point; the line goes from the center to the grid point.
                this.addSightLine(this.referencePosition, gridPosition);
                return;
            }

            // Compute the position corresponding to the intersection.
            Position iPosition = this.terrain.getGlobe().computePositionFromPoint(iPoint);

            // The sight line goes from the user-selected position to the intersection position.
            this.addSightLine(this.referencePosition, new Position(iPosition, 0));

            // Keep track of the intersection positions.
            this.addIntersectionPosition(iPosition);

            this.updateProgress();
        }

        /**
         * Inner {@link Runnable} to perform a single line/terrain intersection calculation.
         */
        protected class Intersector implements Runnable {

            protected final Position gridPosition;

            public Intersector(Position gridPosition) {
                this.gridPosition = gridPosition;
            }

            @Override
            public void run() {
                try {
                    performIntersection(this.gridPosition);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        protected List<Position> buildGrid(Sector sector, double height, int nLatCells, int nLonCells) {
            List<Position> grid = new ArrayList<>((nLatCells + 1) * (nLonCells + 1));

            double dLat = sector.getDeltaLatDegrees() / nLatCells;
            double dLon = sector.getDeltaLonDegrees() / nLonCells;

            for (int j = 0; j <= nLatCells; j++) {
                double lat = j == nLatCells
                        ? sector.getMaxLatitude().degrees : sector.getMinLatitude().degrees + j * dLat;

                for (int i = 0; i <= nLonCells; i++) {
                    double lon = i == nLonCells
                            ? sector.getMaxLongitude().degrees : sector.getMinLongitude().degrees + i * dLon;

                    grid.add(Position.fromDegrees(lat, lon, height));
                }
            }

            return grid;
        }

        protected void preCache(List<Position> grid, Position centerPosition) throws InterruptedException {
            // Pre-cache the tiles that will be needed for the intersection calculations.
            double n = 0;
            final long start = System.currentTimeMillis();
            for (Position gridPos : grid) // for each grid point.
            {
                final double progress = 100 * (n++ / grid.size());
                terrain.cacheIntersectingTiles(centerPosition, gridPos);

                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue((int) progress);
                    progressBar.setString(null);
                });
            }

            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(100);
            });

            long end = System.currentTimeMillis();
            System.out.printf("Pre-caching time %d milliseconds, cache usage %f, tiles %d\n", end - start,
                    terrain.getCacheUsage(), terrain.getNumCacheEntries());
        }

        protected void clearLayers() {
            this.intersectionsLayer.removeAllRenderables();
            this.sightLinesLayer.removeAllRenderables();
            this.gridLayer.removeAllRenderables();
        }

        protected void showIntersections(List<Position> intersections) {
            this.intersectionsLayer.removeAllRenderables();

            // Display the intersections as CYAN points.
            PointPlacemarkAttributes intersectionPointAttributes;
            intersectionPointAttributes = new PointPlacemarkAttributes();
            intersectionPointAttributes.setLineMaterial(Material.CYAN);
            intersectionPointAttributes.setScale(6d);
            intersectionPointAttributes.setUsePointAsDefaultImage(true);

            for (Position p : intersections) {
                PointPlacemark pm = new PointPlacemark(p);
                pm.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                pm.setAttributes(intersectionPointAttributes);
                pm.setValue(AVKey.DISPLAY_NAME, p.toString());
                this.intersectionsLayer.addRenderable(pm);
            }
        }

        protected void showSightLines(List<Position[]> sightLines) {
            this.sightLinesLayer.removeAllRenderables();

            // Display the sight lines as green lines.
            ShapeAttributes lineAttributes;
            lineAttributes = new BasicShapeAttributes();
            lineAttributes.setDrawOutline(true);
            lineAttributes.setDrawInterior(false);
            lineAttributes.setOutlineMaterial(Material.GREEN);
            lineAttributes.setOutlineOpacity(0.6);

            for (Position[] pp : sightLines) {
                List<Position> endPoints = new ArrayList<>();
                endPoints.add(pp[0]);
                endPoints.add(pp[1]);

                Path path = new Path(endPoints);
                path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                path.setAttributes(lineAttributes);
                this.sightLinesLayer.addRenderable(path);
            }
        }

        protected void showGridSightLines(List<Position> grid, Position cPos) {
            this.sightLinesLayer.removeAllRenderables();

            // Display lines from the center to each grid point.
            ShapeAttributes lineAttributes;
            lineAttributes = new BasicShapeAttributes();
            lineAttributes.setDrawOutline(true);
            lineAttributes.setDrawInterior(false);
            lineAttributes.setOutlineMaterial(Material.GREEN);
            lineAttributes.setOutlineOpacity(0.6);

            for (Position p : grid) {
                List<Position> endPoints = new ArrayList<>();
                endPoints.add(cPos);
                endPoints.add(new Position(p.getLatitude(), p.getLongitude(), 0));

                Path path = new Path(endPoints);
                path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                path.setAttributes(lineAttributes);
                this.sightLinesLayer.addRenderable(path);
            }
        }

        protected void showGrid(List<Position> grid, Position cPos) {
            this.gridLayer.removeAllRenderables();

            // Display the grid points in yellow.
            PointPlacemarkAttributes gridPointAttributes;
            gridPointAttributes = new PointPlacemarkAttributes();
            gridPointAttributes.setLineMaterial(Material.YELLOW);
            gridPointAttributes.setScale(6d);
            gridPointAttributes.setUsePointAsDefaultImage(true);

            for (Position p : grid) {
                PointPlacemark pm = new PointPlacemark(p);
                pm.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                pm.setAttributes(gridPointAttributes);
                pm.setLineEnabled(true);
                pm.setValue(AVKey.DISPLAY_NAME, p.toString());
                this.gridLayer.addRenderable(pm);
            }

            showCenterPoint(cPos);
        }

        protected void showCenterPoint(Position cPos) {
            // Display the center point in red.
            PointPlacemarkAttributes selectedLocationAttributes;
            selectedLocationAttributes = new PointPlacemarkAttributes();
            selectedLocationAttributes.setLineMaterial(Material.RED);
            selectedLocationAttributes.setScale(8d);
            selectedLocationAttributes.setUsePointAsDefaultImage(true);

            PointPlacemark pm = new PointPlacemark(cPos);
            pm.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pm.setAttributes(selectedLocationAttributes);
            pm.setValue(AVKey.DISPLAY_NAME, cPos.toString());
            pm.setLineEnabled(true);
            this.gridLayer.addRenderable(pm);
        }
    }

    public static void main(String[] args) {
        // zoom to San Francisco downtown
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 34e3);
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 37.9521d);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -119.7761d);

        // Adjust configuration values before instantiation
        ApplicationTemplate.start("WorldWind Terrain Intersections", AppFrame.class);
    }
}

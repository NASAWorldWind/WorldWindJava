/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.lineofsight;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.*;
import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwind.util.VecBuffer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Computes and displays line-of-sight intersections for terrain and renderables. Uses a {@link Terrain} object in order
 * to determine accurate intersections relative to the highest-resolution elevation data associated with a specified
 * globe.
 * <p/>
 * This class uses a {@link gov.nasa.worldwindx.examples.lineofsight.TerrainLineIntersector} and a {@link
 * gov.nasa.worldwindx.examples.lineofsight.ShapeLineIntersector} to compute the intersections.
 * <p/>
 * <em>Usage:</em> <br/> Shift-click: Calculate lines of sight for a position. <br/> Ctrl-click: Cancel the running
 * computation. <br/> Alt-click: Re-run the most recent line of sight calculation.
 *
 * @author tag
 * @version $Id: LinesOfSight.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class LinesOfSight extends ApplicationTemplate
{
    /** The width and height in degrees of the grid used to calculate intersections. */
    protected static final Angle GRID_RADIUS = Angle.fromDegrees(0.005);

    /** The number of cells along each edge of the grid. */
    protected static final int GRID_DIMENSION = 10; // cells per side

    protected static final int REFERENCE_POSITION_HEIGHT = 5;
    protected static final int GRID_POSITION_HEIGHT = 1;

    /** The desired terrain resolution to use in the intersection calculations. */
    protected static final Double TARGET_RESOLUTION = 20d; // meters, or null for globe's highest resolution

    protected static final int NUM_TERRAIN_THREADS = 1; // set to 1 to run terrain intersections synchronously
    protected static final int NUM_SHAPE_THREADS = 1; // set to 1 to run shape intersections synchronously

    /** The size of the Terrain's cache. * */
    protected static final long CACHE_SIZE = (long) 150e6;

    protected static final boolean SHOW_ONLY_FIRST_INTERSECTIONS = true;

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private static final Cursor WaitCursor = new Cursor(Cursor.WAIT_CURSOR);

        protected HighResolutionTerrain terrain;
        protected TerrainLineIntersector terrainIntersector;
        protected ShapeLineIntersector shapeIntersector;
        protected RenderableLayer gridLayer;
        protected RenderableLayer intersectionsLayer;
        protected RenderableLayer sightLinesLayer;
        protected RenderableLayer tilesLayer;
        protected Thread calculationDispatchThread;
        protected JProgressBar progressBar;
        protected java.util.List<Position> grid;
        protected Position referencePosition;
        protected Vec4 referencePoint;
        protected long startTime, endTime; // for reporting calculation duration
        protected Position previousCurrentPosition;
        protected java.util.Timer updateProgressTimer;
        protected AtomicInteger numPositionsProcessed = new AtomicInteger();
        protected RenderableLayer renderableLayer = new RenderableLayer();
        protected ShapeAttributes sightLineAttributes;
        protected PointPlacemarkAttributes intersectionPointAttributes;
        protected PointPlacemarkAttributes gridPointAttributes;
        protected PointPlacemarkAttributes selectedLocationAttributes;

        public AppFrame()
        {
            super(true, true, false);

            this.makeMenu();

            this.updateProgressTimer = new java.util.Timer();

            // Display a progress bar.
            this.progressBar = new JProgressBar(0, 100);
            this.progressBar.setBorder(new EmptyBorder(0, 10, 0, 10));
            this.progressBar.setBorderPainted(false);
            this.progressBar.setStringPainted(true);
            this.layerPanel.add(this.progressBar, BorderLayout.SOUTH);

            // Create the layer showing the grid.
            this.gridLayer = new RenderableLayer();
            this.gridLayer.setName("Grid");
            this.getWwd().getModel().getLayers().add(this.gridLayer);

            // Create the layer showing the intersections.
            this.intersectionsLayer = new RenderableLayer();
            this.intersectionsLayer.setName("Intersections");
            this.getWwd().getModel().getLayers().add(this.intersectionsLayer);

            // Create the layer showing the sight lines.
            this.sightLinesLayer = new RenderableLayer();
            this.sightLinesLayer.setName("Sight Lines");
            this.getWwd().getModel().getLayers().add(this.sightLinesLayer);

            // Create a Terrain object that uses high-resolution elevation data to compute intersections.
            this.terrain = new HighResolutionTerrain(this.getWwd().getModel().getGlobe(), TARGET_RESOLUTION);
            this.terrain.setCacheCapacity(CACHE_SIZE); // larger cache speeds up repeat calculations

            // Create the intersectors for terrain and shapes.
            this.terrainIntersector = new TerrainLineIntersector(this.terrain, NUM_TERRAIN_THREADS);
            this.shapeIntersector = new ShapeLineIntersector(this.terrain, NUM_SHAPE_THREADS);

            // Set up a mouse handler to generate a grid and start intersection calculations when the user shift-clicks.
            this.getWwd().getInputHandler().addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent mouseEvent)
                {
                    // Control-Click cancels any currently running operation.
                    if ((mouseEvent.getModifiers() & ActionEvent.CTRL_MASK) != 0)
                    {
                        if (calculationDispatchThread != null && calculationDispatchThread.isAlive())
                            calculationDispatchThread.interrupt();
                        return;
                    }

                    // Alt-Click repeats the most recent calculations.
                    if ((mouseEvent.getModifiers() & ActionEvent.ALT_MASK) != 0)
                    {
                        if (previousCurrentPosition == null)
                            return;

                        mouseEvent.consume(); // tell the rest of WW that this event has been processed

                        computeAndShow(previousCurrentPosition);
                        return;
                    }

                    // Perform the intersection tests in response to Shift-Click.
                    if ((mouseEvent.getModifiers() & ActionEvent.SHIFT_MASK) == 0)
                        return;

                    mouseEvent.consume(); // tell the rest of WW that this event has been processed

                    final Position pos = getWwd().getCurrentPosition();
                    if (pos == null)
                        return;

                    computeAndShow(pos);
                }
            });

            // Display the grid points in yellow.
            this.gridPointAttributes = new PointPlacemarkAttributes();
            this.gridPointAttributes.setLineMaterial(Material.YELLOW);
            this.gridPointAttributes.setScale(6d);
            this.gridPointAttributes.setUsePointAsDefaultImage(true);

            // Display the center point in red.
            this.selectedLocationAttributes = new PointPlacemarkAttributes();
            this.selectedLocationAttributes.setLineMaterial(Material.RED);
            this.selectedLocationAttributes.setScale(8d);
            this.selectedLocationAttributes.setUsePointAsDefaultImage(true);

            // Display the sight lines as green lines.
            this.sightLineAttributes = new BasicShapeAttributes();
            this.sightLineAttributes.setDrawOutline(true);
            this.sightLineAttributes.setDrawInterior(false);
            this.sightLineAttributes.setOutlineMaterial(Material.GREEN);
            this.sightLineAttributes.setOutlineOpacity(0.6);

            // Display the intersections as CYAN points.
            this.intersectionPointAttributes = new PointPlacemarkAttributes();
            this.intersectionPointAttributes.setLineMaterial(Material.CYAN);
            this.intersectionPointAttributes.setScale(10d);
            this.intersectionPointAttributes.setUsePointAsDefaultImage(true);
        }

        protected void computeAndShow(final Position curPos)
        {
            this.previousCurrentPosition = curPos;

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    setCursor(WaitCursor);
                }
            });

            // Dispatch the calculation threads in a separate thread to avoid locking up the user interface.
            this.calculationDispatchThread = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        performIntersectionTests(curPos);
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Operation was interrupted");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });

            this.calculationDispatchThread.start();
        }

        protected void performIntersectionTests(final Position curPos) throws InterruptedException
        {
            // Compute the position of the selected location (incorporate its height).
            this.referencePosition = new Position(curPos.getLatitude(), curPos.getLongitude(),
                REFERENCE_POSITION_HEIGHT);
            this.referencePoint = this.terrain.getSurfacePoint(this.referencePosition);

            // Form the grid.
            Sector sector = this.computeGridSector(curPos, GRID_RADIUS.degrees);
            this.grid = buildGrid(sector, GRID_POSITION_HEIGHT, GRID_DIMENSION, GRID_DIMENSION);

            // Set the line definitions.
            this.terrainIntersector.setReferencePosition(this.referencePosition);
            this.terrainIntersector.setPositions(this.grid);

            // Add the renderables, if any, to the shape intersector.
            if (this.renderableLayer.getNumRenderables() > 0)
            {
                this.shapeIntersector.setReferencePosition(this.referencePosition);
                this.shapeIntersector.setPositions(this.grid);
                this.shapeIntersector.setRenderables(this.renderableLayer.getRenderables());
            }

            // On the EDT, show the grid.
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    progressBar.setValue(0);
                    progressBar.setString(null);
                    clearLayers();
                    showGrid(grid, referencePosition);
                    getWwd().redraw();
                }
            });

            if (this.updateProgressTimer != null)
                this.updateProgressTimer.cancel();

            this.updateProgressTimer = new java.util.Timer();
            this.updateProgressTimer.schedule(new TimerTask()
            {
                public void run()
                {
                    updateProgress();
                }
            }, 500, 250);

            // Perform the intersection calculations.
            this.startTime = System.currentTimeMillis();
            this.computeIntersections();
        }

        protected void clearLayers()
        {
            this.intersectionsLayer.removeAllRenderables();
            this.sightLinesLayer.removeAllRenderables();
            this.gridLayer.removeAllRenderables();
        }

        protected void computeIntersections()
        {
            Thread terrainThread = new Thread(this.terrainIntersector);
            terrainThread.start();

            if (this.shapeIntersector.hasRenderables())
            {
                Thread shapeThread = new Thread(this.shapeIntersector);
                shapeThread.start();
            }
        }

        protected Sector computeGridSector(Position curPos, double gridRadius)
        {
            return Sector.fromDegrees(
                curPos.getLatitude().degrees - gridRadius, curPos.getLatitude().degrees + gridRadius,
                curPos.getLongitude().degrees - gridRadius, curPos.getLongitude().degrees + gridRadius);
        }

        protected List<Position> buildGrid(Sector sector, double height, int nRows, int nCols)
        {
            java.util.List<Position> grid = new ArrayList<Position>((nRows) * (nCols));

            double dLat = sector.getDeltaLatDegrees() / (nCols - 1);
            double dLon = sector.getDeltaLonDegrees() / (nRows - 1);

            for (int j = 0; j < nRows; j++)
            {
                double lat = j == nRows - 1 ?
                    sector.getMaxLatitude().degrees : sector.getMinLatitude().degrees + j * dLat;

                for (int i = 0; i < nCols; i++)
                {
                    double lon = i == nCols - 1 ?
                        sector.getMaxLongitude().degrees : sector.getMinLongitude().degrees + i * dLon;

                    grid.add(Position.fromDegrees(lat, lon, height));
                }
            }

            return grid;
        }

        /** Keeps the progress meter current. When calculations are complete, displays the results. */
        protected synchronized void updateProgress()
        {
            int totalNum = this.grid.size();
            int numPositionsProcessed = this.terrainIntersector.getNumProcessedPositions();

            if (this.renderableLayer.getNumRenderables() > 0)
            {
                totalNum += this.grid.size();
                numPositionsProcessed += this.shapeIntersector.getNumProcessedPositions();
            }

            final int progress = (int) (100d * numPositionsProcessed / (double) totalNum);

            // On the EDT, update the progress bar and if calculations are complete, update the World Window.
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    progressBar.setValue(progress);

                    if (progress >= 100)
                    {
                        endTime = System.currentTimeMillis();
                        updateProgressTimer.cancel();
                        updateProgressTimer = null;
                        setCursor(Cursor.getDefaultCursor());
                        progressBar.setString((endTime - startTime) + " ms");
                        showResults();
                        System.out.printf("Calculation time %d milliseconds\n", endTime - startTime);
                    }
                }
            });
        }

        /** Updates the World Wind model with the new intersection locations and sight lines. */
        protected void showResults()
        {
            this.intersectionsLayer.removeAllRenderables();
            this.sightLinesLayer.removeAllRenderables();

            for (Position position : this.grid)
            {
                this.showIntersectionsForPosition(position);
            }

            this.getWwd().redraw();
        }

        protected void showIntersectionsForPosition(Position position)
        {
            List<Intersection> tIntersections = this.terrainIntersector.getIntersections(position);
            List<Intersection> sIntersections = this.shapeIntersector.getIntersections(position);

            if (tIntersections == null && sIntersections == null)
            {
                this.showNonIntersection(position);
                return;
            }

            Queue<Intersection> sortedIntersections = Intersection.sort(this.referencePoint, tIntersections,
                sIntersections);

            if (sortedIntersections.size() == 0)
            {
                this.showSightLine(position);
            }
            else if (SHOW_ONLY_FIRST_INTERSECTIONS)
            {
                this.showSightLine(sortedIntersections.peek().getIntersectionPosition());
                this.showIntersection(sortedIntersections.peek());
            }
            else
            {
                this.showSightLine(position);
                this.showIntersections(sortedIntersections);
            }
        }

        protected void showSightLine(Position position)
        {
            Position refPosAbsolute = this.referencePosition;

            Path path = new Path(refPosAbsolute, position);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setAttributes(this.sightLineAttributes);
            this.sightLinesLayer.addRenderable(path);
        }

        protected void showIntersection(Intersection losi)
        {
            PointPlacemark pm = new PointPlacemark(losi.getIntersectionPosition());
            pm.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pm.setAttributes(this.intersectionPointAttributes);
            pm.setValue(AVKey.DISPLAY_NAME, losi.getIntersectionPosition().toString());
            this.intersectionsLayer.addRenderable(pm);
        }

        protected void showIntersections(Queue<Intersection> intersections)
        {
            for (Intersection losi : intersections)
            {
                PointPlacemark pm = new PointPlacemark(losi.getIntersectionPosition());
                pm.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                pm.setAttributes(this.intersectionPointAttributes);
                pm.setValue(AVKey.DISPLAY_NAME, losi.getIntersectionPosition().toString());
                this.intersectionsLayer.addRenderable(pm);
            }
        }

        protected void showNonIntersection(Position position)
        {
            Path path = new Path(this.referencePosition, position);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setAttributes(this.sightLineAttributes);
            this.sightLinesLayer.addRenderable(path);
        }

        protected void showNonIntersections(Collection<Position> positions)
        {
            for (Position pos : positions)
            {
                Path path = new Path(this.referencePosition, pos);
                path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                path.setAttributes(this.sightLineAttributes);
                this.sightLinesLayer.addRenderable(path);
            }
        }

        protected void showGrid(java.util.List<Position> grid, Position cPos)
        {
            this.gridLayer.removeAllRenderables();

            // Display the grid points in yellow.
            PointPlacemarkAttributes gridPointAttributes;
            gridPointAttributes = new PointPlacemarkAttributes();
            gridPointAttributes.setLineMaterial(Material.YELLOW);
            gridPointAttributes.setScale(6d);
            gridPointAttributes.setUsePointAsDefaultImage(true);

            for (Position p : grid)
            {
                PointPlacemark pm = new PointPlacemark(p);
                pm.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                pm.setAttributes(this.gridPointAttributes);
                pm.setLineEnabled(true);
                pm.setValue(AVKey.DISPLAY_NAME, p.toString());
                this.gridLayer.addRenderable(pm);
            }

            showCenterPoint(cPos);
        }

        protected void showCenterPoint(Position cPos)
        {
            // Display the center point in red.
            PointPlacemarkAttributes selectedLocationAttributes;
            selectedLocationAttributes = new PointPlacemarkAttributes();
            selectedLocationAttributes.setLineMaterial(Material.RED);
            selectedLocationAttributes.setScale(8d);
            selectedLocationAttributes.setUsePointAsDefaultImage(true);

            PointPlacemark pm = new PointPlacemark(cPos);
            pm.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pm.setAttributes(this.selectedLocationAttributes);
            pm.setValue(AVKey.DISPLAY_NAME, cPos.toString());
            pm.setLineEnabled(true);
            this.gridLayer.addRenderable(pm);
        }

        /** Makes the menu for loading shapes from shapefiles. */
        protected void makeMenu()
        {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ESRI Shapefiles", "shp"));

            JMenuBar menuBar = new JMenuBar();
            this.setJMenuBar(menuBar);
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);
            JMenuItem openMenuItem = new JMenuItem(new AbstractAction("Open File...")
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    try
                    {
                        int status = fileChooser.showOpenDialog(AppFrame.this);
                        if (status == JFileChooser.APPROVE_OPTION)
                        {
                            Thread t = new ShapeLoaderThread(fileChooser.getSelectedFile(), getWwd(), renderableLayer,
                                AppFrame.this.layerPanel);
                            t.start();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });

            fileMenu.add(openMenuItem);
        }
    }

    public static class ShapeLoaderThread extends Thread
    {
        protected File file;
        protected WorldWindow wwd;
        protected LayerPanel layerPanel;
        protected RenderableLayer layer;
        protected ShapeAttributes buildingAttributes;

        public ShapeLoaderThread(File file, WorldWindow wwd, RenderableLayer layer, LayerPanel layerPanel)
        {
            this.file = file;
            this.wwd = wwd;
            this.layer = layer;
            this.layerPanel = layerPanel;

            // Display the buildings slightly transparent.
            this.buildingAttributes = new BasicShapeAttributes();
            this.buildingAttributes.setDrawOutline(true);
            this.buildingAttributes.setDrawInterior(true);
            this.buildingAttributes.setInteriorMaterial(Material.LIGHT_GRAY);
            this.buildingAttributes.setInteriorOpacity(0.4);
        }

        public void run()
        {
            Shapefile sf = new Shapefile(this.file);

            try
            {
                while (sf.hasNext())
                {
                    ShapefileRecord r = sf.nextRecord();
                    if (r == null)
                        continue;

                    if (r.getNumberOfPoints() < 4)
                        continue;

                    this.layer.addRenderable(this.makeShape(r));
                }
            }
            finally
            {
                sf.close();
            }

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    insertBeforePlacenames(wwd, layer);
                }
            });
        }

        protected ExtrudedPolygon makeShape(ShapefileRecord record)
        {
            Double height = null;
            Object o = record.getAttributes().getValue("Height");
            if (o != null)
            {
                height = Double.parseDouble(o.toString());
            }

            ExtrudedPolygon pgon = new ExtrudedPolygon();
            pgon.setSideAttributes(this.buildingAttributes);
            pgon.setCapAttributes(this.buildingAttributes);
            VecBuffer vb = record.getPointBuffer(0);
            pgon.setOuterBoundary(vb.getLocations(), height);

            return pgon;
        }
    }

    public static void main(String[] args)
    {
        // Adjust configuration values before instantiation
        ApplicationTemplate.start("World Wind Terrain Intersections", AppFrame.class);
    }
}
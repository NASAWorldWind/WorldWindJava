/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.GraticuleRenderingParams;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * Displays the UTM graticule.
 *
 * @author Patrick Murris
 * @version $Id: UTMGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */
public class UTMGraticuleLayer extends UTMBaseGraticuleLayer
{
    /** Graticule for the UTM zone grid. */
    public static final String GRATICULE_UTM_GRID = "Graticule.UTM.Grid";
    /** Graticule for the 100,000 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_100000M = "Graticule.100000m";
    /** Graticule for the 10,000 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_10000M = "Graticule.10000m";
    /** Graticule for the 1,000 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_1000M = "Graticule.1000m";
    /** Graticule for the 100 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_100M = "Graticule.100m";
    /** Graticule for the 10 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_10M = "Graticule.10m";
    /** Graticule for the 1 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_1M = "Graticule.1m";

    protected static final int MIN_CELL_SIZE_PIXELS = 40; // TODO: make settable
    protected static final int GRID_ROWS = 8;
    protected static final int GRID_COLS = 60;

    protected GraticuleTile[][] gridTiles = new GraticuleTile[GRID_ROWS][GRID_COLS];

    public UTMGraticuleLayer()
    {
        initRenderingParams();
        this.setPickEnabled(false);
        this.setName(Logging.getMessage("layers.Earth.UTMGraticule.Name"));
        this.metricScaleSupport.setMaxResolution(1e6);
    }

    // --- Graticule Rendering --------------------------------------------------------------

    protected void initRenderingParams()
    {
        GraticuleRenderingParams params;
        // UTM zone grid
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.WHITE);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.WHITE);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-16"));
        setRenderingParams(GRATICULE_UTM_GRID, params);
        // 100km
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.GREEN);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.GREEN);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-14"));
        setRenderingParams(GRATICULE_100000M, params);
        // 10km
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0, 102, 255));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(0, 102, 255));
        setRenderingParams(GRATICULE_10000M, params);
        // 1km
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.CYAN);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.CYAN);
        setRenderingParams(GRATICULE_1000M, params);
        // 100m
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0, 153, 153));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(0, 153, 153));
        setRenderingParams(GRATICULE_100M, params);
        // 10m
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(102, 255, 204));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(102, 255, 204));
        setRenderingParams(GRATICULE_10M, params);
        // 1m
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(153, 153, 255));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(153, 153, 255));
        setRenderingParams(GRATICULE_1M, params);
    }

    protected String[] getOrderedTypes()
    {
        return new String[] {
            GRATICULE_UTM_GRID,
            GRATICULE_100000M,
            GRATICULE_10000M,
            GRATICULE_1000M,
            GRATICULE_100M,
            GRATICULE_10M,
            GRATICULE_1M,
        };
    }

    protected String getTypeFor(int resolution)
    {
        if (resolution >= 500000)
            return GRATICULE_UTM_GRID;
        if (resolution >= 100000)
            return GRATICULE_100000M;
        else if (resolution >= 10000)
            return GRATICULE_10000M;
        else if (resolution >= 1000)
            return GRATICULE_1000M;
        else if (resolution >= 100)
            return GRATICULE_100M;
        else if (resolution >= 10)
            return GRATICULE_10M;
        else if (resolution >= 1)
            return GRATICULE_1M;

        return null;
    }

    protected void clear(DrawContext dc)
    {
        super.clear(dc);
        this.applyTerrainConformance();
        this.metricScaleSupport.clear();
        this.metricScaleSupport.computeZone(dc);
    }

    private void applyTerrainConformance()
    {
        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType)
        {
            double lineConformance = type.equals(GRATICULE_UTM_GRID) ? 20 : this.terrainConformance;
            getRenderingParams(type).setValue(GraticuleRenderingParams.KEY_LINE_CONFORMANCE, lineConformance);
        }
    }

    protected void selectRenderables(DrawContext dc)
    {
        this.selectUTMRenderables(dc);
        this.metricScaleSupport.selectRenderables(dc);
    }

    /**
     * Select the visible grid elements
     *
     * @param dc the current <code>DrawContext</code>.
     */
    protected void selectUTMRenderables(DrawContext dc)
    {
        ArrayList<GraticuleTile> tileList = getVisibleTiles(dc);
        if (tileList.size() > 0)
        {
            for (GraticuleTile gt : tileList)
            {
                // Select tile visible elements
                gt.selectRenderables(dc);
            }
        }
    }

    protected ArrayList<GraticuleTile> getVisibleTiles(DrawContext dc)
    {
        ArrayList<GraticuleTile> tileList = new ArrayList<GraticuleTile>();
        Sector vs = dc.getVisibleSector();
        if (vs != null)
        {
            Rectangle2D gridRectangle = getGridRectangleForSector(vs);
            if (gridRectangle != null)
            {
                for (int row = (int) gridRectangle.getY(); row <= gridRectangle.getY() + gridRectangle.getHeight();
                    row++)
                {
                    for (int col = (int) gridRectangle.getX(); col <= gridRectangle.getX() + gridRectangle.getWidth();
                        col++)
                    {
                        if (gridTiles[row][col] == null)
                            gridTiles[row][col] = new GraticuleTile(getGridSector(row, col));
                        if (gridTiles[row][col].isInView(dc))
                            tileList.add(gridTiles[row][col]);
                        else
                            gridTiles[row][col].clearRenderables();
                    }
                }
            }
        }
        return tileList;
    }

    private Rectangle2D getGridRectangleForSector(Sector sector)
    {
        int x1 = getGridColumn(sector.getMinLongitude().degrees);
        int x2 = getGridColumn(sector.getMaxLongitude().degrees);
        int y1 = getGridRow(sector.getMinLatitude().degrees);
        int y2 = getGridRow(sector.getMaxLatitude().degrees);
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    private Sector getGridSector(int row, int col)
    {
        double deltaLat = UTM_MAX_LATITUDE * 2 / GRID_ROWS;
        double deltaLon = 360 / GRID_COLS;
        double minLat = row == 0 ? UTM_MIN_LATITUDE : -UTM_MAX_LATITUDE + deltaLat * row;
        double maxLat = -UTM_MAX_LATITUDE + deltaLat * (row + 1);
        double minLon = -180 + deltaLon * col;
        double maxLon = minLon + deltaLon;
        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    private int getGridColumn(double longitude)
    {
        double deltaLon = 360 / GRID_COLS;
        int col = (int) Math.floor((longitude + 180) / deltaLon);
        return Math.min(col, GRID_COLS - 1);
    }

    private int getGridRow(double latitude)
    {
        double deltaLat = UTM_MAX_LATITUDE * 2 / GRID_ROWS;
        int row = (int) Math.floor((latitude + UTM_MAX_LATITUDE) / deltaLat);
        return Math.max(0, Math.min(row, GRID_ROWS - 1));
    }

    protected void clearTiles()
    {
        for (int row = 0; row < 2; row++)
        {
            for (int col = 0; col < 60; col++)
            {
                if (this.gridTiles[row][col] != null)
                {
                    this.gridTiles[row][col].clearRenderables();
                    this.gridTiles[row][col] = null;
                }
            }
        }
    }

    // --- Graticule tile ----------------------------------------------------------------------

    protected class GraticuleTile
    {
        private Sector sector;
        private int zone;
        private String hemisphere;

        private ArrayList<GridElement> gridElements;
        private ArrayList<SquareZone> squares;

        public GraticuleTile(Sector sector)
        {
            this.sector = sector;
            this.zone = getGridColumn(this.sector.getCentroid().getLongitude().degrees) + 1;
            this.hemisphere = this.sector.getCentroid().latitude.degrees > 0 ? AVKey.NORTH : AVKey.SOUTH;
        }

        public Extent getExtent(Globe globe, double ve)
        {
            return Sector.computeBoundingCylinder(globe, ve, this.sector);
        }

        @SuppressWarnings({"RedundantIfStatement"})
        public boolean isInView(DrawContext dc)
        {
            if (!dc.getView().getFrustumInModelCoordinates().intersects(
                this.getExtent(dc.getGlobe(), dc.getVerticalExaggeration())))
                return false;

            return true;
        }

        public double getSizeInPixels(DrawContext dc)
        {
            View view = dc.getView();
            Vec4 centerPoint = getSurfacePoint(dc, this.sector.getCentroid().getLatitude(),
                this.sector.getCentroid().getLongitude());
            double distance = view.getEyePoint().distanceTo3(centerPoint);
            double tileSizeMeter = this.sector.getDeltaLatRadians() * dc.getGlobe().getRadius();
            return tileSizeMeter / view.computePixelSizeAtDistance(distance);
        }

        public void selectRenderables(DrawContext dc)
        {
            if (this.gridElements == null)
                this.createRenderables();

            // Select tile grid elements
            int resolution = 500000;  // Top level 6 degrees zones
            String graticuleType = getTypeFor(resolution);
            for (GridElement ge : this.gridElements)
            {
                if (ge.isInView(dc))
                    addRenderable(ge.renderable, graticuleType);
            }

            if (getSizeInPixels(dc) / 10 < MIN_CELL_SIZE_PIXELS * 2)
                return;

            // Select child elements
            if (this.squares == null)
                createSquares();
            for (SquareZone sz : this.squares)
            {
                if (sz.isInView(dc))
                {
                    sz.selectRenderables(dc, dc.getVisibleSector());
                }
                else
                    sz.clearRenderables();
            }
        }

        public void clearRenderables()
        {
            if (this.gridElements != null)
            {
                this.gridElements.clear();
                this.gridElements = null;
            }
            if (this.squares != null)
            {
                for (SquareZone sz : this.squares)
                {
                    sz.clearRenderables();
                }
                this.squares.clear();
                this.squares = null;
            }
        }

        private void createSquares()
        {
            try
            {
                // Find grid zone easting and northing boundaries
                UTMCoord UTM;
                UTM = UTMCoord.fromLatLon(this.sector.getMinLatitude(), this.sector.getCentroid().getLongitude(),
                    globe);
                double minNorthing = UTM.getNorthing();
                UTM = UTMCoord.fromLatLon(this.sector.getMaxLatitude(), this.sector.getCentroid().getLongitude(),
                    globe);
                double maxNorthing = UTM.getNorthing();
                maxNorthing = maxNorthing == 0 ? 10e6 : maxNorthing;
                UTM = UTMCoord.fromLatLon(this.sector.getMinLatitude(), this.sector.getMinLongitude(), globe);
                double minEasting = UTM.getEasting();
                UTM = UTMCoord.fromLatLon(this.sector.getMaxLatitude(), this.sector.getMinLongitude(), globe);
                minEasting = UTM.getEasting() < minEasting ? UTM.getEasting() : minEasting;
                double maxEasting = 1e6 - minEasting;

                // Create squares
                this.squares = createSquaresGrid(this.zone, this.hemisphere, this.sector, minEasting, maxEasting,
                    minNorthing, maxNorthing);
            }
            catch (IllegalArgumentException ignore)
            {
            }
        }

        /** Create the grid elements */
        private void createRenderables()
        {
            this.gridElements = new ArrayList<GridElement>();

            ArrayList<Position> positions = new ArrayList<Position>();

            // Generate west meridian
            positions.clear();
            positions.add(new Position(this.sector.getMinLatitude(), this.sector.getMinLongitude(), 0));
            positions.add(new Position(this.sector.getMaxLatitude(), this.sector.getMinLongitude(), 0));
            Object polyline = createLineRenderable(new ArrayList<Position>(positions), AVKey.LINEAR);
            Sector lineSector = new Sector(this.sector.getMinLatitude(), this.sector.getMaxLatitude(),
                this.sector.getMinLongitude(), this.sector.getMinLongitude());
            GridElement ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE);
            ge.value = this.sector.getMinLongitude().degrees;
            this.gridElements.add(ge);

            // Generate south parallel at south pole and equator
            if (this.sector.getMinLatitude().degrees == UTM_MIN_LATITUDE || this.sector.getMinLatitude().degrees == 0)
            {
                positions.clear();
                positions.add(new Position(this.sector.getMinLatitude(), this.sector.getMinLongitude(), 0));
                positions.add(new Position(this.sector.getMinLatitude(), this.sector.getMaxLongitude(), 0));
                polyline = createLineRenderable(new ArrayList<Position>(positions), AVKey.LINEAR);
                lineSector = new Sector(this.sector.getMinLatitude(), this.sector.getMinLatitude(),
                    this.sector.getMinLongitude(), this.sector.getMaxLongitude());
                ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE);
                ge.value = this.sector.getMinLatitude().degrees;
                this.gridElements.add(ge);
            }

            // Generate north parallel at north pole
            if (this.sector.getMaxLatitude().degrees == UTM_MAX_LATITUDE)
            {
                positions.clear();
                positions.add(new Position(this.sector.getMaxLatitude(), this.sector.getMinLongitude(), 0));
                positions.add(new Position(this.sector.getMaxLatitude(), this.sector.getMaxLongitude(), 0));
                polyline = createLineRenderable(new ArrayList<Position>(positions), AVKey.LINEAR);
                lineSector = new Sector(this.sector.getMaxLatitude(), this.sector.getMaxLatitude(),
                    this.sector.getMinLongitude(), this.sector.getMaxLongitude());
                ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE);
                ge.value = this.sector.getMaxLatitude().degrees;
                this.gridElements.add(ge);
            }

            // Add label
            if (this.hasLabel())
            {
                StringBuilder sb = new StringBuilder();
                sb.append(this.zone).append(AVKey.NORTH.equals(this.hemisphere) ? "N" : "S");
                GeographicText text = new UserFacingText(sb.toString(), new Position(this.sector.getCentroid(), 0));
                this.gridElements.add(new GridElement(this.sector, text, GridElement.TYPE_GRIDZONE_LABEL));
            }
        }

        private boolean hasLabel()
        {
            // Has label if it contains hemisphere mid latitude
            double southLat = UTM_MIN_LATITUDE / 2;
            boolean southLabel = this.sector.getMinLatitude().degrees < southLat
                && southLat <= this.sector.getMaxLatitude().degrees;

            double northLat = UTM_MAX_LATITUDE / 2;
            boolean northLabel = this.sector.getMinLatitude().degrees < northLat
                && northLat <= this.sector.getMaxLatitude().degrees;

            return southLabel || northLabel;
        }
    }
}
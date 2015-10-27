/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * Displays the geographic latitude/longitude graticule.
 *
 * @author Patrick Murris
 * @version $Id: LatLonGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */
public class LatLonGraticuleLayer extends AbstractGraticuleLayer
{
    public static final String GRATICULE_LATLON_LEVEL_0 = "Graticule.LatLonLevel0";
    public static final String GRATICULE_LATLON_LEVEL_1 = "Graticule.LatLonLevel1";
    public static final String GRATICULE_LATLON_LEVEL_2 = "Graticule.LatLonLevel2";
    public static final String GRATICULE_LATLON_LEVEL_3 = "Graticule.LatLonLevel3";
    public static final String GRATICULE_LATLON_LEVEL_4 = "Graticule.LatLonLevel4";
    public static final String GRATICULE_LATLON_LEVEL_5 = "Graticule.LatLonLevel5";

    protected static final int MIN_CELL_SIZE_PIXELS = 40; // TODO: make settable

    protected GraticuleTile[][] gridTiles = new GraticuleTile[18][36]; // 10 degrees row/col
    protected ArrayList<Double> latitudeLabels = new ArrayList<Double>();
    protected ArrayList<Double> longitudeLabels = new ArrayList<Double>();
    private String angleFormat = Angle.ANGLE_FORMAT_DMS;

    public LatLonGraticuleLayer()
    {
        initRenderingParams();
        this.setPickEnabled(false);
        this.setName(Logging.getMessage("layers.LatLonGraticule.Name"));
    }

    /**
     * Get the graticule division and angular display format. Can be one of {@link Angle#ANGLE_FORMAT_DD} or
     * {@link Angle#ANGLE_FORMAT_DMS}.
     *
     * @return the graticule division and angular display format.
     */
    public String getAngleFormat()
    {
        return this.angleFormat;
    }

    /**
     * Sets the graticule division and angular display format. Can be one of {@link Angle#ANGLE_FORMAT_DD},
     * {@link Angle#ANGLE_FORMAT_DMS} of {@link Angle#ANGLE_FORMAT_DM}.
     *
     * @param format the graticule division and angular display format.
     *
     * @throws IllegalArgumentException is <code>format</code> is null.
     */
    public void setAngleFormat(String format)
    {
        if (format == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.angleFormat.equals(format))
            return;

        this.angleFormat = format;
        this.clearTiles();
        this.lastEyePoint = null; // force graticule to update
    }

    // --- Graticule Rendering --------------------------------------------------------------

    protected void initRenderingParams()
    {
        GraticuleRenderingParams params;
        // Ten degrees grid
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.WHITE);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.WHITE);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-16"));
        setRenderingParams(GRATICULE_LATLON_LEVEL_0, params);
        // One degree
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.GREEN);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.GREEN);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-14"));
        setRenderingParams(GRATICULE_LATLON_LEVEL_1, params);
        // 1/10th degree - 1/6th (10 minutes)
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0, 102, 255));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(0, 102, 255));
        setRenderingParams(GRATICULE_LATLON_LEVEL_2, params);
        // 1/100th degree - 1/60th (one minutes)
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.CYAN);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.CYAN);
        setRenderingParams(GRATICULE_LATLON_LEVEL_3, params);
        // 1/1000 degree - 1/360th (10 seconds)
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0, 153, 153));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(0, 153, 153));
        setRenderingParams(GRATICULE_LATLON_LEVEL_4, params);
        // 1/10000 degree - 1/3600th (one second)
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(102, 255, 204));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(102, 255, 204));
        setRenderingParams(GRATICULE_LATLON_LEVEL_5, params);
    }

    protected String[] getOrderedTypes()
    {
        return new String[] {
            GRATICULE_LATLON_LEVEL_0,
            GRATICULE_LATLON_LEVEL_1,
            GRATICULE_LATLON_LEVEL_2,
            GRATICULE_LATLON_LEVEL_3,
            GRATICULE_LATLON_LEVEL_4,
            GRATICULE_LATLON_LEVEL_5,
        };
    }

    protected String getTypeFor(double resolution)
    {
        if (resolution >= 10)
            return GRATICULE_LATLON_LEVEL_0;
        else if (resolution >= 1)
            return GRATICULE_LATLON_LEVEL_1;
        else if (resolution >= .1)
            return GRATICULE_LATLON_LEVEL_2;
        else if (resolution >= .01)
            return GRATICULE_LATLON_LEVEL_3;
        else if (resolution >= .001)
            return GRATICULE_LATLON_LEVEL_4;
        else if (resolution >= .0001)
            return GRATICULE_LATLON_LEVEL_5;

        return null;
    }

    protected void clear(DrawContext dc)
    {
        super.clear(dc);
        this.latitudeLabels.clear();
        this.longitudeLabels.clear();
        this.applyTerrainConformance();
    }

    private void applyTerrainConformance()
    {
        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType)
        {
            getRenderingParams(type).setValue(
                GraticuleRenderingParams.KEY_LINE_CONFORMANCE, this.terrainConformance);
        }
    }

    /**
     * Select the visible grid elements
     *
     * @param dc the current <code>DrawContext</code>.
     */
    protected void selectRenderables(DrawContext dc)
    {
        ArrayList<GraticuleTile> tileList = getVisibleTiles(dc);
        if (tileList.size() > 0)
        {
            for (GraticuleTile gz : tileList)
            {
                // Select tile visible elements
                gz.selectRenderables(dc);
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
                            gridTiles[row][col] = new GraticuleTile(getGridSector(row, col), 10, 0);
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
        int minLat = -90 + row * 10;
        int maxLat = minLat + 10;
        int minLon = -180 + col * 10;
        int maxLon = minLon + 10;
        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    private int getGridColumn(Double longitude)
    {
        int col = (int) Math.floor((longitude + 180) / 10d);
        return Math.min(col, 35);
    }

    private int getGridRow(Double latitude)
    {
        int row = (int) Math.floor((latitude + 90) / 10d);
        return Math.min(row, 17);
    }

    protected void clearTiles()
    {
        for (int row = 0; row < 18; row++)
        {
            for (int col = 0; col < 36; col++)
            {
                if (this.gridTiles[row][col] != null)
                {
                    this.gridTiles[row][col].clearRenderables();
                    this.gridTiles[row][col] = null;
                }
            }
        }
    }

    protected String makeAngleLabel(Angle angle, double resolution)
    {
        double epsilon = .000000001;
        String label;
        if (this.getAngleFormat().equals(Angle.ANGLE_FORMAT_DMS))
        {
            if (resolution >= 1)
                label = angle.toDecimalDegreesString(0);
            else
            {
                double[] dms = angle.toDMS();
                if (dms[1] < epsilon && dms[2] < epsilon)
                    label = String.format("%4d\u00B0", (int) dms[0]);
                else if (dms[2] < epsilon)
                    label = String.format("%4d\u00B0 %2d\u2019", (int) dms[0], (int) dms[1]);
                else
                    label = angle.toDMSString();
            }
        }
        else if (this.getAngleFormat().equals(Angle.ANGLE_FORMAT_DM))
        {
            if (resolution >= 1)
                label = angle.toDecimalDegreesString(0);
            else
            {
                double[] dms = angle.toDMS();
                if (dms[1] < epsilon && dms[2] < epsilon)
                    label = String.format("%4d\u00B0", (int) dms[0]);
                else if (dms[2] < epsilon)
                    label = String.format("%4d\u00B0 %2d\u2019", (int) dms[0], (int) dms[1]);
                else
                    label = angle.toDMString();
            }
        }
        else // default to decimal degrees
        {
            if (resolution >= 1)
                label = angle.toDecimalDegreesString(0);
            else if (resolution >= .1)
                label = angle.toDecimalDegreesString(1);
            else if (resolution >= .01)
                label = angle.toDecimalDegreesString(2);
            else if (resolution >= .001)
                label = angle.toDecimalDegreesString(3);
            else
                label = angle.toDecimalDegreesString(4);
        }

        return label;
    }

    protected void addLabel(double value, String labelType, String graticuleType, double resolution, LatLon labelOffset)
    {
        if (labelType.equals(GridElement.TYPE_LATITUDE_LABEL))
        {
            if (!this.latitudeLabels.contains(value))
            {
                this.latitudeLabels.add(value);
                String label = makeAngleLabel(Angle.fromDegrees(value), resolution);
                GeographicText text = new UserFacingText(label,
                    Position.fromDegrees(value, labelOffset.getLongitude().degrees, 0));
                text.setPriority(resolution * 1e6);
                this.addRenderable(text, graticuleType);
            }
        }
        else if (labelType.equals(GridElement.TYPE_LONGITUDE_LABEL))
        {
            if (!this.longitudeLabels.contains(value))
            {
                this.longitudeLabels.add(value);
                String label = makeAngleLabel(Angle.fromDegrees(value), resolution);
                GeographicText text = new UserFacingText(label,
                    Position.fromDegrees(labelOffset.getLatitude().degrees, value, 0));
                text.setPriority(resolution * 1e6);
                this.addRenderable(text, graticuleType);
            }
        }
    }

    // --- Graticule tile ----------------------------------------------------------------------

    protected class GraticuleTile
    {
        private Sector sector;
        private int divisions;
        private int level;

        private ArrayList<GridElement> gridElements;
        private ArrayList<GraticuleTile> subTiles;

        public GraticuleTile(Sector sector, int divisions, int level)
        {
            this.sector = sector;
            this.divisions = divisions;
            this.level = level;
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

            // Check apparent size
            if (this.level != 0 && getSizeInPixels(dc) / this.divisions < MIN_CELL_SIZE_PIXELS)
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

            LatLon labelOffset = computeLabelOffset(dc);
            String graticuleType = getTypeFor(this.sector.getDeltaLatDegrees());
            if (this.level == 0)
            {
                for (GridElement ge : this.gridElements)
                {
                    if (ge.isInView(dc))
                    {
                        // Add level zero bounding lines and labels
                        if (ge.type.equals(GridElement.TYPE_LINE_SOUTH) || ge.type.equals(GridElement.TYPE_LINE_NORTH)
                            || ge.type.equals(GridElement.TYPE_LINE_WEST))
                        {
                            addRenderable(ge.renderable, graticuleType);
                            String labelType = ge.type.equals(GridElement.TYPE_LINE_SOUTH)
                                || ge.type.equals(GridElement.TYPE_LINE_NORTH) ?
                                GridElement.TYPE_LATITUDE_LABEL : GridElement.TYPE_LONGITUDE_LABEL;
                            addLabel(ge.value, labelType, graticuleType, this.sector.getDeltaLatDegrees(), labelOffset);
                        }
                    }
                }
                if (getSizeInPixels(dc) / this.divisions < MIN_CELL_SIZE_PIXELS)
                    return;
            }

            // Select tile grid elements
            double resolution = this.sector.getDeltaLatDegrees() / this.divisions;
            graticuleType = getTypeFor(resolution);
            for (GridElement ge : this.gridElements)
            {
                if (ge.isInView(dc))
                {
                    if (ge.type.equals(GridElement.TYPE_LINE))
                    {
                        addRenderable(ge.renderable, graticuleType);
                        String labelType = ge.sector.getDeltaLatDegrees() == 0 ?
                            GridElement.TYPE_LATITUDE_LABEL : GridElement.TYPE_LONGITUDE_LABEL;
                        addLabel(ge.value, labelType, graticuleType, resolution, labelOffset);
                    }
                }
            }

            if (getSizeInPixels(dc) / this.divisions < MIN_CELL_SIZE_PIXELS * 2)
                return;

            // Select child elements
            if (this.subTiles == null)
                createSubTiles();
            for (GraticuleTile gt : this.subTiles)
            {
                if (gt.isInView(dc))
                {
                    gt.selectRenderables(dc);
                }
                else
                    gt.clearRenderables();
            }
        }

        public void clearRenderables()
        {
            if (this.gridElements != null)
            {
                this.gridElements.clear();
                this.gridElements = null;
            }
            if (this.subTiles != null)
            {
                for (GraticuleTile gt : this.subTiles)
                {
                    gt.clearRenderables();
                }
                this.subTiles.clear();
                this.subTiles = null;
            }
        }

        private void createSubTiles()
        {
            this.subTiles = new ArrayList<GraticuleTile>();
            Sector[] sectors = this.sector.subdivide(this.divisions);
            int subDivisions = 10;
            if ((getAngleFormat().equals(Angle.ANGLE_FORMAT_DMS) || getAngleFormat().equals(Angle.ANGLE_FORMAT_DM))
                && (this.level == 0 || this.level == 2))
                subDivisions = 6;
            for (Sector s : sectors)
            {
                this.subTiles.add(new GraticuleTile(s, subDivisions, this.level + 1));
            }
        }

        /** Create the grid elements */
        private void createRenderables()
        {
            this.gridElements = new ArrayList<GridElement>();

            double step = sector.getDeltaLatDegrees() / this.divisions;

            // Generate meridians with labels
            double lon = sector.getMinLongitude().degrees + (this.level == 0 ? 0 : step);
            while (lon < sector.getMaxLongitude().degrees - step / 2)
            {
                Angle longitude = Angle.fromDegrees(lon);
                // Meridian
                ArrayList<Position> positions = new ArrayList<Position>(2);
                positions.add(new Position(this.sector.getMinLatitude(), longitude, 0));
                positions.add(new Position(this.sector.getMaxLatitude(), longitude, 0));

                Object line = createLineRenderable(positions, AVKey.LINEAR);
                Sector sector = Sector.fromDegrees(
                    this.sector.getMinLatitude().degrees, this.sector.getMaxLatitude().degrees, lon, lon);
                String lineType = lon == this.sector.getMinLongitude().degrees ?
                    GridElement.TYPE_LINE_WEST : GridElement.TYPE_LINE;
                GridElement ge = new GridElement(sector, line, lineType);
                ge.value = lon;
                this.gridElements.add(ge);

                // Increase longitude
                lon += step;
            }

            // Generate parallels
            double lat = this.sector.getMinLatitude().degrees + (this.level == 0 ? 0 : step);
            while (lat < this.sector.getMaxLatitude().degrees - step / 2)
            {
                Angle latitude = Angle.fromDegrees(lat);
                ArrayList<Position> positions = new ArrayList<Position>(2);
                positions.add(new Position(latitude, this.sector.getMinLongitude(), 0));
                positions.add(new Position(latitude, this.sector.getMaxLongitude(), 0));

                Object line = createLineRenderable(positions, AVKey.LINEAR);
                Sector sector = Sector.fromDegrees(
                    lat, lat, this.sector.getMinLongitude().degrees, this.sector.getMaxLongitude().degrees);
                String lineType = lat == this.sector.getMinLatitude().degrees ?
                    GridElement.TYPE_LINE_SOUTH : GridElement.TYPE_LINE;
                GridElement ge = new GridElement(sector, line, lineType);
                ge.value = lat;
                this.gridElements.add(ge);

                // Increase latitude
                lat += step;
            }

            // Draw and label a parallel at the top of the graticule. The line is apparent only on 2D globes.
            if (this.sector.getMaxLatitude().equals(Angle.POS90))
            {
                ArrayList<Position> positions = new ArrayList<Position>(2);
                positions.add(new Position(Angle.POS90, this.sector.getMinLongitude(), 0));
                positions.add(new Position(Angle.POS90, this.sector.getMaxLongitude(), 0));

                Object line = createLineRenderable(positions, AVKey.LINEAR);
                Sector sector = Sector.fromDegrees(
                    90, 90, this.sector.getMinLongitude().degrees, this.sector.getMaxLongitude().degrees);
                GridElement ge = new GridElement(sector, line, GridElement.TYPE_LINE_NORTH);
                ge.value = 90;
                this.gridElements.add(ge);
            }
        }
    }
}

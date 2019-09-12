/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.placename;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import java.awt.*;

/**
 * @author Paul Collins
 * @version $Id: PlaceNameService.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PlaceNameService
{
    // Data retrieval and caching attributes.
    private final String service;
    private final String dataset;
    private final String fileCachePath;
    private static final String FORMAT_SUFFIX = ".xml.gz";
    // Geospatial attributes.
    public static final Sector TILING_SECTOR = Sector.FULL_SPHERE;
    private final LatLon tileDelta;
    // Display attributes.
    private final java.awt.Font font;
    private boolean enabled;
    private java.awt.Color color;
    private java.awt.Color backgroundColor;
    private double minDisplayDistance;
    private double maxDisplayDistance;
    private int numColumns;
    private static final int MAX_ABSENT_TILE_TRIES = 2;
    private static final int MIN_ABSENT_TILE_CHECK_INTERVAL = 10000;
    private final AbsentResourceList absentTiles = new AbsentResourceList(MAX_ABSENT_TILE_TRIES,
        MIN_ABSENT_TILE_CHECK_INTERVAL);
    private boolean addVersionTag = false;
    private Sector maskingSector = null;

    /**
     * PlaceNameService Constructor
     *
     * @param service       server hostong placename data
     * @param dataset       name of the dataset
     * @param fileCachePath location of cache
     * @param sector        sets the masking sector for this service.
     * @param tileDelta     tile size
     * @param font          font for rendering name
     * @param versionTag    dictates if the wfs version tag is added to requests
     *
     * @throws IllegalArgumentException if any parameter is null
     */
    public PlaceNameService(String service, String dataset, String fileCachePath, Sector sector, LatLon tileDelta,
        java.awt.Font font, boolean versionTag)
    {
        // Data retrieval and caching attributes.
        this.service = service;
        this.dataset = dataset;
        this.fileCachePath = fileCachePath;
        // Geospatial attributes.
        this.maskingSector = sector;
        this.tileDelta = tileDelta;
        // Display attributes.
        this.font = font;
        this.enabled = true;
        this.color = java.awt.Color.white;
        this.minDisplayDistance = Double.MIN_VALUE;
        this.maxDisplayDistance = Double.MAX_VALUE;
        this.addVersionTag = versionTag;

        String message = this.validate();
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.numColumns = this.numColumnsInLevel();
    }

    /**
     * @param row    row
     * @param column column
     *
     * @return path of the tile in the cache
     *
     * @throws IllegalArgumentException if either <code>row</code> or <code>column</code> is less than zero
     */
    public String createFileCachePathFromTile(int row, int column)
    {
        if (row < 0 || column < 0)
        {
            String message = Logging.getMessage("PlaceNameService.RowOrColumnOutOfRange", row, column);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        StringBuilder sb = new StringBuilder(this.fileCachePath);
        sb.append(java.io.File.separator).append(this.dataset);
        sb.append(java.io.File.separator).append(row);
        sb.append(java.io.File.separator).append(row).append('_').append(column);
        sb.append(FORMAT_SUFFIX);

        String path = sb.toString();
        return path.replaceAll("[:*?<>|]", "");
    }

    private int numColumnsInLevel()
    {
        int firstCol = Tile.computeColumn(this.tileDelta.getLongitude(), TILING_SECTOR.getMinLongitude(), Angle.NEG180);
        int lastCol = Tile.computeColumn(this.tileDelta.getLongitude(),
            TILING_SECTOR.getMaxLongitude().subtract(this.tileDelta.getLongitude()), Angle.NEG180);

        return lastCol - firstCol + 1;
    }

    public long getTileNumber(int row, int column)
    {
        return row * this.numColumns + column;
    }

    /**
     * @param sector request bounding box
     *
     * @return wfs request url
     *
     * @throws java.net.MalformedURLException thrown if error creating the url
     * @throws IllegalArgumentException       if {@link gov.nasa.worldwind.geom.Sector} is null
     */
    public java.net.URL createServiceURLFromSector(Sector sector) throws java.net.MalformedURLException
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        StringBuilder sb = new StringBuilder(this.service);
        if (sb.charAt(sb.length() - 1) != '?')
            sb.append('?');

        if (addVersionTag)
            sb.append("version=1.0.0&TypeName=").append(
                dataset);   //version=1.0.0  is needed when querying a new wfs server
        else
            sb.append("TypeName=").append(dataset);

        sb.append("&Request=GetFeature");
        sb.append("&Service=WFS");
        sb.append("&OUTPUTFORMAT=GML2-GZIP");
        sb.append("&BBOX=");
        sb.append(sector.getMinLongitude().getDegrees()).append(',');
        sb.append(sector.getMinLatitude().getDegrees()).append(',');
        sb.append(sector.getMaxLongitude().getDegrees()).append(',');
        sb.append(sector.getMaxLatitude().getDegrees());
        return new java.net.URL(sb.toString());
    }

    public synchronized final PlaceNameService deepCopy()
    {
        PlaceNameService copy = new PlaceNameService(this.service, this.dataset, this.fileCachePath, maskingSector,
            this.tileDelta, this.font, this.addVersionTag);
        copy.enabled = this.enabled;
        copy.color = this.color;
        copy.minDisplayDistance = this.minDisplayDistance;
        copy.maxDisplayDistance = this.maxDisplayDistance;
        return copy;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        final PlaceNameService other = (PlaceNameService) o;

        if (this.service != null ? !this.service.equals(other.service) : other.service != null)
            return false;
        if (this.dataset != null ? !this.dataset.equals(other.dataset) : other.dataset != null)
            return false;
        if (this.fileCachePath != null ? !this.fileCachePath.equals(other.fileCachePath) : other.fileCachePath != null)
            return false;
        if (this.maskingSector != null ? !this.maskingSector.equals(other.maskingSector) : other.maskingSector != null)
            return false;
        if (this.tileDelta != null ? !this.tileDelta.equals(other.tileDelta) : other.tileDelta != null)
            return false;
        if (this.font != null ? !this.font.equals(other.font) : other.font != null)
            return false;
        if (this.color != null ? !this.color.equals(other.color) : other.color != null)
            return false;
        if (this.backgroundColor != null ? !this.backgroundColor.equals(other.backgroundColor)
            : other.backgroundColor != null)
            return false;
        if (this.minDisplayDistance != other.minDisplayDistance)
            return false;
        //noinspection RedundantIfStatement
        if (this.maxDisplayDistance != other.maxDisplayDistance)
            return false;

        return true;
    }

    public synchronized final java.awt.Color getColor()
    {
        return this.color;
    }

    public synchronized final Color getBackgroundColor()
    {
        if (this.backgroundColor == null)
            this.backgroundColor = suggestBackgroundColor(this.color);
        return this.backgroundColor;
    }

    private Color suggestBackgroundColor(Color foreground)
    {
        float[] compArray = new float[4];
        Color.RGBtoHSB(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), compArray);
        int colorValue = compArray[2] < 0.5f ? 255 : 0;
        int alphaValue = foreground.getAlpha();
        return new Color(colorValue, colorValue, colorValue, alphaValue);
    }

    public final String getDataset()
    {
        return this.dataset;
    }

    /**
     * @param dc DrawContext
     *
     * @return extent of current drawcontext
     *
     * @throws IllegalArgumentException if {@link gov.nasa.worldwind.render.DrawContext} is null
     */
    public final Extent getExtent(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), this.maskingSector);
    }

    public final String getFileCachePath()
    {
        return this.fileCachePath;
    }

    public final java.awt.Font getFont()
    {
        return this.font;
    }

    public synchronized final double getMaxDisplayDistance()
    {
        return this.maxDisplayDistance;
    }

    public synchronized final double getMinDisplayDistance()
    {
        return this.minDisplayDistance;
    }

    public final LatLon getTileDelta()
    {
        return tileDelta;
    }

    public final Sector getMaskingSector()
    {
        return this.maskingSector;
    }

    public final String getService()
    {
        return this.service;
    }

    public boolean isAddVersionTag()
    {
        return addVersionTag;
    }

    public void setAddVersionTag(boolean addVersionTag)
    {
        this.addVersionTag = addVersionTag;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = (service != null ? service.hashCode() : 0);
        result = 29 * result + (this.dataset != null ? this.dataset.hashCode() : 0);
        result = 29 * result + (this.fileCachePath != null ? this.fileCachePath.hashCode() : 0);
        result = 29 * result + (this.maskingSector != null ? this.maskingSector.hashCode() : 0);
        result = 29 * result + (this.tileDelta != null ? this.tileDelta.hashCode() : 0);
        result = 29 * result + (this.font != null ? this.font.hashCode() : 0);
        result = 29 * result + (this.color != null ? this.color.hashCode() : 0);
        result = 29 * result + ((Double) minDisplayDistance).hashCode();
        result = 29 * result + ((Double) maxDisplayDistance).hashCode();
        return result;
    }

    public synchronized final boolean isEnabled()
    {
        return this.enabled;
    }

    /**
     * @param color color of label
     *
     * @throws IllegalArgumentException if {@link java.awt.Color} is null
     */
    public synchronized final void setColor(java.awt.Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.color = color;
    }

    public synchronized final void setBackgroundColor(java.awt.Color backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    public synchronized final void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * @param maxDisplayDistance maximum distance to display labels for this service
     *
     * @throws IllegalArgumentException if <code>maxDisplayDistance</code> is less than the current minimum display
     *                                  distance
     */
    public synchronized final void setMaxDisplayDistance(double maxDisplayDistance)
    {
        if (maxDisplayDistance < this.minDisplayDistance)
        {
            String message = Logging.getMessage("PlaceNameService.MaxDisplayDistanceLessThanMinDisplayDistance",
                maxDisplayDistance, this.minDisplayDistance);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.maxDisplayDistance = maxDisplayDistance;
    }

    /**
     * @param minDisplayDistance minimum distance to display labels for this service
     *
     * @throws IllegalArgumentException if <code>minDisplayDistance</code> is less than the current maximum display
     *                                  distance
     */
    public synchronized final void setMinDisplayDistance(double minDisplayDistance)
    {
        if (minDisplayDistance > this.maxDisplayDistance)
        {
            String message = Logging.getMessage("PlaceNameService.MinDisplayDistanceGrtrThanMaxDisplayDistance",
                minDisplayDistance, this.maxDisplayDistance);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minDisplayDistance = minDisplayDistance;
    }

    public synchronized final void markResourceAbsent(long tileNumber)
    {
        this.absentTiles.markResourceAbsent(tileNumber);
    }

    public synchronized final boolean isResourceAbsent(long resourceNumber)
    {
        return this.absentTiles.isResourceAbsent(resourceNumber);
    }

    public synchronized final void unmarkResourceAbsent(long tileNumber)
    {
        this.absentTiles.unmarkResourceAbsent(tileNumber);
    }

    /**
     * Determines if this {@link gov.nasa.worldwind.layers.placename.PlaceNameService} constructor arguments are valid.
     *
     * @return null if valid, otherwise a string message containing a description of why it is invalid.
     */
    public final String validate()
    {
        String msg = "";
        if (this.service == null)
        {
            msg += Logging.getMessage("nullValue.ServiceIsNull") + ", ";
        }
        if (this.dataset == null)
        {
            msg += Logging.getMessage("nullValue.DataSetIsNull") + ", ";
        }
        if (this.fileCachePath == null)
        {
            msg += Logging.getMessage("nullValue.FileStorePathIsNull") + ", ";
        }
        if (this.maskingSector == null)
        {
            msg += Logging.getMessage("nullValue.SectorIsNull") + ", ";
        }
        if (this.tileDelta == null)
        {
            msg += Logging.getMessage("nullValue.TileDeltaIsNull") + ", ";
        }
        if (this.font == null)
        {
            msg += Logging.getMessage("nullValue.FontIsNull") + ", ";
        }

        if (msg.length() == 0)
        {
            return null;
        }

        return msg;
    }
}

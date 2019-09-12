/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implements an elevation model for a local file or collection of files containing elevation data.
 * <p>
 * Note: Unless the amount of data associated with the local elevation models is small, it's best to construct and add
 * elevations to a local elevation model on a thread other than the event dispatch thread in order to avoid freezing the
 * user interface.
 *
 * @author tag
 * @version $Id: LocalElevationModel.java 2138 2014-07-10 17:31:58Z tgaskins $
 */
public class LocalElevationModel extends AbstractElevationModel
{
    /** The min and max elevations. */
    protected double[] extremeElevations = null;
    /** The list of elevation rasters, one per file specified. */
    protected CopyOnWriteArrayList<LocalTile> tiles = new CopyOnWriteArrayList<LocalTile>();

    public double getMinElevation()
    {
        return this.extremeElevations[0];
    }

    public double getMaxElevation()
    {
        return this.extremeElevations[1];
    }

    /**
     * Returns the sector spanned by this elevation model.
     *
     * @return the sector spanned by this elevation model.
     */
    public Sector getSector()
    {
        if (this.tiles.size() < 1)
            return null;

        Sector sector = null;

        for (LocalTile tile : this.tiles)
        {
            Sector tileSector = tile.sector;
            sector = sector == null ? tileSector : sector.union(tileSector);
        }

        return sector;
    }

    public double[] getExtremeElevations(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.tiles.size() == 0)
            return new double[] {this.getMinElevation(), this.getMaxElevation()};

        double min = Double.MAX_VALUE;
        double max = -min;

        for (LocalTile tile : this.tiles)
        {
            if (tile.sector.contains(latitude, longitude))
            {
                if (tile.minElevation < min)
                    min = tile.minElevation;
                if (tile.maxElevation > max)
                    max = tile.maxElevation;
            }
        }

        return new double[] {
            min != Double.MAX_VALUE ? min : this.getMinElevation(),
            max != -Double.MAX_VALUE ? max : this.getMaxElevation()};
    }

    public double[] getExtremeElevations(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.tiles.size() == 0)
            return new double[] {this.getMinElevation(), this.getMaxElevation()};

        double min = Double.MAX_VALUE;
        double max = -min;

        for (LocalTile tile : this.tiles)
        {
            if (tile.sector.intersects(sector))
            {
                if (tile.minElevation < min)
                    min = tile.minElevation;
                if (tile.maxElevation > max)
                    max = tile.maxElevation;
            }
        }

        return new double[] {
            min != Double.MAX_VALUE ? min : this.getMinElevation(),
            max != -Double.MAX_VALUE ? max : this.getMaxElevation()};
    }

    public double getBestResolution(Sector sector)
    {
        double res = Double.MAX_VALUE;

        for (LocalTile tile : tiles)
        {
            if (sector != null && !sector.intersects(tile.sector))
                continue;

            double r = tile.sector.getDeltaLatRadians() / tile.tileHeight;
            if (r < res)
                res = r;
        }

        return res;
    }

    @Override
    public void setExtremesCachingEnabled(boolean enabled)
    {
    }

    @Override
    public boolean isExtremesCachingEnabled()
    {
        return false;
    }

    public double getUnmappedElevation(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.contains(latitude, longitude))
            return this.missingDataFlag;

        Double e = this.lookupElevation(latitude.radians, longitude.radians);

        // e will be null if the tile containing the location isn't available, or it will be the computed elevation if
        // there is one for the location, otherwise it will be the missing data signal. In the latter two cases the
        // value is returned by this method. If no tile contains the location, this elevation model's minimum elevation
        // at the location is returned.
        return e != null ? e : this.getExtremeElevations(latitude, longitude)[0];
    }

    public double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer)
    {
        return this.doGetElevations(sector, latlons, targetResolution, buffer, true);
    }

    public double getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution,
        double[] buffer)
    {
        return this.doGetElevations(sector, latlons, targetResolution, buffer, false);
    }

    /**
     * Performs the lookup and assembly of elevations for a list of specified locations. This method is provided to
     * enable subclasses to override this operation.
     *
     * @param sector           the sector containing the specified locations.
     * @param latlons          the list of locations at which to lookup elevations.
     * @param targetResolution the desired maximum horizontal resolution of the elevation data to draw from.
     * @param buffer           a buffer in which to return the elevations. Must be at least as large as the list of
     *                         locations.
     * @param mapMissingData   indicates whether to replace any elevations that match this elevation model's missing
     *                         data signal to this model's missing data replacement value.
     *
     * @return the resolution achieved, in radians, or {@link Double#MAX_VALUE} if individual elevations cannot be
     *         determined for all of the locations.
     *
     * @throws IllegalArgumentException if either the sector, list of locations or buffer is null, or the buffer size is
     *                                  not at least as large as the location list.
     * @see #setMissingDataSignal(double)
     * @see #setMissingDataReplacement(double)
     */
    @SuppressWarnings( {"UnusedParameters"})
    protected double doGetElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution,
        double[] buffer, boolean mapMissingData)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (latlons == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer == null)
        {
            String msg = Logging.getMessage("nullValue.ElevationsBufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer.length < latlons.size())
        {
            String msg = Logging.getMessage("ElevationModel.ElevationsBufferTooSmall", latlons.size());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.intersects(sector) == -1)
            return Double.MAX_VALUE; // as stated in the javadoc above, this is the sentinel for "not in my domain"

        // Mark the model as used this frame.
        this.setValue(AVKey.FRAME_TIMESTAMP, System.currentTimeMillis());

        for (int i = 0; i < latlons.size(); i++)
        {
            LatLon ll = latlons.get(i);
            if (ll == null)
                continue;

            if (!this.contains(ll.getLatitude(), ll.getLongitude()))
                continue;

            // If an elevation at the given location is available, write that elevation to the destination buffer.
            // If an elevation is not available but the location is within the elevation model's coverage, write the
            // elevation models extreme elevation at the location. Do nothing if the location is not within the
            // elevation model's coverage.
            Double e = this.lookupElevation(ll.getLatitude().radians, ll.getLongitude().radians);
            if (e != null && e != this.missingDataFlag)
                buffer[i] = e;
            if (e == null)
                buffer[i] = this.getExtremeElevations(sector)[0];
            else if (mapMissingData && e == this.missingDataFlag)
                buffer[i] = this.getMissingDataReplacement();
        }

        return this.getBestResolution(sector);
    }

    /**
     * Adds the specified elevation data to this elevation model.
     *
     * @param filename the file containing the elevation data.
     *
     * @throws IllegalArgumentException if the filename is null or the file does not exist, or if the file does not
     *                                  contain elevations in a recognizable format.
     * @throws IOException              if an exception occurs while opening or reading the specified file.
     * @throws WWRuntimeException       if an error occurs attempting to read and interpret the elevation data, or if
     *                                  necessary information is missing from the data file or its auxiliary files.
     */
    public void addElevations(String filename) throws IOException
    {
        if (filename == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.addElevations(new File(filename));
    }

    /**
     * Adds the specified elevation data to this elevation model.
     *
     * @param file the file containing the elevation data.
     *
     * @throws IllegalArgumentException if the file is null or does not exist, if the file does not contain elevations
     *                                  in a recognizable format, or if the specified file does not contain required
     *                                  information such as the data's location and data type.
     * @throws IOException              if an exception occurs while opening or reading the specified file.
     * @throws WWRuntimeException       if an error occurs attempting to read and interpret the elevation data, or if an
     *                                  error occurs attempting to read and interpret the file's contents.
     */
    public void addElevations(File file) throws IOException
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!file.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", file.getPath());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Create a raster reader for the file type.
        DataRasterReaderFactory readerFactory = (DataRasterReaderFactory) WorldWind.createConfigurationComponent(
            AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
        DataRasterReader reader = readerFactory.findReaderFor(file, null);

        // Before reading the raster, verify that the file contains elevations.
        AVList metadata = reader.readMetadata(file, null);
        if (metadata == null || !AVKey.ELEVATION.equals(metadata.getStringValue(AVKey.PIXEL_FORMAT)))
        {
            String msg = Logging.getMessage("ElevationModel.SourceNotElevations", file.getAbsolutePath());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Read the file into the raster.
        DataRaster[] rasters = reader.read(file, null);
        if (rasters == null || rasters.length == 0)
        {
            String msg = Logging.getMessage("ElevationModel.CannotReadElevations", file.getAbsolutePath());
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg);
        }

        for (DataRaster raster : rasters)
        {
            this.addRaster(raster, file.getAbsolutePath());
        }
    }

    /**
     * Adds a new raster to this elevation model.
     *
     * @param raster   the raster to add.
     * @param filename the filename associated with the raster. Used only for error reporting.
     *
     * @throws IllegalArgumentException if necessary information is missing from the raster.
     * @throws IOException              if an exception occurs while opening or reading the specified file.
     * @throws WWRuntimeException       if an error occurs attempting to read and interpret the elevation data.
     */
    protected void addRaster(DataRaster raster, String filename) throws IOException
    {
        // Determine the sector covered by the elevations. This information is in the GeoTIFF file or auxiliary
        // files associated with the elevations file.
        final Sector sector = (Sector) raster.getValue(AVKey.SECTOR);
        if (sector == null)
        {
            String msg = Logging.getMessage("DataRaster.MissingMetadata", AVKey.SECTOR);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Request a sub-raster that contains the whole file. This step is necessary because only sub-rasters
        // are reprojected (if necessary); primary rasters are not.
        int width = raster.getWidth();
        int height = raster.getHeight();

        DataRaster subRaster = raster.getSubRaster(width, height, sector, raster);

        // Verify that the sub-raster can create a ByteBuffer, then create one.
        if (!(subRaster instanceof ByteBufferRaster))
        {
            String msg = Logging.getMessage("ElevationModel.CannotCreateElevationBuffer", filename);
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg);
        }
        ByteBuffer elevations = ((ByteBufferRaster) subRaster).getByteBuffer();

        // The sub-raster can now be disposed. Disposal won't affect the ByteBuffer.
        subRaster.dispose();

        this.addElevations(elevations, sector, width, height, raster);

        // Tne primary raster can now be disposed.
        raster.dispose();
    }

    /**
     * Adds new elevations to this elevation model. The elevations are specified as a rectangular array arranged in
     * row-major order in a linear buffer.
     *
     * @param byteBuffer the elevations to add.
     * @param sector     the sector containing the elevations.
     * @param width      the number of elevation values in a row of the array. Each consecutive group of {@code width}
     *                   elevation values is considered one row of the array, with the first element of the buffer
     *                   starting the row of elevations at the sector's maximum latitude.
     * @param height     the number of rows in the array.
     * @param parameters a list of key-value pairs indicating information about the raster. A value for {@code
     *                   AVKey.DATA_TYPE} is required. Values recognized but not required are {@code
     *                   AVKey.MISSING_DATA_SIGNAL}, {@code AVKey.BYTE_ORDER}, {@code AVKey.ELEVATION_MIN} and {@code
     *                   AVKey.ELEVATION_MAX}.
     *
     * @throws IllegalArgumentException if either the buffer, sector or parameters list is null, or if a key-value pair
     *                                  for {@code AVKey.DATA_TYPE} is not specified in the parameters.
     */
    public void addElevations(ByteBuffer byteBuffer, Sector sector, int width, int height, AVList parameters)
    {
        if (byteBuffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (parameters == null)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Setup parameters to instruct BufferWrapper on how to interpret the ByteBuffer.
        AVList bufferParams = new AVListImpl();
        bufferParams.setValues(parameters.copy());

        Double tileMissingDataFlag = AVListImpl.getDoubleValue(bufferParams, AVKey.MISSING_DATA_SIGNAL);
        if (tileMissingDataFlag == null)
            tileMissingDataFlag = this.getMissingDataSignal();

        // Check params for ELEVATION_MIN and ELEVATION_MAX and don't have the tile compute them if present.
        Double minElevation = null;
        Double maxElevation = null;
        Object o = bufferParams.getValue(AVKey.ELEVATION_MIN);
        if (o instanceof Double)
            minElevation = (Double) o;
        o = bufferParams.getValue(AVKey.ELEVATION_MAX);
        if (o instanceof Double)
            maxElevation = (Double) o;

        String dataType = bufferParams.getStringValue(AVKey.DATA_TYPE);
        if (WWUtil.isEmpty(dataType))
        {
            String msg = Logging.getMessage("DataRaster.MissingMetadata", AVKey.DATA_TYPE);
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        BufferWrapper buffer = BufferWrapper.wrap(byteBuffer, bufferParams);

        LocalTile tile = new LocalTile(sector, tileMissingDataFlag, width, height, buffer, minElevation, maxElevation);
        this.tiles.add(tile);
        this.adjustMinMax(tile);
    }

    public int intersects(Sector sector)
    {
        boolean intersects = false;

        for (LocalTile tile : this.tiles)
        {
            if (tile.sector.contains(sector))
                return 0;

            if (tile.sector.intersects(sector))
                intersects = true;
        }

        return intersects ? 1 : -1;
    }

    public boolean contains(Angle latitude, Angle longitude)
    {
        for (LocalTile tile : this.tiles)
        {
            if (tile.sector.contains(latitude, longitude))
                return true;
        }

        return false;
    }

    /**
     * Updates the min and max elevations for this elevation model to account for a specified tile.
     *
     * @param tile the tile to account for.
     */
    protected void adjustMinMax(LocalTile tile)
    {
        if (this.extremeElevations == null && tile != null)
        {
            this.extremeElevations = new double[] {tile.minElevation, tile.maxElevation};
        }
        else if (tile != null) // adjust for just the input tile
        {
            if (tile.minElevation < this.extremeElevations[0])
                this.extremeElevations[0] = tile.minElevation;
            if (tile.maxElevation > this.extremeElevations[1])
                this.extremeElevations[1] = tile.maxElevation;
        }
        else // Find the min and max among all the tiles
        {
            double min = Double.MAX_VALUE;
            double max = -min;

            for (LocalTile t : this.tiles)
            {
                if (t.minElevation < min)
                    min = t.minElevation;
                if (t.maxElevation > max)
                    max = t.maxElevation;
            }

            this.extremeElevations =
                new double[] {min != Double.MAX_VALUE ? min : 0, max != -Double.MAX_VALUE ? max : 0};
        }
    }

    /**
     * Looks up an elevation for a specified location.
     *
     * @param latRadians the latitude of the location, in radians.
     * @param lonRadians the longitude of the location, in radians.
     *
     * @return the elevation if the specified location is contained in this elevation model, null if it's not, or this
     *         elevation model's missing data flag if that's the value at the specified location.
     */
    protected Double lookupElevation(final double latRadians, final double lonRadians)
    {
        LocalTile tile = this.findTile(latRadians, lonRadians);
        if (tile == null)
            return null;

        final double sectorDeltaLat = tile.sector.getDeltaLat().radians;
        final double sectorDeltaLon = tile.sector.getDeltaLon().radians;
        final double dLat = tile.sector.getMaxLatitude().radians - latRadians;
        final double dLon = lonRadians - tile.sector.getMinLongitude().radians;
        final double sLat = dLat / sectorDeltaLat;
        final double sLon = dLon / sectorDeltaLon;

        int j = (int) ((tile.tileHeight - 1) * sLat);
        int i = (int) ((tile.tileWidth - 1) * sLon);
        int k = j * tile.tileWidth + i;

        double eLeft = tile.elevations.getDouble(k);
        double eRight = i < (tile.tileWidth - 1) ? tile.elevations.getDouble(k + 1) : eLeft;

        // Notice that the below test is against the tile flag, but the value returned is the model's flag.
        if (tile.isMissingData(eLeft) || tile.isMissingData(eRight))
            return this.missingDataFlag;

        double dw = sectorDeltaLon / (tile.tileWidth - 1);
        double dh = sectorDeltaLat / (tile.tileHeight - 1);
        double ssLon = (dLon - i * dw) / dw;
        double ssLat = (dLat - j * dh) / dh;

        double eTop = eLeft + ssLon * (eRight - eLeft);

        if (j < tile.tileHeight - 1 && i < tile.tileWidth - 1)
        {
            eLeft = tile.elevations.getDouble(k + tile.tileWidth);
            eRight = tile.elevations.getDouble(k + tile.tileWidth + 1);

            // Notice that the below test is against the tile flag, but the value returned is the model's flag.
            if (tile.isMissingData(eLeft) || tile.isMissingData(eRight))
                return this.missingDataFlag;
        }

        double eBot = eLeft + ssLon * (eRight - eLeft);
        return eTop + ssLat * (eBot - eTop);
    }

    /**
     * Finds the tile in this elevation model that contains a specified location.
     *
     * @param latRadians the location's latitude, in radians.
     * @param lonRadians the location's longitude, in radians.
     *
     * @return the tile that contains the location, or null if no tile contains it.
     */
    protected LocalTile findTile(final double latRadians, final double lonRadians)
    {
        for (LocalTile tile : this.tiles)
        {
            if (tile.sector.containsRadians(latRadians, lonRadians))
                return tile;
        }

        return null;
    }

    /** An internal class that represents one elevation raster in the elevation model. */
    protected static class LocalTile
    {
        /** The sector the tile covers. */
        protected final Sector sector;
        /** The number of elevation values in a row of the raster. */
        protected final int tileWidth;
        /** The number of rows in the raster. */
        protected final int tileHeight;
        /** The minimum elevation contained in this tile's raster. */
        protected double minElevation;
        /** The maximum elevation contained in this tile's raster. */
        protected double maxElevation;
        /** The elevation model's missing data flag. */
        protected final double missingDataFlag;
        /** The elevations. */
        protected final BufferWrapper elevations;

        /**
         * Constructs a new elevations tile.
         *
         * @param sector          the sector the tile covers.
         * @param missingDataFlag the elevation model's missing data flag.
         * @param tileWidth       the number of elevation values in a row of this tile's elevation raster.
         * @param tileHeight      the number of rows in this tile's elevation raster.
         * @param elevations      the elevations.
         * @param minEl           the minimum elevation of this tile. May be null, in which case the minimum is
         *                        determined.
         * @param maxEl           the maximum elevation of this tile. May be null, in which case the maximum is
         *                        determined.
         */
        protected LocalTile(Sector sector, double missingDataFlag, int tileWidth, int tileHeight,
            BufferWrapper elevations, Double minEl, Double maxEl)
        {
            this.sector = sector;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.missingDataFlag = missingDataFlag;
            this.elevations = elevations;

            // One or both of the min/max elevations may have been specified in the metadata.
            if (minEl != null)
                this.minElevation = minEl;
            if (maxEl != null)
                this.maxElevation = maxEl;

            if (minEl == null || maxEl == null)
                this.computeMinMaxElevations();

            return;
        }

        /** Determines the minimum and maximum elevations of this tile. */
        protected void computeMinMaxElevations()
        {
            int len = this.elevations.length();
            if (len == 0)
            {
                this.minElevation = 0;
                this.maxElevation = 0;
                return;
            }

            double min = Double.MAX_VALUE;
            double max = -min;

            for (int i = 0; i < len; i++)
            {
                double v = this.elevations.getDouble(i);
                if (v == this.missingDataFlag)
                    continue;

                if (v < min)
                    min = v;
                if (v > max)
                    max = v;
            }

            this.minElevation = min;
            this.maxElevation = max;
        }

        /**
         * Determines whether a value signifies missing data -- voids. This method not only tests the value againsy the
         * tile's missing data flag, it also tests for its being within range of the tile's minimum and maximum values.
         * The values also tested for the magic value -32767, which for some reason is commonly used but not always
         * identified in GeoTiff files.
         *
         * @param value the value to test.
         *
         * @return true if the value is this tile's missing data signal, or if it is outside the min/max range of the
         *         tile, or if it's equal to -32767.
         */
        protected boolean isMissingData(double value)
        {
            return value == this.missingDataFlag || value < this.minElevation || value > this.maxElevation
                || value == -32767;
        }
    }
}

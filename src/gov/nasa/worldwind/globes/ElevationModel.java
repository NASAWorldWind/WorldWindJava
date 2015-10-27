/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;

import java.util.List;

/**
 * <p/>
 * Provides the elevations to a {@link Globe} or other object holding elevations.
 * <p/>
 * An <code>ElevationModel</code> often approximates elevations at multiple levels of spatial resolution. For any given
 * viewing position, the model determines an appropriate target resolution. That target resolution may not be
 * immediately achievable, however, because the corresponding elevation data might not be locally available and must be
 * retrieved from a remote location. When this is the case, the <code>Elevations</code> object returned for a sector
 * holds the resolution achievable with the data currently available. That resolution may not be the same as the target
 * resolution. The achieved resolution is made available in the interface.
 * <p/>
 *
 * @author Tom Gaskins
 * @version $Id: ElevationModel.java 3420 2015-09-10 23:25:43Z tgaskins $
 */
public interface ElevationModel extends WWObject, Restorable, Disposable
{
    /**
     * Returns the elevation model's name.
     *
     * @return the elevation model's name.
     *
     * @see #setName(String)
     */
    String getName();

    /**
     * Set the elevation model's name. The name is a convenience attribute typically used to identify the elevation
     * model in user interfaces. By default, an elevation model has no name.
     *
     * @param name the name to give the elevation model.
     */
    void setName(String name);

    /**
     * Indicates whether the elevation model is allowed to retrieve data from the network. Some elevation models have no
     * need to retrieve data from the network. This flag is meaningless for such elevation models.
     *
     * @return <code>true</code> if the elevation model is enabled to retrieve network data, else <code>false</code>.
     */
    boolean isNetworkRetrievalEnabled();

    /**
     * Controls whether the elevation model is allowed to retrieve data from the network. Some elevation models have no
     * need for data from the network. This flag may be set but is meaningless for such elevation models.
     *
     * @param networkRetrievalEnabled <code>true</code> if network retrieval is allowed, else <code>false</code>.
     */
    void setNetworkRetrievalEnabled(boolean networkRetrievalEnabled);

    /**
     * Returns the current expiry time.
     *
     * @return the current expiry time.
     *
     * @see #setExpiryTime(long)
     */
    long getExpiryTime();

    /**
     * Specifies the time of the elevation model's most recent dataset update. If greater than zero, the model ignores
     * and eliminates any previously cached data older than the time specified, and requests new information from the
     * data source. If zero, the model uses any expiry times intrinsic to the model, typically initialized at model
     * construction. The default expiry time is 0, thereby enabling a model's intrinsic expiration criteria.
     *
     * @param expiryTime the expiry time of any cached data, expressed as a number of milliseconds beyond the epoch.
     *
     * @see System#currentTimeMillis() for a description of milliseconds beyond the epoch.
     */
    void setExpiryTime(long expiryTime);

    /**
     * Specifies the value used to identify missing data in an elevation model. Locations with this elevation value are
     * assigned the missing-data replacement value, specified by {@link #setMissingDataReplacement(double)}.
     * <p/>
     * The missing-data value is often specified by the metadata of the data set, in which case the elevation model
     * automatically defines that value to be the missing-data signal. When the missing-data signal is not specified in
     * the metadata, the application may specify it via this method.
     *
     * @param flag the missing-data signal value. The default is -{@link Double#MAX_VALUE}.
     *
     * @see #setMissingDataReplacement(double)
     * @see #getMissingDataSignal
     */
    void setMissingDataSignal(double flag);

    /**
     * Returns the current missing-data signal.
     *
     * @return the missing-data signal.
     *
     * @see #getMissingDataReplacement()
     */
    double getMissingDataSignal();

    /**
     * Indicates whether the elevation model covers a specified sector either partially or fully.
     *
     * @param sector the sector in question.
     *
     * @return 0 if the elevation model fully contains the sector, 1 if the elevation model intersects the sector but
     *         does not fully contain it, or -1 if the sector does not intersect the elevation model.
     */
    int intersects(Sector sector);

    /**
     * Indicates whether a specified location is within the elevation model's domain.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return true if the location is within the elevation model's domain, otherwise false.
     */
    boolean contains(Angle latitude, Angle longitude);

    /**
     * Returns the maximum elevation contained in the elevation model. When the elevation model is associated with a
     * globe, this value is the elevation of the highest point on the globe.
     *
     * @return The maximum elevation of the elevation model.
     */
    double getMaxElevation();

    /**
     * Returns the minimum elevation contained in the elevation model. When associated with a globe, this value is the
     * elevation of the lowest point on the globe. It may be negative, indicating a value below mean surface level. (Sea
     * level in the case of Earth.)
     *
     * @return The minimum elevation of the model.
     */
    double getMinElevation();

    /**
     * Returns the minimum and maximum elevations at a specified location.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return A two-element <code>double</code> array indicating, respectively, the minimum and maximum elevations at
     *         the specified location. These values are the global minimum and maximum if the local minimum and maximum
     *         values are currently unknown.
     */
    double[] getExtremeElevations(Angle latitude, Angle longitude);

    /**
     * Returns the minimum and maximum elevations within a specified sector of the elevation model.
     *
     * @param sector the sector in question.
     *
     * @return A two-element <code>double</code> array indicating, respectively, the sector's minimum and maximum
     *         elevations. These elements are the global minimum and maximum if the local minimum and maximum values are
     *         currently unknown.
     */
    double[] getExtremeElevations(Sector sector);

    /**
     * Indicates the best resolution attainable for a specified sector.
     *
     * @param sector the sector in question. If null, the elevation model's best overall resolution is returned. This is
     *               the best attainable at <em>some</em> locations but not necessarily at all locations.
     *
     * @return the best resolution attainable for the specified sector, in radians, or {@link Double#MAX_VALUE} if the
     *         sector does not intersect the elevation model.
     */
    double getBestResolution(Sector sector);

    double[] getBestResolutions(Sector sector);

    /**
     * Returns the detail hint associated with the specified sector. If the elevation model does not have any detail
     * hint for the sector, this method returns zero.
     *
     * @param sector the sector in question.
     *
     * @return The detail hint corresponding to the specified sector.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null.
     */
    double getDetailHint(Sector sector);

    /**
     * Returns the elevation at a specified location. If the elevation at the specified location is the elevation
     * model's missing data signal, or if the location specified is outside the elevation model's coverage area, the
     * elevation model's missing data replacement value is returned.
     * <p/>
     * The elevation returned from this method is the best available in memory. If no elevation is in memory, the
     * elevation model's minimum extreme elevation at the location is returned. Local disk caches are not consulted.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return The elevation corresponding to the specified location, or the elevation model's missing-data replacement
     *         value if there is no elevation for the given location.
     *
     * @see #setMissingDataSignal(double)
     * @see #getUnmappedElevation(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle)
     */
    double getElevation(Angle latitude, Angle longitude);

    /**
     * Returns the elevation at a specified location, but without replacing missing data with the elevation model's
     * missing data replacement value. When a missing data signal is found, the signal value is returned, not the
     * replacement value.
     *
     * @param latitude  the latitude of the location for which to return the elevation.
     * @param longitude the longitude of the location for which to return the elevation.
     *
     * @return the elevation at the specified location, or the elevation model's missing data signal. If no data is
     *         currently in memory for the location, and the location is within the elevation model's coverage area, the
     *         elevation model's minimum elevation at that location is returned.
     */
    double getUnmappedElevation(Angle latitude, Angle longitude);

    /**
     * Returns the elevations of a collection of locations. Replaces any elevation values corresponding to the missing
     * data signal with the elevation model's missing data replacement value. If a location within the elevation model's
     * coverage area cannot currently be determined, the elevation model's minimum extreme elevation for that location
     * is returned in the output buffer. If a location is outside the elevation model's coverage area, the output buffer
     * for that location is not modified; it retains the buffer's original value.
     *
     * @param sector           the sector in question.
     * @param latlons          the locations to return elevations for. If a location is null, the output buffer for that
     *                         location is not modified.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.)
     * @param buffer           an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least as many elements as the list of locations.
     *
     * @return the resolution achieved, in radians, or {@link Double#MAX_VALUE} if individual elevations cannot be
     *         determined for all of the locations.
     *
     * @throws IllegalArgumentException if either the sector, latlons list or elevations array is null.
     * @see #setMissingDataSignal(double)
     */
    @SuppressWarnings({"JavadocReference"})
    double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer);

    /**
     * Returns the elevations of a collection of locations. Replaces any elevation values corresponding to the missing
     * data signal with the elevation model's missing data replacement value. If a location within the elevation model's
     * coverage area cannot currently be determined, the elevation model's minimum extreme elevation for that location
     * is returned in the output buffer. If a location is outside the elevation model's coverage area, the output buffer
     * for that location is not modified; it retains the buffer's original value.
     *
     * @param sector           the sector in question.
     * @param latlons          the locations to return elevations for. If a location is null, the output buffer for that
     *                         location is not modified.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.) This is an array to enable specification of a target resolution per
     *                         elevation model for {@link gov.nasa.worldwind.terrain.CompoundElevationModel}. The
     *                         entries must be in the same order as the elevations in {@link
     *                         gov.nasa.worldwind.terrain.CompoundElevationModel}.
     * @param buffer           an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least as many elements as the list of locations.
     *
     * @return the resolutions achieved, in radians, which will be {@link Double#MAX_VALUE} if individual elevations
     *         cannot be determined for all of the locations. The entries are in the same order as the elevations in
     *         {@link gov.nasa.worldwind.terrain.CompoundElevationModel}.
     *
     * @throws IllegalArgumentException if either the sector, latlons list, target resolutions array or elevations array
     *                                  is null.
     * @see #setMissingDataSignal(double)
     */
    double[] getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution[], double[] buffer);

    /**
     * Returns the elevations of a collection of locations. <em>Does not</em> replace any elevation values corresponding
     * to the missing data signal with the elevation model's missing data replacement value. If a location within the
     * elevation model's coverage area cannot currently be determined, the elevation model's minimum extreme elevation
     * for that location is returned in the output buffer. If a location is outside the elevation model's coverage area,
     * the output buffer for that location is not modified; it retains the buffer's original value.
     *
     * @param sector           the sector in question.
     * @param latlons          the locations to return elevations for. If a location is null, the output buffer for that
     *                         location is not modified.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.)
     * @param buffer           an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least as many elements as the list of locations.
     *
     * @return the resolution achieved, in radians, or {@link Double#MAX_VALUE} if individual elevations cannot be
     *         determined for all of the locations.
     *
     * @see #setMissingDataSignal(double)
     */
    double getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution,
        double[] buffer);

    /**
     * Returns the elevations of a collection of locations. <em>Does not</em> replace any elevation values corresponding
     * to the missing data signal with the elevation model's missing data replacement value. If a location within the
     * elevation model's coverage area cannot currently be determined, the elevation model's minimum extreme elevation
     * for that location is returned in the output buffer. If a location is outside the elevation model's coverage area,
     * the output buffer for that location is not modified; it retains the buffer's original value.
     *
     * @param sector           the sector in question.
     * @param latlons          the locations to return elevations for. If a location is null, the output buffer for that
     *                         location is not modified.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.) This is an array to enable specification of a target resolution per
     *                         elevation model for {@link gov.nasa.worldwind.terrain.CompoundElevationModel}. The
     *                         entries must be in the same order as the elevations in {@link
     *                         gov.nasa.worldwind.terrain.CompoundElevationModel}.
     * @param buffer           an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least as many elements as the list of locations.
     *
     * @return the resolutions achieved, in radians, which will be {@link Double#MAX_VALUE} if individual elevations
     *         cannot be determined for all of the locations. The entries are in the same order as the elevations in
     *         {@link gov.nasa.worldwind.terrain.CompoundElevationModel}.
     *
     * @throws IllegalArgumentException if either the sector, latlons list, target resolutions array or elevations
     *                                  array
     * @see #setMissingDataSignal(double)
     */
    double[] getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution[],
        double[] buffer);

    /**
     * Returns the elevation used for missing values in the elevation model.
     *
     * @return the value that indicates that no data is available at a location.
     *
     * @see #setMissingDataSignal(double)
     * @see #getMissingDataSignal
     */
    double getMissingDataReplacement();

    /**
     * Specifies the elevation used for missing values in the elevation model.
     *
     * @param missingDataValue the value that indicates that no data is available at a location.
     *
     * @see #setMissingDataSignal(double)
     */
    void setMissingDataReplacement(double missingDataValue);

    /**
     * Determines the elevations at specified locations within a specified {@link Sector}.
     *
     * @param sector    the sector containing the locations.
     * @param latlons   the locations for which to return elevations.
     * @param tileWidth the number of locations that comprise one row in the {@code latlons} argument.
     * @param buffer    a buffer in which to put the elevations. The buffer must have at least as many elements as the
     *                  number of specified locations.
     *
     * @throws Exception                if the method fails. Different elevation models may fail for different reasons.
     * @throws IllegalArgumentException if either the sector, list of locations or buffer is null, if the buffer size is
     *                                  not at least as large as the location list, or the tile width is greater than
     *                                  the locations list length or less than 1.
     */
    void composeElevations(Sector sector, List<? extends LatLon> latlons, int tileWidth, double[] buffer)
        throws Exception;

    /**
     * Returns the proportion of this elevation model's data that is local -- in the computer's data cache or installed
     * data filestore -- for a specified sector and target resolution.
     *
     * @param sector           the sector of interest.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.) Specify null to use this elevation model's best resolution.
     *
     * @return the fraction of the data that is local. A value of 1.0 indicates that all the data is available.
     */
    double getLocalDataAvailability(Sector sector, Double targetResolution);

    /**
     * Indicates whether this elevation model is used or ignored.
     *
     * @param enabled true if this elevation model is used, otherwise false.
     */
    void setEnabled(boolean enabled);

    /**
     * Indicates whether this elevation model is used or ignored.
     *
     * @return true if this elevation model is used, otherwise false.
     */
    boolean isEnabled();

    /**
     * Indicates whether extreme values of sectors should be cached as they're computed. Caching should be disabled if
     * especially many sectors are to be used when querying extreme elevations of this elevation model, such as during
     * terrain intersection calculations. The default is for caching to be enabled. During normal operation caching
     * enhances performance, but during terrain intersection computation it can hamper performance.
     *
     * @param enabled true if extreme value caching should be performed.
     */
    void setExtremesCachingEnabled(boolean enabled);

    /**
     * Indicates whether extreme value caching is enabled.
     *
     * @return true if extreme values caching is enabled, otherwise false.
     *
     * @see #setExtremesCachingEnabled(boolean)
     */
    boolean isExtremesCachingEnabled();

    /**
     * Returns the elevation for this elevation model's highest level of detail if the source file for that level and
     * the specified location exists in the local elevation cache on disk. This method is useful only when an elevation
     * dataset has been pre-cached.
     * @param latitude The latitude of the location whose elevation is desired.
     * @param longitude The longitude of the location whose elevation is desired.
     * @return The elevation at the specified location, if that location is contained in this elevation model and the
     * source file for the highest-resolution elevation at that location exists in the current disk cache. Otherwise
     * this elevation model's missing data signal is returned (see {@link #getMissingDataSignal()}).
     */
    double getUnmappedLocalSourceElevation(Angle latitude, Angle longitude);
}

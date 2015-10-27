/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;

/**
 * Provides operations on the best available terrain. Operations such as line/terrain intersection and surface point
 * computation use the highest resolution terrain data available from the globe's elevation model. Because the best
 * available data may not be available when the operations are performed, the operations block while they retrieve the
 * required data from either the local disk cache or a remote server. A timeout may be specified to limit the amount of
 * time allowed for retrieving data. Operations fail if the timeout is exceeded.
 *
 * @author tag
 * @version $Id: Terrain.java 2056 2014-06-13 00:55:07Z tgaskins $
 */
public interface Terrain
{
    /**
     * Returns the object's globe.
     *
     * @return the globe specified to the constructor.
     */
    Globe getGlobe();

    double getVerticalExaggeration();

    /**
     * Computes the Cartesian, model-coordinate point of a position on the terrain.
     * <p/>
     * This operation fails with a {@link gov.nasa.worldwind.exception.WWTimeoutException} if a timeout has been
     * specified and it is exceeded during the operation.
     *
     * @param position the position.
     *
     * @return the Cartesian, model-coordinate point of the specified position, or null if the specified position does
     *         not exist within this instance's sector or if the operation is interrupted.
     *
     * @throws IllegalArgumentException if the position is null.
     * @throws gov.nasa.worldwind.exception.WWTimeoutException
     *                                  if the current timeout is exceeded while retrieving terrain data.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if the operation is interrupted.
     */
    Vec4 getSurfacePoint(Position position);

    /**
     * Computes the Cartesian, model-coordinate point of a location on the terrain.
     * <p/>
     * This operation fails with a {@link gov.nasa.worldwind.exception.WWTimeoutException} if a timeout has been
     * specified and it is exceeded during the operation.
     *
     * @param latitude     the location's latitude.
     * @param longitude    the location's longitude.
     * @param metersOffset the location's distance above the terrain.
     *
     * @return the Cartesian, model-coordinate point of the specified location, or null if the specified location does
     *         not exist within this instance's sector or if the operation is interrupted.
     *
     * @throws IllegalArgumentException if the latitude or longitude are null.
     * @throws gov.nasa.worldwind.exception.WWTimeoutException
     *                                  if the current timeout is exceeded while retrieving terrain data.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if the operation is interrupted.
     */
    Vec4 getSurfacePoint(Angle latitude, Angle longitude, double metersOffset);

    /**
     * Computes the intersections of a line with the terrain. The line is specified by two positions whose altitude
     * field indicates the height above the terrain (not the altitude relative to sea level). All intersection points
     * are returned.
     * <p/>
     * This operation fails with a {@link gov.nasa.worldwind.exception.WWTimeoutException} if a timeout has been
     * specified and it is exceeded during the operation.
     *
     * @param pA the line's first position.
     * @param pB the line's second position.
     *
     * @return an array of Cartesian model-coordinate intersection points, or null if no intersections occur or the
     *         operation is interrupted.
     *
     * @throws IllegalArgumentException if either position is null.
     * @throws gov.nasa.worldwind.exception.WWTimeoutException
     *                                  if the current timeout is exceeded while retrieving terrain data.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if the operation is interrupted.
     */
    Intersection[] intersect(Position pA, Position pB);

    /**
     * Computes the intersections of a line with the terrain. The line is specified by two positions whose altitude
     * field is interpreted according to the specified altitude mode. All intersection points are returned.
     * <p/>
     * This operation fails with a {@link gov.nasa.worldwind.exception.WWTimeoutException} if a timeout has been
     * specified and it is exceeded during the operation.
     *
     * @param pA           the line's first position.
     * @param pB           the line's second position.
     * @param altitudeMode the altitude mode indicating the reference for the altitudes in the specified positions.
     *
     * @return an array of Cartesian model-coordinate intersection points, or null if no intersections occur or the
     *         operation is interrupted.
     *
     * @throws IllegalArgumentException if either position is null.
     * @throws gov.nasa.worldwind.exception.WWTimeoutException
     *                                  if the current timeout is exceeded while retrieving terrain data.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if the operation is interrupted.
     */
    Intersection[] intersect(Position pA, Position pB, int altitudeMode);

    /**
     * Computes the elevation at a specified location.
     * <p/>
     * This operation fails with a {@link gov.nasa.worldwind.exception.WWTimeoutException} if a timeout has been
     * specified and it is exceeded during the operation.
     *
     * @param location the location at which to compute the elevation.
     *
     * @return the elevation at the location, or null if the elevation could not be determined.
     *
     * @throws IllegalArgumentException if the specified location in null.
     * @throws gov.nasa.worldwind.exception.WWTimeoutException
     *                                  if the current timeout is exceeded while retrieving terrain data.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if the operation is interrupted.
     */
    Double getElevation(LatLon location);
}

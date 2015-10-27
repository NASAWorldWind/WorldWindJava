/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.geom.*;

/**
 * Defines an interface to project geographic coordinates to Cartesian coordinates. Used by {@link Globe2D}
 * implementations to transform geographic coordinates to meters and back.
 * <p/>
 * Each implementation of this interface defines its own constructors, which may accept arguments that completely define
 * the projection.
 *
 * @author tag
 * @version $Id: GeographicProjection.java 2277 2014-08-28 21:19:37Z dcollins $
 */
public interface GeographicProjection
{
    /**
     * Returns the projection name.
     *
     * @return The projection name.
     */
    String getName();

    /**
     * Indicates whether it makes sense to treat this projection as contiguous with itself. If true, the scene
     * controller will make the globe using the projection appear to scroll continuously horizontally.
     *
     * @return <code>true</code> if it makes sense to treat this projection as continuous, otherwise
     *         <code>false</code>.
     */
    boolean isContinuous();

    /**
     * Indicates the latitude limits for this projection.
     *
     * @return The projection limits for this projection.
     */
    Sector getProjectionLimits();

    /**
     * Specifies the limits for this projection.
     *
     * @param projectionLimits The projection limits.
     *
     * @throws IllegalArgumentException if the specified limits is null or the limits are outside the normal range of
     *                                  latitude or longitude.
     */
    void setProjectionLimits(Sector projectionLimits);

    /**
     * Converts a geographic position to meters in Cartesian coordinates.
     * <p/>
     * Note: The input arguments are not checked for <code>null</code> prior to being used. The caller, typically a
     * {@link Globe2D} implementation, is expected do perform that check prior to calling this method.
     *
     * @param globe           The globe this projection is applied to.
     * @param latitude        The latitude of the position.
     * @param longitude       The longitude of the position.
     * @param metersElevation The elevation of the position, in meters.
     * @param offset          An optional offset to be applied to the Cartesian output. Typically only projections that
     *                        are continuous (see {@link #isContinuous()} apply this offset. Others ignore it. May be
     *                        null.
     *
     * @return The Cartesian point, in meters, corresponding to the input position.
     *
     * @see #cartesianToGeographic(Globe, gov.nasa.worldwind.geom.Vec4, gov.nasa.worldwind.geom.Vec4)
     */
    Vec4 geographicToCartesian(Globe globe, Angle latitude, Angle longitude, double metersElevation, Vec4 offset);

    /**
     * Converts a grid of geographic positions to a grid of points in Cartesian coordinates.
     * <p/>
     * This method provides an interface for efficient generation of a grid of cartesian points within a sector. The
     * grid is constructed by dividing the sector into <code>numLon x numLat</code> evenly separated points in
     * geographic coordinates. The first and last points in latitude and longitude are placed at the sector's minimum
     * and maximum boundary, and the remaining points are spaced evenly between those boundary points.
     * <p/>
     * For each grid point within the sector, an elevation value is specified via an array of elevations. The
     * calculation at each position incorporates the associated elevation.
     *
     * @param globe           The globe this projection is applied to.
     * @param sector          The sector over which to generate the points.
     * @param numLat          The number of points to generate latitudinally.
     * @param numLon          The number of points to generate longitudinally.
     * @param metersElevation An array of elevations to incorporate in the point calculations. There must be one
     *                        elevation value in the array for each generated point, so the array must have a length of
     *                        at least <code>numLon x numLat</code>. Elevations are read from this array in row major
     *                        order, beginning with the row of minimum latitude.
     * @param offset          An optional offset to be applied to the Cartesian output. Typically only projections that
     *                        are continuous (see {@link #isContinuous()} apply this offset. Others ignore it. May be
     *                        null.
     * @param out             An array to hold the computed cartesian points. It must have a length of at least
     *                        <code>numLon x numLat</code>. Points are written to this array in row major order,
     *                        beginning with the row of minimum latitude.
     */
    void geographicToCartesian(Globe globe, Sector sector, int numLat, int numLon, double[] metersElevation,
        Vec4 offset, Vec4[] out);

    /**
     * Converts a Cartesian point in meters to a geographic position.
     * <p/>
     * Note: The input arguments are not checked for <code>null</code> prior to being used. The caller, typically a
     * {@link Globe2D} implementation, is expected do perform that check prior to calling this method.
     *
     * @param globe  The globe this projection is applied to.
     * @param cart   The Cartesian point, in meters.
     * @param offset An optional offset to be applied to the Cartesian input prior to converting it. Typically only
     *               projections that are continuous (see {@link #isContinuous()} apply this offset. Others ignore it.
     *               May be null.
     *
     * @return The geographic position corresponding to the input point.
     *
     * @see #geographicToCartesian(Globe, gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle, double,
     *      gov.nasa.worldwind.geom.Vec4)
     */
    Position cartesianToGeographic(Globe globe, Vec4 cart, Vec4 offset);

    /**
     * Computes a Cartesian vector that points north and is tangent to the meridian at the specified geographic
     * location.
     *
     * @param globe     The globe this projection is applied to.
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     *
     * @return The north pointing tangent corresponding to the input location.
     */
    Vec4 northPointingTangent(Globe globe, Angle latitude, Angle longitude);
}

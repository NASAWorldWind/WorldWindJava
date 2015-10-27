/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.*;

import java.util.List;

/**
 * Represents a planet's shape and terrain. A globe may be associated with an {@link ElevationModel} that provides
 * elevations for geographic positions on the surface of the globe. Globe provides methods for converting geographic
 * positions (latitude, longitude, and elevation) to cartesian coordinates, and for converting cartesian to geographic.
 * The origin and orientation of the cartesian coordinate system are determined by implementations of this interface.
 * <p/>
 * <h1>Computations in Cartesian Coordinates</h1>
 * <p/>
 * Globe provides methods for performing computations in the coordinate system represented by a globe's surface in
 * cartesian coordinates. These methods perform work with respect to the globe's actual shape in 3D cartesian
 * coordinates. For an ellipsoidal globe, these methods are equivalent to the ellipsoidal coordinate computations below.
 * For an instance of {@link Globe2D}, these methods work in the cartesian coordinates specified by the globe's 2D
 * projection.
 * <p/>
 * <ul> <li>{@link #computePointFromPosition(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle, double)}</li>
 * <li>{@link #computePositionFromPoint(gov.nasa.worldwind.geom.Vec4)}</li> <li>{@link
 * #computeSurfaceNormalAtLocation(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle)}</li> <li>{@link
 * #computeSurfaceOrientationAtPosition(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle, double)}</li>
 * </ul>
 * <p/>
 * <h1>Computations in Ellipsoidal Coordinates</h1>
 * <p/>
 * Globe provides methods for performing computation on the ellipsoid represented by a globe's equatorial radius and its
 * polar radius. These methods perform work with respect to the ellipsoid in 3D cartesian coordinates. Calling any of
 * these methods on an instance of Globe2D will return the same result as a 3D globe with equivalent radii.
 * <p/>
 * <ul> <li>{@link #computeEllipsoidalPointFromPosition(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle,
 * double)}</li> <li>{@link #computePositionFromEllipsoidalPoint(gov.nasa.worldwind.geom.Vec4)}</li> <li>{@link
 * #computeEllipsoidalNormalAtLocation(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle)}</li> <li>{@link
 * #computeEllipsoidalOrientationAtPosition(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle, double)}</li>
 * </ul>
 *
 * @author Tom Gaskins
 * @version $Id: Globe.java 2295 2014-09-04 17:33:25Z tgaskins $
 */
public interface Globe extends WWObject, Extent
{
    /**
     * Indicates the spatial volume contained by this globe.
     *
     * @return An Extent object representing the volume of space enclosed by this globe.
     */
    Extent getExtent();

    /**
     * Indicates the radius of the globe at the equator, in meters.
     *
     * @return The radius at the equator, in meters.
     */
    double getEquatorialRadius();

    /**
     * Indicates the radius of the globe at the poles, in meters.
     *
     * @return The radius at the poles, in meters.
     */
    double getPolarRadius();

    /**
     * Indicates the maximum radius on the globe.
     *
     * @return The maximum radius, in meters.
     */
    double getMaximumRadius();

    /**
     * Indicates the radius in meters of the globe's ellipsoid at a location.
     *
     * @param latitude  Latitude of the location at which to determine radius.
     * @param longitude Longitude of the location at which to determine radius.
     *
     * @return The radius in meters of the globe's ellipsoid at the specified location.
     */
    double getRadiusAt(Angle latitude, Angle longitude);

    /**
     * Indicates the elevation at a specified location. If the elevation at the specified location is the elevation
     * model's missing data signal, or if the location specified is outside the elevation model's coverage area, the
     * elevation model's missing data replacement value is returned.
     * <p/>
     * The elevation returned from this method is the best available in memory. If no elevation is in memory, the
     * elevation model's minimum extreme elevation at the location is returned. Local disk caches are not consulted.
     *
     * @param latitude  the latitude of the location at which to determine elevation.
     * @param longitude the longitude of the location at which to determine elevation.
     *
     * @return The elevation corresponding to the specified location, or the elevation model's missing-data replacement
     *         value if there is no elevation for the given location. Returns zero if no elevation model is available.
     *
     * @see #getElevationModel()
     */
    double getElevation(Angle latitude, Angle longitude);

    /**
     * Indicates the elevations of a collection of locations. Replaces any elevation values corresponding to the missing
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
     * @param elevations       an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least as many elements as the list of locations.
     *
     * @return the resolution achieved, in radians, or {@link Double#MAX_VALUE} if individual elevations cannot be
     *         determined for all of the locations. Returns zero if an elevation model is not available.
     *
     * @throws IllegalArgumentException if either the sector, latlons list or elevations array is null.
     * @see #getElevationModel()
     */
    double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] elevations);

    /**
     * Indicates the elevations of a collection of locations. Replaces any elevation values corresponding to the missing
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
     *                         same units.) This parameter is an array to allow varying resolutions to be specified for
     *                         {@link CompoundElevationModel}.
     * @param elevations       an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least as many elements as the list of locations.
     *
     * @return the resolution achieved, in radians, or {@link Double#MAX_VALUE} if individual elevations cannot be
     *         determined for all of the locations. Returns zero if an elevation model is not available.
     *
     * @throws IllegalArgumentException if either the sector, latlons list, target resolutions array or elevations array
     *                                  is null.
     * @see #getElevationModel()
     */
    double[] getElevations(Sector sector, List<? extends LatLon> latlons, double[] targetResolution,
        double[] elevations);

    /**
     * Indicates the maximum elevation on this globe, in meters.
     *
     * @return The maximum elevation, or zero if the maximum elevation cannot be determined.
     */
    double getMaxElevation();

    /**
     * Indicates the minimum elevation on this globe, in meters.
     *
     * @return The minimum elevation, or zero if the minimum elevation cannot be determined.
     */
    double getMinElevation();

    /**
     * Computes the intersections of this globe and a line.
     *
     * @param line the line with which to intersect this globe.
     *
     * @return the geographic position of the intersection of this globe and specified line. If there are multiple
     *         intersections the intersection nearest to the line's origin is returned. The intersection may be a
     *         tangent. Returns null if the line does not intersect this globe.
     */
    Position getIntersectionPosition(Line line);

    /**
     * Indicates the square of this globe's eccentricity. <a href="http://mathworld.wolfram.com/Eccentricity.html"
     * target="_blank">Eccentricity</a> is a measure of how the equatorial and polar radii are related.
     *
     * @return The square of this globe's eccentricity.
     */
    double getEccentricitySquared();

    /**
     * Computes a cartesian point from a latitude, longitude, and elevation.
     *
     * @param latitude        Latitude of the location to convert to cartesian.
     * @param longitude       Longitude of the location to convert to cartesian.
     * @param metersElevation Elevation, in meters, of the geographic position to convert to cartesian.
     *
     * @return The cartesian point that corresponds to the specified geographic position.
     */
    Vec4 computePointFromPosition(Angle latitude, Angle longitude, double metersElevation);

    /**
     * Computes a cartesian point from a geographic location and elevation.
     *
     * @param latLon          Geographic location to convert to cartesian.
     * @param metersElevation Elevation, in meters, of the geographic position to convert to cartesian.
     *
     * @return The cartesian point that corresponds to the specified geographic position.
     */
    Vec4 computePointFromPosition(LatLon latLon, double metersElevation);

    /**
     * Computes a cartesian point from a geographic position.
     *
     * @param position Geographic position to convert to cartesian. The position may include elevation above or below
     *                 the globe's surface.
     *
     * @return The cartesian point that corresponds to the specified geographic position.
     */
    Vec4 computePointFromPosition(Position position);

    /**
     * Computes a cartesian point from a geographic location on the surface of this globe.
     *
     * @param location Geographic location on the surface of the globe to convert to cartesian.
     *
     * @return The cartesian point that corresponds to the specified geographic location.
     */
    Vec4 computePointFromLocation(LatLon location);

    /**
     * Computes the geographic position of a point in cartesian coordinates.
     *
     * @param point Point of which to find the geographic position.
     *
     * @return The geographic position of the specified point.
     */
    Position computePositionFromPoint(Vec4 point);

    /**
     * Computes a grid of cartesian points corresponding to a grid of geographic positions.
     * <p/>
     * This method provides an interface for efficient generation of a grid of cartesian points within a sector. The
     * grid is constructed by dividing the sector into <code>numLon x numLat</code> evenly separated points in
     * geographic coordinates. The first and last points in latitude and longitude are placed at the sector's minimum
     * and maximum boundary, and the remaining points are spaced evenly between those boundary points.
     * <p/>
     * For each grid point within the sector, an elevation value is specified via an array of elevations. The
     * calculation at each position incorporates the associated elevation.
     *
     * @param sector          The sector over which to generate the points.
     * @param numLat          The number of points to generate latitudinally.
     * @param numLon          The number of points to generate longitudinally.
     * @param metersElevation An array of elevations to incorporate in the point calculations. There must be one
     *                        elevation value in the array for each generated point, so the array must have a length of
     *                        at least <code>numLon x numLat</code>. Elevations are read from this array in row major
     *                        order, beginning with the row of minimum latitude.
     * @param out             An array to hold the computed cartesian points. It must have a length of at least
     *                        <code>numLon x numLat</code>. Points are written to this array in row major order,
     *                        beginning with the row of minimum latitude.
     *
     * @throws IllegalArgumentException If any argument is null, or if numLat or numLon are less than or equal to zero.
     */
    void computePointsFromPositions(Sector sector, int numLat, int numLon, double[] metersElevation, Vec4[] out);

    /**
     * Computes a vector perpendicular to the surface of this globe in cartesian coordinates.
     *
     * @param latitude  Latitude of the location at which to compute the normal vector.
     * @param longitude Longitude of the location at which to compute the normal vector.
     *
     * @return A vector perpendicular to the surface of this globe, at the specified location.
     */
    Vec4 computeSurfaceNormalAtLocation(Angle latitude, Angle longitude);

    /**
     * Computes a vector perpendicular to the surface of this globe, at a cartesian point.
     *
     * @param point Point in cartesian coordinates at which to compute the normal vector.
     *
     * @return A vector perpendicular to the surface of this globe, at the specified point.
     */
    Vec4 computeSurfaceNormalAtPoint(Vec4 point);

    /**
     * Computes a vector tangent to this globe and pointing toward the north pole.
     *
     * @param latitude  Latitude of the location at which to compute the tangent vector.
     * @param longitude Longitude of the location at which to compute the tangent vector.
     *
     * @return A vector tangent to this globe at (latitude, longitude), and pointing toward the north pole of this
     *         globe.
     */
    Vec4 computeNorthPointingTangentAtLocation(Angle latitude, Angle longitude);

    /** @see #computeSurfaceOrientationAtPosition(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle, double). */
    @SuppressWarnings({"JavaDoc"})
    Matrix computeModelCoordinateOriginTransform(Angle latitude, Angle longitude, double metersElevation);

    /** @see #computeSurfaceOrientationAtPosition(gov.nasa.worldwind.geom.Position) */
    @SuppressWarnings({"JavaDoc"})
    Matrix computeModelCoordinateOriginTransform(Position position);

    /**
     * Returns the cartesian transform matrix that maps model coordinates to a local coordinate system at (latitude,
     * longitude, metersElevation). The X axis is mapped to the vector tangent to the globe and pointing East. The Y
     * axis is mapped to the vector tangent to the globe and pointing to the North Pole. The Z axis is mapped to the
     * globe normal at (latitude, longitude, metersElevation). The origin is mapped to the cartesian position of
     * (latitude, longitude, metersElevation).
     *
     * @param latitude        the latitude of the position.
     * @param longitude       the longitude of the position.
     * @param metersElevation the number of meters above or below mean sea level.
     *
     * @return the cartesian transform matrix that maps model coordinates to the local coordinate system at the
     *         specified position.
     */
    Matrix computeSurfaceOrientationAtPosition(Angle latitude, Angle longitude, double metersElevation);

    /**
     * Returns the cartesian transform matrix that maps model coordinates to a local coordinate system at (latitude,
     * longitude, metersElevation). They X axis is mapped to the vector tangent to the globe and pointing East. The Y
     * axis is mapped to the vector tangent to the globe and pointing to the North Pole. The Z axis is mapped to the
     * globe normal at (latitude, longitude, metersElevation). The origin is mapped to the cartesian position of
     * (latitude, longitude, metersElevation).
     *
     * @param position the latitude, longitude, and number of meters above or below mean sea level.
     *
     * @return The cartesian transform matrix that maps model coordinates to the local coordinate system at the
     *         specified position.
     */
    Matrix computeSurfaceOrientationAtPosition(Position position);

    /**
     * Computes a ellipsoidal point from a latitude, longitude, and elevation.
     * <p/>
     * The returned point is a function of this globe's equatorial radius and its polar radius, and always represents a
     * point on the ellipsoid in 3D cartesian coordinates that corresponds to the specified position. Calling this
     * method on an instance of Globe2D will return a point on the ellipsoid defined by the 2D globe's radii.
     *
     * @param latitude        Latitude of the location to convert.
     * @param longitude       Longitude of the location to convert.
     * @param metersElevation Elevation, in meters, of the geographic position to convert.
     *
     * @return The ellipsoidal point that corresponds to the specified geographic position.
     *
     * @throws java.lang.IllegalArgumentException
     *          if the specified latitude or longitude is null.
     */
    Vec4 computeEllipsoidalPointFromPosition(Angle latitude, Angle longitude, double metersElevation);

    /**
     * Computes a ellipsoidal point from a latitude and longitude.
     * <p/>
     * The returned point is a function of this globe's equatorial radius and its polar radius, and always represents a
     * point on the ellipsoid in 3D cartesian coordinates that corresponds to the specified location. Calling this
     * method on an instance of Globe2D will return a point on the ellipsoid defined by the 2D globe's radii.
     *
     * @param location the location to convert.
     *
     * @return The ellipsoidal point that corresponds to the specified geographic location.
     *
     * @throws java.lang.IllegalArgumentException if the specified location is null.
     */
    Vec4 computeEllipsoidalPointFromLocation(LatLon location);

    /**
     * Computes a ellipsoidal point from a latitude, longitude, and elevation.
     * <p/>
     * The returned point is a function of this globe's equatorial radius and its polar radius, and always represents a
     * point on the ellipsoid in 3D cartesian coordinates that corresponds to the specified position. Calling this
     * method on an instance of Globe2D will return a point on the ellipsoid defined by the 2D globe's radii.
     *
     * @param position Position of the location to convert.
     *
     * @return The ellipsoidal point that corresponds to the specified geographic position.
     *
     * @throws java.lang.IllegalArgumentException
     *          if the specified position is null.
     */
    Vec4 computeEllipsoidalPointFromPosition(Position position);

    /**
     * Computes the geographic position of a point in ellipsoidal coordinates.
     * <p/>
     * The returned position is a function of this globe's equatorial radius and its polar radius, and always represents
     * a position corresponding to the point on the ellipsoid in 3D cartesian coordinates. Calling this method on an
     * instance of Globe2D will return a position corresponding to the ellipsoid defined by the 2D globe's radii.
     *
     * @param ellipsoidalPoint Point of which to find the geographic position, relative to the ellipsoid defined by the
     *                         globe's radii.
     *
     * @return The geographic position of the specified ellipsoidal point.
     */
    Position computePositionFromEllipsoidalPoint(Vec4 ellipsoidalPoint);

    /**
     * Computes a vector perpendicular to the surface of the ellipsoid specified by this globe, in cartesian
     * coordinates.
     * <p/>
     * The returned vector is a function of this globe's equatorial radius and its polar radius, and always represents a
     * vector normal to the corresponding ellipsoid in 3D cartesian coordinates. Calling this method on an instance of
     * Globe2D will return a vector normal to the ellipsoid defined by the 2D globe's radii.
     *
     * @param latitude  Latitude of the location at which to compute the normal vector.
     * @param longitude Longitude of the location at which to compute the normal vector.
     *
     * @return A vector perpendicular to the surface of the ellipsoid specified by this globe, at the specified
     *         location.
     *
     * @throws IllegalArgumentException if either angle is null.
     */
    Vec4 computeEllipsoidalNormalAtLocation(Angle latitude, Angle longitude);

    /**
     * Returns the cartesian transform matrix that maps local model coordinates to an ellipsoidal coordinate system at
     * (latitude, longitude, metersElevation). The X axis is mapped to the vector tangent to the ellipsoid and pointing
     * East. The Y axis is mapped to the vector tangent to the ellipsoid and pointing to the North Pole. The Z axis is
     * mapped to the ellipsoidal normal at (latitude, longitude, metersElevation). The origin is mapped to the
     * ellipsoidal position of (latitude, longitude, metersElevation).
     * <p/>
     * The returned matrix is a function of this globe's equatorial radius and its polar radius, and always represents a
     * transform matrix appropriate for the corresponding ellipsoid in 3D cartesian coordinates. Calling this method on
     * an instance of Globe2D will return a transform matrix for the ellipsoid defined by the 2D globe's radii.
     *
     * @param latitude        the latitude of the position.
     * @param longitude       the longitude of the position.
     * @param metersElevation the number of meters above or below mean sea level.
     *
     * @return The cartesian transform matrix that maps model coordinates to the ellipsoidal coordinate system at the
     *         specified position.
     */

    Matrix computeEllipsoidalOrientationAtPosition(Angle latitude, Angle longitude, double metersElevation);

    /**
     * Indicates the radius in meters of the globe's ellipsoid at a location.
     *
     * @param location the location at which to determine radius.
     *
     * @return The radius in meters of the globe's ellipsoid at the specified location.
     */
    double getRadiusAt(LatLon location);

    /**
     * Returns the minimum and maximum elevations at a specified location on this Globe. This returns a two-element
     * array filled with zero if this Globe has no elevation model.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return A two-element <code>double</code> array indicating the minimum and maximum elevations at the specified
     *         location, respectively. These values are the global minimum and maximum if the local minimum and maximum
     *         values are currently unknown, or zero if this Globe has no elevation model.
     */
    double[] getMinAndMaxElevations(Angle latitude, Angle longitude);

    /**
     * Returns the minimum and maximum elevations within a specified sector on this Globe. This returns a two-element
     * array filled with zero if this Globe has no elevation model.
     *
     * @param sector the sector in question.
     *
     * @return A two-element <code>double</code> array indicating the sector's minimum and maximum elevations,
     *         respectively. These elements are the global minimum and maximum if the local minimum and maximum values
     *         are currently unknown, or zero if this Globe has no elevation model.
     */
    double[] getMinAndMaxElevations(Sector sector);

    /**
     * Intersects a specified line with this globe. Only the ellipsoid itself is considered; terrain elevations are not
     * incorporated.
     *
     * @param line     the line to intersect.
     * @param altitude a distance in meters to expand the globe's equatorial and polar radii prior to performing the
     *                 intersection.
     *
     * @return the intersection points, or null if no intersection occurs or the <code>line</code> is null.
     */
    Intersection[] intersect(Line line, double altitude);

    /**
     * Intersects a specified triangle with the globe. Only the ellipsoid itself is considered; terrain elevations are
     * not incorporated.
     *
     * @param triangle the triangle to intersect.
     * @param altitude a distance in meters to expand the globe's equatorial and polar radii prior to performing the
     *                 intersection.
     *
     * @return the intersection points, or null if no intersection occurs or <code>triangle</code> is null.
     */
    Intersection[] intersect(Triangle triangle, double altitude);

    /**
     * Returns this globe's current tessellator.
     *
     * @return the globe's current tessellator.
     */
    Tessellator getTessellator();

    /**
     * Specifies this globe's tessellator.
     *
     * @param tessellator the new tessellator. Specify null to use the default tessellator.
     */
    void setTessellator(Tessellator tessellator);

    /**
     * Tessellate this globe for the currently visible region.
     *
     * @param dc the current draw context.
     *
     * @return the tessellation, or null if the tessellation failed or the draw context identifies no visible region.
     *
     * @throws IllegalStateException if the globe has no tessellator and a default tessellator cannot be created.
     */
    SectorGeometryList tessellate(DrawContext dc);

    /**
     * Returns a state key identifying this globe's current configuration. Can be used to subsequently determine whether
     * the globe's configuration has changed.
     *
     * @param dc the current draw context.
     *
     * @return a state key for the globe's current configuration.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    Object getStateKey(DrawContext dc);

    /**
     * Returns a typed state key identifying this globe's current configuration. Can be used to subsequently determine
     * whether the globe's configuration has changed.
     *
     * @param dc the current draw context.
     *
     * @return a state key for the globe's current configuration.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    GlobeStateKey getGlobeStateKey(DrawContext dc);

    /**
     * Returns a typed state key identifying this globe's current configuration. Can be used to subsequently determine
     * whether the globe's configuration has changed.
     *
     * @return a state key for the globe's current configuration.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    GlobeStateKey getGlobeStateKey();

    /**
     * Indicates this globe's elevation model.
     *
     * @return this globe's elevation model.
     */
    ElevationModel getElevationModel();

    /**
     * Specifies this globe's elevation model.
     *
     * @param elevationModel this globe's elevation model. May be null to indicate no elevation model.
     */
    void setElevationModel(ElevationModel elevationModel);

    /**
     * Determines whether a point is above a given elevation.
     *
     * @param point     the <code>Vec4</code> point to test. If null, this method returns false.
     * @param elevation the elevation to test for.
     *
     * @return true if the given point is above the given elevation, otherwise false.
     */
    boolean isPointAboveElevation(Vec4 point, double elevation);
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Represents a point on the two-dimensional surface of a globe. Latitude is the degrees North and ranges between [-90,
 * 90], while longitude refers to degrees East, and ranges between (-180, 180].
 * <p/>
 * Instances of <code>LatLon</code> are immutable.
 *
 * @author Tom Gaskins
 * @version $Id: LatLon.java 3427 2015-09-30 23:24:13Z dcollins $
 */
public class LatLon
{
    public static final LatLon ZERO = new LatLon(Angle.ZERO, Angle.ZERO);

    /**
     * A near zero threshold used in some of the rhumb line calculations where floating point calculations cause
     * errors.
     */
    protected final static double NEAR_ZERO_THRESHOLD = 1e-15;

    public final Angle latitude;
    public final Angle longitude;

    /**
     * Factor method for obtaining a new <code>LatLon</code> from two angles expressed in radians.
     *
     * @param latitude  in radians
     * @param longitude in radians
     *
     * @return a new <code>LatLon</code> from the given angles, which are expressed as radians
     */
    public static LatLon fromRadians(double latitude, double longitude)
    {
        return new LatLon(Math.toDegrees(latitude), Math.toDegrees(longitude));
    }

    /**
     * Factory method for obtaining a new <code>LatLon</code> from two angles expressed in degrees.
     *
     * @param latitude  in degrees
     * @param longitude in degrees
     *
     * @return a new <code>LatLon</code> from the given angles, which are expressed as degrees
     */
    public static LatLon fromDegrees(double latitude, double longitude)
    {
        return new LatLon(latitude, longitude);
    }

    private LatLon(double latitude, double longitude)
    {
        this.latitude = Angle.fromDegrees(latitude);
        this.longitude = Angle.fromDegrees(longitude);
    }

    /**
     * Constructs a new  <code>LatLon</code> from two angles. Neither angle may be null.
     *
     * @param latitude  latitude
     * @param longitude longitude
     *
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null
     */
    public LatLon(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLon(LatLon latLon)
    {
        if (latLon == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.latitude = latLon.latitude;
        this.longitude = latLon.longitude;
    }

    /**
     * Obtains the latitude of this <code>LatLon</code>.
     *
     * @return this <code>LatLon</code>'s latitude
     */
    public final Angle getLatitude()
    {
        return this.latitude;
    }

    /**
     * Obtains the longitude of this <code>LatLon</code>.
     *
     * @return this <code>LatLon</code>'s longitude
     */
    public final Angle getLongitude()
    {
        return this.longitude;
    }

    /**
     * Returns an array of this object's latitude and longitude in degrees.
     *
     * @return the array of latitude and longitude, arranged in that order.
     */
    public double[] asDegreesArray()
    {
        return new double[] {this.getLatitude().degrees, this.getLongitude().degrees};
    }

    /**
     * Returns an array of this object's latitude and longitude in radians.
     *
     * @return the array of latitude and longitude, arranged in that order.
     */
    public double[] asRadiansArray()
    {
        return new double[] {this.getLatitude().radians, this.getLongitude().radians};
    }

    /**
     * Returns an interpolated location between <code>value1</code> and <code>value2</code>, according to the specified
     * path type. If the path type is {@link AVKey#GREAT_CIRCLE} this returns an interpolated value on the great arc
     * that spans the two locations (see {@link #interpolateGreatCircle(double, LatLon, LatLon)}). If the path type is
     * {@link AVKey#RHUMB_LINE} or {@link AVKey#LOXODROME} this returns an interpolated value on the rhumb line that
     * spans the two locations (see {@link #interpolateRhumb(double, LatLon, LatLon)}. Otherwise, this returns the
     * linear interpolation of the two locations (see {@link #interpolate(double, LatLon, LatLon)}.
     *
     * @param pathType the path type used to interpolate between geographic locations.
     * @param amount   the interpolation factor
     * @param value1   the first location.
     * @param value2   the second location.
     *
     * @return an interpolated location between <code>value1</code> and <code>value2</code>, according to the specified
     * path type.
     *
     * @throws IllegalArgumentException if the path type or either location is null.
     */
    public static LatLon interpolate(String pathType, double amount, LatLon value1, LatLon value2)
    {
        if (pathType == null)
        {
            String message = Logging.getMessage("nullValue.PathTypeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (value1 == null || value2 == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (pathType.equals(AVKey.GREAT_CIRCLE))
        {
            return interpolateGreatCircle(amount, value1, value2);
        }
        else if (pathType.equals(AVKey.RHUMB_LINE) || pathType.equals(AVKey.LOXODROME))
        {
            return interpolateRhumb(amount, value1, value2);
        }
        else // Default to linear interpolation.
        {
            return interpolate(amount, value1, value2);
        }
    }

    /**
     * Returns the linear interpolation of <code>value1</code> and <code>value2</code>, treating the geographic
     * locations as simple 2D coordinate pairs.
     *
     * @param amount the interpolation factor
     * @param value1 the first location.
     * @param value2 the second location.
     *
     * @return the linear interpolation of <code>value1</code> and <code>value2</code>.
     *
     * @throws IllegalArgumentException if either location is null.
     */
    public static LatLon interpolate(double amount, LatLon value1, LatLon value2)
    {
        if (value1 == null || value2 == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (LatLon.equals(value1, value2))
            return value1;

        Line line;
        try
        {
            line = Line.fromSegment(
                new Vec4(value1.getLongitude().radians, value1.getLatitude().radians, 0),
                new Vec4(value2.getLongitude().radians, value2.getLatitude().radians, 0));
        }
        catch (IllegalArgumentException e)
        {
            // Locations became coincident after calculations.
            return value1;
        }

        Vec4 p = line.getPointAt(amount);

        return LatLon.fromRadians(p.y(), p.x);
    }

    /**
     * Returns the an interpolated location along the great-arc between <code>value1</code> and <code>value2</code>. The
     * interpolation factor <code>amount</code> defines the weight given to each value, and is clamped to the range [0,
     * 1]. If <code>a</code> is 0 or less, this returns <code>value1</code>. If <code>amount</code> is 1 or more, this
     * returns <code>value2</code>. Otherwise, this returns the location on the great-arc between <code>value1</code>
     * and <code>value2</code> corresponding to the specified interpolation factor.
     * This method uses a spherical model, not elliptical.
     *
     * @param amount the interpolation factor
     * @param value1 the first location.
     * @param value2 the second location.
     *
     * @return an interpolated location along the great-arc between <code>value1</code> and <code>value2</code>.
     *
     * @throws IllegalArgumentException if either location is null.
     */
    public static LatLon interpolateGreatCircle(double amount, LatLon value1, LatLon value2)
    {
        if (value1 == null || value2 == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (LatLon.equals(value1, value2))
            return value1;

        double t = WWMath.clamp(amount, 0d, 1d);
        Angle azimuth = LatLon.greatCircleAzimuth(value1, value2);
        Angle distance = LatLon.greatCircleDistance(value1, value2);
        Angle pathLength = Angle.fromDegrees(t * distance.degrees);

        return LatLon.greatCircleEndPosition(value1, azimuth, pathLength);
    }

    /**
     * Returns the an interpolated location along the rhumb line between <code>value1</code> and <code>value2</code>.
     * The interpolation factor <code>amount</code> defines the weight given to each value, and is clamped to the range
     * [0, 1]. If <code>a</code> is 0 or less, this returns <code>value1</code>. If <code>amount</code> is 1 or more,
     * this returns <code>value2</code>. Otherwise, this returns the location on the rhumb line between
     * <code>value1</code> and <code>value2</code> corresponding to the specified interpolation factor.
     * This method uses a spherical model, not elliptical.
     *
     * @param amount the interpolation factor
     * @param value1 the first location.
     * @param value2 the second location.
     *
     * @return an interpolated location along the rhumb line between <code>value1</code> and <code>value2</code>
     *
     * @throws IllegalArgumentException if either location is null.
     */
    public static LatLon interpolateRhumb(double amount, LatLon value1, LatLon value2)
    {
        if (value1 == null || value2 == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (LatLon.equals(value1, value2))
            return value1;

        double t = WWMath.clamp(amount, 0d, 1d);
        Angle azimuth = LatLon.rhumbAzimuth(value1, value2);
        Angle distance = LatLon.rhumbDistance(value1, value2);
        Angle pathLength = Angle.fromDegrees(t * distance.degrees);

        return LatLon.rhumbEndPosition(value1, azimuth, pathLength);
    }

    /**
     * Returns the length of the path between <code>value1</code> and <code>value2</code>, according to the specified
     * path type. If the path type is {@link AVKey#GREAT_CIRCLE} this returns the length of the great arc that spans the
     * two locations (see {@link #greatCircleDistance(LatLon, LatLon)}). If the path type is {@link AVKey#RHUMB_LINE} or
     * {@link AVKey#LOXODROME} this returns the length of the rhumb line that spans the two locations (see {@link
     * #rhumbDistance(LatLon, LatLon)}). Otherwise, this returns the linear distance between the two locations (see
     * {@link #linearDistance(LatLon, LatLon)}).
     *
     * @param pathType the path type used to interpolate between geographic locations.
     * @param value1   the first location.
     * @param value2   the second location.
     *
     * @return an length of the path between <code>value1</code> and <code>value2</code>, according to the specified
     * path type.
     *
     * @throws IllegalArgumentException if the path type or either location is null.
     */
    public static Angle pathDistance(String pathType, LatLon value1, LatLon value2)
    {
        if (pathType == null)
        {
            String message = Logging.getMessage("nullValue.PathTypeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (value1 == null || value2 == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (pathType.equals(AVKey.GREAT_CIRCLE))
        {
            return greatCircleDistance(value1, value2);
        }
        else if (pathType.equals(AVKey.RHUMB_LINE) || pathType.equals(AVKey.LOXODROME))
        {
            return rhumbDistance(value1, value2);
        }
        else // Default to linear interpolation.
        {
            return linearDistance(value1, value2);
        }
    }

    /**
     * Computes the great circle angular distance between two locations. The return value gives the distance as the
     * angle between the two positions on the pi radius circle. In radians, this angle is also the arc length of the
     * segment between the two positions on that circle. To compute a distance in meters from this value, multiply it by
     * the radius of the globe.
     * This method uses a spherical model, not elliptical.
     *
     * @param p1 LatLon of the first location
     * @param p2 LatLon of the second location
     *
     * @return the angular distance between the two locations. In radians, this value is the arc length on the radius pi
     * circle.
     */
    public static Angle greatCircleDistance(LatLon p1, LatLon p2)
    {
        if ((p1 == null) || (p2 == null))
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat1 = p1.getLatitude().radians;
        double lon1 = p1.getLongitude().radians;
        double lat2 = p2.getLatitude().radians;
        double lon2 = p2.getLongitude().radians;

        if (lat1 == lat2 && lon1 == lon2)
            return Angle.ZERO;

        // "Haversine formula," taken from http://en.wikipedia.org/wiki/Great-circle_distance#Formul.C3.A6
        double a = Math.sin((lat2 - lat1) / 2.0);
        double b = Math.sin((lon2 - lon1) / 2.0);
        double c = a * a + +Math.cos(lat1) * Math.cos(lat2) * b * b;
        double distanceRadians = 2.0 * Math.asin(Math.sqrt(c));

        return Double.isNaN(distanceRadians) ? Angle.ZERO : Angle.fromRadians(distanceRadians);
    }

    /**
     * Computes the azimuth angle (clockwise from North) that points from the first location to the second location.
     * This angle can be used as the starting azimuth for a great circle arc that begins at the first location, and
     * passes through the second location.
     * This method uses a spherical model, not elliptical.
     *
     * @param p1 LatLon of the first location
     * @param p2 LatLon of the second location
     *
     * @return Angle that points from the first location to the second location.
     */
    public static Angle greatCircleAzimuth(LatLon p1, LatLon p2)
    {
        if ((p1 == null) || (p2 == null))
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat1 = p1.getLatitude().radians;
        double lon1 = p1.getLongitude().radians;
        double lat2 = p2.getLatitude().radians;
        double lon2 = p2.getLongitude().radians;

        if (lat1 == lat2 && lon1 == lon2)
            return Angle.ZERO;

        if (lon1 == lon2)
            return lat1 > lat2 ? Angle.POS180 : Angle.ZERO;

        // Taken from "Map Projections - A Working Manual", page 30, equation 5-4b.
        // The atan2() function is used in place of the traditional atan(y/x) to simplify the case when x==0.
        double y = Math.cos(lat2) * Math.sin(lon2 - lon1);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);
        double azimuthRadians = Math.atan2(y, x);

        return Double.isNaN(azimuthRadians) ? Angle.ZERO : Angle.fromRadians(azimuthRadians);
    }

    /**
     * Computes the location on a great circle arc with the given starting location, azimuth, and arc distance.
     * This method uses a spherical model, not elliptical.
     *
     * @param p                  LatLon of the starting location
     * @param greatCircleAzimuth great circle azimuth angle (clockwise from North)
     * @param pathLength         arc distance to travel
     *
     * @return LatLon location on the great circle arc.
     */
    public static LatLon greatCircleEndPosition(LatLon p, Angle greatCircleAzimuth, Angle pathLength)
    {
        if (p == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (greatCircleAzimuth == null || pathLength == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat = p.getLatitude().radians;
        double lon = p.getLongitude().radians;
        double azimuth = greatCircleAzimuth.radians;
        double distance = pathLength.radians;

        if (distance == 0)
            return p;

        // Taken from "Map Projections - A Working Manual", page 31, equation 5-5 and 5-6.
        double endLatRadians = Math.asin(Math.sin(lat) * Math.cos(distance)
            + Math.cos(lat) * Math.sin(distance) * Math.cos(azimuth));
        double endLonRadians = lon + Math.atan2(
            Math.sin(distance) * Math.sin(azimuth),
            Math.cos(lat) * Math.cos(distance) - Math.sin(lat) * Math.sin(distance) * Math.cos(azimuth));

        if (Double.isNaN(endLatRadians) || Double.isNaN(endLonRadians))
            return p;

        return new LatLon(
            Angle.fromRadians(endLatRadians).normalizedLatitude(),
            Angle.fromRadians(endLonRadians).normalizedLongitude());
    }

    /**
     * Computes the location on a great circle arc with the given starting location, azimuth, and arc distance.
     * This method uses a spherical model, not elliptical.
     *
     * @param p                         LatLon of the starting location
     * @param greatCircleAzimuthRadians great circle azimuth angle (clockwise from North), in radians
     * @param pathLengthRadians         arc distance to travel, in radians
     *
     * @return LatLon location on the great circle arc.
     */
    public static LatLon greatCircleEndPosition(LatLon p, double greatCircleAzimuthRadians, double pathLengthRadians)
    {
        if (p == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return greatCircleEndPosition(p,
            Angle.fromRadians(greatCircleAzimuthRadians), Angle.fromRadians(pathLengthRadians));
    }

    /**
     * Returns two locations with the most extreme latitudes on the great circle with the given starting location and
     * azimuth.
     * This method uses a spherical model, not elliptical.
     *
     * @param location location on the great circle.
     * @param azimuth  great circle azimuth angle (clockwise from North).
     *
     * @return two locations where the great circle has its extreme latitudes.
     *
     * @throws IllegalArgumentException if either <code>location</code> or <code>azimuth</code> are null.
     */
    public static LatLon[] greatCircleExtremeLocations(LatLon location, Angle azimuth)
    {
        if (location == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (azimuth == null)
        {
            String message = Logging.getMessage("nullValue.AzimuthIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat0 = location.getLatitude().radians;
        double az = azimuth.radians;

        // Derived by solving the function for longitude on a great circle against the desired longitude. We start with
        // the equation in "Map Projections - A Working Manual", page 31, equation 5-5:
        //
        // lat = asin( sin(lat0) * cos(c) + cos(lat0) * sin(c) * cos(Az) )
        //
        // Where (lat0, lon) are the starting coordinates, c is the angular distance along the great circle from the
        // starting coordinate, and Az is the azimuth. All values are in radians.
        //
        // Solving for angular distance gives distance to the equator:
        //
        // tan(c) = -tan(lat0) / cos(Az)
        //
        // The great circle is by definition centered about the Globe's origin. Therefore intersections with the
        // equator will be antipodal (exactly 180 degrees opposite each other), as will be the extreme latitudes.
        // By observing the symmetry of a great circle, it is also apparent that the extreme latitudes will be 90
        // degrees from either intersection with the equator.
        //
        // d1 = c + 90
        // d2 = c - 90

        double tanDistance = -Math.tan(lat0) / Math.cos(az);
        double distance = Math.atan(tanDistance);

        Angle extremeDistance1 = Angle.fromRadians(distance + (Math.PI / 2.0));
        Angle extremeDistance2 = Angle.fromRadians(distance - (Math.PI / 2.0));

        return new LatLon[]
            {
                greatCircleEndPosition(location, azimuth, extremeDistance1),
                greatCircleEndPosition(location, azimuth, extremeDistance2)
            };
    }

    /**
     * Returns two locations with the most extreme latitudes on the great circle arc defined by, and limited to, the two
     * locations.
     * This method uses a spherical model, not elliptical.
     *
     * @param begin beginning location on the great circle arc.
     * @param end   ending location on the great circle arc.
     *
     * @return two locations with the most extreme latitudes on the great circle arc.
     *
     * @throws IllegalArgumentException if either <code>begin</code> or <code>end</code> are null.
     */
    public static LatLon[] greatCircleArcExtremeLocations(LatLon begin, LatLon end)
    {
        if (begin == null)
        {
            String message = Logging.getMessage("nullValue.BeginIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (end == null)
        {
            String message = Logging.getMessage("nullValue.EndIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LatLon minLatLocation = null;
        LatLon maxLatLocation = null;
        double minLat = Angle.POS90.degrees;
        double maxLat = Angle.NEG90.degrees;

        // Compute the min and max latitude and associated locations from the arc endpoints.
        for (LatLon ll : java.util.Arrays.asList(begin, end))
        {
            if (minLat >= ll.getLatitude().degrees)
            {
                minLat = ll.getLatitude().degrees;
                minLatLocation = ll;
            }
            if (maxLat <= ll.getLatitude().degrees)
            {
                maxLat = ll.getLatitude().degrees;
                maxLatLocation = ll;
            }
        }

        // Compute parameters for the great circle arc defined by begin and end. Then compute the locations of extreme
        // latitude on entire the great circle which that arc is part of.
        Angle greatArcAzimuth = greatCircleAzimuth(begin, end);
        Angle greatArcDistance = greatCircleDistance(begin, end);
        LatLon[] greatCircleExtremes = greatCircleExtremeLocations(begin, greatArcAzimuth);

        // Determine whether either of the extreme locations are inside the arc defined by begin and end. If so,
        // adjust the min and max latitude accordingly.
        for (LatLon ll : greatCircleExtremes)
        {
            Angle az = LatLon.greatCircleAzimuth(begin, ll);
            Angle d = LatLon.greatCircleDistance(begin, ll);

            // The extreme location must be between the begin and end locations. Therefore its azimuth relative to
            // the begin location should have the same signum, and its distance relative to the begin location should
            // be between 0 and greatArcDistance, inclusive.
            if (Math.signum(az.degrees) == Math.signum(greatArcAzimuth.degrees))
            {
                if (d.degrees >= 0 && d.degrees <= greatArcDistance.degrees)
                {
                    if (minLat >= ll.getLatitude().degrees)
                    {
                        minLat = ll.getLatitude().degrees;
                        minLatLocation = ll;
                    }
                    if (maxLat <= ll.getLatitude().degrees)
                    {
                        maxLat = ll.getLatitude().degrees;
                        maxLatLocation = ll;
                    }
                }
            }
        }

        return new LatLon[] {minLatLocation, maxLatLocation};
    }

    /**
     * Returns two locations with the most extreme latitudes on the sequence of great circle arcs defined by each pair
     * of locations in the specified iterable.
     * This method uses a spherical model, not elliptical.
     *
     * @param locations the pairs of locations defining a sequence of great circle arcs.
     *
     * @return two locations with the most extreme latitudes on the great circle arcs.
     *
     * @throws IllegalArgumentException if <code>locations</code> is null.
     */
    public static LatLon[] greatCircleArcExtremeLocations(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LatLon minLatLocation = null;
        LatLon maxLatLocation = null;

        LatLon lastLocation = null;

        for (LatLon ll : locations)
        {
            if (lastLocation != null)
            {
                LatLon[] extremes = LatLon.greatCircleArcExtremeLocations(lastLocation, ll);
                if (extremes == null)
                    continue;

                if (minLatLocation == null || minLatLocation.getLatitude().degrees > extremes[0].getLatitude().degrees)
                    minLatLocation = extremes[0];
                if (maxLatLocation == null || maxLatLocation.getLatitude().degrees < extremes[1].getLatitude().degrees)
                    maxLatLocation = extremes[1];
            }

            lastLocation = ll;
        }

        return new LatLon[] {minLatLocation, maxLatLocation};
    }

    /**
     * Computes the length of the rhumb line between two locations. The return value gives the distance as the angular
     * distance between the two positions on the pi radius circle. In radians, this angle is also the arc length of the
     * segment between the two positions on that circle. To compute a distance in meters from this value, multiply it by
     * the radius of the globe.
     * This method uses a spherical model, not elliptical.
     *
     * @param p1 LatLon of the first location
     * @param p2 LatLon of the second location
     *
     * @return the arc length of the rhumb line between the two locations. In radians, this value is the arc length on
     * the radius pi circle.
     */
    public static Angle rhumbDistance(LatLon p1, LatLon p2)
    {
        if (p1 == null || p2 == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat1 = p1.getLatitude().radians;
        double lon1 = p1.getLongitude().radians;
        double lat2 = p2.getLatitude().radians;
        double lon2 = p2.getLongitude().radians;

        if (lat1 == lat2 && lon1 == lon2)
            return Angle.ZERO;

        // Taken from http://www.movable-type.co.uk/scripts/latlong.html
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double q;
        if (Math.abs(dLat) < NEAR_ZERO_THRESHOLD)
        {
            q = Math.cos(lat1);
        }
        else
        {
            double dPhi = Math.log(Math.tan(lat2 / 2.0 + Math.PI / 4.0) / Math.tan(lat1 / 2.0 + Math.PI / 4.0));
            q = dLat / dPhi;
        }

        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI)
        {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }

        double distanceRadians = Math.sqrt(dLat * dLat + q * q * dLon * dLon);

        return Double.isNaN(distanceRadians) ? Angle.ZERO : Angle.fromRadians(distanceRadians);
    }

    /**
     * Computes the azimuth angle (clockwise from North) of a rhumb line (a line of constant heading) between two
     * locations.
     * This method uses a spherical model, not elliptical.
     *
     * @param p1 LatLon of the first location
     * @param p2 LatLon of the second location
     *
     * @return azimuth Angle of a rhumb line between the two locations.
     */
    public static Angle rhumbAzimuth(LatLon p1, LatLon p2)
    {
        if (p1 == null || p2 == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat1 = p1.getLatitude().radians;
        double lon1 = p1.getLongitude().radians;
        double lat2 = p2.getLatitude().radians;
        double lon2 = p2.getLongitude().radians;

        if (lat1 == lat2 && lon1 == lon2)
            return Angle.ZERO;

        // Taken from http://www.movable-type.co.uk/scripts/latlong.html
        double dLon = lon2 - lon1;
        double dPhi = Math.log(Math.tan(lat2 / 2.0 + Math.PI / 4.0) / Math.tan(lat1 / 2.0 + Math.PI / 4.0));
        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI)
        {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }
        double azimuthRadians = Math.atan2(dLon, dPhi);

        return Double.isNaN(azimuthRadians) ? Angle.ZERO : Angle.fromRadians(azimuthRadians);
    }

    /**
     * Computes the location on a rhumb line with the given starting location, rhumb azimuth, and arc distance along the
     * line.
     * This method uses a spherical model, not elliptical.
     *
     * @param p            LatLon of the starting location
     * @param rhumbAzimuth rhumb azimuth angle (clockwise from North)
     * @param pathLength   arc distance to travel
     *
     * @return LatLon location on the rhumb line.
     */
    public static LatLon rhumbEndPosition(LatLon p, Angle rhumbAzimuth, Angle pathLength)
    {
        if (p == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (rhumbAzimuth == null || pathLength == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat1 = p.getLatitude().radians;
        double lon1 = p.getLongitude().radians;
        double azimuth = rhumbAzimuth.radians;
        double distance = pathLength.radians;

        if (distance == 0)
            return p;

        // Taken from http://www.movable-type.co.uk/scripts/latlong.html
        double dLat = distance * Math.cos(azimuth);
        double lat2 = lat1 + dLat;
        double q;
        if (Math.abs(dLat) < NEAR_ZERO_THRESHOLD)
        {
            q = Math.cos(lat1);
        }
        else
        {
            double dPhi = Math.log(Math.tan(lat2 / 2.0 + Math.PI / 4.0) / Math.tan(lat1 / 2.0 + Math.PI / 4.0));
            q = (lat2 - lat1) / dPhi;
        }

        double dLon = distance * Math.sin(azimuth) / q;
        // Handle latitude passing over either pole.
        if (Math.abs(lat2) > Math.PI / 2.0)
        {
            lat2 = lat2 > 0 ? Math.PI - lat2 : -Math.PI - lat2;
        }
        double lon2 = (lon1 + dLon + Math.PI) % (2 * Math.PI) - Math.PI;

        if (Double.isNaN(lat2) || Double.isNaN(lon2))
            return p;

        return new LatLon(
            Angle.fromRadians(lat2).normalizedLatitude(),
            Angle.fromRadians(lon2).normalizedLongitude());
    }

    /**
     * Computes the location on a rhumb line with the given starting location, rhumb azimuth, and arc distance along the
     * line.
     * This method uses a spherical model, not elliptical.
     *
     * @param p                   LatLon of the starting location
     * @param rhumbAzimuthRadians rhumb azimuth angle (clockwise from North), in radians
     * @param pathLengthRadians   arc distance to travel, in radians
     *
     * @return LatLon location on the rhumb line.
     */
    public static LatLon rhumbEndPosition(LatLon p, double rhumbAzimuthRadians, double pathLengthRadians)
    {
        if (p == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return rhumbEndPosition(p, Angle.fromRadians(rhumbAzimuthRadians), Angle.fromRadians(pathLengthRadians));
    }

    /**
     * Computes the length of the linear path between two locations. The return value gives the distance as the angular
     * distance between the two positions on the pi radius circle. In radians, this angle is also the arc length of the
     * segment between the two positions on that circle. To compute a distance in meters from this value, multiply it by
     * the radius of the globe.
     *
     * @param p1 LatLon of the first location
     * @param p2 LatLon of the second location
     *
     * @return the arc length of the line between the two locations. In radians, this value is the arc length on the
     * radius pi circle.
     */
    public static Angle linearDistance(LatLon p1, LatLon p2)
    {
        if (p1 == null || p2 == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat1 = p1.getLatitude().radians;
        double lon1 = p1.getLongitude().radians;
        double lat2 = p2.getLatitude().radians;
        double lon2 = p2.getLongitude().radians;

        if (lat1 == lat2 && lon1 == lon2)
            return Angle.ZERO;

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // If lonChange over 180 take shorter path across 180 meridian.
        if (Math.abs(dLon) > Math.PI)
        {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }

        double distanceRadians = Math.hypot(dLat, dLon);

        return Double.isNaN(distanceRadians) ? Angle.ZERO : Angle.fromRadians(distanceRadians);
    }

    /**
     * Computes the azimuth angle (clockwise from North) of a linear path two locations.
     *
     * @param p1 LatLon of the first location
     * @param p2 LatLon of the second location
     *
     * @return azimuth Angle of a linear path between the two locations.
     */
    public static Angle linearAzimuth(LatLon p1, LatLon p2)
    {
        if (p1 == null || p2 == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat1 = p1.getLatitude().radians;
        double lon1 = p1.getLongitude().radians;
        double lat2 = p2.getLatitude().radians;
        double lon2 = p2.getLongitude().radians;

        if (lat1 == lat2 && lon1 == lon2)
            return Angle.ZERO;

        double dLon = lon2 - lon1;
        double dLat = lat2 - lat1;

        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI)
        {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }
        double azimuthRadians = Math.atan2(dLon, dLat);

        return Double.isNaN(azimuthRadians) ? Angle.ZERO : Angle.fromRadians(azimuthRadians);
    }

    /**
     * Computes the location on a linear path given a starting location, azimuth, and arc distance along the line. A
     * linear path is determined by treating latitude and longitude as a rectangular grid. This type of path is a
     * straight line in the equidistant cylindrical map projection (also called equirectangular).
     *
     * @param p             LatLon of the starting location
     * @param linearAzimuth azimuth angle (clockwise from North)
     * @param pathLength    arc distance to travel
     *
     * @return LatLon location on the line.
     */
    public static LatLon linearEndPosition(LatLon p, Angle linearAzimuth, Angle pathLength)
    {
        if (p == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (linearAzimuth == null || pathLength == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat1 = p.getLatitude().radians;
        double lon1 = p.getLongitude().radians;
        double azimuth = linearAzimuth.radians;
        double distance = pathLength.radians;

        if (distance == 0)
            return p;

        double lat2 = lat1 + distance * Math.cos(azimuth);

        // Handle latitude passing over either pole.
        if (Math.abs(lat2) > Math.PI / 2.0)
        {
            lat2 = lat2 > 0 ? Math.PI - lat2 : -Math.PI - lat2;
        }
        double lon2 = (lon1 + distance * Math.sin(azimuth) + Math.PI) % (2 * Math.PI) - Math.PI;

        if (Double.isNaN(lat2) || Double.isNaN(lon2))
            return p;

        return new LatLon(
            Angle.fromRadians(lat2).normalizedLatitude(),
            Angle.fromRadians(lon2).normalizedLongitude());
    }

    /**
     * Compute the average rhumb distance between locations.
     *
     * @param locations Locations of which to compute average.
     *
     * @return Average rhumb line distance between locations, as an angular distance.
     */
    public static Angle getAverageDistance(Iterable<? extends LatLon> locations)
    {
        if ((locations == null))
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double totalDistance = 0.0;
        int count = 0;

        for (LatLon p1 : locations)
        {
            for (LatLon p2 : locations)
            {
                if (p1 != p2)
                {
                    double d = rhumbDistance(p1, p2).radians;
                    totalDistance += d;
                    count++;
                }
            }
        }

        return (count == 0) ? Angle.ZERO : Angle.fromRadians(totalDistance / (double) count);
    }

    /**
     * Computes the average distance between a specified center point and a list of locations.
     *
     * @param globe     the globe to use for the computations.
     * @param center    the center point.
     * @param locations the locations.
     *
     * @return the average distance.
     *
     * @throws java.lang.IllegalArgumentException if any of the specified globe, center or locations are null.
     */
    public static Angle getAverageDistance(Globe globe, LatLon center, Iterable<? extends LatLon> locations)
    {
        if ((globe == null))
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if ((center == null))
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if ((locations == null))
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int count = 0;
        for (LatLon ignored : locations)
        {
            ++count;
        }

        Vec4 centerPoint = globe.computeEllipsoidalPointFromLocation(center);

        double totalDistance = 0;
        for (LatLon location : locations)
        {
            double distance = globe.computeEllipsoidalPointFromLocation(location).subtract3(centerPoint).getLength3();
            totalDistance += distance / count;
        }

        return (count == 0) ? Angle.ZERO : Angle.fromRadians(totalDistance / globe.getEquatorialRadius());
    }

    /**
     * Computes the average location of a specified list of locations.
     *
     * @param locations the locations.
     *
     * @return the average of the locations.
     *
     * @throws java.lang.IllegalArgumentException if the specified locations is null.
     */
    public static LatLon getCenter(Iterable<? extends LatLon> locations)
    {
        if ((locations == null))
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double latitude = 0;
        double longitude = 0;
        int count = 0;
        for (LatLon location : locations)
        {
            double lon = location.getLongitude().radians;
            if (lon < 0)
                lon += 2 * Math.PI;
            longitude += lon;

            latitude += location.getLatitude().radians;

            ++count;
        }

        if (count > 0)
        {
            latitude /= count;
            longitude /= count;
        }

        if (longitude > Math.PI)
            longitude -= 2 * Math.PI;

        return LatLon.fromRadians(latitude, longitude);
    }


    /**
     * Computes the average location of a specified list of locations.
     *
     * @param globe the globe to use for the computations.
     * @param locations the locations.
     *
     * @return the average of the locations.
     *
     * @throws java.lang.IllegalArgumentException if either the specified globe or locations is null.
     */
    public static LatLon getCenter(Globe globe, Iterable<? extends LatLon> locations)
    {
        if ((globe == null))
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if ((locations == null))
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 center = Vec4.ZERO;

        int count = 0;
        for (LatLon location : locations)
        {
            center = center.add3(globe.computeEllipsoidalPointFromLocation(location));
            ++count;
        }

        return globe.computePositionFromEllipsoidalPoint(center.divide3(count));
    }

    public LatLon add(LatLon that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle lat = Angle.normalizedLatitude(this.latitude.add(that.latitude));
        Angle lon = Angle.normalizedLongitude(this.longitude.add(that.longitude));

        return new LatLon(lat, lon);
    }

    public LatLon subtract(LatLon that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle lat = Angle.normalizedLatitude(this.latitude.subtract(that.latitude));
        Angle lon = Angle.normalizedLongitude(this.longitude.subtract(that.longitude));

        return new LatLon(lat, lon);
    }

    public LatLon add(Position that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle lat = Angle.normalizedLatitude(this.latitude.add(that.getLatitude()));
        Angle lon = Angle.normalizedLongitude(this.longitude.add(that.getLongitude()));

        return new LatLon(lat, lon);
    }

    public LatLon subtract(Position that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle lat = Angle.normalizedLatitude(this.latitude.subtract(that.getLatitude()));
        Angle lon = Angle.normalizedLongitude(this.longitude.subtract(that.getLongitude()));

        return new LatLon(lat, lon);
    }

    public static boolean locationsCrossDateLine(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        LatLon pos = null;
        for (LatLon posNext : locations)
        {
            if (pos != null)
            {
                // A segment cross the line if end pos have different longitude signs
                // and are more than 180 degrees longitude apart
                if (Math.signum(pos.getLongitude().degrees) != Math.signum(posNext.getLongitude().degrees))
                {
                    double delta = Math.abs(pos.getLongitude().degrees - posNext.getLongitude().degrees);
                    if (delta > 180 && delta < 360)
                        return true;
                }
            }
            pos = posNext;
        }

        return false;
    }

    public static boolean locationsCrossDateline(LatLon p1, LatLon p2)
    {
        if (p1 == null || p2 == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // A segment cross the line if end pos have different longitude signs
        // and are more than 180 degrees longitude apart
        if (Math.signum(p1.getLongitude().degrees) != Math.signum(p2.getLongitude().degrees))
        {
            double delta = Math.abs(p1.getLongitude().degrees - p2.getLongitude().degrees);
            if (delta > 180 && delta < 360)
                return true;
        }

        return false;
    }

    /**
     * Determines if a sequence of geographic locations encloses either the North or South pole. The sequence is treated
     * as a closed loop. (If the first and last positions are not equal the loop will be closed for purposes of this
     * computation.)
     *
     * @param locations The locations to test.
     *
     * @return AVKey.NORTH if the North Pole is enclosed, AVKey.SOUTH if the South Pole is enclosed, or null if neither
     * pole is enclosed.
     *
     * @throws java.lang.IllegalArgumentException if the locations are null.
     */
    public static String locationsContainPole(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Determine how many times the path crosses the dateline. Shapes that include a pole will cross an odd number
        // of times.
        // TODO handle locations that contain both poles.
        boolean containsPole = false;

        double minLatitude = 90.0;
        double maxLatitude = -90.0;

        LatLon first = null;
        LatLon prev = null;
        for (LatLon ll : locations)
        {
            if (first == null)
                first = ll;

            if (prev != null && LatLon.locationsCrossDateline(prev, ll))
                containsPole = !containsPole;

            if (ll.latitude.degrees < minLatitude)
                minLatitude = ll.latitude.degrees;

            if (ll.latitude.degrees > maxLatitude)
                maxLatitude = ll.latitude.degrees;

            prev = ll;
        }

        // Close the loop by connecting the last position to the first. If the loop is already closed then the following
        // test will always fail, and will not affect the result.
        if (first != null && LatLon.locationsCrossDateline(first, prev))
            containsPole = !containsPole;

        if (!containsPole)
            return null;

        // Determine which pole is enclosed. If the shape is entirely in one hemisphere, then assume that it encloses
        // the pole in that hemisphere. Otherwise, assume that it encloses the pole that is closest to the shape's
        // extreme latitude.
        if (minLatitude > 0)
            return AVKey.NORTH; // Entirely in Northern Hemisphere
        else if (maxLatitude < 0)
            return AVKey.SOUTH; // Entirely in Southern Hemisphere
        else if (Math.abs(maxLatitude) >= Math.abs(minLatitude))
            return AVKey.NORTH; // Spans equator, but more north than south
        else
            return AVKey.SOUTH;
    }

    /**
     * Returns a list containing two copies of a sequence of geographic locations that cross the dateline: one that
     * extends across the -180 longitude boundary and one that extends across the +180 longitude boundary. If the
     * sequence does not cross the dateline this returns a list containing a copy of the original list.
     *
     * @param locations The locations to repeat.
     *
     * @return A list containing two new location lists, one copy for either side of the dateline.
     *
     * @throws java.lang.IllegalArgumentException if the locations are null.
     */
    public static List<List<LatLon>> repeatLocationsAroundDateline(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        List<List<LatLon>> list = new ArrayList<List<LatLon>>();

        LatLon prev = null;
        double lonOffset = 0;
        boolean applyLonOffset = false;

        List<LatLon> locationsA = new ArrayList<LatLon>();
        list.add(locationsA);

        for (LatLon cur : locations)
        {
            if (prev != null && LatLon.locationsCrossDateline(prev, cur))
            {
                if (lonOffset == 0)
                    lonOffset = (prev.longitude.degrees < 0 ? -360 : 360);

                applyLonOffset = !applyLonOffset;
            }

            if (applyLonOffset)
            {
                locationsA.add(LatLon.fromDegrees(cur.latitude.degrees, cur.longitude.degrees + lonOffset));
            }
            else
            {
                locationsA.add(cur);
            }

            prev = cur;
        }

        if (lonOffset != 0) // longitude offset is non-zero when the locations cross the dateline
        {
            List<LatLon> locationsB = new ArrayList<LatLon>();
            list.add(locationsB);

            for (LatLon cur : locationsA)
            {
                locationsB.add(LatLon.fromDegrees(cur.latitude.degrees, cur.longitude.degrees - lonOffset));
            }
        }

        return list;
    }

    /**
     * Divides a sequence of geographic locations that encloses a pole along the international dateline. This method
     * determines where the locations cross the dateline, and inserts locations to the pole, and then back to the
     * intersection position. This allows the shape to be "unrolled" when projected in a lat-lon projection.
     *
     * @param locations Locations to cut at dateline. This list is not modified.
     * @param pole      Pole contained by locations, either AVKey.NORTH or AVKey.SOUTH.
     * @param globe     Current globe, or null to treat geographic coordinates as linear for the purpose of computing
     *                  the dateline intersection.
     *
     * @return New location list with locations added to correctly handle dateline intersection.
     *
     * @throws java.lang.IllegalArgumentException if the locations are null or if the pole is null.
     */
    public static List<LatLon> cutLocationsAlongDateLine(Iterable<? extends LatLon> locations, String pole, Globe globe)
    {
        if (locations == null)
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (pole == null)
        {
            String msg = Logging.getMessage("nullValue.PoleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        List<LatLon> newLocations = new ArrayList<LatLon>();

        Angle poleLat = AVKey.NORTH.equals(pole) ? Angle.POS90 : Angle.NEG90;

        LatLon pos = null;
        for (LatLon posNext : locations)
        {
            if (pos != null)
            {
                newLocations.add(pos);
                if (LatLon.locationsCrossDateline(pos, posNext))
                {
                    // Determine where the segment crosses the dateline.
                    LatLon separation = LatLon.intersectionWithMeridian(pos, posNext, Angle.POS180, globe);
                    double sign = Math.signum(pos.getLongitude().degrees);

                    Angle lat = separation.getLatitude();
                    Angle thisSideLon = Angle.POS180.multiply(sign);
                    Angle otherSideLon = thisSideLon.multiply(-1);

                    // Add locations that run from the intersection to the pole, then back to the intersection. Note
                    // that the longitude changes sign when the path returns from the pole.
                    //         . Pole
                    //      2 ^ | 3
                    //        | |
                    //      1 | v 4
                    // --->---- ------>
                    newLocations.add(new LatLon(lat, thisSideLon));
                    newLocations.add(new LatLon(poleLat, thisSideLon));
                    newLocations.add(new LatLon(poleLat, otherSideLon));
                    newLocations.add(new LatLon(lat, otherSideLon));
                }
            }
            pos = posNext;
        }
        newLocations.add(pos);

        return newLocations;
    }

    /**
     * Transform the negative longitudes of a dateline-spanning location list to positive values that maintain the
     * relationship with the other locations in the list. Negative longitudes are transformed to values greater than 180
     * degrees, as though longitude spanned [0, 360] rather than [-180, 180]. This enables arithmetic operations to be
     * performed on the locations without having to take into account the longitude jump at the dateline.
     *
     * @param locations the locations to transform. This list is not modified.
     *
     * @return a new list of locations transformed as described above.
     *
     * @throws IllegalArgumentException if the location list is null.
     */
    public static List<LatLon> makeDatelineCrossingLocationsPositive(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Iterator iter = locations.iterator();
        if (!iter.hasNext())
            return Collections.emptyList();

        ArrayList<LatLon> newLocations = new ArrayList<LatLon>();

        for (LatLon location : locations)
        {
            if (location == null)
                continue;

            if (location.getLongitude().degrees < 0)
            {
                newLocations.add(
                    LatLon.fromDegrees(location.getLatitude().degrees, location.getLongitude().degrees + 360));
            }
            else
            {
                newLocations.add(location);
            }
        }

        return newLocations;
    }

    /**
     * Determine where a line between two locations crosses a given meridian. The intersection test is performed by
     * intersecting a line in Cartesian space between the two positions with a plane through the meridian. Thus, it is
     * most suitable for working with positions that are fairly close together as the calculation does not take into
     * account great circle or rhumb paths.
     *
     * @param p1       The first location.
     * @param p2       The second location.
     * @param meridian The line of constant longitude to intersect with.
     * @param globe    Globe used to compute intersection, or null to treat geographic coordinates as linear for the
     *                 purpose of computing the intersection.
     *
     * @return The intersection location along the meridian.
     *
     * @throws java.lang.IllegalArgumentException if either location is null, or if the meridian is null.
     */
    public static LatLon intersectionWithMeridian(LatLon p1, LatLon p2, Angle meridian, Globe globe)
    {
        if (p1 == null || p2 == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (meridian == null)
        {
            String msg = Logging.getMessage("nullValue.MeridianIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (globe == null || globe instanceof Globe2D)
        {
            return intersectionWithMeridian(p1, p2, meridian);
        }

        Vec4 pt1 = globe.computePointFromLocation(p1);
        Vec4 pt2 = globe.computePointFromLocation(p2);

        // Compute a plane through the origin, North Pole, and the desired meridian.
        Vec4 northPole = globe.computePointFromLocation(new LatLon(Angle.POS90, meridian));
        Vec4 pointOnEquator = globe.computePointFromLocation(new LatLon(Angle.ZERO, meridian));

        Plane plane = Plane.fromPoints(northPole, pointOnEquator, Vec4.ZERO);

        Vec4 intersectionPoint = plane.intersect(Line.fromSegment(pt1, pt2));
        if (intersectionPoint == null)
            return null;

        Position intersectionPos = globe.computePositionFromPoint(intersectionPoint);

        return new LatLon(intersectionPos.getLatitude(), meridian);
    }

    /**
     * Determine where a line between two locations crosses a given meridian. The intersection test is performed by
     * treating geographic coordinates as linear and computing the intersection of the linear segment with the vertical
     * line indicated by the meridian. This computation correctly handles intersections with either side of the
     * antimeridian.
     *
     * @param p1       The first location.
     * @param p2       The second location.
     * @param meridian The line of constant longitude to intersect with.
     *
     * @return The intersection location along the meridian.
     *
     * @throws java.lang.IllegalArgumentException if either location is null, or if the meridian is null.
     */
    public static LatLon intersectionWithMeridian(LatLon p1, LatLon p2, Angle meridian)
    {
        if (p1 == null || p2 == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (meridian == null)
        {
            String msg = Logging.getMessage("nullValue.MeridianIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // y = mx + b case after normalizing negative angles.
        double lon1 = p1.getLongitude().degrees < 0 ? p1.getLongitude().degrees + 360 : p1.getLongitude().degrees;
        double lon2 = p2.getLongitude().degrees < 0 ? p2.getLongitude().degrees + 360 : p2.getLongitude().degrees;
        if (lon1 == lon2)
            return null;

        double med = meridian.degrees < 0 ? meridian.degrees + 360 : meridian.degrees;
        double slope = (p2.latitude.degrees - p1.latitude.degrees) / (lon2 - lon1);
        double lat = p1.latitude.degrees + slope * (med - lon1);

        return LatLon.fromDegrees(lat, meridian.degrees);
    }

    /**
     * Parses a string containing latitude and longitude coordinates in either Degrees-minutes-seconds or decimal
     * degrees. The latitude must precede the longitude and the angles must be separated by a comma.
     *
     * @param latLonString a string containing the comma separated latitude and longitude in either DMS or decimal
     *                     degrees.
     *
     * @return a <code>LatLon</code> instance with the parsed angles.
     *
     * @throws IllegalArgumentException if <code>latLonString</code> is null.
     * @throws NumberFormatException    if the string does not form a latitude, longitude pair.
     */
    public LatLon parseLatLon(String latLonString) // TODO
    {
        if (latLonString == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        throw new UnsupportedOperationException(); // TODO: remove when implemented
    }

    @Override
    public String toString()
    {
        String las = String.format("Lat %7.4f\u00B0", this.getLatitude().getDegrees());
        String los = String.format("Lon %7.4f\u00B0", this.getLongitude().getDegrees());
        return "(" + las + ", " + los + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final gov.nasa.worldwind.geom.LatLon latLon = (gov.nasa.worldwind.geom.LatLon) o;

        if (!latitude.equals(latLon.latitude))
            return false;
        //noinspection RedundantIfStatement
        if (!longitude.equals(latLon.longitude))
            return false;

        return true;
    }

    public static boolean equals(LatLon a, LatLon b)
    {
        return a.getLatitude().equals(b.getLatitude()) && a.getLongitude().equals(b.getLongitude());
    }

    @Override
    public int hashCode()
    {
        int result;
        result = latitude.hashCode();
        result = 29 * result + longitude.hashCode();
        return result;
    }

    /**
     * Compute the forward azimuth between two positions
     *
     * @param p1               first position
     * @param p2               second position
     * @param equatorialRadius the equatorial radius of the globe in meters
     * @param polarRadius      the polar radius of the globe in meters
     *
     * @return the azimuth
     */
    public static Angle ellipsoidalForwardAzimuth(LatLon p1, LatLon p2, double equatorialRadius, double polarRadius)
    {
        if (p1 == null || p2 == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO: What if polar radius is larger than equatorial radius?
        // Calculate flattening
        final double f = (equatorialRadius - polarRadius) / equatorialRadius; // flattening

        // Calculate reduced latitudes and related sines/cosines
        final double U1 = Math.atan((1.0 - f) * Math.tan(p1.latitude.radians));
        final double cU1 = Math.cos(U1);
        final double sU1 = Math.sin(U1);

        final double U2 = Math.atan((1.0 - f) * Math.tan(p2.latitude.radians));
        final double cU2 = Math.cos(U2);
        final double sU2 = Math.sin(U2);

        // Calculate difference in longitude
        final double L = p2.longitude.subtract(p1.longitude).radians;

        // Vincenty's Formula for Forward Azimuth
        // iterate until change in lambda is negligible (e.g. 1e-12 ~= 0.06mm)
        // first approximation
        double lambda = L;
        double sLambda = Math.sin(lambda);
        double cLambda = Math.cos(lambda);

        // dummy value to ensure
        double lambda_prev = Double.MAX_VALUE;
        int count = 0;
        while (Math.abs(lambda - lambda_prev) > 1e-12 && count++ < 100)
        {
            // Store old lambda
            lambda_prev = lambda;
            // Calculate new lambda
            double sSigma = Math.sqrt(Math.pow(cU2 * sLambda, 2)
                + Math.pow(cU1 * sU2 - sU1 * cU2 * cLambda, 2));
            double cSigma = sU1 * sU2 + cU1 * cU2 * cLambda;
            double sigma = Math.atan2(sSigma, cSigma);
            double sAlpha = cU1 * cU2 * sLambda / sSigma;
            double cAlpha2 = 1 - sAlpha * sAlpha; // trig identity
            // As cAlpha2 approaches zeros, set cSigmam2 to zero to converge on a solution
            double cSigmam2;
            if (Math.abs(cAlpha2) < 1e-6)
            {
                cSigmam2 = 0;
            }
            else
            {
                cSigmam2 = cSigma - 2 * sU1 * sU2 / cAlpha2;
            }
            double c = f / 16 * cAlpha2 * (4 + f * (4 - 3 * cAlpha2));

            lambda = L + (1 - c) * f * sAlpha * (sigma + c * sSigma * (cSigmam2 + c * cSigma * (-1 + 2 * cSigmam2)));
            sLambda = Math.sin(lambda);
            cLambda = Math.cos(lambda);
        }

        return Angle.fromRadians(Math.atan2(cU2 * sLambda, cU1 * sU2 - sU1 * cU2 * cLambda));
    }

    // TODO: Need method to compute end position from initial position, azimuth and distance. The companion to the
    // spherical version, endPosition(), above.

    /**
     * Computes the distance between two points on an ellipsoid iteratively.
     * <p/>
     * NOTE: This method was copied from the UniData NetCDF Java library. http://www.unidata.ucar.edu/software/netcdf-java/
     * <p/>
     * Algorithm from U.S. National Geodetic Survey, FORTRAN program "inverse," subroutine "INVER1," by L. PFEIFER and
     * JOHN G. GERGEN. See http://www.ngs.noaa.gov/TOOLS/Inv_Fwd/Inv_Fwd.html
     * <p/>
     * Original documentation: SOLUTION OF THE GEODETIC INVERSE PROBLEM AFTER T.VINCENTY MODIFIED RAINSFORD'S METHOD
     * WITH HELMERT'S ELLIPTICAL TERMS EFFECTIVE IN ANY AZIMUTH AND AT ANY DISTANCE SHORT OF ANTIPODAL
     * STANDPOINT/FOREPOINT MUST NOT BE THE GEOGRAPHIC POLE
     * <p/>
     * Requires close to 1.4 E-5 seconds wall clock time per call on a 550 MHz Pentium with Linux 7.2.
     * <p>
     * The algorithm used is iterative and will iterate only 10 times if it does not converge.
     * </p>
     *
     * @param p1               first position
     * @param p2               second position
     * @param equatorialRadius the equatorial radius of the globe in meters
     * @param polarRadius      the polar radius of the globe in meters
     *
     * @return distance in meters between the two points
     */
    public static double ellipsoidalDistance(LatLon p1, LatLon p2, double equatorialRadius, double polarRadius)
    {
        // TODO: I think there is a non-iterative way to calculate the distance. Find it and compare with this one.
        // TODO: What if polar radius is larger than equatorial radius?
        final double F = (equatorialRadius - polarRadius) / equatorialRadius; // flattening = 1.0 / 298.257223563;
        final double R = 1.0 - F;
        final double EPS = 0.5E-13;

        if (p1 == null || p2 == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Algorithm from National Geodetic Survey, FORTRAN program "inverse,"
        // subroutine "INVER1," by L. PFEIFER and JOHN G. GERGEN.
        // http://www.ngs.noaa.gov/TOOLS/Inv_Fwd/Inv_Fwd.html
        // Conversion to JAVA from FORTRAN was made with as few changes as possible
        // to avoid errors made while recasting form, and to facilitate any future
        // comparisons between the original code and the altered version in Java.
        // Original documentation:
        // SOLUTION OF THE GEODETIC INVERSE PROBLEM AFTER T.VINCENTY
        // MODIFIED RAINSFORD'S METHOD WITH HELMERT'S ELLIPTICAL TERMS
        // EFFECTIVE IN ANY AZIMUTH AND AT ANY DISTANCE SHORT OF ANTIPODAL
        // STANDPOINT/FOREPOINT MUST NOT BE THE GEOGRAPHIC POLE
        // A IS THE SEMI-MAJOR AXIS OF THE REFERENCE ELLIPSOID
        // F IS THE FLATTENING (NOT RECIPROCAL) OF THE REFERNECE ELLIPSOID
        // LATITUDES GLAT1 AND GLAT2
        // AND LONGITUDES GLON1 AND GLON2 ARE IN RADIANS POSITIVE NORTH AND EAST
        // FORWARD AZIMUTHS AT BOTH POINTS RETURNED IN RADIANS FROM NORTH
        //
        // Reference ellipsoid is the WGS-84 ellipsoid.
        // See http://www.colorado.edu/geography/gcraft/notes/datum/elist.html
        // FAZ is forward azimuth in radians from pt1 to pt2;
        // BAZ is backward azimuth from point 2 to 1;
        // S is distance in meters.
        //
        // Conversion to JAVA from FORTRAN was made with as few changes as possible
        // to avoid errors made while recasting form, and to facilitate any future
        // comparisons between the original code and the altered version in Java.
        //
        //IMPLICIT REAL*8 (A-H,O-Z)
        //  COMMON/CONST/PI,RAD

        double GLAT1 = p1.getLatitude().radians;
        double GLAT2 = p2.getLatitude().radians;
        double TU1 = R * Math.sin(GLAT1) / Math.cos(GLAT1);
        double TU2 = R * Math.sin(GLAT2) / Math.cos(GLAT2);
        double CU1 = 1. / Math.sqrt(TU1 * TU1 + 1.);
        double SU1 = CU1 * TU1;
        double CU2 = 1. / Math.sqrt(TU2 * TU2 + 1.);
        double S = CU1 * CU2;
        double BAZ = S * TU2;
        double FAZ = BAZ * TU1;
        double GLON1 = p1.getLongitude().radians;
        double GLON2 = p2.getLongitude().radians;
        double X = GLON2 - GLON1;
        double D, SX, CX, SY, CY, Y, SA, C2A, CZ, E, C;
        int iterCount = 0;
        do
        {
            SX = Math.sin(X);
            CX = Math.cos(X);
            TU1 = CU2 * SX;
            TU2 = BAZ - SU1 * CU2 * CX;
            SY = Math.sqrt(TU1 * TU1 + TU2 * TU2);
            CY = S * CX + FAZ;
            Y = Math.atan2(SY, CY);
            SA = S * SX / SY;
            C2A = -SA * SA + 1.;
            CZ = FAZ + FAZ;
            if (C2A > 0.)
            {
                CZ = -CZ / C2A + CY;
            }
            E = CZ * CZ * 2. - 1.;
            C = ((-3. * C2A + 4.) * F + 4.) * C2A * F / 16.;
            D = X;
            X = ((E * CY * C + CZ) * SY * C + Y) * SA;
            X = (1. - C) * X * F + GLON2 - GLON1;
            //IF(DABS(D-X).GT.EPS) GO TO 100

            ++iterCount;
        }
        while (Math.abs(D - X) > EPS && iterCount <= 10);

        //FAZ = Math.atan2(TU1, TU2);
        //BAZ = Math.atan2(CU1 * SX, BAZ * CX - SU1 * CU2) + Math.PI;
        X = Math.sqrt((1. / R / R - 1.) * C2A + 1.) + 1.;
        X = (X - 2.) / X;
        C = 1. - X;
        C = (X * X / 4. + 1.) / C;
        D = (0.375 * X * X - 1.) * X;
        X = E * CY;
        S = 1. - E - E;
        S = ((((SY * SY * 4. - 3.) * S * CZ * D / 6. - X) * D / 4. + CZ) * SY
            * D + Y) * C * equatorialRadius * R;

        return S;
    }

    /**
     * Computes a new set of locations translated from a specified location to a new location.
     *
     * @param oldLocation the original reference location.
     * @param newLocation the new reference location.
     * @param locations   the locations to translate.
     *
     * @return the translated locations, or null if the locations could not be translated.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    public static List<LatLon> computeShiftedLocations(Position oldLocation, Position newLocation,
        Iterable<? extends LatLon> locations)
    {
        // TODO: Account for dateline spanning
        if (oldLocation == null || newLocation == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (locations == null)
        {
            String msg = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        ArrayList<LatLon> newPositions = new ArrayList<LatLon>();

        for (LatLon location : locations)
        {
            Angle distance = LatLon.greatCircleDistance(oldLocation, location);
            Angle azimuth = LatLon.greatCircleAzimuth(oldLocation, location);
            newPositions.add(Position.greatCircleEndPosition(newLocation, azimuth, distance));
        }

        return newPositions;
    }

    public static List<LatLon> computeShiftedLocations(Globe globe, LatLon oldLocation, LatLon newLocation,
        Iterable<? extends LatLon> locations)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (oldLocation == null || newLocation == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (locations == null)
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        ArrayList<LatLon> newLocations = new ArrayList<LatLon>();

        Vec4 oldPoint = globe.computeEllipsoidalPointFromLocation(oldLocation);
        Vec4 newPoint = globe.computeEllipsoidalPointFromLocation(newLocation);
        Vec4 delta = newPoint.subtract3(oldPoint);

        for (LatLon latLon : locations)
        {
            Vec4 point = globe.computeEllipsoidalPointFromLocation(latLon);
            point = point.add3(delta);
            Position newPos = globe.computePositionFromEllipsoidalPoint(point);

            newLocations.add(newPos);
        }

        return newLocations;
    }
}

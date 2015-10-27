/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.util.Logging;

import java.awt.geom.*;
import java.util.*;

/**
 * <code>Sector</code> represents a rectangular region of latitude and longitude. The region is defined by four angles:
 * its minimum and maximum latitude, its minimum and maximum longitude. The angles are assumed to be normalized to +/-
 * 90 degrees latitude and +/- 180 degrees longitude. The minimums and maximums are relative to these ranges, e.g., -80
 * is less than 20. Behavior of the class is undefined for angles outside these ranges. Normalization is not performed
 * on the angles by this class, nor is it verified by the class' methods. See {@link Angle} for a description of
 * specifying angles. <p/> <code>Sector</code> instances are immutable. </p>
 *
 * @author Tom Gaskins
 * @version $Id: Sector.java 2397 2014-10-28 17:13:04Z dcollins $
 * @see Angle
 */
public class Sector implements Cacheable, Comparable<Sector>, Iterable<LatLon>
{
    /** A <code>Sector</code> of latitude [-90 degrees, + 90 degrees] and longitude [-180 degrees, + 180 degrees]. */
    public static final Sector FULL_SPHERE = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG180, Angle.POS180);
    public static final Sector EMPTY_SECTOR = new Sector(Angle.ZERO, Angle.ZERO, Angle.ZERO, Angle.ZERO);

    private final Angle minLatitude;
    private final Angle maxLatitude;
    private final Angle minLongitude;
    private final Angle maxLongitude;
    private final Angle deltaLat;
    private final Angle deltaLon;

    /**
     * Creates a new <code>Sector</code> and initializes it to the specified angles. The angles are assumed to be
     * normalized to +/- 90 degrees latitude and +/- 180 degrees longitude, but this method does not verify that.
     *
     * @param minLatitude  the sector's minimum latitude in degrees.
     * @param maxLatitude  the sector's maximum latitude in degrees.
     * @param minLongitude the sector's minimum longitude in degrees.
     * @param maxLongitude the sector's maximum longitude in degrees.
     *
     * @return the new <code>Sector</code>
     */
    public static Sector fromDegrees(double minLatitude, double maxLatitude, double minLongitude,
        double maxLongitude)
    {
        return new Sector(Angle.fromDegrees(minLatitude), Angle.fromDegrees(maxLatitude), Angle.fromDegrees(
            minLongitude), Angle.fromDegrees(maxLongitude));
    }

    /**
     * Creates a new <code>Sector</code> and initializes it to the specified angles. The angles are assumed to be
     * normalized to +/- 90 degrees latitude and +/- 180 degrees longitude, but this method does not verify that.
     *
     * @param minLatitude  the sector's minimum latitude in degrees.
     * @param maxLatitude  the sector's maximum latitude in degrees.
     * @param minLongitude the sector's minimum longitude in degrees.
     * @param maxLongitude the sector's maximum longitude in degrees.
     *
     * @return the new <code>Sector</code>
     */
    public static Sector fromDegreesAndClamp(double minLatitude, double maxLatitude, double minLongitude,
        double maxLongitude)
    {
        if (minLatitude < -90)
            minLatitude = -90;
        if (maxLatitude > 90)
            maxLatitude = 90;
        if (minLongitude < -180)
            minLongitude = -180;
        if (maxLongitude > 180)
            maxLongitude = 180;

        return new Sector(Angle.fromDegrees(minLatitude), Angle.fromDegrees(maxLatitude), Angle.fromDegrees(
            minLongitude), Angle.fromDegrees(maxLongitude));
    }

    /**
     * Creates a new <code>Sector</code> and initializes it to angles in the specified array. The array is assumed to
     * hold four elements containing the Sector's angles, and must be ordered as follows: minimum latitude, maximum
     * latitude, minimum longitude, and maximum longitude. Additionally, the angles are assumed to be normalized to +/-
     * 90 degrees latitude and +/- 180 degrees longitude, but this method does not verify that.
     *
     * @param array the array of angles in degrees.
     *
     * @return he new <code>Sector</code>
     *
     * @throws IllegalArgumentException if <code>array</code> is null or if its length is less than 4.
     */
    public static Sector fromDegrees(double[] array)
    {
        if (array == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (array.length < 4)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", array.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return fromDegrees(array[0], array[1], array[2], array[3]);
    }

    /**
     * Creates a new <code>Sector</code> and initializes it to the angles resulting from the given {@link
     * java.awt.geom.Rectangle2D} in degrees lat-lon coordinates where x corresponds to longitude and y to latitude. The
     * resulting geographic angles are assumed to be normalized to +/- 90 degrees latitude and +/- 180 degrees
     * longitude, but this method does not verify that.
     *
     * @param rectangle the sector's rectangle in degrees lat-lon coordinates.
     *
     * @return the new <code>Sector</code>
     */
    public static Sector fromDegrees(java.awt.geom.Rectangle2D rectangle)
    {
        return fromDegrees(rectangle.getY(), rectangle.getMaxY(), rectangle.getX(), rectangle.getMaxX());
    }

    /**
     * Creates a new <code>Sector</code> and initializes it to the specified angles. The angles are assumed to be
     * normalized to +/- \u03c0/2 radians latitude and +/- \u03c0 radians longitude, but this method does not verify
     * that.
     *
     * @param minLatitude  the sector's minimum latitude in radians.
     * @param maxLatitude  the sector's maximum latitude in radians.
     * @param minLongitude the sector's minimum longitude in radians.
     * @param maxLongitude the sector's maximum longitude in radians.
     *
     * @return the new <code>Sector</code>
     */
    public static Sector fromRadians(double minLatitude, double maxLatitude, double minLongitude,
        double maxLongitude)
    {
        return new Sector(Angle.fromRadians(minLatitude), Angle.fromRadians(maxLatitude), Angle.fromRadians(
            minLongitude), Angle.fromRadians(maxLongitude));
    }

    /**
     * Returns a geographic Sector which bounds the specified UTM rectangle. The UTM rectangle is located in specified
     * UTM zone and hemisphere.
     *
     * @param zone        the UTM zone.
     * @param hemisphere  the UTM hemisphere, either {@link gov.nasa.worldwind.avlist.AVKey#NORTH} or {@link
     *                    gov.nasa.worldwind.avlist.AVKey#SOUTH}.
     * @param minEasting  the minimum UTM easting, in meters.
     * @param maxEasting  the maximum UTM easting, in meters.
     * @param minNorthing the minimum UTM northing, in meters.
     * @param maxNorthing the maximum UTM northing, in meters.
     *
     * @return a Sector that bounds the specified UTM rectangle.
     *
     * @throws IllegalArgumentException if <code>zone</code> is outside the range 1-60, if <code>hemisphere</code> is
     *                                  null, or if <code>hemisphere</code> is not one of {@link
     *                                  gov.nasa.worldwind.avlist.AVKey#NORTH} or {@link gov.nasa.worldwind.avlist.AVKey#SOUTH}.
     */
    public static Sector fromUTMRectangle(int zone, String hemisphere, double minEasting, double maxEasting,
        double minNorthing, double maxNorthing)
    {
        if (zone < 1 || zone > 60)
        {
            String message = Logging.getMessage("generic.ZoneIsInvalid", zone);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!AVKey.NORTH.equals(hemisphere) && !AVKey.SOUTH.equals(hemisphere))
        {
            String message = Logging.getMessage("generic.HemisphereIsInvalid", hemisphere);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LatLon ll = UTMCoord.locationFromUTMCoord(zone, hemisphere, minEasting, minNorthing, null);
        LatLon lr = UTMCoord.locationFromUTMCoord(zone, hemisphere, maxEasting, minNorthing, null);
        LatLon ur = UTMCoord.locationFromUTMCoord(zone, hemisphere, maxEasting, maxNorthing, null);
        LatLon ul = UTMCoord.locationFromUTMCoord(zone, hemisphere, minEasting, maxNorthing, null);

        return boundingSector(Arrays.asList(ll, lr, ur, ul));
    }

    public static Sector boundingSector(java.util.Iterator<TrackPoint> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.TracksPointsIteratorNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!positions.hasNext())
            return EMPTY_SECTOR;

        TrackPoint position = positions.next();
        double minLat = position.getLatitude();
        double minLon = position.getLongitude();
        double maxLat = minLat;
        double maxLon = minLon;

        while (positions.hasNext())
        {
            TrackPoint p = positions.next();
            double lat = p.getLatitude();
            if (lat < minLat)
                minLat = lat;
            else if (lat > maxLat)
                maxLat = lat;

            double lon = p.getLongitude();
            if (lon < minLon)
                minLon = lon;
            else if (lon > maxLon)
                maxLon = lon;
        }

        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    public static Sector boundingSector(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!locations.iterator().hasNext())
            return EMPTY_SECTOR; // TODO: should be returning null

        double minLat = Angle.POS90.getDegrees();
        double minLon = Angle.POS180.getDegrees();
        double maxLat = Angle.NEG90.getDegrees();
        double maxLon = Angle.NEG180.getDegrees();

        for (LatLon p : locations)
        {
            double lat = p.getLatitude().getDegrees();
            if (lat < minLat)
                minLat = lat;
            if (lat > maxLat)
                maxLat = lat;

            double lon = p.getLongitude().getDegrees();
            if (lon < minLon)
                minLon = lon;
            if (lon > maxLon)
                maxLon = lon;
        }

        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    public static Sector[] splitBoundingSectors(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.LocationInListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!locations.iterator().hasNext())
            return null;

        double minLat = Angle.POS90.getDegrees();
        double minLon = Angle.POS180.getDegrees();
        double maxLat = Angle.NEG90.getDegrees();
        double maxLon = Angle.NEG180.getDegrees();

        LatLon lastLocation = null;

        for (LatLon ll : locations)
        {
            double lat = ll.getLatitude().getDegrees();
            if (lat < minLat)
                minLat = lat;
            if (lat > maxLat)
                maxLat = lat;

            double lon = ll.getLongitude().getDegrees();
            if (lon >= 0 && lon < minLon)
                minLon = lon;
            if (lon <= 0 && lon > maxLon)
                maxLon = lon;

            if (lastLocation != null)
            {
                double lastLon = lastLocation.getLongitude().getDegrees();
                if (Math.signum(lon) != Math.signum(lastLon))
                {
                    if (Math.abs(lon - lastLon) < 180)
                    {
                        // Crossing the zero longitude line too
                        maxLon = 0;
                        minLon = 0;
                    }
                }
            }
            lastLocation = ll;
        }

        if (minLat == maxLat && minLon == maxLon)
            return null;

        return new Sector[]
            {
                Sector.fromDegrees(minLat, maxLat, minLon, 180), // Sector on eastern hemisphere.
                Sector.fromDegrees(minLat, maxLat, -180, maxLon) // Sector on western hemisphere.
            };
    }

    public static Sector boundingSector(LatLon pA, LatLon pB)
    {
        if (pA == null || pB == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double minLat = pA.getLatitude().degrees;
        double minLon = pA.getLongitude().degrees;
        double maxLat = pA.getLatitude().degrees;
        double maxLon = pA.getLongitude().degrees;

        if (pB.getLatitude().degrees < minLat)
            minLat = pB.getLatitude().degrees;
        else if (pB.getLatitude().degrees > maxLat)
            maxLat = pB.getLatitude().degrees;

        if (pB.getLongitude().degrees < minLon)
            minLon = pB.getLongitude().degrees;
        else if (pB.getLongitude().degrees > maxLon)
            maxLon = pB.getLongitude().degrees;

        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    /**
     * Returns a new <code>Sector</code> encompassing a circle centered at a given position, with a given radius in
     * meter.
     *
     * @param globe  a Globe instance.
     * @param center the circle center position.
     * @param radius the circle radius in meter.
     *
     * @return the circle bounding sector.
     */
    public static Sector boundingSector(Globe globe, LatLon center, double radius)
    {
        double halfDeltaLatRadians = radius / globe.getRadiusAt(center);
        double halfDeltaLonRadians = Math.PI * 2;
        if (center.getLatitude().cos() > 0)
            halfDeltaLonRadians = halfDeltaLatRadians / center.getLatitude().cos();

        return new Sector(
            Angle.fromRadiansLatitude(center.getLatitude().radians - halfDeltaLatRadians),
            Angle.fromRadiansLatitude(center.getLatitude().radians + halfDeltaLatRadians),
            Angle.fromRadiansLongitude(center.getLongitude().radians - halfDeltaLonRadians),
            Angle.fromRadiansLongitude(center.getLongitude().radians + halfDeltaLonRadians));
    }

    /**
     * Returns an array of Sectors encompassing a circle centered at a given position, with a given radius in meters. If
     * the geometry defined by the circle and radius spans the international dateline, this will return two sectors, one
     * for each side of the dateline. Otherwise, this will return a single bounding sector. This returns null if the
     * radius is zero.
     *
     * @param globe  a Globe instance.
     * @param center the circle center location.
     * @param radius the circle radius in meters.
     *
     * @return the circle's bounding sectors, or null if the radius is zero.
     *
     * @throws IllegalArgumentException if either the globe or center is null, or if the radius is less than zero.
     */
    public static Sector[] splitBoundingSectors(Globe globe, LatLon center, double radius)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double halfDeltaLatRadians = radius / globe.getRadiusAt(center);

        double minLat = center.getLatitude().radians - halfDeltaLatRadians;
        double maxLat = center.getLatitude().radians + halfDeltaLatRadians;

        double minLon;
        double maxLon;

        // If the circle does not cross a pole, then compute the max and min longitude.
        if (minLat >= Angle.NEG90.radians && maxLat <= Angle.POS90.radians)
        {
            // We want to find the maximum and minimum longitude values on the circle. We will start with equation 5-6
            // from "Map Projections - A Working Manual", page 31, and solve for the value of Az that will maximize
            // lon - lon0.
            //
            // Eq. 5-6:
            // lon = lon0 + arctan( h(lat0, c, az) )
            // h(lat0, c, az) = sin(c) sin(az) / (cos(lat0) cos(c) - sin(lat1) sin(c) cos(Az))
            //
            // Where (lat0, lon0) are the starting coordinates, c is the angular distance along the great circle from
            // the starting coordinate, Az is the azimuth, and lon is the end position longitude. All values are in
            // radians.
            //
            // lon - lon0 is maximized when h(lat0, c, Az) is maximized because arctan(x) -> 1 as x -> infinity.
            //
            // Taking the partial derivative of h with respect to Az we get:
            // h'(Az) = (sin(c) cos(c) cos(lat0) cos(Az) - sin^2(c) sin(lat0)) / (cos(lat0) cos(c) - sin(lat0) sin(c) cos(Az))^2
            //
            // Setting h' = 0 to find maxima:
            // 0 = sin(c) cos(c) cos(lat0) cos(Az) - sin^2(c) sin(lat0)
            //
            // And solving for Az:
            // Az = arccos( tan(lat0) tan(c) )
            //
            // +/- Az are bearings from North that will give positions of max and min longitude.

            // If halfDeltaLatRadians == 90 degrees, then tan is undefined. This can happen if the circle radius is one
            // quarter of the globe diameter, and the circle is centered on the equator. tan(center lat) is always
            // defined because the center lat is in the range -90 to 90 exclusive. If it were equal to 90, then the
            // circle would cover a pole.
            double az;
            if (Math.abs(Angle.POS90.radians - halfDeltaLatRadians)
                > 0.001) // Consider within 1/1000th of a radian to be equal
                az = Math.acos(Math.tan(halfDeltaLatRadians) * Math.tan(center.latitude.radians));
            else
                az = Angle.POS90.radians;

            LatLon east = LatLon.greatCircleEndPosition(center, az, halfDeltaLatRadians);
            LatLon west = LatLon.greatCircleEndPosition(center, -az, halfDeltaLatRadians);

            minLon = Math.min(east.longitude.radians, west.longitude.radians);
            maxLon = Math.max(east.longitude.radians, west.longitude.radians);
        }
        else
        {
            // If the circle crosses the pole then it spans the full circle of longitude
            minLon = Angle.NEG180.radians;
            maxLon = Angle.POS180.radians;
        }

        LatLon ll = new LatLon(
            Angle.fromRadiansLatitude(minLat),
            Angle.normalizedLongitude(Angle.fromRadians(minLon)));
        LatLon ur = new LatLon(
            Angle.fromRadiansLatitude(maxLat),
            Angle.normalizedLongitude(Angle.fromRadians(maxLon)));

        Iterable<? extends LatLon> locations = java.util.Arrays.asList(ll, ur);

        if (LatLon.locationsCrossDateLine(locations))
        {
            return splitBoundingSectors(locations);
        }
        else
        {
            Sector s = boundingSector(locations);
            return (s != null && !s.equals(Sector.EMPTY_SECTOR)) ? new Sector[] {s} : null;
        }
    }

    /**
     * Creates a new <code>Sector</code> and initializes it to the specified angles. The angles are assumed to be
     * normalized to +/- 90 degrees latitude and +/- 180 degrees longitude, but this method does not verify that.
     *
     * @param minLatitude  the sector's minimum latitude.
     * @param maxLatitude  the sector's maximum latitude.
     * @param minLongitude the sector's minimum longitude.
     * @param maxLongitude the sector's maximum longitude.
     *
     * @throws IllegalArgumentException if any of the angles are null
     */
    public Sector(Angle minLatitude, Angle maxLatitude, Angle minLongitude, Angle maxLongitude)
    {
        if (minLatitude == null || maxLatitude == null || minLongitude == null || maxLongitude == null)
        {
            String message = Logging.getMessage("nullValue.InputAnglesNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
        this.deltaLat = Angle.fromDegrees(this.maxLatitude.degrees - this.minLatitude.degrees);
        this.deltaLon = Angle.fromDegrees(this.maxLongitude.degrees - this.minLongitude.degrees);
    }

    public Sector(Sector sector)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minLatitude = new Angle(sector.getMinLatitude());
        this.maxLatitude = new Angle(sector.getMaxLatitude());
        this.minLongitude = new Angle(sector.getMinLongitude());
        this.maxLongitude = new Angle(sector.getMaxLongitude());
        this.deltaLat = Angle.fromDegrees(this.maxLatitude.degrees - this.minLatitude.degrees);
        this.deltaLon = Angle.fromDegrees(this.maxLongitude.degrees - this.minLongitude.degrees);
    }

    /**
     * Returns the sector's minimum latitude.
     *
     * @return The sector's minimum latitude.
     */
    public final Angle getMinLatitude()
    {
        return minLatitude;
    }

    /**
     * Returns the sector's minimum longitude.
     *
     * @return The sector's minimum longitude.
     */
    public final Angle getMinLongitude()
    {
        return minLongitude;
    }

    /**
     * Returns the sector's maximum latitude.
     *
     * @return The sector's maximum latitude.
     */
    public final Angle getMaxLatitude()
    {
        return maxLatitude;
    }

    /**
     * Returns the sector's maximum longitude.
     *
     * @return The sector's maximum longitude.
     */
    public final Angle getMaxLongitude()
    {
        return maxLongitude;
    }

    /**
     * Returns the angular difference between the sector's minimum and maximum latitudes: max - min
     *
     * @return The angular difference between the sector's minimum and maximum latitudes.
     */
    public final Angle getDeltaLat()
    {
        return this.deltaLat;//Angle.fromDegrees(this.maxLatitude.degrees - this.minLatitude.degrees);
    }

    public final double getDeltaLatDegrees()
    {
        return this.deltaLat.degrees;//this.maxLatitude.degrees - this.minLatitude.degrees;
    }

    public final double getDeltaLatRadians()
    {
        return this.deltaLat.radians;//this.maxLatitude.radians - this.minLatitude.radians;
    }

    /**
     * Returns the angular difference between the sector's minimum and maximum longitudes: max - min.
     *
     * @return The angular difference between the sector's minimum and maximum longitudes
     */
    public final Angle getDeltaLon()
    {
        return this.deltaLon;//Angle.fromDegrees(this.maxLongitude.degrees - this.minLongitude.degrees);
    }

    public final double getDeltaLonDegrees()
    {
        return this.deltaLon.degrees;//this.maxLongitude.degrees - this.minLongitude.degrees;
    }

    public final double getDeltaLonRadians()
    {
        return this.deltaLon.radians;//this.maxLongitude.radians - this.minLongitude.radians;
    }

    public boolean isWithinLatLonLimits()
    {
        return this.minLatitude.degrees >= -90 && this.maxLatitude.degrees <= 90
            && this.minLongitude.degrees >= -180 && this.maxLongitude.degrees <= 180;
    }

    public boolean isSameSector(Iterable<? extends LatLon> corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!isSector(corners))
            return false;

        Sector s = Sector.boundingSector(corners);

        return s.equals(this);
    }

    @SuppressWarnings({"RedundantIfStatement"})
    public static boolean isSector(Iterable<? extends LatLon> corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LatLon[] latlons = new LatLon[5];

        int i = 0;
        for (LatLon ll : corners)
        {
            if (i > 4 || ll == null)
                return false;

            latlons[i++] = ll;
        }

        if (!latlons[0].getLatitude().equals(latlons[1].getLatitude()))
            return false;
        if (!latlons[2].getLatitude().equals(latlons[3].getLatitude()))
            return false;
        if (!latlons[0].getLongitude().equals(latlons[3].getLongitude()))
            return false;
        if (!latlons[1].getLongitude().equals(latlons[2].getLongitude()))
            return false;

        if (i == 5 && !latlons[4].equals(latlons[0]))
            return false;

        return true;
    }

    /**
     * Returns the latitude and longitude of the sector's angular center: (minimum latitude + maximum latitude) / 2,
     * (minimum longitude + maximum longitude) / 2.
     *
     * @return The latitude and longitude of the sector's angular center
     */
    public LatLon getCentroid()
    {
        Angle la = Angle.fromDegrees(0.5 * (this.getMaxLatitude().degrees + this.getMinLatitude().degrees));
        Angle lo = Angle.fromDegrees(0.5 * (this.getMaxLongitude().degrees + this.getMinLongitude().degrees));
        return new LatLon(la, lo);
    }

    /**
     * Computes the Cartesian coordinates of a Sector's center.
     *
     * @param globe        The globe associated with the sector.
     * @param exaggeration The vertical exaggeration to apply.
     *
     * @return the Cartesian coordinates of the sector's center.
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    public Vec4 computeCenterPoint(Globe globe, double exaggeration)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double lat = 0.5 * (this.minLatitude.degrees + this.maxLatitude.degrees);
        double lon = 0.5 * (this.minLongitude.degrees + this.maxLongitude.degrees);

        Angle cLat = Angle.fromDegrees(lat);
        Angle cLon = Angle.fromDegrees(lon);
        return globe.computePointFromPosition(cLat, cLon, exaggeration * globe.getElevation(cLat, cLon));
    }

    /**
     * Computes the Cartesian coordinates of a Sector's corners.
     *
     * @param globe        The globe associated with the sector.
     * @param exaggeration The vertical exaggeration to apply.
     *
     * @return an array of four Cartesian points.
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    public Vec4[] computeCornerPoints(Globe globe, double exaggeration)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        Vec4[] corners = new Vec4[4];

        Angle minLat = this.minLatitude;
        Angle maxLat = this.maxLatitude;
        Angle minLon = this.minLongitude;
        Angle maxLon = this.maxLongitude;

        corners[0] = globe.computePointFromPosition(minLat, minLon, exaggeration * globe.getElevation(minLat, minLon));
        corners[1] = globe.computePointFromPosition(minLat, maxLon, exaggeration * globe.getElevation(minLat, maxLon));
        corners[2] = globe.computePointFromPosition(maxLat, maxLon, exaggeration * globe.getElevation(maxLat, maxLon));
        corners[3] = globe.computePointFromPosition(maxLat, minLon, exaggeration * globe.getElevation(maxLat, minLon));

        return corners;
    }

    /**
     * Returns a sphere that minimally surrounds the sector at a specified vertical exaggeration.
     *
     * @param globe                the globe the sector is associated with
     * @param verticalExaggeration the vertical exaggeration to apply to the globe's elevations when computing the
     *                             sphere.
     * @param sector               the sector to return the bounding sphere for.
     *
     * @return The minimal bounding sphere in Cartesian coordinates.
     *
     * @throws IllegalArgumentException if <code>globe</code> or <code>sector</code> is null
     */
    static public Sphere computeBoundingSphere(Globe globe, double verticalExaggeration, Sector sector)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        LatLon center = sector.getCentroid();
        double[] minAndMaxElevations = globe.getMinAndMaxElevations(sector);
        double minHeight = minAndMaxElevations[0] * verticalExaggeration;
        double maxHeight = minAndMaxElevations[1] * verticalExaggeration;

        Vec4[] points = new Vec4[9];
        points[0] = globe.computePointFromPosition(center.getLatitude(), center.getLongitude(), maxHeight);
        points[1] = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMinLongitude(), maxHeight);
        points[2] = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMaxLongitude(), maxHeight);
        points[3] = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), maxHeight);
        points[4] = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), maxHeight);
        points[5] = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMinLongitude(), minHeight);
        points[6] = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMaxLongitude(), minHeight);
        points[7] = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), minHeight);
        points[8] = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), minHeight);

        return Sphere.createBoundingSphere(points);
    }

    /**
     * Returns a {@link gov.nasa.worldwind.geom.Box} that bounds the specified sector on the surface of the specified
     * {@link gov.nasa.worldwind.globes.Globe}. The returned box encloses the globe's surface terrain in the sector,
     * according to the specified vertical exaggeration and the globe's minimum and maximum elevations in the sector. If
     * the minimum and maximum elevation are equal, this assumes a maximum elevation of 10 + the minimum. If this fails
     * to compute a box enclosing the sector, this returns a unit box enclosing one of the boxes corners.
     *
     * @param globe                the globe the extent relates to.
     * @param verticalExaggeration the globe's vertical surface exaggeration.
     * @param sector               a sector on the globe's surface to compute a bounding box for.
     *
     * @return a box enclosing the globe's surface on the specified sector.
     *
     * @throws IllegalArgumentException if either the globe or sector is null.
     */
    public static Box computeBoundingBox(Globe globe, double verticalExaggeration, Sector sector)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double[] minAndMaxElevations = globe.getMinAndMaxElevations(sector);
        return computeBoundingBox(globe, verticalExaggeration, sector, minAndMaxElevations[0], minAndMaxElevations[1]);
    }

    /**
     * Returns a {@link gov.nasa.worldwind.geom.Box} that bounds the specified sector on the surface of the specified
     * {@link gov.nasa.worldwind.globes.Globe}. The returned box encloses the globe's surface terrain in the sector,
     * according to the specified vertical exaggeration, minimum elevation, and maximum elevation. If the minimum and
     * maximum elevation are equal, this assumes a maximum elevation of 10 + the minimum. If this fails to compute a box
     * enclosing the sector, this returns a unit box enclosing one of the boxes corners.
     *
     * @param globe                the globe the extent relates to.
     * @param verticalExaggeration the globe's vertical surface exaggeration.
     * @param sector               a sector on the globe's surface to compute a bounding box for.
     * @param minElevation         the globe's minimum elevation in the sector.
     * @param maxElevation         the globe's maximum elevation in the sector.
     *
     * @return a box enclosing the globe's surface on the specified sector.
     *
     * @throws IllegalArgumentException if either the globe or sector is null.
     */
    public static Box computeBoundingBox(Globe globe, double verticalExaggeration, Sector sector,
        double minElevation, double maxElevation)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute the exaggerated minimum and maximum heights.
        double min = minElevation * verticalExaggeration;
        double max = maxElevation * verticalExaggeration;

        // Ensure the top and bottom heights are not equal.
        if (min == max)
        {
            max = min + 10;
        }

        // Create an array for a 3x5 grid of elevations. Use min height at the corners and max height everywhere else.
        double[] elevations = {
            min, max, max, max, min,
            max, max, max, max, max,
            min, max, max, max, min};

        // Compute the cartesian points for a 3x5 geographic grid. This grid captures enough detail to bound the sector.
        Vec4[] points = new Vec4[15];
        globe.computePointsFromPositions(sector, 3, 5, elevations, points);

        try
        {
            return Box.computeBoundingBox(Arrays.asList(points));
        }
        catch (Exception e)
        {
            return new Box(points[0]); // unit box around point
        }
    }

    /**
     * Returns a cylinder that minimally surrounds the specified sector at a specified vertical exaggeration.
     *
     * @param globe                The globe associated with the sector.
     * @param verticalExaggeration the vertical exaggeration to apply to the minimum and maximum elevations when
     *                             computing the cylinder.
     * @param sector               the sector to return the bounding cylinder for.
     *
     * @return The minimal bounding cylinder in Cartesian coordinates.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null
     */
    static public Cylinder computeBoundingCylinder(Globe globe, double verticalExaggeration, Sector sector)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double[] minAndMaxElevations = globe.getMinAndMaxElevations(sector);
        return computeBoundingCylinder(globe, verticalExaggeration, sector,
            minAndMaxElevations[0], minAndMaxElevations[1]);
    }

    /**
     * Returns a cylinder that minimally surrounds the specified sector at a specified vertical exaggeration and minimum
     * and maximum elevations for the sector.
     *
     * @param globe                The globe associated with the sector.
     * @param verticalExaggeration the vertical exaggeration to apply to the minimum and maximum elevations when
     *                             computing the cylinder.
     * @param sector               the sector to return the bounding cylinder for.
     * @param minElevation         the minimum elevation of the bounding cylinder.
     * @param maxElevation         the maximum elevation of the bounding cylinder.
     *
     * @return The minimal bounding cylinder in Cartesian coordinates.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null
     */
    public static Cylinder computeBoundingCylinder(Globe globe, double verticalExaggeration, Sector sector,
        double minElevation, double maxElevation)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute the exaggerated minimum and maximum heights.
        double minHeight = minElevation * verticalExaggeration;
        double maxHeight = maxElevation * verticalExaggeration;

        if (minHeight == maxHeight)
            maxHeight = minHeight + 1; // ensure the top and bottom of the cylinder won't be coincident

        List<Vec4> points = new ArrayList<Vec4>();
        for (LatLon ll : sector)
        {
            points.add(globe.computePointFromPosition(ll, minHeight));
            points.add(globe.computePointFromPosition(ll, maxHeight));
        }
        points.add(globe.computePointFromPosition(sector.getCentroid(), maxHeight));

        if (sector.getDeltaLonDegrees() > 180)
        {
            // Need to compute more points to ensure the box encompasses the full sector.
            Angle cLon = sector.getCentroid().getLongitude();
            Angle cLat = sector.getCentroid().getLatitude();

            // centroid latitude, longitude midway between min longitude and centroid longitude
            Angle lon = Angle.midAngle(sector.getMinLongitude(), cLon);
            points.add(globe.computePointFromPosition(cLat, lon, maxHeight));

            // centroid latitude, longitude midway between centroid longitude and max longitude
            lon = Angle.midAngle(cLon, sector.getMaxLongitude());
            points.add(globe.computePointFromPosition(cLat, lon, maxHeight));

            // centroid latitude, longitude at min longitude and max longitude
            points.add(globe.computePointFromPosition(cLat, sector.getMinLongitude(), maxHeight));
            points.add(globe.computePointFromPosition(cLat, sector.getMaxLongitude(), maxHeight));
        }

        try
        {
            return Cylinder.computeBoundingCylinder(points);
        }
        catch (Exception e)
        {
            return new Cylinder(points.get(0), points.get(0).add3(Vec4.UNIT_Y), 1);
        }
    }

    static public Cylinder computeBoundingCylinderOrig(Globe globe, double verticalExaggeration, Sector sector)
    {
        return Cylinder.computeVerticalBoundingCylinder(globe, verticalExaggeration, sector);
    }

    /**
     * Returns a cylinder that minimally surrounds the specified minimum and maximum elevations in the sector at a
     * specified vertical exaggeration.
     *
     * @param globe                The globe associated with the sector.
     * @param verticalExaggeration the vertical exaggeration to apply to the minimum and maximum elevations when
     *                             computing the cylinder.
     * @param sector               the sector to return the bounding cylinder for.
     * @param minElevation         the minimum elevation of the bounding cylinder.
     * @param maxElevation         the maximum elevation of the bounding cylinder.
     *
     * @return The minimal bounding cylinder in Cartesian coordinates.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null
     */
    public static Cylinder computeBoundingCylinderOrig(Globe globe, double verticalExaggeration, Sector sector,
        double minElevation, double maxElevation)
    {
        return Cylinder.computeVerticalBoundingCylinder(globe, verticalExaggeration, sector, minElevation,
            maxElevation);
    }

    public final boolean contains(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return containsDegrees(latitude.degrees, longitude.degrees);
    }

    /**
     * Determines whether a latitude/longitude position is within the sector. The sector's angles are assumed to be
     * normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the operation is undefined if
     * they are not.
     *
     * @param latLon the position to test, with angles normalized to +/- &#960 latitude and +/- 2&#960 longitude.
     *
     * @return <code>true</code> if the position is within the sector, <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if <code>latlon</code> is null.
     */
    public final boolean contains(LatLon latLon)
    {
        if (latLon == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.contains(latLon.getLatitude(), latLon.getLongitude());
    }

    /**
     * Determines whether a latitude/longitude postion expressed in radians is within the sector. The sector's angles
     * are assumed to be normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the
     * operation is undefined if they are not.
     *
     * @param radiansLatitude  the latitude in radians of the position to test, normalized +/- &#960.
     * @param radiansLongitude the longitude in radians of the position to test, normalized +/- 2&#960.
     *
     * @return <code>true</code> if the position is within the sector, <code>false</code> otherwise.
     */
    public boolean containsRadians(double radiansLatitude, double radiansLongitude)
    {
        return radiansLatitude >= this.minLatitude.radians && radiansLatitude <= this.maxLatitude.radians
            && radiansLongitude >= this.minLongitude.radians && radiansLongitude <= this.maxLongitude.radians;
    }

    public boolean containsDegrees(double degreesLatitude, double degreesLongitude)
    {
        return degreesLatitude >= this.minLatitude.degrees && degreesLatitude <= this.maxLatitude.degrees
            && degreesLongitude >= this.minLongitude.degrees && degreesLongitude <= this.maxLongitude.degrees;
    }

    /**
     * Determines whether another sector is fully contained within this one. The sector's angles are assumed to be
     * normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the operation is undefined if
     * they are not.
     *
     * @param that the sector to test for containment.
     *
     * @return <code>true</code> if this sector fully contains the input sector, otherwise <code>false</code>.
     */
    public boolean contains(Sector that)
    {
        if (that == null)
            return false;

        // Assumes normalized angles -- [-180, 180], [-90, 90]
        if (that.minLongitude.degrees < this.minLongitude.degrees)
            return false;
        if (that.maxLongitude.degrees > this.maxLongitude.degrees)
            return false;
        if (that.minLatitude.degrees < this.minLatitude.degrees)
            return false;
        //noinspection RedundantIfStatement
        if (that.maxLatitude.degrees > this.maxLatitude.degrees)
            return false;

        return true;
    }

    /**
     * Determines whether this sector intersects another sector's range of latitude and longitude. The sector's angles
     * are assumed to be normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the
     * operation is undefined if they are not.
     *
     * @param that the sector to test for intersection.
     *
     * @return <code>true</code> if the sectors intersect, otherwise <code>false</code>.
     */
    public boolean intersects(Sector that)
    {
        if (that == null)
            return false;

        // Assumes normalized angles -- [-180, 180], [-90, 90]
        if (that.maxLongitude.degrees < this.minLongitude.degrees)
            return false;
        if (that.minLongitude.degrees > this.maxLongitude.degrees)
            return false;
        if (that.maxLatitude.degrees < this.minLatitude.degrees)
            return false;
        //noinspection RedundantIfStatement
        if (that.minLatitude.degrees > this.maxLatitude.degrees)
            return false;

        return true;
    }

    /**
     * Determines whether the interiors of this sector and another sector intersect. The sector's angles are assumed to
     * be normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the operation is undefined
     * if they are not.
     *
     * @param that the sector to test for intersection.
     *
     * @return <code>true</code> if the sectors' interiors intersect, otherwise <code>false</code>.
     *
     * @see #intersects(Sector)
     */
    public boolean intersectsInterior(Sector that)
    {
        if (that == null)
            return false;

        // Assumes normalized angles -- [-180, 180], [-90, 90]
        if (that.maxLongitude.degrees <= this.minLongitude.degrees)
            return false;
        if (that.minLongitude.degrees >= this.maxLongitude.degrees)
            return false;
        if (that.maxLatitude.degrees <= this.minLatitude.degrees)
            return false;
        //noinspection RedundantIfStatement
        if (that.minLatitude.degrees >= this.maxLatitude.degrees)
            return false;

        return true;
    }

    /**
     * Determines whether this sector intersects the specified geographic line segment. The line segment is specified by
     * a begin location and an end location. The locations are are assumed to be connected by a linear path in
     * geographic space. This returns true if any location along that linear path intersects this sector, including the
     * begin and end locations.
     *
     * @param begin the line segment begin location.
     * @param end   the line segment end location.
     *
     * @return true <code>true</code> if this sector intersects the line segment, otherwise <code>false</code>.
     *
     * @throws IllegalArgumentException if either the begin location or the end location is null.
     */
    public boolean intersectsSegment(LatLon begin, LatLon end)
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

        Vec4 segmentBegin = new Vec4(begin.getLongitude().degrees, begin.getLatitude().degrees, 0);
        Vec4 segmentEnd = new Vec4(end.getLongitude().degrees, end.getLatitude().degrees, 0);
        Vec4 tmp = segmentEnd.subtract3(segmentBegin);
        Vec4 segmentCenter = segmentBegin.add3(segmentEnd).divide3(2);
        Vec4 segmentDirection = tmp.normalize3();
        double segmentExtent = tmp.getLength3() / 2.0;

        LatLon centroid = this.getCentroid();
        Vec4 boxCenter = new Vec4(centroid.getLongitude().degrees, centroid.getLatitude().degrees, 0);
        double boxExtentX = this.getDeltaLonDegrees() / 2.0;
        double boxExtentY = this.getDeltaLatDegrees() / 2.0;

        Vec4 diff = segmentCenter.subtract3(boxCenter);

        if (Math.abs(diff.x) > (boxExtentX + segmentExtent * Math.abs(segmentDirection.x)))
        {
            return false;
        }

        if (Math.abs(diff.y) > (boxExtentY + segmentExtent * Math.abs(segmentDirection.y)))
        {
            return false;
        }

        //noinspection SuspiciousNameCombination
        Vec4 segmentPerp = new Vec4(segmentDirection.y, -segmentDirection.x, 0);

        return Math.abs(segmentPerp.dot3(diff)) <=
            (boxExtentX * Math.abs(segmentPerp.x) + boxExtentY * Math.abs(segmentPerp.y));
    }

    /**
     * Determines whether this sector intersects any one of the sectors in the specified iterable. This returns true if
     * at least one of the sectors is non-null and intersects this sector.
     *
     * @param sectors the sectors to test for intersection.
     *
     * @return true if at least one of the sectors is non-null and intersects this sector, otherwise false.
     *
     * @throws java.lang.IllegalArgumentException if the iterable is null.
     */
    public boolean intersectsAny(Iterable<? extends Sector> sectors)
    {
        if (sectors == null)
        {
            String msg = Logging.getMessage("nullValue.SectorListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        for (Sector s : sectors)
        {
            if (s != null && s.intersects(this))
                return true;
        }

        return false;
    }

    /**
     * Returns a new sector whose angles are the extremes of the this sector and another. The new sector's minimum
     * latitude and longitude will be the minimum of the two sectors. The new sector's maximum latitude and longitude
     * will be the maximum of the two sectors. The sectors are assumed to be normalized to +/- 90 degrees latitude and
     * +/- 180 degrees longitude. The result of the operation is undefined if they are not.
     *
     * @param that the sector to join with <code>this</code>.
     *
     * @return A new sector formed from the extremes of the two sectors, or <code>this</code> if the incoming sector is
     *         <code>null</code>.
     */
    public final Sector union(Sector that)
    {
        if (that == null)
            return this;

        Angle minLat = this.minLatitude;
        Angle maxLat = this.maxLatitude;
        Angle minLon = this.minLongitude;
        Angle maxLon = this.maxLongitude;

        if (that.minLatitude.degrees < this.minLatitude.degrees)
            minLat = that.minLatitude;
        if (that.maxLatitude.degrees > this.maxLatitude.degrees)
            maxLat = that.maxLatitude;
        if (that.minLongitude.degrees < this.minLongitude.degrees)
            minLon = that.minLongitude;
        if (that.maxLongitude.degrees > this.maxLongitude.degrees)
            maxLon = that.maxLongitude;

        return new Sector(minLat, maxLat, minLon, maxLon);
    }

    public final Sector union(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
            return this;

        Angle minLat = this.minLatitude;
        Angle maxLat = this.maxLatitude;
        Angle minLon = this.minLongitude;
        Angle maxLon = this.maxLongitude;

        if (latitude.degrees < this.minLatitude.degrees)
            minLat = latitude;
        if (latitude.degrees > this.maxLatitude.degrees)
            maxLat = latitude;
        if (longitude.degrees < this.minLongitude.degrees)
            minLon = longitude;
        if (longitude.degrees > this.maxLongitude.degrees)
            maxLon = longitude;

        return new Sector(minLat, maxLat, minLon, maxLon);
    }

    public static Sector union(Sector sectorA, Sector sectorB)
    {
        if (sectorA == null || sectorB == null)
        {
            if (sectorA == sectorB)
                return sectorA;

            return sectorB == null ? sectorA : sectorB;
        }

        return sectorA.union(sectorB);
    }

    public static Sector union(Iterable<? extends Sector> sectors)
    {
        if (sectors == null)
        {
            String msg = Logging.getMessage("nullValue.SectorListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle minLat = Angle.POS90;
        Angle maxLat = Angle.NEG90;
        Angle minLon = Angle.POS180;
        Angle maxLon = Angle.NEG180;

        for (Sector s : sectors)
        {
            if (s == null)
                continue;

            for (LatLon p : s)
            {
                if (p.getLatitude().degrees < minLat.degrees)
                    minLat = p.getLatitude();
                if (p.getLatitude().degrees > maxLat.degrees)
                    maxLat = p.getLatitude();
                if (p.getLongitude().degrees < minLon.degrees)
                    minLon = p.getLongitude();
                if (p.getLongitude().degrees > maxLon.degrees)
                    maxLon = p.getLongitude();
            }
        }

        return new Sector(minLat, maxLat, minLon, maxLon);
    }

    public final Sector intersection(Sector that)
    {
        if (that == null)
            return this;

        Angle minLat, maxLat;
        minLat = (this.minLatitude.degrees > that.minLatitude.degrees) ? this.minLatitude : that.minLatitude;
        maxLat = (this.maxLatitude.degrees < that.maxLatitude.degrees) ? this.maxLatitude : that.maxLatitude;
        if (minLat.degrees > maxLat.degrees)
            return null;

        Angle minLon, maxLon;
        minLon = (this.minLongitude.degrees > that.minLongitude.degrees) ? this.minLongitude : that.minLongitude;
        maxLon = (this.maxLongitude.degrees < that.maxLongitude.degrees) ? this.maxLongitude : that.maxLongitude;
        if (minLon.degrees > maxLon.degrees)
            return null;

        return new Sector(minLat, maxLat, minLon, maxLon);
    }

    public final Sector intersection(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
            return this;

        if (!this.contains(latitude, longitude))
            return null;
        return new Sector(latitude, latitude, longitude, longitude);
    }

    /**
     * Returns the intersection of all sectors in the specified iterable. This returns a non-null sector if the iterable
     * contains at least one non-null entry and all non-null entries intersect. The returned sector represents the
     * geographic region in which all sectors intersect. This returns null if at least one of the sectors does not
     * intersect the others.
     *
     * @param sectors the sectors to intersect.
     *
     * @return the intersection of all sectors in the specified iterable, or null if at least one of the sectors does
     * not intersect the others.
     *
     * @throws java.lang.IllegalArgumentException if the iterable is null.
     */
    public static Sector intersection(Iterable<? extends Sector> sectors)
    {
        if (sectors == null)
        {
            String msg = Logging.getMessage("nullValue.SectorListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Sector result = null;

        for (Sector s : sectors)
        {
            if (s == null)
                continue; // ignore null sectors

            if (result == null)
                result = s; // start with the first non-null sector
            else if ((result = result.intersection(s)) == null)
                break; // at least one of the sectors does not intersect the others
        }

        return result;
    }

    public Sector[] subdivide()
    {
        Angle midLat = Angle.average(this.minLatitude, this.maxLatitude);
        Angle midLon = Angle.average(this.minLongitude, this.maxLongitude);

        Sector[] sectors = new Sector[4];
        sectors[0] = new Sector(this.minLatitude, midLat, this.minLongitude, midLon);
        sectors[1] = new Sector(this.minLatitude, midLat, midLon, this.maxLongitude);
        sectors[2] = new Sector(midLat, this.maxLatitude, this.minLongitude, midLon);
        sectors[3] = new Sector(midLat, this.maxLatitude, midLon, this.maxLongitude);

        return sectors;
    }

    public Sector[] subdivide(int div)
    {
        double dLat = this.deltaLat.degrees / div;
        double dLon = this.deltaLon.degrees / div;

        Sector[] sectors = new Sector[div * div];
        int idx = 0;
        for (int row = 0; row < div; row++)
        {
            for (int col = 0; col < div; col++)
            {
                sectors[idx++] = Sector.fromDegrees(
                    this.minLatitude.degrees + dLat * row,
                    this.minLatitude.degrees + dLat * row + dLat,
                    this.minLongitude.degrees + dLon * col,
                    this.minLongitude.degrees + dLon * col + dLon);
            }
        }

        return sectors;
    }

    /**
     * Returns an approximation of the distance in model coordinates between the surface geometry defined by this sector
     * and the specified model coordinate point. The returned value represents the shortest distance between the
     * specified point and this sector's corner points or its center point. The draw context defines the globe and the
     * elevations that are used to compute the corner points and the center point.
     *
     * @param dc    The draw context defining the surface geometry.
     * @param point The model coordinate point to compute a distance to.
     *
     * @return The distance between this sector's surface geometry and the specified point, in model coordinates.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    public double distanceTo(DrawContext dc, Vec4 point)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (point == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4[] corners = this.computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration());
        Vec4 centerPoint = this.computeCenterPoint(dc.getGlobe(), dc.getVerticalExaggeration());

        // Get the distance for each of the sector's corners and its center.
        double d1 = point.distanceTo3(corners[0]);
        double d2 = point.distanceTo3(corners[1]);
        double d3 = point.distanceTo3(corners[2]);
        double d4 = point.distanceTo3(corners[3]);
        double d5 = point.distanceTo3(centerPoint);

        // Find the minimum distance.
        double minDistance = d1;
        if (minDistance > d2)
            minDistance = d2;
        if (minDistance > d3)
            minDistance = d3;
        if (minDistance > d4)
            minDistance = d4;
        if (minDistance > d5)
            minDistance = d5;

        return minDistance;
    }

    /**
     * Returns a four element array containing the Sector's angles in degrees. The returned array is ordered as follows:
     * minimum latitude, maximum latitude, minimum longitude, and maximum longitude.
     *
     * @return four-element array containing the Sector's angles.
     */
    public double[] toArrayDegrees()
    {
        return new double[]
            {
                this.minLatitude.degrees, this.maxLatitude.degrees,
                this.minLongitude.degrees, this.maxLongitude.degrees
            };
    }

    /**
     * Returns a {@link java.awt.geom.Rectangle2D} corresponding to this Sector in degrees lat-lon coordinates where x
     * corresponds to longitude and y to latitude.
     *
     * @return a {@link java.awt.geom.Rectangle2D} corresponding to this Sector in degrees lat-lon coordinates.
     */
    public Rectangle2D toRectangleDegrees()
    {
        return new Rectangle2D.Double(this.getMinLongitude().degrees, this.getMinLatitude().degrees,
            this.getDeltaLonDegrees(), this.getDeltaLatDegrees());
    }

    /**
     * Returns a string indicating the sector's angles.
     *
     * @return A string indicating the sector's angles.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.minLatitude.toString());
        sb.append(", ");
        sb.append(this.minLongitude.toString());
        sb.append(")");

        sb.append(", ");

        sb.append("(");
        sb.append(this.maxLatitude.toString());
        sb.append(", ");
        sb.append(this.maxLongitude.toString());
        sb.append(")");

        return sb.toString();
    }

    /**
     * Retrieve the size of this object in bytes. This implementation returns an exact value of the object's size.
     *
     * @return the size of this object in bytes
     */
    public long getSizeInBytes()
    {
        return 4 * minLatitude.getSizeInBytes();  // 4 angles
    }

    /**
     * Compares this sector to a specified sector according to their minimum latitude, minimum longitude, maximum
     * latitude, and maximum longitude, respectively.
     *
     * @param that the <code>Sector</code> to compareTo with <code>this</code>.
     *
     * @return -1 if this sector compares less than that specified, 0 if they're equal, and 1 if it compares greater.
     *
     * @throws IllegalArgumentException if <code>that</code> is null
     */
    public int compareTo(Sector that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.getMinLatitude().compareTo(that.getMinLatitude()) < 0)
            return -1;

        if (this.getMinLatitude().compareTo(that.getMinLatitude()) > 0)
            return 1;

        if (this.getMinLongitude().compareTo(that.getMinLongitude()) < 0)
            return -1;

        if (this.getMinLongitude().compareTo(that.getMinLongitude()) > 0)
            return 1;

        if (this.getMaxLatitude().compareTo(that.getMaxLatitude()) < 0)
            return -1;

        if (this.getMaxLatitude().compareTo(that.getMaxLatitude()) > 0)
            return 1;

        if (this.getMaxLongitude().compareTo(that.getMaxLongitude()) < 0)
            return -1;

        if (this.getMaxLongitude().compareTo(that.getMaxLongitude()) > 0)
            return 1;

        return 0;
    }

    /**
     * Creates an iterator over the four corners of the sector, starting with the southwest position and continuing
     * counter-clockwise.
     *
     * @return an iterator for the sector.
     */
    public Iterator<LatLon> iterator()
    {
        return new Iterator<LatLon>()
        {
            private int position = 0;

            public boolean hasNext()
            {
                return this.position < 4;
            }

            public LatLon next()
            {
                if (this.position > 3)
                    throw new NoSuchElementException();

                LatLon p;
                switch (this.position)
                {
                    case 0:
                        p = new LatLon(Sector.this.getMinLatitude(), Sector.this.getMinLongitude());
                        break;
                    case 1:
                        p = new LatLon(Sector.this.getMinLatitude(), Sector.this.getMaxLongitude());
                        break;
                    case 2:
                        p = new LatLon(Sector.this.getMaxLatitude(), Sector.this.getMaxLongitude());
                        break;
                    default:
                        p = new LatLon(Sector.this.getMaxLatitude(), Sector.this.getMinLongitude());
                        break;
                }
                ++this.position;

                return p;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns the coordinates of the sector as a list, in the order minLat, maxLat, minLon, maxLon.
     *
     * @return the list of sector coordinates.
     */
    public List<LatLon> asList()
    {
        ArrayList<LatLon> list = new ArrayList<LatLon>(4);

        for (LatLon ll : this)
        {
            list.add(ll);
        }

        return list;
    }

    /**
     * Returns the coordinates of the sector as an array of values in degrees, in the order minLat, maxLat, minLon,
     * maxLon.
     *
     * @return the array of sector coordinates.
     */
    public double[] asDegreesArray()
    {
        return new double[]
            {
                this.getMinLatitude().degrees, this.getMaxLatitude().degrees,
                this.getMinLongitude().degrees, this.getMaxLongitude().degrees
            };
    }

    /**
     * Returns the coordinates of the sector as an array of values in radians, in the order minLat, maxLat, minLon,
     * maxLon.
     *
     * @return the array of sector coordinates.
     */
    public double[] asRadiansArray()
    {
        return new double[]
            {
                this.getMinLatitude().radians, this.getMaxLatitude().radians,
                this.getMinLongitude().radians, this.getMaxLongitude().radians
            };
    }

    /**
     * Returns a list of the Lat/Lon coordinates of a Sector's corners.
     *
     * @return an array of the four corner locations, in the order SW, SE, NE, NW
     */
    public LatLon[] getCorners()
    {
        LatLon[] corners = new LatLon[4];

        corners[0] = new LatLon(this.minLatitude, this.minLongitude);
        corners[1] = new LatLon(this.minLatitude, this.maxLongitude);
        corners[2] = new LatLon(this.maxLatitude, this.maxLongitude);
        corners[3] = new LatLon(this.maxLatitude, this.minLongitude);

        return corners;
    }

    /**
     * Tests the equality of the sectors' angles. Sectors are equal if all of their corresponding angles are equal.
     *
     * @param o the sector to compareTo with <code>this</code>.
     *
     * @return <code>true</code> if the four corresponding angles of each sector are equal, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final gov.nasa.worldwind.geom.Sector sector = (gov.nasa.worldwind.geom.Sector) o;

        if (!maxLatitude.equals(sector.maxLatitude))
            return false;
        if (!maxLongitude.equals(sector.maxLongitude))
            return false;
        if (!minLatitude.equals(sector.minLatitude))
            return false;
        //noinspection RedundantIfStatement
        if (!minLongitude.equals(sector.minLongitude))
            return false;

        return true;
    }

    /**
     * Computes a hash code from the sector's four angles.
     *
     * @return a hash code incorporating the sector's four angles.
     */
    @Override
    public int hashCode()
    {
        int result;
        result = minLatitude.hashCode();
        result = 29 * result + maxLatitude.hashCode();
        result = 29 * result + minLongitude.hashCode();
        result = 29 * result + maxLongitude.hashCode();
        return result;
    }
}
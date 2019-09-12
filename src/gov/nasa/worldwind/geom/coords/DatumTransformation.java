/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.util.Logging;

/**
 * Class with static methods for datum transformation.  Currently shifts between NAD27 and WGS84. Other shifts will be
 * added as needed.
 *
 * @author jparsons
 * @version $Id: DatumTransformation.java 1958 2014-04-24 19:25:37Z tgaskins $
 */
public class DatumTransformation
{

    private final static double Clarke1866_EQUATORIAL_RADIUS = 6378206.4;   // ellipsoid equatorial getRadius, in meters
    private final static double Clarke1866_POLAR_RADIUS = 6356583.8;        // ellipsoid polar getRadius, in meters
    private final static double Clarke1866_ES = 0.00676865799729;           // eccentricity squared, semi-major axis
    public static Globe CLARKE1866_GLOBE = new EllipsoidalGlobe(Clarke1866_EQUATORIAL_RADIUS, Clarke1866_POLAR_RADIUS,
        Clarke1866_ES,
        EllipsoidalGlobe.makeElevationModel(AVKey.EARTH_ELEVATION_MODEL_CONFIG_FILE,
            "config/Earth/EarthElevations2.xml"));

    /**
     * Shift datum from NAD27 to WGS84
     *
     * @param pos the original {@link Position} in NAD27
     *
     * @return the {@link Position} in WGS84
     *
     * @throws IllegalArgumentException if Position is null
     */
    public static Position convertNad27toWGS84(Position pos)
    {
        if (pos == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        //todo cite source for shift values
        double dx_nad27_to_wgs84 = -8.0;
        double dy_nad27_to_wgs84 = 160;
        double dz_nad27_to_wgs84 = 176;

        return DatumTransformation.threeParamMolodenski(pos, CLARKE1866_GLOBE, new Earth(),
            dx_nad27_to_wgs84, dy_nad27_to_wgs84, dz_nad27_to_wgs84);
    }

    /**
     * Shift datum from WGS84 to NAD27
     *
     * @param pos the original {@link Position} in WGS84
     *
     * @return the {@link Position} in NAD27
     *
     * @throws IllegalArgumentException if Position is null
     */
    public static Position convertWGS84toNad27(Position pos)
    {
        if (pos == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        //todo cite source for shift values
        double dx_wgs84_to_nad27 = 8.0;
        double dy_wgs84_to_nad27 = -160;
        double dz_wgs84_to_nad27 = -176;

        return DatumTransformation.threeParamMolodenski(pos, new Earth(), CLARKE1866_GLOBE,
            dx_wgs84_to_nad27, dy_wgs84_to_nad27, dz_wgs84_to_nad27);
    }

    private static Position threeParamMolodenski(Position source, Globe fromGlobe, Globe toGlobe,
        double dx, double dy, double dz)
    {

        double sinLat = Math.sin(source.getLatitude().getRadians());
        double cosLat = Math.cos(source.getLatitude().getRadians());
        double sinLon = Math.sin(source.getLongitude().getRadians());
        double cosLon = Math.cos(source.getLongitude().getRadians());
        double sinLatsquared = sinLat * sinLat;
        double fromF = (fromGlobe.getEquatorialRadius() - fromGlobe.getPolarRadius()) / fromGlobe.getEquatorialRadius();
        double toF = (toGlobe.getEquatorialRadius() - toGlobe.getPolarRadius()) / toGlobe.getEquatorialRadius();
        double dF = toF - fromF;
        double adb = 1.0 / (1.0 - fromF);

        double dEquatorialRadius = (toGlobe.getEquatorialRadius() - fromGlobe.getEquatorialRadius());

        double rn = fromGlobe.getEquatorialRadius() / Math.sqrt(
            1.0 - fromGlobe.getEccentricitySquared() * sinLatsquared);
        double rm = fromGlobe.getEquatorialRadius() * (1. - fromGlobe.getEccentricitySquared()) /
            Math.pow((1.0 - fromGlobe.getEccentricitySquared() * sinLatsquared), 1.5);

        double dLat = (((((-dx * sinLat * cosLon - dy * sinLat * sinLon) + dz * cosLat)
            + (dEquatorialRadius * ((rn * fromGlobe.getEccentricitySquared() * sinLat * cosLat)
            / fromGlobe.getEquatorialRadius())))
            + (dF * (rm * adb + rn / adb) * sinLat * cosLat)))
            / (rm + source.getElevation());

        double dLon = (-dx * sinLon + dy * cosLon) / ((rn + source.getElevation()) * cosLat);

        double dh = (dx * cosLat * cosLon) + (dy * cosLat * sinLon) + (dz * sinLat)
            - (dEquatorialRadius * (fromGlobe.getEquatorialRadius() / rn)) + ((dF * rn * sinLatsquared) / adb);

        return Position.fromRadians(source.getLatitude().getRadians() + dLat,
            source.getLongitude().getRadians() + dLon, source.getElevation() + dh);
    }
}


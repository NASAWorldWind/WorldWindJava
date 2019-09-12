/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.avlist.AVKey;

/**
 * Defines a model of the Earth projected onto a plane. The Earth's radius as defined by the <a
 * href="http://en.wikipedia.org/wiki/World_Geodetic_System" target="_blank">World Geodetic System</a> (WGS84).
 *
 * @author Tom Gaskins
 * @version $Id: EarthFlat.java 1958 2014-04-24 19:25:37Z tgaskins $
 */

public class EarthFlat extends FlatGlobe
{
    public static final double WGS84_EQUATORIAL_RADIUS = 6378137.0; // ellipsoid equatorial getRadius, in meters
    public static final double WGS84_POLAR_RADIUS = 6356752.3; // ellipsoid polar getRadius, in meters
    public static final double WGS84_ES = 0.00669437999013; // eccentricity squared, semi-major axis

    public EarthFlat()
    {
        super(WGS84_EQUATORIAL_RADIUS, WGS84_POLAR_RADIUS, WGS84_ES,
            EllipsoidalGlobe.makeElevationModel(AVKey.EARTH_ELEVATION_MODEL_CONFIG_FILE,
                "config/Earth/EarthElevations2.xml"));
    }

    public String toString()
    {
        return "Flat Earth";
    }
}

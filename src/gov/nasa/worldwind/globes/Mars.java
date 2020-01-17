/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
 */
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.avlist.AVKey;

/**
 * @author Patrick Murris
 * @version $Id: Mars.java 5189 2008-04-27 04:06:56Z patrickmurris $
 */
public class Mars extends EllipsoidalGlobe {

    // From http://en.wikipedia.org/wiki/Mars
    public static final double WGS84_EQUATORIAL_RADIUS = 3396200.0; // ellipsoid equatorial getRadius, in meters
    public static final double WGS84_POLAR_RADIUS = 3376200.0; // ellipsoid polar getRadius, in meters
    public static final double WGS84_ES = 0.00589; // eccentricity squared, semi-major axis

    public Mars() {
        // super(WGS84_EQUATORIAL_RADIUS, WGS84_POLAR_RADIUS, WGS84_ES, new MarsElevationModel());
        super(WGS84_EQUATORIAL_RADIUS, WGS84_POLAR_RADIUS, WGS84_ES,
            EllipsoidalGlobe.makeElevationModel(AVKey.MARS_ELEVATION_MODEL_CONFIG_FILE,
                "config/Mars/MarsElevations.xml"));
    }
}

/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.globes.projections;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.util.Logging;

/**
 * Defines a polar equidistant projection centered on a specified pole.
 *
 * @author tag
 * @version $Id: ProjectionPolarEquidistant.java 2277 2014-08-28 21:19:37Z dcollins $
 */
public class ProjectionPolarEquidistant extends AbstractGeographicProjection
{
    protected static final int NORTH = 0;
    protected static final int SOUTH = 1;

    protected int pole = NORTH;

    /**
     * Creates a projection centered on the North pole.
     */
    public ProjectionPolarEquidistant()
    {
        super(Sector.FULL_SPHERE);
    }

    /**
     * Creates a projection centered on the specified pole, which can be either {@link AVKey#NORTH} or {@link
     * AVKey#SOUTH}.
     *
     * @param pole The pole to center on, either {@link AVKey#NORTH} or {@link AVKey#SOUTH}.
     *
     * @throws IllegalArgumentException if the specified pole is null.
     */
    public ProjectionPolarEquidistant(String pole)
    {
        super(Sector.FULL_SPHERE);

        if (pole == null)
        {
            String message = Logging.getMessage("nullValue.HemisphereIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pole = pole.equals(AVKey.SOUTH) ? SOUTH : NORTH;
    }

    public String getName()
    {
        return (this.pole == SOUTH ? "South " : "North ") + "Polar Equidistant";
    }

    @Override
    public boolean isContinuous()
    {
        return false;
    }

    /**
     * Indicates the pole on which this projection is centered.
     *
     * @return The pole on which this projection is centered, either {@link AVKey#NORTH} or {@link AVKey#SOUTH}.
     */
    public String getPole()
    {
        return this.pole == SOUTH ? AVKey.SOUTH : AVKey.NORTH;
    }

    @Override
    public Vec4 geographicToCartesian(Globe globe, Angle latitude, Angle longitude, double metersElevation, Vec4 offset)
    {
        // Formulae taken from "Map Projections -- A Working Manual", Snyder, USGS paper 1395, pg. 195.

        if ((this.pole == NORTH && latitude.degrees == 90) || (this.pole == SOUTH && latitude.degrees == -90))
            return new Vec4(0, 0, metersElevation);

        double a = globe.getRadius() * (Math.PI / 2 + latitude.radians * (this.pole == SOUTH ? 1 : -1));
        double x = a * Math.sin(longitude.radians);
        double y = a * Math.cos(longitude.radians) * (this.pole == SOUTH ? 1 : -1);

        return new Vec4(x, y, metersElevation);
    }

    @Override
    public void geographicToCartesian(Globe globe, Sector sector, int numLat, int numLon, double[] metersElevation,
        Vec4 offset, Vec4[] out)
    {
        double radius = globe.getRadius();
        double minLat = sector.getMinLatitude().radians;
        double maxLat = sector.getMaxLatitude().radians;
        double minLon = sector.getMinLongitude().radians;
        double maxLon = sector.getMaxLongitude().radians;
        double deltaLat = (maxLat - minLat) / (numLat > 1 ? numLat - 1 : 1);
        double deltaLon = (maxLon - minLon) / (numLon > 1 ? numLon - 1 : 1);
        double pole = (this.pole == SOUTH) ? 1 : -1;
        double pi_2 = Math.PI / 2;
        int pos = 0;

        // Iterate over the longitude coordinates in the specified sector and compute the cosine and sine of each
        // longitude value required to compute Cartesian points for the specified sector. This eliminates the need to
        // re-compute the same cosine and sine results for each row of constant latitude (and varying longitude).
        double[] cosLon = new double[numLon];
        double[] sinLon = new double[numLon];
        double lon = minLon;
        for (int i = 0; i < numLon; i++, lon += deltaLon)
        {
            if (i == numLon - 1) // explicitly set the last lon to the max longitude to ensure alignment
                lon = maxLon;

            cosLon[i] = Math.cos(lon);
            sinLon[i] = Math.sin(lon);
        }

        // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian point
        // corresponding to each latitude and longitude.
        double lat = minLat;
        for (int j = 0; j < numLat; j++, lat += deltaLat)
        {
            if (j == numLat - 1) // explicitly set the last lat to the max latitude to ensure alignment
                lat = maxLat;

            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            double a = radius * (pi_2 + lat * pole);
            if ((this.pole == NORTH && lat == pi_2) || (this.pole == SOUTH && lat == -pi_2))
            {
                a = 0;
            }

            for (int i = 0; i < numLon; i++)
            {
                double x = a * sinLon[i];
                double y = a * cosLon[i] * pole;
                double z = metersElevation[pos];
                out[pos++] = new Vec4(x, y, z);
            }
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public Position cartesianToGeographic(Globe globe, Vec4 cart, Vec4 offset)
    {
        // Formulae taken from "Map Projections -- A Working Manual", Snyder, USGS paper 1395, pg. 196.

        double rho = Math.sqrt(cart.x * cart.x + cart.y * cart.y);
        if (rho < 1.0e-4)
            return Position.fromDegrees((this.pole == SOUTH ? -90 : 90), 0, cart.z);

        double c = rho / globe.getRadius();
        if (c > Math.PI) // map cartesian points beyond the projections radius to the edge of the projection
            c = Math.PI;

        double lat = Math.asin(Math.cos(c) * (this.pole == SOUTH ? -1 : 1));
        double lon = Math.atan2(cart.x, cart.y * (this.pole == SOUTH ? 1 : -1)); // use atan2(x,y) instead of atan(x/y)

        return Position.fromRadians(lat, lon, cart.z);
    }

    @Override
    public Vec4 northPointingTangent(Globe globe, Angle latitude, Angle longitude)
    {
        // The north pointing tangent depends on the pole. With the south pole, the north pointing tangent points in the
        // same direction as the vector returned by cartesianToGeographic. With the north pole, the north pointing
        // tangent has the opposite direction.

        double x = Math.sin(longitude.radians) * (this.pole == SOUTH ? 1 : -1);
        double y = Math.cos(longitude.radians);

        return new Vec4(x, y, 0);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ProjectionPolarEquidistant that = (ProjectionPolarEquidistant) o;

        if (pole != that.pole)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return pole;
    }
}

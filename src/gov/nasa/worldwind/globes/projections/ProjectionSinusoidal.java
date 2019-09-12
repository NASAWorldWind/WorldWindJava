/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.globes.projections;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.WWMath;

/**
 * Provides a Sinusoidal spherical projection.
 *
 * @author tag
 * @version $Id: ProjectionSinusoidal.java 2277 2014-08-28 21:19:37Z dcollins $
 */
public class ProjectionSinusoidal extends AbstractGeographicProjection
{
    public ProjectionSinusoidal()
    {
        super(Sector.FULL_SPHERE);
    }

    @Override
    public String getName()
    {
        return "Sinusoidal";
    }

    @Override
    public boolean isContinuous()
    {
        return false;
    }

    @Override
    public Vec4 geographicToCartesian(Globe globe, Angle latitude, Angle longitude, double metersElevation, Vec4 offset)
    {
        double latCos = latitude.cos();
        double x = latCos > 0 ? globe.getEquatorialRadius() * longitude.radians * latCos : 0;
        double y = globe.getEquatorialRadius() * latitude.radians;

        return new Vec4(x, y, metersElevation);
    }

    @Override
    public void geographicToCartesian(Globe globe, Sector sector, int numLat, int numLon, double[] metersElevation,
        Vec4 offset, Vec4[] out)
    {
        double eqr = globe.getEquatorialRadius();
        double minLat = sector.getMinLatitude().radians;
        double maxLat = sector.getMaxLatitude().radians;
        double minLon = sector.getMinLongitude().radians;
        double maxLon = sector.getMaxLongitude().radians;
        double deltaLat = (maxLat - minLat) / (numLat > 1 ? numLat - 1 : 1);
        double deltaLon = (maxLon - minLon) / (numLon > 1 ? numLon - 1 : 1);
        int pos = 0;

        // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian point
        // corresponding to each latitude and longitude.
        double lat = minLat;
        for (int j = 0; j < numLat; j++, lat += deltaLat)
        {
            if (j == numLat - 1) // explicitly set the last lat to the max latitude to ensure alignment
                lat = maxLat;

            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            double y = eqr * lat;
            double cosLat = Math.cos(lat);
            cosLat = cosLat > 0 ? cosLat : 0;

            double lon = minLon;
            for (int i = 0; i < numLon; i++, lon += deltaLon)
            {
                if (i == numLon - 1) // explicitly set the last lon to the max longitude to ensure alignment
                    lon = maxLon;

                double x = eqr * lon * cosLat;
                double z = metersElevation[pos];
                out[pos++] = new Vec4(x, y, z);
            }
        }
    }

    @Override
    public Position cartesianToGeographic(Globe globe, Vec4 cart, Vec4 offset)
    {
        double latRadians = cart.y / globe.getEquatorialRadius();
        latRadians = WWMath.clamp(latRadians, -Math.PI / 2, Math.PI / 2);

        double latCos = Math.cos(latRadians);
        double lonRadians = latCos > 0 ? cart.x / (globe.getEquatorialRadius() * latCos) : 0;
        lonRadians = WWMath.clamp(lonRadians, -Math.PI, Math.PI);

        return Position.fromRadians(latRadians, lonRadians, cart.z);
    }

    @Override
    public Vec4 northPointingTangent(Globe globe, Angle latitude, Angle longitude)
    {
        // Computed by taking the partial derivative of the x and y components in geographicToCartesian with
        // respect to latitude (keeping longitude a constant).

        double x = globe.getEquatorialRadius() * longitude.radians * -latitude.sin();
        double y = globe.getEquatorialRadius();

        return new Vec4(x, y, 0).normalize3();
    }
}

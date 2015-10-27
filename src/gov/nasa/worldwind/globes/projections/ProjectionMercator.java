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
 * Provides a Mercator projection of an ellipsoidal globe.
 *
 * @author tag
 * @version $Id: ProjectionMercator.java 2277 2014-08-28 21:19:37Z dcollins $
 */
public class ProjectionMercator extends AbstractGeographicProjection
{
    public ProjectionMercator()
    {
        super(Sector.fromDegrees(-78, 78, -180, 180));
    }

    @Override
    public String getName()
    {
        return "Mercator";
    }

    @Override
    public boolean isContinuous()
    {
        return true;
    }

    @Override
    public Vec4 geographicToCartesian(Globe globe, Angle latitude, Angle longitude, double metersElevation, Vec4 offset)
    {
        if (latitude.degrees > this.getProjectionLimits().getMaxLatitude().degrees)
            latitude = this.getProjectionLimits().getMaxLatitude();
        if (latitude.degrees < this.getProjectionLimits().getMinLatitude().degrees)
            latitude = this.getProjectionLimits().getMinLatitude();
        if (longitude.degrees > this.getProjectionLimits().getMaxLongitude().degrees)
            longitude = this.getProjectionLimits().getMaxLongitude();
        if (longitude.degrees < this.getProjectionLimits().getMinLongitude().degrees)
            longitude = this.getProjectionLimits().getMinLongitude();

        double xOffset = offset != null ? offset.x : 0;

        // See "Map Projections: A Working Manual", page 44 for the source of the below formulas.

        double x = globe.getEquatorialRadius() * longitude.radians + xOffset;

        double ecc = Math.sqrt(globe.getEccentricitySquared());
        double sinPhi = Math.sin(latitude.radians);
        double s = ((1 + sinPhi) / (1 - sinPhi)) * Math.pow((1 - ecc * sinPhi) / (1 + ecc * sinPhi), ecc);
        double y = 0.5 * globe.getEquatorialRadius() * Math.log(s);

        return new Vec4(x, y, metersElevation);
    }

    @Override
    public void geographicToCartesian(Globe globe, Sector sector, int numLat, int numLon, double[] metersElevation,
        Vec4 offset, Vec4[] out)
    {
        double eqr = globe.getEquatorialRadius();
        double ecc = Math.sqrt(globe.getEccentricitySquared());
        double minLat = sector.getMinLatitude().radians;
        double maxLat = sector.getMaxLatitude().radians;
        double minLon = sector.getMinLongitude().radians;
        double maxLon = sector.getMaxLongitude().radians;
        double deltaLat = (maxLat - minLat) / (numLat > 1 ? numLat - 1 : 1);
        double deltaLon = (maxLon - minLon) / (numLon > 1 ? numLon - 1 : 1);
        double minLatLimit = this.getProjectionLimits().getMinLatitude().radians;
        double maxLatLimit = this.getProjectionLimits().getMaxLatitude().radians;
        double minLonLimit = this.getProjectionLimits().getMinLongitude().radians;
        double maxLonLimit = this.getProjectionLimits().getMaxLongitude().radians;
        double offset_x = offset.x;
        int pos = 0;

        // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian point
        // corresponding to each latitude and longitude.
        double lat = minLat;
        for (int j = 0; j < numLat; j++, lat += deltaLat)
        {
            if (j == numLat - 1) // explicitly set the last lat to the max latitude to ensure alignment
                lat = maxLat;
            lat = WWMath.clamp(lat, minLatLimit, maxLatLimit); // limit lat to projection limits

            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            double sinLat = Math.sin(lat);
            double s = ((1 + sinLat) / (1 - sinLat)) * Math.pow((1 - ecc * sinLat) / (1 + ecc * sinLat), ecc);
            double y = eqr * Math.log(s) * 0.5;

            double lon = minLon;
            for (int i = 0; i < numLon; i++, lon += deltaLon)
            {
                if (i == numLon - 1) // explicitly set the last lon to the max longitude to ensure alignment
                    lon = maxLon;
                lon = WWMath.clamp(lon, minLonLimit, maxLonLimit); // limit lon to projection limits

                double x = eqr * lon + offset_x;
                double z = metersElevation[pos];
                out[pos++] = new Vec4(x, y, z);
            }
        }
    }

    @Override
    public Position cartesianToGeographic(Globe globe, Vec4 cart, Vec4 offset)
    {
        double xOffset = offset != null ? offset.x : 0;

        // See "Map Projections: A Working Manual", pages 45 and 19 for the source of the below formulas.

        double ecc2 = globe.getEccentricitySquared();
        double ecc4 = ecc2 * ecc2;
        double ecc6 = ecc4 * ecc2;
        double ecc8 = ecc6 * ecc2;
        double t = Math.pow(Math.E, -cart.y / globe.getEquatorialRadius());

        double A = Math.PI / 2 - 2 * Math.atan(t);
        double B = ecc2 / 2 + 5 * ecc4 / 24 + ecc6 / 12 + 13 * ecc8 / 360;
        double C = 7 * ecc4 / 48 + 29 * ecc6 / 240 + 811 * ecc8 / 11520;
        double D = 7 * ecc6 / 120 + 81 * ecc8 / 1120;
        double E = 4279 * ecc8 / 161280;

        double Ap = A - C + E;
        double Bp = B - 3 * D;
        double Cp = 2 * C - 8 * E;
        double Dp = 4 * D;
        double Ep = 8 * E;

        double s2p = Math.sin(2 * A);

        double lat = Ap + s2p * (Bp + s2p * (Cp + s2p * (Dp + Ep * s2p)));

        return Position.fromRadians(lat, (cart.x - xOffset) / globe.getEquatorialRadius(), cart.z);
    }

    @Override
    public Vec4 northPointingTangent(Globe globe, Angle latitude, Angle longitude)
    {
        return Vec4.UNIT_Y;
    }
}

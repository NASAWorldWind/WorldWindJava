/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.layers.mercator;

import gov.nasa.worldwind.geom.*;

/**
 * @version $Id: MercatorSector.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MercatorSector extends Sector
{
    private double minLatPercent, maxLatPercent;

    public MercatorSector(double minLatPercent, double maxLatPercent,
        Angle minLongitude, Angle maxLongitude)
    {
        super(gudermannian(minLatPercent), gudermannian(maxLatPercent),
            minLongitude, maxLongitude);
        this.minLatPercent = minLatPercent;
        this.maxLatPercent = maxLatPercent;
    }

    public static MercatorSector fromDegrees(double minLatPercent,
        double maxLatPercent, double minLongitude, double maxLongitude)
    {
        return new MercatorSector(minLatPercent, maxLatPercent, Angle
            .fromDegrees(minLongitude), Angle.fromDegrees(maxLongitude));
    }

    public static MercatorSector fromSector(Sector sector)
    {
        return new MercatorSector(gudermannianInverse(sector.getMinLatitude()),
            gudermannianInverse(sector.getMaxLatitude()), new Angle(sector
                .getMinLongitude()),
            new Angle(sector.getMaxLongitude()));
    }

    public static double gudermannianInverse(Angle latitude)
    {
        return Math.log(Math.tan(Math.PI / 4.0 + latitude.radians / 2.0))
            / Math.PI;
    }

    public static Angle gudermannian(double percent)
    {
        return Angle.fromRadians(Math.atan(Math.sinh(percent * Math.PI)));
    }

    public double getMinLatPercent()
    {
        return minLatPercent;
    }

    public double getMaxLatPercent()
    {
        return maxLatPercent;
    }
}

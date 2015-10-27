/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.globes.projections;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.Logging;

/**
 * Implements a TransverseMercator projection for a specified UTM zone.
 *
 * @author tag
 * @version $Id: ProjectionUTM.java 2097 2014-06-25 18:19:42Z tgaskins $
 */
public class ProjectionUTM extends ProjectionTransverseMercator
{
    protected static final int DEFAULT_ZONE = 1;

    protected int zone = DEFAULT_ZONE;

    /** Creates a projection for UTM zone 1. */
    public ProjectionUTM()
    {
        super(centralMeridianForZone(DEFAULT_ZONE));
    }

    /**
     * Implements a Transverse Mercator projection for a specified UTM zone.
     *
     * @param zone The UTM zone of this projection, a value between 1 and 60, inclusive.
     *
     * @throws IllegalArgumentException if the specified zone is less than 1 or greater than 60.
     */
    public ProjectionUTM(int zone)
    {
        super(centralMeridianForZone(zone));
    }

    protected double getScale()
    {
        return 0.9996;
    }

    /**
     * Indicates the UTM zone of this projection.
     *
     * @return The UTM zone, a value between 1 and 60, inclusive.
     */
    public int getZone()
    {
        return zone;
    }

    /**
     * Specifies the UTM zone for this projection.
     *
     * @param zone The UTM zone, a value between 1 and 60, inclusive.
     *
     * @throws IllegalArgumentException If the specified zone is less than 1 or greater than 60.
     * @see ProjectionTransverseMercator
     */
    public void setZone(int zone)
    {
        if (zone < 1 || zone > 60)
        {
            String message = Logging.getMessage("UTM.InvalidZone", zone);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.zone = zone;

        this.setCentralMeridian(centralMeridianForZone(this.zone));
    }

    public static Angle centralMeridianForZone(int zone)
    {
        if (zone < 1 || zone > 60)
        {
            String message = Logging.getMessage("UTM.InvalidZone", zone);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees((3 + (zone - 1) * 6) - (zone > 30 ? 360 : 0));
    }
}

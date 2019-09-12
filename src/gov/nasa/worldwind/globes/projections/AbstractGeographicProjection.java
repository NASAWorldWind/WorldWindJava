/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.globes.projections;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.GeographicProjection;
import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: AbstractGeographicProjection.java 2270 2014-08-25 20:58:41Z tgaskins $
 */
public abstract class AbstractGeographicProjection implements GeographicProjection
{
    protected Sector projectionLimits;

    public AbstractGeographicProjection(Sector projectionLimits)
    {
        if (projectionLimits == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.projectionLimits = projectionLimits;
    }

    @Override
    public Sector getProjectionLimits()
    {
        return projectionLimits;
    }

    @Override
    public void setProjectionLimits(Sector projectionLimits)
    {
        if (projectionLimits == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!projectionLimits.isWithinLatLonLimits())
        {
            String message = Logging.getMessage("generic.AngleOutOfRange", projectionLimits);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.projectionLimits = projectionLimits;
    }

}

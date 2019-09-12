/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;

import java.util.List;

/**
 * An elevation model that always returns zero elevations.
 *
 * @author tag
 * @version $Id: ZeroElevationModel.java 2014 2014-05-20 19:46:55Z tgaskins $
 */
public class ZeroElevationModel extends AbstractElevationModel
{
    public double getMaxElevation()
    {
        return 1;
    }

    public double getMinElevation()
    {
        return 0;
    }

    public double[] getExtremeElevations(Angle latitude, Angle longitude)
    {
        return new double[] {0, 1};
    }

    public double[] getExtremeElevations(Sector sector)
    {
        return new double[] {0, 1};
    }

    public double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer)
    {
        for (int i = 0; i < latlons.size(); i++)
        {
            buffer[i] = 0;
        }

        // Mark the model as used this frame.
        this.setValue(AVKey.FRAME_TIMESTAMP, System.currentTimeMillis());

        return 0;
    }

    public double getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution,
        double[] buffer)
    {
        return this.getElevations(sector, latlons, targetResolution, buffer);
    }

    public int intersects(Sector sector)
    {
        return 0;
    }

    public boolean contains(Angle latitude, Angle longitude)
    {
        return true;
    }

    @SuppressWarnings({"JavadocReference"})
    public double getBestResolution(Sector sector)
    {
        return 1.6e-6; // corresponds to about 10 meters for Earth (radius approx. 6.4e6 meters)
    }

    public double getUnmappedElevation(Angle latitude, Angle longitude)
    {
        return 0;
    }

    @Override
    public void setExtremesCachingEnabled(boolean enabled)
    {
    }

    @Override
    public boolean isExtremesCachingEnabled()
    {
        return false;
    }
}

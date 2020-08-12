/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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

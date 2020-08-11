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

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.ElevationModel;

import java.util.List;

/**
 * An elevation model to map another elevation model's depths to a constant value, usually 0. It's typically used to
 * produce flat oceans when using an elevation model containing bathymetry. This elevation model filters elevations
 * returned from the source elevation model and sets all elevations less than a specified threshold (0 by default) to
 * that threshold. All elevations returned by the source elevation model are filtered, including the source model's
 * extreme elevations.
 *
 * @author tag
 * @version $Id: BathymetryFilterElevationModel.java 2014 2014-05-20 19:46:55Z tgaskins $
 */
public class BathymetryFilterElevationModel extends AbstractElevationModel
{
    protected ElevationModel sourceModel;
    protected double threshold = 0d;

    /**
     * Constructs an elevation model that filters the elevations from a specified elevation model.
     *
     * @param source the elevation model to filter.
     */
    public BathymetryFilterElevationModel(ElevationModel source)
    {
        this.sourceModel = source;
    }

    public void dispose()
    {
        if (this.sourceModel != null)
            this.sourceModel.dispose();
    }

    /**
     * Returns the source elevation model.
     *
     * @return the source elevation model.
     */
    public ElevationModel getSourceModel()
    {
        return this.sourceModel;
    }

    /**
     * Returns the threshold value.
     *
     * @return the threshold.
     */
    public double getThreshold()
    {
        return this.threshold;
    }

    /**
     * Sets the value of the threshold. The default threshold is 0.
     *
     * @param threshold the desired threshold. Elevations less than this value are set to this value prior to being
     *                  return by any method returning one or more elevations.
     */
    public void setThreshold(double threshold)
    {
        this.threshold = threshold;
    }

    public double getMaxElevation()
    {
        return this.clampElevation(this.sourceModel.getMaxElevation());
    }

    public double getMinElevation()
    {
        return this.clampElevation(this.sourceModel.getMinElevation());
    }

    public double[] getExtremeElevations(Angle latitude, Angle longitude)
    {
        double[] elevs = this.sourceModel.getExtremeElevations(latitude, longitude);
        if (elevs == null)
            return elevs;

        return new double[] {this.clampElevation(elevs[0]), this.clampElevation(elevs[1])};
    }

    public double[] getExtremeElevations(Sector sector)
    {
        double[] elevs = this.sourceModel.getExtremeElevations(sector);
        if (elevs == null)
            return elevs;

        return new double[] {this.clampElevation(elevs[0]), this.clampElevation(elevs[1])};
    }

    public double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer)
    {
        double resolution = this.sourceModel.getElevations(sector, latlons, targetResolution, buffer);

        for (int i = 0; i < latlons.size(); i++)
        {
            LatLon ll = latlons.get(i);
            if (this.sourceModel.contains(ll.getLatitude(), ll.getLongitude()) && buffer[i] < this.threshold)
                buffer[i] = this.threshold;
        }

        return resolution;
    }

    public double getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution,
        double[] buffer)
    {
        double resolution = this.sourceModel.getElevations(sector, latlons, targetResolution, buffer);

        for (int i = 0; i < latlons.size(); i++)
        {
            LatLon ll = latlons.get(i);
            if (this.sourceModel.contains(ll.getLatitude(), ll.getLongitude())
                && buffer[i] != this.sourceModel.getMissingDataSignal() && buffer[i] < this.threshold)
                buffer[i] = this.threshold;
        }

        return resolution;
    }

    public int intersects(Sector sector)
    {
        return this.sourceModel.intersects(sector);
    }

    public boolean contains(Angle latitude, Angle longitude)
    {
        return this.sourceModel.contains(latitude, longitude);
    }

    public double getBestResolution(Sector sector)
    {
        return this.sourceModel.getBestResolution(sector);
    }

    public double getUnmappedElevation(Angle latitude, Angle longitude)
    {
        double elev = this.sourceModel.getUnmappedElevation(latitude, longitude);

        return elev != this.sourceModel.getMissingDataSignal() && elev < this.threshold ? this.threshold : elev;
    }

    /**
     * Called to clamp a source elevation to this elevation model's threshold.
     *
     * @param elevation the elevation to check and map.
     *
     * @return the input elevation if it is greater than or equal to the threshold elevation, otherwise the threshold
     *         elevation.
     */
    protected double clampElevation(double elevation)
    {
        return elevation < this.threshold ? this.threshold : elevation;
    }

    @Override
    public double getLocalDataAvailability(Sector sector, Double targetResolution)
    {
        return this.sourceModel.getLocalDataAvailability(sector, targetResolution);
    }

    @Override
    public Object getValue(String key)
    {
        Object o = super.getValue(key);

        return o != null ? o : this.sourceModel != null ? this.sourceModel.getValue(key) : null;
    }

    @Override
    public void setExtremesCachingEnabled(boolean enabled)
    {
        this.sourceModel.setExtremesCachingEnabled(enabled);
    }

    @Override
    public boolean isExtremesCachingEnabled()
    {
        return this.sourceModel.isExtremesCachingEnabled();
    }
}

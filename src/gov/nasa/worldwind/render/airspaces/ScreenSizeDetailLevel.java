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
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: ScreenSizeDetailLevel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScreenSizeDetailLevel extends AVListImpl implements DetailLevel
{
    private static final double DEFAULT_MIN_SIZE = 40.0;
    private static final double DEFAULT_MAX_SIZE = 700.0;

    private final double screenSize;
    private final String key;

    public ScreenSizeDetailLevel(double minimumScreenSize, String key)
    {
        this.screenSize = minimumScreenSize;
        this.key = key;
    }

    public double getScreenSize()
    {
        return this.screenSize;
    }

    public String getKey()
    {
        return this.key;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        ScreenSizeDetailLevel that = (ScreenSizeDetailLevel) o;
        return Double.compare(this.screenSize, that.screenSize) == 0;
    }

    public int compareTo(DetailLevel level)
    {
        if (this == level)
            return 0;
        if (level == null || this.getClass() != level.getClass())
            return -1;

        ScreenSizeDetailLevel that = (ScreenSizeDetailLevel) level;
        return Double.compare(this.screenSize, that.screenSize);
    }

    public int hashCode()
    {
        long temp = this.screenSize != +0.0d ? Double.doubleToLongBits(this.screenSize) : 0L;
        return (int) (temp ^ (temp >>> 32));
    }

    public String toString()
    {
        return this.key;
    }

    public boolean meetsCriteria(DrawContext dc, Airspace airspace)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc.getView() == null)
        {
            String message = "nullValue.DrawingContextViewIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Extent extent = airspace.getExtent(dc);
        if (extent == null)
            return false;

        double d = dc.getView().getEyePoint().distanceTo3(extent.getCenter());
        double pixelSize = dc.getView().computePixelSizeAtDistance(d);
        double shapeScreenSize = extent.getDiameter() / pixelSize;
        return shapeScreenSize < this.screenSize;
    }

    public static double[] computeDefaultScreenSizeRamp(int levels)
    {
        return computeLinearScreenSizeRamp(levels, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE);
    }

    public static double[] computeLinearScreenSizeRamp(int levels, double min, double max)
    {
        double[] ramp = new double[levels];
        double a;

        for (int i = 0; i < levels; i++)
        {
            a = (double) i / (double) (levels - 1);
            ramp[levels - i - 1] = min + a * (max - min);
        }

        return ramp;
    }
}

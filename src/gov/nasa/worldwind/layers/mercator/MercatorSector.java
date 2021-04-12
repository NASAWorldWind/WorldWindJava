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

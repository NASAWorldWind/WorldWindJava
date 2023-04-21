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

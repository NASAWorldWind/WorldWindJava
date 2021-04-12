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
package gov.nasa.worldwind.animation;

import gov.nasa.worldwind.util.Logging;

import java.util.Date;

/**
 * @author jym
 * @version $Id: ScheduledInterpolator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScheduledInterpolator implements Interpolator
{
    private long startTime = -1;
    private final long length;

    public ScheduledInterpolator(long lengthMillis)
    {
        this(null, lengthMillis);
    }

    public ScheduledInterpolator(Date startTime, long lengthMillis)
    {
        if (lengthMillis < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", lengthMillis);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (startTime != null)
            this.startTime = startTime.getTime();
        this.length = lengthMillis;
    }

    public ScheduledInterpolator(Date startTime, Date stopTime)
    {
        if (startTime == null || stopTime == null)
        {
            String message = Logging.getMessage("nullValue.DateIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (startTime.after(stopTime))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", startTime);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.startTime = startTime.getTime();
        this.length = stopTime.getTime() - startTime.getTime();
    }

    public double nextInterpolant()
    {


        long currentTime = System.currentTimeMillis();
        // When no start time is specified, begin counting time on the first run.
        if (this.startTime < 0)
            this.startTime = currentTime;
        // Exit when current time is before starting time.
        if (currentTime < this.startTime)
            return 0;

        long elapsedTime = currentTime - this.startTime;
        double unclampedInterpolant = ((double) elapsedTime) / ((double) this.length);
        return AnimationSupport.clampDouble(unclampedInterpolant, 0, 1);
    }

}

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
package gov.nasa.worldwind.layers.rpf.wizard;

/**
 * @author dcollins
 * @version $Id: TimeFormatter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TimeFormatter
{
    private static final long ONE_HOUR   = 60L * 60L * 1000L;
    private static final long ONE_MINUTE = 60L * 1000L;
    private static final long ONE_SECOND = 1000L;

    public TimeFormatter()
    {}

    public String formatPrecise(long millis)
    {
        long[] hms = millisToHMS(millis);
        return String.format("%02d:%02d:%02d", hms[0], hms[1], hms[2]);
    }

    public String formatEstimate(long millis)
    {
        String result;

        // Less than a minute.
        if (millis < ONE_MINUTE)
        {
            result = "less than 1 minute";
        }
        // Report time in one-minute increments.
        else if (millis < 10L* ONE_MINUTE)
        {
            millis = ONE_MINUTE * Math.round(millis / (double) ONE_MINUTE);
            long m = millis / ONE_MINUTE;
            result = "about " + m + (m > 1 ? " minutes" : " minute");
        }
        // Report time in ten-minute increments.
        else if (millis < 55L * ONE_MINUTE)
        {
            millis = 10L * ONE_MINUTE * Math.round(millis / (10d * ONE_MINUTE));
            long m = millis / ONE_MINUTE;
            result = "about " + m + " minutes";
        }
        // Report time in half-hour increments.
        else
        {
            millis = 30L * ONE_MINUTE * Math.round(millis / (30d * ONE_MINUTE));
            long h = millis / ONE_HOUR;
            result = "about " + h + (h > 1 ? " hours" : " hour");
            long m = (millis / ONE_MINUTE) % 60L;
            if (m > 0)
                result += " " + m + " minutes";
        }

        return result;
    }

    private static long[] millisToHMS(long millis)
    {
        return new long[] {
            (long) (Math.floor(millis / (double) ONE_HOUR)   % 60d),  // hours
            (long) (Math.floor(millis / (double) ONE_MINUTE) % 60d),  // minutes
            (long) (Math.floor(millis / (double) ONE_SECOND) % 60d)}; // seconds

    }
}

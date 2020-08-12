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

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.render.DrawContext;

import java.util.Random;

/**
 * Handles expiration after some interval of time has passed. Expiration time is computed as a random value between a
 * specified minimum and a specified maximum delay interval.
 *
 * @version $Id: TimedExpirySupport.java 2065 2014-06-20 16:58:48Z dcollins $
 */
public class TimedExpirySupport
{
    protected boolean expired = true;
    protected long expiryTime = -1L;
    protected long minExpiryTime;
    protected long maxExpiryTime;
    protected static Random rand = new Random();

    /** Constructs an instance with minimum expiry interval of 1 second and a max of 2 seconds. */
    public TimedExpirySupport()
    {
        this.minExpiryTime = 2000;
        this.maxExpiryTime = 3000;
    }

    /**
     * Constructs an instance with specified minimum and maximum expiry intervals. An interval is set to 0 if its
     * specified value is less than 0.
     *
     * @param minExpiryTime the minimum interval allowed to pass before expiration, in milliseconds.
     * @param maxExpiryTime the maximum interval allowed to pass before expiration, in milliseconds.
     */
    public TimedExpirySupport(long minExpiryTime, long maxExpiryTime)
    {
        this.minExpiryTime = Math.max(minExpiryTime, 0);
        this.maxExpiryTime = Math.max(maxExpiryTime, 0);
    }

    /**
     * Set the expiration state of this timer.
     *
     * @param expired true to indicate expired, false to indicate not expired.
     */
    public void setExpired(boolean expired)
    {
        this.expired = expired;
    }

    /**
     * Indicates the current expiration time, which is a random value between the specified minimum and maximum.
     *
     * @return the current expiration time, in milliseconds.
     */
    public long getExpiryTime()
    {
        return this.expiryTime;
    }

    /**
     * Sets the current expiration time to a specified value. This method ignores the configured minimum and maximum
     * expiry times.
     *
     * @param expiryTime the new expiration time, in milliseconds.
     */
    public void setExpiryTime(long expiryTime)
    {
        this.expiryTime = expiryTime;
    }

    /**
     * Specifies the minimum and maximum expiration intervals. An interval is set to 0 if its specified value is less
     * than 0.
     *
     * @param minExpiryTime the minimum interval allowed to pass before expiration, in milliseconds.
     * @param maxExpiryTime the maximum interval allowed to pass before expiration, in milliseconds.
     */
    public void setExpiryTime(long minExpiryTime, long maxExpiryTime)
    {
        this.minExpiryTime = Math.max(minExpiryTime, 0);
        this.maxExpiryTime = Math.max(maxExpiryTime, 0);
    }

    /**
     * Indicates this timer's minimum expiry interval.
     *
     * @return this timer's minimum expiry interval, in milliseconds.
     */
    public long getMinExpiryTime()
    {
        return this.minExpiryTime;
    }

    /**
     * Indicates this timer's maximum expiry interval.
     *
     * @return this timer's maximum expiry interval, in milliseconds.
     */
    public long getMaxExpiryTime()
    {
        return this.maxExpiryTime;
    }

    /**
     * Indicates whether this timer has expired.
     *
     * @param dc the current draw context.
     *
     * @return true if this timer has expired relative to the frame time, otherwise false.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public boolean isExpired(DrawContext dc)
    {
        if (this.expired)
            return true;

        long now = dc != null ? dc.getFrameTimeStamp() : System.currentTimeMillis();
        if (now >= this.expiryTime)
            return true;

        return false;
    }

    /**
     * Indicates whether this timer has expired.
     *
     * @param now the time to relate this timer's expiration time to. The timer is considered expired if this timer's
     *            expiry time is less than this value.
     *
     * @return true if this timer has expired relative to system time, otherwise false.
     */
    public boolean isExpired(long now)
    {
        return this.expired || this.expiryTime < now;
    }

    /**
     * Marks this timer as not expired and restarts the timer.
     *
     * @param dc the current draw context.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public void restart(DrawContext dc)
    {
        if (this.maxExpiryTime == 0 || this.maxExpiryTime < this.minExpiryTime)
        {
            this.expired = true;
        }
        else
        {
            long now = dc != null ? dc.getFrameTimeStamp() : System.currentTimeMillis();
            this.expiryTime = now + this.minExpiryTime
                + rand.nextInt((int) (this.maxExpiryTime - this.minExpiryTime));
            this.expired = false;
        }
    }
}

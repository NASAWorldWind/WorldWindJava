/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.animation;

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
            throw new IllegalArgumentException();
        }

        if (startTime != null)
            this.startTime = startTime.getTime();
        this.length = lengthMillis;
    }

    public ScheduledInterpolator(Date startTime, Date stopTime)
    {
        if (startTime == null || stopTime == null)
        {
            throw new IllegalArgumentException();
        }

        if (startTime.after(stopTime))
        {
            throw new IllegalArgumentException();
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

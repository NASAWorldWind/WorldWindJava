/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.cache.BasicSessionCache;

/**
 * Maintains a list of missing resources. Once added, a resource is considered absent until a specified time interval
 * elapses. If marked absent a specified number of times, the resource is considered permanently absent until a second
 * time interval expires -- the try-again interval -- at which time the resource is considered not absent, indicating
 * that the caller may attempt to find the resource again. If the resource is subsequently marked absent, the cycle
 * repeats.
 *
 * @author tag
 * @version $Id: AbsentResourceList.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AbsentResourceList
{
    // Absent resources: A resource is deemed absent if a specified maximum number of attempts have been made to retrieve it.
    // Retrieval attempts are governed by a minimum time interval between successive attempts. If an attempt is made
    // within this interval, the resource is still deemed to be absent until the interval expires.

    /** The default number of times a resource is marked as absent before being marked as permanently absent. */
    protected static final int DEFAULT_MAX_ABSENT_RESOURCE_TRIES = 3;
    /** The default interval to wait before indicating the resource is not absent. */
    protected static final int DEFAULT_MIN_ABSENT_RESOURCE_CHECK_INTERVAL = 10000;
    /** The default interval at which a resources is marked as not absent after having been marked permanently absent. */
    protected static final int DEFAULT_TRY_AGAIN_INTERVAL = (int) 60e3; // seconds

    /** The maximum number of times a resource is marked as absent before being marked as permanently absent. */
    protected int maxTries = DEFAULT_MAX_ABSENT_RESOURCE_TRIES;
    /** The interval to wait, in milliseconds, before indicating the resource is not absent. */
    protected int minCheckInterval = DEFAULT_MIN_ABSENT_RESOURCE_CHECK_INTERVAL;
    /** The interval at which a resource is marked as not absent after having been marked as permanently absent. */
    protected int tryAgainInterval = DEFAULT_TRY_AGAIN_INTERVAL;

    /** Internal class that maintains a resource's state. */
    protected static class AbsentResourceEntry
    {
        /** The time the resource was last marked as absent by a call to {@link AbsentResourceList#markResourceAbsent(String)}. */
        long timeOfLastMark; // meant to be the time of the most recent attempt to find the resource
        /**
         * The maximum number of times the resource is marked as absent beyond which the resource is considered
         * permanently absent.
         */
        int numTries;
    }

    /** The map of absent resources. */
    protected BasicSessionCache possiblyAbsent = new BasicSessionCache(1000);

    /**
     * Construct an absent-resource list with default values for max tries (3), check interval (10 seconds) and
     * try-again interval (60 seconds).
     */
    public AbsentResourceList()
    {
    }

    /**
     * Construct an absent-resource list with a specified number of maximum tries and a check interval.
     *
     * @param maxTries         the number of max tries. Must be greater than 0.
     * @param minCheckInterval the check interval. Must be greater than or equal to 0.
     *
     * @throws IllegalArgumentException if max-tries is less than 1 or the minimum check interval is less than 0.
     */
    public AbsentResourceList(int maxTries, int minCheckInterval)
    {
        if (maxTries < 1)
        {
            String message = Logging.getMessage("AbsentResourceList.MaxTriesLessThanOne");
            throw new IllegalArgumentException(message);
        }

        if (minCheckInterval < 0)
        {
            String message = Logging.getMessage("AbsentResourceList.CheckIntervalLessThanZero");
            throw new IllegalArgumentException(message);
        }

        this.maxTries = maxTries;
        this.minCheckInterval = minCheckInterval;
    }

    /**
     * Construct an absent-resource list with a specified number of maximum tries, a check interval and a retry
     * interval.
     *
     * @param cacheSize        the maximum number of absent resources the list may hold. If this limit is exceeded, the
     *                         oldest entries in the list are ejected and the resources they refer to are subsequently
     *                         not considered to be absent resources.
     * @param maxTries         the number of max tries. Must be greater than 0.
     * @param minCheckInterval the check interval. Must be greater than or equal to 0.
     * @param tryAgainInterval the try-again interval.  Must be greater than or equal to 0.
     *
     * @throws IllegalArgumentException if max-tries is less than 1 or if either the minimum check interval or try-again
     *                                  interval is less than 0.
     */
    public AbsentResourceList(Integer cacheSize, int maxTries, int minCheckInterval, int tryAgainInterval)
    {
        if (maxTries < 1)
        {
            String message = Logging.getMessage("AbsentResourceList.MaxTriesLessThanOne");
            throw new IllegalArgumentException(message);
        }

        if (minCheckInterval < 0)
        {
            String message = Logging.getMessage("AbsentResourceList.CheckIntervalLessThanZero");
            throw new IllegalArgumentException(message);
        }

        if (tryAgainInterval < 0)
        {
            String message = Logging.getMessage("AbsentResourceList.RetryIntervalLessThanZero");
            throw new IllegalArgumentException(message);
        }

        if (cacheSize != null && cacheSize < 1)
        {
            String message = Logging.getMessage("AbsentResourceList.MaximumListSizeLessThanOne");
            throw new IllegalArgumentException(message);
        }

        if (cacheSize != null)
            this.possiblyAbsent.setCapacity(cacheSize);

        this.maxTries = Math.max(maxTries, 1);
        this.minCheckInterval = minCheckInterval;
        this.tryAgainInterval = tryAgainInterval;
    }

    /**
     * Indicates the maximum number of times a resource is marked as absent before being marked as permanently absent.
     *
     * @return the maximum number of absent markings.
     */
    public int getMaxTries()
    {
        return maxTries;
    }

    /**
     * Specifies the maximum number of times a resource is marked as absent before being marked as permanently absent.
     *
     * @param maxTries the number of max tries. Must be greater than 0.
     *
     * @throws IllegalArgumentException if max-tries is less than 1.
     */
    public void setMaxTries(int maxTries)
    {
        if (maxTries < 1)
        {
            String message = Logging.getMessage("AbsentResourceList.MaxTriesLessThanOne");
            throw new IllegalArgumentException(message);
        }

        this.maxTries = maxTries;
    }

    /**
     * Indicates the time interval that must elapse before the resource is considered not absent if its max-tries has
     * not been reached.
     *
     * @return the interval, in milliseconds.
     */
    public int getMinCheckInterval()
    {
        return minCheckInterval;
    }

    /**
     * Specifies the time interval that must elapse before the resource is considered absent if its max-tries has not
     * been reached.
     *
     * @param minCheckInterval the check interval. Must be greater than or equal to 0.
     *
     * @throws IllegalArgumentException if the minimum check interval is less than 0.
     */
    public void setMinCheckInterval(int minCheckInterval)
    {
        if (minCheckInterval < 0)
        {
            String message = Logging.getMessage("AbsentResourceList.CheckIntervalLessThanZero");
            throw new IllegalArgumentException(message);
        }

        this.minCheckInterval = minCheckInterval;
    }

    /**
     * Indicates the time interval that must elapse before a resource marked as permanently absent is again considered
     * not absent. This effectively expires the absent state of the resource.
     *
     * @return the interval, in milliseconds.
     */
    public int getTryAgainInterval()
    {
        return tryAgainInterval;
    }

    /**
     * Specifies the time interval that must elapse before a resource marked as permanently absent is again considered
     * not absent. This effectively expires the absent state of the resource.
     *
     * @param tryAgainInterval the try-again interval.  Must be greater than or equal to 0.
     *
     * @throws IllegalArgumentException if the try-again interval is less than 0.
     */
    public void setTryAgainInterval(int tryAgainInterval)
    {
        if (tryAgainInterval < 0)
        {
            String message = Logging.getMessage("AbsentResourceList.RetryIntervalLessThanZero");
            throw new IllegalArgumentException(message);
        }

        this.tryAgainInterval = tryAgainInterval;
    }

    /**
     * Mark a specified resource as absent. If the resource is already marked as absent, its max-tries value is
     * incremented.
     *
     * @param resourceID the resource to mark as absent.
     */
    public final void markResourceAbsent(long resourceID)
    {
        this.markResourceAbsent(Long.toString(resourceID));
    }

    /**
     * Indicates whether a resource is considered absent.
     *
     * @param resourceID the resource in question.
     *
     * @return true if the resource is considered absent, otherwise false.
     */
    public final boolean isResourceAbsent(long resourceID)
    {
        return this.isResourceAbsent(Long.toString(resourceID));
    }

    /**
     * Mark the resource as not-absent, effectively removing it from this absent-resource list.
     *
     * @param resourceID the resource to mark as not absent.
     */
    public final void unmarkResourceAbsent(long resourceID)
    {
        this.unmarkResourceAbsent(Long.toString(resourceID));
    }

    /**
     * Mark a specified resource as absent. If the resource is already marked as absent, its max-tries value is
     * incremented.
     *
     * @param resourceID the resource to mark as absent.
     */
    synchronized public final void markResourceAbsent(String resourceID)
    {
        AbsentResourceEntry entry = (AbsentResourceEntry) this.possiblyAbsent.get(resourceID);
        if (entry == null)
            this.possiblyAbsent.put(resourceID, entry = new AbsentResourceEntry());

        ++entry.numTries;
        entry.timeOfLastMark = System.currentTimeMillis();
    }

    /**
     * Indicates whether a resource is considered absent.
     *
     * @param resourceID the resource in question.
     *
     * @return true if the resource is considered absent, otherwise false.
     */
    synchronized public final boolean isResourceAbsent(String resourceID)
    {
        AbsentResourceEntry entry = (AbsentResourceEntry) this.possiblyAbsent.get(resourceID);
        if (entry == null)
            return false;

        long timeSinceLastMark = System.currentTimeMillis() - entry.timeOfLastMark;

        if (timeSinceLastMark > this.tryAgainInterval)
        {
            this.possiblyAbsent.remove(resourceID);
            return false;
        }

        return timeSinceLastMark < this.minCheckInterval || entry.numTries > this.maxTries;
    }

    /**
     * Remove a resource from this absent-resource list if the resource is contained in the list.
     *
     * @param resourceID the resource to remove from this list.
     */
    synchronized public final void unmarkResourceAbsent(String resourceID)
    {
        this.possiblyAbsent.remove(resourceID);
    }
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.util.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tag
 * @version $Id: BasicMemoryCacheSet.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicMemoryCacheSet implements MemoryCacheSet
{
    private ConcurrentHashMap<String, MemoryCache> caches = new ConcurrentHashMap<String, MemoryCache>();

    public synchronized boolean containsCache(String key)
    {
        return this.caches.containsKey(key);
    }

    public synchronized MemoryCache getCache(String cacheKey)
    {
        MemoryCache cache = this.caches.get(cacheKey);

        if (cache == null)
        {
            String message = Logging.getMessage("MemoryCacheSet.CacheDoesNotExist",  cacheKey);
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return cache;
    }

    public Map<String, MemoryCache> getAllCaches()
    {
        return this.caches;
    }

    public synchronized MemoryCache addCache(String key, MemoryCache cache)
    {
        if (this.containsCache(key))
        {
            String message = Logging.getMessage("MemoryCacheSet.CacheAlreadyExists");
            Logging.logger().fine(message);
            throw new IllegalStateException(message);
        }

        if (cache == null)
        {
            String message = Logging.getMessage("nullValue.CacheIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.caches.put(key, cache);

        return cache;
    }

    public synchronized void clear()
    {
        for (MemoryCache cache : this.caches.values())
        {
            cache.clear();
        }
    }

    public Collection<PerformanceStatistic> getPerformanceStatistics()
    {
        ArrayList<PerformanceStatistic> stats = new ArrayList<PerformanceStatistic>();

        for (MemoryCache cache : this.caches.values())
        {
            stats.add(new PerformanceStatistic(PerformanceStatistic.MEMORY_CACHE, "Cache Size (Kb): " + cache.getName(),
                cache.getUsedCapacity() / 1000));
        }

        return stats;
    }
}

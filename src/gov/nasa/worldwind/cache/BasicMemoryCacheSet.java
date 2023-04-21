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

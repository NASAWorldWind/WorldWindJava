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

/**
 * BasicSessionCache is a general receiving area for data represented as key-value pairs. Entries in a BasicSessionCache
 * may persist for the length of a Virtual Machine's run time, but may be evicted if the cache size increases beyond its
 * capacity.
 * <p>
 * Eviction of BasicSessionCache entries is accomplished by controlling the maximum number of entries in the cache. This
 * maximum value is set by calling {@link #setCapacity(int)}. The eldest entry in the cache (the first entry added) is
 * always evicted before any others.
 * <p>
 * BasicSessionClass is a thread safe class. Access to the cache data structures is synchronized at the method level.
 * Care must be taken by subclasses to ensure that method level synchronization is maintained.
 *
 * @author dcollins
 * @version $Id: BasicSessionCache.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicSessionCache implements SessionCache
{
    protected static final int DEFAULT_CAPACITY = 8;

    protected BoundedHashMap<Object, Object> entries;

    /**
     * Creates a BasicSessionCache with a specified maximum number of entries.
     *
     * @param capacity maximum number of entries in the cache.
     *
     * @throws IllegalArgumentException if capacity is negative.
     */
    public BasicSessionCache(int capacity)
    {
        if (capacity < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "capacity < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.entries = new BoundedHashMap<Object, Object>(capacity);
    }

    /** Creates a BasicSessionCache with the default capacity. */
    public BasicSessionCache()
    {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Returns the maximum number of entries in the cache.
     *
     * @return maximum number of entries in the cache.
     */
    public synchronized int getCapacity()
    {
        return this.entries.getCapacity();
    }

    /**
     * Sets the maximum number of entries in the cache. If the new capacity is less than the number of cache entries,
     * this evicts the eldest entry until the cache size is equal to its capacity.
     *
     * @param capacity maximum number of entries in the cache.
     *
     * @throws IllegalArgumentException if capacity is negative.
     */
    public synchronized void setCapacity(int capacity)
    {
        if (capacity < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "capacity < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.entries.setCapacity(capacity);
    }

    /**
     * Returns the number of entries currently in the cache.
     *
     * @return number of cached entries.
     */
    public synchronized int getEntryCount()
    {
        return this.entries.size();
    }

    /**
     * Returns a {@link java.util.Set} view of the keys contained in the cache. The returned set is immutable: changes
     * to the set are not reflected in the session cache.
     *
     * @return a {@link java.util.Set} view of the keys contained in the cache.
     */
    public synchronized java.util.Set<Object> getKeySet()
    {
        return java.util.Collections.unmodifiableSet(this.entries.keySet());
    }

    /**
     * Returns true if the cache contains a specified key, and false if it does not.
     *
     * @param key the entry key in question. A null value is not permitted.
     *
     * @return true if the cache contains the key; false otherwise.
     *
     * @throws IllegalArgumentException if the key is null.
     */
    public synchronized boolean contains(Object key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.entries.containsKey(key);
    }

    /**
     * Returns a reference to an entry's value in the cache corresponding to a specified key, or null if no entry with
     * that key exists.
     *
     * @param key the entry key to look for.
     *
     * @return a reference to the found entry's value.
     *
     * @throws IllegalArgumentException if the key is null.
     */
    public synchronized Object get(Object key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.entries.get(key);
    }

    /**
     * Adds an entry in the cache with a specified key and value. If the cache size after adding the new entry is
     * greater than its capacity, this evicts the eldest entry in the cache.
     *
     * @param key   the entry's key. A null value is not permitted.
     * @param value the entry's value. A null value is permitted.
     *
     * @throws IllegalArgumentException if the key is null.
     */
    public synchronized void put(Object key, Object value)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.entries.put(key, value);
    }

    /**
     * Removes the entry with the specified key from the cache, and returns that entry's value. If no entry exists for
     * the specified key, this does nothing and returns null.
     *
     * @param key the entry key to look for.
     *
     * @return a reference to the removed entry's value, or null of no entry matches the specified key.
     */
    public synchronized Object remove(Object key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.entries.remove(key);
    }

    /** Removes all entries from the cache. */
    public synchronized void clear()
    {
        this.entries.clear();
    }
}

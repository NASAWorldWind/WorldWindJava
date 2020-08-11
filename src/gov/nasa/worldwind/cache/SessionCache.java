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

/**
 * SessionCache is a general receiving area for data represented as key-value pairs. Entries in a SessionCache may
 * persist for the length of a Virtual Machine's run time, but may be evicted at any time.
 * <p>
 * Eviction of SessionCache entries is accomplished by controlling the maximum number of entries in the cache. This
 * maximum value is set by calling {@link #setCapacity(int)}. A SessionCache may be implemented with any eviction policy
 * (including a policy of no eviction). Most implementations evict the eldest entry added to the cache.
 *
 * @author dcollins
 * @version $Id: SessionCache.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface SessionCache
{
    /**
     * Returns the maximum number of entries in the cache.
     *
     * @return maximum number of entries in the cache.
     */
    int getCapacity();

    /**
     * Sets the maximum number of entries in the cache.
     *
     * @param capacity maximum number of enties in the cache.
     */
    void setCapacity(int capacity);

    /**
     * Returns the number of entries currently in the cache.
     *
     * @return number of cached entries.
     */
    int getEntryCount();

    /**
     * Returns a {@link java.util.Set} view of the keys contained in the cache.
     *
     * @return a {@link java.util.Set} view of the keys contained in the cache.
     */
    java.util.Set<Object> getKeySet();

    /**
     * Returns true if the cache contains a specified key, and false if it does not.
     *
     * @param key the entry key in question.
     *
     * @return true if the cache contains the key; false otherwise.
     */
    boolean contains(Object key);

    /**
     * Returns a reference to an entry's value in the cache corresponding to a specified key, or null if no entry with
     * that key exists.
     *
     * @param key the entry key to look for.
     *
     * @return a reference to the found entry's value.
     */
    Object get(Object key);

    /**
     * Adds an entry in the cache with a specified key and value.
     *
     * @param key   the entry's key.
     * @param value the entry's value.
     */
    void put(Object key, Object value);

    /**
     * Removes the entry with the specified key from the cache, and returns that entry's value. If no entry exists for
     * the specified key, this does nothing and returns null.
     *
     * @param key the entry key to look for.
     *
     * @return a reference to the removed entry's value, or null of no entry matches the specified key.
     */
    Object remove(Object key);

    /**
     * Removes all entries from the cache.
     */
    void clear();
}

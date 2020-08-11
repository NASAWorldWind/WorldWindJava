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

/**
 * BoundedHashMap is a map with a fixed capacity. When the map's size exceeds its capacity, it automatically removes
 * elements until its size is equal to its capacity. <p> BoundedHashMap can operate in two ordering modes: insertion
 * order and access order. The mode specified which entries are automatically removed when the map is over capacity. In
 * insertion order mode the map removes the eldest entry (the first entry added). In access order mode, the map
 * automatically removes the least recently used entry.
 *
 * @author dcollins
 * @version $Id: BoundedHashMap.java 1171 2013-02-11 21:45:02Z dcollins $
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public class BoundedHashMap<K, V> extends java.util.LinkedHashMap<K, V>
{
    protected static final int DEFAULT_CAPACITY = 16;
    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private int capacity;

    /**
     * Creates a BoundedHashMap with a specified maximum capacity and ordering mode.
     *
     * @param capacity    the maximum number of entries in the map.
     * @param accessOrder the ordering mode: true specifies access order, false specifies insertion order.
     */
    public BoundedHashMap(int capacity, boolean accessOrder)
    {
        super(getInitialCapacity(capacity, DEFAULT_LOAD_FACTOR), DEFAULT_LOAD_FACTOR, accessOrder);
        this.capacity = capacity;
    }

    /**
     * Creates a BoundedHashMap with a specified maximum capacity, in insertion order mode.
     *
     * @param capacity the maximum number of entries in the map.
     */
    public BoundedHashMap(int capacity)
    {
        this(capacity, false);
    }

    /** Creates a BoundedHashMap with a capacity of 16, in insertion order mode. */
    public BoundedHashMap()
    {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Returns the maximum number of entries in the map.
     *
     * @return maximum number of entries in the map.
     */
    public int getCapacity()
    {
        return this.capacity;
    }

    /**
     * Sets the maximum number of entries in the map. If the new capacity is less than the map's current size, this
     * automatically removes entries until the map's size is equal to its capacity.
     *
     * @param capacity maximum number of entries in the map.
     */
    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
        this.removeOverCapacityEntries();
    }

    protected static int getInitialCapacity(int capacity, float loadFactor)
    {
        return WWMath.powerOfTwoCeiling((int) Math.ceil(capacity / loadFactor));
    }

    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest)
    {
        return this.size() > this.getCapacity();
    }

    /**
     * Removes the first n entries in the map, where n is the number of entries in the map beyond its capacity. Note
     * that the entry set and the corresponding iterator are backed by the map itself, so changes to an entry iterator
     * correspond to changes in the map. We use the iterator's remove() method because we're removing elements from the
     * entry set as we iterate over them.
     */
    protected void removeOverCapacityEntries()
    {
        int count = this.size() - this.getCapacity();
        if (count <= 0)
            return;

        java.util.Iterator<java.util.Map.Entry<K, V>> iter = this.entrySet().iterator();
        for (int i = 0; i < count && iter.hasNext(); i++)
        {
            iter.next();
            iter.remove();
        }
    }
}

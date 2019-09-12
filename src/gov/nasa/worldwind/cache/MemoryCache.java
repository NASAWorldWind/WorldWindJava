/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.cache;

/**
 * @author Eric Dalgliesh
 * @version $Id: MemoryCache.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface MemoryCache
{
    void setName(String name);

    String getName();

    /**
     * Provides the interface for cache clients to be notified of key events. Currently the only key event is the
     * removal of an entry from the cache. A client may need to know a removal instigated by the cache occurred in order
     * to adjust its own state or to free resources associated with the removed entry.
     */
    public interface CacheListener
    {
        /**
         * Called just after an entry has been removed from the cache. Listeners should deallocate any resources that
         * won't be deallocated by normal garbage collection.
         *
         * @param key          the entry's cache key.
         * @param clientObject the cached object.
         */
        public void entryRemoved(Object key, Object clientObject);

        /**
         * Called when an exception occurs within the {@link #entryRemoved(Object, Object)} call.
         *
         * @param exception    the exception that occurred.
         * @param key          the entry's cache key.
         * @param clientObject the cached object.
         */
        public void removalException(Throwable exception, Object key, Object clientObject);
    }

    /**
     * Adds a new <code>cacheListener</code>, which will be sent notification whenever an entry is removed from the
     * cache.
     *
     * @param listener the new <code>MemoryCache.CacheListener</code>
     */
    void addCacheListener(CacheListener listener);

    /**
     * Removes a <code>CacheListener</code>, notifications of events will no longer be sent to this listener.
     *
     * @param listener the cache listener to remove.
     */
    void removeCacheListener(CacheListener listener);

    /**
     * Discovers whether or not this cache contains the object referenced by <code> key </code>. Currently no interface exists
     * to discover if an object resides in the cache by referencing itself.
     *
     * @param key the key which the object is referenced by.
     *
     * @return true if the key is found in the cache, false otherwise.
     */
    boolean contains(Object key);

    /**
     * Attempts to add the object <code>clientObject</code>, with size <code>objectSize</code> and referred to by
     * <code>key</code> to the cache. <code>objectSize</code> is the size in cache units, but is not checked for
     * accuracy. Returns whether or not the add was successful.
     * <p>
     * Note that the size passed in may be used, rather than the real size of the object. In some implementations, the
     * accuracy of the space used calls will depend on the collection of these sizes, rather than actual size.
     * <p>
     * This method should be declared <code>synchronized</code> when it is implemented.
     *
     * @param key          an object used to reference the cached item.
     * @param clientObject the item to be cached.
     * @param objectSize   the size of the item in cache units.
     *
     * @return true if object was added, false otherwise.
     */
    boolean add(Object key, Object clientObject, long objectSize);

    /**
     * Attempts to add the <code>Cacheable</code> object referenced by the key. No explicit size value is required as
     * this method queries the Cacheable to discover the size.
     * <p>
     * This method should be declared <code>synchronized</code> when it is implemented.
     *
     * @param key          an object used to reference the cached item.
     * @param clientObject the item to be cached.
     *
     * @return true if object was added, false otherwise.
     *
     * @see Cacheable
     */
    boolean add(Object key, Cacheable clientObject);

    /**
     * Remove an object from the MemoryCache referenced by <code>key</code>. If the object is already absent, this
     * method simply returns without indicating the absence.
     *
     * @param key an <code>Object</code> used to represent the item to remove.
     */
    void remove(Object key);

    /**
     * Retrieves the requested item from the cache. If <code>key</code> is null or the item is not found, this method
     * returns null.
     *
     * @param key an <code>Object</code> used to represent the item to retrieve.
     *
     * @return the requested <code>Object</code> if found, null otherwise.
     */
    Object getObject(Object key);

    /**
     * Empties the cache. After calling <code>clear()</code> on a <code>MemoryCache</code>, calls relating to used
     * capacity and number of items should return zero and the free capacity should be the maximum capacity.
     * <p>
     * This method should be declared <code>synchronized</code> when it is implemented and should notify all
     * <code>CacheListener</code>s of entries removed.
     */
    void clear();

    /* *************************************************************************/
    // capacity related accessors

    /**
     * Retrieve the number of items stored in the <code>MemoryCache</code>.
     *
     * @return the number of items in the cache.
     */
    int getNumObjects();

    /**
     * Retrieves the maximum size of the cache.
     *
     * @return the maximum size of the <code>MemoryCache</code>.
     */
    long getCapacity();

    /**
     * Retrieves the amount of used <code>MemoryCache</code> space. The value returned is in cache units.
     *
     * @return the long value of the number of cache units used by cached items.
     */
    long getUsedCapacity();

    /**
     * Retrieves the available space for storing new items.
     *
     * @return the long value of the remaining space for storing cached items.
     */
    long getFreeCapacity();

    /**
     * Retrieves the low water value of the <code>MemoryCache</code>. When a <code>MemoryCache</code> runs out of free
     * space, it must remove some items if it wishes to add any more. It continues removing items until the low water
     * level is reached. Not every <code>MemoryCache</code> necessarily uses the low water system, so this may not
     * return a useful value.
     *
     * @return the low water value of the <code>MemoryCache</code>.
     */
    long getLowWater();

    /* *******************************************************************************/
    //capacity related mutators

    /**
     * Sets the new low water capacity value for this <code>MemoryCache</code>. When a <code>MemoryCache</code> runs out
     * of free space, it must remove some items if it wishes to add any more. It continues removing items until the low
     * water level is reached. Not every <code>MemoryCache</code> necessarily uses the low water system, so this method
     * may not have any actual effect in some implementations.
     *
     * @param loWater the new low water value.
     */
    void setLowWater(long loWater);

    /**
     * Sets the maximum capacity for this <code>cache</code>. This capacity has no impact on the number of items stored
     * in the <code>MemoryCache</code>, except that every item must have a positive size. Generally the used capacity is
     * the total of the sizes of all stored items.
     *
     * @param capacity the new capacity.
     */
    void setCapacity(long capacity);
}

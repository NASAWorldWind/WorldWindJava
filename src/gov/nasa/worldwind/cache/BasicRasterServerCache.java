/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.util.Logging;

import java.lang.ref.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Lado Garakanidze
 * @version $Id: BasicRasterServerCache.java 1171 2013-02-11 21:45:02Z dcollins $
 */

/**
 * The <code>BasicRasterServerCache</code> is an implementation of the memory cache that is specific to store maximum
 * possible cacheable items, until the heap size allows. Once the memory limit is hit, it will drop ALL cached items.
 * Also, BasicRasterServerCache creates a pruner thread that removes all cached items which were not used for 20 seconds
 * or more. The least recent use timeout is configurable via the <code>setLeastRecentUseTimeout()<code> method. In
 * addition, the <code>BasicRasterServerCache</code> allocates 100MB memory and keeps only a phantom reference to the
 * allocated 100M memory. Once any part of the application needs more memory the phantom referenced memory will be
 * immediately released and the phantom reference will be added to the internal reference queue, which is monitored by
 * an internal <code>MemoryMonitorThread</code>. Once the phantom reference is added to the reference queue, the entire
 * cached content will be released. This approach allows to use almost entire available heap memory to cache rasters and
 * release memory when more memory is needed to the application itself.
 */
public class BasicRasterServerCache extends BasicMemoryCache
{
    protected static final int DEFAULT_INACCESSIBLE_MEMORY_SIZE = 100 * 1024 * 1024;
    protected static final long DEFAULT_PRUNER_THREAD_TIMEOUT_MSEC = 5000L; // 20 secs = 20,000 milli-seconds
    protected static final long DEFAULT_LEAST_RECENTLY_USED_TIMEOUT_NSEC = 20000000000L;
    // 20 sec = 20,000,000,000 nano-sec

    protected AtomicInteger inaccessibleMemorySize = new AtomicInteger(DEFAULT_INACCESSIBLE_MEMORY_SIZE);
    protected final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();
    protected Reference<Object> lowMemorySemaphore = null;

    protected long timeoutLeastRecentUseInNanoSeconds = DEFAULT_LEAST_RECENTLY_USED_TIMEOUT_NSEC;

    private final ReentrantLock removalLock = new ReentrantLock();

    /**
     * Constructs a new cache which uses entire memory, but will immediately drop all cached entries ones there is a
     * need for more memory by anyone else.
     */
    public BasicRasterServerCache()
    {
        super(0L,
            Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory());

        new Thread(new MemoryMonitorThread()).start();
        new Thread(new CachePrunerThread()).start();
    }

    public BasicRasterServerCache(int inaccessibleMemorySize)
    {
        this();
        this.inaccessibleMemorySize.set(inaccessibleMemorySize);
    }

    @Override
    public boolean add(Object key, Object clientObject, long clientObjectSize)
    {
        BasicMemoryCache.CacheEntry entry = new BasicMemoryCache.CacheEntry(key, clientObject, clientObjectSize);

        synchronized (this.lock)
        {
            this.removeExpiredEntries();

            CacheEntry existing = this.entries.get(key);
            if (existing != null) // replacing
            {
                this.removeEntry(existing);
            }

            this.currentUsedCapacity.addAndGet(clientObjectSize);
            this.entries.putIfAbsent(entry.key, entry);
            this.updateMemorySemaphore();
        }

        return true;
    }

    protected void updateMemorySemaphore()
    {
        try
        {
            if (this.lowMemorySemaphore == null || null == this.lowMemorySemaphore.get())
                this.lowMemorySemaphore = new SoftReference<Object>(new byte[this.inaccessibleMemorySize.get()],
                    this.queue);
        }
        catch (Throwable t)
        {
            Logging.logger().finest(t.getMessage());
        }
    }

    public long getLeastRecentUseTimeout()
    {
        return this.timeoutLeastRecentUseInNanoSeconds;
    }

    public void setLeastRecentUseTimeout(long nanoSeconds)
    {
        this.timeoutLeastRecentUseInNanoSeconds = nanoSeconds;
    }

    protected void removeExpiredEntries()
    {
        if (this.entries.size() == 0)
            return;

        if (this.removalLock.tryLock())
        {
            try
            {
                CacheEntry[] timeOrderedEntries = new CacheEntry[this.entries.size()];
                java.util.Arrays.sort(this.entries.values().toArray(timeOrderedEntries));

                for (CacheEntry entry : timeOrderedEntries)
                {
                    if (null != entry && (System.nanoTime() - entry.lastUsed) > this.getLeastRecentUseTimeout())
                    {
                        this.removeEntry(entry);
                    }
                }
            }
            finally
            {
                this.removalLock.unlock();
            }
        }
    }

    private class MemoryMonitorThread implements Runnable
    {
        public void run()
        {
            try
            {
                for (; ;)
                {
                    Reference ref = queue.remove();
                    if (null != ref)
                    {
                        ref.clear();  // clear the soft reference

                        clear(); // drop entire cache

//                    System.runFinalization();
//                    System.gc();
                    }
                }
            }
            catch (InterruptedException ignore)
            {
            }
            finally
            {
                if (Thread.currentThread().isInterrupted())
                    Thread.interrupted();
            }
        }
    }

    private class CachePrunerThread implements Runnable
    {
        public void run()
        {
            try
            {
                for (; ;)
                {
                    Thread.sleep(DEFAULT_PRUNER_THREAD_TIMEOUT_MSEC);
                    removeExpiredEntries();
                }
            }
            catch (InterruptedException ignore)
            {
            }
            finally
            {
                if (Thread.currentThread().isInterrupted())
                    Thread.interrupted();
            }
        }
    }
}

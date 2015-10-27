/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.cache;

import com.jogamp.opengl.util.texture.Texture;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.*;
import java.util.logging.Level;

/**
 * Provides the interface for caching of OpenGL resources that are stored on or registered with a GL context. This cache
 * maintains a map of resources that fit within a specifiable memory capacity. If adding a resource would exceed this
 * cache's capacity, existing but least recently used resources are removed from the cache to make room. The cache is
 * reduced to the "low water" size in this case (see {@link #setLowWater(long)}.
 * <p/>
 * When a resource is removed from the cache, and if it is a recognized OpenGL resource -- a texture, a list of vertex
 * buffer IDs, a list of display list IDs, etc. -- and there is a current Open GL context, the appropriate glDelete
 * function is called to de-register the resource with the GPU. If there is no current OpenGL context the resource is
 * not deleted and will likely remain allocated on the GPU until the GL context is destroyed.
 *
 * @author tag
 * @version $Id: BasicGpuResourceCache.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicGpuResourceCache implements GpuResourceCache
{
    public static class CacheEntry implements Cacheable
    {
        protected final String resourceType;
        protected final Object resource;
        protected long resourceSize;

        public CacheEntry(Object resource, String resourceType)
        {
            this.resource = resource;
            this.resourceType = resourceType;
        }

        public CacheEntry(Object resource, String resourceType, long size)
        {
            this.resource = resource;
            this.resourceType = resourceType;
            this.resourceSize = size;
        }

        public long getSizeInBytes()
        {
            return this.resourceSize;
        }
    }

    protected final BasicMemoryCache resources;

    public BasicGpuResourceCache(long loWater, long hiWater)
    {
        this.resources = new BasicMemoryCache(loWater, hiWater);
        this.resources.setName("GPU Resource Cache");
        this.resources.addCacheListener(new MemoryCache.CacheListener()
        {
            public void entryRemoved(Object key, Object clientObject)
            {
                onEntryRemoved(key, clientObject);
            }

            public void removalException(Throwable e, Object key, Object clientObject)
            {
                String msg = Logging.getMessage("BasicMemoryCache.ExceptionFromRemovalListener", e.getMessage());
                Logging.logger().log(Level.INFO, msg);
            }
        });
    }

    @SuppressWarnings({"UnusedParameters"})
    protected void onEntryRemoved(Object key, Object clientObject)
    {
        GLContext context = GLContext.getCurrent();
        if (context == null || context.getGL() == null)
            return;

        if (!(clientObject instanceof CacheEntry)) // shouldn't be null or wrong type, but check anyway
            return;

        CacheEntry entry = (CacheEntry) clientObject;
        GL2 gl = context.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (entry.resourceType == TEXTURE)
        {
            // Unbind a tile's texture when the tile leaves the cache.
            ((Texture) entry.resource).destroy(gl);
        }
        else if (entry.resourceType == VBO_BUFFERS)
        {
            int[] ids = (int[]) entry.resource;
            gl.glDeleteBuffers(ids.length, ids, 0);
        }
        else if (entry.resourceType == DISPLAY_LISTS)
        {
            // Delete display list ids. They're in a two-element int array, with the id at 0 and the count at 1
            int[] ids = (int[]) entry.resource;
            gl.glDeleteLists(ids[0], ids[1]);
        }
    }

    public void put(Object key, Texture texture)
    {
        CacheEntry te = this.createCacheEntry(texture, TEXTURE);
        this.resources.add(key, te);
    }

    public void put(Object key, Object resource, String resourceType, long size)
    {
        CacheEntry te = this.createCacheEntry(resource, resourceType, size);
        this.resources.add(key, te);
    }

    protected CacheEntry createCacheEntry(Object resource, String resourceType)
    {
        CacheEntry entry = new CacheEntry(resource, resourceType);
        entry.resourceSize = this.computeEntrySize(entry);

        return entry;
    }

    protected CacheEntry createCacheEntry(Object resource, String resourceType, long size)
    {
        CacheEntry entry = new CacheEntry(resource, resourceType, size);
        entry.resourceSize = size;

        return entry;
    }

    public Object get(Object key)
    {
        CacheEntry entry = (CacheEntry) this.resources.getObject(key);
        return entry != null ? entry.resource : null;
    }

    public Texture getTexture(Object key)
    {
        CacheEntry entry = (CacheEntry) this.resources.getObject(key);
        return entry != null && entry.resourceType == TEXTURE ? (Texture) entry.resource : null;
    }

    public void remove(Object key)
    {
        this.resources.remove(key);
    }

    public int getNumObjects()
    {
        return this.resources.getNumObjects();
    }

    public long getCapacity()
    {
        return this.resources.getCapacity();
    }

    public long getUsedCapacity()
    {
        return this.resources.getUsedCapacity();
    }

    public long getFreeCapacity()
    {
        return this.resources.getFreeCapacity();
    }

    public boolean contains(Object key)
    {
        return this.resources.contains(key);
    }

    public void clear()
    {
        this.resources.clear();
    }

    /**
     * Sets the new capacity (in bytes) for the cache. When decreasing cache size, it is recommended to check that the
     * lowWater variable is suitable. If the capacity infringes on items stored in the cache, these items are removed.
     * Setting a new low water is up to the user, that is, it remains unchanged and may be higher than the maximum
     * capacity. When the low water level is higher than or equal to the maximum capacity, it is ignored, which can lead
     * to poor performance when adding entries.
     *
     * @param newCapacity the new capacity of the cache.
     */
    public synchronized void setCapacity(long newCapacity)
    {
        this.resources.setCapacity(newCapacity);
    }

    /**
     * Sets the new low water level in bytes, which controls how aggresively the cache discards items.
     * <p/>
     * When the cache fills, it removes items until it reaches the low water level.
     * <p/>
     * Setting a high loWater level will increase cache misses, but decrease average add time, but setting a low loWater
     * will do the opposite.
     *
     * @param loWater the new low water level in bytes.
     */
    public synchronized void setLowWater(long loWater)
    {
        this.resources.setLowWater(loWater);
    }

    /**
     * Returns the low water level in bytes. When the cache fills, it removes items until it reaches the low water
     * level.
     *
     * @return the low water level in bytes.
     */
    public long getLowWater()
    {
        return this.resources.getLowWater();
    }

    protected long computeEntrySize(CacheEntry entry)
    {
        if (entry.resourceType == TEXTURE)
            return this.computeTextureSize(entry);

        return 0;
    }

    protected long computeTextureSize(CacheEntry entry)
    {
        Texture texture = (Texture) entry.resource;

        long size = texture.getEstimatedMemorySize();

        // JOGL returns a zero estimated memory size for some textures, so calculate a size ourselves.
        if (size < 1)
            size = texture.getHeight() * texture.getWidth() * 4;

        return size;
    }
}

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;

/**
 * The <code>CachedDataRaster</code> is used to hold data raster's source and metadata, while the actual data raster may
 * not be loaded in to the memory. This is mostly used together with a memory caches. <code>CachedDataRaster</code>
 * actually implements all interfaces of the <code>DataRaster</code>, and acts as a proxy, that loads a real data raster
 * only when it is actually needed.
 *
 * @author Lado Garakanidze
 * @version $Id: CachedDataRaster.java 3037 2015-04-17 23:08:47Z tgaskins $
 */
public class CachedDataRaster extends AVListImpl implements DataRaster
{
    protected enum ErrorHandlerMode
    {
        ALLOW_EXCEPTIONS, DISABLE_EXCEPTIONS
    }

    protected Object dataSource = null;
    protected DataRasterReader dataReader = null;

    protected MemoryCache rasterCache = null;
    protected MemoryCache.CacheListener cacheListener = null;

    protected final Object rasterUsageLock = new Object();
    protected final Object rasterRetrievalLock = new Object();

    protected String[] requiredKeys = new String[] {AVKey.SECTOR, AVKey.PIXEL_FORMAT};

    /**
     * Create a cached data raster.
     *
     * @param source the location of the local file, expressed as either a String path, a File, or a file URL.
     * @param params metadata as AVList, it is expected to next parameters: AVKey.WIDTH, AVKey.HEIGHT, AVKey.SECTOR,
     *               AVKey.PIXEL_FORMAT.
     *               <p>
     *               If any of these keys is missing, there will be an attempt made to retrieve missign metadata from
     *               the source using the reader.
     * @param reader A reference to a DataRasterReader instance
     * @param cache  A reference to a MemoryCache instance
     *
     * @throws java.io.IOException      thrown if there is an error to read metadata from the source
     * @throws IllegalArgumentException thrown when a source or a reader are null
     */
    public CachedDataRaster(Object source, AVList params, DataRasterReader reader, MemoryCache cache)
        throws java.io.IOException, IllegalArgumentException
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (reader == null)
        {
            String message = Logging.getMessage("nullValue.ReaderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        params = (null == params) ? new AVListImpl() : params;
        this.assembleMetadata(source, params, reader);

        this.dataSource = source;
        this.dataReader = reader;
        this.setValues(params.copy());

        this.rasterCache = cache;
        if (this.rasterCache != null)
        {
            this.cacheListener = new CacheListener(this.dataSource);
            this.rasterCache.addCacheListener(this.cacheListener);
        }
    }

    protected void assembleMetadata(Object source, AVList params, DataRasterReader reader)
        throws java.io.IOException, IllegalArgumentException
    {
        if (!this.hasRequiredMetadata(params, ErrorHandlerMode.DISABLE_EXCEPTIONS))
        {
            if (!reader.canRead(source, params))
            {
                String message = Logging.getMessage("DataRaster.CannotRead", source);
                Logging.logger().severe(message);
                throw new java.io.IOException(message);
            }

            if (!this.hasRequiredMetadata(params, ErrorHandlerMode.DISABLE_EXCEPTIONS))
            {
                reader.readMetadata(source, params);
                this.hasRequiredMetadata(params, ErrorHandlerMode.ALLOW_EXCEPTIONS);
            }
        }
    }

    protected String[] getRequiredKeysList()
    {
        return this.requiredKeys;
    }

    /**
     * Validates if params (AVList) has all required keys.
     *
     * @param params         AVList of key/value pairs
     * @param throwException specifies weather to throw exception when a key/value is missing, or just return false.
     *
     * @return TRUE, if all required keys are present in the params list, or both params and required keys are empty,
     *         otherwise returns FALSE (if throwException is false)
     *
     * @throws IllegalArgumentException If a key/value is missing and throwException is set to TRUE
     */
    protected boolean hasRequiredMetadata(AVList params, ErrorHandlerMode throwException)
        throws IllegalArgumentException
    {
        String[] keys = this.getRequiredKeysList();

        if (null == params || params.getEntries().size() == 0)
        {
            // return TRUE if required keys is empty, otherwise return FALSE
            return (null == keys || keys.length == 0);
        }

        if (null != keys && keys.length > 0)
        {
            for (String key : keys)
            {
                Object value = params.getValue(key);
                if (WWUtil.isEmpty(value))
                {
                    if (throwException == ErrorHandlerMode.ALLOW_EXCEPTIONS)
                    {
                        String message = Logging.getMessage("generic.MissingRequiredParameter", key);
                        Logging.logger().finest(message);
                        throw new IllegalArgumentException(message);
                    }
                    else
                        return false;
                }
            }
        }

        return true;
    }

    public int getWidth()
    {
        Object o = this.getValue(AVKey.WIDTH);
        if (null != o && o instanceof Integer)
            return (Integer) o;
        throw new WWRuntimeException(Logging.getMessage("generic.MissingRequiredParameter", AVKey.WIDTH));
    }

    public int getHeight()
    {
        Object o = this.getValue(AVKey.HEIGHT);
        if (null != o && o instanceof Integer)
            return (Integer) o;
        throw new WWRuntimeException(Logging.getMessage("generic.MissingRequiredParameter", AVKey.HEIGHT));
    }

    public Sector getSector()
    {
        Object o = this.getValue(AVKey.SECTOR);
        if (null != o && o instanceof Sector)
            return (Sector) o;
        throw new WWRuntimeException(Logging.getMessage("generic.MissingRequiredParameter", AVKey.SECTOR));
    }

    public Object getDataSource()
    {
        return this.dataSource;
    }

    public AVList getParams()
    {
        return this.getMetadata();
    }

    public AVList getMetadata()
    {
        return this.copy();
    }

    public DataRasterReader getDataRasterReader()
    {
        return this.dataReader;
    }

    public void dispose()
    {
        String message = Logging.getMessage("generic.ExceptionWhileDisposing", this.dataSource);
        Logging.logger().severe(message);
        throw new IllegalStateException(message);
    }

    protected DataRaster[] getDataRasters() throws IOException, WWRuntimeException
    {
        synchronized (this.rasterRetrievalLock)
        {
            DataRaster[] rasters = (this.rasterCache != null)
                ? (DataRaster[]) this.rasterCache.getObject(this.dataSource) : null;

            if (null != rasters)
                return rasters;

            // prevent an attempt to re-read rasters which failed to load
            if (this.rasterCache == null || !this.rasterCache.contains(this.dataSource))
            {
                long memoryDelta = 0L;

                try
                {
                    AVList rasterParams = this.copy();

                    try
                    {
                        long before = getTotalUsedMemory();
                        rasters = this.dataReader.read(this.getDataSource(), rasterParams);
                        memoryDelta = getTotalUsedMemory() - before;
                    }
                    catch (OutOfMemoryError e)
                    {
                        Logging.logger().finest(this.composeExceptionReason(e));
                        this.releaseMemory();
                        // let's retry after the finalization and GC

                        long before = getTotalUsedMemory();
                        rasters = this.dataReader.read(this.getDataSource(), rasterParams);
                        memoryDelta = getTotalUsedMemory() - before;
                    }
                }
                catch (Throwable t)
                {
                    disposeRasters(rasters); // cleanup in case of exception
                    rasters = null;
                    String message = Logging.getMessage("DataRaster.CannotRead", this.composeExceptionReason(t));
                    Logging.logger().severe(message);
                    throw new WWRuntimeException(message);
                }
                finally
                {
                    // Add rasters to the cache, even if "rasters" is null to prevent multiple failed reads.
                    if (this.rasterCache != null)
                    {
                        long totalBytes = getSizeInBytes(rasters);
                        totalBytes = (memoryDelta > totalBytes) ? memoryDelta : totalBytes;
                        if (totalBytes > 0L)
                            this.rasterCache.add(this.dataSource, rasters, totalBytes);
                    }
                }
            }

            if (null == rasters || rasters.length == 0)
            {
                String message = Logging.getMessage("generic.CannotCreateRaster", this.getDataSource());
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            return rasters;
        }
    }

    public void drawOnTo(DataRaster canvas)
    {
        synchronized (this.rasterUsageLock)
        {
            try
            {
                DataRaster[] rasters;
                try
                {
                    rasters = this.getDataRasters();
                    for (DataRaster raster : rasters)
                    {
                        raster.drawOnTo(canvas);
                    }
                }
                catch (OutOfMemoryError e)
                {
                    Logging.logger().finest(this.composeExceptionReason(e));
                    this.releaseMemory();

                    rasters = this.getDataRasters();
                    for (DataRaster raster : rasters)
                    {
                        raster.drawOnTo(canvas);
                    }
                }
            }
            catch (Throwable t)
            {
                String reason = this.composeExceptionReason(t);
                Logging.logger().log(Level.SEVERE, reason, t);
            }
        }
    }

    public DataRaster getSubRaster(AVList params)
    {
        synchronized (this.rasterUsageLock)
        {
            try
            {
                DataRaster[] rasters;
                try
                {
                    rasters = this.getDataRasters();
                    return rasters[0].getSubRaster(params);
                }
                catch (OutOfMemoryError e)
                {
                    Logging.logger().finest(this.composeExceptionReason(e));
                    this.releaseMemory();

                    // let's retry after the finalization and GC
                    rasters = this.getDataRasters();
                    return rasters[0].getSubRaster(params);
                }
            }
            catch (Throwable t)
            {
                String reason = this.composeExceptionReason(t);
                Logging.logger().log(Level.SEVERE, reason, t);
            }

            String message = Logging.getMessage("generic.CannotCreateRaster", this.getDataSource());
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
    }

    public DataRaster getSubRaster(int width, int height, Sector sector, AVList params)
    {
        if (null == params)
            params = new AVListImpl();

        params.setValue(AVKey.WIDTH, width);
        params.setValue(AVKey.HEIGHT, height);
        params.setValue(AVKey.SECTOR, sector);

        return this.getSubRaster(params);
    }

    protected void releaseMemory()
    {
        if (this.rasterCache != null)
            this.rasterCache.clear();

        System.runFinalization();

        System.gc();

        Thread.yield();
    }

    protected String composeExceptionReason(Throwable t)
    {
        StringBuffer sb = new StringBuffer();

        if (null != this.dataSource)
            sb.append(this.dataSource).append(" : ");

        sb.append(WWUtil.extractExceptionReason(t));

        return sb.toString();
    }

    protected long getSizeInBytes(DataRaster[] rasters)
    {
        long totalBytes = 0L;

        if (rasters != null)
        {
            for (DataRaster raster : rasters)
            {
                if (raster != null && raster instanceof Cacheable)
                    totalBytes += ((Cacheable) raster).getSizeInBytes();
            }
        }

        return totalBytes;
    }

    protected static void disposeRasters(DataRaster[] rasters)
    {
        if (rasters != null)
        {
            for (DataRaster raster : rasters)
            {
                raster.dispose();
            }
        }
    }

    private static class CacheListener implements MemoryCache.CacheListener
    {
        private Object key;

        private CacheListener(Object key)
        {
            this.key = key;
        }

        public void entryRemoved(Object key, Object clientObject)
        {
            if (key != this.key)
                return;

            if (clientObject == null || !(clientObject instanceof DataRaster[]))
            {
                String message = MessageFormat.format("Cannot dispose {0}", clientObject);
                Logging.logger().warning(message);
                return;
            }

            try
            {
                disposeRasters((DataRaster[]) clientObject);
            }
            catch (Exception e)
            {
                String message = Logging.getMessage("generic.ExceptionWhileDisposing", clientObject);
                Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            }
        }

        public void removalException(Throwable t, Object key, Object clientObject)
        {
            String reason = t.getMessage();
            reason = (WWUtil.isEmpty(reason) && null != t.getCause()) ? t.getCause().getMessage() : reason;
            String msg = Logging.getMessage("BasicMemoryCache.ExceptionFromRemovalListener", reason);
            Logging.logger().info(msg);
        }
    }

    protected static long getTotalUsedMemory()
    {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory());
    }
}

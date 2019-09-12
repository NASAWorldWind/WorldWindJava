/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.cache.SessionCache;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.retrieve.*;

import java.beans.*;
import java.nio.channels.ClosedByInterruptException;

/**
 * SessionCache inspects the results of a retrieval for success or failure. If the retrieval succeeded, this places the
 * retrieved data in a specified session cache with a specified cache key, and marks the resource as available. If the
 * retrieval failed, this logs the cause and marks the resource as missing. Finally, this optionally fires a property
 * change event signalling that the retrieval is complete.
 *
 * @author dcollins
 * @version $Id: SessionCacheRetrievalPostProcessor.java 3086 2015-05-13 20:27:38Z dcollins $
 */
public class SessionCacheRetrievalPostProcessor implements RetrievalPostProcessor
{
    protected String name;
    protected final SessionCache cache;
    protected final Object cacheKey;
    protected final AbsentResourceList absentResourceList;
    protected final long resourceID;
    protected final PropertyChangeListener propertyListener;
    protected final String propertyName;

    /**
     * Constructs a SessionCachePostProcessor with a specified cache and cache key, and an optional property listener
     * and property name.
     *
     * @param cache              cache that receives the retrieved data.
     * @param cacheKey           cache key to place the retrieved data under.
     * @param absentResourceList the absent resource list to update.
     * @param resourceID         the resource ID to use in the absent resource list.
     * @param propertyListener   property listener to notify when the data is available. Can be null.
     * @param propertyName       property name to use for the property event when the data is available. Can be null.
     */
    public SessionCacheRetrievalPostProcessor(SessionCache cache, Object cacheKey,
        AbsentResourceList absentResourceList, long resourceID,
        PropertyChangeListener propertyListener, String propertyName)
    {
        if (cache == null)
        {
            String message = Logging.getMessage("nullValue.CacheIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (cacheKey == null)
        {
            String message = Logging.getMessage("nullValue.CacheKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.cache = cache;
        this.cacheKey = cacheKey;
        this.absentResourceList = absentResourceList;
        this.resourceID = resourceID;
        this.propertyListener = propertyListener;
        this.propertyName = propertyName;
    }

    /**
     * Returns this post processor's name, or null if this post processor doesn't have a name.
     *
     * @return this post processor's name. May be null.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets this post processor's name to a specified String value, or null to specify that the post processor does not
     * have a name.
     *
     * @param name this post processor's name. May be null.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the session cache that receives the retrieved data.
     *
     * @return the session cache that receives data.
     */
    public final SessionCache getCache()
    {
        return this.cache;
    }

    /**
     * Returns the cache key which identifies where the retrieved data is placed in the session cache.
     *
     * @return cache key for the retrieved data.
     */
    public final Object getCacheKey()
    {
        return this.cacheKey;
    }

    /**
     * Returns the absent resource list that is updated when the retrieval completes.
     *
     * @return the absent resource list.
     */
    public final AbsentResourceList getAbsentResourceList()
    {
        return this.absentResourceList;
    }

    /**
     * Returns the resource ID to use in the absent resource list.
     *
     * @return resource ID to use in the absent resource list.
     */
    public final long getResourceID()
    {
        return this.resourceID;
    }

    /**
     * Returns the property change listener which is fired when the retrieved data is available. A null value indicates
     * that no property event is fired.
     *
     * @return property change listener to fire when retrieved data is available.
     */
    public final PropertyChangeListener getPropertyListener()
    {
        return this.propertyListener;
    }

    /**
     * Returns the property name to use in the property change event fired when retrieved data is available. A null
     * value indicates that no property event is fired.
     *
     * @return property name to fire when retrieved data is available.
     */
    public final String getPropertyName()
    {
        return this.propertyName;
    }

    /**
     * Inspect the results of a retrieval for success or failure. If the retrieval succeeded, this places the retrieved
     * data in the session cache with the specified cache key and marks the resource as available. If the retrieval
     * failed, this logs the cause and marks the resource as missing. Finally, this optionally fires a property change
     * event signalling that the retrieval is complete.
     *
     * @param retriever the Retriever which has either succeeded or failed in fetching the data.
     *
     * @return the retrieved data.
     */
    public java.nio.ByteBuffer run(Retriever retriever)
    {
        if (retriever == null)
        {
            String message = Logging.getMessage("nullValue.RetrieverIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String message = this.validate(retriever);

        if (message == null)
        {
            this.onRetrievalSuceeded(retriever);
        }
        else
        {
            this.onRetrievalFailed(retriever);
            Logging.logger().severe(message);
        }

        this.signalRetrievalComplete();
        return retriever.getBuffer();
    }

    protected void onRetrievalSuceeded(Retriever retriever)
    {
        try
        {
            this.handleContent(retriever);

            if (this.absentResourceList != null)
                this.absentResourceList.unmarkResourceAbsent(this.resourceID);
        }
        catch (Exception e)
        {
            this.handleContentException(retriever, e);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void onRetrievalFailed(Retriever retriever)
    {
        if (this.absentResourceList != null)
            this.absentResourceList.markResourceAbsent(this.resourceID);
    }

    protected String validate(Retriever retriever)
    {
        if (!retriever.getState().equals(Retriever.RETRIEVER_STATE_SUCCESSFUL))
            return Logging.getMessage("generic.RetrievalFailed", this.toString());

        if (retriever.getBuffer() == null || retriever.getBuffer().limit() == 0)
            return Logging.getMessage("generic.RetrievalReturnedNoContent", this.toString());

        return null;
    }

    protected void signalRetrievalComplete()
    {
        // If both the property listener and property name are non-null, then fire a property change event,
        // signalling that the retrieval has completed.s
        if (this.propertyListener != null && this.propertyName != null)
        {
            this.propertyListener.propertyChange(
                new PropertyChangeEvent(this, this.propertyName, null, this.propertyListener));
        }
    }

    protected void handleContent(Retriever retriever) throws Exception
    {
        String uppercaseName = retriever.getName().toUpperCase();
        if (uppercaseName.contains("SERVICE=WMS") && uppercaseName.contains("REQUEST=GETCAPABILITIES"))
        {
            this.handleWMSCapabilitiesContent(retriever);
        }
        else
        {
            this.handleUnknownContent(retriever);
        }
    }

    protected void handleWMSCapabilitiesContent(Retriever retriever) throws Exception
    {
        WMSCapabilities caps = new WMSCapabilities(retriever.getBuffer());
        this.cache.put(this.cacheKey, caps.parse());
    }

    protected void handleUnknownContent(Retriever retriever) throws Exception
    {
        this.cache.put(this.cacheKey, retriever.getBuffer());
    }

    protected void handleContentException(Retriever retriever, Exception e)
    {
        if (e instanceof ClosedByInterruptException)
        {
            Logging.logger().log(java.util.logging.Level.FINE,
                Logging.getMessage("generic.OperationCancelled",
                    "retrieval post-processing for " + retriever.getName()), e);
        }
        else
        {
            this.onRetrievalFailed(retriever);
            Logging.logger().log(java.util.logging.Level.SEVERE,
                Logging.getMessage("generic.ExceptionWhileSavingRetreivedData", retriever.getName()), e);
        }
    }

    /**
     * If the post processor has a non-null String name, this returns that name. Otherwise, this returns the superclass'
     * toString().
     *
     * @return String representation of this post processor.
     */
    public String toString()
    {
        if (this.getName() != null)
            return this.getName();

        return super.toString();
    }
}

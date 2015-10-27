/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.RasterServer;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: LocalRasterServerRetriever.java 2257 2014-08-22 18:02:19Z tgaskins $
 */
public class LocalRasterServerRetriever extends WWObjectImpl implements Retriever
{
    //    protected AVList params;
    protected RetrievalPostProcessor postProcessor;

    protected RasterServer server = null;
    protected volatile String state = RETRIEVER_STATE_NOT_STARTED;
    protected volatile int contentLength = 0;
    protected AtomicInteger contentLengthRead = new AtomicInteger(0);
    protected ByteBuffer byteBuffer;
    protected int staleRequestLimit = -1;
    protected long submitTime;
    protected long beginTime;
    protected long endTime;

    public LocalRasterServerRetriever(AVList params, RasterServer rasterServer, RetrievalPostProcessor postProcessor)
    {
        if (null != params)
            this.setValues(params);
        this.server = rasterServer;
        this.postProcessor = postProcessor;
    }

    public RasterServer getServer()
    {
        return this.server;
    }

    public void setServer(RasterServer server)
    {
        this.server = server;
    }

    public ByteBuffer getBuffer()
    {
        return this.byteBuffer;
    }

    public int getContentLength()
    {
        return this.contentLength;
    }

    public int getContentLengthRead()
    {
        return this.contentLengthRead.get();
    }

    public String getName()
    {
        Object o = this.getStringValue(AVKey.DISPLAY_NAME);
        return (WWUtil.isEmpty(o)) ? null : (String) o;
    }

    public String getState()
    {
        return this.state;
    }

    public String getContentType()
    {
        Object o = this.getValue(AVKey.IMAGE_FORMAT);
        return (WWUtil.isEmpty(o)) ? null : (String) o;
    }

    /**
     * {@inheritDoc}
     *
     * @return Always returns zero (no expiration).
     */
    public long getExpirationTime()
    {
        return 0;
    }

    public long getSubmitTime()
    {
        return this.submitTime;
    }

    public void setSubmitTime(long submitTime)
    {
        this.submitTime = submitTime;
    }

    public long getBeginTime()
    {
        return this.beginTime;
    }

    public void setBeginTime(long beginTime)
    {
        this.beginTime = beginTime;
    }

    public long getEndTime()
    {
        return this.endTime;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public int getConnectTimeout()
    {
        return 0;// Not applicable to this retriever type
    }

    public int getReadTimeout()
    {
        return 0;// Not applicable to this retriever type
    }

    public void setReadTimeout(int readTimeout)
    {
        // Not applicable to this retriever type
    }

    public void setConnectTimeout(int connectTimeout)
    {
        // Not applicable to this retriever type
    }

    public int getStaleRequestLimit()
    {
        return this.staleRequestLimit;
    }

    public void setStaleRequestLimit(int staleRequestLimit)
    {
        this.staleRequestLimit = staleRequestLimit;
    }

    public Retriever call() throws Exception
    {
        try
        {
            this.setState(RETRIEVER_STATE_STARTED);

            if (null == this.server)
            {
                this.setState(RETRIEVER_STATE_ERROR);
                String message = Logging.getMessage("nullValue.RasterServerIsNull");
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            this.byteBuffer = this.server.getRasterAsByteBuffer(this.copy());
            if (null != this.byteBuffer)
            {
                this.setState(RETRIEVER_STATE_SUCCESSFUL);
                this.contentLength = this.byteBuffer.capacity();
                this.contentLengthRead.set(this.contentLength);
            }
            else
                this.setState(RETRIEVER_STATE_ERROR);

            if (this.postProcessor != null)
                this.byteBuffer = this.postProcessor.run(this);
        }
        catch (Exception e)
        {
            this.setState(RETRIEVER_STATE_ERROR);

            Logging.logger().log(Level.SEVERE, Logging.getMessage("Retriever.ErrorPostProcessing", this.getName()), e);
            throw e;
        }

        return this;
    }

    protected void setState(String state)
    {
        String oldState = this.state;
        this.state = state;
        this.firePropertyChange(AVKey.RETRIEVER_STATE, oldState, this.state);
    }
}

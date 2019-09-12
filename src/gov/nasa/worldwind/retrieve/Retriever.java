/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.WWObject;

/**
 * @author Tom Gaskins
 * @version $Id: Retriever.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Retriever extends WWObject, java.util.concurrent.Callable<Retriever>
{
    public final String RETRIEVER_STATE_NOT_STARTED = "gov.nasa.worldwind.RetrieverStatusNotStarted";
    public final String RETRIEVER_STATE_STARTED = "gov.nasa.worldwind.RetrieverStatusStarted";
    public final String RETRIEVER_STATE_CONNECTING = "gov.nasa.worldwind.RetrieverStatusConnecting";
    public final String RETRIEVER_STATE_READING = "gov.nasa.worldwind.RetrieverStatusReading";
    public final String RETRIEVER_STATE_INTERRUPTED = "gov.nasa.worldwind.RetrieverStatusInterrupted";
    public final String RETRIEVER_STATE_ERROR = "gov.nasa.worldwind.RetrieverStatusError";
    public final String RETRIEVER_STATE_SUCCESSFUL = "gov.nasa.worldwind.RetrieverStatusSuccessful";

    public java.nio.ByteBuffer getBuffer();

    public int getContentLength();

    public int getContentLengthRead();

    public String getName();

    public String getState();

    String getContentType();

    /**
     * Indicates the expiration time of the resource retrieved by this Retriever.
     *
     * @return The expiration time of the resource, in milliseconds since the Epoch (January 1, 1970, 00:00:00 GMT).
     *         Zero indicates that there is no expiration time.
     */
    long getExpirationTime();

    long getSubmitTime();

    void setSubmitTime(long submitTime);

    long getBeginTime();

    void setBeginTime(long beginTime);

    long getEndTime();

    void setEndTime(long endTime);

    int getConnectTimeout();

    int getReadTimeout();

    void setReadTimeout(int readTimeout);

    void setConnectTimeout(int connectTimeout);

    int getStaleRequestLimit();

    void setStaleRequestLimit(int staleRequestLimit);
}

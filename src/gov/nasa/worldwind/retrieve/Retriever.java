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

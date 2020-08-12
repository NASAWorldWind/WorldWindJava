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
 * @version $Id: RetrievalService.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface RetrievalService extends WWObject
{
    RetrievalFuture runRetriever(Retriever retriever);

    RetrievalFuture runRetriever(Retriever retriever, double priority);

    void setRetrieverPoolSize(int poolSize);

    int getRetrieverPoolSize();

    boolean hasActiveTasks();

    boolean isAvailable();

    boolean contains(Retriever retriever);

    int getNumRetrieversPending();

    void shutdown(boolean immediately);

    public interface SSLExceptionListener
    {
        void onException(Throwable e, String path);
    }

    /**
     * Specifies the listener called when a {@link javax.net.ssl.SSLHandshakeException} is thrown during resource
     * retrieval.
     *
     * @param listener to listener to invoke, or null if no listener is to be invoked.
     */
    void setSSLExceptionListener(SSLExceptionListener listener);

    /**
     * Indicates the listener to be called when {@link javax.net.ssl.SSLHandshakeException}s are thrown during resource
     * retrieval.
     *
     * @return the exception listener, or null if no listener has been specified.
     */
    SSLExceptionListener getSSLExceptionListener();
}

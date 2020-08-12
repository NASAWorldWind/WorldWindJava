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

import gov.nasa.worldwind.WorldWind;

import java.util.concurrent.atomic.*;

/**
 * Periodically checks network availability.
 *
 * @author tag
 * @version $Id: NetworkCheckThread.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NetworkCheckThread extends Thread
{
    protected static final long DEFAULT_NET_CHECK_INTERVAL = 1000; // milliseconds

    protected AtomicBoolean showNetStatus;
    protected AtomicBoolean isNetAvailable;
    protected AtomicLong netChecInterval = new AtomicLong(DEFAULT_NET_CHECK_INTERVAL);

    /**
     * Constructs a new instance of this class. Once started, the thread checks network availability at a specified
     * frequency and stores the result in an atomic variable specified to the constructor. The thread terminates when
     * it's interrupted or when a specified boolean atomic variable has the value <code>false</code>.
     *
     * @param showNetStatus  a reference to an atomic variable indicating whether the thread should continue running.
     *                       This variable is tested prior to each network check. The thread terminates when it becomes
     *                       false.
     * @param isNetAvailable a reference to an atomic variable in which to write the status of the network check.
     * @param interval       the interval at which to perform the network check, or null if the default interval of one
     *                       second is to be used.
     */
    public NetworkCheckThread(AtomicBoolean showNetStatus, AtomicBoolean isNetAvailable, Long interval)
    {
        if (showNetStatus == null)
        {
            String msg = Logging.getMessage("nullValue.StatusReferenceIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (isNetAvailable == null)
        {
            String msg = Logging.getMessage("nullValue.ReturnReferenceIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.showNetStatus = showNetStatus;
        this.isNetAvailable = isNetAvailable;

        if (interval != null && interval > 0)
            this.netChecInterval.set(interval);
    }

    @Override
    public void run()
    {
        while (showNetStatus.get() && !Thread.currentThread().isInterrupted())
        {
            //noinspection EmptyCatchBlock
            try
            {
                Thread.sleep(DEFAULT_NET_CHECK_INTERVAL);
                this.isNetAvailable.set(!WorldWind.getNetworkStatus().isNetworkUnavailable());
            }
            catch (InterruptedException e)
            {
                // Intentionally empty
            }
        }
    }
}

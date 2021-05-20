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

import java.util.concurrent.*;

/**
 * A service to execute tasks periodically, or after a delay.
 *
 * @author pabercrombie
 * @version $Id: ScheduledTaskService.java 1171 2013-02-11 21:45:02Z dcollins $
 *
 * @see TaskService
 */
public interface ScheduledTaskService
{
    /**
     * Shut down the service. If the {@code immediate} parameter is {@code true}, the service will attempt to stop all
     * active tasks, and will not begin work on any other tasks in the queue. Otherwise, the service will complete all
     * tasks in the work queue, but will not accept any new tasks.
     *
     * @param immediately {@code true} to shutdown immediately.
     */
    void shutdown(boolean immediately);

    /**
     * Enqueues a task to run. Duplicate tasks are ignored.
     *
     * @param runnable the task to add
     *
     * @throws IllegalArgumentException if <code>runnable</code> is null
     */
    void addTask(Runnable runnable);

    /**
     * Enqueues a task to run after a delay. Duplicate tasks are ignored.
     *
     * @param runnable the task to add.
     * @param delay    delay before execution of the task. {@code timeUnit} determines the units of the value.
     * @param timeUnit time unit of {@code initialDelay} and {@code period}.
     *
     * @return a ScheduledFuture that can be used to get the result of the task, or cancel the task, or {@code null} if
     *         the task was not enqueued.
     *
     * @throws IllegalArgumentException if <code>runnable</code> is null
     */
    ScheduledFuture<?> addScheduledTask(Runnable runnable, long delay, TimeUnit timeUnit);

    /**
     * Enqueues a task to run periodically. This method follows the same semantics as {@link
     * java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate}. Duplicate tasks are ignored.
     *
     * @param runnable     the task to add.
     * @param initialDelay delay before the first execution of the task. {@code timeUnit} determines the units of the
     *                     value.
     * @param period       interval between executions of the task. {@code timeUnit} determines the units of the value.
     * @param timeUnit     time unit of {@code initialDelay} and {@code period}.
     *
     * @return a ScheduledFuture that can be used to get the result of the task, or cancel the task, or {@code null} if
     *         the task was not enqueued.
     *
     * @throws IllegalArgumentException if <code>runnable</code> is null
     */
    ScheduledFuture<?> addRepeatingTask(Runnable runnable, long initialDelay, long period, TimeUnit timeUnit);
}
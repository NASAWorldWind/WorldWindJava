/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
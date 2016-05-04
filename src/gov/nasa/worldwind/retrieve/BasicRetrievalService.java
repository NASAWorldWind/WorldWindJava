/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;

import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;

/**
 * Performs threaded retrieval of data.
 *
 * @author Tom Gaskins
 * @version $Id: BasicRetrievalService.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public final class BasicRetrievalService extends WWObjectImpl
    implements RetrievalService, Thread.UncaughtExceptionHandler
{
    // These constants are last-ditch values in case Configuration lacks defaults
    private static final int DEFAULT_QUEUE_SIZE = 100;
    private static final int DEFAULT_POOL_SIZE = 5;
    private static final long DEFAULT_STALE_REQUEST_LIMIT = 30000; // milliseconds
    private static final int DEFAULT_TIME_PRIORITY_GRANULARITY = 500; // milliseconds

    private static final String RUNNING_THREAD_NAME_PREFIX = null;
    private static final String IDLE_THREAD_NAME_PREFIX = null;

    private RetrievalExecutor executor; // thread pool for running retrievers
    private ConcurrentLinkedQueue<RetrievalTask> activeTasks; // tasks currently allocated a thread
    private int queueSize; // maximum queue size

    /** Encapsulates a single threaded retrieval as a {@link java.util.concurrent.FutureTask}. */
    private static class RetrievalTask extends FutureTask<Retriever>
        implements RetrievalFuture, Comparable<RetrievalTask>
    {
        private Retriever retriever;
        private double priority; // retrieval secondary priority (primary priority is submit time)

        private RetrievalTask(Retriever retriever, double priority)
        {
            super(retriever);
            this.retriever = retriever;
            this.priority = priority;
        }

        public double getPriority()
        {
            return priority;
        }

        public Retriever getRetriever()
        {
            return this.retriever;
        }

        @Override
        public void run()
        {
            if (this.isDone() || this.isCancelled())
                return;

            super.run();
        }

        /**
         * @param that the task to compare with this one
         *
         * @return 0 if task priorities are equal, -1 if priority of this is less than that, 1 otherwise
         *
         * @throws IllegalArgumentException if <code>that</code> is null
         */
        public int compareTo(RetrievalTask that)
        {
            if (that == null)
            {
                    throw new IllegalArgumentException();
            }

            if (this.priority > 0 && that.priority > 0) // only secondary priority used if either is negative
            {
                // Requests submitted within different time-granularity periods are ordered exclusive of their
                // client-specified priority.
                long now = System.currentTimeMillis();
                long thisElapsedTime = now - this.retriever.getSubmitTime();
                long thatElapsedTime = now - that.retriever.getSubmitTime();
                if (((thisElapsedTime - thatElapsedTime) / DEFAULT_TIME_PRIORITY_GRANULARITY) != 0)
                    return thisElapsedTime < thatElapsedTime ? -1 : 1;
            }

            // The client-specified priority is compared for requests submitted within the same granularity period.
            return this.priority == that.priority ? 0 : this.priority < that.priority ? -1 : 1;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final RetrievalTask that = (RetrievalTask) o;

            // Tasks are equal if their retrievers are equivalent
            return this.retriever.equals(that.retriever);
            // Priority and submit time are not factors in equality
        }

        public int hashCode()
        {
            return this.retriever.getName().hashCode();
        }
    }

    protected SSLExceptionListener sslExceptionListener;

    public SSLExceptionListener getSSLExceptionListener()
    {
        return sslExceptionListener;
    }

    public void setSSLExceptionListener(SSLExceptionListener sslExceptionListener)
    {
        this.sslExceptionListener = sslExceptionListener;
    }

    public void uncaughtException(Thread thread, Throwable throwable)
    {
    }

    private class RetrievalExecutor extends ThreadPoolExecutor
    {
        private static final long THREAD_TIMEOUT = 2; // keep idle threads alive this many seconds
        private long staleRequestLimit; // reject requests older than this

        private RetrievalExecutor(int poolSize, int queueSize)
        {
            super(poolSize, poolSize, THREAD_TIMEOUT, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>(queueSize),
                new ThreadFactory()
                {
                    public Thread newThread(Runnable runnable)
                    {
                        Thread thread = new Thread(runnable);
                        thread.setDaemon(true);
                        thread.setPriority(Thread.MIN_PRIORITY);
                        thread.setUncaughtExceptionHandler(BasicRetrievalService.this);
                        return thread;
                    }
                }, new ThreadPoolExecutor.DiscardPolicy() // abandon task when queue is full
            {
                // This listener is invoked only when the executor queue is a bounded queue and runs out of room.
                // If the queue is a java.util.concurrent.PriorityBlockingQueue, this listener is never invoked.
                public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor)
                {
                    // Interposes logging for rejected execution

                    super.rejectedExecution(runnable, threadPoolExecutor);
                }
            });

            this.staleRequestLimit = Configuration.getLongValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT,
                DEFAULT_STALE_REQUEST_LIMIT);
        }

        /**
         * @param thread   the thread the task is running on
         * @param runnable the <code>Retriever</code> running on the thread
         *
         * @throws IllegalArgumentException if either <code>thread</code> or <code>runnable</code> is null
         */
        protected void beforeExecute(Thread thread, Runnable runnable)
        {
            if (thread == null)
            {
                    throw new IllegalArgumentException();
            }
            if (runnable == null)
            {
                    throw new IllegalArgumentException();
            }

            RetrievalTask task = (RetrievalTask) runnable;

            task.retriever.setBeginTime(System.currentTimeMillis());
            long limit = task.retriever.getStaleRequestLimit() >= 0
                ? task.retriever.getStaleRequestLimit() : this.staleRequestLimit;
            if (task.retriever.getBeginTime() - task.retriever.getSubmitTime() > limit)
            {
                // Task has been sitting on the queue too long
                task.cancel(true);
            }

            if (BasicRetrievalService.this.activeTasks.contains(task))
            {
                // Task is a duplicate
                task.cancel(true);
            }

            BasicRetrievalService.this.activeTasks.add(task);

            thread.setName(RUNNING_THREAD_NAME_PREFIX + task.getRetriever().getName());
            thread.setPriority(Thread.MIN_PRIORITY); // Subordinate thread priority to rendering
            thread.setUncaughtExceptionHandler(BasicRetrievalService.this);

            super.beforeExecute(thread, runnable);
        }

        /**
         * @param runnable  the <code>Retriever</code> running on the thread
         * @param throwable an exception thrown during retrieval, will be null if no exception occurred
         *
         * @throws IllegalArgumentException if <code>runnable</code> is null
         */
        protected void afterExecute(Runnable runnable, Throwable throwable)
        {
            if (runnable == null)
            {
                    throw new IllegalArgumentException();
            }

            super.afterExecute(runnable, throwable);

            RetrievalTask task = (RetrievalTask) runnable;
            BasicRetrievalService.this.activeTasks.remove(task);
            task.retriever.setEndTime(System.currentTimeMillis());

            try
            {
                if (throwable != null)
                {
                }

                task.get(); // Wait for task to finish, cancel or break
            }
            catch (java.util.concurrent.ExecutionException e)
            {
                    if (e.getCause() instanceof SocketTimeoutException)
                {
                }
                else if (e.getCause() instanceof SSLHandshakeException)
                {
                    if (sslExceptionListener != null)
                        sslExceptionListener.onException(e.getCause(), task.getRetriever().getName());
                    else
                    {
                    }
                }
                else
                {
                }
            }
            catch (InterruptedException e)
            {
            }
            catch (java.util.concurrent.CancellationException e)
            {
            }
            finally
            {
                Thread.currentThread().setName(IDLE_THREAD_NAME_PREFIX);
            }
        }
    }

    public BasicRetrievalService()
    {
        Integer poolSize = Configuration.getIntegerValue(AVKey.RETRIEVAL_POOL_SIZE, DEFAULT_POOL_SIZE);
        this.queueSize = Configuration.getIntegerValue(AVKey.RETRIEVAL_QUEUE_SIZE, DEFAULT_QUEUE_SIZE);

        // this.executor runs the retrievers, each in their own thread
        this.executor = new RetrievalExecutor(poolSize, this.queueSize);

        // this.activeTasks holds the list of currently executing tasks (*not* those pending on the queue)
        this.activeTasks = new ConcurrentLinkedQueue<RetrievalTask>();
    }

    public void shutdown(boolean immediately)
    {
        if (immediately)
            this.executor.shutdownNow();
        else
            this.executor.shutdown();

        this.activeTasks.clear();
    }

    /**
     * @param retriever the retriever to run
     *
     * @return a future object that can be used to query the request status of cancel the request.
     *
     * @throws IllegalArgumentException if <code>retriever</code> is null or has no name
     */
    public RetrievalFuture runRetriever(Retriever retriever)
    {
        if (retriever == null)
        {
            throw new IllegalArgumentException();
        }
        if (retriever.getName() == null)
        {
            throw new IllegalArgumentException();
        }

        // Add with secondary priority that removes most recently added requests first.
        return this.runRetriever(retriever, (double) (Long.MAX_VALUE - System.currentTimeMillis()));
    }

    /**
     * @param retriever the retriever to run
     * @param priority  the secondary priority of the retriever, or negative if it is to be the primary priority
     *
     * @return a future object that can be used to query the request status of cancel the request.
     *
     * @throws IllegalArgumentException if <code>retriever</code> is null or has no name
     */
    public synchronized RetrievalFuture runRetriever(Retriever retriever, double priority)
    {
        if (retriever == null)
        {
            throw new IllegalArgumentException();
        }

        if (retriever.getName() == null)
        {
            throw new IllegalArgumentException();
        }

        if (!this.isAvailable())
        {
        }

        RetrievalTask task = new RetrievalTask(retriever, priority);
        retriever.setSubmitTime(System.currentTimeMillis());

        // Do not queue duplicates.
        if (this.activeTasks.contains(task) || this.executor.getQueue().contains(task))
            return null;

        this.executor.execute(task);

        return task;
    }

    /**
     * @param poolSize the number of threads in the thread pool
     *
     * @throws IllegalArgumentException if <code>poolSize</code> is non-positive
     */
    public void setRetrieverPoolSize(int poolSize)
    {
        if (poolSize < 1)
        {
            throw new IllegalArgumentException();
        }

        this.executor.setCorePoolSize(poolSize);
        this.executor.setMaximumPoolSize(poolSize);
    }

    public int getRetrieverPoolSize()
    {
        return this.executor.getCorePoolSize();
    }

    private boolean hasRetrievers()
    {
        Thread[] threads = new Thread[Thread.activeCount()];
        int numThreads = Thread.enumerate(threads);
        for (int i = 0; i < numThreads; i++)
        {
            if (threads[i].getName().startsWith(RUNNING_THREAD_NAME_PREFIX))
                return true;
        }
        return false;
    }

    public boolean hasActiveTasks()
    {
        return this.hasRetrievers();
    }

    public boolean isAvailable()
    {
        return this.executor.getQueue().size() < this.queueSize;
//            && !WorldWind.getNetworkStatus().isNetworkUnavailable();
    }

    public int getNumRetrieversPending()
    {
        // Could use same method to determine active tasks as hasRetrievers() above, but this method only advisory.
        return this.activeTasks.size() + this.executor.getQueue().size();
    }

    /**
     * @param retriever the retriever to check
     *
     * @return <code>true</code> if the retriever is being run or pending execution
     *
     * @throws IllegalArgumentException if <code>retriever</code> is null
     */
    public boolean contains(Retriever retriever)
    {
        if (retriever == null)
        {
            throw new IllegalArgumentException();
        }
        RetrievalTask task = new RetrievalTask(retriever, 0d);
        return (this.activeTasks.contains(task) || this.executor.getQueue().contains(task));
    }

    public double getProgress()
    {
        int totalContentLength = 0;
        int totalBytesRead = 0;

        for (RetrievalTask task : this.activeTasks)
        {
            if (task.isDone())
                continue;

            Retriever retriever = task.getRetriever();
            try
            {
                double tcl = retriever.getContentLength();
                if (tcl > 0)
                {
                    totalContentLength += tcl;
                    totalBytesRead += retriever.getContentLengthRead();
                }
            }
            catch (Exception e)
            {
            }
        }

        for (Runnable runnable : this.executor.getQueue())
        {
            RetrievalTask task =
                (RetrievalTask) runnable;

            Retriever retriever = task.getRetriever();
            try
            {
                double tcl = retriever.getContentLength();
                if (tcl > 0)
                {
                    totalContentLength += tcl;
                    totalBytesRead += retriever.getContentLengthRead();
                }
            }
            catch (Exception e)
            {
                String message = null + (
                    retriever.getName() != null ? retriever.getName() : "");
            }
        }

        // Compute an aggregated progress notification.

        double progress;

        if (totalContentLength < 1)
            progress = 0;
        else
            progress = Math.min(100.0, 100.0 * (double) totalBytesRead / (double) totalContentLength);

        return progress;
    }
}

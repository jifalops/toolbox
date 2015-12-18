package com.jifalops.toolbox.android.util;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jacob.phillips on 12/17/15.
 */
public class ThreadPool {
    private static final String TAG = ThreadPool.class.getSimpleName();

    private static ThreadPool instance;
    public static ThreadPool getInstance() {
        if (instance == null) instance = new ThreadPool();
        return instance;
    }

    private ScheduledThreadPoolExecutor threadPool;

    private ThreadPool() {
        setupThreadPool();
    }

    private void setupThreadPool() {
        int cores = Runtime.getRuntime().availableProcessors();
        Log.i(TAG, "There are " + cores + " available cores on this device.");
        int threads = cores - 1; // spare one core for the UI thread

        // We want 1 thread per core, with a minimum of 2 threads.
        //      1 thread for network activity and
        //      1 thread for file I/O
        if (threads < 2) threads = 2;
        Log.i(TAG, "Creating thread pool with " + threads + " threads");
        threadPool = new ScheduledThreadPoolExecutor(threads);

        threadPool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        threadPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        threadPool.allowCoreThreadTimeOut(false);

        // Maximum of 2 threads per core, except the UI thread gets a whole core
        threadPool.setMaximumPoolSize(threads * 2);
    }

    public void shutDown(boolean terminateExisting) {
        if (terminateExisting) threadPool.shutdownNow();
        else threadPool.shutdown();
    }


    //
    // Expose thread pool methods
    //
    /** See {@link ScheduledThreadPoolExecutor#execute(Runnable)} */
    public void execute(Runnable command) {
        threadPool.execute(command);
        Log.d(TAG, "Pool size: " + threadPool.getPoolSize());
        Log.d(TAG, "Active thread count: " + threadPool.getActiveCount());
    }
    /** See {@link ScheduledThreadPoolExecutor#schedule(Callable, long, TimeUnit)} */
    public ScheduledFuture<?> schedule(Callable<?> callable, long delay, TimeUnit unit) {
        return threadPool.schedule(callable, delay, unit);
    }
    /** See {@link ScheduledThreadPoolExecutor#schedule(Runnable, long, TimeUnit)} */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return threadPool.schedule(command, delay, unit);
    }
    /** See {@link ScheduledThreadPoolExecutor#scheduleAtFixedRate(Runnable, long, long, TimeUnit)}  */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return threadPool.scheduleAtFixedRate(command, initialDelay, period, unit);
    }
    /** See {@link ScheduledThreadPoolExecutor#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)}  */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return threadPool.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
    /** See {@link ScheduledThreadPoolExecutor#submit(Callable)} */
    public Future<?> submit(Callable<?> callable) {
        return threadPool.submit(callable);
    }
    /** See {@link ScheduledThreadPoolExecutor#submit(Runnable)} */
    public Future<?> submit(Runnable task) {
        return threadPool.submit(task);
    }
    /** See {@link ScheduledThreadPoolExecutor#submit(Runnable, Object)}  */
    public Future<?> submit(Runnable task, Object result) {
        return threadPool.submit(task, result);
    }
    /** See {@link ScheduledThreadPoolExecutor#getPoolSize()}   */
    public int getPoolSize() {
        return threadPool.getPoolSize();
    }
    /** See {@link ScheduledThreadPoolExecutor#getActiveCount()}    */
    public int getActiveCount() {
        return threadPool.getActiveCount();
    }
}

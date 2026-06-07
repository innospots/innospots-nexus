package com.innospots.nexus.base.thread;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Custom {@link ThreadPoolExecutor} that captures and propagates
 * {@link TLC} context from the submitting thread to the worker thread.
 */
public class NexusThreadPoolExecutor extends ThreadPoolExecutor {

    private final String poolName;

    public NexusThreadPoolExecutor(
            String poolName,
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory,
            RejectedExecutionHandler handler
    ) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.poolName = poolName;
    }

    /** Returns the human-readable pool name. */
    public String poolName() {
        return poolName;
    }

    /** Returns true if at least one thread is available for immediate work. */
    public boolean hasAvailableThread() {
        return availableThreadCount() > 0;
    }

    /** Returns the number of threads not currently executing tasks. */
    public int availableThreadCount() {
        return Math.max(0, getMaximumPoolSize() - getActiveCount());
    }

    @Override
    public void execute(Runnable command) {
        super.execute(wrap(command));
    }

    @Override
    public Future<?> submit(Runnable task) {
        FutureTask<Void> future = new FutureTask<>(wrap(task), null);
        execute(future);
        return future;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> future = new FutureTask<>(wrap(task));
        execute(future);
        return future;
    }

    /**
     * Wraps a Runnable to capture the submitting thread's TLC context
     * and restore it in the worker thread. The worker's original context
     * is restored in the finally block.
     */
    private Runnable wrap(Runnable command) {
        Map<String, Object> captured = TLC.snapshot();
        return () -> {
            Map<String, Object> previous = TLC.snapshot();
            try {
                TLC.restore(captured);
                command.run();
            } finally {
                TLC.restore(previous);
            }
        };
    }

    /**
     * Wraps a Callable to capture and propagate TLC context across threads.
     */
    private <T> Callable<T> wrap(Callable<T> command) {
        Map<String, Object> captured = TLC.snapshot();
        return () -> {
            Map<String, Object> previous = TLC.snapshot();
            try {
                TLC.restore(captured);
                return command.call();
            } finally {
                TLC.restore(previous);
            }
        };
    }
}

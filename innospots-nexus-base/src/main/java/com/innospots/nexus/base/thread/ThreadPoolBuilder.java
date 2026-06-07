package com.innospots.nexus.base.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Fluent builder for {@link NexusThreadPoolExecutor} instances. Defaults:
 * core = available processors, max = core, queue = 20k, keep-alive = 120s,
 * daemon = false, rejected execution = caller-runs policy.
 */
public final class ThreadPoolBuilder {

    public static final int DEFAULT_QUEUE_CAPACITY = 20_000;
    public static final int DEFAULT_KEEP_ALIVE_SECONDS = 120;

    private final String poolName;
    private int coreSize = Runtime.getRuntime().availableProcessors();
    private int maxSize = coreSize;
    private int queueCapacity = DEFAULT_QUEUE_CAPACITY;
    private int keepAliveSeconds = DEFAULT_KEEP_ALIVE_SECONDS;
    private boolean daemon;
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

    private ThreadPoolBuilder(String poolName) {
        this.poolName = poolName;
    }

    /**
     * Creates a builder with the given pool name.
     */
    public static ThreadPoolBuilder builder(String poolName) {
        return new ThreadPoolBuilder(poolName);
    }

    /**
     * Convenience method that builds a pool in one call.
     */
    public static NexusThreadPoolExecutor build(int coreSize, int maxSize, int queueCapacity, String poolName) {
        return builder(poolName).coreSize(coreSize).maxSize(maxSize).queueCapacity(queueCapacity).build();
    }

    /** Sets the core pool size (minimum 1). */
    public ThreadPoolBuilder coreSize(int coreSize) {
        this.coreSize = Math.max(1, coreSize);
        return this;
    }

    /** Sets the maximum pool size (minimum 1). */
    public ThreadPoolBuilder maxSize(int maxSize) {
        this.maxSize = Math.max(1, maxSize);
        return this;
    }

    /** Sets the work queue capacity. Zero or negative uses a SynchronousQueue. */
    public ThreadPoolBuilder queueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        return this;
    }

    /** Sets the keep-alive time in seconds for idle threads. */
    public ThreadPoolBuilder keepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = Math.max(0, keepAliveSeconds);
        return this;
    }

    /** Sets whether worker threads should be daemon threads. */
    public ThreadPoolBuilder daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /** Sets the rejected execution handler (default: CallerRunsPolicy). */
    public ThreadPoolBuilder rejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        if (rejectedExecutionHandler != null) {
            this.rejectedExecutionHandler = rejectedExecutionHandler;
        }
        return this;
    }

    /**
     * Builds the {@link NexusThreadPoolExecutor}. Max pool size is
     * normalized to be at least the core size.
     */
    public NexusThreadPoolExecutor build() {
        int normalizedMax = Math.max(coreSize, maxSize);
        return new NexusThreadPoolExecutor(
                poolName,
                coreSize,
                normalizedMax,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                createQueue(queueCapacity),
                new NexusThreadFactory(poolName, daemon),
                rejectedExecutionHandler
        );
    }

    /**
     * Creates a bounded queue if capacity > 0, otherwise a SynchronousQueue.
     */
    static BlockingQueue<Runnable> createQueue(int queueCapacity) {
        return queueCapacity > 0 ? new ArrayBlockingQueue<>(queueCapacity) : new SynchronousQueue<>();
    }
}

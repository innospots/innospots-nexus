package com.innospots.nexus.base.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Global async executor facade backed by a singleton {@link NexusThreadPoolExecutor}.
 * Lazily initialized with sensible defaults (core = available processors, no queue).
 */
public final class AsyncExecutors {

    private static volatile NexusThreadPoolExecutor executor;

    private AsyncExecutors() {
    }

    public static synchronized void initialize() {
        initialize(Runtime.getRuntime().availableProcessors(), 0, "nexus-async");
    }

    public static synchronized void initialize(int coreSize, int queueCapacity, String poolName) {
        close();
        executor = ThreadPoolBuilder.builder(poolName)
                .coreSize(coreSize)
                .maxSize(coreSize)
                .queueCapacity(queueCapacity)
                .build();
    }

    public static Future<?> submit(Runnable runnable) {
        return current().submit(runnable);
    }

    public static <T> Future<T> submit(Callable<T> callable) {
        return current().submit(callable);
    }

    public static synchronized void close() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private static NexusThreadPoolExecutor current() {
        NexusThreadPoolExecutor current = executor;
        if (current == null) {
            initialize();
            current = executor;
        }
        return current;
    }
}

package com.innospots.nexus.base.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 全局异步执行器门面，由单例 {@link NexusThreadPoolExecutor} 支撑。
 * 延迟初始化，默认核心线程数 = 可用处理器数，无队列缓冲。
 *
 * @author Smars
 * @date 2026/09/13
 * @see NexusThreadPoolExecutor
 * @see ThreadPoolBuilder
 */
public final class AsyncExecutors {

    private static volatile NexusThreadPoolExecutor executor;

    private AsyncExecutors() {
    }

    /**
     * 使用默认参数初始化全局异步执行器。
     */
    public static synchronized void initialize() {
        initialize(Runtime.getRuntime().availableProcessors(), 0, "nexus-async");
    }

    /**
     * 使用指定参数初始化全局异步执行器。
     *
     * @param coreSize      核心线程数
     * @param queueCapacity 队列容量
     * @param poolName      线程池名称
     */
    public static synchronized void initialize(int coreSize, int queueCapacity, String poolName) {
        close();
        executor = ThreadPoolBuilder.builder(poolName)
                .coreSize(coreSize)
                .maxSize(coreSize)
                .queueCapacity(queueCapacity)
                .build();
    }

    /**
     * 提交异步 {@link Runnable} 任务。
     *
     * @param runnable 待执行的任务
     * @return 任务 Future
     */
    public static Future<?> submit(Runnable runnable) {
        return current().submit(runnable);
    }

    /**
     * 提交异步 {@link Callable} 任务。
     *
     * @param callable 待执行的任务
     * @param <T>      返回值类型
     * @return 任务 Future
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        return current().submit(callable);
    }

    /**
     * 关闭全局异步执行器并释放资源。
     */
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

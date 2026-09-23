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
 * 自定义 {@link ThreadPoolExecutor}，在提交线程与工作线程之间捕获并传播 {@link TLC} 上下文。
 *
 * @author Smars
 * @date 2026/09/13
 * @see TLC
 * @see ThreadPoolExecutor
 */
public class NexusThreadPoolExecutor extends ThreadPoolExecutor {

    private final String poolName;

    /**
     * 创建线程池执行器。
     *
     * @param poolName        线程池名称
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   空闲线程存活时间
     * @param unit            存活时间单位
     * @param workQueue       工作队列
     * @param threadFactory   线程工厂
     * @param handler         拒绝处理策略
     */
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

    /**
     * 返回可读性良好的线程池名称。
     *
     * @return 线程池名称
     */
    public String poolName() {
        return poolName;
    }

    /**
     * 判断是否至少有一个线程可立即处理任务。
     *
     * @return 有可用线程时返回 {@code true}
     */
    public boolean hasAvailableThread() {
        return availableThreadCount() > 0;
    }

    /**
     * 返回当前未在执行任务的线程数。
     *
     * @return 可用线程数
     */
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
     * 包装 {@link Runnable}：捕获提交线程的 TLC 上下文并在工作线程中恢复，
     * 工作线程的原始上下文在 finally 块中还原。
     *
     * @param command 原始任务
     * @return 包装后的任务
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
     * 包装 {@link Callable}：跨线程捕获并传播 TLC 上下文。
     *
     * @param command 原始任务
     * @param <T>     返回值类型
     * @return 包装后的任务
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

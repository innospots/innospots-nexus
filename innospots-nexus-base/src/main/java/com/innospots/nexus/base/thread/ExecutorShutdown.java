package com.innospots.nexus.base.thread;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@link ExecutorService} 优雅关闭工具。
 *
 * @author Smars
 * @date 2026/09/19
 * @see ThreadPoolBuilder
 * @see ScheduledThreadPoolBuilder
 */
public final class ExecutorShutdown {

    private static final Duration DEFAULT_AWAIT = Duration.ofSeconds(5);

    private ExecutorShutdown() {
    }

    /**
     * 使用默认 5 秒等待时间优雅关闭。
     *
     * @param executor 待关闭执行器
     */
    public static void shutdownGracefully(ExecutorService executor) {
        shutdownGracefully(executor, DEFAULT_AWAIT);
    }

    /**
     * 发起 {@link ExecutorService#shutdown()} 并等待；超时后 {@link ExecutorService#shutdownNow()}。
     *
     * @param executor 待关闭执行器
     * @param await    等待时长
     */
    public static void shutdownGracefully(ExecutorService executor, Duration await) {
        if (executor == null || executor.isShutdown()) {
            return;
        }
        Duration timeout = await == null || await.isNegative() || await.isZero()
                ? DEFAULT_AWAIT
                : await;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException interrupted) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

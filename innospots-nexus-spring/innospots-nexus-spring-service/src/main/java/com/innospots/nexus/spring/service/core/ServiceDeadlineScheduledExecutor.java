package com.innospots.nexus.spring.service.core;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.innospots.nexus.base.thread.ExecutorShutdown;
import com.innospots.nexus.base.thread.ScheduledThreadPoolBuilder;
import com.innospots.nexus.service.runtime.time.DeadlineScheduler;

/**
 * {@link DeadlineScheduler} 使用的单线程调度池，遵循 base 后台线程命名与优雅关闭约定。
 *
 * @author Smars
 * @date 2026/09/19
 * @see DeadlineScheduler
 * @see ServiceCoreConfiguration
 */
public final class ServiceDeadlineScheduledExecutor implements AutoCloseable {

    private static final String POOL_NAME = "service-deadline";

    private final ScheduledThreadPoolExecutor executor;

    /**
     * 创建守护后台调度池。
     */
    public ServiceDeadlineScheduledExecutor() {
        this.executor = ScheduledThreadPoolBuilder.builder(POOL_NAME).background().build();
    }

    /**
     * 返回供 {@link DeadlineScheduler} 注入的执行器视图。
     *
     * @return 调度执行器
     */
    public ScheduledExecutorService executor() {
        return executor;
    }

    /**
     * 优雅关闭调度池。
     */
    @Override
    public void close() {
        ExecutorShutdown.shutdownGracefully(executor);
    }
}

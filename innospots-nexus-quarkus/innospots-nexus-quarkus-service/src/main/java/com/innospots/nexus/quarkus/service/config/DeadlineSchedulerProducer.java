package com.innospots.nexus.quarkus.service.config;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.innospots.nexus.base.thread.ExecutorShutdown;
import com.innospots.nexus.base.thread.ScheduledThreadPoolBuilder;
import com.innospots.nexus.service.runtime.time.DeadlineScheduler;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * 提供进程级截止时间调度器。
 */
@ApplicationScoped
public final class DeadlineSchedulerProducer {

    private static final String POOL_NAME = "service-deadline";

    private final ScheduledThreadPoolExecutor executor =
            ScheduledThreadPoolBuilder.builder(POOL_NAME).background().build();
    private final DeadlineScheduler scheduler = new DeadlineScheduler(executor);

    public DeadlineScheduler scheduler() {
        return scheduler;
    }

    @PreDestroy
    void shutdown() {
        ExecutorShutdown.shutdownGracefully(executor);
    }
}

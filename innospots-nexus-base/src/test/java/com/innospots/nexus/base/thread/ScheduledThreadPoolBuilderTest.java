package com.innospots.nexus.base.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledThreadPoolBuilderTest {

    private ScheduledThreadPoolExecutor executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            ExecutorShutdown.shutdownGracefully(executor);
        }
    }

    @Test
    void backgroundPoolUsesDaemonThreadsByDefault() throws Exception {
        executor = ScheduledThreadPoolBuilder.builder("deadline").build();
        AtomicReference<Boolean> daemon = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        executor.execute(() -> {
            daemon.set(Thread.currentThread().isDaemon());
            latch.countDown();
        });
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(daemon.get()).isTrue();
        assertThat(executor.getThreadFactory()).isInstanceOf(NexusThreadFactory.class);
        assertThat(((NexusThreadFactory) executor.getThreadFactory()).executionRole())
                .isEqualTo(ThreadExecutionRole.BACKGROUND);
    }

    @Test
    void workerPoolUsesNonDaemonThreads() throws Exception {
        executor = ScheduledThreadPoolBuilder.builder("worker-scheduler").worker().build();
        AtomicReference<Boolean> daemon = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        executor.execute(() -> {
            daemon.set(Thread.currentThread().isDaemon());
            latch.countDown();
        });
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(daemon.get()).isFalse();
    }
}

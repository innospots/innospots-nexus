package com.innospots.nexus.spring.service.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceDeadlineScheduledExecutorTest {

    @Test
    void closeShutsDownScheduledPool() throws InterruptedException {
        ServiceDeadlineScheduledExecutor scheduledExecutor = new ServiceDeadlineScheduledExecutor();
        AtomicBoolean executed = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);
        scheduledExecutor.executor().schedule(() -> {
            executed.set(true);
            latch.countDown();
        }, 10, TimeUnit.MILLISECONDS);
        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(executed).isTrue();

        scheduledExecutor.close();
        assertThat(scheduledExecutor.executor().isShutdown()).isTrue();
    }
}

package com.innospots.nexus.base.thread;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadPoolBuilderTest {

    private NexusThreadPoolExecutor executor;

    @AfterEach
    void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
        }
        TLC.clear();
    }

    @Test
    void buildsNamedThreadPoolAndPropagatesContext() throws Exception {
        TLC.put("traceId", "trace-1");
        TLC.projectId(1001L);
        executor = ThreadPoolBuilder.builder("agent-worker")
                .coreSize(1)
                .maxSize(1)
                .queueCapacity(4)
                .build();

        Future<String> result = executor.submit(() ->
                Thread.currentThread().getName() + "|" + TLC.getString("traceId") + "|" + TLC.projectId());

        assertThat(result.get(2, TimeUnit.SECONDS)).startsWith("agent-worker-").endsWith("|trace-1|1001");
    }

    @Test
    void asyncExecutorsManageSharedExecutorLifecycle() throws Exception {
        AsyncExecutors.initialize(1, 2, "shared-agent");

        Future<String> result = AsyncExecutors.submit(() -> Thread.currentThread().getName());

        assertThat(result.get(2, TimeUnit.SECONDS)).startsWith("shared-agent-");
    }
}

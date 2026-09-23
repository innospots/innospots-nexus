package com.innospots.nexus.service.runtime.lifecycle;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 服务运行时启停关闭幂等测试。
 */
class ServiceRuntimeTest {

    @Test
    void startStopAndCloseAreIdempotent() {
        ServiceRuntime runtime = ServiceRuntime.builder().build();

        runtime.start();
        runtime.start();
        assertThat(runtime.state()).isEqualTo(RuntimeState.READY);
        assertThat(runtime.admitsNewWork()).isTrue();

        runtime.stop(Duration.ofSeconds(1)).toCompletableFuture().join();
        runtime.stop(Duration.ofSeconds(1)).toCompletableFuture().join();
        assertThat(runtime.state()).isEqualTo(RuntimeState.STOPPED);
        assertThat(runtime.admitsNewWork()).isFalse();

        runtime.close().toCompletableFuture().join();
        runtime.close().toCompletableFuture().join();
        assertThat(runtime.state()).isEqualTo(RuntimeState.CLOSED);
        assertThat(runtime.admitsNewWork()).isFalse();
    }

    @Test
    void closeFromReadyStopsThenCloses() {
        ServiceRuntime runtime = ServiceRuntime.builder().build();
        runtime.start();

        runtime.close().toCompletableFuture().join();

        assertThat(runtime.state()).isEqualTo(RuntimeState.CLOSED);
    }

    @Test
    void restartAfterCloseIsRejected() {
        ServiceRuntime runtime = ServiceRuntime.builder().build();
        runtime.start();
        runtime.close().toCompletableFuture().join();

        assertThatThrownBy(runtime::start)
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.CONFIG_ERROR.fullCode());
    }
}

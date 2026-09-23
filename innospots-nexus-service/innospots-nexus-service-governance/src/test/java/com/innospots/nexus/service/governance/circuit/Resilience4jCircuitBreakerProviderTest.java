package com.innospots.nexus.service.governance.circuit;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.invocation.OutcomeType;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.governance.config.CircuitBreakerPolicy;
import com.innospots.nexus.service.governance.config.GovernanceConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 断路器失败分类与 OPEN 拒绝测试。
 */
class Resilience4jCircuitBreakerProviderTest {

    @Test
    void ignoresPermissionAndLimitFailures() {
        assertThat(Resilience4jCircuitBreakerProvider.shouldRecordFailure(outcome(
                OutcomeType.REJECTED,
                NexusStatusCode.NO_PERMISSION.fullCode()))).isFalse();
        assertThat(Resilience4jCircuitBreakerProvider.shouldRecordFailure(outcome(
                OutcomeType.FAILED,
                NexusStatusCode.LIMIT_EXCEEDED.fullCode()))).isFalse();
        assertThat(Resilience4jCircuitBreakerProvider.shouldRecordFailure(outcome(
                OutcomeType.CANCELLED,
                ServiceStatusCode.OPERATION_CANCELLED.fullCode()))).isFalse();
    }

    @Test
    void openCircuitRejectsWithSrv060005() {
        Resilience4jCircuitBreakerProvider provider = provider(new CircuitBreakerPolicy(
                4,
                2,
                50F,
                100F,
                Duration.ofSeconds(5),
                Duration.ofMinutes(1),
                1));
        recordFailure(provider, "downstream", ServiceStatusCode.DOWNSTREAM_FAILED.fullCode());
        recordFailure(provider, "downstream", ServiceStatusCode.DOWNSTREAM_FAILED.fullCode());

        assertThatThrownBy(() -> provider.acquire("downstream"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.CIRCUIT_OPEN.fullCode());
    }

    private static void recordFailure(Resilience4jCircuitBreakerProvider provider, String key, String code) {
        provider.acquire(key).finish(outcome(OutcomeType.FAILED, code));
    }

    private static InvocationOutcome outcome(OutcomeType type, String code) {
        return new InvocationOutcome(type, code, Instant.now(), Duration.ofMillis(5), 0, 0);
    }

    private static Resilience4jCircuitBreakerProvider provider(CircuitBreakerPolicy policy) {
        GovernanceConfig config = new GovernanceConfig(
                Map.of(),
                Map.of(),
                Map.of("downstream", policy),
                Map.of(),
                100,
                Duration.ofMinutes(15),
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofSeconds(30));
        return new Resilience4jCircuitBreakerProvider(config);
    }
}

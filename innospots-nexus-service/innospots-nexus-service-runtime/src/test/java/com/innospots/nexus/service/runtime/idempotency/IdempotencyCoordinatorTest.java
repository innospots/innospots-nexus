package com.innospots.nexus.service.runtime.idempotency;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.invocation.OutcomeType;
import com.innospots.nexus.service.contract.policy.AuditMode;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.policy.ResponseProfile;
import com.innospots.nexus.service.contract.security.ResourceRef;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 幂等协调器准入与冲突测试。
 */
class IdempotencyCoordinatorTest {

    @Test
    void disabledCoordinatorIsNoOp() {
        IdempotencyCoordinator coordinator = new IdempotencyCoordinator(false);
        coordinator.enter(context("key-a")).toCompletableFuture().join();
    }

    @Test
    void missingHeaderIsNoOp() {
        IdempotencyCoordinator coordinator = new IdempotencyCoordinator(true);
        coordinator.enter(context(null)).toCompletableFuture().join();
    }

    @Test
    void duplicateFingerprintWhileInFlightIsConflict() {
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore(100, Duration.ofMinutes(1));
        IdempotencyCoordinator coordinator = new IdempotencyCoordinator(true, store);
        var firstLease = coordinator.enter(context("dup-key")).toCompletableFuture().join();
        assertThatThrownBy(() -> coordinator.enter(context("dup-key")).toCompletableFuture().join())
                .hasRootCauseInstanceOf(NexusException.class)
                .rootCause()
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.IDEMPOTENCY_CONFLICT.fullCode());
        firstLease.finish(outcome(OutcomeType.SUCCEEDED)).toCompletableFuture().join();
    }

    @Test
    void differentFingerprintWithSameKeyIsConflict() {
        IdempotencyCoordinator coordinator = new IdempotencyCoordinator(true);
        coordinator.enter(context("same-key")).toCompletableFuture().join()
                .finish(outcome(OutcomeType.SUCCEEDED)).toCompletableFuture().join();
        InvocationContext otherResource = new InvocationContext(
                "inv-2",
                "adapter.write",
                serviceContext("same-key"),
                emptyPolicy(),
                new ResourceRef("adapter", "other", ServiceScope.platform()));
        assertThatThrownBy(() -> coordinator.enter(otherResource).toCompletableFuture().join())
                .hasRootCauseInstanceOf(NexusException.class)
                .rootCause()
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.IDEMPOTENCY_CONFLICT.fullCode());
    }

    private static InvocationContext context(String idempotencyKey) {
        return new InvocationContext(
                "inv-1",
                "adapter.write",
                serviceContext(idempotencyKey),
                emptyPolicy(),
                new ResourceRef("adapter", "target", ServiceScope.platform()));
    }

    private static ServiceContext serviceContext(String idempotencyKey) {
        Map<String, List<String>> headers = idempotencyKey == null
                ? Map.of()
                : Map.of("idempotency-key", List.of(idempotencyKey));
        return new ServiceContext(
                "req-idem",
                new RequestMetadata("POST", "/write", "/write", headers, "127.0.0.1", null),
                ServicePrincipal.anonymous("adapter"),
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());
    }

    private static InvocationOutcome outcome(OutcomeType type) {
        return new InvocationOutcome(
                type,
                NexusStatusCode.SUCCESS.fullCode(),
                Instant.now(),
                Duration.ZERO,
                0,
                0);
    }

    private static OperationPolicy emptyPolicy() {
        return new OperationPolicy(
                Set.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                AuditMode.BEST_EFFORT,
                ResponseProfile.LEGACY);
    }
}

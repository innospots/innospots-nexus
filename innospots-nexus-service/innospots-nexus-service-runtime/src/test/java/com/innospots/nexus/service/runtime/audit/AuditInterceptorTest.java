package com.innospots.nexus.service.runtime.audit;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.audit.AuditEvent;
import com.innospots.nexus.service.contract.audit.AuditStorage;
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
import com.innospots.nexus.base.status.NexusStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 审计拦截器 REQUIRED 拒绝与 BEST_EFFORT 分发测试。
 */
class AuditInterceptorTest {

    @Test
    void requiredWithoutTransactionalStorageRejectsBeforeBusiness() {
        AuditDispatcher dispatcher = new AuditDispatcher(noopStorage(), Optional.empty(), 8);
        AuditInterceptor interceptor = new AuditInterceptor(dispatcher, true);
        InvocationContext context = auditedContext(AuditMode.REQUIRED);

        assertThatThrownBy(() -> interceptor.enter(context).toCompletableFuture().join())
                .hasRootCauseInstanceOf(NexusException.class)
                .rootCause()
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.AUDIT_UNAVAILABLE.fullCode());
    }

    @Test
    void bestEffortDispatchesOnFinish() throws Exception {
        AtomicInteger appended = new AtomicInteger();
        AuditStorage storage = new AuditStorage() {
            @Override
            public CompletionStage<Void> append(AuditEvent event) {
                appended.incrementAndGet();
                assertThat(event.action()).isEqualTo("ORDER_CREATE");
                assertThat(event.result()).isEqualTo("SUCCEEDED");
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletionStage<Void> flush(Duration timeout) {
                return CompletableFuture.completedFuture(null);
            }
        };
        AuditDispatcher dispatcher = new AuditDispatcher(storage, Optional.empty(), 8);
        AuditInterceptor interceptor = new AuditInterceptor(dispatcher, true);
        InvocationContext context = auditedContext(AuditMode.BEST_EFFORT);
        var lease = interceptor.enter(context).toCompletableFuture().join();
        lease.finish(successOutcome()).toCompletableFuture().join();
        assertThat(appended).hasValue(1);
    }

    private static InvocationContext auditedContext(AuditMode mode) {
        ServiceContext service = new ServiceContext(
                "req-audit",
                new RequestMetadata("POST", "/orders", "/orders", Map.of(), "127.0.0.1", null),
                ServicePrincipal.anonymous("adapter"),
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());
        OperationPolicy policy = new OperationPolicy(
                Set.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                "ORDER_CREATE",
                "order",
                null,
                mode,
                ResponseProfile.LEGACY);
        return new InvocationContext(
                "inv-1",
                "order.create",
                service,
                policy,
                new ResourceRef("order", "order-1", ServiceScope.platform()));
    }

    private static InvocationOutcome successOutcome() {
        Instant finished = Instant.now();
        return new InvocationOutcome(
                OutcomeType.SUCCEEDED,
                NexusStatusCode.SUCCESS.fullCode(),
                finished,
                Duration.ZERO,
                0,
                0);
    }

    private static AuditStorage noopStorage() {
        return new AuditStorage() {
            @Override
            public CompletionStage<Void> append(AuditEvent event) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletionStage<Void> flush(Duration timeout) {
                return CompletableFuture.completedFuture(null);
            }
        };
    }
}

package com.innospots.nexus.service.runtime.audit;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.audit.AuditEvent;
import com.innospots.nexus.service.contract.audit.AuditStorage;
import com.innospots.nexus.service.contract.policy.AuditMode;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 审计分发器尽力而为丢弃与必需模式拒绝测试。
 */
class AuditDispatcherTest {

    @Test
    void bestEffortDropsWhenQueueCapacityExceeded() {
        AtomicInteger appended = new AtomicInteger();
        AuditStorage slowStorage = new AuditStorage() {
            @Override
            public CompletionStage<Void> append(AuditEvent event) {
                appended.incrementAndGet();
                return new CompletableFuture<>();
            }

            @Override
            public CompletionStage<Void> flush(Duration timeout) {
                return CompletableFuture.completedFuture(null);
            }
        };
        AuditDispatcher dispatcher = new AuditDispatcher(slowStorage, Optional.empty(), 1);
        AuditEvent event = sampleEvent("evt-1");

        dispatcher.dispatch(AuditMode.BEST_EFFORT, event);
        dispatcher.dispatch(AuditMode.BEST_EFFORT, sampleEvent("evt-2")).toCompletableFuture().join();
        dispatcher.dispatch(AuditMode.BEST_EFFORT, sampleEvent("evt-3")).toCompletableFuture().join();

        assertThat(appended).hasValue(1);
        assertThat(dispatcher.droppedCount()).isEqualTo(2);
    }

    @Test
    void requiredWithoutTransactionalStorageIsRejected() {
        AuditDispatcher dispatcher = new AuditDispatcher(noopStorage(), Optional.empty(), 8);

        assertThatThrownBy(() -> dispatcher.dispatch(AuditMode.REQUIRED, sampleEvent("evt-required")).toCompletableFuture().join())
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.AUDIT_UNAVAILABLE.fullCode());
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

    private static AuditEvent sampleEvent(String eventId) {
        return new AuditEvent(
                eventId,
                Instant.now(),
                new ServicePrincipal("user-1", PrincipalType.USER, "local", null, null, null),
                ServiceScope.platform(),
                "ORDER_CREATE",
                "order",
                "order-1",
                "req-1",
                "",
                "SUCCEEDED",
                Map.of(),
                Map.of(),
                Map.of());
    }
}

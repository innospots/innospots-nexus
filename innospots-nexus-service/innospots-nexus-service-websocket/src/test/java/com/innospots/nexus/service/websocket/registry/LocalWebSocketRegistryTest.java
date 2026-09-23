package com.innospots.nexus.service.websocket.registry;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;
import com.innospots.nexus.service.runtime.context.ContextSnapshot;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.ConnectionState;
import com.innospots.nexus.service.websocket.session.WebSocketClose;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 本地 WebSocket 注册表隔离、广播计数与关闭授权测试。
 */
class LocalWebSocketRegistryTest {

    private static final String MESSAGE_TYPE = "notify";
    private static final Set<String> OUTPUT_TYPES = Set.of(MESSAGE_TYPE);

    private final ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();
    private final LocalWebSocketRegistry registry = LocalWebSocketRegistry.builder()
            .contexts(contexts)
            .build();
    private ContextSnapshot snapshot;

    @AfterEach
    void clearContext() {
        if (snapshot != null) {
            contexts.restore(snapshot);
            snapshot = null;
        }
    }

    @Test
    void rejectsCrossTenantSend() {
        ServiceScope tenantOne = new ServiceScope("tenant-1", null, null);
        ServiceScope tenantTwo = new ServiceScope("tenant-2", null, null);
        registry.register(binding("conn-1", "session-1", user("owner-1"), tenantOne, false, false));
        install(user("caller-2"), tenantTwo);

        WebSocketMessage<String> message = message("msg-1");

        assertThatThrownBy(() -> registry.send("conn-1", message))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
    }

    @Test
    void sendToSessionCountsPartialFailures() {
        ServiceScope scope = new ServiceScope("tenant-1", "ws-1", null);
        registry.register(binding("conn-ok", "session-1", user("owner-1"), scope, false, false));
        registry.register(binding("conn-fail", "session-1", user("owner-1"), scope, true, false));
        install(user("owner-1"), scope);

        BroadcastResult result = registry.sendToSession("session-1", message("msg-2"))
                .toCompletableFuture()
                .join();

        assertThat(result.attemptedCount()).isEqualTo(2);
        assertThat(result.sentCount()).isEqualTo(1);
        assertThat(result.unavailableCount()).isEqualTo(1);
        assertThat(result.rejectedCount()).isZero();
    }

    @Test
    void closeRequiresConnectionOwner() {
        ServiceScope scope = new ServiceScope("tenant-1", "ws-1", null);
        registry.register(binding("conn-1", "session-1", user("owner-1"), scope, false, false));
        install(user("intruder"), scope);

        assertThatThrownBy(() -> registry.close("conn-1", new WebSocketClose(1000, "bye")))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
    }

    @Test
    void closeMissingConnectionIsIdempotent() {
        install(user("owner-1"), new ServiceScope("tenant-1", null, null));

        registry.close("missing", new WebSocketClose(1000, "bye")).toCompletableFuture().join();

        assertThat(registry.activeConnections()).isZero();
    }

    private void install(ServicePrincipal principal, ServiceScope scope) {
        ServiceContext context = new ServiceContext(
                "req-" + principal.id(),
                new RequestMetadata("POST", "/ws", "/ws", Map.of(), "127.0.0.1", null),
                principal,
                scope,
                TraceSnapshot.empty(),
                com.innospots.nexus.service.contract.cancellation.CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());
        snapshot = contexts.install(context);
    }

    private static ServicePrincipal user(String userId) {
        return new ServicePrincipal(userId, PrincipalType.USER, "local", null, null, null);
    }

    private static WebSocketMessage<String> message(String id) {
        return new WebSocketMessage<>(
                id,
                MESSAGE_TYPE,
                "",
                1L,
                Instant.parse("2026-09-15T10:00:00Z"),
                "payload",
                Map.of());
    }

    private static WebSocketRegistry.WebSocketConnectionBinding binding(
            String connectionId,
            String sessionId,
            ServicePrincipal principal,
            ServiceScope scope,
            boolean failDeliver,
            boolean failClose) {
        return new WebSocketRegistry.WebSocketConnectionBinding() {
            @Override
            public String connectionId() {
                return connectionId;
            }

            @Override
            public String sessionId() {
                return sessionId;
            }

            @Override
            public String realm() {
                return principal.realm();
            }

            @Override
            public ServicePrincipal principal() {
                return principal;
            }

            @Override
            public ServiceScope scope() {
                return scope;
            }

            @Override
            public ConnectionState state() {
                return ConnectionState.OPEN;
            }

            @Override
            public Set<String> allowedOutputTypes() {
                return OUTPUT_TYPES;
            }

            @Override
            public CompletionStage<Void> deliver(WebSocketMessage<?> message) {
                if (failDeliver) {
                    return CompletableFuture.failedFuture(new IllegalStateException("deliver failed"));
                }
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletionStage<Void> close(WebSocketClose close) {
                if (failClose) {
                    return CompletableFuture.failedFuture(new IllegalStateException("close failed"));
                }
                return CompletableFuture.completedFuture(null);
            }
        };
    }
}

package com.innospots.nexus.service.websocket.message;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;
import com.innospots.nexus.service.websocket.session.WebSocketContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebSocket 消息 envelope 与上下文契约测试。
 */
class WebSocketMessageContractsTest {

    @Test
    void envelopeExposesRequiredFields() {
        Instant timestamp = Instant.parse("2026-09-15T10:00:00Z");
        WebSocketMessage<String> message = new WebSocketMessage<>(
                "msg-1",
                "chat.message",
                "corr-1",
                7L,
                timestamp,
                "hello",
                Map.of("channel", "general"));

        assertThat(message.id()).isEqualTo("msg-1");
        assertThat(message.type()).isEqualTo("chat.message");
        assertThat(message.correlationId()).isEqualTo("corr-1");
        assertThat(message.sequence()).isEqualTo(7L);
        assertThat(message.timestamp()).isEqualTo(timestamp);
        assertThat(message.data()).isEqualTo("hello");
        assertThat(message.metadata()).containsEntry("channel", "general");
    }

    @Test
    void contextKeepsConnectionAndSessionIdentifiersDistinct() {
        ServiceContext service = new ServiceContext(
                "req-1",
                new RequestMetadata("GET", "/ws/chat", "/ws/chat", Map.of(), "127.0.0.1", null),
                new ServicePrincipal("user-1", PrincipalType.USER, "local", null, null, null),
                com.innospots.nexus.service.contract.security.ServiceScope.platform(),
                TraceSnapshot.empty(),
                com.innospots.nexus.service.contract.cancellation.CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());

        WebSocketContext context = new WebSocketContext("conn-physical-1", "session-business-9", service);

        assertThat(context.connectionId()).isEqualTo("conn-physical-1");
        assertThat(context.sessionId()).isEqualTo("session-business-9");
        assertThat(context.connectionId()).isNotEqualTo(context.sessionId());
        assertThat(context.service()).isSameAs(service);
    }
}

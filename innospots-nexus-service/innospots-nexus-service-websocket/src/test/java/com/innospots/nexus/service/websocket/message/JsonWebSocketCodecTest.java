package com.innospots.nexus.service.websocket.message;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.invocation.ExecutionMode;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JSON WebSocket 编解码错误路径测试。
 */
class JsonWebSocketCodecTest {

    private static final MessageDescriptor CHAT_MESSAGE = new MessageDescriptor(
            "chat.message",
            String.class,
            String.class,
            Set.of(),
            null,
            ExecutionMode.BLOCKING,
            FrameType.TEXT,
            null,
            null,
            null,
            null);

    @Test
    void unknownTypeReturnsMessageTypeUnknown() {
        JsonWebSocketCodec codec = JsonWebSocketCodec.builder()
                .descriptors(Map.of(CHAT_MESSAGE.type(), CHAT_MESSAGE))
                .maxMessageBytes(4096)
                .build();
        ByteBuffer frame = ByteBuffer.wrap("""
                {"id":"1","type":"unknown.type","sequence":1,"timestamp":"2026-09-15T10:00:00Z","data":"x"}
                """.strip().getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> codec.decode(frame, String.class))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.MESSAGE_TYPE_UNKNOWN.fullCode());
    }

    @Test
    void oversizedFrameReturnsPayloadTooLarge() {
        JsonWebSocketCodec codec = JsonWebSocketCodec.builder()
                .descriptors(Map.of(CHAT_MESSAGE.type(), CHAT_MESSAGE))
                .maxMessageBytes(32)
                .build();
        ByteBuffer frame = ByteBuffer.wrap("""
                {"id":"1","type":"chat.message","sequence":1,"timestamp":"2026-09-15T10:00:00Z","data":"payload"}
                """.strip().getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> codec.decode(frame, String.class))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.PAYLOAD_TOO_LARGE.fullCode());
    }

    @Test
    void roundTripEncodesRegisteredType() {
        JsonWebSocketCodec codec = JsonWebSocketCodec.builder()
                .descriptors(Map.of(CHAT_MESSAGE.type(), CHAT_MESSAGE))
                .build();
        WebSocketMessage<String> message = new WebSocketMessage<>(
                "msg-1",
                CHAT_MESSAGE.type(),
                "corr-1",
                3L,
                Instant.parse("2026-09-15T10:00:00Z"),
                "hello",
                Map.of());

        ByteBuffer encoded = codec.encode(message);
        WebSocketMessage<?> decoded = codec.decode(encoded, String.class);

        assertThat(decoded.id()).isEqualTo("msg-1");
        assertThat(decoded.type()).isEqualTo(CHAT_MESSAGE.type());
        assertThat(decoded.data()).isEqualTo("hello");
    }
}

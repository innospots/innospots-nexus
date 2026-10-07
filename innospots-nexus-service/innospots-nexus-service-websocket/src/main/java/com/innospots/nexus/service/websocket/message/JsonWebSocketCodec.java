package com.innospots.nexus.service.websocket.message;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * 基于 JSON 的 {@link WebSocketCodec} 实现。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class JsonWebSocketCodec implements WebSocketCodec {

    private final long maxMessageBytes;
    private final Map<String, MessageDescriptor> descriptorsByType;
    private final ObjectMapper mapper;

    private JsonWebSocketCodec(Builder builder) {
        this.maxMessageBytes = builder.maxMessageBytes;
        this.descriptorsByType = Map.copyOf(builder.descriptorsByType);
        this.mapper = builder.mapper;
    }

    /**
     * 返回构建器。
     *
     * @return 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 返回已注册消息类型。
     *
     * @return 类型集合
     */
    public Set<String> registeredTypes() {
        return descriptorsByType.keySet();
    }

    /**
     * 返回已注册类型的描述符，供入站治理派发使用。
     *
     * @param type 消息类型
     * @return 描述符
     */
    public MessageDescriptor descriptor(String type) {
        Checks.notBlank(type, "type");
        MessageDescriptor descriptor = descriptorsByType.get(type);
        if (descriptor == null) {
            throw NexusException.build(ServiceStatusCode.MESSAGE_TYPE_UNKNOWN);
        }
        return descriptor;
    }

    @Override
    public WebSocketMessage<?> decode(ByteBuffer frame, Type payloadType) {
        Checks.notNull(frame, "frame");
        int size = frame.remaining();
        if (size > maxMessageBytes) {
            throw NexusException.build(ServiceStatusCode.PAYLOAD_TOO_LARGE);
        }
        byte[] bytes = new byte[size];
        frame.get(bytes);
        String json = new String(bytes, StandardCharsets.UTF_8);
        Map<String, Object> envelope = Jsons.toMap(json);
        String type = stringValue(envelope.get("type"));
        if (type == null || type.isBlank()) {
            throw NexusException.build(ServiceStatusCode.MESSAGE_TYPE_UNKNOWN);
        }
        MessageDescriptor descriptor = descriptorsByType.get(type);
        if (descriptor == null) {
            throw NexusException.build(ServiceStatusCode.MESSAGE_TYPE_UNKNOWN);
        }
        Type resolvedType = descriptor.inputType() != null ? descriptor.inputType() : payloadType;
        Object data = convertData(envelope.get("data"), resolvedType);
        return new WebSocketMessage<>(
                requireString(envelope, "id"),
                type,
                stringValue(envelope.get("correlationId")),
                longValue(envelope.get("sequence")),
                instantValue(envelope.get("timestamp")),
                data,
                stringMap(envelope.get("metadata")));
    }

    @Override
    public ByteBuffer encode(WebSocketMessage<?> message) {
        Checks.notNull(message, "message");
        MessageDescriptor descriptor = descriptorsByType.get(message.type());
        if (descriptor == null) {
            throw NexusException.build(ServiceStatusCode.MESSAGE_TYPE_UNKNOWN);
        }
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("id", message.id());
        envelope.put("type", message.type());
        envelope.put("correlationId", message.correlationId());
        envelope.put("sequence", message.sequence());
        envelope.put("timestamp", message.timestamp());
        envelope.put("data", message.data());
        envelope.put("metadata", message.metadata());
        String json = Jsons.toJson(envelope);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > maxMessageBytes) {
            throw NexusException.build(ServiceStatusCode.PAYLOAD_TOO_LARGE);
        }
        return ByteBuffer.wrap(bytes);
    }

    private Object convertData(Object raw, Type targetType) {
        if (raw == null || targetType == null) {
            return raw;
        }
        if (targetType instanceof Class<?> clazz && clazz.isInstance(raw)) {
            return raw;
        }
        JavaType javaType = mapper.getTypeFactory().constructType(targetType);
        return mapper.convertValue(raw, javaType);
    }

    private static String requireString(Map<String, Object> envelope, String field) {
        String value = stringValue(envelope.get(field));
        if (value == null || value.isBlank()) {
            throw NexusException.build(ServiceStatusCode.MESSAGE_TYPE_UNKNOWN);
        }
        return value;
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        return Objects.toString(value);
    }

    private static long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }

    private static Instant instantValue(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value == null) {
            return Instant.now();
        }
        return Instant.parse(value.toString());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> stringMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        if (!(value instanceof Map<?, ?> raw)) {
            return Map.of();
        }
        Map<String, String> mapped = new LinkedHashMap<>();
        raw.forEach((key, entry) -> mapped.put(Objects.toString(key), Objects.toString(entry, "")));
        return Map.copyOf(mapped);
    }

    /**
     * 构建器。
     */
    public static final class Builder {

        private long maxMessageBytes = 1024L * 1024L;
        private Map<String, MessageDescriptor> descriptorsByType = Map.of();
        private ObjectMapper mapper = Jsons.mapper();

        private Builder() {
        }

        public Builder maxMessageBytes(long maxMessageBytes) {
            this.maxMessageBytes = maxMessageBytes;
            return this;
        }

        public Builder descriptors(Map<String, MessageDescriptor> descriptorsByType) {
            this.descriptorsByType = Checks.notNull(descriptorsByType, "descriptorsByType");
            return this;
        }

        public Builder mapper(ObjectMapper mapper) {
            this.mapper = Checks.notNull(mapper, "mapper");
            return this;
        }

        public JsonWebSocketCodec build() {
            Checks.isTrue(maxMessageBytes > 0, "maxMessageBytes must be positive");
            return new JsonWebSocketCodec(this);
        }
    }
}

package com.innospots.nexus.spring.service.test.support;

import java.util.Map;
import java.util.Objects;

import com.innospots.nexus.base.json.Jsons;

/**
 * 从 SSE {@code data} 行解析 NDJSON {@link com.innospots.nexus.service.stream.event.StreamEvent} 载荷。
 */
public final class StreamEventSseParser {

    private StreamEventSseParser() {
    }

    /**
     * 读取 envelope 中的 {@code data} 节点并转为目标类型。
     *
     * @param sseDataJson SSE data 行（完整 StreamEvent JSON）
     * @param payloadType 业务载荷类型
     * @param <T>         载荷类型
     * @return 业务载荷
     */
    public static <T> T parsePayload(String sseDataJson, Class<T> payloadType) {
        Objects.requireNonNull(sseDataJson, "sseDataJson");
        Objects.requireNonNull(payloadType, "payloadType");
        Map<String, Object> envelope = Jsons.toMap(sseDataJson);
        Object data = envelope.get("data");
        return Jsons.mapper().convertValue(data, payloadType);
    }
}

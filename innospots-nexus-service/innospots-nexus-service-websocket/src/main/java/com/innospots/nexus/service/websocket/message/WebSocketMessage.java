package com.innospots.nexus.service.websocket.message;

import java.time.Instant;
import java.util.Map;

import com.innospots.nexus.base.util.Checks;

/**
 * WebSocket 消息 envelope。
 *
 * @param <T> 载荷类型
 * @param id              消息标识
 * @param type            消息类型
 * @param correlationId   关联标识
 * @param sequence        连接方向序号
 * @param timestamp       时间戳
 * @param data            载荷
 * @param metadata        元数据
 * @author Smars
 * @date 2026/09/15
 */
public record WebSocketMessage<T>(
        String id,
        String type,
        String correlationId,
        long sequence,
        Instant timestamp,
        T data,
        Map<String, String> metadata
) {

    public WebSocketMessage {
        Checks.notBlank(id, "id");
        Checks.notBlank(type, "type");
        correlationId = correlationId == null ? "" : correlationId;
        Checks.notNull(timestamp, "timestamp");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}

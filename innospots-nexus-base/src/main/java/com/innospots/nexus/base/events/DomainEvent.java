package com.innospots.nexus.base.events;

import java.time.Instant;
import java.util.UUID;

/**
 * 所有领域事件的基础接口。每个事件拥有唯一 ID、类型标识符和发生时间戳。
 *
 * @author Smars
 * @date 2026/09/13
 * @see EventBus
 * @see EventHandler
 */
public interface DomainEvent {

    /**
     * 返回事件唯一标识（默认随机 UUID）。
     *
     * @return 事件 ID
     */
    default String eventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 返回事件类型标识符。
     *
     * @return 事件类型
     */
    String eventType();

    /**
     * 返回事件发生时间（默认为当前时刻）。
     *
     * @return 发生时间
     */
    default Instant occurredAt() {
        return Instant.now();
    }
}

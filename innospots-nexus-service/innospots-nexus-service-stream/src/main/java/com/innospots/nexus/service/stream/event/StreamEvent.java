package com.innospots.nexus.service.stream.event;

import java.time.Instant;

import com.innospots.nexus.base.util.Checks;

/**
 * 流式传输 envelope。
 *
 * @param id        事件标识
 * @param sequence  会话内顺序
 * @param type      事件类型
 * @param timestamp 事件时间
 * @param data      载荷
 * @param <T>       载荷类型
 * @author Smars
 * @date 2026/09/15
 */
public record StreamEvent<T>(String id, long sequence, String type, Instant timestamp, T data) {

    public StreamEvent {
        Checks.notBlank(id, "id");
        Checks.notBlank(type, "type");
        Checks.notNull(timestamp, "timestamp");
    }
}

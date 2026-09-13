package com.innospots.nexus.core.plugin.event;

import java.time.Instant;

/**
 * 不包含异常对象或配置值的插件失败观测事件。
 *
 * @param pluginId 稳定的插件标识
 * @param phase 失败发生的生命周期阶段
 * @param errorCode 脱敏后的状态码
 * @param occurredAt 事件发生时间
 */
public record PluginFailedEvent(
        String pluginId,
        String phase,
        String errorCode,
        Instant occurredAt
) implements PluginEvent {
}

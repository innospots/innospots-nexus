package com.innospots.nexus.core.plugin.event;

import java.time.Instant;

/**
 * 资源释放后发布的插件停止观测事件。
 *
 * @param pluginId 稳定的插件标识
 * @param occurredAt 事件发生时间
 */
public record PluginStoppedEvent(String pluginId, Instant occurredAt) implements PluginEvent {
}

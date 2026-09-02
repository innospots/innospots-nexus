package com.innospots.nexus.core.plugin.event;

import java.time.Instant;
import java.util.List;

import com.innospots.nexus.core.plugin.capability.CapabilityKey;

/**
 * 所有 Capability 对外可见后发布的插件激活观测事件。
 *
 * @param pluginId 稳定的插件标识
 * @param version 插件版本
 * @param capabilities 已发布的 Capability 身份列表
 * @param occurredAt 事件发生时间
 */
public record PluginStartedEvent(
        String pluginId,
        String version,
        List<CapabilityKey> capabilities,
        Instant occurredAt
) implements PluginEvent {

    public PluginStartedEvent {
        capabilities = List.copyOf(capabilities);
    }
}

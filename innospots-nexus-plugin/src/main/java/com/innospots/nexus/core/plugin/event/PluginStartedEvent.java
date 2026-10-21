package com.innospots.nexus.core.plugin.event;

import java.time.Instant;
import java.util.List;

/**
 * 插件激活完成后发布的观测事件。
 *
 * @param pluginId 稳定的插件标识
 * @param version 插件版本
 * @param contributions 已提交的扩展类型标识列表
 * @param occurredAt 事件发生时间
 * @author Smars
 * @date 2026/09/13
 */
public record PluginStartedEvent(
        String pluginId,
        String version,
        List<String> contributions,
        Instant occurredAt
) implements PluginEvent {

    public PluginStartedEvent {
        contributions = List.copyOf(contributions);
    }
}

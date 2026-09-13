package com.innospots.nexus.core.plugin.discovery;

import java.time.Instant;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.declaration.PluginSource;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 一个已发现的插件实例及其缓存的不可变定义。
 *
 * @param plugin 由 ServiceLoader 创建的插件实例
 * @param definition 只读取一次的插件定义快照
 * @param discoveredAt 发现时间
 * @param source 声明来源元数据
 */
public record DiscoveredPlugin(
        Plugin plugin,
        PluginDefinition definition,
        Instant discoveredAt,
        PluginSource source
) {

    /**
     * @throws NexusException 运行时实例、定义、时间戳或来源为空时抛出
     */
    public DiscoveredPlugin {
        if (plugin == null || definition == null || discoveredAt == null || source == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "discovered plugin instance, definition and timestamp are required");
        }
    }

    /**
     * 兼容 Java SPI 旧构造形式，自动补齐 Java 来源。
     *
     * @param plugin 由 ServiceLoader 创建的插件实例
     * @param definition 只读取一次的插件定义快照
     * @param discoveredAt 发现时间
     */
    public DiscoveredPlugin(Plugin plugin, PluginDefinition definition, Instant discoveredAt) {
        this(plugin, definition, discoveredAt, PluginSource.java(plugin.getClass().getName(), discoveredAt));
    }
}

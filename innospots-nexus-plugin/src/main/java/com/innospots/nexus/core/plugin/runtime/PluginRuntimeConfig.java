package com.innospots.nexus.core.plugin.runtime;

import java.util.Map;
import java.util.Set;

import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigSource;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 一个独立插件管理器实例使用的不可变宿主配置。
 *
 * <p>构建后不可变；每个 {@link DefaultPluginManager} 实例持有独立副本。</p>
 *
 * @param requiredPluginIds 宿主启动必须激活的插件
 * @param disabledPluginIds 排除在自动启动之外的插件
 * @param hostConfig 扁平化宿主静态配置
 * @param configSources 宿主动态配置来源；在每次插件启动解析时调用
 * @param runtimeVariables 优先级最高的运行时覆盖值
 * @param defaultRoutes 按 Capability 标识配置的默认标签
 * @param pluginClassLoader ServiceLoader 使用的类加载器；为 null 时使用 Core 类加载器
 */
public record PluginRuntimeConfig(
        Set<String> requiredPluginIds,
        Set<String> disabledPluginIds,
        Map<String, String> hostConfig,
        List<ConfigSource> configSources,
        Map<String, String> runtimeVariables,
        Map<CapabilityKey, Tags> defaultRoutes,
        ClassLoader pluginClassLoader
) {

    /**
     * 使用空动态配置来源的兼容构造形式。
     */
    public PluginRuntimeConfig(
            Set<String> requiredPluginIds,
            Set<String> disabledPluginIds,
            Map<String, String> hostConfig,
            Map<String, String> runtimeVariables,
            Map<CapabilityKey, Tags> defaultRoutes,
            ClassLoader pluginClassLoader
    ) {
        this(requiredPluginIds, disabledPluginIds, hostConfig, List.of(),
                runtimeVariables, defaultRoutes, pluginClassLoader);
    }

    /**
     * @throws NexusException 插件标识、配置映射或路由条目非法，或必需与禁用集合重叠时抛出
     */
    public PluginRuntimeConfig {
        requiredPluginIds = immutablePluginIds(requiredPluginIds, "required");
        disabledPluginIds = immutablePluginIds(disabledPluginIds, "disabled");
        hostConfig = immutableStringMap(hostConfig, "host configuration");
        configSources = immutableConfigSources(configSources);
        runtimeVariables = immutableStringMap(runtimeVariables, "runtime variables");
        defaultRoutes = immutableRoutes(defaultRoutes);
        if (!java.util.Collections.disjoint(requiredPluginIds, disabledPluginIds)) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "required and disabled plugin ids must not overlap");
        }
    }

    /**
     * 返回已配置的类加载器；未配置时返回传入的运行时回退类加载器。
     *
     * @param fallback 未配置插件专用类加载器时使用的回退类加载器
     * @return 实际使用的插件类加载器
     */
    public ClassLoader resolvedClassLoader(ClassLoader fallback) {
        return pluginClassLoader == null ? fallback : pluginClassLoader;
    }

    private static Set<String> immutablePluginIds(Set<String> source, String kind) {
        if (source == null) {
            return Set.of();
        }
        for (String pluginId : source) {
            if (pluginId == null || pluginId.isBlank()) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        kind + " plugin ids must not be blank");
            }
        }
        return Set.copyOf(source);
    }

    private static Map<String, String> immutableStringMap(Map<String, String> source, String kind) {
        if (source == null) {
            return Map.of();
        }
        for (Map.Entry<String, String> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        kind + " keys and values must not be null");
            }
        }
        return Map.copyOf(source);
    }

    private static List<ConfigSource> immutableConfigSources(List<ConfigSource> source) {
        if (source == null) {
            return List.of();
        }
        for (ConfigSource configSource : source) {
            if (configSource == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        "config sources must not contain null entries");
            }
        }
        return List.copyOf(source);
    }

    private static Map<CapabilityKey, Tags> immutableRoutes(Map<CapabilityKey, Tags> source) {
        if (source == null) {
            return Map.of();
        }
        for (Map.Entry<CapabilityKey, Tags> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        "default capability routes must not contain null entries");
            }
        }
        return Map.copyOf(source);
    }
}

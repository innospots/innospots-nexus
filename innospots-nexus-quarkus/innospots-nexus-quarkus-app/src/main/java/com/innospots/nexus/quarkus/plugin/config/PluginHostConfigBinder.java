package com.innospots.nexus.quarkus.plugin.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.config.Config;

/**
 * 将 MicroProfile 配置中的插件键扁平化为 Core 宿主配置映射。
 *
 * @see com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig#hostConfig()
 */
public final class PluginHostConfigBinder {

    /** 插件扁平配置键前缀，对应 {@code plugins.<pluginId>.<key>}。 */
    public static final String PLUGIN_PREFIX = "plugins.";

    private PluginHostConfigBinder() {
    }

    /**
     * 枚举配置中所有以 {@link #PLUGIN_PREFIX} 开头的属性。
     *
     * @param config MicroProfile 配置
     * @return 不可变扁平配置映射
     */
    public static Map<String, String> flattenPluginConfig(Config config) {
        Map<String, String> hostConfig = new LinkedHashMap<>();
        for (String name : config.getPropertyNames()) {
            if (!name.startsWith(PLUGIN_PREFIX)) {
                continue;
            }
            // 仅收集已解析为字符串的键，忽略未定义占位
            config.getOptionalValue(name, String.class)
                    .ifPresent(value -> hostConfig.put(name, value));
        }
        return Map.copyOf(hostConfig);
    }
}

package com.innospots.nexus.spring.plugin.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * 将 Spring 配置环境中的插件键扁平化为 Core 宿主配置映射。
 *
 * <p>Core 不直接读取 Spring 配置文件；宿主在启动时汇总 {@code plugins.*} 后传入
 * {@link com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig#hostConfig()}。</p>
 */
public final class PluginHostConfigBinder {

    /** 插件扁平配置键前缀，对应 {@code plugins.<pluginId>.<key>}。 */
    public static final String PLUGIN_PREFIX = "plugins.";

    private PluginHostConfigBinder() {
    }

    /**
     * 枚举环境中所有以 {@link #PLUGIN_PREFIX} 开头的属性。
     *
     * <p>遍历可枚举的 {@link PropertySource}，跳过 Map 等非枚举来源；
     * 保留键的原始扁平形式供 Core 按插件 ID 解析。</p>
     *
     * @param environment Spring 环境
     * @return 不可变扁平配置映射
     */
    public static Map<String, String> flattenPluginConfig(Environment environment) {
        Map<String, String> hostConfig = new LinkedHashMap<>();
        if (!(environment instanceof ConfigurableEnvironment configurableEnvironment)) {
            // 非可配置环境无法枚举属性源，返回空映射
            return Map.copyOf(hostConfig);
        }
        for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
            if (!(propertySource instanceof EnumerablePropertySource<?> enumerable)) {
                // 例如 Servlet 上下文属性源不可枚举键名
                continue;
            }
            for (String name : enumerable.getPropertyNames()) {
                if (!name.startsWith(PLUGIN_PREFIX)) {
                    continue;
                }
                String value = environment.getProperty(name);
                if (value != null) {
                    hostConfig.put(name, value);
                }
            }
        }
        return Map.copyOf(hostConfig);
    }
}

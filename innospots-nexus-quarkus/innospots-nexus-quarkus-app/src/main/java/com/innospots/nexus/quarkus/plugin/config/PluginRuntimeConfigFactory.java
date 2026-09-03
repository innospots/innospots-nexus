package com.innospots.nexus.quarkus.plugin.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;

import com.innospots.nexus.core.plugin.config.ConfigSource;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

/**
 * 组装 {@link PluginRuntimeConfig} 的工厂 Bean。
 *
 * <p>合并宿主策略、{@code plugins.*} 扁平配置与可选 {@link ConfigSource} 列表。</p>
 */
@ApplicationScoped
public class PluginRuntimeConfigFactory {

    private final NexusPluginHostConfig hostConfig;
    private final Config config;
    private final Instance<ConfigSource> configSources;

    /**
     * @param hostConfig    宿主安装/启停策略
     * @param config        MicroProfile 配置，用于枚举 {@code plugins.*}
     * @param configSources 可选动态配置来源；无 Bean 时为空列表
     */
    @Inject
    public PluginRuntimeConfigFactory(
            NexusPluginHostConfig hostConfig,
            Config config,
            Instance<ConfigSource> configSources) {
        this.hostConfig = hostConfig;
        this.config = config;
        this.configSources = configSources;
    }

    /**
     * 创建不可变插件运行时配置。
     *
     * <p>每次插件宿主启动时调用；运行时覆盖与默认路由在 app 模块保持空映射。</p>
     *
     * @return 插件运行时配置
     */
    public PluginRuntimeConfig create() {
        return new PluginRuntimeConfig(
                Set.copyOf(hostConfig.plugins().required().orElseGet(List::of)),
                Set.copyOf(hostConfig.plugins().disabled().orElseGet(List::of)),
                PluginHostConfigBinder.flattenPluginConfig(config),
                configSources.stream().toList(),
                Map.of(),
                Map.of(),
                null);
    }
}

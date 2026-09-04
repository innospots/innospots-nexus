package com.innospots.nexus.spring.plugin.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import com.innospots.nexus.core.plugin.config.ConfigSource;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

/**
 * 应用服务插件宿主 Spring 装配。
 *
 * <p>由 {@link EnableNexusPluginHost} 显式引入。
 * Contribution 相关 Bean 由 console 模块可选注入；未引入时使用空注册表。
 * 插件子系统在 {@link com.innospots.nexus.spring.bootstrap.NexusStartupConfiguration}
 * 组装的 {@link com.innospots.nexus.core.bootstrap.NexusStartup} 中启用。</p>
 *
 * @see PluginHostProperties
 * @see PluginHostConfigBinder
 */
@Configuration
@EnableConfigurationProperties(PluginHostProperties.class)
public class PluginHostConfiguration {

    /**
     * 组装 Core 运行时配置 Bean。
     *
     * @param properties    宿主安装/启停策略
     * @param environment   Spring 环境，用于枚举 {@code plugins.*} 键
     * @param configSources 动态配置来源；无 Bean 时注入空列表
     * @return 不可变插件运行时配置
     */
    @Bean
    PluginRuntimeConfig pluginRuntimeConfig(
            PluginHostProperties properties,
            Environment environment,
            List<ConfigSource> configSources) {
        return new PluginRuntimeConfig(
                Set.copyOf(properties.getPlugins().getRequired()),
                Set.copyOf(properties.getPlugins().getDisabled()),
                PluginHostConfigBinder.flattenPluginConfig(environment),
                configSources,
                Map.of(),
                Map.of(),
                null);
    }

    /**
     * 持有启动编排完成后 enable 的安装管理器。
     *
     * @return 进程级安装管理器持有器
     */
    @Bean(destroyMethod = "close")
    PluginInstallationManagerHolder pluginInstallationManagerHolder() {
        return new PluginInstallationManagerHolder();
    }

    /**
     * 延迟暴露已 enable 的安装管理器，避免在启动编排执行前物化依赖方 Bean。
     *
     * @param holder 安装管理器持有器
     * @return 已启用的安装管理器
     */
    @Bean
    @Lazy
    PluginInstallationManager pluginInstallationManager(PluginInstallationManagerHolder holder) {
        return holder.requireManager();
    }
}

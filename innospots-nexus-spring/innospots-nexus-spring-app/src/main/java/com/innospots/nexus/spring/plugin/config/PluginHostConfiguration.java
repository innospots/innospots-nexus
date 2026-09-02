package com.innospots.nexus.spring.plugin.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.innospots.nexus.core.plugin.config.ConfigSource;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrap;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

/**
 * 应用服务插件宿主 Spring 装配。
 *
 * <p>由 {@link EnableNexusPluginHost} 显式引入。
 * Contribution 相关 Bean 由 console 模块可选注入；未引入时使用空注册表。</p>
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
     * 启用插件子系统并暴露安装管理器 Bean。
     *
     * <p>容器关闭时调用 {@link PluginInstallationManager#close()} 释放插件运行时。</p>
     *
     * @param installationDao         安装表 DAO
     * @param runtimeConfig           插件运行时配置
     * @param properties              宿主安装策略
     * @param contributionDecoders    可选 YAML Contribution 解码表
     * @param contributionHandlers    运行时 Contribution 处理器列表
     * @param contributionSnapshotters  可选对账快照序列化表
     * @return 已 enable 的安装管理器
     */
    @Bean(destroyMethod = "close")
    PluginInstallationManager pluginInstallationManager(
            PluginInstallationDao installationDao,
            PluginRuntimeConfig runtimeConfig,
            PluginHostProperties properties,
            ObjectProvider<PluginContributionDecoderRegistry> contributionDecoders,
            List<PluginContributionHandler<?>> contributionHandlers,
            ObjectProvider<PluginContributionSnapshotterRegistry> contributionSnapshotters) {
        // console 模块未装配时使用空表，仍允许纯 Java SPI 插件运行
        PluginContributionDecoderRegistry decoderRegistry = contributionDecoders.getIfAvailable(
                () -> PluginContributionDecoderRegistry.builder().build());
        PluginContributionSnapshotterRegistry snapshotterRegistry = contributionSnapshotters.getIfAvailable(
                () -> PluginContributionSnapshotterRegistry.builder().build());
        PluginInstallationConfig installationConfig =
                new PluginInstallationConfig(properties.getPlugin().isAutoInstall());
        return PluginHostBootstrap.enable(new PluginHostBootstrapRequest(
                installationDao,
                runtimeConfig,
                installationConfig,
                decoderRegistry,
                contributionHandlers,
                snapshotterRegistry,
                null));
    }
}

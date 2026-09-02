package com.innospots.nexus.spring.plugin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.plugin.endpoint.PluginManagementEndpoint;

/**
 * 管理控制台插件 REST 装配。
 *
 * <p>由 {@link com.innospots.nexus.spring.console.EnableNexusConsole} 显式引入。
 * 依赖 {@link PluginInstallationManagerHolder} 中已 enable 的安装管理器。</p>
 */
@Configuration
public class PluginConsoleConfiguration {

    /**
     * 插件安装/启停管理 REST 端点。
     *
     * <p>延迟初始化，确保在 {@link PluginHostBootstrapRunner} 完成后再绑定管理器。</p>
     *
     * @param managerHolder 安装管理器持有器
     * @return Jakarta REST 管理端点实例
     */
    @Bean
    @Lazy
    PluginManagementEndpoint pluginManagementEndpoint(PluginInstallationManagerHolder managerHolder) {
        return new PluginManagementEndpoint(managerHolder.requireManager());
    }
}

package com.innospots.nexus.spring.plugin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.plugin.endpoint.PluginManagementEndpoint;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;

/**
 * 管理控制台插件 REST 装配。
 *
 * <p>由 {@link com.innospots.nexus.spring.console.EnableNexusConsole} 显式引入。
 * 依赖同上下文中的 {@link PluginInstallationManager}。</p>
 */
@Configuration
public class PluginConsoleConfiguration {

    /**
     * 插件安装/启停管理 REST 端点。
     *
     * @param installationManager 已 enable 的插件安装管理器
     * @return Jakarta REST 管理端点实例
     */
    @Bean
    PluginManagementEndpoint pluginManagementEndpoint(PluginInstallationManager installationManager) {
        return new PluginManagementEndpoint(installationManager);
    }
}

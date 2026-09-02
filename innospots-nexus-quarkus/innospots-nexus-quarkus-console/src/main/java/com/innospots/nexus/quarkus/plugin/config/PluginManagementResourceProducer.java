package com.innospots.nexus.quarkus.plugin.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.innospots.nexus.console.plugin.endpoint.PluginManagementEndpoint;

/**
 * 将 {@link PluginManagementEndpoint} 注册为 CDI Bean。
 *
 * <p>使用 {@link Dependent} 作用域，在首次注入时从
 * {@link PluginInstallationManagerHolder} 获取已启动的管理器，
 * 避免在 {@link io.quarkus.runtime.StartupEvent} 之前物化 REST 资源。</p>
 */
@ApplicationScoped
public class PluginManagementResourceProducer {

    private final PluginInstallationManagerHolder managerHolder;

    /**
     * @param managerHolder 安装管理器持有器
     */
    @Inject
    public PluginManagementResourceProducer(PluginInstallationManagerHolder managerHolder) {
        this.managerHolder = managerHolder;
    }

    /**
     * 插件安装/启停管理 REST 端点 Bean。
     *
     * @return 绑定 {@code /console/plugins} 的 Jakarta REST 资源
     */
    @Produces
    @Dependent
    PluginManagementEndpoint pluginManagementEndpoint() {
        return new PluginManagementEndpoint(managerHolder.requireManager());
    }
}

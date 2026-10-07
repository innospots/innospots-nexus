package com.innospots.nexus.quarkus.console.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import io.quarkus.arc.Unremovable;

import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.console.plugin.converter.PluginManagementConverter;
import com.innospots.nexus.console.plugin.endpoint.PluginManagementEndpoint;
import com.innospots.nexus.quarkus.plugin.config.PluginInstallationManagerHolder;

/**
 * 控制台需特殊构造的 REST 资源生产者。
 *
 * <p>多数 {@code @Path} 资源由 Quarkus REST 直接作为 CDI Bean 注册；仅插件管理需在运行时从
 * {@link PluginInstallationManagerHolder} 解析管理器，并注入目录同步服务。</p>
 */
@ApplicationScoped
public class ConsoleResourceProducers {

    private final PluginInstallationManagerHolder managerHolder;
    private final ConsoleCatalogSyncService syncService;

    @Inject
    public ConsoleResourceProducers(
            PluginInstallationManagerHolder managerHolder,
            ConsoleCatalogSyncService syncService) {
        this.managerHolder = managerHolder;
        this.syncService = syncService;
    }

    @Produces
    @Dependent
    @Unremovable
    PluginManagementEndpoint pluginManagementEndpoint() {
        return new PluginManagementEndpoint(
                managerHolder.requireManager(),
                PluginManagementConverter.INSTANCE,
                syncService);
    }
}

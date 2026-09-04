package com.innospots.nexus.quarkus.console.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.innospots.nexus.console.catalog.endpoint.ConsoleCatalogEndpoint;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.core.plugin.contribution.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.console.navigation.endpoint.NavigationMenuEndpoint;
import com.innospots.nexus.console.navigation.service.NavigationMenuAssembler;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.plugin.converter.PluginManagementConverter;
import com.innospots.nexus.console.plugin.endpoint.PluginManagementEndpoint;
import com.innospots.nexus.quarkus.plugin.config.PluginInstallationManagerHolder;

/**
 * 控制台 catalog / navigation / plugin REST 资源生产者。
 */
@ApplicationScoped
public class ConsoleResourceProducers {

    private final PluginInstallationManagerHolder managerHolder;
    private final ConsoleCatalogService catalogService;
    private final ConsoleCatalogSyncService syncService;
    private final NavigationMenuAssembler navigationMenuAssembler;
    private final AuthorizationSubjectResolver subjectResolver;

    /**
     * @param managerHolder           安装管理器持有器
     * @param catalogService          目录树服务
     * @param syncService             目录同步服务
     * @param navigationMenuAssembler 导航组装器
     * @param subjectResolver         鉴权主体解析器
     */
    @Inject
    public ConsoleResourceProducers(
            PluginInstallationManagerHolder managerHolder,
            ConsoleCatalogService catalogService,
            ConsoleCatalogSyncService syncService,
            NavigationMenuAssembler navigationMenuAssembler,
            AuthorizationSubjectResolver subjectResolver) {
        this.managerHolder = managerHolder;
        this.catalogService = catalogService;
        this.syncService = syncService;
        this.navigationMenuAssembler = navigationMenuAssembler;
        this.subjectResolver = subjectResolver;
    }

    @Produces
    @Dependent
    ConsoleCatalogEndpoint consoleCatalogEndpoint() {
        return new ConsoleCatalogEndpoint(catalogService, syncService);
    }

    @Produces
    @Dependent
    NavigationMenuEndpoint navigationMenuEndpoint() {
        return new NavigationMenuEndpoint(navigationMenuAssembler, subjectResolver);
    }

    @Produces
    @Dependent
    PluginManagementEndpoint pluginManagementEndpoint() {
        return new PluginManagementEndpoint(
                managerHolder.requireManager(),
                PluginManagementConverter.INSTANCE,
                syncService);
    }
}

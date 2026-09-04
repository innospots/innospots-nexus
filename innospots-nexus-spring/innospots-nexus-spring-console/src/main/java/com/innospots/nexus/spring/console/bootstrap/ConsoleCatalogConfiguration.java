package com.innospots.nexus.spring.console.bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.base.ui.spec.config.UiSpecConfig;
import com.innospots.nexus.base.ui.spec.loader.ClasspathUiSpecLoader;
import com.innospots.nexus.base.ui.spec.loader.UiSpecLoader;
import com.innospots.nexus.base.ui.spec.parser.JacksonUiSpecParser;
import com.innospots.nexus.console.catalog.bootstrap.ConsoleCatalogSyncStartupTask;
import com.innospots.nexus.console.catalog.endpoint.ConsoleCatalogEndpoint;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.core.plugin.contribution.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.console.navigation.endpoint.NavigationMenuEndpoint;
import com.innospots.nexus.console.navigation.service.NavigationMenuAssembler;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.core.plugin.contribution.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.plugin.converter.PluginManagementConverter;
import com.innospots.nexus.console.plugin.endpoint.PluginManagementEndpoint;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.spring.plugin.config.PluginInstallationManagerHolder;

/**
 * 管理控制台目录、导航与启动同步装配。
 */
@Configuration
public class ConsoleCatalogConfiguration {

    /**
     * UiSpec 加载器。
     */
    @Bean
    UiSpecLoader uiSpecLoader() {
        UiSpecConfig config = UiSpecConfig.defaults();
        return new ClasspathUiSpecLoader(config, new JacksonUiSpecParser(config), null);
    }

    /**
     * 权限资源目录同步服务。
     */
    @Bean
    ConsoleCatalogSyncService permissionResourceSyncService(
            ConsoleCatalogResourceDao resourceDao,
            ConsoleContributionCatalog contributionCatalog,
            UiSpecLoader uiSpecLoader) {
        return new ConsoleCatalogSyncService(resourceDao, contributionCatalog, uiSpecLoader);
    }

    /**
     * 权限设置目录树服务。
     */
    @Bean
    ConsoleCatalogService consoleCatalogService(ConsoleCatalogResourceDao resourceDao) {
        return new ConsoleCatalogService(resourceDao);
    }

    /**
     * 启动后目录同步任务。
     */
    @Bean
    ConsoleCatalogSyncStartupTask consoleCatalogSyncStartupTask(
            ConsoleCatalogSyncService syncService) {
        return new ConsoleCatalogSyncStartupTask(syncService);
    }

    /**
     * 将目录同步任务注册进启动编排。
     */
    @Bean
    NexusStartupTask catalogSyncStartupTask(ConsoleCatalogSyncStartupTask task) {
        return task;
    }

    /**
     * 权限资源可见性服务。
     */
    @Bean
    PermissionVisibilityService permissionVisibilityService(
            ConsoleCatalogResourceDao resourceDao,
            PermissionGrantDao grantDao) {
        return new PermissionVisibilityService(resourceDao, grantDao);
    }

    /**
     * 导航菜单组装器。
     */
    @Bean
    NavigationMenuAssembler navigationMenuAssembler(PermissionVisibilityService visibilityService) {
        return new NavigationMenuAssembler(visibilityService);
    }

    /**
     * 鉴权主体解析占位实现；应用层应提供真实实现。
     */
    @Bean
    AuthorizationSubjectResolver authorizationSubjectResolver() {
        return () -> java.util.Optional.empty();
    }

    /**
     * 权限设置目录接口。
     */
    @Bean
    @Lazy
    ConsoleCatalogEndpoint consoleCatalogEndpoint(
            ConsoleCatalogService catalogService,
            ConsoleCatalogSyncService syncService) {
        return new ConsoleCatalogEndpoint(catalogService, syncService);
    }

    /**
     * 导航菜单接口。
     */
    @Bean
    @Lazy
    NavigationMenuEndpoint navigationMenuEndpoint(
            NavigationMenuAssembler assembler,
            AuthorizationSubjectResolver subjectResolver) {
        return new NavigationMenuEndpoint(assembler, subjectResolver);
    }

    /**
     * 插件管理接口（含启停后目录同步）。
     */
    @Bean
    @Lazy
    PluginManagementEndpoint pluginManagementEndpoint(
            PluginInstallationManagerHolder managerHolder,
            ConsoleCatalogSyncService syncService) {
        return new PluginManagementEndpoint(
                managerHolder.requireManager(),
                PluginManagementConverter.INSTANCE,
                syncService);
    }
}

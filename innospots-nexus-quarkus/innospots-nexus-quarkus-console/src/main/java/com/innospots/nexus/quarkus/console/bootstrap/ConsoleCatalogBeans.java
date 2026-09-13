package com.innospots.nexus.quarkus.console.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader.ClasspathPageDslLoader;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader.PageDslLoader;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.parser.JacksonPageDslParser;
import com.innospots.nexus.console.catalog.bootstrap.ConsoleCatalogSyncStartupTask;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.console.navigation.service.NavigationMenuAssembler;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;

/**
 * 管理控制台目录与导航 CDI 生产者。
 */
@ApplicationScoped
public class ConsoleCatalogBeans {

    @Produces
    @Singleton
    PageDslLoader pageDslLoader() {
        PageDslConfig config = PageDslConfig.defaults();
        return new ClasspathPageDslLoader(config, new JacksonPageDslParser(config), null);
    }

    @Produces
    @Singleton
    ConsoleCatalogSyncService permissionResourceSyncService(
            ConsoleCatalogResourceDao resourceDao,
            ConsoleContributionCatalog contributionCatalog,
            PageDslLoader pageDslLoader) {
        return new ConsoleCatalogSyncService(resourceDao, contributionCatalog, pageDslLoader);
    }

    @Produces
    @Singleton
    ConsoleCatalogService consoleCatalogService(ConsoleCatalogResourceDao resourceDao) {
        return new ConsoleCatalogService(resourceDao);
    }

    @Produces
    @Singleton
    ConsoleCatalogSyncStartupTask consoleCatalogSyncStartupTask(
            ConsoleCatalogSyncService syncService) {
        return new ConsoleCatalogSyncStartupTask(syncService);
    }

    @Produces
    @Singleton
    NexusStartupTask catalogSyncStartupTask(ConsoleCatalogSyncStartupTask task) {
        return task;
    }

    @Produces
    @Singleton
    PermissionVisibilityService permissionVisibilityService(
            ConsoleCatalogResourceDao resourceDao,
            PermissionGrantDao grantDao) {
        return new PermissionVisibilityService(resourceDao, grantDao);
    }

    @Produces
    @Singleton
    NavigationMenuAssembler navigationMenuAssembler(PermissionVisibilityService visibilityService) {
        return new NavigationMenuAssembler(visibilityService);
    }

    @Produces
    @Singleton
    AuthorizationSubjectResolver authorizationSubjectResolver() {
        return () -> java.util.Optional.empty();
    }
}

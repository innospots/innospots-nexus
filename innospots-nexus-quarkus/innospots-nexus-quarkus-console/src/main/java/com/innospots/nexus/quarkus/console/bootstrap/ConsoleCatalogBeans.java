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
import com.innospots.nexus.console.permission.authorization.SessionAuthorizationSubjectResolver;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;

/**
 * 管理控制台目录与导航 CDI 生产者。
 *
 * <p>归属 {@code innospots-nexus-quarkus-console}；DAO 由 MyBatis 扩展自动扫描。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see ConsoleResourceProducers
 */
@ApplicationScoped
public class ConsoleCatalogBeans {

    /**
     * PageDsl 加载器 Bean。
     */
    @Produces
    @Singleton
    PageDslLoader pageDslLoader() {
        PageDslConfig config = PageDslConfig.defaults();
        return new ClasspathPageDslLoader(config, new JacksonPageDslParser(config), null);
    }

    /**
     * 宿主级目录同步服务 Bean。
     */
    @Produces
    @Singleton
    ConsoleCatalogSyncService permissionResourceSyncService(
            ConsoleCatalogResourceDao resourceDao,
            ConsoleContributionCatalog contributionCatalog,
            PageDslLoader pageDslLoader) {
        return new ConsoleCatalogSyncService(resourceDao, contributionCatalog, pageDslLoader);
    }

    /**
     * 权限设置目录树服务 Bean。
     */
    @Produces
    @Singleton
    ConsoleCatalogService consoleCatalogService(ConsoleCatalogResourceDao resourceDao) {
        return new ConsoleCatalogService(resourceDao);
    }

    /**
     * 启动后目录同步任务 Bean。
     */
    @Produces
    @Singleton
    ConsoleCatalogSyncStartupTask consoleCatalogSyncStartupTask(
            ConsoleCatalogSyncService syncService) {
        return new ConsoleCatalogSyncStartupTask(syncService);
    }

    /**
     * 将目录同步任务注册进启动编排。
     */
    @Produces
    @Singleton
    NexusStartupTask catalogSyncStartupTask(ConsoleCatalogSyncStartupTask task) {
        return task;
    }

    /**
     * 权限资源可见性服务 Bean。
     */
    @Produces
    @Singleton
    PermissionVisibilityService permissionVisibilityService(
            ConsoleCatalogResourceDao resourceDao,
            PermissionGrantDao grantDao) {
        return new PermissionVisibilityService(resourceDao, grantDao);
    }

    /**
     * 导航菜单组装器 Bean。
     */
    @Produces
    @Singleton
    NavigationMenuAssembler navigationMenuAssembler(PermissionVisibilityService visibilityService) {
        return new NavigationMenuAssembler(visibilityService);
    }

    /**
     * 基于会话与角色绑定的鉴权主体解析。
     */
    @Produces
    @Singleton
    AuthorizationSubjectResolver authorizationSubjectResolver(
            RoleBindingDao roleBindingDao,
            RoleDao roleDao) {
        return new SessionAuthorizationSubjectResolver(roleBindingDao, roleDao);
    }
}


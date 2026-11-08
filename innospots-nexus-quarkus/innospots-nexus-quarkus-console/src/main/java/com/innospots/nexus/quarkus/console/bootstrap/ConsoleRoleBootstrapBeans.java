package com.innospots.nexus.quarkus.console.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.bootstrap.loader.RoleAuthorizationManifestLoader;
import com.innospots.nexus.console.bootstrap.parser.RoleAuthorizationManifestParser;
import com.innospots.nexus.console.bootstrap.service.RoleDefinitionSyncService;
import com.innospots.nexus.console.bootstrap.service.RoleGrantSyncService;
import com.innospots.nexus.console.bootstrap.task.RoleBootstrapTask;
import com.innospots.nexus.console.bootstrap.task.RoleGrantBootstrapTask;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.role.dao.RoleDao;

/**
 * {@code console.bootstrap} 角色侧 Quarkus CDI 装配：角色定义与角色资源授权。
 *
 * <p>对应 Spring 侧 {@code ConsoleRoleBootstrapConfiguration}，
 * 覆盖启动引导管线的第 2、3 阶段。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
@ApplicationScoped
public class ConsoleRoleBootstrapBeans {

    /**
     * 角色授权清单解析器。
     *
     * @return 解析器
     */
    @Produces
    @Singleton
    RoleAuthorizationManifestParser roleAuthorizationManifestParser() {
        return new RoleAuthorizationManifestParser();
    }

    /**
     * 角色授权清单加载器。
     *
     * @param parser YAML 解析器
     * @return 加载器
     */
    @Produces
    @Singleton
    RoleAuthorizationManifestLoader roleAuthorizationManifestLoader(
            RoleAuthorizationManifestParser parser) {
        return new RoleAuthorizationManifestLoader(parser, null);
    }

    /**
     * 角色定义同步服务。
     *
     * @param roleDao 角色 DAO
     * @return 同步服务
     */
    @Produces
    @Singleton
    RoleDefinitionSyncService roleDefinitionSyncService(RoleDao roleDao) {
        return new RoleDefinitionSyncService(roleDao);
    }

    /**
     * 角色授权同步服务。
     *
     * @param roleDefinitionSyncService 角色同步服务
     * @param grantDao                  授权 DAO
     * @param catalogService            权限目录服务
     * @return 同步服务
     */
    @Produces
    @Singleton
    RoleGrantSyncService roleGrantSyncService(
            RoleDefinitionSyncService roleDefinitionSyncService,
            PermissionGrantDao grantDao,
            ConsoleCatalogService catalogService) {
        return new RoleGrantSyncService(roleDefinitionSyncService, grantDao, catalogService);
    }

    /**
     * 角色阶段启动任务。
     *
     * @param loader      清单加载器
     * @param syncService 同步服务
     * @return 启动任务
     */
    @Produces
    @Singleton
    RoleBootstrapTask roleBootstrapTask(
            RoleAuthorizationManifestLoader loader,
            RoleDefinitionSyncService syncService) {
        return new RoleBootstrapTask(loader, syncService);
    }

    /**
     * 角色授权阶段启动任务。
     *
     * @param loader      清单加载器
     * @param syncService 同步服务
     * @return 启动任务
     */
    @Produces
    @Singleton
    RoleGrantBootstrapTask roleGrantBootstrapTask(
            RoleAuthorizationManifestLoader loader,
            RoleGrantSyncService syncService) {
        return new RoleGrantBootstrapTask(loader, syncService);
    }
}

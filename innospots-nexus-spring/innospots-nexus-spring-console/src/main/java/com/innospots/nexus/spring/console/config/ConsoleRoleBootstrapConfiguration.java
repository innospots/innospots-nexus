package com.innospots.nexus.spring.console.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
 * {@code console.bootstrap} 角色侧 Spring 装配：角色定义与角色资源授权。
 *
 * <p>对应启动引导管线的第 2、3 阶段。两阶段都依赖权限目录，
 * 因此本配置只在完整控制台（{@code EnableNexusConsole}）中启用，
 * 目录索引由 {@code ConsoleCatalogConfiguration} 提供。</p>
 *
 * <p>配置资源路径固定为 {@value RoleAuthorizationManifestLoader#RESOURCE_PATH}，
 * 由服务端提供唯一一份，不跨模块合并。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
@Configuration
public class ConsoleRoleBootstrapConfiguration {

    @Bean
    RoleAuthorizationManifestParser roleAuthorizationManifestParser() {
        return new RoleAuthorizationManifestParser();
    }

    @Bean
    RoleAuthorizationManifestLoader roleAuthorizationManifestLoader(
            RoleAuthorizationManifestParser parser) {
        return new RoleAuthorizationManifestLoader(parser, null);
    }

    @Bean
    RoleDefinitionSyncService roleDefinitionSyncService(RoleDao roleDao) {
        return new RoleDefinitionSyncService(roleDao);
    }

    @Bean
    RoleGrantSyncService roleGrantSyncService(
            RoleDefinitionSyncService roleDefinitionSyncService,
            PermissionGrantDao grantDao,
            ConsoleCatalogService catalogService) {
        return new RoleGrantSyncService(roleDefinitionSyncService, grantDao, catalogService);
    }

    @Bean
    RoleBootstrapTask roleBootstrapTask(
            RoleAuthorizationManifestLoader loader,
            RoleDefinitionSyncService syncService) {
        return new RoleBootstrapTask(loader, syncService);
    }

    @Bean
    RoleGrantBootstrapTask roleGrantBootstrapTask(
            RoleAuthorizationManifestLoader loader,
            RoleGrantSyncService syncService) {
        return new RoleGrantBootstrapTask(loader, syncService);
    }
}

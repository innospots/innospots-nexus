package com.innospots.nexus.spring.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.bootstrap.loader.InitialUserManifestLoader;
import com.innospots.nexus.console.bootstrap.parser.InitialUserManifestParser;
import com.innospots.nexus.console.bootstrap.service.InitialUserRoleBindingService;
import com.innospots.nexus.console.bootstrap.service.InitialUserSyncService;
import com.innospots.nexus.console.bootstrap.task.UserBootstrapTask;
import com.innospots.nexus.console.bootstrap.task.UserRoleBindingBootstrapTask;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.user.admin.dao.AdminUserDao;

/**
 * {@code console.bootstrap} 用户侧 Spring 装配：初始用户与初始用户角色绑定。
 *
 * <p>对应启动引导管线的第 4、5 阶段。仅 platform 装配，
 * 因为初始用户直接依赖 {@code nx_admin_user} 表；portal 侧没有该表。</p>
 *
 * <p>配置资源路径固定为 {@value InitialUserManifestLoader#RESOURCE_PATH}，
 * 由服务端提供唯一一份；清单中的 {@code enabled: false} 可整体关闭用户初始化与绑定。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
@Configuration
public class PlatformBootstrapConfiguration {

    @Bean
    InitialUserManifestParser initialUserManifestParser() {
        return new InitialUserManifestParser();
    }

    @Bean
    InitialUserManifestLoader initialUserManifestLoader(InitialUserManifestParser parser) {
        return new InitialUserManifestLoader(parser, null);
    }

    @Bean
    InitialUserSyncService initialUserSyncService(
            AdminUserDao adminUserDao,
            CredentialService credentialService) {
        return new InitialUserSyncService(adminUserDao, credentialService);
    }

    @Bean
    InitialUserRoleBindingService initialUserRoleBindingService(
            InitialUserSyncService initialUserSyncService,
            RoleDao roleDao,
            RoleBindingDao roleBindingDao) {
        return new InitialUserRoleBindingService(initialUserSyncService, roleDao, roleBindingDao);
    }

    @Bean
    UserBootstrapTask userBootstrapTask(
            InitialUserManifestLoader loader,
            InitialUserSyncService syncService) {
        return new UserBootstrapTask(loader, syncService);
    }

    @Bean
    UserRoleBindingBootstrapTask userRoleBindingBootstrapTask(
            InitialUserManifestLoader loader,
            InitialUserRoleBindingService bindingService) {
        return new UserRoleBindingBootstrapTask(loader, bindingService);
    }
}

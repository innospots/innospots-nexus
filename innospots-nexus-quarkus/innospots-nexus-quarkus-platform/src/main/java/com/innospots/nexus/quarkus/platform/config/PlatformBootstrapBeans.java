package com.innospots.nexus.quarkus.platform.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

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
 * {@code console.bootstrap} 用户侧 Quarkus CDI 装配：初始用户与角色绑定。
 *
 * <p>对应 Spring 侧 {@code PlatformBootstrapConfiguration}，
 * 覆盖启动引导管线的第 4、5 阶段。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
@ApplicationScoped
public class PlatformBootstrapBeans {

    /**
     * 初始用户清单解析器。
     *
     * @return 解析器
     */
    @Produces
    @Singleton
    InitialUserManifestParser initialUserManifestParser() {
        return new InitialUserManifestParser();
    }

    /**
     * 初始用户清单加载器。
     *
     * @param parser YAML 解析器
     * @return 加载器
     */
    @Produces
    @Singleton
    InitialUserManifestLoader initialUserManifestLoader(InitialUserManifestParser parser) {
        return new InitialUserManifestLoader(parser, null);
    }

    /**
     * 初始用户同步服务。
     *
     * @param adminUserDao      后台用户 DAO
     * @param credentialService 凭证服务
     * @return 同步服务
     */
    @Produces
    @Singleton
    InitialUserSyncService initialUserSyncService(
            AdminUserDao adminUserDao,
            CredentialService credentialService) {
        return new InitialUserSyncService(adminUserDao, credentialService);
    }

    /**
     * 用户角色绑定服务。
     *
     * @param initialUserSyncService 用户同步服务
     * @param roleDao                角色 DAO
     * @param roleBindingDao         角色绑定 DAO
     * @return 绑定服务
     */
    @Produces
    @Singleton
    InitialUserRoleBindingService initialUserRoleBindingService(
            InitialUserSyncService initialUserSyncService,
            RoleDao roleDao,
            RoleBindingDao roleBindingDao) {
        return new InitialUserRoleBindingService(initialUserSyncService, roleDao, roleBindingDao);
    }

    /**
     * 用户阶段启动任务。
     *
     * @param loader      清单加载器
     * @param syncService 同步服务
     * @return 启动任务
     */
    @Produces
    @Singleton
    UserBootstrapTask userBootstrapTask(
            InitialUserManifestLoader loader,
            InitialUserSyncService syncService) {
        return new UserBootstrapTask(loader, syncService);
    }

    /**
     * 用户角色绑定阶段启动任务。
     *
     * @param loader         清单加载器
     * @param bindingService 绑定服务
     * @return 启动任务
     */
    @Produces
    @Singleton
    UserRoleBindingBootstrapTask userRoleBindingBootstrapTask(
            InitialUserManifestLoader loader,
            InitialUserRoleBindingService bindingService) {
        return new UserRoleBindingBootstrapTask(loader, bindingService);
    }
}

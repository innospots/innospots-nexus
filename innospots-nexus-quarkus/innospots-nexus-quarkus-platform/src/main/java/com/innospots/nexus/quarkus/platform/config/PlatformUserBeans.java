package com.innospots.nexus.quarkus.platform.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.user.admin.dao.AdminUserDao;
import com.innospots.nexus.console.user.admin.operator.AdminUserOperator;
import com.innospots.nexus.console.user.admin.service.AdminUserService;
import com.innospots.nexus.console.user.admin.support.AdminUserRoleProvisioner;

/**
 * 运营管理平台用户 Quarkus CDI 装配。
 */
@ApplicationScoped
public class PlatformUserBeans {

    @Produces
    @Singleton
    AdminUserOperator platformUserOperator(AdminUserDao platformUserDao) {
        return new AdminUserOperator(platformUserDao);
    }

    @Produces
    @Singleton
    AdminUserRoleProvisioner platformUserRoleProvisioner(RoleDao roleDao, RoleBindingDao roleBindingDao) {
        return new AdminUserRoleProvisioner(roleDao, roleBindingDao);
    }

    @Produces
    @Singleton
    AdminUserService platformUserService(
            AdminUserOperator platformUserOperator,
            CredentialService credentialService,
            PasswordDecryptor passwordDecryptor,
            AdminUserRoleProvisioner platformUserRoleProvisioner) {
        return new AdminUserService(
                platformUserOperator,
                credentialService,
                passwordDecryptor,
                platformUserRoleProvisioner);
    }
}

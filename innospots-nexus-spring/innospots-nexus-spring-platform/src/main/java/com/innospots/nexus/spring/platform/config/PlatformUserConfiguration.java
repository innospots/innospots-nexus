package com.innospots.nexus.spring.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.user.admin.dao.AdminUserDao;
import com.innospots.nexus.platform.user.endpoint.PlatformAdminUserEndpoint;
import com.innospots.nexus.console.user.admin.operator.AdminUserOperator;
import com.innospots.nexus.console.user.admin.service.AdminUserService;
import com.innospots.nexus.console.user.admin.support.AdminUserRoleProvisioner;

/**
 * 运营管理平台用户 Spring 装配。
 */
@Configuration
public class PlatformUserConfiguration {

    @Bean
    AdminUserOperator platformUserOperator(AdminUserDao platformUserDao) {
        return new AdminUserOperator(platformUserDao);
    }

    @Bean
    AdminUserRoleProvisioner platformUserRoleProvisioner(RoleDao roleDao, RoleBindingDao roleBindingDao) {
        return new AdminUserRoleProvisioner(roleDao, roleBindingDao);
    }

    @Bean
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

    @Bean
    @Lazy
    PlatformAdminUserEndpoint platformUserEndpoint(AdminUserService platformUserService) {
        return new PlatformAdminUserEndpoint(platformUserService);
    }
}

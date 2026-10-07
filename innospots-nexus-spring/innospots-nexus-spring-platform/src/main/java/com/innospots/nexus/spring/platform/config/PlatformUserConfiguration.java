package com.innospots.nexus.spring.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.endpoint.PlatformUserEndpoint;
import com.innospots.nexus.platform.user.operator.PlatformUserOperator;
import com.innospots.nexus.platform.user.service.PlatformUserService;
import com.innospots.nexus.platform.user.support.PlatformUserRoleProvisioner;

/**
 * 运营管理平台用户 Spring 装配。
 */
@Configuration
public class PlatformUserConfiguration {

    @Bean
    PlatformUserOperator platformUserOperator(PlatformUserDao platformUserDao) {
        return new PlatformUserOperator(platformUserDao);
    }

    @Bean
    PlatformUserRoleProvisioner platformUserRoleProvisioner(RoleDao roleDao, RoleBindingDao roleBindingDao) {
        return new PlatformUserRoleProvisioner(roleDao, roleBindingDao);
    }

    @Bean
    PlatformUserService platformUserService(
            PlatformUserOperator platformUserOperator,
            CredentialService credentialService,
            PasswordDecryptor passwordDecryptor,
            PlatformUserRoleProvisioner platformUserRoleProvisioner) {
        return new PlatformUserService(
                platformUserOperator,
                credentialService,
                passwordDecryptor,
                platformUserRoleProvisioner);
    }

    @Bean
    @Lazy
    PlatformUserEndpoint platformUserEndpoint(PlatformUserService platformUserService) {
        return new PlatformUserEndpoint(platformUserService);
    }
}

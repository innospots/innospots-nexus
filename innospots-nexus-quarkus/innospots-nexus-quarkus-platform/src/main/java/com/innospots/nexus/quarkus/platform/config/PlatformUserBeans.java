package com.innospots.nexus.quarkus.platform.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.operator.PlatformUserOperator;
import com.innospots.nexus.platform.user.service.PlatformUserService;

/**
 * 运营管理平台用户 Quarkus CDI 装配。
 */
@ApplicationScoped
public class PlatformUserBeans {

    @Produces
    @Singleton
    PlatformUserOperator platformUserOperator(PlatformUserDao platformUserDao) {
        return new PlatformUserOperator(platformUserDao);
    }

    @Produces
    @Singleton
    PlatformUserService platformUserService(
            PlatformUserOperator platformUserOperator,
            CredentialService credentialService,
            PasswordDecryptor passwordDecryptor) {
        return new PlatformUserService(platformUserOperator, credentialService, passwordDecryptor);
    }
}

package com.innospots.nexus.quarkus.console.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.credential.otp.dao.OtpChallengeDao;
import com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.otp.service.OtpPasswordVerificationOperator;
import com.innospots.nexus.console.credential.password.PasswordValidator;
import com.innospots.nexus.console.credential.password.PasswordVerificationOperator;
import com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithmRegistry;
import com.innospots.nexus.console.credential.password.dao.UserCredentialDao;
import com.innospots.nexus.console.credential.password.operator.UserCredentialOperator;
import com.innospots.nexus.console.credential.password.policy.LoginLockPolicy;
import com.innospots.nexus.console.credential.password.service.CredentialService;

/**
 * {@code console.credential} 域 Quarkus CDI 装配。
 */
@ApplicationScoped
public class ConsoleCredentialBeans {

    @Produces
    @Singleton
    CredentialAlgorithmRegistry credentialAlgorithmRegistry() {
        return CredentialAlgorithmRegistry.defaults();
    }

    @Produces
    @Singleton
    UserCredentialOperator userCredentialOperator(
            UserCredentialDao credentialDao,
            CredentialAlgorithmRegistry credentialAlgorithmRegistry) {
        return new UserCredentialOperator(credentialDao, credentialAlgorithmRegistry);
    }

    @Produces
    @Singleton
    PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }

    @Produces
    @Singleton
    CredentialService credentialService(
            UserCredentialOperator userCredentialOperator,
            PasswordValidator passwordValidator,
            NexusConsoleAuthConfig authProperties) {
        LoginLockPolicy lockPolicy = new LoginLockPolicy(
                authProperties.loginMaxFailedAttempts(),
                authProperties.loginLockMinutes());
        return new CredentialService(userCredentialOperator, passwordValidator, lockPolicy);
    }

    @Produces
    @Singleton
    OtpChallengeService otpChallengeService(OtpChallengeDao otpChallengeDao) {
        return new OtpChallengeService(otpChallengeDao);
    }

    @Produces
    @Singleton
    CaptchaChallengeService captchaChallengeService(OtpChallengeService otpChallengeService) {
        return new CaptchaChallengeService(otpChallengeService);
    }

    @Produces
    @Singleton
    PasswordVerificationOperator passwordVerificationOperator(OtpChallengeService otpChallengeService) {
        return new OtpPasswordVerificationOperator(otpChallengeService);
    }
}

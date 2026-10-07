package com.innospots.nexus.spring.console.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
import com.innospots.nexus.console.credential.totp.MfaAuthenticator;
import com.innospots.nexus.console.credential.totp.TotpMasterKeyProvider;
import com.innospots.nexus.console.credential.totp.TotpSecretProtector;
import com.innospots.nexus.console.credential.totp.adapter.DefaultMfaAuthenticator;
import com.innospots.nexus.console.credential.totp.service.DefaultTotpEnrollmentService;
import com.innospots.nexus.console.credential.totp.service.TotpEnrollmentService;

/**
 * {@code console.credential} 域装配：密码、OTP/TOTP 与登录锁定策略。
 *
 * @author Smars
 * @date 2026/09/23
 * @see ConsoleCredentialTotpProperties
 */
@Configuration
@EnableConfigurationProperties(ConsoleCredentialTotpProperties.class)
@MapperScan(
        basePackages = {
            "com.innospots.nexus.console.credential.password.dao",
            "com.innospots.nexus.console.credential.otp.dao"
        },
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsoleCredentialConfiguration {

    @Bean
    CredentialAlgorithmRegistry credentialAlgorithmRegistry() {
        return CredentialAlgorithmRegistry.defaults();
    }

    @Bean
    UserCredentialOperator userCredentialOperator(
            UserCredentialDao credentialDao,
            CredentialAlgorithmRegistry credentialAlgorithmRegistry) {
        return new UserCredentialOperator(credentialDao, credentialAlgorithmRegistry);
    }

    @Bean
    PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }

    @Bean
    CredentialService credentialService(
            UserCredentialOperator userCredentialOperator,
            PasswordValidator passwordValidator,
            ConsoleAuthProperties authProperties) {
        LoginLockPolicy lockPolicy = new LoginLockPolicy(
                authProperties.getLoginMaxFailedAttempts(),
                authProperties.getLoginLockMinutes());
        return new CredentialService(userCredentialOperator, passwordValidator, lockPolicy);
    }

    @Bean
    OtpChallengeService otpChallengeService(OtpChallengeDao otpChallengeDao) {
        return new OtpChallengeService(otpChallengeDao);
    }

    @Bean
    CaptchaChallengeService captchaChallengeService(OtpChallengeService otpChallengeService) {
        return new CaptchaChallengeService(otpChallengeService);
    }

    @Bean
    PasswordVerificationOperator passwordVerificationOperator(OtpChallengeService otpChallengeService) {
        return new OtpPasswordVerificationOperator(otpChallengeService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "nexus.console.credential.totp", name = "master-key")
    TotpMasterKeyProvider totpMasterKeyProvider(ConsoleCredentialTotpProperties totpProperties) {
        return TotpMasterKeyProvider.fixed(totpProperties.getMasterKey());
    }

    @Bean
    @ConditionalOnBean(TotpMasterKeyProvider.class)
    TotpSecretProtector totpSecretProtector(TotpMasterKeyProvider totpMasterKeyProvider) {
        return new TotpSecretProtector(totpMasterKeyProvider);
    }

    @Bean
    @ConditionalOnBean(TotpSecretProtector.class)
    TotpEnrollmentService totpEnrollmentService(
            UserCredentialOperator userCredentialOperator,
            TotpSecretProtector totpSecretProtector) {
        return new DefaultTotpEnrollmentService(userCredentialOperator, totpSecretProtector);
    }

    @Bean
    @ConditionalOnBean(TotpSecretProtector.class)
    MfaAuthenticator mfaAuthenticator(
            UserCredentialOperator userCredentialOperator,
            TotpSecretProtector totpSecretProtector) {
        return new DefaultMfaAuthenticator(userCredentialOperator, totpSecretProtector);
    }
}

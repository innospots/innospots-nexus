package com.innospots.nexus.quarkus.console.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.innospots.nexus.console.auth.service.RegistrationPolicyService;
import com.innospots.nexus.console.config.RegistrationConfig;
import com.innospots.nexus.console.config.RegistrationMode;
import com.innospots.nexus.console.config.RegistrationOtpVerification;
import com.innospots.nexus.console.credential.otp.support.RegistrationOtpDebugLogger;

/**
 * Quarkus 侧注册配置 Bean：等价于 Spring {@link com.innospots.nexus.spring.console.config.ConsoleRegistrationConfiguration}。
 *
 * @author Smars
 * @date 2026/10/18
 */
@ApplicationScoped
public class ConsoleRegistrationBeans {

    @Produces
    @Singleton
    RegistrationConfig registrationConfig(
            @ConfigProperty(name = "nexus.console.registration.mode", defaultValue = "INVITE")
                    RegistrationMode mode,
            @ConfigProperty(name = "nexus.console.registration.otp-verification", defaultValue = "NONE")
                    RegistrationOtpVerification otpVerification,
            @ConfigProperty(name = "nexus.console.registration.otp.debug-log-enabled", defaultValue = "false")
                    boolean otpDebugLogEnabled,
            @ConfigProperty(name = "nexus.console.registration.otp.email.host", defaultValue = "")
                    String emailHost,
            @ConfigProperty(name = "nexus.console.registration.otp.email.port", defaultValue = "587")
                    int emailPort,
            @ConfigProperty(name = "nexus.console.registration.otp.email.username", defaultValue = "")
                    String emailUsername,
            @ConfigProperty(name = "nexus.console.registration.otp.email.password", defaultValue = "")
                    String emailPassword,
            @ConfigProperty(name = "nexus.console.registration.otp.email.from", defaultValue = "")
                    String emailFrom,
            @ConfigProperty(name = "nexus.console.registration.otp.email.from-name", defaultValue = "")
                    String emailFromName,
            @ConfigProperty(name = "nexus.console.registration.otp.email.tls", defaultValue = "true")
                    boolean emailTls,
            @ConfigProperty(name = "nexus.console.registration.otp.mobile.api-url", defaultValue = "")
                    String mobileApiUrl,
            @ConfigProperty(name = "nexus.console.registration.otp.mobile.api-key", defaultValue = "")
                    String mobileApiKey) {
        RegistrationConfig config = new RegistrationConfig();
        config.setMode(mode);
        config.setOtpVerification(otpVerification);
        RegistrationConfig.OtpDelivery otp = new RegistrationConfig.OtpDelivery();
        otp.setDebugLogEnabled(otpDebugLogEnabled);
        RegistrationConfig.Email email = new RegistrationConfig.Email();
        email.setHost(emailHost.isBlank() ? null : emailHost);
        email.setPort(emailPort);
        email.setUsername(emailUsername.isBlank() ? null : emailUsername);
        email.setPassword(emailPassword.isBlank() ? null : emailPassword);
        email.setFrom(emailFrom.isBlank() ? null : emailFrom);
        email.setFromName(emailFromName.isBlank() ? null : emailFromName);
        email.setTls(emailTls);
        otp.setEmail(email);
        RegistrationConfig.Mobile mobile = new RegistrationConfig.Mobile();
        mobile.setApiUrl(mobileApiUrl.isBlank() ? null : mobileApiUrl);
        mobile.setApiKey(mobileApiKey.isBlank() ? null : mobileApiKey);
        otp.setMobile(mobile);
        config.setOtp(otp);
        RegistrationOtpDebugLogger.registerIfEnabled(config);
        return config;
    }

    @Produces
    @Singleton
    RegistrationPolicyService registrationPolicyService(RegistrationConfig registrationConfig) {
        return new RegistrationPolicyService(registrationConfig);
    }
}

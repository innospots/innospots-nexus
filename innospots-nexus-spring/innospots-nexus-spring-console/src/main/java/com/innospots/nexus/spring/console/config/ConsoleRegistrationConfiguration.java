package com.innospots.nexus.spring.console.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.auth.service.RegistrationPolicyService;
import com.innospots.nexus.console.config.RegistrationConfig;
import com.innospots.nexus.console.credential.otp.support.RegistrationOtpDebugLogger;

/**
 * 注册策略 Spring 装配：将 {@link ConsoleRegistrationProperties} 转为 {@link RegistrationConfig}
 * 与 {@link RegistrationPolicyService}。
 *
 * @author Smars
 * @date 2026/10/18
 * @see EnableNexusSimpleConsole
 */
@Configuration
@EnableConfigurationProperties(ConsoleRegistrationProperties.class)
public class ConsoleRegistrationConfiguration {

    /**
     * 将 Spring 属性映射为 framework-neutral {@link RegistrationConfig}。
     *
     * @param properties 绑定后的配置属性
     * @return 注册配置 Bean
     */
    @Bean
    RegistrationConfig registrationConfig(ConsoleRegistrationProperties properties) {
        RegistrationConfig config = new RegistrationConfig();
        config.setMode(properties.getMode());
        config.setOtpVerification(properties.getOtpVerification());
        RegistrationConfig.OtpDelivery otp = new RegistrationConfig.OtpDelivery();
        otp.setDebugLogEnabled(properties.getOtp().isDebugLogEnabled());
        ConsoleRegistrationProperties.Email emailProps = properties.getOtp().getEmail();
        RegistrationConfig.Email email = new RegistrationConfig.Email();
        email.setHost(emailProps.getHost());
        email.setPort(emailProps.getPort());
        email.setUsername(emailProps.getUsername());
        email.setPassword(emailProps.getPassword());
        email.setFrom(emailProps.getFrom());
        email.setFromName(emailProps.getFromName());
        email.setTls(emailProps.isTls());
        otp.setEmail(email);
        ConsoleRegistrationProperties.Mobile mobileProps = properties.getOtp().getMobile();
        RegistrationConfig.Mobile mobile = new RegistrationConfig.Mobile();
        mobile.setApiUrl(mobileProps.getApiUrl());
        mobile.setApiKey(mobileProps.getApiKey());
        otp.setMobile(mobile);
        config.setOtp(otp);
        return config;
    }

    @Bean
    RegistrationPolicyService registrationPolicyService(RegistrationConfig registrationConfig) {
        return new RegistrationPolicyService(registrationConfig);
    }

    /**
     * 在启用 OTP 调试日志时注册 {@link RegistrationOtpDebugLogger}（进程内 EventBus 订阅）。
     */
    @Bean
    @ConditionalOnProperty(prefix = "nexus.console.registration.otp", name = "debug-log-enabled", havingValue = "true")
    Object registrationOtpDebugLoggerBootstrap(RegistrationConfig registrationConfig) {
        RegistrationOtpDebugLogger.registerIfEnabled(registrationConfig);
        return new Object();
    }
}

package com.innospots.nexus.spring.console.role.support;

import java.util.Optional;
import java.util.Set;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import org.mockito.Mockito;

import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.logger.InvocationLogHandler;
import com.innospots.nexus.console.logger.support.AuditLogRecorder;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer;

/**
 * Jersey Web 集成测试所需的最小控制台横切 Bean。
 */
@TestConfiguration
public class RoleEndpointIntegrationTestConfiguration {

    /** 角色切片仅装配 Web 依赖，审计持久化由平台审计集成测试覆盖。 */
    @Bean
    AuditLogRecorder roleEndpointIntegrationAuditLogRecorder() {
        return new AuditLogRecorder(Mockito.mock(InvocationLogHandler.class));
    }

    @Bean
    AuthConfig roleEndpointIntegrationAuthConfig() {
        AuthConfig config = new AuthConfig();
        config.setTokenSecret(AuthConfig.DEFAULT_TOKEN_SECRET);
        return config;
    }

    @Bean
    @Primary
    ConsolePagePermissionAuthorizer roleEndpointIntegrationPagePermissionAuthorizer() {
        return Mockito.mock(ConsolePagePermissionAuthorizer.class);
    }

    @Bean
    AuthorizationSubjectResolver roleEndpointIntegrationSubjectResolver() {
        return () -> Optional.of(new AuthorizationSubject("1001", Set.of("role-1"), Set.of(), false));
    }
}

package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import java.util.Optional;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.logger.InvocationLogHandler;
import com.innospots.nexus.console.logger.support.AuditLogRecorder;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogEndpoint;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.ConsoleResourcePermissionAuthorizer;

/**
 * Web 过滤器测试用最小 Bean（不含真实 {@link ConsoleResourcePermissionAuthorizer}）。
 */
@Configuration
@Profile("jaxrs-web-it")
public class ConsoleJaxRsWebTestConfiguration {

    /** 过滤器切片不验证落库；完整审计持久化由平台审计集成测试覆盖。 */
    @Bean
    AuditLogRecorder consoleJaxRsWebTestAuditLogRecorder() {
        return new AuditLogRecorder(org.mockito.Mockito.mock(InvocationLogHandler.class));
    }

    @Bean
    OpenApiCatalogOperator consoleJaxRsWebOpenApiCatalogOperator() {
        return new OpenApiCatalogOperator();
    }

    @Bean
    OpenApiCatalogEndpoint consoleJaxRsWebOpenApiCatalogEndpoint(
            OpenApiCatalogOperator openApiCatalogOperator) {
        return new OpenApiCatalogEndpoint(openApiCatalogOperator);
    }

    @Bean
    AuthConfig consoleJaxRsWebTestAuthConfig() {
        AuthConfig config = new AuthConfig();
        config.setTokenSecret(AuthConfig.DEFAULT_TOKEN_SECRET);
        return config;
    }

    @Bean
    @Primary
    TokenIssuer consoleJaxRsWebTestTokenIssuer(AuthConfig authConfig) {
        return new TokenIssuer(authConfig);
    }

    @Bean
    @Primary
    ConsoleResourcePermissionAuthorizer consoleJaxRsWebTestResourcePermissionAuthorizer() {
        return ConsoleJaxRsWebTestMocks.RESOURCE_PERMISSION_AUTHORIZER;
    }

    @Bean
    AuthorizationSubjectResolver consoleJaxRsWebTestSubjectResolver() {
        return () -> Optional.of(new AuthorizationSubject("1001", Set.of("role-1"), Set.of(), false));
    }

    @Bean
    ConsoleJaxRsWebOpenApiTestResource consoleJaxRsWebOpenApiTestResource() {
        return new ConsoleJaxRsWebOpenApiTestResource();
    }

    @Bean
    ConsoleJaxRsWebSecuredTestResource consoleJaxRsWebSecuredTestResource() {
        return new ConsoleJaxRsWebSecuredTestResource();
    }

    @Bean
    ConsoleJaxRsWebGrantedApiTestResource consoleJaxRsWebGrantedApiTestResource() {
        return new ConsoleJaxRsWebGrantedApiTestResource();
    }

}

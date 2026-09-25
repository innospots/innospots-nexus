package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import java.util.Optional;
import java.util.Set;

import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.openapi.endpoint.OpenApiCatalogEndpoint;
import com.innospots.nexus.console.openapi.operator.OpenApiCatalogOperator;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.RequestAuthorizer;

/**
 * Web 过滤器测试用最小 Bean（不含真实 {@link com.innospots.nexus.console.permission.authorization.RequestAuthorizer}）。
 */
@Configuration
public class ConsoleJaxRsWebTestConfiguration {

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
    RequestAuthorizer consoleJaxRsWebTestRequestAuthorizer() {
        return ConsoleJaxRsWebTestMocks.REQUEST_AUTHORIZER;
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
    ConsoleJaxRsWebDatasourceTestResource consoleJaxRsWebDatasourceTestResource() {
        return new ConsoleJaxRsWebDatasourceTestResource();
    }

    @Bean
    ResourceConfigCustomizer consoleJaxRsWebTestResourceClasses() {
        return resourceConfig -> {
            resourceConfig.register(ConsoleJaxRsWebOpenApiTestResource.class);
            resourceConfig.register(ConsoleJaxRsWebSecuredTestResource.class);
            resourceConfig.register(ConsoleJaxRsWebDatasourceTestResource.class);
        };
    }

}

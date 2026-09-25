package com.innospots.nexus.spring.console.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.openapi.endpoint.OpenApiCatalogEndpoint;
import com.innospots.nexus.console.openapi.operator.OpenApiCatalogOperator;

/**
 * OpenAPI 规范 Jakarta REST Bean；HTTP 由 {@link com.innospots.nexus.spring.console.jaxrs.NexusJaxRsConfiguration} 暴露。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ConsoleOpenApiConfiguration {

    @Bean
    OpenApiCatalogOperator openApiCatalogOperator() {
        return new OpenApiCatalogOperator();
    }

    @Bean
    OpenApiCatalogEndpoint openApiCatalogEndpoint(OpenApiCatalogOperator openApiCatalogOperator) {
        return new OpenApiCatalogEndpoint(openApiCatalogOperator);
    }
}

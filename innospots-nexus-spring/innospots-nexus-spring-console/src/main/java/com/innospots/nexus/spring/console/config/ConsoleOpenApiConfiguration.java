package com.innospots.nexus.spring.console.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.openapi.endpoint.OpenApiCatalogEndpoint;
import com.innospots.nexus.console.openapi.operator.OpenApiCatalogOperator;

/**
 * OpenAPI 规范目录的 Jakarta REST Bean 装配。
 *
 * <p>HTTP 路由由 {@link com.innospots.nexus.spring.console.jaxrs.NexusJaxRsConfiguration} 注册；
 * Scalar 文档页由 {@link com.innospots.nexus.spring.console.jaxrs.NexusScalarJerseyConfiguration} 提供。</p>
 *
 * @author Smars
 * @date 2026/09/23
 * @see OpenApiCatalogEndpoint
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ConsoleOpenApiConfiguration {

    /** 从 classpath {@code META-INF/nexus-openapi} 加载构建期生成的 YAML 规范。 */
    @Bean
    OpenApiCatalogOperator openApiCatalogOperator() {
        return new OpenApiCatalogOperator();
    }

    /** {@code GET /openapi/specs} 与按 specId 获取单份规范。 */
    @Bean
    OpenApiCatalogEndpoint openApiCatalogEndpoint(OpenApiCatalogOperator openApiCatalogOperator) {
        return new OpenApiCatalogEndpoint(openApiCatalogOperator);
    }
}

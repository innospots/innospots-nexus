package com.innospots.nexus.spring.core.openapi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogEndpoint;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;

/**
 * OpenAPI 规范目录的 Jakarta REST Bean 装配（与 console/portal/platform 业务域无关）。
 *
 * <p>HTTP 路由由宿主 Jersey 配置注册；Scalar 文档页由 {@code *ScalarJerseyConfiguration} 提供。</p>
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OpenApiCatalogConfiguration {

    @Bean
    OpenApiCatalogOperator openApiCatalogOperator() {
        return new OpenApiCatalogOperator();
    }

    @Bean
    OpenApiCatalogEndpoint openApiCatalogEndpoint(OpenApiCatalogOperator openApiCatalogOperator) {
        return new OpenApiCatalogEndpoint(openApiCatalogOperator);
    }
}

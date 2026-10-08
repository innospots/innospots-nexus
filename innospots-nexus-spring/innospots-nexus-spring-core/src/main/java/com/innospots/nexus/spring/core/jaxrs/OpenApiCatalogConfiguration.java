package com.innospots.nexus.spring.core.jaxrs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogEndpoint;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;

/**
 * OpenAPI 规范目录的 Jakarta REST Bean 装配（与 console/portal/platform 业务域无关）。
 *
 * <p>HTTP 路由由宿主 Jersey 配置注册；Scalar 文档页由 {@code *ScalarJerseyConfiguration} 提供。</p>
 *
 * @see OpenApiCatalogEndpoint
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ResourceConfigCustomizer.class)
public class OpenApiCatalogConfiguration {

    @Bean
    OpenApiCatalogOperator openApiCatalogOperator() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = OpenApiCatalogOperator.class.getClassLoader();
        }
        return new OpenApiCatalogOperator(loader);
    }

    @Bean
    OpenApiCatalogEndpoint openApiCatalogEndpoint(OpenApiCatalogOperator openApiCatalogOperator) {
        return new OpenApiCatalogEndpoint(openApiCatalogOperator);
    }

    @Bean
    static ResourceConfigCustomizer openApiCatalogJerseyEndpointCustomizer(ApplicationContext applicationContext) {
        return resourceConfig -> {
            if (applicationContext.containsBean("openApiCatalogEndpoint")) {
                resourceConfig.register(applicationContext.getBean(OpenApiCatalogEndpoint.class));
            }
        };
    }
}

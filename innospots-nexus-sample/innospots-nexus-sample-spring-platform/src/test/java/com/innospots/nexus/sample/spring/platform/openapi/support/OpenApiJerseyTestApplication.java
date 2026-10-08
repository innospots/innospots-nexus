package com.innospots.nexus.sample.spring.platform.openapi.support;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogEndpoint;
import com.innospots.nexus.spring.core.jaxrs.NexusJaxRsConfiguration;
import com.innospots.nexus.spring.core.jaxrs.NexusScalarJerseyConfiguration;
import com.innospots.nexus.spring.core.jaxrs.OpenApiCatalogConfiguration;

/**
 * 仅装配 OpenAPI 目录与 Scalar/Jersey，供 HTTP 契约测试使用（不启用完整控制台宿主）。
 */
@SpringBootApplication
@Import({
        OpenApiCatalogConfiguration.class,
        NexusJaxRsConfiguration.class,
        NexusScalarJerseyConfiguration.class
})
public class OpenApiJerseyTestApplication {

    /**
     * 切片测试的 {@link ResourceConfigCustomizer} 须挂在宿主 {@link SpringBootApplication} 上，
     * 以便在 {@code JerseyAutoConfiguration} 应用 customizer 时 {@link OpenApiCatalogEndpoint} 已就绪。
     */
    @Bean
    static ResourceConfigCustomizer openApiJerseyTestCatalogEndpoint(ApplicationContext applicationContext) {
        return resourceConfig -> resourceConfig.register(applicationContext.getBean(OpenApiCatalogEndpoint.class));
    }
}

package com.innospots.nexus.sample.spring.platform.openapi.support;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

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
}

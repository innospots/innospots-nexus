package com.innospots.nexus.sample.spring.platform.openapi.support;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.console.config.ConsoleOpenApiConfiguration;
import com.innospots.nexus.spring.console.jaxrs.NexusJaxRsConfiguration;
import com.innospots.nexus.spring.console.jaxrs.NexusScalarJerseyConfiguration;

/**
 * 仅装配 OpenAPI 目录与 Scalar/Jersey，供 HTTP 契约测试使用（不启用完整控制台宿主）。
 */
@SpringBootApplication
@Import({
        ConsoleOpenApiConfiguration.class,
        NexusJaxRsConfiguration.class,
        NexusScalarJerseyConfiguration.class
})
public class OpenApiJerseyTestApplication {
}

package com.innospots.nexus.spring.core.jaxrs;

import java.io.IOException;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.server.model.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;
import com.innospots.nexus.core.openapi.scalar.OpenApiScalarDocumentation;

/**
 * 通过 {@link NexusJerseyResourceConfigurer} 暴露 Scalar 文档页与内置 {@code scalar.js}，
 * OpenAPI 规范仍由 {@link com.innospots.nexus.core.openapi.catalog.OpenApiCatalogEndpoint} 提供。
 *
 * @see OpenApiScalarSpringProperties
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(Resource.class)
@EnableConfigurationProperties(OpenApiScalarSpringProperties.class)
public class NexusScalarJerseyConfiguration {

    @Bean
    NexusJerseyResourceConfigurer scalarJerseyResourceConfigurer(
            OpenApiScalarSpringProperties scalarProperties,
            OpenApiCatalogOperator openApiCatalogOperator) throws IOException {
        if (!scalarProperties.isEnabled()) {
            return resourceConfig -> {
            };
        }

        String docsPath = scalarProperties.resolveDocumentationPath();
        String scriptPath = scalarProperties.resolveScalarJavascriptPath();
        byte[] javascript = OpenApiScalarDocumentation.scalarJavascriptContent();
        String specsBase = scalarProperties.getSpecsBase();

        return resourceConfig -> {
            Resource.Builder page = Resource.builder(docsPath);
            page.addMethod(HttpMethod.GET)
                    .produces(MediaType.TEXT_HTML)
                    .handledBy(new OpenApiScalarJerseyHandlers.DocumentationHtmlInflector(
                            scalarProperties, openApiCatalogOperator, specsBase));

            Resource.Builder script = Resource.builder(scriptPath);
            script.addMethod(HttpMethod.GET)
                    .produces("application/javascript")
                    .handledBy(new OpenApiScalarJerseyHandlers.ScalarJavascriptInflector(javascript));

            resourceConfig.registerResources(page.build(), script.build());
        };
    }
}

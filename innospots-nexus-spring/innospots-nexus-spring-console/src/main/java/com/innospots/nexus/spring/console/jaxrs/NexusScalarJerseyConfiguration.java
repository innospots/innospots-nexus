package com.innospots.nexus.spring.console.jaxrs;

import java.io.IOException;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.model.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;
import com.innospots.nexus.core.openapi.scalar.OpenApiScalarDocumentation;
import com.innospots.nexus.spring.console.config.OpenApiScalarSpringProperties;

/**
 * 通过 {@link NexusJerseyResourceConfigurer} 暴露 Scalar 文档页与内置 {@code scalar.js}，
 * OpenAPI 规范仍由 {@link com.innospots.nexus.core.openapi.catalog.OpenApiCatalogEndpoint} 提供。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
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
                    .handledBy((ContainerRequestContext request) -> {
                        try {
                            String html = OpenApiScalarDocumentation.renderDocumentationHtml(
                                    scalarProperties,
                                    openApiCatalogOperator,
                                    specsBase);
                            return Response.ok(html, "text/html;charset=UTF-8").build();
                        } catch (IOException exception) {
                            return Response.serverError()
                                    .entity("Failed to render OpenAPI documentation UI")
                                    .build();
                        }
                    });

            Resource.Builder script = Resource.builder(scriptPath);
            script.addMethod(HttpMethod.GET)
                    .produces("application/javascript")
                    .handledBy((ContainerRequestContext request) ->
                            Response.ok(javascript, "application/javascript;charset=UTF-8").build()
                    );

            resourceConfig.registerResources(page.build(), script.build());
        };
    }
}

package com.innospots.nexus.spring.console.jaxrs;

import java.io.IOException;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.model.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.openapi.operator.OpenApiCatalogOperator;
import com.innospots.nexus.console.openapi.scalar.OpenApiScalarDocumentation;
import com.scalar.maven.core.ScalarProperties;

/**
 * 通过 {@link NexusJerseyResourceConfigurer} 暴露 Scalar 文档页与内置 {@code scalar.js}，
 * OpenAPI 规范仍由 {@link com.innospots.nexus.console.openapi.endpoint.OpenApiCatalogEndpoint} 提供。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class NexusScalarJerseyConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "scalar")
    ScalarProperties scalarProperties() {
        return OpenApiScalarDocumentation.createDefaultProperties();
    }

    @Bean
    NexusJerseyResourceConfigurer scalarJerseyResourceConfigurer(
            ScalarProperties scalarProperties,
            OpenApiCatalogOperator openApiCatalogOperator) throws IOException {
        if (!scalarProperties.isEnabled()) {
            return resourceConfig -> {
            };
        }

        String html = OpenApiScalarDocumentation.renderDocumentationHtml(scalarProperties, openApiCatalogOperator);
        String docsPath = scalarProperties.getPath();
        byte[] javascript = OpenApiScalarDocumentation.scalarJavascriptContent();

        return resourceConfig -> {
            Resource.Builder page = Resource.builder(docsPath);
            page.addMethod(HttpMethod.GET)
                    .produces(MediaType.TEXT_HTML)
                    .handledBy((ContainerRequestContext request) ->
                            Response.ok(html, "text/html;charset=UTF-8").build()
                    );

            Resource.Builder script = Resource.builder(docsPath + "/scalar.js");
            script.addMethod(HttpMethod.GET)
                    .produces("application/javascript")
                    .handledBy((ContainerRequestContext request) ->
                            Response.ok(javascript, "application/javascript;charset=UTF-8").build()
                    );

            resourceConfig.registerResources(page.build(), script.build());
        };
    }
}

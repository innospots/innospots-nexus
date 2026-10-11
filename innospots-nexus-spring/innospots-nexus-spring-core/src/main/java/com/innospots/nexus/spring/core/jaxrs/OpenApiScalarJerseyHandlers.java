package com.innospots.nexus.spring.core.jaxrs;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.process.Inflector;

import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;
import com.innospots.nexus.core.openapi.scalar.OpenApiScalarDocumentation;

/**
 * Scalar 文档页与 {@code scalar.js} 的程序化 Jersey 路由处理器（显式 {@link Inflector}，避免 lambda 模型警告）。
 */
final class OpenApiScalarJerseyHandlers {

    private OpenApiScalarJerseyHandlers() {
    }

    static final class DocumentationHtmlInflector implements Inflector<ContainerRequestContext, Response> {

        private final OpenApiScalarSpringProperties scalarProperties;
        private final OpenApiCatalogOperator openApiCatalogOperator;
        private final String specsBase;

        DocumentationHtmlInflector(
                OpenApiScalarSpringProperties scalarProperties,
                OpenApiCatalogOperator openApiCatalogOperator,
                String specsBase) {
            this.scalarProperties = scalarProperties;
            this.openApiCatalogOperator = openApiCatalogOperator;
            this.specsBase = specsBase;
        }

        @Override
        public Response apply(ContainerRequestContext request) {
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
        }
    }

    static final class ScalarJavascriptInflector implements Inflector<ContainerRequestContext, Response> {

        private final byte[] javascript;

        ScalarJavascriptInflector(byte[] javascript) {
            this.javascript = javascript;
        }

        @Override
        public Response apply(ContainerRequestContext request) {
            return Response.ok(javascript, "application/javascript;charset=UTF-8").build();
        }
    }
}

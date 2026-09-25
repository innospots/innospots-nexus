package com.innospots.nexus.sample.spring.platform.openapi;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.openapi.endpoint.OpenApiCatalogEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenAPI 目录 JAX-RS 契约（与 console 模块契约测试一致，确保 sample 依赖的端点形状未变）。
 */
class OpenApiCatalogEndpointContractsTest {

    @Test
    void openApiCatalogEndpointExposesSpecRoutes() throws NoSuchMethodException {
        assertThat(OpenApiCatalogEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/openapi/specs");
        assertThat(OpenApiCatalogEndpoint.class.getMethod("listSpecs").getAnnotation(GET.class)).isNotNull();
        assertThat(OpenApiCatalogEndpoint.class.getMethod("listSpecs").getAnnotation(Path.class)).isNull();
        assertThat(OpenApiCatalogEndpoint.class.getMethod("getSpec", String.class).getAnnotation(Path.class).value())
                .isEqualTo("/{specId}");
    }
}

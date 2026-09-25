package com.innospots.nexus.console.openapi.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

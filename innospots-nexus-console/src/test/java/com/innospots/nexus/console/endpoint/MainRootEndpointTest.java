package com.innospots.nexus.console.endpoint;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.OpenApiCatalogPaths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MainRootEndpoint} JAX-RS 契约与重定向行为。
 */
class MainRootEndpointTest {

    @Test
    void pathAndRedirectContract() throws NoSuchMethodException {
        assertThat(MainRootEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/");
        assertThat(MainRootEndpoint.class.getDeclaredMethod("redirectRoot").getAnnotation(GET.class)).isNotNull();
    }

    @Test
    void redirectRootReturnsTemporaryRedirectToConfiguredPath() {
        MainRootEndpoint endpoint = new MainRootEndpoint("/custom/docs");
        Response response = endpoint.redirectRoot();
        assertThat(response.getStatus()).isEqualTo(Response.Status.TEMPORARY_REDIRECT.getStatusCode());
        assertThat(response.getLocation()).isEqualTo(URI.create("/custom/docs"));
    }

    @Test
    void redirectRootFallsBackToOpenApiUiWhenPathBlank() {
        MainRootEndpoint endpoint = new MainRootEndpoint("  ");
        Response response = endpoint.redirectRoot();
        assertThat(response.getLocation()).isEqualTo(URI.create(OpenApiCatalogPaths.UI_DEFAULT));
    }
}

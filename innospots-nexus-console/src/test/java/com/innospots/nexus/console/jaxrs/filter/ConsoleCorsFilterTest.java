package com.innospots.nexus.console.jaxrs.filter;

import java.util.List;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;

import com.innospots.nexus.console.jaxrs.web.ConsoleWebCorsSettings;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ConsoleCorsFilterTest {

    @Test
    void doesNothingWhenCorsDisabled() {
        ConsoleWebCorsSettings cors = new ConsoleWebCorsSettings();
        cors.setEnabled(false);
        ConsoleCorsFilter filter = new ConsoleCorsFilter(cors);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/api/d/nexus/status", "OPTIONS", null);

        filter.filter(request);

        verify(request, never()).abortWith(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void optionsPreflightAbortsWithCorsHeaders() {
        ConsoleWebCorsSettings cors = enabledCors();
        ConsoleCorsFilter filter = new ConsoleCorsFilter(cors);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest(
                "/api/d/nexus/status",
                "OPTIONS",
                java.util.Map.of("Origin", "https://app.example.com"));

        filter.filter(request);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(request).abortWith(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeaderString("Access-Control-Allow-Origin")).isEqualTo("https://app.example.com");
        assertThat(response.getHeaderString("Access-Control-Allow-Methods")).contains("GET");
    }

    @Test
    void nonOptionsRequestDoesNotAbort() {
        ConsoleWebCorsSettings cors = enabledCors();
        ConsoleCorsFilter filter = new ConsoleCorsFilter(cors);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest(
                "/api/d/nexus/status",
                "GET",
                java.util.Map.of("Origin", "https://app.example.com"));

        filter.filter(request);

        verify(request, never()).abortWith(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void responseFilterAddsCorsHeadersWhenOriginPresent() {
        ConsoleWebCorsSettings cors = enabledCors();
        ConsoleCorsFilter filter = new ConsoleCorsFilter(cors);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest(
                "/api/d/nexus/status",
                "GET",
                java.util.Map.of("Origin", "https://app.example.com"));
        ContainerResponseContext response = JaxRsFilterTestSupport.mockResponse();

        filter.filter(request, response);

        assertThat(response.getHeaders().getFirst("Access-Control-Allow-Origin"))
                .isEqualTo("https://app.example.com");
        assertThat(response.getHeaders().getFirst("Access-Control-Allow-Methods")).isNotNull();
        assertThat(response.getHeaders().getFirst("Access-Control-Max-Age")).isEqualTo("3600");
    }

    @Test
    void responseFilterSkipsWhenOriginMissing() {
        ConsoleWebCorsSettings cors = enabledCors();
        ConsoleCorsFilter filter = new ConsoleCorsFilter(cors);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/api/d/nexus/status");
        ContainerResponseContext response = JaxRsFilterTestSupport.mockResponse();

        filter.filter(request, response);

        assertThat(response.getHeaders()).isEmpty();
    }

    @Test
    void wildcardOriginWithoutCredentials() {
        ConsoleWebCorsSettings cors = enabledCors();
        cors.setAllowedOrigins(List.of("*"));
        ConsoleCorsFilter filter = new ConsoleCorsFilter(cors);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest(
                "/x",
                "OPTIONS",
                java.util.Map.of("Origin", "https://app.example.com"));

        filter.filter(request);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(request).abortWith(responseCaptor.capture());
        assertThat(responseCaptor.getValue().getHeaderString("Access-Control-Allow-Origin")).isEqualTo("*");
    }

    private static ConsoleWebCorsSettings enabledCors() {
        ConsoleWebCorsSettings cors = new ConsoleWebCorsSettings();
        cors.setEnabled(true);
        cors.setAllowedOrigins(List.of("https://app.example.com"));
        return cors;
    }
}

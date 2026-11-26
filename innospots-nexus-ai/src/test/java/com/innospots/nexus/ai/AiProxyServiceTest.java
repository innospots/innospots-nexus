package com.innospots.nexus.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.innospots.nexus.ai.config.AiProxyConfig;
import com.innospots.nexus.ai.config.AiRuntimeConfig;
import com.innospots.nexus.ai.health.AiAvailabilityGuard;
import com.innospots.nexus.ai.proxy.AiProxyService;
import com.innospots.nexus.ai.runtime.AiEndpoint;
import com.innospots.nexus.ai.runtime.AiRuntimeStatus;
import com.sun.net.httpserver.HttpServer;

class AiProxyServiceTest {

    @Test
    void forwardsJsonResponse() throws Exception {
        HttpServer upstream = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        upstream.createContext("/v1/agents", exchange -> {
            byte[] body = "{\"items\":[]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        upstream.start();
        try {
            AiEndpoint endpoint = new AiEndpoint(URI.create("http://127.0.0.1:" + upstream.getAddress().getPort()));
            FakeAiRuntime runtime = new FakeAiRuntime(endpoint);
            runtime.state(AiRuntimeStatus.RUNNING);
            AiRuntimeConfig config = AiRuntimeConfig.defaults(java.nio.file.Path.of("/tmp/nexus-ai"));
            AiProxyService service = new AiProxyService(runtime, new AiAvailabilityGuard(runtime, config),
                    AiProxyConfig.defaults());
            AsyncResponse async = mock(AsyncResponse.class);
            UriInfo uriInfo = mock(UriInfo.class);
            when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost/ai/v1/agents"));
            HttpHeaders headers = mock(HttpHeaders.class);

            service.forward("GET", "/v1/agents", uriInfo, headers, null, async);

            ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
            verify(async).resume(captor.capture());
            Response response = captor.getValue();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getEntity()).isInstanceOf(byte[].class);
            assertThat(new String((byte[]) response.getEntity(), StandardCharsets.UTF_8)).contains("items");
        } finally {
            upstream.stop(0);
        }
    }

    @Test
    void returns503WhenRuntimeUnavailable() {
        FakeAiRuntime runtime = new FakeAiRuntime(new AiEndpoint(URI.create("http://127.0.0.1:1")));
        runtime.state(AiRuntimeStatus.STOPPED);
        AiRuntimeConfig config = new AiRuntimeConfig(
                java.nio.file.Path.of("/tmp/nexus-ai"), java.nio.file.Path.of("/tmp/config"), null, "node",
                "127.0.0.1", 0, Duration.ofSeconds(1), Duration.ofSeconds(1), 0,
                AiRuntimeConfig.StartupPolicy.EAGER, Duration.ofSeconds(1), Duration.ofMillis(100),
                Duration.ofMillis(100), 3, false, null, java.util.Map.of());
        AiProxyService service = new AiProxyService(runtime, new AiAvailabilityGuard(runtime, config),
                AiProxyConfig.defaults());
        AsyncResponse async = mock(AsyncResponse.class);
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost/ai/v1/sessions"));
        service.forward("GET", "/v1/sessions", uriInfo, mock(HttpHeaders.class), null, async);
        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(async).resume(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(503);
        assertThat(captor.getValue().getHeaderString("Retry-After")).isNotBlank();
    }
}

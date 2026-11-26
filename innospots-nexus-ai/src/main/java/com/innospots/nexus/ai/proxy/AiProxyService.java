package com.innospots.nexus.ai.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;

import com.innospots.nexus.ai.config.AiProxyConfig;
import com.innospots.nexus.ai.health.AiAvailabilityGuard;
import com.innospots.nexus.ai.health.AiUnavailableException;
import com.innospots.nexus.ai.runtime.AiEndpoint;
import com.innospots.nexus.ai.runtime.AiRuntime;
import com.innospots.nexus.base.thread.TLC;

/**
 * 透明的 loopback HTTP/SSE 转发器。
 */
public final class AiProxyService {

    private final AiRuntime runtime;
    private final AiAvailabilityGuard guard;
    private final AiProxyConfig config;
    private final HttpClient client;

    public AiProxyService(AiRuntime runtime, AiAvailabilityGuard guard, AiProxyConfig config) {
        this.runtime = runtime;
        this.guard = guard;
        this.config = config;
        this.client = HttpClient.newBuilder()
                .connectTimeout(config.connectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * 转发请求。
     *
     * @param method       HTTP 方法
     * @param upstreamPath Node 路径
     * @param uriInfo      外部请求信息
     * @param headers      外部请求头
     * @param body         请求体
     * @param response     异步响应
     */
    public void forward(String method, String upstreamPath, UriInfo uriInfo, HttpHeaders headers,
                        InputStream body, AsyncResponse response) {
        try {
            guard.ensure();
            AiEndpoint endpoint = runtime.endpoint()
                    .orElseThrow(() -> new AiUnavailableException("runtime_unavailable",
                            "AI runtime endpoint is not available", runtime.status(), Duration.ofSeconds(5)));
            URI uri = upstreamUri(endpoint, upstreamPath, uriInfo);
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(config.requestTimeout())
                    .method(method, bodyPublisher(body));
            copyRequestHeaders(headers, builder);
            String userId = TLC.userId();
            if (userId != null && !userId.isBlank()) {
                builder.header("X-Nexus-User", userId);
            }
            builder.header("X-Request-Id", requestId(headers));
            HttpResponse<InputStream> upstream = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            String contentType = upstream.headers().firstValue("content-type").orElse("");
            if (contentType.toLowerCase().contains(MediaType.SERVER_SENT_EVENTS)) {
                sseStream(response, upstream, contentType);
            } else {
                response.resume(normalResponse(upstream, contentType));
            }
        } catch (AiUnavailableException e) {
            response.resume(unavailable(e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.resume(errorResponse(502, "upstream_timeout", "AI runtime request was interrupted"));
        } catch (Exception e) {
            response.resume(errorResponse(502, "upstream_failed", e.getMessage()));
        }
    }

    private Response normalResponse(HttpResponse<InputStream> upstream, String contentType) throws IOException {
        byte[] bytes = upstream.body().readAllBytes();
        Response.ResponseBuilder builder = Response.status(upstream.statusCode())
                .type(contentType.isBlank() ? MediaType.APPLICATION_JSON : contentType)
                .entity(bytes);
        copyResponseHeaders(upstream, builder);
        return builder.build();
    }

    private void sseStream(AsyncResponse response, HttpResponse<InputStream> upstream, String contentType) {
        StreamingOutput output = out -> {
            try (InputStream in = upstream.body()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, read);
                    out.flush();
                }
            }
        };
        Response.ResponseBuilder builder = Response.status(upstream.statusCode())
                .type(contentType)
                .entity(output);
        copyResponseHeaders(upstream, builder);
        response.resume(builder.build());
    }

    private Response unavailable(AiUnavailableException e) {
        long seconds = Math.max(1, e.retryAfter().toSeconds());
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("state", e.state().name());
        details.put("retryAfterSeconds", seconds);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", e.code());
        error.put("message", e.getMessage());
        error.put("requestId", "");
        error.put("details", details);
        return Response.status(503)
                .type(MediaType.APPLICATION_JSON)
                .header("Retry-After", String.valueOf(seconds))
                .entity(Map.of("error", error))
                .build();
    }

    private Response errorResponse(int status, String code, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message == null ? code : message);
        error.put("requestId", "");
        error.put("details", Map.of());
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(Map.of("error", error)).build();
    }

    private static void copyRequestHeaders(HttpHeaders headers, HttpRequest.Builder builder) {
        for (String name : List.of("Accept", "Content-Type", "Last-Event-ID", "X-Request-Id")) {
            String value = headers.getHeaderString(name);
            if (value != null && !value.isBlank()) {
                builder.header(name, value);
            }
        }
    }

    private static void copyResponseHeaders(HttpResponse<?> upstream, Response.ResponseBuilder builder) {
        for (String name : List.of("Cache-Control", "X-Request-Id", "Retry-After")) {
            Optional<String> value = upstream.headers().firstValue(name);
            value.ifPresent(v -> builder.header(name, v));
        }
    }

    private static URI upstreamUri(AiEndpoint endpoint, String path, UriInfo uriInfo) {
        String query = uriInfo.getRequestUri().getRawQuery();
        String base = endpoint.baseUri().toString();
        return URI.create(base + path + (query == null || query.isBlank() ? "" : "?" + query));
    }

    private static HttpRequest.BodyPublisher bodyPublisher(InputStream body) {
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }
        return HttpRequest.BodyPublishers.ofInputStream(() -> body);
    }

    private static String requestId(HttpHeaders headers) {
        String value = headers.getHeaderString("X-Request-Id");
        return value == null || value.isBlank() ? java.util.UUID.randomUUID().toString() : value;
    }
}

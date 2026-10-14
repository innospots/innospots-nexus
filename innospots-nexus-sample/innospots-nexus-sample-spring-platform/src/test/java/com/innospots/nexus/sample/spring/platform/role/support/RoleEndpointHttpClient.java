package com.innospots.nexus.sample.spring.platform.role.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * 角色 REST HTTP 集成测试客户端。
 */
public final class RoleEndpointHttpClient {

    private final int port;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public RoleEndpointHttpClient(int port) {
        this.port = port;
    }

    public HttpResponse<String> get(String path) throws Exception {
        return exchange("GET", path, Map.of(), null);
    }

    public HttpResponse<String> postJson(String path, String jsonBody) throws Exception {
        return exchange("POST", path, Map.of("Content-Type", "application/json"), jsonBody);
    }

    public HttpResponse<String> exchange(
            String method,
            String path,
            Map<String, String> headers,
            String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path));
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        if (headers != null) {
            headers.forEach(builder::header);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}

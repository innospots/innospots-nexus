package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 嵌入式 Jersey Web 测试 HTTP 客户端。
 */
public final class ConsoleJaxRsWebHttpClient {

    private final int port;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public ConsoleJaxRsWebHttpClient(int port) {
        this.port = port;
    }

    public HttpResponse<String> get(String path) throws Exception {
        return exchange("GET", path, Map.of(), null);
    }

    public HttpResponse<String> get(String path, Map<String, String> headers) throws Exception {
        return exchange("GET", path, headers, null);
    }

    public HttpResponse<String> options(String path, Map<String, String> headers) throws Exception {
        return exchange("OPTIONS", path, headers, null);
    }

    public HttpResponse<String> exchange(
            String method,
            String path,
            Map<String, String> headers,
            String bearerToken) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .method(method, HttpRequest.BodyPublishers.noBody());
        Map<String, String> effectiveHeaders = new LinkedHashMap<>();
        if (headers != null) {
            effectiveHeaders.putAll(headers);
        }
        if (bearerToken != null && !bearerToken.isBlank()) {
            effectiveHeaders.put("Authorization", "Bearer " + bearerToken);
        }
        effectiveHeaders.forEach(builder::header);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}

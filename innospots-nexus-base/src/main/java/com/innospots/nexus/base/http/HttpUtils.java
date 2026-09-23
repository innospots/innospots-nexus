package com.innospots.nexus.base.http;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP GET 与 POST（JSON）请求的便捷方法。
 * 使用 {@link HttpClientBuilder} 为每次调用创建短生命周期客户端。
 *
 * @author Smars
 * @date 2026/09/13
 * @see HttpResult
 * @see HttpClientBuilder
 */
public final class HttpUtils {

    private HttpUtils() {
    }

    /**
     * 发送 GET 请求（无自定义请求头）。
     *
     * @param url 请求 URL
     * @return HTTP 结果
     * @throws IOException 请求失败时
     */
    public static HttpResult get(String url) throws IOException {
        return get(url, Map.of());
    }

    /**
     * 发送带自定义请求头的 GET 请求。
     *
     * @param url     请求 URL
     * @param headers 请求头
     * @return HTTP 结果
     * @throws IOException 请求失败时
     */
    public static HttpResult get(String url, Map<String, String> headers) throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            return get(client, url, headers);
        }
    }

    /**
     * 使用已有客户端发送 GET 请求（无自定义请求头）。
     *
     * @param client HTTP 客户端
     * @param url    请求 URL
     * @return HTTP 结果
     * @throws IOException 请求失败时
     */
    public static HttpResult get(CloseableHttpClient client, String url) throws IOException {
        return get(client, url, Map.of());
    }

    /**
     * 使用已有客户端发送带自定义请求头的 GET 请求。
     *
     * @param client  HTTP 客户端
     * @param url     请求 URL
     * @param headers 请求头
     * @return HTTP 结果
     * @throws IOException 请求失败时
     */
    public static HttpResult get(CloseableHttpClient client, String url, Map<String, String> headers) throws IOException {
        HttpGet request = new HttpGet(url);
        applyHeaders(request, headers);
        return execute(client, request);
    }

    /**
     * 发送 JSON POST 请求（无自定义请求头）。
     *
     * @param url      请求 URL
     * @param jsonBody JSON 请求体
     * @return HTTP 结果
     * @throws IOException 请求失败时
     */
    public static HttpResult postJson(String url, String jsonBody) throws IOException {
        return postJson(url, jsonBody, Map.of());
    }

    /**
     * 发送带自定义请求头的 JSON POST 请求。
     *
     * @param url      请求 URL
     * @param jsonBody JSON 请求体
     * @param headers  请求头
     * @return HTTP 结果
     * @throws IOException 请求失败时
     */
    public static HttpResult postJson(String url, String jsonBody, Map<String, String> headers) throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            return postJson(client, url, jsonBody, headers);
        }
    }

    /**
     * 使用已有客户端发送 JSON POST 请求。
     *
     * @param client   HTTP 客户端
     * @param url      请求 URL
     * @param jsonBody JSON 请求体
     * @param headers  请求头
     * @return HTTP 结果
     * @throws IOException 请求失败时
     */
    public static HttpResult postJson(
            CloseableHttpClient client,
            String url,
            String jsonBody,
            Map<String, String> headers
    ) throws IOException {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(jsonBody == null ? "" : jsonBody, ContentType.APPLICATION_JSON));
        applyHeaders(request, headers);
        return execute(client, request);
    }

    /**
     * 使用已有客户端执行 HTTP 请求。
     *
     * @param client  HTTP 客户端
     * @param request HTTP 请求
     * @return HTTP 结果
     * @throws IOException 请求失败时
     */
    public static HttpResult execute(CloseableHttpClient client, org.apache.hc.core5.http.ClassicHttpRequest request)
            throws IOException {
        return client.execute(request, HttpUtils::toResult);
    }

    private static void applyHeaders(org.apache.hc.core5.http.ClassicHttpRequest request, Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        headers.forEach((name, value) -> {
            if (name != null && value != null) {
                request.setHeader(name, value);
            }
        });
    }

    private static HttpResult toResult(ClassicHttpResponse response) throws IOException {
        String body = "";
        if (response.getEntity() != null) {
            try {
                body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            } catch (ParseException e) {
                throw new IOException("Failed to parse HTTP response body", e);
            }
        }
        return new HttpResult(
                response.getCode(),
                response.getReasonPhrase(),
                body,
                headers(response.getHeaders())
        );
    }

    private static Map<String, List<String>> headers(Header[] headers) {
        Map<String, List<String>> values = new LinkedHashMap<>();
        for (Header header : headers) {
            values.computeIfAbsent(header.getName(), key -> new ArrayList<>()).add(header.getValue());
        }
        return Map.copyOf(values);
    }
}

package com.innospots.nexus.service.adapter.test.client;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

/**
 * JDK HTTP 客户端封装。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class HttpTestClient {

    private final HttpClient client;

    /**
     * 使用默认客户端创建。
     */
    public HttpTestClient() {
        this(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build());
    }

    /**
     * 使用指定客户端创建。
     *
     * @param client HTTP 客户端
     */
    public HttpTestClient(HttpClient client) {
        this.client = Checks.notNull(client, "client");
    }

    /**
     * 发送 GET 请求。
     *
     * @param target  目标宿主
     * @param path    相对路径
     * @param timeout 超时
     * @return 交换结果
     */
    public HttpExchange get(AdapterTestTarget target, String path, Duration timeout) {
        Checks.notNull(target, "target");
        Checks.notBlank(path, "path");
        Checks.notNull(timeout, "timeout");
        HttpRequest.Builder builder = HttpRequest.newBuilder(target.resolve(path))
                .GET()
                .timeout(timeout);
        target.defaultHeaders().forEach(builder::header);
        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return toExchange(response);
        } catch (IOException ex) {
            throw new IllegalStateException("HTTP request failed", ex);
        } catch (InterruptedException ex) {
            throw new IllegalStateException("HTTP request failed", ex);
        }
    }

    /**
     * 发送 GET 请求，默认 10 秒超时。
     *
     * @param target 目标宿主
     * @param path   相对路径
     * @return 交换结果
     */
    public HttpExchange get(AdapterTestTarget target, String path) {
        return get(target, path, Duration.ofSeconds(10));
    }

    private static HttpExchange toExchange(HttpResponse<String> response) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        response.headers().map().forEach(headers::put);
        return new HttpExchange(response.statusCode(), headers, response.body());
    }

    /**
     * HTTP 交换结果。
     *
     * @param status  状态码
     * @param headers 响应头
     * @param body    响应体
     */
    public record HttpExchange(int status, Map<String, List<String>> headers, String body) {

        public HttpExchange {
            headers = headers == null ? Map.of() : Map.copyOf(headers);
            body = body == null ? "" : body;
        }

        /**
         * 返回首个匹配头值。
         *
         * @param name 头名
         * @return 头值
         */
        public Optional<String> firstHeader(String name) {
            return headers.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                    .map(Map.Entry::getValue)
                    .filter(values -> !values.isEmpty())
                    .map(values -> values.getFirst())
                    .findFirst();
        }
    }
}

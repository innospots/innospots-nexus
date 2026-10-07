package com.innospots.nexus.base.http;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.Timeout;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link CloseableHttpClient} 的流式构建器，提供合理默认值
 * （连接超时 10s、响应超时 30s、启用重定向）。
 *
 * @author Smars
 * @date 2026/09/13
 * @see HttpUtils
 */
public final class HttpClientBuilder {

    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration responseTimeout = Duration.ofSeconds(30);
    private boolean redirectsEnabled = true;
    private String userAgent;
    private final List<Header> defaultHeaders = new ArrayList<>();

    private HttpClientBuilder() {
    }

    /**
     * 创建构建器实例。
     *
     * @return 构建器
     */
    public static HttpClientBuilder create() {
        return new HttpClientBuilder();
    }

    /**
     * 设置连接超时。为 null 时回退到默认值（10s）。
     *
     * @param connectTimeout 连接超时
     * @return 当前构建器
     */
    public HttpClientBuilder connectTimeout(Duration connectTimeout) {
        if (connectTimeout != null) {
            this.connectTimeout = connectTimeout;
        }
        return this;
    }

    /**
     * 设置响应/套接字超时。为 null 时回退到默认值（30s）。
     *
     * @param responseTimeout 响应超时
     * @return 当前构建器
     */
    public HttpClientBuilder responseTimeout(Duration responseTimeout) {
        if (responseTimeout != null) {
            this.responseTimeout = responseTimeout;
        }
        return this;
    }

    /**
     * 启用或禁用 HTTP 重定向跟随（默认：启用）。
     *
     * @param redirectsEnabled 是否跟随重定向
     * @return 当前构建器
     */
    public HttpClientBuilder redirectsEnabled(boolean redirectsEnabled) {
        this.redirectsEnabled = redirectsEnabled;
        return this;
    }

    /**
     * 设置每个请求发送的 User-Agent 请求头。
     *
     * @param userAgent User-Agent 值
     * @return 当前构建器
     */
    public HttpClientBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * 添加每个请求默认发送的请求头。
     *
     * @param name  请求头名称
     * @param value 请求头值
     * @return 当前构建器
     */
    public HttpClientBuilder defaultHeader(String name, String value) {
        if (name != null && value != null) {
            defaultHeaders.add(new BasicHeader(name, value));
        }
        return this;
    }

    /**
     * 按配置构建 {@link CloseableHttpClient}。
     * 使用带单一连接配置的池化连接管理器。
     *
     * @return HTTP 客户端
     */
    public CloseableHttpClient build() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(timeout(connectTimeout))
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(timeout(responseTimeout))
                .setRedirectsEnabled(redirectsEnabled)
                .build();
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .build();
        org.apache.hc.client5.http.impl.classic.HttpClientBuilder builder = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig);
        if (!defaultHeaders.isEmpty()) {
            builder.setDefaultHeaders(defaultHeaders);
        }
        if (userAgent != null && !userAgent.isBlank()) {
            builder.setUserAgent(userAgent);
        }
        return builder.build();
    }

    private static Timeout timeout(Duration duration) {
        return Timeout.ofMilliseconds(duration.toMillis());
    }
}

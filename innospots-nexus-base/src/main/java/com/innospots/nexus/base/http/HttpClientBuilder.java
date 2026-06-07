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
 * Fluent builder for {@link CloseableHttpClient} instances with sensible
 * defaults (10s connect timeout, 30s response timeout, redirects enabled).
 */
public final class HttpClientBuilder {

    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration responseTimeout = Duration.ofSeconds(30);
    private boolean redirectsEnabled = true;
    private String userAgent;
    private final List<Header> defaultHeaders = new ArrayList<>();

    private HttpClientBuilder() {
    }

    public static HttpClientBuilder create() {
        return new HttpClientBuilder();
    }

    /** Sets the connection timeout. Falls back to default (10s) if null. */
    public HttpClientBuilder connectTimeout(Duration connectTimeout) {
        if (connectTimeout != null) {
            this.connectTimeout = connectTimeout;
        }
        return this;
    }

    /** Sets the response/socket timeout. Falls back to default (30s) if null. */
    public HttpClientBuilder responseTimeout(Duration responseTimeout) {
        if (responseTimeout != null) {
            this.responseTimeout = responseTimeout;
        }
        return this;
    }

    /** Enables or disables HTTP redirect following (default: enabled). */
    public HttpClientBuilder redirectsEnabled(boolean redirectsEnabled) {
        this.redirectsEnabled = redirectsEnabled;
        return this;
    }

    /** Sets the User-Agent header to be sent with every request. */
    public HttpClientBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /** Adds a default header to be sent with every request. */
    public HttpClientBuilder defaultHeader(String name, String value) {
        if (name != null && value != null) {
            defaultHeaders.add(new BasicHeader(name, value));
        }
        return this;
    }

    /**
     * Builds a {@link CloseableHttpClient} with the configured settings.
     * Uses a pooled connection manager with a single connection config.
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

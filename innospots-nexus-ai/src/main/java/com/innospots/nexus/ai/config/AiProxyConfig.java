package com.innospots.nexus.ai.config;

import java.time.Duration;
import java.util.List;

/**
 * Java 反向代理配置。
 *
 * @param pathPrefix          对外前缀
 * @param maxRequestBodyBytes 请求体上限
 * @param connectTimeout      连接超时
 * @param requestTimeout      普通请求超时
 * @param sseIdleTimeout      SSE 空闲超时
 * @param allowedRoutes       精确路由
 * @param anonymousRoutes     匿名路由（Node 内路径）
 * @param failFastOnUnavailable 不可用时快速失败
 * @param retryAfter          503 的 Retry-After
 */
public record AiProxyConfig(
        String pathPrefix,
        int maxRequestBodyBytes,
        Duration connectTimeout,
        Duration requestTimeout,
        Duration sseIdleTimeout,
        List<ProxyRoute> allowedRoutes,
        List<ProxyRoute> anonymousRoutes,
        boolean failFastOnUnavailable,
        Duration retryAfter) {

    /**
     * 一条代理路由。
     *
     * @param method HTTP 方法
     * @param path   Node 路径
     */
    public record ProxyRoute(String method, String path) {
    }

    /**
     * 默认代理配置。
     *
     * @return 配置
     */
    public static AiProxyConfig defaults() {
        return new AiProxyConfig(
                "/ai",
                8 * 1024 * 1024,
                Duration.ofSeconds(5),
                Duration.ofSeconds(60),
                Duration.ofSeconds(120),
                List.of(
                        new ProxyRoute("GET", "/v1/openapi.yaml"),
                        new ProxyRoute("GET", "/v1/agents"),
                        new ProxyRoute("POST", "/v1/sessions"),
                        new ProxyRoute("GET", "/v1/sessions"),
                        new ProxyRoute("GET", "/v1/sessions/{id}"),
                        new ProxyRoute("DELETE", "/v1/sessions/{id}"),
                        new ProxyRoute("GET", "/v1/sessions/{id}/messages"),
                        new ProxyRoute("POST", "/v1/sessions/{id}/messages"),
                        new ProxyRoute("GET", "/v1/runs/{id}"),
                        new ProxyRoute("POST", "/v1/runs/{id}/cancel")),
                List.of(new ProxyRoute("GET", "/v1/openapi.yaml")),
                true,
                Duration.ofSeconds(5));
    }
}

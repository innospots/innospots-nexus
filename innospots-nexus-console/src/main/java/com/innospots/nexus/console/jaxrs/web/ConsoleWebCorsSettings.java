package com.innospots.nexus.console.jaxrs.web;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Jersey {@code ContainerResponseFilter} 输出的 CORS 头配置。
 */
@Getter
@Setter
public class ConsoleWebCorsSettings {

    /** 是否向响应附加 CORS 头；默认 {@code false}。 */
    private boolean enabled = false;

    /** {@code Access-Control-Allow-Origin}；默认 {@code *}。 */
    private List<String> allowedOrigins = List.of("*");

    /** {@code Access-Control-Allow-Methods}。 */
    private List<String> allowedMethods = List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD");

    /** {@code Access-Control-Allow-Headers}。 */
    private List<String> allowedHeaders = List.of("*");

    /** {@code Access-Control-Expose-Headers}。 */
    private List<String> exposedHeaders = List.of();

    /** {@code Access-Control-Allow-Credentials}。 */
    private boolean allowCredentials = false;

    /** 预检请求 {@code Access-Control-Max-Age}（秒）。 */
    private long maxAgeSeconds = 3600L;

    /**
     * 设置允许的来源列表。
     *
     * @param allowedOrigins 来源；{@code null} 时置为空列表
     */
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }

    /**
     * 设置允许的 HTTP 方法列表。
     *
     * @param allowedMethods 方法名；{@code null} 时置为空列表
     */
    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods == null ? List.of() : List.copyOf(allowedMethods);
    }

    /**
     * 设置允许的请求头列表。
     *
     * @param allowedHeaders 头名；{@code null} 时置为空列表
     */
    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders == null ? List.of() : List.copyOf(allowedHeaders);
    }

    /**
     * 设置暴露给浏览器的响应头列表。
     *
     * @param exposedHeaders 头名；{@code null} 时置为空列表
     */
    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders == null ? List.of() : List.copyOf(exposedHeaders);
    }
}

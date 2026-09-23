package com.innospots.nexus.spring.service.http.mvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;
import com.innospots.nexus.service.http.header.RequestIdPolicy;
import com.innospots.nexus.service.http.header.StandardHeaders;
import com.innospots.nexus.service.runtime.cancellation.CancellationSource;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * 从 Servlet 或 WebFlux 请求构造 {@link ServiceContext}。
 */
public final class ServiceTransportSupport {

    private final RequestIdPolicy requestIdPolicy;

    /**
     * 创建传输支持组件。
     *
     * @param requestIdPolicy requestId 策略
     */
    public ServiceTransportSupport(RequestIdPolicy requestIdPolicy) {
        this.requestIdPolicy = Checks.notNull(requestIdPolicy, "requestIdPolicy");
    }

    /**
     * 构造预认证上下文。
     *
     * @param request             HTTP 请求
     * @param cancellationSource  取消源
     * @return 服务上下文
     */
    public ServiceContext buildContext(HttpServletRequest request, CancellationSource cancellationSource) {
        Checks.notNull(request, "request");
        Checks.notNull(cancellationSource, "cancellationSource");
        return buildContext(
                request.getMethod(),
                request.getRequestURI(),
                servletHeaders(request),
                remoteAddress(request),
                request.getHeader(StandardHeaders.CLIENT_ID),
                cancellationSource);
    }

    /**
     * 从 WebFlux 请求构造预认证上下文。
     *
     * @param request             HTTP 请求
     * @param cancellationSource  取消源
     * @return 服务上下文
     */
    public ServiceContext buildContext(ServerHttpRequest request, CancellationSource cancellationSource) {
        Checks.notNull(request, "request");
        Checks.notNull(cancellationSource, "cancellationSource");
        Map<String, List<String>> headers = reactiveHeaders(request);
        return buildContext(
                request.getMethod().name(),
                request.getURI().getRawPath(),
                headers,
                reactiveRemoteAddress(request),
                request.getHeaders().getFirst(StandardHeaders.CLIENT_ID),
                cancellationSource);
    }

    /**
     * 从握手请求头构造预认证上下文。
     *
     * @param method             HTTP 方法
     * @param path               请求路径
     * @param headers            小写化请求头
     * @param cancellationSource 取消源
     * @return 服务上下文
     */
    public ServiceContext buildContext(
            String method,
            String path,
            Map<String, List<String>> headers,
            CancellationSource cancellationSource) {
        Checks.notBlank(method, "method");
        Checks.notBlank(path, "path");
        Checks.notNull(headers, "headers");
        Checks.notNull(cancellationSource, "cancellationSource");
        return buildContext(
                method,
                path,
                headers,
                firstHeader(headers, "x-forwarded-for", "127.0.0.1"),
                firstHeader(headers, StandardHeaders.CLIENT_ID, null),
                cancellationSource);
    }

    private ServiceContext buildContext(
            String method,
            String path,
            Map<String, List<String>> headers,
            String remoteAddress,
            String clientId,
            CancellationSource cancellationSource) {
        String requestId = requestIdPolicy.resolve(Optional.ofNullable(firstHeader(headers, StandardHeaders.REQUEST_ID, null)));
        RequestMetadata metadata = new RequestMetadata(method, path, path, headers, remoteAddress, clientId);
        return new ServiceContext(
                requestId,
                metadata,
                ServicePrincipal.anonymous("adapter"),
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                cancellationSource.token(),
                Deadline.unlimited(),
                ContextAttributes.empty());
    }

    private static String firstHeader(Map<String, List<String>> headers, String name, String defaultValue) {
        List<String> values = headers.get(name.toLowerCase(Locale.ROOT));
        if (values == null || values.isEmpty()) {
            return defaultValue;
        }
        String value = values.getFirst();
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        if ("x-forwarded-for".equalsIgnoreCase(name)) {
            return value.split(",")[0].trim();
        }
        return value;
    }

    private static Map<String, List<String>> headers(HttpServletRequest request) {
        return servletHeaders(request);
    }

    /**
     * 复制 Servlet 请求头为小写 map。
     *
     * @param request HTTP 请求
     * @return 请求头
     */
    public static Map<String, List<String>> servletHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement().toLowerCase(Locale.ROOT);
            List<String> values = new ArrayList<>();
            Enumeration<String> headerValues = request.getHeaders(name);
            while (headerValues.hasMoreElements()) {
                values.add(headerValues.nextElement());
            }
            headers.put(name, List.copyOf(values));
        }
        return headers;
    }

    private static String remoteAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return Optional.ofNullable(request.getRemoteAddr()).orElse("127.0.0.1");
    }

    private static Map<String, List<String>> reactiveHeaders(ServerHttpRequest request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        request.getHeaders().forEach((name, values) -> headers.put(name.toLowerCase(Locale.ROOT), List.copyOf(values)));
        return headers;
    }

    private static String reactiveRemoteAddress(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return Optional.ofNullable(request.getRemoteAddress())
                .map(address -> address.getAddress().getHostAddress())
                .orElse("127.0.0.1");
    }
}

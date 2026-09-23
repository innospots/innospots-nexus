package com.innospots.nexus.quarkus.service.rest;

import java.util.ArrayList;
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;

/**
 * 从 JAX-RS 请求构造 {@link ServiceContext}。
 */
@ApplicationScoped
public final class ServiceTransportSupport {

    private final RequestIdPolicy requestIdPolicy;

    public ServiceTransportSupport(RequestIdPolicy requestIdPolicy) {
        this.requestIdPolicy = Checks.notNull(requestIdPolicy, "requestIdPolicy");
    }

    /**
     * 构造预认证上下文。
     *
     * @param requestContext     REST 请求上下文
     * @param cancellationSource 取消源
     * @return 服务上下文
     */
    public ServiceContext buildContext(ContainerRequestContext requestContext, CancellationSource cancellationSource) {
        Checks.notNull(requestContext, "requestContext");
        Checks.notNull(cancellationSource, "cancellationSource");
        return buildContext(
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri().getPath(),
                headers(requestContext),
                remoteAddress(requestContext),
                requestContext.getHeaderString(StandardHeaders.CLIENT_ID),
                cancellationSource);
    }

    /**
     * 从握手请求构造预认证上下文。
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

    private static Map<String, List<String>> headers(ContainerRequestContext requestContext) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        requestContext.getHeaders().forEach((name, values) -> {
            List<String> copied = new ArrayList<>();
            if (values != null) {
                copied.addAll(values);
            }
            headers.put(name.toLowerCase(Locale.ROOT), List.copyOf(copied));
        });
        return headers;
    }

    private static String remoteAddress(ContainerRequestContext requestContext) {
        String forwarded = requestContext.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return "127.0.0.1";
    }
}

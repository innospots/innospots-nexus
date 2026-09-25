package com.innospots.nexus.spring.console.jaxrs.filter;

import java.util.List;
import java.util.StringJoiner;

import jakarta.annotation.Priority;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.spring.console.config.ConsoleWebProperties;

/**
 * 可配置的 JAX-RS CORS 支持；默认关闭。
 */
@Provider
@Priority(50)
public final class ConsoleCorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private final ConsoleWebProperties.Cors cors;

    public ConsoleCorsFilter(ConsoleWebProperties webProperties) {
        this.cors = webProperties.getCors();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!cors.isEnabled()) {
            return;
        }
        if (!HttpMethod.OPTIONS.equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }
        Response.ResponseBuilder builder = Response.ok();
        applyHeaders(builder, requestContext.getHeaderString("Origin"));
        requestContext.abortWith(builder.build());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (!cors.isEnabled()) {
            return;
        }
        String origin = requestContext.getHeaderString("Origin");
        if (origin == null || origin.isBlank()) {
            return;
        }
        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", resolveOrigin(origin));
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", join(cors.getAllowedMethods()));
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", join(cors.getAllowedHeaders()));
        if (!cors.getExposedHeaders().isEmpty()) {
            responseContext.getHeaders().putSingle("Access-Control-Expose-Headers", join(cors.getExposedHeaders()));
        }
        if (cors.isAllowCredentials()) {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        }
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", String.valueOf(cors.getMaxAgeSeconds()));
    }

    private void applyHeaders(Response.ResponseBuilder builder, String origin) {
        builder.header("Access-Control-Allow-Origin", resolveOrigin(origin));
        builder.header("Access-Control-Allow-Methods", join(cors.getAllowedMethods()));
        builder.header("Access-Control-Allow-Headers", join(cors.getAllowedHeaders()));
        if (cors.isAllowCredentials()) {
            builder.header("Access-Control-Allow-Credentials", "true");
        }
        builder.header("Access-Control-Max-Age", String.valueOf(cors.getMaxAgeSeconds()));
    }

    private String resolveOrigin(String requestOrigin) {
        List<String> allowed = cors.getAllowedOrigins();
        if (allowed.contains("*")) {
            return cors.isAllowCredentials() && requestOrigin != null && !requestOrigin.isBlank()
                    ? requestOrigin
                    : "*";
        }
        if (requestOrigin != null && allowed.contains(requestOrigin)) {
            return requestOrigin;
        }
        return allowed.isEmpty() ? "*" : allowed.getFirst();
    }

    private static String join(List<String> values) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String value : values) {
            joiner.add(value);
        }
        return joiner.toString();
    }
}

package com.innospots.nexus.spring.console.jaxrs.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.spring.console.config.ConsoleWebProperties;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleAntPathMatcher;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleHttpHeaders;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleTokenSessionBinder;

/**
 * 解析 Bearer 访问令牌并填充 {@link com.innospots.nexus.base.thread.SessionContext}。
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public final class ConsoleAuthenticationFilter implements ContainerRequestFilter {

    private final ConsoleWebProperties.Security security;
    private final TokenIssuer tokenIssuer;

    public ConsoleAuthenticationFilter(ConsoleWebProperties webProperties, TokenIssuer tokenIssuer) {
        this.security = webProperties.getSecurity();
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = normalizedPath(requestContext);
        if (ConsoleAntPathMatcher.matchesAny(security.getPermitAllPatterns(), path)) {
            return;
        }
        String authorization = requestContext.getHeaderString(ConsoleHttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        String token = extractBearerToken(authorization);
        ConsoleTokenSessionBinder.bindAccessToken(tokenIssuer.parse(token));
    }

    private static String extractBearerToken(String authorization) {
        String prefix = "Bearer ";
        if (!authorization.regionMatches(true, 0, prefix, 0, prefix.length())) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        String token = authorization.substring(prefix.length()).trim();
        if (token.isEmpty()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        return token;
    }

    private static String normalizedPath(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}

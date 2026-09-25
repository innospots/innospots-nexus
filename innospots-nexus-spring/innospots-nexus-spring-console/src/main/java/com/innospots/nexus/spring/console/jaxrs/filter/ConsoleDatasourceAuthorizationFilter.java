package com.innospots.nexus.spring.console.jaxrs.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.permission.authorization.AuthorizationDecision;
import com.innospots.nexus.console.permission.authorization.AuthorizationRequest;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.RequestAuthorizer;
import com.innospots.nexus.spring.console.config.ConsoleWebProperties;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleAntPathMatcher;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleWebRequestProperties;

/**
 * 对 catalog datasource 代理路径执行 {@link RequestAuthorizer} 鉴权。
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 100)
public final class ConsoleDatasourceAuthorizationFilter implements ContainerRequestFilter {

    private final ConsoleWebProperties.Security security;
    private final RequestAuthorizer requestAuthorizer;
    private final AuthorizationSubjectResolver subjectResolver;

    public ConsoleDatasourceAuthorizationFilter(
            ConsoleWebProperties webProperties,
            RequestAuthorizer requestAuthorizer,
            AuthorizationSubjectResolver subjectResolver) {
        this.security = webProperties.getSecurity();
        this.requestAuthorizer = requestAuthorizer;
        this.subjectResolver = subjectResolver;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = normalizedPath(requestContext);
        if (!ConsoleAntPathMatcher.matchesAny(security.getDatasourcePathPatterns(), path)) {
            return;
        }
        String pageKey = requestContext.getHeaderString(security.getPageKeyHeader());
        if (pageKey == null || pageKey.isBlank()) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(
                SessionContext.requireWorkspaceId(),
                requestContext.getMethod(),
                path,
                pageKey,
                subjectResolver.resolve()
                        .orElseThrow(() -> NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED)));
        AuthorizationDecision decision = requestAuthorizer.authorize(authorizationRequest);
        if (!decision.allowed()) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        requestContext.setProperty(ConsoleWebRequestProperties.AUTHORIZATION_CONTEXT, decision.context());
    }

    private static String normalizedPath(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}

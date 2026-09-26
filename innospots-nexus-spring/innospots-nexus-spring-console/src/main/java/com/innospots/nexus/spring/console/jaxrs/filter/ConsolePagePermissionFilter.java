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
import com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer;
import com.innospots.nexus.spring.console.config.ConsoleWebProperties;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleAntPathMatcher;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleWebRequestProperties;

/**
 * 对 {@link ConsoleWebProperties.Security#getConsolePathPatterns()} 命中的路径执行控制台页面权限校验
 *（在 {@link ConsoleAuthenticationFilter} Bearer 鉴权之后）。
 *
 * <p>将 HTTP 方法、路径与 {@link ConsoleWebProperties.Security#getPageKeyHeader()} 转为
 * {@link AuthorizationRequest}，委托 {@link ConsolePagePermissionAuthorizer} 判定 catalog PAGE/DATASOURCE
 * 授权；与「是否已登录」的认证 Filter 语义区分。</p>
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 100)
public final class ConsolePagePermissionFilter implements ContainerRequestFilter {

    private final ConsoleWebProperties.Security security;
    private final ConsolePagePermissionAuthorizer pagePermissionAuthorizer;
    private final AuthorizationSubjectResolver subjectResolver;

    public ConsolePagePermissionFilter(
            ConsoleWebProperties webProperties,
            ConsolePagePermissionAuthorizer pagePermissionAuthorizer,
            AuthorizationSubjectResolver subjectResolver) {
        this.security = webProperties.getSecurity();
        this.pagePermissionAuthorizer = pagePermissionAuthorizer;
        this.subjectResolver = subjectResolver;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!security.isEnabled()) {
            return;
        }
        String path = normalizedPath(requestContext);
        if (!ConsoleAntPathMatcher.matchesAny(security.getConsolePathPatterns(), path)) {
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
        AuthorizationDecision decision = pagePermissionAuthorizer.authorize(authorizationRequest);
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

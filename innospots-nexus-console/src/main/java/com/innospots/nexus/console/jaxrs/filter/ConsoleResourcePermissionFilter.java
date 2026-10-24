package com.innospots.nexus.console.jaxrs.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.permission.authorization.AuthorizationDecision;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.ConsoleResourcePermissionAuthorizer;
import com.innospots.nexus.console.jaxrs.support.ConsoleAntPathMatcher;
import com.innospots.nexus.console.jaxrs.support.ConsoleRequestGrants;
import com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings;
import com.innospots.nexus.console.jaxrs.support.ConsolePublicApiPaths;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.core.jaxrs.support.RequestProperties;

/**
 * 对 {@link ConsoleWebSecuritySettings#getConsolePathPatterns()} 命中的路径执行控制台资源权限校验
 *（在 {@link ConsoleAuthenticationFilter} Bearer 鉴权之后）。
 *
 * <p>公共 API（{@link com.innospots.nexus.console.config.ConsoleConstant#PUBLIC_API_PREFIX}）不执行权限校验。
 * 读取请求上的 {@link ResolvedEndpointGrant}，按 {@code domainKey.moduleKey:grantName} 拼资源键，
 * 委托 {@link ConsoleResourcePermissionAuthorizer} 判定是否落在授权表中；与「是否已登录」的认证 Filter 语义区分。</p>
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 100)
public final class ConsoleResourcePermissionFilter implements ContainerRequestFilter {

    private final ConsoleWebSecuritySettings security;
    private final ConsoleResourcePermissionAuthorizer resourcePermissionAuthorizer;
    private final AuthorizationSubjectResolver subjectResolver;

    public ConsoleResourcePermissionFilter(
            ConsoleWebSecuritySettings security,
            ConsoleResourcePermissionAuthorizer resourcePermissionAuthorizer,
            AuthorizationSubjectResolver subjectResolver) {
        this.security = security;
        this.resourcePermissionAuthorizer = resourcePermissionAuthorizer;
        this.subjectResolver = subjectResolver;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!security.isEnabled()) {
            return;
        }
        ResolvedEndpointGrant grant = ConsoleRequestGrants.resolvedGrant(requestContext);
        if (grant == null || grant.ignorePermission()) {
            return;
        }
        String path = normalizedPath(requestContext);
        if (ConsolePublicApiPaths.matches(path)) {
            return;
        }
        if (!ConsoleAntPathMatcher.matchesAny(security.getConsolePathPatterns(), path)) {
            return;
        }
        String workspaceId = SessionContext.workspaceId();
        if (ConsoleSessionContext.scope() != ConsoleSessionScope.PLATFORM) {
            workspaceId = SessionContext.requireWorkspaceId();
        }
        AuthorizationDecision decision = resourcePermissionAuthorizer.authorizeEndpoint(
                workspaceId,
                grant,
                subjectResolver.resolve()
                        .orElseThrow(() -> ConsoleExceptions.build(NexusStatusCode.AUTHENTICATION_FAILED)));
        if (!decision.allowed()) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        requestContext.setProperty(RequestProperties.AUTHORIZATION_CONTEXT, decision.context());
    }

    private static String normalizedPath(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}

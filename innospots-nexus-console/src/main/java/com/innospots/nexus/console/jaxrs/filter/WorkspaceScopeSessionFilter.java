package com.innospots.nexus.console.jaxrs.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.thread.SessionContext;

/**
 * 按业务方解析结果绑定租户、工作区（可选项目）作用域。
 *
 * <p>执行位置：{@link ConsoleAuthenticationFilter} 之后
 * （{@link Priorities#AUTHENTICATION} + 50），早于
 * {@link ConsoleResourcePermissionFilter}（{@link Priorities#AUTHENTICATION} + 100），
 * 这样资源授权与业务查询都能读到最终作用域。</p>
 *
 * <p><b>与令牌的分工：</b>令牌只承载身份与安全域。租户、工作区、项目都属于
 * 「当前操作上下文」，由客户端状态（cookie）声明，本过滤器负责校验与绑定。</p>
 *
 * <p>本过滤器只做绑定：「客户端选了什么」「该用户能否进入」由
 * {@link WorkspaceScopeResolver} 决定，console 不感知业务语义。</p>
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 50)
public final class WorkspaceScopeSessionFilter implements ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceScopeSessionFilter.class);

    /** 标准 Cookie 请求头。 */
    private static final String COOKIE_HEADER = "Cookie";

    private final WorkspaceScopeResolver workspaceScopeResolver;

    /**
     * 创建过滤器。
     *
     * @param workspaceScopeResolver 业务方提供的作用域解析器
     */
    public WorkspaceScopeSessionFilter(WorkspaceScopeResolver workspaceScopeResolver) {
        this.workspaceScopeResolver = workspaceScopeResolver;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String cookieHeader = requestContext.getHeaderString(COOKIE_HEADER);
        WorkspaceScopeResolver.ResolvedWorkspaceScope scope =
                workspaceScopeResolver.resolve(cookieHeader);
        if (scope == null || !scope.isValid()) {
            return;
        }
        bindTenant(scope);
        bindWorkspace(scope);
        bindProject(scope);
        LOG.debug("scope bound tenantId={} workspaceId={} projectId={}",
                scope.tenantId(), scope.workspaceId(), scope.projectId());
    }

    private static void bindTenant(WorkspaceScopeResolver.ResolvedWorkspaceScope scope) {
        SessionContext.bindTenant(
                new TenantSnapshot(
                        scope.tenantId(),
                        scope.tenantCode(),
                        scope.tenantName(),
                        BasicStatus.ENABLED),
                new OrganizationSnapshot(
                        scope.tenantId(),
                        scope.tenantCode(),
                        scope.tenantName(),
                        null,
                        null,
                        null,
                        BasicStatus.ENABLED));
    }

    private static void bindWorkspace(WorkspaceScopeResolver.ResolvedWorkspaceScope scope) {
        SessionContext.bindWorkspace(new WorkspaceSnapshot(
                scope.tenantId(),
                scope.workspaceId(),
                scope.workspaceCode(),
                scope.workspaceName(),
                BasicStatus.ENABLED));
    }

    /**
     * 绑项目；未声明项目时清除残留，避免沿用上一次请求的上下文。
     */
    private static void bindProject(WorkspaceScopeResolver.ResolvedWorkspaceScope scope) {
        if (scope.projectId() == null || scope.projectId().isBlank()) {
            SessionContext.clearProject();
            return;
        }
        SessionContext.bindProject(new ProjectSnapshot(
                scope.tenantId(),
                scope.workspaceId(),
                scope.projectId(),
                scope.projectCode(),
                scope.projectName(),
                null,
                BasicStatus.ENABLED));
    }
}

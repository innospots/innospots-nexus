package com.innospots.nexus.console.scope.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.api.MembershipDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.TenantMembership;
import com.innospots.nexus.console.auth.domain.request.SelectProjectRequest;
import com.innospots.nexus.console.auth.domain.request.SelectWorkspaceRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.auth.service.AuthFacade;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.scope.api.ProjectScopeDirectory;
import com.innospots.nexus.console.scope.api.WorkspaceScopeDirectory;

/**
 * Activates workspace and project scope for tenant business sessions.
 */
@Slf4j
@RequiredArgsConstructor
public class ScopeFacade {

    private final MembershipDirectory membershipDirectory;
    private final WorkspaceScopeDirectory workspaceScopeDirectory;
    private final ProjectScopeDirectory projectScopeDirectory;
    private final SessionScopeBinder sessionScopeBinder;
    private final AuthTokenPairIssuer tokenPairIssuer;

    /**
     * Exchanges a tenant business session for a workspace-scoped token pair.
     *
     * @param tenantUserId tenant-realm user identifier
     * @param request      tenant and workspace to activate
     * @return workspace-scoped business token pair
     */
    public AuthTokenVo selectWorkspace(String tenantUserId, SelectWorkspaceRequest request) {
        Checks.notBlank(tenantUserId, "tenantUserId");
        Checks.notNull(request, "request");
        Checks.notBlank(request.tenantId(), "tenantId");
        Checks.notBlank(request.workspaceId(), "workspaceId");
        TenantMembership membership = requireMembership(tenantUserId, request.tenantId());
        WorkspaceSnapshot workspace = workspaceScopeDirectory
                .findWorkspace(request.tenantId(), request.workspaceId())
                .orElseThrow(() -> NexusException.build(NexusStatusCode.NO_PERMISSION));
        AuthSessionScope scope = new AuthSessionScope(
                AuthFacade.TOKEN_TYPE_BUSINESS,
                membership.tenantId(),
                membership.tenantMemberId(),
                workspace.workspaceId(),
                null);
        sessionScopeBinder.bindScope(scope);
        return tokenPairIssuer.issue(SecurityRealm.TENANT, tenantUserId, scope);
    }

    /**
     * Exchanges a workspace-scoped session for a project-scoped token pair.
     *
     * @param tenantUserId tenant-realm user identifier
     * @param request      tenant, workspace, and project to activate
     * @return project-scoped business token pair
     */
    public AuthTokenVo selectProject(String tenantUserId, SelectProjectRequest request) {
        Checks.notBlank(tenantUserId, "tenantUserId");
        Checks.notNull(request, "request");
        Checks.notBlank(request.tenantId(), "tenantId");
        Checks.notBlank(request.workspaceId(), "workspaceId");
        Checks.notBlank(request.projectId(), "projectId");
        TenantMembership membership = requireMembership(tenantUserId, request.tenantId());
        WorkspaceSnapshot workspace = workspaceScopeDirectory
                .findWorkspace(request.tenantId(), request.workspaceId())
                .orElseThrow(() -> NexusException.build(NexusStatusCode.NO_PERMISSION));
        ProjectSnapshot project = projectScopeDirectory
                .findProject(request.tenantId(), request.workspaceId(), request.projectId())
                .orElseThrow(() -> NexusException.build(NexusStatusCode.NO_PERMISSION));
        AuthSessionScope scope = new AuthSessionScope(
                AuthFacade.TOKEN_TYPE_BUSINESS,
                membership.tenantId(),
                membership.tenantMemberId(),
                workspace.workspaceId(),
                project.projectId());
        sessionScopeBinder.bindScope(scope);
        return tokenPairIssuer.issue(SecurityRealm.TENANT, tenantUserId, scope);
    }

    private TenantMembership requireMembership(String tenantUserId, String tenantId) {
        return membershipDirectory.listActiveMemberships(tenantUserId).stream()
                .filter(item -> tenantId.equals(item.tenantId()))
                .findFirst()
                .orElseThrow(() -> NexusException.build(NexusStatusCode.NO_PERMISSION));
    }
}

package com.innospots.nexus.kernel.scope.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.kernel.auth.api.MembershipDirectory;
import com.innospots.nexus.kernel.auth.domain.model.TenantMembership;
import com.innospots.nexus.kernel.scope.domain.request.SelectProjectRequest;
import com.innospots.nexus.kernel.scope.domain.request.SelectWorkspaceRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.auth.service.AuthFacade;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.kernel.scope.KernelProjectScopeDirectory;
import com.innospots.nexus.kernel.scope.KernelWorkspaceScopeDirectory;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;

/**
 * 为租户业务会话激活工作区与项目作用域。
 */
@Slf4j
@RequiredArgsConstructor
public class ScopeFacade {

    private final MembershipDirectory membershipDirectory;
    private final KernelWorkspaceScopeDirectory workspaceScopeDirectory;
    private final KernelProjectScopeDirectory projectScopeDirectory;
    private final SessionScopeBinder sessionScopeBinder;
    private final AuthTokenPairIssuer tokenPairIssuer;

    /**
     * 将租户业务会话交换为工作区作用域令牌对。
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
     * 将工作区作用域会话交换为项目作用域令牌对。
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

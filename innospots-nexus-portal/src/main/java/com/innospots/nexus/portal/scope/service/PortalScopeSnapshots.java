package com.innospots.nexus.portal.scope.service;

import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.portal.scope.PortalProjectScopeDirectory;
import com.innospots.nexus.portal.scope.PortalWorkspaceScopeDirectory;

/**
 * 将 portal 作用域目录解析结果绑定到 {@link SessionContext}。
 */
public final class PortalScopeSnapshots {

    private PortalScopeSnapshots() {
    }

    public static void bindWorkspaceAndProject(
            AuthSessionScope scope,
            PortalWorkspaceScopeDirectory workspaceScopeDirectory,
            PortalProjectScopeDirectory projectScopeDirectory) {
        if (scope.workspaceId() != null && !scope.workspaceId().isBlank()) {
            workspaceScopeDirectory.findWorkspace(scope.tenantId(), scope.workspaceId())
                    .ifPresent(SessionContext::bindWorkspace);
        }
        if (scope.projectId() != null && !scope.projectId().isBlank()) {
            projectScopeDirectory.findProject(scope.tenantId(), scope.workspaceId(), scope.projectId())
                    .ifPresent(SessionContext::bindProject);
        }
    }
}

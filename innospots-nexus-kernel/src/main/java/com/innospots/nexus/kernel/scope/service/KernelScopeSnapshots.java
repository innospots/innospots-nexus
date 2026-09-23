package com.innospots.nexus.kernel.scope.service;

import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.kernel.scope.KernelProjectScopeDirectory;
import com.innospots.nexus.kernel.scope.KernelWorkspaceScopeDirectory;

/**
 * 将 kernel 作用域目录解析结果绑定到 {@link SessionContext}。
 */
public final class KernelScopeSnapshots {

    private KernelScopeSnapshots() {
    }

    public static void bindWorkspaceAndProject(
            AuthSessionScope scope,
            KernelWorkspaceScopeDirectory workspaceScopeDirectory,
            KernelProjectScopeDirectory projectScopeDirectory) {
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

package com.innospots.nexus.kernel.scope;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.console.scope.api.WorkspaceScopeDirectory;
import com.innospots.nexus.kernel.workspace.operator.WorkspaceOperator;

/**
 * Kernel-backed workspace scope directory.
 */
@RequiredArgsConstructor
public class KernelWorkspaceScopeDirectory implements WorkspaceScopeDirectory {

    private final WorkspaceOperator workspaceOperator;

    @Override
    public Optional<WorkspaceSnapshot> findWorkspace(String tenantId, String workspaceId) {
        return workspaceOperator.findSnapshot(tenantId, workspaceId);
    }
}

package com.innospots.nexus.console.scope.api;

import java.util.Optional;

import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;

/**
 * Loads workspace snapshots for session binding.
 */
public interface WorkspaceScopeDirectory {

    /**
     * Finds a workspace snapshot scoped to the given tenant.
     *
     * @param tenantId    owning tenant identifier
     * @param workspaceId workspace identifier
     * @return workspace snapshot when found
     */
    Optional<WorkspaceSnapshot> findWorkspace(String tenantId, String workspaceId);
}

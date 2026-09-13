package com.innospots.nexus.console.scope.api;

import java.util.Optional;

import com.innospots.nexus.base.domain.project.ProjectSnapshot;

/**
 * Loads project snapshots for session binding.
 */
public interface ProjectScopeDirectory {

    /**
     * Finds a project snapshot scoped to the given tenant and workspace.
     *
     * @param tenantId    owning tenant identifier
     * @param workspaceId owning workspace identifier
     * @param projectId   project identifier
     * @return project snapshot when found
     */
    Optional<ProjectSnapshot> findProject(String tenantId, String workspaceId, String projectId);
}

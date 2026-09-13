package com.innospots.nexus.kernel.scope;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.console.scope.api.ProjectScopeDirectory;
import com.innospots.nexus.kernel.project.operator.ProjectOperator;

/**
 * Kernel-backed project scope directory.
 */
@RequiredArgsConstructor
public class KernelProjectScopeDirectory implements ProjectScopeDirectory {

    private final ProjectOperator projectOperator;

    @Override
    public Optional<ProjectSnapshot> findProject(String tenantId, String workspaceId, String projectId) {
        return projectOperator.findSnapshot(tenantId, workspaceId, projectId);
    }
}

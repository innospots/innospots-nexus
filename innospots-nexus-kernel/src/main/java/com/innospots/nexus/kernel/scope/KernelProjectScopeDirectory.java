package com.innospots.nexus.kernel.scope;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.kernel.project.operator.ProjectOperator;

/**
 * Kernel 支持的项目作用域目录。
 *
 * @author Smars
 * @date 2026/09/13
 */
@RequiredArgsConstructor
public class KernelProjectScopeDirectory {

    private final ProjectOperator projectOperator;

    public Optional<ProjectSnapshot> findProject(String tenantId, String workspaceId, String projectId) {
        return projectOperator.findSnapshot(tenantId, workspaceId, projectId);
    }
}

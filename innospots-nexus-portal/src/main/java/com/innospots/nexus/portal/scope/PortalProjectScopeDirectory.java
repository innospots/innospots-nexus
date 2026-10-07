package com.innospots.nexus.portal.scope;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.portal.project.operator.ProjectOperator;

/**
 * Portal 支持的项目作用域目录。
 *
 * @author Smars
 * @date 2026/09/13
 */
@RequiredArgsConstructor
public class PortalProjectScopeDirectory {

    private final ProjectOperator projectOperator;

    public Optional<ProjectSnapshot> findProject(String tenantId, String workspaceId, String projectId) {
        return projectOperator.findSnapshot(tenantId, workspaceId, projectId);
    }
}

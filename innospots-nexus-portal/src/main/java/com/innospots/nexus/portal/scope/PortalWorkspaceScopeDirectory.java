package com.innospots.nexus.portal.scope;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.portal.workspace.operator.WorkspaceOperator;

/**
 * Portal 支持的工作区作用域目录。
 *
 * @author Smars
 * @date 2026/09/13
 */
@RequiredArgsConstructor
public class PortalWorkspaceScopeDirectory {

    private final WorkspaceOperator workspaceOperator;

    public Optional<WorkspaceSnapshot> findWorkspace(String tenantId, String workspaceId) {
        return workspaceOperator.findSnapshot(tenantId, workspaceId);
    }
}

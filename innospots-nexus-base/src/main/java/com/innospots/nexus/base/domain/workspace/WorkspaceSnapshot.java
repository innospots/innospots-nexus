package com.innospots.nexus.base.domain.workspace;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Session/transport snapshot of a tenant workspace ({@code nx_workspace}).
 * Workspaces are the resource-sharing boundary under a tenant.
 */
public record WorkspaceSnapshot(
        String tenantId,
        String workspaceId,
        String workspaceCode,
        String workspaceName,
        BasicStatus status
) {
}

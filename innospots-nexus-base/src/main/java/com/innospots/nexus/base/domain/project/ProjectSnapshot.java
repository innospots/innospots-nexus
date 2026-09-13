package com.innospots.nexus.base.domain.project;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Session/transport snapshot of a project within a workspace.
 * Projects provide business isolation under a shared workspace.
 */
public record ProjectSnapshot(
        String tenantId,
        String workspaceId,
        String projectId,
        String projectCode,
        String projectName,
        String description,
        BasicStatus status
) {
}

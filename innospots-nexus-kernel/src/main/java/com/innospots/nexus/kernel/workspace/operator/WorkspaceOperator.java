package com.innospots.nexus.kernel.workspace.operator;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.kernel.workspace.dao.WorkspaceDao;
import com.innospots.nexus.kernel.workspace.domain.entity.WorkspaceEntity;

/**
 * Tenant workspace data operator backed by MyBatis-Plus DAO objects.
 */
@RequiredArgsConstructor
public class WorkspaceOperator {

    private final WorkspaceDao workspaceDao;

    /**
     * Finds a workspace snapshot scoped to the given tenant.
     *
     * @param tenantId    owning tenant identifier
     * @param workspaceId workspace identifier
     * @return workspace snapshot when found and tenant matches
     */
    public Optional<WorkspaceSnapshot> findSnapshot(String tenantId, String workspaceId) {
        if (tenantId == null || workspaceId == null) {
            return Optional.empty();
        }
        WorkspaceEntity entity = workspaceDao.selectById(workspaceId);
        if (entity == null || !tenantId.equals(entity.getTenantId())) {
            return Optional.empty();
        }
        return Optional.of(toSnapshot(entity));
    }

    private static WorkspaceSnapshot toSnapshot(WorkspaceEntity entity) {
        return new WorkspaceSnapshot(
                entity.getTenantId(),
                entity.getWorkspaceId(),
                entity.getWorkspaceCode(),
                entity.getWorkspaceName(),
                toBasicStatus(entity.getStatus()));
    }

    private static BasicStatus toBasicStatus(String status) {
        if (status == null || status.isBlank()) {
            return BasicStatus.ENABLED;
        }
        if ("ACTIVE".equalsIgnoreCase(status) || "ENABLED".equalsIgnoreCase(status)) {
            return BasicStatus.ENABLED;
        }
        return BasicStatus.DISABLED;
    }
}

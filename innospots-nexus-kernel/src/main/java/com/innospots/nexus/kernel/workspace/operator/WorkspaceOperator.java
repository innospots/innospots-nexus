package com.innospots.nexus.kernel.workspace.operator;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.kernel.workspace.dao.WorkspaceDao;
import com.innospots.nexus.kernel.workspace.domain.entity.WorkspaceEntity;

/**
 * 基于 MyBatis-Plus DAO 的租户工作区数据操作器。
 *
 * @author Smars
 * @date 2026/09/13
 */
@RequiredArgsConstructor
public class WorkspaceOperator {

    private final WorkspaceDao workspaceDao;

    /**
     * 查找给定租户作用域内的工作区快照。
     *
     * @param tenantId    owning tenant 标识符
     * @param workspaceId workspace 标识符
     * @return workspace 找到且租户匹配时返回的快照
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

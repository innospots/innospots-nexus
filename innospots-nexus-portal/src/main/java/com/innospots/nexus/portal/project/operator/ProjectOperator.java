package com.innospots.nexus.portal.project.operator;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.portal.project.dao.ProjectDao;
import com.innospots.nexus.portal.project.domain.entity.ProjectEntity;

/**
 * 基于 MyBatis-Plus DAO 的工作区项目数据操作器。
 *
 * @author Smars
 * @date 2026/09/13
 */
@RequiredArgsConstructor
public class ProjectOperator {

    private final ProjectDao projectDao;

    /**
     * 查找给定租户与工作区作用域内的项目快照。
     *
     * @param tenantId    owning tenant 标识符
     * @param workspaceId owning workspace 标识符
     * @param projectId   project 标识符
     * @return project 找到且作用域匹配时返回的快照
     */
    public Optional<ProjectSnapshot> findSnapshot(String tenantId, String workspaceId, String projectId) {
        if (tenantId == null || workspaceId == null || projectId == null) {
            return Optional.empty();
        }
        ProjectEntity entity = projectDao.selectById(projectId);
        if (entity == null
                || !tenantId.equals(entity.getTenantId())
                || !workspaceId.equals(entity.getWorkspaceId())) {
            return Optional.empty();
        }
        return Optional.of(toSnapshot(entity));
    }

    private static ProjectSnapshot toSnapshot(ProjectEntity entity) {
        return new ProjectSnapshot(
                entity.getTenantId(),
                entity.getWorkspaceId(),
                entity.getProjectId(),
                entity.getProjectCode(),
                entity.getProjectName(),
                entity.getDescription(),
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

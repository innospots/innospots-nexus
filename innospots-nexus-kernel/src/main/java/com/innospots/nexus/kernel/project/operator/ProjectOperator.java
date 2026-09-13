package com.innospots.nexus.kernel.project.operator;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.kernel.project.dao.ProjectDao;
import com.innospots.nexus.kernel.project.domain.entity.ProjectEntity;

/**
 * Workspace project data operator backed by MyBatis-Plus DAO objects.
 */
@RequiredArgsConstructor
public class ProjectOperator {

    private final ProjectDao projectDao;

    /**
     * Finds a project snapshot scoped to the given tenant and workspace.
     *
     * @param tenantId    owning tenant identifier
     * @param workspaceId owning workspace identifier
     * @param projectId   project identifier
     * @return project snapshot when found and scope matches
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

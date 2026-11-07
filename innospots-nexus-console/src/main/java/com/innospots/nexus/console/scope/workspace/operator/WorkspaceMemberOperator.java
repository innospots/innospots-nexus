package com.innospots.nexus.console.scope.workspace.operator;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.console.scope.tenant.domain.enums.TenantMemberStatus;
import com.innospots.nexus.console.scope.workspace.dao.WorkspaceMemberDao;
import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceMemberEntity;

/**
 * 工作区成员关系的持久化操作。
 *
 * <p>成员关系把租户成员绑定到工作区；调用方负责保证
 * {@code tenantId} / {@code workspaceId} 与会话作用域一致。</p>
 *
 * @author Smars
 * @date 2026/11/07
 */
@RequiredArgsConstructor
public class WorkspaceMemberOperator {

    private final WorkspaceMemberDao workspaceMemberDao;

    /**
     * 把租户成员加入工作区。
     *
     * @param tenantId       租户标识
     * @param workspaceId    工作区标识
     * @param tenantMemberId 租户成员标识
     * @return 新建成员关系
     */
    public WorkspaceMemberEntity add(
            String tenantId, String workspaceId, String tenantMemberId) {
        WorkspaceMemberEntity relation = new WorkspaceMemberEntity();
        relation.setTenantId(tenantId);
        relation.setWorkspaceId(workspaceId);
        relation.setTenantMemberId(tenantMemberId);
        relation.setStatus(TenantMemberStatus.ACTIVE.name());
        workspaceMemberDao.insert(relation);
        return relation;
    }

    /**
     * 解析指定工作区内的成员关系。
     *
     * @param workspaceId    工作区标识
     * @param tenantMemberId 租户成员标识
     * @return 成员关系；不存在时为空
     */
    public Optional<WorkspaceMemberEntity> find(String workspaceId, String tenantMemberId) {
        if (workspaceId == null || workspaceId.isBlank()
                || tenantMemberId == null || tenantMemberId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(workspaceMemberDao.selectOne(
                Wrappers.<WorkspaceMemberEntity>lambdaQuery()
                        .eq(WorkspaceMemberEntity::getWorkspaceId, workspaceId.trim())
                        .eq(WorkspaceMemberEntity::getTenantMemberId, tenantMemberId.trim())
                        .last("LIMIT 1")));
    }

    /**
     * 判断租户成员是否属于指定工作区。
     *
     * @param workspaceId    工作区标识
     * @param tenantMemberId 租户成员标识
     * @return 是否为工作区成员
     */
    public boolean isMember(String workspaceId, String tenantMemberId) {
        return find(workspaceId, tenantMemberId).isPresent();
    }

    /**
     * 列出工作区的全部成员关系。
     *
     * @param workspaceId 工作区标识
     * @return 成员关系列表
     */
    public List<WorkspaceMemberEntity> listByWorkspace(String workspaceId) {
        if (workspaceId == null || workspaceId.isBlank()) {
            return List.of();
        }
        return workspaceMemberDao.selectList(Wrappers.<WorkspaceMemberEntity>lambdaQuery()
                .eq(WorkspaceMemberEntity::getWorkspaceId, workspaceId.trim())
                .orderByAsc(WorkspaceMemberEntity::getCreatedAt));
    }

    /**
     * 列出指定租户成员加入的全部工作区标识。
     *
     * @param tenantId       租户标识
     * @param tenantMemberId 租户成员标识
     * @return 工作区标识列表
     */
    public List<String> listWorkspaceIds(String tenantId, String tenantMemberId) {
        if (tenantId == null || tenantId.isBlank()
                || tenantMemberId == null || tenantMemberId.isBlank()) {
            return List.of();
        }
        return workspaceMemberDao.selectList(Wrappers.<WorkspaceMemberEntity>lambdaQuery()
                        .eq(WorkspaceMemberEntity::getTenantId, tenantId.trim())
                        .eq(WorkspaceMemberEntity::getTenantMemberId, tenantMemberId.trim())
                        .eq(WorkspaceMemberEntity::getStatus, TenantMemberStatus.ACTIVE.name()))
                .stream()
                .map(WorkspaceMemberEntity::getWorkspaceId)
                .toList();
    }

    /**
     * 统计工作区成员数量。
     *
     * @param workspaceId 工作区标识
     * @return 成员数量
     */
    public long countByWorkspace(String workspaceId) {
        if (workspaceId == null || workspaceId.isBlank()) {
            return 0L;
        }
        return workspaceMemberDao.selectCount(Wrappers.<WorkspaceMemberEntity>lambdaQuery()
                .eq(WorkspaceMemberEntity::getWorkspaceId, workspaceId.trim()));
    }

    /**
     * 把成员移出工作区。
     *
     * @param workspaceId    工作区标识
     * @param tenantMemberId 租户成员标识
     * @return 是否删除成功
     */
    public boolean remove(String workspaceId, String tenantMemberId) {
        if (workspaceId == null || workspaceId.isBlank()
                || tenantMemberId == null || tenantMemberId.isBlank()) {
            return false;
        }
        return workspaceMemberDao.delete(Wrappers.<WorkspaceMemberEntity>lambdaQuery()
                .eq(WorkspaceMemberEntity::getWorkspaceId, workspaceId.trim())
                .eq(WorkspaceMemberEntity::getTenantMemberId, tenantMemberId.trim())) > 0;
    }
}

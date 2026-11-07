package com.innospots.nexus.console.scope.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.scope.domain.model.WorkspaceContext;
import com.innospots.nexus.console.scope.workspace.dao.WorkspaceMemberDao;
import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceEntity;
import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceMemberEntity;
import com.innospots.nexus.console.scope.workspace.operator.WorkspaceOperator;

/**
 * 工作区上下文解析。
 *
 * <p>把「工作区 → 当前用户的工作区成员关系 → 该成员在工作区内的角色编码」一次查全。</p>
 *
 * <p>角色编码取自 {@code nx_role_binding}（主体为该用户）与
 * {@code nx_role}（归属层级为 WORKSPACE 且 ownerId 为该工作区）。</p>
 *
 * @author Smars
 * @date 2026/11/07
 */
@RequiredArgsConstructor
public class ConsoleWorkspaceContextService {

    private final WorkspaceOperator workspaceOperator;
    private final WorkspaceMemberDao workspaceMemberDao;
    private final RoleBindingDao roleBindingDao;
    private final RoleDao roleDao;

    /**
     * 解析用户在指定工作区下的上下文。
     *
     * <p>任一条不满足都返回空：工作区不存在、不属于该租户、用户不是该工作区成员。</p>
     *
     * @param userId      用户标识（租户用户主键）
     * @param tenantId    租户标识；非空时校验工作区归属
     * @param workspaceId 工作区标识
     * @return 工作区上下文；不满足条件时为空
     */
    public Optional<WorkspaceContext> resolve(String userId, String tenantId, String workspaceId) {
        if (isBlank(userId) || isBlank(workspaceId)) {
            return Optional.empty();
        }
        WorkspaceEntity workspace = workspaceOperator.findById(workspaceId.trim());
        if (workspace == null) {
            return Optional.empty();
        }
        if (!isBlank(tenantId) && !tenantId.trim().equals(workspace.getTenantId())) {
            return Optional.empty();
        }
        WorkspaceMemberEntity member = findMember(userId.trim(), workspace.getWorkspaceId());
        if (member == null) {
            return Optional.empty();
        }
        return Optional.of(new WorkspaceContext(
                workspace, member, workspaceRoleCodes(userId.trim(), workspace.getWorkspaceId())));
    }

    /**
     * 查询用户在指定工作区内的角色编码。
     *
     * @param userId      用户标识
     * @param workspaceId 工作区标识
     * @return 角色编码列表
     */
    public List<String> workspaceRoleCodes(String userId, String workspaceId) {
        if (isBlank(userId) || isBlank(workspaceId)) {
            return List.of();
        }
        List<String> roleIds = roleBindingDao.selectList(
                        Wrappers.<RoleBindingEntity>lambdaQuery()
                                .eq(RoleBindingEntity::getSubjectType,
                                        RoleBindingSubjectType.USER.name())
                                .eq(RoleBindingEntity::getSubjectId, userId)
                                .eq(RoleBindingEntity::getOwnerType, RoleOwnerType.WORKSPACE.name())
                                .eq(RoleBindingEntity::getOwnerId, workspaceId))
                .stream()
                .map(RoleBindingEntity::getRoleId)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        Set<String> codes = new LinkedHashSet<>();
        for (RoleEntity role : roleDao.selectBatchIds(roleIds)) {
            if (role.getRoleCode() != null && !role.getRoleCode().isBlank()) {
                codes.add(role.getRoleCode());
            }
        }
        return List.copyOf(codes);
    }

    /**
     * 按用户标识查询工作区成员关系。
     */
    private WorkspaceMemberEntity findMember(String userId, String workspaceId) {
        return workspaceMemberDao.selectOne(Wrappers.<WorkspaceMemberEntity>lambdaQuery()
                .eq(WorkspaceMemberEntity::getWorkspaceId, workspaceId)
                .eq(WorkspaceMemberEntity::getPortalUserId, userId)
                .last("LIMIT 1"));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

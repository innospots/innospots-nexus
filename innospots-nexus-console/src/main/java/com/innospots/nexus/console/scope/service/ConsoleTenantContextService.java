package com.innospots.nexus.console.scope.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
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
import com.innospots.nexus.console.scope.domain.model.TenantContext;
import com.innospots.nexus.console.scope.tenant.dao.TenantDao;
import com.innospots.nexus.console.scope.tenant.dao.TenantMemberDao;
import com.innospots.nexus.console.scope.tenant.domain.entity.TenantEntity;
import com.innospots.nexus.console.scope.tenant.domain.entity.TenantMemberEntity;

/**
 * 租户上下文解析。
 *
 * <p>把「租户 → 当前用户的租户成员关系 → 该成员在租户内的角色编码」一次查全，
 * 调用方只需关心「这个用户能不能进这个租户」。</p>
 *
 * <p>角色编码取自 {@code nx_role_binding}（主体为该用户）与
 * {@code nx_role}（归属层级为 TENANT 且 ownerId 为该租户）。</p>
 *
 * @author Smars
 * @date 2026/11/07
 */
@RequiredArgsConstructor
public class ConsoleTenantContextService {

    private final TenantDao tenantDao;
    private final TenantMemberDao tenantMemberDao;
    private final RoleBindingDao roleBindingDao;
    private final RoleDao roleDao;

    /**
     * 解析用户在指定租户下的上下文。
     *
     * <p>任一条不满足都返回空：租户不存在、用户在该租户下没有有效成员关系。</p>
     *
     * @param userId   用户标识（租户用户主键）
     * @param tenantId 租户标识
     * @return 租户上下文；不满足条件时为空
     */
    public Optional<TenantContext> resolve(String userId, String tenantId) {
        if (isBlank(userId) || isBlank(tenantId)) {
            return Optional.empty();
        }
        TenantEntity tenant = tenantDao.selectById(tenantId.trim());
        if (tenant == null) {
            return Optional.empty();
        }
        TenantMemberEntity member = tenantMemberDao.findActiveMembership(userId.trim(), tenant.getTenantId());
        if (member == null) {
            return Optional.empty();
        }
        return Optional.of(new TenantContext(
                tenant, member, tenantRoleCodes(userId.trim(), tenant.getTenantId())));
    }

    /**
     * 查询用户在指定租户内的角色编码。
     *
     * @param userId   用户标识
     * @param tenantId 租户标识
     * @return 角色编码列表
     */
    public List<String> tenantRoleCodes(String userId, String tenantId) {
        if (isBlank(userId) || isBlank(tenantId)) {
            return List.of();
        }
        List<String> roleIds = roleBindingDao.selectList(
                        Wrappers.<RoleBindingEntity>lambdaQuery()
                                .eq(RoleBindingEntity::getSubjectType,
                                        RoleBindingSubjectType.USER.name())
                                .eq(RoleBindingEntity::getSubjectId, userId)
                                .eq(RoleBindingEntity::getOwnerType, RoleOwnerType.TENANT.name())
                                .eq(RoleBindingEntity::getOwnerId, tenantId))
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

    private static boolean isBlank(String value) {
        return Objects.isNull(value) || value.isBlank();
    }
}

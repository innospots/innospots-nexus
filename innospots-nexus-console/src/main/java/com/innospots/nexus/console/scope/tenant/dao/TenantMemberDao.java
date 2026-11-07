package com.innospots.nexus.console.scope.tenant.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.console.scope.tenant.domain.entity.TenantMemberEntity;
import com.innospots.nexus.console.scope.tenant.domain.enums.TenantMemberStatus;

/**
 * 租户成员关系记录的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface TenantMemberDao extends BaseMapper<TenantMemberEntity> {

    /**
     * 列出用户当前有效的成员关系。
     *
     * @param portalUserId 租户用户标识
     * @return 状态为 ACTIVE 的成员关系；无输入时返回空列表
     */
    default List<TenantMemberEntity> listActiveMemberships(String portalUserId) {
        if (portalUserId == null || portalUserId.isBlank()) {
            return List.of();
        }
        return selectList(Wrappers.<TenantMemberEntity>lambdaQuery()
                .eq(TenantMemberEntity::getPortalUserId, portalUserId.trim())
                .eq(TenantMemberEntity::getStatus, TenantMemberStatus.ACTIVE.name()));
    }

    /**
     * 查询用户在指定租户中的有效成员关系。
     *
     * @param portalUserId 租户用户标识
     * @param tenantId     租户标识
     * @return 成员关系；不存在或非 ACTIVE 时为空
     */
    default TenantMemberEntity findActiveMembership(String portalUserId, String tenantId) {
        if (portalUserId == null || portalUserId.isBlank()
                || tenantId == null || tenantId.isBlank()) {
            return null;
        }
        return selectOne(Wrappers.<TenantMemberEntity>lambdaQuery()
                .eq(TenantMemberEntity::getPortalUserId, portalUserId.trim())
                .eq(TenantMemberEntity::getTenantId, tenantId.trim())
                .eq(TenantMemberEntity::getStatus, TenantMemberStatus.ACTIVE.name())
                .last("LIMIT 1"));
    }

    /**
     * 查询用户在指定租户中的成员关系标识。
     *
     * @param portalUserId 租户用户标识
     * @param tenantId     租户标识
     * @return 成员关系标识；不存在时为空
     */
    default String findTenantMemberId(String portalUserId, String tenantId) {
        TenantMemberEntity member = findActiveMembership(portalUserId, tenantId);
        return member == null ? null : member.getTenantMemberId();
    }
}

package com.innospots.nexus.portal.auth.adapter;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.portal.auth.api.MembershipDirectory;
import com.innospots.nexus.portal.auth.domain.model.TenantMembership;
import com.innospots.nexus.portal.member.dao.TenantMemberDao;
import com.innospots.nexus.portal.member.domain.entity.TenantMemberEntity;
import com.innospots.nexus.portal.member.domain.enums.TenantMemberStatus;

/**
 * 租户成员关系 {@link MembershipDirectory} 实现。
 */
@RequiredArgsConstructor
public class PortalMembershipDirectory implements MembershipDirectory {

    private final TenantMemberDao tenantMemberDao;

    @Override
    public List<TenantMembership> listActiveMemberships(String tenantUserId) {
        if (tenantUserId == null || tenantUserId.isBlank()) {
            return List.of();
        }
        return tenantMemberDao.selectList(new LambdaQueryWrapper<TenantMemberEntity>()
                        .eq(TenantMemberEntity::getTenantUserId, tenantUserId)
                        .eq(TenantMemberEntity::getStatus, TenantMemberStatus.ACTIVE.name()))
                .stream()
                .map(entity -> new TenantMembership(entity.getTenantId(), entity.getTenantMemberId()))
                .toList();
    }
}

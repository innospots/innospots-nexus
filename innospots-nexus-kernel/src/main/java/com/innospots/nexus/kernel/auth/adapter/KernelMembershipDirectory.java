package com.innospots.nexus.kernel.auth.adapter;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.auth.api.MembershipDirectory;
import com.innospots.nexus.console.auth.domain.model.TenantMembership;
import com.innospots.nexus.kernel.member.dao.TenantMemberDao;
import com.innospots.nexus.kernel.member.domain.entity.TenantMemberEntity;
import com.innospots.nexus.kernel.member.domain.enums.TenantMemberStatus;

/**
 * 租户成员关系 {@link MembershipDirectory} 实现。
 */
@RequiredArgsConstructor
public class KernelMembershipDirectory implements MembershipDirectory {

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

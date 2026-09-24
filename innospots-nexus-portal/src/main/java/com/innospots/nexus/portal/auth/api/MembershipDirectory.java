package com.innospots.nexus.portal.auth.api;

import java.util.List;

import com.innospots.nexus.portal.auth.domain.model.TenantMembership;

/**
 * 租户域身份认证后用于查询租户成员关系。
 */
public interface MembershipDirectory {

    /**
     * 列出租户域用户的活跃成员关系。
     *
     * @param tenantUserId 租户域用户 ID
     * @return active 成员关系列表，永不为 null
     */
    List<TenantMembership> listActiveMemberships(String tenantUserId);

    /**
     * 列出租户用户拥有 ACTIVE 成员关系的租户 ID。
     *
     * @param tenantUserId 租户域用户 ID
     * @return active tenant 标识符，永不为 null
     */
    default List<String> listActiveTenantIds(String tenantUserId) {
        return listActiveMemberships(tenantUserId).stream()
                .map(TenantMembership::tenantId)
                .toList();
    }
}

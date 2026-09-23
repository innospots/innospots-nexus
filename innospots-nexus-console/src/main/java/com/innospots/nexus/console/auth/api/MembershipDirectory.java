package com.innospots.nexus.console.auth.api;

import java.util.List;

import com.innospots.nexus.console.auth.domain.model.TenantMembership;

/**
 * 租户域身份认证后用于查询租户成员关系。
 *
 * @author Smars
 * @date 2026/09/13
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
     * @return active tenant 标识符s, never null
     */
    default List<String> listActiveTenantIds(String tenantUserId) {
        return listActiveMemberships(tenantUserId).stream()
                .map(TenantMembership::tenantId)
                .toList();
    }
}

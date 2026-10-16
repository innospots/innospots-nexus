package com.innospots.nexus.console.tenant.api;

import java.util.Optional;

/**
 * 按邮箱解析租户域用户（{@code nx_tenant_user}）。由 portal 装配实现；platform 登记租户管理员时可选注入。
 *
 * @author Smars
 * @date 2026/10/16
 */
public interface TenantUserResolver {

    /**
     * 按邮箱查找仍有效的租户用户。
     *
     * @param email 规范化后的邮箱
     * @return 用户主键与展示字段
     */
    Optional<ResolvedTenantUser> findByEmail(String email);

    /**
     * 租户用户解析结果。
     *
     * @param tenantUserId 租户用户主键
     * @param email        邮箱
     * @param displayName  展示名
     */
    record ResolvedTenantUser(String tenantUserId, String email, String displayName) {
    }
}

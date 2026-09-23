package com.innospots.nexus.base.domain.tenant;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 平台租户（{@code nx_tenant}）的会话/传输快照。
 * 与租户企业档案（{@link com.innospots.nexus.base.domain.organization.OrganizationSnapshot}）一一对应。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantId   租户 ID
 * @param tenantCode 租户编码
 * @param tenantName 租户名称
 * @param status     状态
 * @see com.innospots.nexus.base.domain.organization.OrganizationSnapshot
 */
public record TenantSnapshot(
        String tenantId,
        String tenantCode,
        String tenantName,
        BasicStatus status
) {
}

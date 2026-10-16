package com.innospots.nexus.platform.organization.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 更新租户可编辑字段的 REST 请求体（不含状态、组织形态与套餐）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.organization.endpoint.TenantEndpoint#updateTenant
 * @param tenantName 显示名称
 */
@Schema(name = "TenantUpdateRequest", description = "更新租户资料")
public record TenantUpdateRequest(
        @Schema(description = "租户显示名称")
        String tenantName
) {
}

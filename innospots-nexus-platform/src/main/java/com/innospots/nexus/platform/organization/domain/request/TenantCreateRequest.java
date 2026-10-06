package com.innospots.nexus.platform.organization.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 开通平台租户（不含企业法定档案）的 REST 请求体。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.organization.endpoint.TenantEndpoint#createTenant
 * @param tenantName        显示名称
 * @param tenantCode        唯一租户编码
 * @param tenantType        组织形态（{@link com.innospots.nexus.platform.organization.domain.enums.TenantType}）
 * @param planCode          可选 plan reference
 * @param ownerTenantUserId 可选 initial tenant-user owner
 */
@Schema(name = "TenantCreateRequest", description = "开通平台租户（不含企业档案）")
public record TenantCreateRequest(
        @Schema(description = "租户显示名称", required = true, examples = {"示例租户"})
        String tenantName,
        @Schema(description = "唯一租户编码", required = true, examples = {"demo-tenant"})
        String tenantCode,
        @Schema(description = "组织形态（ENTERPRISE、TEAM、INDIVIDUAL）", required = true, examples = {"TEAM"})
        String tenantType,
        @Schema(description = "套餐/计划编码")
        String planCode,
        @Schema(description = "初始租户用户 owner ID")
        String ownerTenantUserId
) {
}

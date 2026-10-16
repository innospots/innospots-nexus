package com.innospots.nexus.platform.organization.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 开通平台租户（仅身份字段；企业档案、套餐与 owner 不在此接口维护）。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.organization.endpoint.TenantEndpoint#createTenant
 * @param tenantName 显示名称
 * @param tenantCode 唯一租户编码（字母、数字、下划线）
 * @param tenantType 组织形态（{@link com.innospots.nexus.platform.organization.domain.enums.TenantType}）
 */
@Schema(name = "TenantCreateRequest", description = "开通平台租户（仅租户名称、类型与唯一编码）")
public record TenantCreateRequest(
        @Schema(description = "租户显示名称", required = true, examples = {"示例租户"})
        String tenantName,
        @Schema(description = "唯一租户编码（字母、数字、下划线）", required = true, examples = {"acme_corp"})
        String tenantCode,
        @Schema(description = "组织形态（ENTERPRISE、TEAM、INDIVIDUAL）", required = true, examples = {"TEAM"})
        String tenantType
) {
}

package com.innospots.nexus.platform.tenant.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 平台 API 返回的租户与企业概要。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantId           tenant 标识符
 * @param tenantName         显示名称
 * @param tenantCode         唯一租户编码
 * @param status             生命周期状态
 * @param planCode           可选 plan reference
 * @param ownerTenantUserId  可选 initial tenant-user owner
 * @param enterpriseId       enterprise profile 标识符
 * @param legalName          企业法定名称
 */
@Schema(name = "TenantVo", description = "租户与企业概要")
public record TenantVo(
        @Schema(description = "租户 ID", required = true)
        String tenantId,
        @Schema(description = "显示名称", required = true)
        String tenantName,
        @Schema(description = "唯一租户编码", required = true)
        String tenantCode,
        @Schema(description = "生命周期状态", examples = {"ACTIVE"})
        String status,
        @Schema(description = "套餐/计划编码")
        String planCode,
        @Schema(description = "租户用户 owner ID")
        String ownerTenantUserId,
        @Schema(description = "企业档案 ID")
        String enterpriseId,
        @Schema(description = "企业法定名称")
        String legalName
) {
}

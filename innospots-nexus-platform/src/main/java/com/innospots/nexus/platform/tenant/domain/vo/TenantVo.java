package com.innospots.nexus.platform.tenant.domain.vo;

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
public record TenantVo(
        String tenantId,
        String tenantName,
        String tenantCode,
        String status,
        String planCode,
        String ownerTenantUserId,
        String enterpriseId,
        String legalName
) {
}

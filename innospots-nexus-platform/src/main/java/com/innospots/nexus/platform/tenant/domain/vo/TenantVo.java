package com.innospots.nexus.platform.tenant.domain.vo;

/**
 * Tenant plus enterprise summary returned by platform APIs.
 *
 * @param tenantId           tenant identifier
 * @param tenantName         display name
 * @param tenantCode         unique tenant code
 * @param status             lifecycle status
 * @param planCode           optional plan reference
 * @param ownerTenantUserId  optional initial tenant-user owner
 * @param enterpriseId       enterprise profile identifier
 * @param legalName          enterprise legal name
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

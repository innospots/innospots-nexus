package com.innospots.nexus.base.domain.tenant;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Session/transport snapshot of a platform tenant ({@code nx_tenant}).
 * Pairs 1:1 with the tenant business profile ({@link com.innospots.nexus.base.domain.organization.OrganizationSnapshot}).
 */
public record TenantSnapshot(
        String tenantId,
        String tenantCode,
        String tenantName,
        BasicStatus status
) {
}

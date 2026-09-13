package com.innospots.nexus.console.scope.domain.model;

import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;

/**
 * Tenant identity paired with its business organization profile.
 *
 * @param tenant       platform tenant snapshot
 * @param organization tenant business-facing profile
 */
public record TenantScope(TenantSnapshot tenant, OrganizationSnapshot organization) {
}

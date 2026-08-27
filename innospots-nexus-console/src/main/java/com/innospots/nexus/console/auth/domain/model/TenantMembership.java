package com.innospots.nexus.console.auth.domain.model;

/**
 * Active tenant membership used after tenant-realm identity authentication.
 *
 * @param tenantId       tenant identifier
 * @param tenantMemberId tenant member identifier
 */
public record TenantMembership(String tenantId, String tenantMemberId) {
}

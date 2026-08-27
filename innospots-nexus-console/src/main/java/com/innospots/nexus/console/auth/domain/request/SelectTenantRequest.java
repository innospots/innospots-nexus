package com.innospots.nexus.console.auth.domain.request;

/**
 * Selects the current tenant after identity authentication.
 *
 * @param tenantId tenant to activate
 */
public record SelectTenantRequest(String tenantId) {
}

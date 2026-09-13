package com.innospots.nexus.console.auth.domain.request;

/**
 * Activates a workspace within the current tenant business scope.
 *
 * @param tenantId    tenant to validate membership against
 * @param workspaceId workspace to activate
 */
public record SelectWorkspaceRequest(String tenantId, String workspaceId) {
}

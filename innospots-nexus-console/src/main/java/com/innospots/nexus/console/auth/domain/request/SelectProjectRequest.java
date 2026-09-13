package com.innospots.nexus.console.auth.domain.request;

/**
 * Activates a project within the current workspace scope.
 *
 * @param tenantId    owning tenant identifier
 * @param workspaceId owning workspace identifier
 * @param projectId   project to activate
 */
public record SelectProjectRequest(String tenantId, String workspaceId, String projectId) {
}

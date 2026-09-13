package com.innospots.nexus.console.auth.service;

/**
 * Scope carried by an issued auth token pair.
 *
 * @param tokenType       IDENTITY or BUSINESS
 * @param tenantId        tenant on BUSINESS tokens
 * @param tenantMemberId  tenant member on BUSINESS tokens
 * @param workspaceId     active workspace on scoped BUSINESS tokens
 * @param projectId       active project on scoped BUSINESS tokens
 */
public record AuthSessionScope(
        String tokenType,
        String tenantId,
        String tenantMemberId,
        String workspaceId,
        String projectId
) {
}

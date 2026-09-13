package com.innospots.nexus.console.auth.domain.model;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * Compact claims encrypted into an access or refresh token.
 *
 * @param realm           PLATFORM or TENANT
 * @param purpose         ACCESS or REFRESH
 * @param tokenType       IDENTITY or BUSINESS
 * @param userId          platform or tenant user identifier
 * @param tenantId        tenant on BUSINESS tokens
 * @param tenantMemberId  tenant member on BUSINESS tokens
 * @param workspaceId     active workspace on scoped BUSINESS tokens
 * @param projectId       active project on scoped BUSINESS tokens
 * @param expiresAt       epoch-second expiry
 */
public record TokenClaims(
        SecurityRealm realm,
        String purpose,
        String tokenType,
        String userId,
        String tenantId,
        String tenantMemberId,
        String workspaceId,
        String projectId,
        long expiresAt
) {
}

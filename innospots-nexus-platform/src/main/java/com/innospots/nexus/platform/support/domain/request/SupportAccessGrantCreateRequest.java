package com.innospots.nexus.platform.support.domain.request;

import java.time.LocalDateTime;

/**
 * Request to create a pending support-access grant.
 *
 * @param tenantId       tenant to access
 * @param platformUserId platform user receiving access
 * @param reason         business reason
 * @param expireAt       absolute expiry
 */
public record SupportAccessGrantCreateRequest(
        String tenantId,
        String platformUserId,
        String reason,
        LocalDateTime expireAt
) {
}

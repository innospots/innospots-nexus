package com.innospots.nexus.platform.support.domain.vo;

import java.time.LocalDateTime;

/**
 * Support-access grant summary.
 *
 * @param grantId        grant identifier
 * @param tenantId       tenant being accessed
 * @param platformUserId platform user receiving access
 * @param reason         business reason
 * @param approvedBy     tenant-admin approver, if any
 * @param expireAt       absolute expiry
 * @param status         lifecycle status name
 */
public record SupportAccessGrantVo(
        String grantId,
        String tenantId,
        String platformUserId,
        String reason,
        String approvedBy,
        LocalDateTime expireAt,
        String status
) {
}

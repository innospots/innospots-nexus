package com.innospots.nexus.kernel.audit;

import java.time.Instant;

/**
 * Login audit entity recorded by authentication workflows.
 *
 * @param logId       log identifier
 * @param userId      user identifier
 * @param account     submitted account name
 * @param remoteIp    client IP address
 * @param result      login result
 * @param occurredAt  login attempt time
 */
public record LoginLog(
        String logId,
        String userId,
        String account,
        String remoteIp,
        LoginResult result,
        Instant occurredAt
) {
}

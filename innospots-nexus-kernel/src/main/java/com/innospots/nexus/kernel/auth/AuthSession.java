package com.innospots.nexus.kernel.auth;

import java.time.Instant;

/**
 * Authenticated principal session issued by an authentication capability.
 *
 * @param sessionId unique session identifier
 * @param userId    authenticated user identifier
 * @param grantType authentication grant type
 * @param issuedAt  issue time
 * @param expiresAt expiry time
 */
public record AuthSession(
        String sessionId,
        String userId,
        AuthGrantType grantType,
        Instant issuedAt,
        Instant expiresAt
) {
}

package com.innospots.nexus.service.contract.security;

import java.time.Instant;

import com.innospots.nexus.base.util.Checks;

/**
 * Result of authenticating a pre-auth context. {@code expiresAt} may be null for non-expiring
 * mechanisms such as host-validated mTLS.
 *
 * @param principal authenticated principal
 * @param scope     resolved scope
 * @param expiresAt optional expiry
 * @author Smars
 * @date 2026/09/13
 * @see SecurityProvider
 */
public record AuthenticationResult(ServicePrincipal principal, ServiceScope scope, Instant expiresAt) {

    public AuthenticationResult {
        Checks.notNull(principal, "principal");
        Checks.notNull(scope, "scope");
    }
}

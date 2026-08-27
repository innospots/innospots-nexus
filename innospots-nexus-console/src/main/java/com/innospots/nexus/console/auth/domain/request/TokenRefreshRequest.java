package com.innospots.nexus.console.auth.domain.request;

/**
 * Refresh-token exchange.
 *
 * @param refreshToken refresh token issued for the same realm
 */
public record TokenRefreshRequest(String refreshToken) {
}

package com.innospots.nexus.kernel.auth;

import java.util.Optional;

/**
 * Port for authentication and session validation operations.
 */
public interface AuthOperator {

    /**
     * Authenticates a user by account name and secret.
     *
     * @param account account name, email, or mobile identifier
     * @param secret  authentication secret
     * @return authenticated session when credentials are accepted
     */
    Optional<AuthSession> authenticate(String account, String secret);

    /**
     * Validates an existing session token.
     *
     * @param token session token
     * @return active session when the token is valid
     */
    Optional<AuthSession> validate(String token);

    /**
     * Revokes a session.
     *
     * @param sessionId session identifier
     * @return true when a session was revoked
     */
    boolean revoke(String sessionId);
}

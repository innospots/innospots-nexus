package com.innospots.nexus.console.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Token issuance settings for console authentication.
 */
@Getter
@Setter
public class AuthConfig {

    /**
     * Default AES secret used by tests and local development.
     */
    public static final String DEFAULT_TOKEN_SECRET = "0123456789abcdef";

    /**
     * AES secret for compact token encryption.
     */
    private String tokenSecret = DEFAULT_TOKEN_SECRET;

    /**
     * Access-token lifetime in seconds.
     */
    private long accessTokenTtlSeconds = 7200L;

    /**
     * Refresh-token lifetime in seconds.
     */
    private long refreshTokenTtlSeconds = 604800L;
}

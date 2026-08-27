package com.innospots.nexus.console.auth.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.config.AuthConfig;

/**
 * Issues and parses AES-GCM compact tokens. Console does not persist users.
 */
@RequiredArgsConstructor
public class TokenIssuer {

    public static final String PURPOSE_ACCESS = "ACCESS";
    public static final String PURPOSE_REFRESH = "REFRESH";

    private final AuthConfig authConfig;

    /**
     * Encrypts claims into a compact token string.
     *
     * @param claims token claims
     * @return AES-GCM compact token
     */
    public String issue(TokenClaims claims) {
        return CryptoUtils.encryptAesGcm(Jsons.toJson(claims), authConfig.getTokenSecret());
    }

    /**
     * Decrypts a compact token into claims.
     *
     * @param token compact token
     * @return parsed claims
     */
    public TokenClaims parse(String token) {
        if (token == null || token.isBlank()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        try {
            return Jsons.fromJson(
                    CryptoUtils.decryptAesGcm(token, authConfig.getTokenSecret()), TokenClaims.class);
        } catch (NexusException ex) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
    }

    /**
     * Returns the access-token lifetime in seconds.
     *
     * @return access TTL
     */
    public long accessTokenTtlSeconds() {
        return authConfig.getAccessTokenTtlSeconds();
    }

    /**
     * Returns the refresh-token lifetime in seconds.
     *
     * @return refresh TTL
     */
    public long refreshTokenTtlSeconds() {
        return authConfig.getRefreshTokenTtlSeconds();
    }
}

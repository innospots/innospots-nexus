package com.innospots.nexus.console.auth.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.config.AuthConfig;

/**
 * 签发并解析 AES-GCM 紧凑令牌。Console 不持久化用户。
 *
 * @author Smars
 * @date 2026/09/13
 */
@RequiredArgsConstructor
public class TokenIssuer {

    public static final String PURPOSE_ACCESS = "ACCESS";
    public static final String PURPOSE_REFRESH = "REFRESH";

    private final AuthConfig authConfig;

    /**
     * 将声明加密为紧凑令牌字符串。
     *
     * @param claims 令牌声明
     * @return AES-GCM 紧凑令牌
     */
    public String issue(TokenClaims claims) {
        return CryptoUtils.encryptAesGcm(Jsons.toJson(claims), authConfig.getTokenSecret());
    }

    /**
     * 将紧凑令牌解密为声明。
     *
     * @param token 紧凑令牌
     * @return parsed 声明
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
     * 返回访问令牌有效期（秒）。
     *
     * @return access TTL
     */
    public long accessTokenTtlSeconds() {
        return authConfig.getAccessTokenTtlSeconds();
    }

    /**
     * 返回刷新令牌有效期（秒）。
     *
     * @return refresh TTL
     */
    public long refreshTokenTtlSeconds() {
        return authConfig.getRefreshTokenTtlSeconds();
    }
}

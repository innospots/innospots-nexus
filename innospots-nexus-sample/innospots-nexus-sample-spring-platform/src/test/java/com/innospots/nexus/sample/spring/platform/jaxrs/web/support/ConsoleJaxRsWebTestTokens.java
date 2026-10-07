package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import java.time.Instant;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.TokenIssuer;

/**
 * 为过滤器测试签发访问令牌。
 */
public final class ConsoleJaxRsWebTestTokens {

    private ConsoleJaxRsWebTestTokens() {
    }

    public static String platformAccessToken(TokenIssuer tokenIssuer) {
        long now = Instant.now().getEpochSecond();
        TokenClaims claims = new TokenClaims(
                SecurityRealm.PLATFORM,
                TokenIssuer.PURPOSE_ACCESS,
                "BUSINESS",
                "1001",
                "tnt-demo",
                "mbr-demo",
                "wks-demo",
                null,
                now + 3600L);
        return tokenIssuer.issue(claims);
    }
}

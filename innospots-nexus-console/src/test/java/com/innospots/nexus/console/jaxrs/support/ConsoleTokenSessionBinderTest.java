package com.innospots.nexus.console.jaxrs.support;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.TokenIssuer;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleTokenSessionBinderTest {

    @AfterEach
    void clear() {
        ConsoleTokenSessionBinder.clear();
    }

    @Test
    void bindsUserIdForTenantRealmWithoutPlatformUserId() {
        ConsoleTokenSessionBinder.bindAccessToken(claims(SecurityRealm.TENANT, "TUS1", "tnt-a", "tmb-a"));

        assertThat(TLC.userId()).isEqualTo("TUS1");
        assertThat(TLC.securityRealm()).isEqualTo(SecurityRealm.TENANT.name());
        assertThat(TLC.tenantMemberId()).isEqualTo("tmb-a");
        assertThat(SessionContext.tenantId()).isEqualTo("tnt-a");
        // 租户域用户不得被当成平台用户。
        assertThat(TLC.platformUserId()).isNull();
    }

    @Test
    void bindsPlatformUserIdForPlatformRealm() {
        ConsoleTokenSessionBinder.bindAccessToken(claims(SecurityRealm.PLATFORM, "PUS1", null, null));

        assertThat(TLC.userId()).isEqualTo("PUS1");
        assertThat(TLC.securityRealm()).isEqualTo(SecurityRealm.PLATFORM.name());
        assertThat(TLC.platformUserId()).isEqualTo("PUS1");
        assertThat(SessionContext.tenant()).isEmpty();
    }

    private static TokenClaims claims(
            SecurityRealm realm, String userId, String tenantId, String tenantMemberId) {
        return new TokenClaims(
                realm,
                TokenIssuer.PURPOSE_ACCESS,
                "IDENTITY",
                userId,
                tenantId,
                tenantMemberId,
                Instant.now().getEpochSecond() + 3600);
    }
}

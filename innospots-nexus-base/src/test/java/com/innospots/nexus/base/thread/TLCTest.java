package com.innospots.nexus.base.thread;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TLCTest {

    @AfterEach
    void clear() {
        TLC.clear();
    }

    @Test
    void storesAndRestoresThreadLocalContextScopes() {
        TLC.put("traceId", "root");

        try (TLC.Scope ignored = TLC.scope(Map.of("traceId", "child", "tenantId", "t1"))) {
            assertThat(TLC.getString("traceId")).isEqualTo("child");
            assertThat(TLC.getString("tenantId")).isEqualTo("t1");
        }

        assertThat(TLC.getString("traceId")).isEqualTo("root");
        assertThat(TLC.get("tenantId")).isNull();
    }

    @Test
    void snapshotsAreIndependentCopies() {
        TLC.put("traceId", "a");
        Map<String, Object> snapshot = TLC.snapshot();

        TLC.put("traceId", "b");
        TLC.restore(snapshot);

        assertThat(TLC.getString("traceId")).isEqualTo("a");
    }

    @Test
    void exposesWorkspaceAndUserIdentityKeys() {
        TLC.workspaceId("wks01");
        TLC.tenantId("tnt01");
        TLC.userId(2002L);
        TLC.userName("alice");

        assertThat(TLC.workspaceId()).isEqualTo("wks01");
        assertThat(TLC.tenantId()).isEqualTo("tnt01");
        assertThat(TLC.userId()).isEqualTo(2002L);
        assertThat(TLC.userName()).isEqualTo("alice");
        assertThat(TLC.snapshot()).containsEntry(TLC.WORKSPACE_ID, "wks01")
                .containsEntry(TLC.TENANT_ID, "tnt01")
                .containsEntry(TLC.USER_ID, 2002L)
                .containsEntry(TLC.USER_NAME, "alice");
    }

    @Test
    void exposesSecurityRealmAndRealmUserKeys() {
        TLC.securityRealm("TENANT");
        TLC.tenantMemberId("tmb01");
        TLC.platformUserId("pus01");

        assertThat(TLC.securityRealm()).isEqualTo("TENANT");
        assertThat(TLC.tenantMemberId()).isEqualTo("tmb01");
        assertThat(TLC.platformUserId()).isEqualTo("pus01");
        assertThat(TLC.snapshot())
                .containsEntry(TLC.SECURITY_REALM, "TENANT")
                .containsEntry(TLC.TENANT_MEMBER_ID, "tmb01")
                .containsEntry(TLC.PLATFORM_USER_ID, "pus01");
    }
}

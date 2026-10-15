package com.innospots.nexus.console.scope.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.scope.ConsoleSessionScope;

import static org.assertj.core.api.Assertions.assertThat;

class SessionScopeTlcTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void platformRealmForcesPlatformSessionScopeOnPut() {
        TLC.securityRealm(SecurityRealm.PLATFORM.name());

        SessionScopeTlc.putSessionScope(ConsoleSessionScope.TENANT);

        assertThat(SessionScopeTlc.sessionScope()).isEqualTo(ConsoleSessionScope.PLATFORM);
        assertThat(TLC.getString(SessionScopeTlc.SESSION_SCOPE)).isEqualTo(ConsoleSessionScope.PLATFORM.name());
    }

    @Test
    void tenantRealmRejectsPlatformSessionScopeOnRead() {
        TLC.securityRealm(SecurityRealm.TENANT.name());
        TLC.put(SessionScopeTlc.SESSION_SCOPE, ConsoleSessionScope.PLATFORM.name());

        assertThat(SessionScopeTlc.sessionScope()).isEqualTo(ConsoleSessionScope.TENANT);
    }

    @Test
    void workspaceScopeDowngradesWithoutWorkspaceSnapshot() {
        TLC.securityRealm(SecurityRealm.TENANT.name());
        SessionScopeTlc.putSessionScope(ConsoleSessionScope.WORKSPACE);

        assertThat(SessionScopeTlc.sessionScope()).isEqualTo(ConsoleSessionScope.TENANT);
    }
}

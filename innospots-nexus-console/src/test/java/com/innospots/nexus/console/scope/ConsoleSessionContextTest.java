package com.innospots.nexus.console.scope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleSessionContextTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void infersPlatformScopeFromRealm() {
        TLC.put(TLC.SECURITY_REALM, SecurityRealm.PLATFORM.name());
        assertThat(ConsoleSessionContext.inferFromSession()).isEqualTo(ConsoleSessionScope.PLATFORM);
    }

    @Test
    void infersTenantScopeWhenTenantBoundWithoutWorkspace() {
        bindTenantOnly("tnt-1");
        assertThat(ConsoleSessionContext.inferFromSession()).isEqualTo(ConsoleSessionScope.TENANT);
    }

    @Test
    void explicitScopeOverridesInference() {
        bindTenantOnly("tnt-1");
        TLC.put(TLC.SECURITY_REALM, SecurityRealm.TENANT.name());
        ConsoleSessionContext.bindScope(ConsoleSessionScope.TENANT);
        assertThat(ConsoleSessionContext.scope()).isEqualTo(ConsoleSessionScope.TENANT);
    }

    @Test
    void platformRealmCannotRetainTenantScope() {
        TLC.put(TLC.SECURITY_REALM, SecurityRealm.PLATFORM.name());
        ConsoleSessionContext.bindScope(ConsoleSessionScope.WORKSPACE);
        assertThat(ConsoleSessionContext.scope()).isEqualTo(ConsoleSessionScope.PLATFORM);
    }

    private static void bindTenantOnly(String tenantId) {
        TenantSnapshot tenant = new TenantSnapshot(tenantId, tenantId, tenantId, BasicStatus.ENABLED);
        OrganizationSnapshot organization = new OrganizationSnapshot(
                tenantId, tenantId, tenantId, "en-US", "USD", null, BasicStatus.ENABLED);
        SessionContext.bindTenant(tenant, organization);
    }
}

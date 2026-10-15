package com.innospots.nexus.console.role.domain.enums;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

import static org.assertj.core.api.Assertions.assertThat;

class RoleOwnerTypeTest {

    @Test
    void resolvesSecurityRealmFromOwnerType() {
        assertThat(RoleOwnerType.PLATFORM.securityRealm()).isEqualTo(SecurityRealm.PLATFORM);
        assertThat(RoleOwnerType.TENANT.securityRealm()).isEqualTo(SecurityRealm.TENANT);
        assertThat(RoleOwnerType.WORKSPACE.securityRealm()).isEqualTo(SecurityRealm.TENANT);
    }
}

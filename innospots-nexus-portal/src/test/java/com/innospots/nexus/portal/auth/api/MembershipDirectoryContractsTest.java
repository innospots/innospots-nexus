package com.innospots.nexus.portal.auth.api;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.portal.auth.domain.model.TenantMembership;

import static org.assertj.core.api.Assertions.assertThat;

class MembershipDirectoryContractsTest {

    @Test
    void membershipDirectoryIsInterface() throws Exception {
        assertThat(MembershipDirectory.class).isInterface();
        assertThat(MembershipDirectory.class.getMethod("listActiveMemberships", String.class)).isNotNull();
        assertThat(TenantMembership.class.isRecord()).isTrue();
    }
}

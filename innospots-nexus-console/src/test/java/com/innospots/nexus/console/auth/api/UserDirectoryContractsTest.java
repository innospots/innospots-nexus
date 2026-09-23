package com.innospots.nexus.console.auth.api;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;

import static org.assertj.core.api.Assertions.assertThat;

class UserDirectoryContractsTest {

    @Test
    void authPortsAreInterfaces() throws Exception {
        assertThat(UserDirectory.class).isInterface();
        assertThat(UserDirectory.class.getMethod("findById", String.class)).isNotNull();
        assertThat(AuthUser.class.isRecord()).isTrue();
        assertThat(CredentialRecord.class.isRecord()).isTrue();
    }
}

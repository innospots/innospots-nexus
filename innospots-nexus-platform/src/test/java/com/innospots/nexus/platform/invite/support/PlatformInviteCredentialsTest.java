package com.innospots.nexus.platform.invite.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformInviteCredentialsTest {

    @Test
    void newInviteCodeUsesDashFormat() {
        String code = PlatformInviteCredentials.newInviteCode();
        assertThat(code).matches("[A-Z0-9]{4}-[A-Z0-9]{4}");
    }

    @Test
    void normalizeInviteCodeStripsSpacesAndUppercases() {
        assertThat(PlatformInviteCredentials.normalizeInviteCode("ab12 cd34"))
                .isEqualTo("AB12-CD34");
    }
}

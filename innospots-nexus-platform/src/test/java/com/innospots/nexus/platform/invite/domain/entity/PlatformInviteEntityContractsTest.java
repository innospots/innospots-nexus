package com.innospots.nexus.platform.invite.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformInviteEntityContractsTest {

    @Test
    void usesUppercaseIdPrefix() {
        assertThat(new PlatformInviteEntity().idPrefix()).isEqualTo("PIV");
    }
}

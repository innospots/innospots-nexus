package com.innospots.nexus.platform.access.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformAccessRequestEntityContractsTest {

    @Test
    void usesUppercaseIdPrefix() {
        assertThat(new PlatformAccessRequestEntity().idPrefix()).isEqualTo("PAR");
    }
}

package com.innospots.nexus.console.logger.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogEntityContractsTest {

    @Test
    void usesUppercaseIdPrefix() {
        assertThat(new AuditLogEntity().idPrefix()).isEqualTo("ALG");
    }
}

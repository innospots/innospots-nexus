package com.innospots.nexus.core.plugin.installation.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PluginInstallationEntityContractsTest {

    @Test
    void usesUppercaseIdPrefix() {
        assertThat(new PluginInstallationEntity().idPrefix()).isEqualTo("PLG");
    }
}

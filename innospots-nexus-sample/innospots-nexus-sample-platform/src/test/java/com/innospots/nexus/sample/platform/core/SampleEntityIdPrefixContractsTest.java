package com.innospots.nexus.sample.platform.core;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.sample.platform.core.announcement.domain.entity.AnnouncementEntity;
import com.innospots.nexus.sample.platform.core.support.domain.entity.SupportTicketEntity;

import static org.assertj.core.api.Assertions.assertThat;

class SampleEntityIdPrefixContractsTest {

    @Test
    void sampleEntitiesUseStableUppercaseIdPrefixes() {
        assertThat(new AnnouncementEntity().idPrefix()).isEqualTo("ANN");
        assertThat(new SupportTicketEntity().idPrefix()).isEqualTo("SPT");
    }
}

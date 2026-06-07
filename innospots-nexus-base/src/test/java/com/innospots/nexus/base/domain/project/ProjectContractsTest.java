package com.innospots.nexus.base.domain.project;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectContractsTest {

    @Test
    void definesProjectProfile() {
        ProjectInfo project = ProjectInfo.named(100L, "Nexus", "nexus")
                .organizationId(1L)
                .description("AI platform foundation")
                .status(BasicStatus.ENABLED);

        assertThat(project.projectId()).isEqualTo(100L);
        assertThat(project.projectCode()).isEqualTo("nexus");
        assertThat(project.organizationId()).isEqualTo(1L);
        assertThat(project.status()).isEqualTo(BasicStatus.ENABLED);
    }
}

package com.innospots.nexus.base.domain.project;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectContractsTest {

    @Test
    void definesProjectProfileUnderWorkspace() {
        ProjectSnapshot project = new ProjectSnapshot(
                "tnt01", "wks01", "prj01", "nexus", "Nexus", "AI platform foundation", BasicStatus.ENABLED);

        assertThat(project.projectId()).isEqualTo("prj01");
        assertThat(project.workspaceId()).isEqualTo("wks01");
        assertThat(project.tenantId()).isEqualTo("tnt01");
        assertThat(project.status()).isEqualTo(BasicStatus.ENABLED);
    }
}

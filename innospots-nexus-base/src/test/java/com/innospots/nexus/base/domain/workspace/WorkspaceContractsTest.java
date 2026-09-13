package com.innospots.nexus.base.domain.workspace;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceContractsTest {

    @Test
    void definesWorkspaceProfile() {
        WorkspaceSnapshot workspace = new WorkspaceSnapshot(
                "tnt01", "wks01", "default", "Default Workspace", BasicStatus.ENABLED);

        assertThat(workspace.workspaceId()).isEqualTo("wks01");
        assertThat(workspace.tenantId()).isEqualTo("tnt01");
        assertThat(workspace.status()).isEqualTo(BasicStatus.ENABLED);
    }
}

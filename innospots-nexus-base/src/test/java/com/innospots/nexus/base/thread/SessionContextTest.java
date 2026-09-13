package com.innospots.nexus.base.thread;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionContextTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void bindsUserSnapshotAndSynchronizesTlcKeys() {
        UserSnapshot user = UserSnapshot.simple(9L, "alice", "Alice");

        SessionContext.bindUser(user);

        assertThat(SessionContext.user()).contains(user);
        assertThat(TLC.userId()).isEqualTo(9L);
        assertThat(TLC.userName()).isEqualTo("alice");
    }

    @Test
    void bindsTenantOrganizationAndWorkspaceScope() {
        TenantSnapshot tenant = new TenantSnapshot("tnt01", "acme", "Acme", BasicStatus.ENABLED);
        OrganizationSnapshot organization = new OrganizationSnapshot(
                "tnt01", "acme", "Acme Corp", "zh-CN", "CNY", "logo", BasicStatus.ENABLED);
        WorkspaceSnapshot workspace = new WorkspaceSnapshot(
                "tnt01", "wks01", "default", "Default Workspace", BasicStatus.ENABLED);

        SessionContext.bindTenant(tenant, organization);
        SessionContext.bindWorkspace(workspace);

        assertThat(SessionContext.tenant()).contains(tenant);
        assertThat(SessionContext.organization()).contains(organization);
        assertThat(SessionContext.workspace()).contains(workspace);
        assertThat(SessionContext.tenantId()).isEqualTo("tnt01");
        assertThat(SessionContext.workspaceId()).isEqualTo("wks01");
    }

    @Test
    void bindsProjectScopeUnderWorkspace() {
        ProjectSnapshot project = new ProjectSnapshot(
                "tnt01", "wks01", "prj01", "nexus", "Nexus", "foundation", BasicStatus.ENABLED);

        SessionContext.bindProject(project);

        assertThat(SessionContext.project()).contains(project);
        assertThat(SessionContext.projectId()).isEqualTo("prj01");
        assertThat(SessionContext.tenantId()).isEqualTo("tnt01");
        assertThat(SessionContext.workspaceId()).isEqualTo("wks01");
    }

    @Test
    void clearsBoundUserAndTlcIdentityKeys() {
        SessionContext.bindUser(UserSnapshot.simple(1L, "bob", "Bob"));

        SessionContext.clearUser();

        assertThat(SessionContext.user()).isEmpty();
        assertThat(TLC.userId()).isNull();
        assertThat(TLC.userName()).isNull();
    }

    @Test
    void requireUserFailsWhenNoIdentityIsBound() {
        assertThatThrownBy(SessionContext::requireUser)
                .isInstanceOf(NexusException.class);
    }

    @Test
    void requireWorkspaceIdFailsWhenWorkspaceIsMissing() {
        assertThatThrownBy(SessionContext::requireWorkspaceId)
                .isInstanceOf(NexusException.class);
    }
}

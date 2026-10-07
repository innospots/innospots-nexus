package com.innospots.nexus.core.persistence.scope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.core.resource.domain.entity.MetaResourceEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwnershipScopeTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void captureFromSessionPrefersWorkspaceOwnership() {
        bindWorkspace("tnt-a", "wks-a");
        PersistenceOwnership ownership = OwnershipScope.captureFromSession();

        assertThat(ownership.ownerType()).isEqualTo(OwnerType.WORKSPACE);
        assertThat(ownership.ownerId()).isEqualTo("wks-a");
        assertThat(ownership.securityRealm()).isEqualTo("TENANT");
    }

    @Test
    void assertOwnershipRejectsForeignWorkspace() {
        bindWorkspace("tnt-a", "wks-a");
        MetaResourceEntity entity = new MetaResourceEntity();
        OwnershipScope.stamp(entity, new PersistenceOwnership(OwnerType.WORKSPACE, "wks-b", "TENANT"));

        assertThatThrownBy(() -> OwnershipScope.assertOwnership(entity, OwnershipScope.captureFromSession()))
                .isInstanceOf(NexusException.class);
    }

    private static void bindWorkspace(String tenantId, String workspaceId) {
        TenantSnapshot tenant = new TenantSnapshot(tenantId, "code", "Tenant", BasicStatus.ENABLED);
        OrganizationSnapshot organization = new OrganizationSnapshot(
                tenantId, "org", "Org", "zh-CN", "CNY", null, BasicStatus.ENABLED);
        SessionContext.bindTenant(tenant, organization);
        SessionContext.bindWorkspace(new WorkspaceSnapshot(
                tenantId, workspaceId, "main", "Main", BasicStatus.ENABLED));
    }
}

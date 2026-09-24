package com.innospots.nexus.portal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortalModuleBoundaryTest {

    @Test
    void portalDoesNotHostConsoleOwnedIamOrRuntimeTypes() {
        assertTypeAbsent("com.innospots.nexus.portal.role.endpoint.RoleEndpoint");
        assertTypeAbsent("com.innospots.nexus.portal.role.domain.entity.RoleEntity");
        assertTypeAbsent("com.innospots.nexus.portal.menu.endpoint.MenuEndpoint");
        assertTypeAbsent("com.innospots.nexus.portal.menu.domain.entity.MenuEntity");
        assertTypeAbsent("com.innospots.nexus.portal.group.endpoint.GroupEndpoint");
        assertTypeAbsent("com.innospots.nexus.portal.group.domain.entity.GroupEntity");
        assertTypeAbsent("com.innospots.nexus.portal.permission.endpoint.GrantManagementEndpoint");
        assertTypeAbsent("com.innospots.nexus.portal.permission.authorization.RequestAuthorizer");
        assertTypeAbsent("com.innospots.nexus.portal.permission.domain.entity.PermissionResourceEntity");
        assertTypeAbsent("com.innospots.nexus.portal.logger.AuditLog");
        assertTypeAbsent("com.innospots.nexus.portal.logger.LogExecutor");
        assertTypeAbsent("com.innospots.nexus.portal.extension.service.ExtensionRegistry");
        assertTypeAbsent("com.innospots.nexus.portal.extension.domain.entity.ExtensionInstallationEntity");
    }

    @Test
    void portalKeepsTenantDomainsWithoutPermissionCatalogSync() throws ClassNotFoundException {
        assertTypeAbsent("com.innospots.nexus.portal.permission.service.PermissionResourceSyncService");
        assertThat(Class.forName("com.innospots.nexus.portal.user.operator.UserOperator"))
                .isNotInterface();
        assertThat(Class.forName("com.innospots.nexus.portal.member.domain.entity.TenantMemberEntity"))
                .isNotInterface();
        assertThat(Class.forName("com.innospots.nexus.portal.organization.domain.entity.OrganizationUnitEntity"))
                .isNotInterface();
        assertThat(Class.forName("com.innospots.nexus.portal.workspace.domain.entity.WorkspaceEntity"))
                .isNotInterface();
    }

    private static void assertTypeAbsent(String className) {
        assertThatThrownBy(() -> Class.forName(className))
                .as(className)
                .isInstanceOf(ClassNotFoundException.class);
    }
}

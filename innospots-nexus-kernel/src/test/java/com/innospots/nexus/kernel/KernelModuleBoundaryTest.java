package com.innospots.nexus.kernel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KernelModuleBoundaryTest {

    @Test
    void kernelDoesNotHostConsoleOwnedIamOrRuntimeTypes() {
        assertTypeAbsent("com.innospots.nexus.kernel.role.endpoint.RoleEndpoint");
        assertTypeAbsent("com.innospots.nexus.kernel.role.domain.entity.RoleEntity");
        assertTypeAbsent("com.innospots.nexus.kernel.menu.endpoint.MenuEndpoint");
        assertTypeAbsent("com.innospots.nexus.kernel.menu.domain.entity.MenuEntity");
        assertTypeAbsent("com.innospots.nexus.kernel.group.endpoint.GroupEndpoint");
        assertTypeAbsent("com.innospots.nexus.kernel.group.domain.entity.GroupEntity");
        assertTypeAbsent("com.innospots.nexus.kernel.permission.endpoint.GrantManagementEndpoint");
        assertTypeAbsent("com.innospots.nexus.kernel.permission.authorization.RequestAuthorizer");
        assertTypeAbsent("com.innospots.nexus.kernel.permission.domain.entity.PermissionResourceEntity");
        assertTypeAbsent("com.innospots.nexus.kernel.logger.AuditLog");
        assertTypeAbsent("com.innospots.nexus.kernel.logger.LogExecutor");
        assertTypeAbsent("com.innospots.nexus.kernel.extension.service.ExtensionRegistry");
        assertTypeAbsent("com.innospots.nexus.kernel.extension.domain.entity.ExtensionInstallationEntity");
    }

    @Test
    void kernelKeepsTenantDomainsWithoutPermissionCatalogSync() throws ClassNotFoundException {
        assertTypeAbsent("com.innospots.nexus.kernel.permission.service.PermissionResourceSyncService");
        assertThat(Class.forName("com.innospots.nexus.kernel.user.operator.UserOperator"))
                .isNotInterface();
        assertThat(Class.forName("com.innospots.nexus.kernel.member.domain.entity.TenantMemberEntity"))
                .isNotInterface();
        assertThat(Class.forName("com.innospots.nexus.kernel.organization.domain.entity.OrganizationUnitEntity"))
                .isNotInterface();
        assertThat(Class.forName("com.innospots.nexus.kernel.workspace.domain.entity.WorkspaceEntity"))
                .isNotInterface();
    }

    private static void assertTypeAbsent(String className) {
        assertThatThrownBy(() -> Class.forName(className))
                .as(className)
                .isInstanceOf(ClassNotFoundException.class);
    }
}

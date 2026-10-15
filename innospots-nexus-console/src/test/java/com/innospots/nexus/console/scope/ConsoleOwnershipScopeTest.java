package com.innospots.nexus.console.scope;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

class ConsoleOwnershipScopeTest {

    @Test
    void allowsReadablePlatformRoleInPlatformSession() {
        RoleEntity platformRole = new RoleEntity();
        platformRole.setOwnerType(RoleOwnerType.PLATFORM.name());
        platformRole.setOwnerId(null);
        platformRole.setSecurityRealm(SecurityRealm.PLATFORM.name());
        platformRole.setStatus(BasicStatus.ENABLED.name());

        ConsoleOwnership platformSession = new ConsoleOwnership(
                RoleOwnerType.PLATFORM,
                null,
                SecurityRealm.PLATFORM.name());

        assertThatCode(() -> ConsoleOwnershipScope.assertRoleReadable(
                platformRole,
                null,
                platformSession)).doesNotThrowAnyException();
    }

    @Test
    void tenantSessionRejectsWorkspaceScopedRole() {
        RoleEntity workspaceRole = new RoleEntity();
        workspaceRole.setOwnerType(RoleOwnerType.WORKSPACE.name());
        workspaceRole.setOwnerId("wks-1");
        workspaceRole.setSecurityRealm(SecurityRealm.TENANT.name());

        ConsoleOwnership tenantSession = new ConsoleOwnership(
                RoleOwnerType.TENANT,
                "tnt-1",
                SecurityRealm.TENANT.name());

        assertThatThrownBy(() -> ConsoleOwnershipScope.assertRoleReadable(
                workspaceRole,
                "tnt-1",
                tenantSession)).isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
    }

    @Test
    void platformSessionRejectsTenantScopedRole() {
        RoleEntity tenantRole = new RoleEntity();
        tenantRole.setOwnerType(RoleOwnerType.TENANT.name());
        tenantRole.setOwnerId("tnt-1");
        tenantRole.setSecurityRealm(SecurityRealm.TENANT.name());

        ConsoleOwnership platformSession = new ConsoleOwnership(
                RoleOwnerType.PLATFORM,
                null,
                SecurityRealm.PLATFORM.name());

        assertThatThrownBy(() -> ConsoleOwnershipScope.assertRoleReadable(
                tenantRole,
                null,
                platformSession)).isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
    }
}

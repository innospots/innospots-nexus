package com.innospots.nexus.console.scope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.jaxrs.support.ConsoleDevSessionBinder;
import com.innospots.nexus.console.jaxrs.web.ConsoleWebDevSessionSettings;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

class ConsoleOwnershipScopeTest {

    @AfterEach
    void clearSession() {
        TLC.clear();
    }

    @Test
    void platformAuditIgnoresTenantAndWorkspaceSnapshots() {
        ConsoleWebDevSessionSettings settings = new ConsoleWebDevSessionSettings();
        settings.setRealm(SecurityRealm.PLATFORM.name());
        ConsoleDevSessionBinder.bind(settings);

        ConsoleOwnership ownership = ConsoleOwnershipScope.captureForAudit();

        assertThat(ownership.ownerType()).isEqualTo(RoleOwnerType.PLATFORM);
        assertThat(ownership.ownerId()).isNull();
        assertThat(ownership.securityRealm()).isEqualTo(SecurityRealm.PLATFORM.name());
    }

    @Test
    void tenantAuditKeepsWorkspaceOwnership() {
        ConsoleWebDevSessionSettings settings = new ConsoleWebDevSessionSettings();
        settings.setRealm(SecurityRealm.TENANT.name());
        ConsoleDevSessionBinder.bind(settings);

        ConsoleOwnership ownership = ConsoleOwnershipScope.captureForAudit();

        assertThat(ownership.ownerType()).isEqualTo(RoleOwnerType.WORKSPACE);
        assertThat(ownership.ownerId()).isEqualTo(settings.getWorkspaceId());
        assertThat(ownership.securityRealm()).isEqualTo(SecurityRealm.TENANT.name());
    }

    @Test
    void tenantAuditWithoutWorkspaceKeepsTenantOwnership() {
        ConsoleWebDevSessionSettings settings = new ConsoleWebDevSessionSettings();
        settings.setRealm(SecurityRealm.TENANT.name());
        settings.setWorkspaceId(null);
        ConsoleDevSessionBinder.bind(settings);

        ConsoleOwnership ownership = ConsoleOwnershipScope.captureForAudit();

        assertThat(ownership.ownerType()).isEqualTo(RoleOwnerType.TENANT);
        assertThat(ownership.ownerId()).isEqualTo(settings.getTenantId());
        assertThat(ownership.securityRealm()).isEqualTo(SecurityRealm.TENANT.name());
    }

    @Test
    void applyPlatformOwnershipBuildsSqlSegmentWithoutEntityLambdaCache() {
        ConsoleOwnership platformSession = new ConsoleOwnership(
                RoleOwnerType.PLATFORM,
                null,
                SecurityRealm.PLATFORM.name());

        // 仅走 apply SQL 片段，不附带实体方法引用，避免无 MyBatis 元数据时的 lambda 缓存依赖
        LambdaQueryWrapper<RoleBindingEntity> query = ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<>(),
                platformSession);

        assertThatCode(query::getCustomSqlSegment).doesNotThrowAnyException();
        String sql = query.getCustomSqlSegment();
        assertThat(sql).contains("owner_type");
        assertThat(sql).contains("owner_id");
        assertThat(sql).contains("security_realm");
    }

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

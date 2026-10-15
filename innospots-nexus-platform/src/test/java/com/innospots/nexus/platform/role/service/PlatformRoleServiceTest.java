package com.innospots.nexus.platform.role.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.operator.RoleOperator;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformRoleServiceTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void createRoleHardcodesPlatformOwnership() {
        bindPlatformSession();
        RoleOperator operator = mock(RoleOperator.class);
        RoleVo vo = new RoleVo(
                "rol-1",
                "Ops",
                "ops",
                RoleOwnerType.PLATFORM,
                null,
                SecurityRealm.PLATFORM,
                "d",
                BasicStatus.ENABLED,
                1,
                false,
                false,
                0L,
                null,
                null);
        when(operator.createRole(eq("Ops"), eq("ops"), eq("d"), eq(RoleOwnerType.PLATFORM), isNull()))
                .thenReturn(vo);
        PlatformRoleService service = new PlatformRoleService(operator);

        RoleVo created = service.createRole(new RoleCreateRequest("Ops", "ops", "d"));

        assertThat(created.ownerType()).isEqualTo(RoleOwnerType.PLATFORM);
        verify(operator).createRole("Ops", "ops", "d", RoleOwnerType.PLATFORM, null);
    }

    @Test
    void getRoleDelegatesWithOwnershipOnlyOperatorApi() {
        bindPlatformSession();
        RoleOperator operator = mock(RoleOperator.class);
        PlatformRoleService service = new PlatformRoleService(operator);

        service.getRole("rol-1");

        verify(operator).getRole(eq("rol-1"), any(ConsoleOwnership.class));
    }

    @Test
    void createRoleRejectsNonPlatformSession() {
        ConsoleSessionContext.bindScope(ConsoleSessionScope.TENANT);
        TLC.put(TLC.SECURITY_REALM, SecurityRealm.TENANT.name());
        PlatformRoleService service = new PlatformRoleService(mock(RoleOperator.class));

        assertThatThrownBy(() -> service.createRole(new RoleCreateRequest("Ops", "ops", null)))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
    }

    private static void bindPlatformSession() {
        TLC.put(TLC.SECURITY_REALM, SecurityRealm.PLATFORM.name());
        ConsoleSessionContext.bindScope(ConsoleSessionScope.PLATFORM);
    }
}

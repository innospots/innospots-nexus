package com.innospots.nexus.platform.role.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PlatformRoleBindingServiceTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void addBindingsUsesPlatformOwnership() {
        bindPlatformSession();
        RoleBindingOperator operator = mock(RoleBindingOperator.class);
        PlatformRoleBindingService service = new PlatformRoleBindingService(operator);
        RoleBindingAddRequest request = new RoleBindingAddRequest(
                RoleBindingSubjectType.USER, java.util.List.of("u-1"));

        service.addRoleBindings("rol-1", request);

        verify(operator).addRoleBindings(eq("rol-1"), eq(request), any(ConsoleOwnership.class));
    }

    @Test
    void rejectsNonPlatformSession() {
        ConsoleSessionContext.bindScope(ConsoleSessionScope.TENANT);
        TLC.put(TLC.SECURITY_REALM, SecurityRealm.TENANT.name());
        PlatformRoleBindingService service = new PlatformRoleBindingService(mock(RoleBindingOperator.class));

        assertThatThrownBy(() -> service.pageRoleBindings("rol-1", null))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
    }

    private static void bindPlatformSession() {
        TLC.put(TLC.SECURITY_REALM, SecurityRealm.PLATFORM.name());
        ConsoleSessionContext.bindScope(ConsoleSessionScope.PLATFORM);
    }
}

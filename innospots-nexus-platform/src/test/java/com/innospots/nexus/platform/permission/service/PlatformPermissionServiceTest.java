package com.innospots.nexus.platform.permission.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.permission.service.PermissionGrantService;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformPermissionServiceTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void listRolePermissionsUsesOwnershipOnlyGrantApi() {
        bindPlatformSession();
        PermissionGrantService grantService = mock(PermissionGrantService.class);
        PermissionGrantReplaceRequest expected = new PermissionGrantReplaceRequest(java.util.List.of());
        when(grantService.list(eq(PermissionSubjectType.ROLE), eq("rol-1"), any(ConsoleOwnership.class)))
                .thenReturn(expected);

        PlatformPermissionService service = new PlatformPermissionService(
                grantService,
                mock(AuthorizationSubjectResolver.class),
                mock(PermissionVisibilityService.class));

        service.listRolePermissions("rol-1");

        verify(grantService).list(eq(PermissionSubjectType.ROLE), eq("rol-1"), any(ConsoleOwnership.class));
    }

    private static void bindPlatformSession() {
        TLC.put(TLC.SECURITY_REALM, SecurityRealm.PLATFORM.name());
        ConsoleSessionContext.bindScope(ConsoleSessionScope.PLATFORM);
    }
}

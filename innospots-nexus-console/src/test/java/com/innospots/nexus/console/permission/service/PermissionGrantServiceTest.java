package com.innospots.nexus.console.permission.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantItemRequest;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PermissionGrantServiceTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void listWithOwnershipOnlyLoadsGrantsWithoutSubjectVisibilityCheck() {
        ConsoleOwnership ownership = new ConsoleOwnership(
                RoleOwnerType.PLATFORM, null, SecurityRealm.PLATFORM.name());
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(List.of(grantRow("platform.user:default", null)));
        GrantSubjectAccess grantSubjectAccess = mock(GrantSubjectAccess.class);
        PermissionGrantService service = new PermissionGrantService(
                grantDao, catalogService("platform.user:default"), grantSubjectAccess);

        PermissionGrantReplaceRequest result = service.list(PermissionSubjectType.ROLE, "rol-1", ownership);

        assertThat(result.grants()).hasSize(1);
        verifyNoInteractions(grantSubjectAccess);
    }

    @Test
    void listMapsPersistedGrantsToReplaceRequest() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(List.of(
                grantRow("platform.user:default", null),
                grantRow("platform.user:platform.user.create", "{\"filter\":\"x\"}")));
        GrantSubjectAccess grantSubjectAccess = mock(GrantSubjectAccess.class);

        PermissionGrantService service = new PermissionGrantService(
                grantDao,
                catalogService(
                        "platform.user:default",
                        "platform.user:platform.user.create"),
                grantSubjectAccess);
        PermissionGrantReplaceRequest result = service.list(
                PermissionSubjectType.ROLE, "rol-1", ownership, "tnt-1", "wks-1");

        assertThat(result.grants()).containsExactly(
                new PermissionGrantItemRequest("platform.user:default", null),
                new PermissionGrantItemRequest("platform.user:platform.user.create", "{\"filter\":\"x\"}"));
        verifyNoInteractions(grantSubjectAccess);
    }

    @Test
    void replaceRejectsDuplicateResourceKeys() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantService service = newService(
                mock(PermissionGrantDao.class),
                catalogService("platform.user:default"));

        PermissionGrantReplaceRequest request = new PermissionGrantReplaceRequest(List.of(
                new PermissionGrantItemRequest("platform.user:default", null),
                new PermissionGrantItemRequest("platform.user:default", null)));

        assertThatThrownBy(() -> service.replace(
                PermissionSubjectType.ROLE, "rol-1", request, ownership, "tnt-1", "wks-1"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("unique");
    }

    @Test
    void replaceRejectsUnknownResourceKey() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        PermissionGrantService service = newService(grantDao, catalogService());
        PermissionGrantReplaceRequest request = new PermissionGrantReplaceRequest(List.of(
                new PermissionGrantItemRequest("module:sales", null)));

        assertThatThrownBy(() -> service.replace(
                PermissionSubjectType.ROLE, "rol-1", request, ownership, "tnt-1", "wks-1"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("Unknown permission resource");
    }

    @Test
    void replaceAcceptsPageAccessGrantKey() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        PermissionGrantService service = newService(
                grantDao,
                catalogService("platform.user:default"));
        PermissionGrantReplaceRequest request = new PermissionGrantReplaceRequest(List.of(
                new PermissionGrantItemRequest("platform.user:default", null)));

        service.replace(
                PermissionSubjectType.ROLE, "rol-1", request, ownership, "tnt-1", "wks-1");

        verify(grantDao).insert(any(PermissionGrantEntity.class));
    }

    @Test
    void replaceAcceptsOperationGrantKey() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        PermissionGrantService service = newService(
                grantDao,
                catalogService("platform.user:platform.user.create"));
        PermissionGrantReplaceRequest request = new PermissionGrantReplaceRequest(List.of(
                new PermissionGrantItemRequest("platform.user:platform.user.create", null)));

        service.replace(
                PermissionSubjectType.ROLE, "rol-1", request, ownership, "tnt-1", "wks-1");

        verify(grantDao).insert(any(PermissionGrantEntity.class));
    }

    @Test
    void replaceRejectsConstraintDefinition() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantService service = newService(
                mock(PermissionGrantDao.class),
                catalogService("sales.orders:sales-orders"));
        PermissionGrantReplaceRequest request = new PermissionGrantReplaceRequest(List.of(
                new PermissionGrantItemRequest("sales.orders:sales-orders", "{\"filter\":\"x\"}")));

        assertThatThrownBy(() -> service.replace(
                PermissionSubjectType.ROLE, "rol-1", request, ownership, "tnt-1", "wks-1"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("Constraint");
    }

    @Test
    void replaceDeletesPreviousGrantsAndInsertsNewRows() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        List<PermissionGrantEntity> inserted = new ArrayList<>();
        doAnswer(invocation -> {
            inserted.add(invocation.getArgument(0));
            return 1;
        }).when(grantDao).insert(any(PermissionGrantEntity.class));

        GrantSubjectAccess grantSubjectAccess = mock(GrantSubjectAccess.class);
        PermissionGrantService service = new PermissionGrantService(
                grantDao,
                catalogService("sales.orders:sales-orders"),
                grantSubjectAccess);

        PermissionGrantReplaceRequest request = new PermissionGrantReplaceRequest(List.of(
                new PermissionGrantItemRequest("sales.orders:sales-orders", null)));

        service.replace(PermissionSubjectType.ROLE, "rol-9", request, ownership, "tnt-1", "wks-1");

        verify(grantDao).delete(any());
        assertThat(inserted).hasSize(1);
        assertThat(inserted.get(0).getSubjectType()).isEqualTo(PermissionSubjectType.ROLE.name());
        assertThat(inserted.get(0).getSubjectId()).isEqualTo("rol-9");
        assertThat(inserted.get(0).getGrantKey()).isEqualTo("sales.orders:sales-orders");
        assertThat(inserted.get(0).getOwnerId()).isEqualTo("wks-1");
        verify(grantSubjectAccess).ensureSubjectInScope(
                PermissionSubjectType.ROLE, "rol-9", ownership, "tnt-1", "wks-1");
    }

    @Test
    void replaceFullReplaceClearsGrantsWhenRequestIsEmpty() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        GrantSubjectAccess grantSubjectAccess = mock(GrantSubjectAccess.class);
        PermissionGrantService service = new PermissionGrantService(
                grantDao, catalogService(), grantSubjectAccess);

        service.replace(
                PermissionSubjectType.ROLE,
                "rol-1",
                new PermissionGrantReplaceRequest(List.of()),
                ownership,
                "tnt-1",
                "wks-1");

        verify(grantDao).delete(any());
        verify(grantDao, never()).insert(any(PermissionGrantEntity.class));
    }

    private static PermissionGrantService newService(
            PermissionGrantDao grantDao,
            ConsoleCatalogService catalogService
    ) {
        return new PermissionGrantService(grantDao, catalogService, mock(GrantSubjectAccess.class));
    }

    private static ConsoleCatalogService catalogService(String... grantKeys) {
        ConsoleCatalogService service = mock(ConsoleCatalogService.class);
        when(service.grantResourceKeys()).thenReturn(Set.of(grantKeys));
        return service;
    }

    private static ConsoleOwnership workspaceOwnership(String workspaceId) {
        return ConsoleOwnershipScope.workspaceOwnership(workspaceId, SecurityRealm.TENANT.name());
    }

    private static PermissionGrantEntity grantRow(String grantKey, String constraint) {
        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setGrantKey(grantKey);
        grant.setConstraintDefinition(constraint);
        return grant;
    }

    private static void bindWorkspaceScope(String tenantId, String workspaceId) {
        TenantSnapshot tenant = new TenantSnapshot(tenantId, "code", "Tenant", BasicStatus.ENABLED);
        OrganizationSnapshot organization = new OrganizationSnapshot(
                tenantId, "org", "Org", "zh-CN", "CNY", null, BasicStatus.ENABLED);
        SessionContext.bindTenant(tenant, organization);
        SessionContext.bindWorkspace(new WorkspaceSnapshot(
                tenantId, workspaceId, "main", "Main", BasicStatus.ENABLED));
    }
}

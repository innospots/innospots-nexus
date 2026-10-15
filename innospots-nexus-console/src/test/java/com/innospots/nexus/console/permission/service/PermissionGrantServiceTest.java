package com.innospots.nexus.console.permission.service;

import java.util.ArrayList;
import java.util.List;

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
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
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
import static org.mockito.Mockito.when;

class PermissionGrantServiceTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void listWithOwnershipOnlySkipsTenantAndWorkspaceParameters() {
        ConsoleOwnership ownership = new ConsoleOwnership(
                RoleOwnerType.PLATFORM, null, SecurityRealm.PLATFORM.name());
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(List.of(grantRow("menu-res", null)));
        GrantSubjectAccess grantSubjectAccess = mock(GrantSubjectAccess.class);
        PermissionGrantService service = new PermissionGrantService(
                grantDao, mock(ConsoleCatalogResourceDao.class), grantSubjectAccess);

        PermissionGrantReplaceRequest result = service.list(PermissionSubjectType.ROLE, "rol-1", ownership);

        assertThat(result.grants()).hasSize(1);
        verify(grantSubjectAccess).ensureSubjectInScope(PermissionSubjectType.ROLE, "rol-1", ownership);
    }

    @Test
    void listMapsPersistedGrantsToReplaceRequest() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(List.of(
                grantRow("menu-res", null),
                grantRow("page-res", "{\"filter\":\"x\"}")));

        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        GrantSubjectAccess grantSubjectAccess = mock(GrantSubjectAccess.class);

        PermissionGrantService service = new PermissionGrantService(grantDao, resourceDao, grantSubjectAccess);
        PermissionGrantReplaceRequest result = service.list(
                PermissionSubjectType.ROLE, "rol-1", ownership, "tnt-1", "wks-1");

        assertThat(result.grants()).containsExactly(
                new PermissionGrantItemRequest("menu-res", null),
                new PermissionGrantItemRequest("page-res", "{\"filter\":\"x\"}"));
        verify(grantSubjectAccess).ensureSubjectInScope(
                PermissionSubjectType.ROLE, "rol-1", ownership, "tnt-1", "wks-1");
    }

    @Test
    void replaceRejectsDuplicateResourceIds() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantService service = newService(mock(PermissionGrantDao.class), mock(ConsoleCatalogResourceDao.class));

        PermissionGrantReplaceRequest request = new PermissionGrantReplaceRequest(List.of(
                new PermissionGrantItemRequest("res-a", null),
                new PermissionGrantItemRequest("res-a", null)));

        assertThatThrownBy(() -> service.replace(
                PermissionSubjectType.ROLE, "rol-1", request, ownership, "tnt-1", "wks-1"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("unique");
    }

    @Test
    void replaceRejectsGrantOnModuleResource() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        ConsoleCatalogResourceEntity module = catalogResource(
                "mod-1", CatalogResourceType.MODULE, "module:sales", null);
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(module));

        PermissionGrantService service = newService(grantDao, resourceDao);
        PermissionGrantReplaceRequest request = new PermissionGrantReplaceRequest(List.of(
                new PermissionGrantItemRequest("mod-1", null)));

        assertThatThrownBy(() -> service.replace(
                PermissionSubjectType.ROLE, "rol-1", request, ownership, "tnt-1", "wks-1"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("cannot be granted");
    }

    @Test
    void replaceRejectsPageWithoutMenuParentGrant() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        ConsoleCatalogResourceEntity module = catalogResource(
                "mod-1", CatalogResourceType.MODULE, "module:sales", null);
        ConsoleCatalogResourceEntity menu = catalogResource(
                "menu-1", CatalogResourceType.MENU, "menu:sales.orders", "mod-1");
        ConsoleCatalogResourceEntity page = catalogResource(
                "page-1", CatalogResourceType.PAGE, "page:sales.orders", "menu-1");

        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(page), List.of(menu));

        PermissionGrantService service = newService(mock(PermissionGrantDao.class), resourceDao);
        PermissionGrantReplaceRequest request = new PermissionGrantReplaceRequest(List.of(
                new PermissionGrantItemRequest("page-1", null)));

        assertThatThrownBy(() -> service.replace(
                PermissionSubjectType.ROLE, "rol-1", request, ownership, "tnt-1", "wks-1"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("parent must also be granted");
    }

    @Test
    void replaceDeletesPreviousGrantsAndInsertsNewRows() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        ConsoleCatalogResourceEntity module = catalogResource(
                "mod-1", CatalogResourceType.MODULE, "module:sales", null);
        ConsoleCatalogResourceEntity menu = catalogResource(
                "menu-1", CatalogResourceType.MENU, "menu:sales.orders", "mod-1");

        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(menu), List.of(module));

        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        List<PermissionGrantEntity> inserted = new ArrayList<>();
        doAnswer(invocation -> {
            inserted.add(invocation.getArgument(0));
            return 1;
        }).when(grantDao).insert(any(PermissionGrantEntity.class));

        GrantSubjectAccess grantSubjectAccess = mock(GrantSubjectAccess.class);
        PermissionGrantService service = new PermissionGrantService(grantDao, resourceDao, grantSubjectAccess);

        PermissionGrantReplaceRequest request = new PermissionGrantReplaceRequest(List.of(
                new PermissionGrantItemRequest("menu-1", null)));

        service.replace(PermissionSubjectType.ROLE, "rol-9", request, ownership, "tnt-1", "wks-1");

        verify(grantDao).delete(any());
        assertThat(inserted).hasSize(1);
        assertThat(inserted.get(0).getSubjectType()).isEqualTo(PermissionSubjectType.ROLE.name());
        assertThat(inserted.get(0).getSubjectId()).isEqualTo("rol-9");
        assertThat(inserted.get(0).getResourceId()).isEqualTo("menu-1");
        assertThat(inserted.get(0).getOwnerId()).isEqualTo("wks-1");
        verify(grantSubjectAccess).ensureSubjectInScope(
                PermissionSubjectType.ROLE, "rol-9", ownership, "tnt-1", "wks-1");
    }

    @Test
    void replaceFullReplaceClearsGrantsWhenRequestIsEmpty() {
        bindWorkspaceScope("tnt-1", "wks-1");
        ConsoleOwnership ownership = workspaceOwnership("wks-1");
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        GrantSubjectAccess grantSubjectAccess = mock(GrantSubjectAccess.class);
        PermissionGrantService service = new PermissionGrantService(grantDao, resourceDao, grantSubjectAccess);

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
            ConsoleCatalogResourceDao resourceDao
    ) {
        return new PermissionGrantService(grantDao, resourceDao, mock(GrantSubjectAccess.class));
    }

    private static ConsoleOwnership workspaceOwnership(String workspaceId) {
        return ConsoleOwnershipScope.workspaceOwnership(workspaceId, SecurityRealm.TENANT.name());
    }

    private static PermissionGrantEntity grantRow(String resourceId, String constraint) {
        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setResourceId(resourceId);
        grant.setConstraintDefinition(constraint);
        return grant;
    }

    private static ConsoleCatalogResourceEntity catalogResource(
            String id,
            CatalogResourceType type,
            String resourceKey,
            String parentId
    ) {
        ConsoleCatalogResourceEntity resource = new ConsoleCatalogResourceEntity();
        resource.setResourceId(id);
        resource.setResourceType(type.name());
        resource.setResourceKey(resourceKey);
        resource.setParentResourceId(parentId);
        resource.setStatus(BasicStatus.ENABLED.name());
        resource.setSecurityRealm("TENANT");
        return resource;
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

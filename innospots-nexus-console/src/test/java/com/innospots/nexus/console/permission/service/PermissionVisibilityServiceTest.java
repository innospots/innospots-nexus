package com.innospots.nexus.console.permission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

class PermissionVisibilityServiceTest {

    @Test
    void visibleIncludesModuleAndMenuWhenGranted() {
        ConsoleCatalogResourceEntity module = resource(
                "module", CatalogResourceType.MODULE, "module-1", null);
        ConsoleCatalogResourceEntity menu = resource(
                "menu", CatalogResourceType.MENU, "menu-1", "module-1");

        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(module, menu));

        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(List.of());

        AuthorizationSubject subject = new AuthorizationSubject(
                "user-1", java.util.Set.of(), java.util.Set.of(), true);
        List<ConsoleCatalogResourceEntity> visible = new PermissionVisibilityService(
                resourceDao, grantDao)
                .visible(ConsoleOwnershipScope.workspaceOwnership("ws-1", "TENANT"), subject);

        assertThat(visible).extracting(ConsoleCatalogResourceEntity::getResourceId)
                .contains("module-1", "menu-1");
    }

    @Test
    void visibleIncludesGrantedLeafModule() {
        ConsoleCatalogResourceEntity leafModule = resource(
                "module", CatalogResourceType.MODULE, "module-1", null);
        leafModule.setEntryPageKey("demo-main");
        leafModule.setViewGroups("[{\"group\":\"default\",\"label\":{\"en\":\"View\"}}]");

        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(leafModule));

        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setGrantKey("nexus.demo:default");
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(List.of(grant));

        AuthorizationSubject subject = new AuthorizationSubject(
                "user-1", java.util.Set.of("role-1"), java.util.Set.of(), false);
        List<ConsoleCatalogResourceEntity> visible = new PermissionVisibilityService(
                resourceDao, grantDao)
                .visible(ConsoleOwnershipScope.workspaceOwnership("ws-1", "TENANT"), subject);

        assertThat(visible).extracting(ConsoleCatalogResourceEntity::getResourceId)
                .containsExactly("module-1");
    }

    @Test
    void operationGrantDoesNotRevealNavigationMenu() {
        ConsoleCatalogResourceEntity leafModule = resource(
                "module", CatalogResourceType.MODULE, "module-1", null);
        leafModule.setEntryPageKey("demo-main");

        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(leafModule));

        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setGrantKey("nexus.demo:demo.create");
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(List.of(grant));

        AuthorizationSubject subject = new AuthorizationSubject(
                "user-1", java.util.Set.of("role-1"), java.util.Set.of(), false);
        List<ConsoleCatalogResourceEntity> visible = new PermissionVisibilityService(
                resourceDao, grantDao)
                .visible(ConsoleOwnershipScope.workspaceOwnership("ws-1", "TENANT"), subject);

        assertThat(visible).isEmpty();
    }

    private static ConsoleCatalogResourceEntity resource(
            String key,
            CatalogResourceType type,
            String id,
            String parentId
    ) {
        ConsoleCatalogResourceEntity resource = new ConsoleCatalogResourceEntity();
        resource.setResourceId(id);
        resource.setOwnerPluginId("com.innospots.nexus.test-plugin");
        resource.setDomainKey("nexus");
        resource.setModuleKey("demo");
        resource.setResourceType(type.name());
        resource.setResourceKey(key + ":" + id);
        resource.setParentResourceId(parentId);
        resource.setEntryPageKey(type == CatalogResourceType.MENU ? "demo-main" : null);
        resource.setSortOrder(0);
        resource.setStatus(BasicStatus.ENABLED.name());
        resource.setSecurityRealm("TENANT");
        return resource;
    }
}

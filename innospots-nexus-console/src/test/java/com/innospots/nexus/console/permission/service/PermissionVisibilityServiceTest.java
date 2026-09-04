package com.innospots.nexus.console.permission.service;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.core.plugin.contribution.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionVisibilityServiceTest {

    @Test
    void returnsGrantedTreeAndDoesNotExposeOrphanedActions() {
        ConsoleCatalogResourceEntity module = resource("module", CatalogResourceType.MODULE,
                "module:sales", null);
        ConsoleCatalogResourceEntity menu = resource("menu", CatalogResourceType.MENU,
                "menu:sales.orders", "module");
        ConsoleCatalogResourceEntity page = resource("page", CatalogResourceType.PAGE,
                "page:sales.orders", "module");
        ConsoleCatalogResourceEntity action = resource("action", CatalogResourceType.ACTION,
                "action:sales.orders.approve", "page");
        ConsoleCatalogResourceEntity datasource = resource("datasource",
                CatalogResourceType.DATASOURCE,
                "datasource:sales.orders.approve", "page");
        List<ConsoleCatalogResourceEntity> resources = List.of(module, menu, page, action, datasource);
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(resources);
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(
                List.of(grant("menu"), grant("page"), grant("action"), grant("datasource")),
                List.of());

        List<ConsoleCatalogResourceEntity> visible = new PermissionVisibilityService(
                resourceDao, grantDao).visible(
                "1",
                new AuthorizationSubject("user-1", Set.of("role-1"), Set.of(), false));

        assertThat(visible).extracting(ConsoleCatalogResourceEntity::getResourceId)
                .containsExactly("module", "menu", "page", "action", "datasource");
    }

    private static ConsoleCatalogResourceEntity resource(
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
        resource.setStatus("ENABLED");
        return resource;
    }

    private static PermissionGrantEntity grant(String resourceId) {
        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setResourceId(resourceId);
        return grant;
    }
}

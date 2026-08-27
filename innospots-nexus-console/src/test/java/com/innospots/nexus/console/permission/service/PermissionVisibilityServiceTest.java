package com.innospots.nexus.console.permission.service;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.dao.PermissionResourceDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.entity.PermissionResourceEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionResourceType;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionVisibilityServiceTest {

    @Test
    void returnsGrantedTreeAndDoesNotExposeOrphanedActions() {
        PermissionResourceEntity module = resource("module", PermissionResourceType.MODULE,
                "module:sales", null);
        PermissionResourceEntity menu = resource("menu", PermissionResourceType.MENU,
                "menu:sales.orders", "module");
        PermissionResourceEntity page = resource("page", PermissionResourceType.PAGE,
                "page:sales.orders", "module");
        PermissionResourceEntity action = resource("action", PermissionResourceType.ACTION,
                "action:sales.orders.approve", "page");
        PermissionResourceEntity datasource = resource("datasource",
                PermissionResourceType.DATASOURCE,
                "datasource:sales.orders.approve", "page");
        List<PermissionResourceEntity> resources = List.of(module, menu, page, action, datasource);
        PermissionResourceDao resourceDao = mock(PermissionResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(resources);
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(
                List.of(grant("menu"), grant("page"), grant("action"), grant("datasource")),
                List.of());

        List<PermissionResourceEntity> visible = new PermissionVisibilityService(
                resourceDao, grantDao).visible(
                "1",
                new AuthorizationSubject("user-1", Set.of("role-1"), Set.of(), false));

        assertThat(visible).extracting(PermissionResourceEntity::getResourceId)
                .containsExactly("module", "menu", "page", "action", "datasource");
    }

    private static PermissionResourceEntity resource(
            String id,
            PermissionResourceType type,
            String resourceKey,
            String parentId
    ) {
        PermissionResourceEntity resource = new PermissionResourceEntity();
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

package com.innospots.nexus.console.permission.authorization;

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
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;

class ConsolePagePermissionAuthorizerTest {

    @Test
    void allowsAdministrator() {
        ConsoleCatalogResourceEntity menu = menuRow();
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(menu));

        ConsolePagePermissionAuthorizer authorizer = new ConsolePagePermissionAuthorizer(
                resourceDao, mock(PermissionGrantDao.class));

        AuthorizationDecision decision = authorizer.authorize(new AuthorizationRequest(
                "ws-1",
                "GET",
                "/api/example",
                "demo-main",
                new AuthorizationSubject("u1", java.util.Set.of(), java.util.Set.of(), true)));

        assertThat(decision.allowed()).isTrue();
    }

    @Test
    void allowsSubjectWithViewGrant() {
        ConsoleCatalogResourceEntity menu = menuRow();
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(menu));
        PermissionGrantDao grantDao = grantDaoForViewGrant("demo-main:demo.menu", true);

        ConsolePagePermissionAuthorizer authorizer = new ConsolePagePermissionAuthorizer(
                resourceDao, grantDao);

        AuthorizationDecision decision = authorizer.authorize(new AuthorizationRequest(
                "ws-1",
                "GET",
                "/api/example",
                "demo-main",
                new AuthorizationSubject("u1", java.util.Set.of("role-1"), java.util.Set.of(), false)));

        assertThat(decision.allowed()).isTrue();
    }

    @Test
    void deniesSubjectWithOnlyOperationGrant() {
        ConsoleCatalogResourceEntity menu = menuRow();
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(menu));
        PermissionGrantDao grantDao = grantDaoForViewGrant("demo-main:demo.menu", false);

        ConsolePagePermissionAuthorizer authorizer = new ConsolePagePermissionAuthorizer(
                resourceDao, grantDao);

        AuthorizationDecision decision = authorizer.authorize(new AuthorizationRequest(
                "ws-1",
                "GET",
                "/api/example",
                "demo-main",
                new AuthorizationSubject("u1", java.util.Set.of("role-1"), java.util.Set.of(), false)));

        assertThat(decision.allowed()).isFalse();
    }

    private static ConsoleCatalogResourceEntity menuRow() {
        ConsoleCatalogResourceEntity menu = new ConsoleCatalogResourceEntity();
        menu.setResourceId("menu-1");
        menu.setResourceType(CatalogResourceType.MENU.name());
        menu.setDomainKey("demo");
        menu.setModuleKey("menu");
        menu.setEntryPageKey("demo-main");
        menu.setStatus(BasicStatus.ENABLED.name());
        return menu;
    }

    private static PermissionGrantEntity grant(String grantKey) {
        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setGrantKey(grantKey);
        return grant;
    }

    private static PermissionGrantDao grantDaoForViewGrant(String viewGrantKey, boolean granted) {
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any()))
                .thenReturn(granted ? List.of(grant(viewGrantKey)) : List.of());
        return grantDao;
    }
}

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

    private static ConsoleCatalogResourceEntity menuRow() {
        ConsoleCatalogResourceEntity menu = new ConsoleCatalogResourceEntity();
        menu.setResourceId("menu-1");
        menu.setResourceType(CatalogResourceType.MENU.name());
        menu.setEntryPageKey("demo-main");
        menu.setStatus(BasicStatus.ENABLED.name());
        return menu;
    }
}

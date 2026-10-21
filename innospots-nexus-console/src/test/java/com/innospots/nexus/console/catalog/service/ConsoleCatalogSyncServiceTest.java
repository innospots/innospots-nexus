package com.innospots.nexus.console.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;

class ConsoleCatalogSyncServiceTest {

    @Test
    void syncCreatesModuleAndMenuRows() {
        ConsoleContributeNode menu = new ConsoleContributeNode(
                null,
                I18nObject.of(Map.of("en", "Menu")),
                I18nObject.of(Map.of()),
                0,
                "demo-main",
                null,
                List.of(),
                List.of(),
                List.of());
        ConsoleContributeNode module = new ConsoleContributeNode(
                "demo",
                I18nObject.of(Map.of("en", "Demo")),
                I18nObject.of(Map.of()),
                0,
                null,
                null,
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Views")))),
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Ops")))),
                List.of(menu));
        ConsolePluginContribution contribution = new ConsolePluginContribution(
                "nexus",
                I18nObject.of(Map.of()),
                I18nObject.of(Map.of()),
                0,
                List.of(module));

        ConsoleContributionCatalog catalog = mock(ConsoleContributionCatalog.class);
        when(catalog.activeContributions()).thenReturn(List.of(
                new ConsoleContributionCatalog.ActiveConsoleContribution(
                        "com.innospots.nexus.test-plugin",
                        contribution,
                        1L)));

        ConsoleCatalogResourceDao dao = mock(ConsoleCatalogResourceDao.class);
        when(dao.selectList(any())).thenReturn(List.of());
        when(dao.insert(isA(ConsoleCatalogResourceEntity.class))).thenAnswer(invocation -> {
            ConsoleCatalogResourceEntity entity = invocation.getArgument(0);
            entity.setResourceId("id-" + entity.getResourceKey());
            return 1;
        });

        ConsoleCatalogSyncService service = new ConsoleCatalogSyncService(dao, catalog);
        var result = service.sync();

        assertThat(result.createdResources()).isEqualTo(2);
    }

    @Test
    void catalogResourceTypeOnlyModuleAndMenu() {
        assertThat(CatalogResourceType.values()).containsExactly(
                CatalogResourceType.MODULE, CatalogResourceType.MENU);
    }
}

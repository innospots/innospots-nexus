package com.innospots.nexus.console.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;

class ConsoleCatalogSyncServiceTest {

    private static final String PLUGIN_ID = "com.innospots.nexus.test-plugin";

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
    void syncCreatesLeafModuleWithoutMenuRow() {
        ConsoleContributeNode module = ConsoleContributeNode.singlePageModule(
                "demo",
                7,
                I18nObject.of(Map.of("zh-CN", "演示", "en", "Demo")),
                I18nObject.of(Map.of("zh-CN", "演示页面", "en", "Demo page")),
                "demo-main",
                "home");
        ConsoleCatalogResourceDao dao = mockDao(List.of());

        var result = new ConsoleCatalogSyncService(dao, catalog(module)).sync();

        assertThat(result.createdResources()).isEqualTo(1);
        ArgumentCaptor<ConsoleCatalogResourceEntity> inserted =
                ArgumentCaptor.forClass(ConsoleCatalogResourceEntity.class);
        verify(dao).insert(inserted.capture());

        ConsoleCatalogResourceEntity row = inserted.getValue();
        assertThat(row.getResourceType()).isEqualTo(CatalogResourceType.MODULE.name());
        assertThat(row.getResourceKey()).isEqualTo("module:demo");
        assertThat(row.getParentResourceId()).isNull();
        assertThat(row.getEntryPageKey()).isEqualTo("demo-main");
        assertThat(row.getIcon()).isEqualTo("home");
        assertThat(row.getViewGroups()).contains("页面访问");
        assertThat(row.getOperationGroups()).contains("操作");
    }

    @Test
    void syncWritesModuleAndMenuFieldsForDatabaseIndex() {
        ConsoleContributeNode module = ConsoleContributeNode.module(
                "demo",
                7,
                I18nObject.of(Map.of("zh-CN", "演示", "en", "Demo")),
                I18nObject.of(Map.of("zh-CN", "演示模块", "en", "Demo module")),
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Views")))),
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Ops")))),
                List.of(ConsoleContributeNode.menu(
                        I18nObject.of(Map.of("zh-CN", "演示页", "en", "Demo page")),
                        3,
                        "demo-page",
                        "home")));
        ConsoleCatalogResourceDao dao = mockDao(List.of());

        var result = new ConsoleCatalogSyncService(dao, catalog(module)).sync();

        assertThat(result.createdResources()).isEqualTo(2);
        assertThat(result.updatedResources()).isZero();
        assertThat(result.disabledResources()).isZero();

        ArgumentCaptor<ConsoleCatalogResourceEntity> inserted =
                ArgumentCaptor.forClass(ConsoleCatalogResourceEntity.class);
        verify(dao, times(2)).insert(inserted.capture());
        ConsoleCatalogResourceEntity moduleRow = rowOfType(
                inserted.getAllValues(), CatalogResourceType.MODULE);
        ConsoleCatalogResourceEntity menuRow = rowOfType(
                inserted.getAllValues(), CatalogResourceType.MENU);

        assertThat(moduleRow.getResourceId()).isEqualTo("id-module:demo");
        assertThat(moduleRow.getOwnerPluginId()).isEqualTo(PLUGIN_ID);
        assertThat(moduleRow.getDomainKey()).isEqualTo("nexus");
        assertThat(moduleRow.getModuleKey()).isEqualTo("demo");
        assertThat(moduleRow.getResourceKey()).isEqualTo("module:demo");
        assertThat(moduleRow.getParentResourceId()).isNull();
        assertThat(moduleRow.getEntryPageKey()).isNull();
        assertThat(moduleRow.getSortOrder()).isEqualTo(7);
        assertThat(moduleRow.getDisplayName()).isNotBlank();
        assertThat(moduleRow.getDescription()).isNotBlank();
        assertThat(moduleRow.getViewGroups()).as("MODULE rows do not store groups").isNull();
        assertThat(moduleRow.getOperationGroups()).as("MODULE rows do not store groups").isNull();
        assertThat(moduleRow.getStatus()).isEqualTo(BasicStatus.ENABLED.name());
        assertThat(moduleRow.getSecurityRealm()).isEqualTo("TENANT");

        assertThat(menuRow.getResourceId()).isEqualTo("id-menu:demo.demo-page");
        assertThat(menuRow.getOwnerPluginId()).isEqualTo(PLUGIN_ID);
        assertThat(menuRow.getDomainKey()).isEqualTo("nexus");
        assertThat(menuRow.getModuleKey()).isEqualTo("demo");
        assertThat(menuRow.getResourceType()).isEqualTo(CatalogResourceType.MENU.name());
        assertThat(menuRow.getResourceKey()).isEqualTo("menu:demo.demo-page");
        assertThat(menuRow.getParentResourceId()).isEqualTo("id-module:demo");
        assertThat(menuRow.getEntryPageKey()).isEqualTo("demo-page");
        assertThat(menuRow.getIcon()).isEqualTo("home");
        assertThat(menuRow.getSortOrder()).isEqualTo(3);
        assertThat(menuRow.getViewGroups()).contains("Views");
        assertThat(menuRow.getOperationGroups()).contains("Ops");
        assertThat(menuRow.getStatus()).isEqualTo(BasicStatus.ENABLED.name());
        assertThat(menuRow.getSecurityRealm()).isEqualTo("TENANT");
    }

    @Test
    void syncUpdatesChangedRowsAndDisablesRowsNoLongerContributed() {
        ConsoleContributeNode module = ConsoleContributeNode.module(
                "demo",
                1,
                I18nObject.of(Map.of("en", "Demo")),
                I18nObject.of(Map.of("en", "Demo module")),
                List.of(),
                List.of(),
                List.of(ConsoleContributeNode.menu(
                        I18nObject.of(Map.of("en", "Demo page")), 0, "demo-page", "home")));
        ConsoleCatalogResourceEntity changed = existingModule("existing-demo", "module:demo");
        changed.setDisplayName("stale");
        changed.setSortOrder(99);
        ConsoleCatalogResourceEntity missing = existingModule("existing-gone", "module:gone");
        ConsoleCatalogResourceDao dao = mockDao(List.of(changed, missing));

        var result = new ConsoleCatalogSyncService(dao, catalog(module)).sync();

        assertThat(result.createdResources()).as("new menu row").isEqualTo(1);
        assertThat(result.updatedResources()).as("changed module row").isEqualTo(1);
        assertThat(result.disabledResources()).as("module removed from contributions").isEqualTo(1);

        assertThat(changed.getDisplayName()).isNotEqualTo("stale");
        assertThat(changed.getSortOrder()).isEqualTo(1);
        assertThat(changed.getStatus()).isEqualTo(BasicStatus.ENABLED.name());
        assertThat(missing.getStatus()).isEqualTo(BasicStatus.DISABLED.name());

        verify(dao, times(2)).updateById(any(ConsoleCatalogResourceEntity.class));
    }

    @Test
    void catalogResourceTypeOnlyModuleAndMenu() {
        assertThat(CatalogResourceType.values()).containsExactly(
                CatalogResourceType.MODULE, CatalogResourceType.MENU);
    }

    private static ConsoleContributionCatalog catalog(ConsoleContributeNode... modules) {
        ConsolePluginContribution contribution = new ConsolePluginContribution(
                "nexus",
                I18nObject.of(Map.of()),
                I18nObject.of(Map.of()),
                0,
                List.of(modules));
        ConsoleContributionCatalog catalog = mock(ConsoleContributionCatalog.class);
        when(catalog.activeContributions()).thenReturn(List.of(
                new ConsoleContributionCatalog.ActiveConsoleContribution(
                        PLUGIN_ID,
                        contribution,
                        1L)));
        return catalog;
    }

    private static ConsoleCatalogResourceDao mockDao(List<ConsoleCatalogResourceEntity> existing) {
        ConsoleCatalogResourceDao dao = mock(ConsoleCatalogResourceDao.class);
        when(dao.selectList(any())).thenReturn(existing);
        when(dao.insert(isA(ConsoleCatalogResourceEntity.class))).thenAnswer(invocation -> {
            ConsoleCatalogResourceEntity entity = invocation.getArgument(0);
            entity.setResourceId("id-" + entity.getResourceKey());
            return 1;
        });
        when(dao.updateById(any(ConsoleCatalogResourceEntity.class))).thenReturn(1);
        return dao;
    }

    private static ConsoleCatalogResourceEntity existingModule(String resourceId, String resourceKey) {
        ConsoleCatalogResourceEntity entity = new ConsoleCatalogResourceEntity();
        entity.setResourceId(resourceId);
        entity.setOwnerPluginId(PLUGIN_ID);
        entity.setDomainKey("nexus");
        entity.setModuleKey(resourceKey.substring("module:".length()));
        entity.setResourceType(CatalogResourceType.MODULE.name());
        entity.setResourceKey(resourceKey);
        entity.setSortOrder(0);
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setSecurityRealm("TENANT");
        return entity;
    }

    private static ConsoleCatalogResourceEntity rowOfType(
            List<ConsoleCatalogResourceEntity> rows,
            CatalogResourceType type) {
        return rows.stream()
                .filter(row -> type.name().equals(row.getResourceType()))
                .findFirst()
                .orElseThrow();
    }
}

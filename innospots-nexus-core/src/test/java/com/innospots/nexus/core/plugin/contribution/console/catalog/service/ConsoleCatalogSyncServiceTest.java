package com.innospots.nexus.core.plugin.contribution.console.catalog.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.ui.spec.PageInfo;
import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.action.ActionType;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import com.innospots.nexus.base.ui.spec.datasource.UiDatasource;
import com.innospots.nexus.base.ui.spec.loader.UiSpecLoader;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.config.PluginConfig;
import com.innospots.nexus.core.plugin.contribution.PluginContributionContext;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleModuleDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.console.MenuDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ReservedPluginResourceCatalog;
import com.innospots.nexus.core.plugin.contribution.console.UiSpecPageDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.model.CatalogSyncResult;
import com.innospots.nexus.core.plugin.lifecycle.PluginAvailability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsoleCatalogSyncServiceTest {

    @AfterEach
    void clearProjectContext() {
        TLC.clear();
    }

    @Test
    void buildsCatalogueFromActiveExtensionAndUiSpecAndIsIdempotent() {
        ConsoleContributionCatalog registry = activeCatalog();

        UiSpec spec = UiSpec.page(PageInfo.of("orders", I18nObject.of("en", "Orders")))
                .datasource("list", UiDatasource.get("/api/orders"))
                .datasource("approve", UiDatasource.post("/api/orders/{orderId}/approve"))
                .actionDefinition(UiAction.of("approve", ActionType.API)
                        .datasourceKey("approve"));
        UiSpecLoader loader = (moduleKey, pageKey) -> spec;
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        List<ConsoleCatalogResourceEntity> inserted = new ArrayList<>();
        doAnswer(invocation -> {
            ConsoleCatalogResourceEntity entity = invocation.getArgument(0);
            entity.setResourceId("resource-" + inserted.size());
            inserted.add(entity);
            return 1;
        }).when(resourceDao).insert(any(ConsoleCatalogResourceEntity.class));
        when(resourceDao.selectList(any())).thenReturn(List.of());

        ConsoleCatalogSyncService service = new ConsoleCatalogSyncService(
                resourceDao, registry, loader);

        CatalogSyncResult first = service.sync();

        assertThat(first).isEqualTo(new CatalogSyncResult(6, 0, 0));
        assertThat(inserted).extracting(ConsoleCatalogResourceEntity::getResourceType)
                .containsExactlyInAnyOrder(
                        CatalogResourceType.MODULE.name(),
                        CatalogResourceType.MENU.name(),
                        CatalogResourceType.PAGE.name(),
                        CatalogResourceType.ACTION.name(),
                        CatalogResourceType.DATASOURCE.name(),
                        CatalogResourceType.DATASOURCE.name());
        assertThat(inserted).filteredOn(value ->
                        CatalogResourceType.DATASOURCE.name().equals(value.getResourceType()))
                .extracting(ConsoleCatalogResourceEntity::getRequestMethod)
                .containsExactlyInAnyOrder("GET", "POST");

        when(resourceDao.selectList(any())).thenReturn(List.copyOf(inserted));
        CatalogSyncResult second = service.sync();

        assertThat(second).isEqualTo(new CatalogSyncResult(0, 0, 0));
    }

    @Test
    void updatesChangedResourceMetadata() {
        ConsoleContributionCatalog registry = activeCatalog();

        UiSpec firstSpec = UiSpec.page(PageInfo.of("orders", I18nObject.of("en", "Orders")))
                .datasource("list", UiDatasource.get("/api/orders"));
        UiSpec secondSpec = UiSpec.page(PageInfo.of("orders", I18nObject.of("en", "Orders")))
                .datasource("list", UiDatasource.post("/api/orders/search"));
        UiSpec[] current = {firstSpec};
        UiSpecLoader loader = (moduleKey, pageKey) -> current[0];
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        List<ConsoleCatalogResourceEntity> stored = new ArrayList<>();
        doAnswer(invocation -> {
            ConsoleCatalogResourceEntity entity = invocation.getArgument(0);
            entity.setResourceId("resource-" + stored.size());
            stored.add(entity);
            return 1;
        }).when(resourceDao).insert(any(ConsoleCatalogResourceEntity.class));
        when(resourceDao.selectList(any())).thenAnswer(invocation -> List.copyOf(stored));

        ConsoleCatalogSyncService service = new ConsoleCatalogSyncService(
                resourceDao, registry, loader);
        service.sync();
        current[0] = secondSpec;

        CatalogSyncResult result = service.sync();

        assertThat(result).isEqualTo(new CatalogSyncResult(0, 1, 0));
        assertThat(stored).filteredOn(value ->
                        CatalogResourceType.DATASOURCE.name().equals(value.getResourceType()))
                .singleElement()
                .satisfies(value -> {
                    assertThat(value.getRequestMethod()).isEqualTo("POST");
                    assertThat(value.getRequestUrl()).isEqualTo("/api/orders/search");
                });
        verify(resourceDao).updateById(any(ConsoleCatalogResourceEntity.class));
    }

    private static ConsoleContributionCatalog activeCatalog() {
        ConsoleContributionCatalog catalog = new ConsoleContributionCatalog();
        ConsolePluginContributionHandler handler = new ConsolePluginContributionHandler(
                catalog, new ReservedPluginResourceCatalog(List.of()));
        PluginAvailability availability = new PluginAvailability();
        var prepared = handler.prepare(
                new PluginContributionContext(
                        new ProviderRef("com.example.sales", "contribution-console-1"),
                        emptyConfig(), availability),
                contribution());
        prepared.stage();
        prepared.commit();
        availability.activate();
        return catalog;
    }

    private static ConsolePluginContribution contribution() {
        return new ConsolePluginContribution(List.of(new ConsoleModuleDeclaration(
                        "sales",
                        I18nObject.of("en", "Sales"),
                        I18nObject.of("en", "Sales module"),
                        List.of(new UiSpecPageDeclaration("orders", "/orders", List.of())),
                        List.of(MenuDeclaration.page(
                                "orders",
                                I18nObject.of("en", "Orders"),
                                null,
                                0,
                                "orders")))));
    }

    private static PluginConfig emptyConfig() {
        return new PluginConfig() {
            @Override public java.util.Optional<String> get(String key) { return java.util.Optional.empty(); }
            @Override public String require(String key) { throw new IllegalArgumentException(key); }
            @Override public int getInt(String key, int defaultValue) { return defaultValue; }
            @Override public long getLong(String key, long defaultValue) { return defaultValue; }
            @Override public boolean getBoolean(String key, boolean defaultValue) { return defaultValue; }
            @Override public java.time.Duration getDuration(String key, java.time.Duration defaultValue) { return defaultValue; }
            @Override public com.innospots.nexus.core.plugin.config.SecretValue requireSecret(String key) { throw new IllegalArgumentException(key); }
        };
    }
}

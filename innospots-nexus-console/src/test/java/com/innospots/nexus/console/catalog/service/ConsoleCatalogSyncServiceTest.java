package com.innospots.nexus.console.catalog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.HttpRequest;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageMeta;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionOrList;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource.HttpDataSource;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader.PageDslLoader;
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
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.model.CatalogSyncResult;
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
    void buildsCatalogueFromActiveExtensionAndPageDslAndIsIdempotent() {
        ConsoleContributionCatalog registry = activeCatalog();

        PageDsl document = ordersPage(
                httpDataSource("GET", "/api/orders"),
                httpDataSource("POST", "/api/orders/{orderId}/approve"));
        ActionConfig approve = new ActionConfig();
        approve.setAction("reload");
        approve.setParams(Map.of("dataSource", "approve"));
        document.getActions().put("approve", new ActionOrList(List.of(approve)));

        PageDslLoader loader = (moduleKey, pageKey) -> document;
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

        PageDsl firstDocument = ordersPage(httpDataSource("GET", "/api/orders"));
        PageDsl secondDocument = ordersPage(httpDataSource("POST", "/api/orders/search"));
        PageDsl[] current = {firstDocument};
        PageDslLoader loader = (moduleKey, pageKey) -> current[0];
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
        current[0] = secondDocument;

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

    private static PageDsl ordersPage(HttpDataSource list, HttpDataSource approve) {
        PageDsl document = PageDsl.of(PageMeta.of("orders", "Orders"));
        document.getDataSources().put("list", list);
        document.getDataSources().put("approve", approve);
        return document;
    }

    private static PageDsl ordersPage(HttpDataSource list) {
        PageDsl document = PageDsl.of(PageMeta.of("orders", "Orders"));
        document.getDataSources().put("list", list);
        return document;
    }

    private static HttpDataSource httpDataSource(String method, String url) {
        HttpDataSource dataSource = new HttpDataSource();
        HttpRequest request = new HttpRequest();
        request.setMethod(method);
        request.setUrl(url);
        dataSource.setRequest(request);
        return dataSource;
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
                        com.innospots.nexus.base.i18n.I18nObject.of("en", "Sales"),
                        com.innospots.nexus.base.i18n.I18nObject.of("en", "Sales module"),
                        List.of(new UiSpecPageDeclaration("orders", "/orders", List.of())),
                        List.of(MenuDeclaration.page(
                                "orders",
                                com.innospots.nexus.base.i18n.I18nObject.of("en", "Orders"),
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

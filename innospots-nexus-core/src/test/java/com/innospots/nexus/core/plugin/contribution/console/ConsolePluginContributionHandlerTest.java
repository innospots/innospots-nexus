package com.innospots.nexus.core.plugin.contribution.console;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.contribution.PluginContributionContext;
import com.innospots.nexus.core.plugin.contribution.PluginContributionEntry;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.lifecycle.PluginAvailability;
import com.innospots.nexus.core.plugin.config.ConfigDefinition;
import com.innospots.nexus.core.plugin.config.PluginConfig;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Console Contribution 的声明校验、预提交和可用性门控测试。 */
class ConsolePluginContributionHandlerTest {

    @Test
    void publishesResourcesOnlyAfterSharedAvailabilityIsActive() {
        ConsolePluginContribution contribution = new ConsolePluginContribution(List.of(
                new ConsoleModuleDeclaration(
                        "sales",
                        I18nObject.of("en", "Sales"),
                        I18nObject.of("en", "Sales administration"),
                        List.of(new UiSpecPageDeclaration("orders", "/sales/orders", List.of())),
                        List.of(MenuDeclaration.page(
                                "orders", I18nObject.of("en", "Orders"), "orders", 10, "orders")))));
        PluginAvailability availability = new PluginAvailability();
        ConsoleContributionCatalog catalog = new ConsoleContributionCatalog();
        ConsolePluginContributionHandler handler = new ConsolePluginContributionHandler(
                catalog, new ReservedPluginResourceCatalog(List.of()));

        handler.validate(PluginCatalog.of(List.of()), List.of());
        var prepared = handler.prepare(
                new PluginContributionContext(
                        new ProviderRef("com.example.sales", "contribution-console-1"),
                        emptyConfig(), availability),
                contribution);
        prepared.stage();
        prepared.commit();

        assertThat(catalog.activeContributions()).isEmpty();
        availability.activate();
        assertThat(catalog.activeContributions()).hasSize(1);

        prepared.rollback();
        prepared.close();
        assertThat(catalog.activeContributions()).isEmpty();
    }

    @Test
    void rejectsMenuPageWithRequiredPathVariables() {
        ConsolePluginContribution contribution = new ConsolePluginContribution(List.of(
                new ConsoleModuleDeclaration(
                        "sales",
                        I18nObject.of("en", "Sales"),
                        I18nObject.of("en", "Sales administration"),
                        List.of(new UiSpecPageDeclaration("order", "/sales/orders/{orderId}", List.of())),
                        List.of(MenuDeclaration.page(
                                "order", I18nObject.of("en", "Order"), "order", 10, "order")))));
        ConsolePluginContributionHandler handler = new ConsolePluginContributionHandler(
                new ConsoleContributionCatalog(), new ReservedPluginResourceCatalog(List.of()));

        assertThatThrownBy(() -> handler.validate(
                PluginCatalog.of(List.of()),
                List.of(new PluginContributionEntry<>(
                        new ProviderRef("com.example.sales", "contribution-console-1"), contribution))))
                .hasMessageContaining("static menu");
    }

    @Test
    void rejectsSameNormalizedRouteAcrossDifferentModules() {
        ConsolePluginContribution contribution = new ConsolePluginContribution(List.of(
                module("sales", "orders", "/settings/{id}"),
                module("support", "tickets", "/settings/{ticketId}")));
        ConsolePluginContributionHandler handler = new ConsolePluginContributionHandler(
                new ConsoleContributionCatalog(), new ReservedPluginResourceCatalog(List.of()));

        assertThatThrownBy(() -> handler.validate(
                PluginCatalog.of(List.of()),
                List.of(new PluginContributionEntry<>(
                        new ProviderRef("com.example.console", "contribution-console-1"), contribution))))
                .hasMessageContaining("pagePath conflicts");
    }

    @Test
    void decodesOptionalDescriptionAndRequiresNonEmptyModulePages() {
        ConsolePluginContribution contribution = new ConsolePluginContributionDecoder().decode(Map.of(
                "type", "console",
                "majorVersion", 1,
                "modules", List.of(Map.of(
                        "moduleKey", "sales",
                        "displayName", Map.of("en", "Sales"),
                        "pages", List.of(Map.of("pageKey", "home", "pagePath", "/sales"))))));

        assertThat(contribution.modules()).singleElement()
                .extracting(ConsoleModuleDeclaration::description)
                .extracting(I18nObject::isEmpty)
                .isEqualTo(true);

        assertThatThrownBy(() -> new ConsolePluginContributionDecoder().decode(Map.of(
                "type", "console", "majorVersion", 1, "modules", List.of(Map.of(
                        "moduleKey", "sales",
                        "displayName", Map.of("en", "Sales"),
                        "pages", List.of())))))
                .hasMessageContaining("pages");
    }

    @Test
    void rejectsEncodedPathTraversal() {
        assertThatThrownBy(() -> new UiSpecPageDeclaration("home", "/sales/%2e%2e/admin", List.of()))
                .hasMessageContaining("traversal");
    }

    @Test
    void rejectsUnknownNestedFieldsAndNonIntegralOrderIndex() {
        ConsolePluginContributionDecoder decoder = new ConsolePluginContributionDecoder();
        Map<String, Object> unknownPage = Map.of(
                "pageKey", "home",
                "pagePath", "/sales",
                "unknown", true);
        Map<String, Object> unknownModule = Map.of(
                "moduleKey", "sales",
                "displayName", Map.of("en", "Sales"),
                "pages", List.of(unknownPage));
        Map<String, Object> unknownContribution = Map.of(
                "type", "console",
                "majorVersion", 1,
                "modules", List.of(unknownModule));

        assertThatThrownBy(() -> decoder.decode(unknownContribution))
                .hasMessageContaining("unknown");

        Map<String, Object> fractionalOrderMenu = Map.of(
                "menuKey", "home",
                "title", Map.of("en", "Home"),
                "orderIndex", 1.5,
                "pageKey", "home");
        Map<String, Object> fractionalOrderModule = Map.of(
                "moduleKey", "sales",
                "displayName", Map.of("en", "Sales"),
                "pages", List.of(Map.of("pageKey", "home", "pagePath", "/sales")),
                "menuTree", List.of(fractionalOrderMenu));
        Map<String, Object> fractionalOrderContribution = Map.of(
                "type", "console",
                "majorVersion", 1,
                "modules", List.of(fractionalOrderModule));

        assertThatThrownBy(() -> decoder.decode(fractionalOrderContribution))
                .hasMessageContaining("orderIndex");
    }

    @Test
    void rejectsInvalidProgrammaticLocalizedText() {
        assertThatThrownBy(() -> new ConsoleModuleDeclaration(
                "sales",
                I18nObject.of(Map.of("en", " ")),
                I18nObject.of("en", "Sales"),
                List.of(new UiSpecPageDeclaration("home", "/sales", List.of())),
                List.of()))
                .hasMessageContaining("displayName");
    }

    @Test
    void snapshotsDirectoryMenusWithoutNullValues() {
        ConsolePluginContribution contribution = new ConsolePluginContribution(List.of(
                new ConsoleModuleDeclaration(
                        "sales",
                        I18nObject.of("en", "Sales"),
                        null,
                        List.of(new UiSpecPageDeclaration("home", "/sales", List.of())),
                        List.of(MenuDeclaration.directory(
                                "sales", I18nObject.of("en", "Sales"), null, 0,
                                List.of(MenuDeclaration.page(
                                        "home", I18nObject.of("en", "Home"), null, 0, "home")))))));

        Map<String, Object> snapshot = new ConsolePluginContributionSnapshotter().snapshot(contribution);

        assertThat(snapshot).containsKey("modules");
    }

    private static ConsoleModuleDeclaration module(String moduleKey, String pageKey, String pagePath) {
        return new ConsoleModuleDeclaration(
                moduleKey,
                I18nObject.of("en", moduleKey),
                I18nObject.of("en", moduleKey + " administration"),
                List.of(new UiSpecPageDeclaration(pageKey, pagePath, List.of())),
                List.of());
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

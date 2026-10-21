package com.innospots.nexus.console.entry;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsoleModuleEntrySupportTest {

    @Test
    void buildsSingleMenuForModuleWithAdditionalPages() {
        ConsoleModuleDescriptor module = ConsoleModuleDescriptor.builtin(
                "com.example.multi-page",
                "sales",
                "reports",
                "reports-home",
                "chart",
                10,
                I18nObject.of("en", "Reports"),
                I18nObject.of("en", "Reports module"),
                I18nObject.of("en", "Reports Home"),
                List.of("reports-detail"),
                List.of());

        ConsoleContributeNode declared = soleModule(ConsoleModuleEntrySupport.definition(module));

        assertThat(module.allPageKeys()).containsExactly("reports-home", "reports-detail");
        assertThat(declared.children()).hasSize(1);
        assertThat(declared.children().getFirst().entryPageKey()).isEqualTo("reports-home");
    }

    @Test
    void buildsMultipleMenuEntriesForSingleModule() {
        ConsoleModuleDescriptor module = ConsoleModuleDescriptor.builtin(
                "com.example.multi-menu",
                "nexus",
                "settings",
                "settings-main",
                null,
                0,
                I18nObject.of("en", "Settings"),
                I18nObject.of("en", "Settings module"),
                I18nObject.of("en", "Unused single menu title"),
                List.of("settings-advanced"),
                List.of(
                        new ConsoleMenuItemDescriptor(
                                "settings-main",
                                I18nObject.of("en", "General"),
                                "setting",
                                10,
                                "settings-main"),
                        new ConsoleMenuItemDescriptor(
                                "settings-advanced-menu",
                                I18nObject.of("en", "Advanced"),
                                "tool",
                                20,
                                "settings-advanced")));

        ConsoleContributeNode declared = soleModule(ConsoleModuleEntrySupport.definition(module));

        assertThat(declared.children()).hasSize(2);
        assertThat(declared.children().get(0).entryPageKey()).isEqualTo("settings-main");
        assertThat(declared.children().get(1).entryPageKey()).isEqualTo("settings-advanced");
    }

    @Test
    void buildsMultipleModulesInOnePlugin() {
        ConsoleModuleDescriptor alpha = ConsoleModuleDescriptor.builtin(
                "com.example.bundle",
                "nexus",
                "alpha",
                "alpha-home",
                "a",
                10,
                I18nObject.of("en", "Alpha"),
                I18nObject.of("en", "Alpha module"),
                I18nObject.of("en", "Alpha"));
        ConsoleModuleDescriptor beta = ConsoleModuleDescriptor.builtin(
                "com.example.bundle",
                "sales",
                "beta",
                "beta-home",
                "b",
                20,
                I18nObject.of("en", "Beta"),
                I18nObject.of("en", "Beta module"),
                I18nObject.of("en", "Beta"));

        ConsoleEntryPluginDescriptor entry = ConsoleEntryPluginDescriptor.of(
                "com.example.bundle",
                I18nObject.of("en", "Bundle"),
                I18nObject.of("en", "Two modules"),
                List.of(alpha, beta));

        ConsolePluginContribution contribution = definitionContribution(
                ConsoleModuleEntrySupport.definition(entry));

        assertThat(contribution.modules()).hasSize(2);
        assertThat(contribution.modules().get(0).moduleKey()).isEqualTo("alpha");
        assertThat(contribution.modules().get(1).moduleKey()).isEqualTo("beta");
        assertThat(BuiltinConsoleEntryPlugins.tagsFor(entry).get(BuiltinConsoleEntryPlugins.TAG_MODULE))
                .contains("alpha,beta");
        assertThat(BuiltinConsoleEntryPlugins.tagsFor(entry).get(BuiltinConsoleEntryPlugins.TAG_DOMAIN))
                .contains("nexus,sales");
    }

    @Test
    void contributionUsesCustomDomainKey() {
        ConsoleModuleDescriptor module = ConsoleModuleDescriptor.builtin(
                "com.example.custom-domain",
                "sales",
                "orders",
                "order-list",
                "list",
                0,
                I18nObject.of("en", "Orders"),
                I18nObject.of("en", "Orders"),
                I18nObject.of("en", "Orders"));

        ConsolePluginContribution contribution = definitionContribution(
                ConsoleModuleEntrySupport.definition(module));

        assertThat(contribution.domainKey()).isEqualTo("sales");
        assertThat(contribution.modules().getFirst().children().getFirst().entryPageKey()).isEqualTo("order-list");
        assertThat(ConsoleModuleDescriptor.pagePath("sales", "orders", "order-list"))
                .isEqualTo("/page/sales/orders/order-list");
    }

    @Test
    void rejectsMismatchedModulePluginId() {
        ConsoleModuleDescriptor otherPlugin = ConsoleModuleDescriptor.builtin(
                "com.example.other",
                "nexus",
                "lonely",
                "lonely-home",
                "x",
                0,
                I18nObject.of("en", "Lonely"),
                I18nObject.of("en", "Lonely"),
                I18nObject.of("en", "Lonely"));

        assertThatThrownBy(() -> ConsoleEntryPluginDescriptor.of(
                "com.example.bundle",
                I18nObject.of("en", "Bundle"),
                I18nObject.of("en", "Bundle"),
                List.of(otherPlugin)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pluginId");
    }

    private static ConsoleContributeNode soleModule(PluginDefinition definition) {
        return definitionContribution(definition).modules().getFirst();
    }

    private static ConsolePluginContribution definitionContribution(PluginDefinition definition) {
        return definition.contributions().stream()
                .filter(ConsolePluginContribution.class::isInstance)
                .map(ConsolePluginContribution.class::cast)
                .findFirst()
                .orElseThrow();
    }
}

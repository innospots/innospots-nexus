package com.innospots.nexus.console.entry;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleModuleDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsoleModuleEntrySupportTest {

    @Test
    void buildsMultiplePagesForSingleModule() {
        ConsoleModuleDescriptor module = ConsoleModuleDescriptor.builtin(
                "com.example.multi-page",
                "reports",
                "chart",
                10,
                I18nObject.of("en", "Reports"),
                I18nObject.of("en", "Reports module"),
                I18nObject.of("en", "Reports Home"),
                List.of("reports-detail"),
                List.of());

        PluginDefinition definition = ConsoleModuleEntrySupport.definition(module);
        ConsoleModuleDeclaration declared = soleModule(definition);

        assertThat(declared.pages()).extracting("pageKey")
                .containsExactly("reports-main", "reports-detail");
        assertThat(declared.menuTree()).hasSize(1);
    }

    @Test
    void buildsMultipleMenuEntriesForSingleModule() {
        ConsoleModuleDescriptor module = ConsoleModuleDescriptor.builtin(
                "com.example.multi-menu",
                "settings",
                null,
                0,
                I18nObject.of("en", "Settings"),
                I18nObject.of("en", "Settings module"),
                I18nObject.of("en", "Unused single menu title"),
                List.of("settings-advanced"),
                List.of(
                        new ConsoleMenuItemDescriptor(
                                "settings-home",
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

        ConsoleModuleDeclaration declared = soleModule(ConsoleModuleEntrySupport.definition(module));

        assertThat(declared.menuTree()).hasSize(2);
        assertThat(declared.menuTree().get(0).pageKey()).isEqualTo("settings-main");
        assertThat(declared.menuTree().get(1).pageKey()).isEqualTo("settings-advanced");
    }

    @Test
    void buildsMultipleModulesInOnePlugin() {
        ConsoleModuleDescriptor alpha = ConsoleModuleDescriptor.builtin(
                "com.example.bundle",
                "alpha",
                "a",
                10,
                I18nObject.of("en", "Alpha"),
                I18nObject.of("en", "Alpha module"),
                I18nObject.of("en", "Alpha"));
        ConsoleModuleDescriptor beta = ConsoleModuleDescriptor.builtin(
                "com.example.bundle",
                "beta",
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
    }

    @Test
    void rejectsMismatchedModulePluginId() {
        ConsoleModuleDescriptor otherPlugin = ConsoleModuleDescriptor.builtin(
                "com.example.other",
                "lonely",
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

    private static ConsoleModuleDeclaration soleModule(PluginDefinition definition) {
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

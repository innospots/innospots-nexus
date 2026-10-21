package com.innospots.nexus.core.plugin.contribution.console;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleContributionsTest {

    @Test
    void buildsSinglePageModule() {
        ConsoleContributeNode module = ConsoleContributeNode.singlePageModule(
                "reports",
                10,
                I18nObject.of("en", "Reports"),
                I18nObject.of("en", "Reports module"),
                "reports-home",
                "chart");

        PluginDefinition definition = ConsoleContributions.entryPlugin(
                "com.example.single-menu",
                "1.0.0",
                ConsoleContributions.discoveryTags("com.example.single-menu", "sales", List.of("reports")),
                I18nObject.of("en", "Reports"),
                I18nObject.of("en", "Reports module"),
                "sales",
                List.of(module));

        ConsoleContributeNode declared = soleModule(definition);

        assertThat(module.children()).hasSize(1);
        assertThat(module.children().getFirst().entryPageKey()).isEqualTo("reports-home");
        assertThat(module.viewGroups()).extracting(ConsolePermissionGroupDeclaration::group)
                .containsExactly(ConsoleContributeNode.DEFAULT_VIEW_GROUP);
        assertThat(module.operationGroups()).extracting(ConsolePermissionGroupDeclaration::group)
                .containsExactly(ConsoleContributeNode.DEFAULT_VIEW_GROUP);
        assertThat(declared.children()).hasSize(1);
        assertThat(declared.children().getFirst().entryPageKey()).isEqualTo("reports-home");
    }

    @Test
    void buildsMultipleMenuChildren() {
        ConsoleContributeNode module = ConsoleContributeNode.module(
                "settings",
                0,
                I18nObject.of("en", "Settings"),
                I18nObject.of("en", "Settings module"),
                null,
                null,
                List.of(
                        ConsoleContributeNode.menu(
                                I18nObject.of("en", "General"),
                                10,
                                "settings-main",
                                "setting"),
                        ConsoleContributeNode.menu(
                                I18nObject.of("en", "Advanced"),
                                20,
                                "settings-advanced",
                                "tool")));

        ConsoleContributeNode declared = soleModule(ConsoleContributions.entryPlugin(
                "com.example.multi-menu",
                "1.0.0",
                ConsoleContributions.discoveryTags("com.example.multi-menu", "nexus", List.of("settings")),
                I18nObject.of("en", "Settings"),
                I18nObject.of("en", "Settings module"),
                "nexus",
                List.of(module)));

        assertThat(declared.children()).hasSize(2);
        assertThat(declared.children().get(0).entryPageKey()).isEqualTo("settings-main");
        assertThat(declared.children().get(1).entryPageKey()).isEqualTo("settings-advanced");
    }

    @Test
    void buildsMultipleModulesInOnePlugin() {
        ConsoleContributeNode alpha = ConsoleContributeNode.singlePageModule(
                "alpha",
                10,
                I18nObject.of("en", "Alpha"),
                I18nObject.of("en", "Alpha module"),
                "alpha-home",
                "a");
        ConsoleContributeNode beta = ConsoleContributeNode.singlePageModule(
                "beta",
                20,
                I18nObject.of("en", "Beta"),
                I18nObject.of("en", "Beta module"),
                "beta-home",
                "b");

        PluginDefinition definition = ConsoleContributions.entryPlugin(
                "com.example.bundle",
                "1.0.0",
                ConsoleContributions.discoveryTags("com.example.bundle", "nexus", List.of("alpha", "beta")),
                I18nObject.of("en", "Bundle"),
                I18nObject.of("en", "Two modules"),
                "nexus",
                List.of(alpha, beta));

        ConsolePluginContribution contribution = definitionContribution(definition);

        assertThat(contribution.modules()).hasSize(2);
        assertThat(contribution.modules().get(0).moduleKey()).isEqualTo("alpha");
        assertThat(contribution.modules().get(1).moduleKey()).isEqualTo("beta");
        assertThat(ConsoleContributions.discoveryTags("com.example.bundle", contribution)
                .get(ConsoleContributions.DISCOVERY_TAG_MODULE))
                .contains("alpha,beta");
        assertThat(ConsoleContributions.discoveryTags("com.example.bundle", contribution)
                .get(ConsoleContributions.DISCOVERY_TAG_DOMAIN))
                .contains("nexus");
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

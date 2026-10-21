package com.innospots.nexus.core.plugin.declaration;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.config.ConfigDefinition;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PluginDefinitionTest {

    @Test
    void buildsDefinitionWithConsoleContribution() {
        PluginDefinition definition = PluginDefinition.builder("com.example.sample")
                .name("Sample")
                .version("1.0.0")
                .contribute(sampleConsoleContribution())
                .build();

        assertThat(definition.contributions()).hasSize(1);
        assertThat(definition.contributions().getFirst()).isInstanceOf(ConsolePluginContribution.class);
        assertThatThrownBy(() -> definition.contributions().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsDuplicateContributionTypes() {
        assertThatThrownBy(() -> PluginDefinition.builder("com.example.sample")
                .name("Sample")
                .version("1.0.0")
                .contribute(sampleConsoleContribution())
                .contribute(sampleConsoleContribution())
                .build())
                .isInstanceOf(NexusException.class);
    }

    @Test
    void copiesTagsAndConfig() {
        PluginDefinition definition = PluginDefinition.builder("com.example.sample")
                .name("Sample")
                .version("1.0.0")
                .tags(Tags.of("env", "test"))
                .config(ConfigDefinition.empty())
                .contribute(sampleConsoleContribution())
                .build();

        assertThat(definition.tags().asMap()).containsEntry("env", "test");
        assertThat(definition.config().items()).isEmpty();
    }

    private static ConsolePluginContribution sampleConsoleContribution() {
        ConsoleContributeNode menu = new ConsoleContributeNode(
                null,
                I18nObject.of(Map.of("en", "Menu")),
                I18nObject.of(Map.of()),
                0,
                "sample-page",
                null,
                List.of(),
                List.of(),
                List.of());
        ConsoleContributeNode module = new ConsoleContributeNode(
                "role",
                I18nObject.of(Map.of("en", "Role")),
                I18nObject.of(Map.of()),
                0,
                null,
                null,
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Views")))),
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Ops")))),
                List.of(menu));
        return new ConsolePluginContribution(
                "nexus",
                I18nObject.of(Map.of("en", "Nexus")),
                I18nObject.of(Map.of()),
                0,
                List.of(module));
    }
}

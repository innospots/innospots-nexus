package com.innospots.nexus.console.entry;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.plugin.entry.PluginManagementEntryPlugin;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributions;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class BuiltinConsoleEntryPluginsTest {

    @Test
    void pluginVersionIsNeverBlank() {
        assertThat(BuiltinConsoleEntryPlugins.pluginVersion()).isNotBlank();
    }

    @Test
    void entryPluginDefinitionUsesPlatformVersionAndModuleTags() {
        PluginDefinition definition = new PluginManagementEntryPlugin().definition();

        assertThat(definition.version()).isEqualTo(BuiltinConsoleEntryPlugins.pluginVersion());
        assertThat(definition.tags().get(ConsoleContributions.DISCOVERY_TAG_KIND))
                .contains(ConsoleContributions.DISCOVERY_TAG_KIND_ENTRY);
        assertThat(definition.tags().get(ConsoleContributions.DISCOVERY_TAG_DOMAIN))
                .contains(ConsolePageRoutes.BUILTIN_DOMAIN_KEY);
        assertThat(definition.tags().get(ConsoleContributions.DISCOVERY_TAG_MODULE))
                .contains("plugin");
        assertThat(definition.tags().get("scope")).isEmpty();
    }

    @Test
    void tagsForContributionReflectsDomainAndModule() {
        PluginDefinition definition = new PluginManagementEntryPlugin().definition();
        var contribution = definition.contributions().stream()
                .filter(c -> c instanceof com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution)
                .map(c -> (com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution) c)
                .findFirst()
                .orElseThrow();

        assertThat(BuiltinConsoleEntryPlugins.tagsFor(definition.pluginId(), contribution)
                .get(ConsoleContributions.DISCOVERY_TAG_DOMAIN))
                .contains(ConsolePageRoutes.BUILTIN_DOMAIN_KEY);
        assertThat(BuiltinConsoleEntryPlugins.tagsFor(definition.pluginId(), contribution)
                .get(ConsoleContributions.DISCOVERY_TAG_MODULE))
                .contains("plugin");
    }

    @Test
    void discoveryTagsForModuleKeys() {
        assertThat(ConsoleContributions.discoveryTags(
                BuiltinConsoleEntryPlugins.PLUGIN_MANAGEMENT,
                ConsolePageRoutes.BUILTIN_DOMAIN_KEY,
                List.of("plugin")).get(ConsoleContributions.DISCOVERY_TAG_MODULE))
                .contains("plugin");
    }
}

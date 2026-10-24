package com.innospots.nexus.platform.entry;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformConsoleEntryPluginTest {

    @Test
    void declaresAggregatedPlatformConsoleContribution() {
        PluginDefinition definition = new PlatformConsoleEntryPlugin().definition();

        assertThat(definition.pluginId()).isEqualTo(BuiltinPlatformEntryPlugins.PLATFORM_CONSOLE);
        assertThat(definition.tags().asMap()).containsEntry(
                BuiltinPlatformEntryPlugins.TAG_REALM,
                BuiltinPlatformEntryPlugins.TAG_REALM_PLATFORM);

        ConsolePluginContribution contribution = definition.contributions().stream()
                .filter(ConsolePluginContribution.class::isInstance)
                .map(ConsolePluginContribution.class::cast)
                .findFirst()
                .orElseThrow();

        assertThat(contribution.modules()).extracting(ConsoleContributeNode::moduleKey)
                .containsExactly(
                        "organization",
                        "menu",
                        "user",
                        "dictionary",
                        "role",
                        "permission",
                        "logger",
                        "settings");
        assertThat(definition.contributions()).isNotEmpty();
    }

    @Test
    void eachModuleDeclaresSinglePageModuleWithEntryPageKey() {
        ConsolePluginContribution contribution = consoleContribution();
        ConsoleContributeNode userModule = contribution.modules().stream()
                .filter(module -> "user".equals(module.moduleKey()))
                .findFirst()
                .orElseThrow();

        assertThat(PlatformModuleDescriptors.USERS.children()).isEmpty();
        assertThat(PlatformModuleDescriptors.USERS.isModule()).isTrue();
        assertThat(userModule.children()).isEmpty();
        assertThat(userModule.entryPageKey())
                .isEqualTo(PlatformModuleDescriptors.USERS.entryPageKey());

        ConsoleContributeNode organization = contribution.modules().stream()
                .filter(module -> "organization".equals(module.moduleKey()))
                .findFirst()
                .orElseThrow();
        assertThat(organization.children()).isEmpty();
        assertThat(organization.viewGroups()).extracting(group -> group.group())
                .containsExactly(ConsoleContributeNode.DEFAULT_VIEW_GROUP);
        assertThat(organization.operationGroups()).extracting(group -> group.group())
                .containsExactly(ConsoleContributeNode.DEFAULT_VIEW_GROUP);

        ConsoleContributeNode settings = contribution.modules().stream()
                .filter(module -> "settings".equals(module.moduleKey()))
                .findFirst()
                .orElseThrow();
        assertThat(settings.viewGroups()).extracting(group -> group.group())
                .containsExactly(ConsoleContributeNode.DEFAULT_VIEW_GROUP);
    }

    @Test
    void registersThroughSpi() throws Exception {
        String servicePath = "META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin";
        String content = new String(getClass().getClassLoader().getResourceAsStream(servicePath).readAllBytes());
        List<String> pluginClasses = content.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        assertThat(pluginClasses).contains("com.innospots.nexus.platform.entry.PlatformConsoleEntryPlugin");
    }

    private static ConsolePluginContribution consoleContribution() {
        return new PlatformConsoleEntryPlugin().definition().contributions().stream()
                .filter(ConsolePluginContribution.class::isInstance)
                .map(ConsolePluginContribution.class::cast)
                .findFirst()
                .orElseThrow();
    }

}

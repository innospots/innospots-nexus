package com.innospots.nexus.platform.entry;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionDecoder;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.discovery.ClasspathPluginDiscovery;
import com.innospots.nexus.core.plugin.discovery.PluginDiscoveryReport;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformConsoleEntryPluginTest {

    @Test
    void declaresAggregatedPlatformConsoleContributionFromYaml() {
        PluginDefinition definition = platformConsoleDefinition();

        assertThat(definition.pluginId()).isEqualTo(BuiltinPlatformEntryPlugins.PLATFORM_CONSOLE);
        assertThat(definition.version()).isEqualTo("0.1.0");
        assertThat(definition.tags().asMap()).containsEntry(
                BuiltinPlatformEntryPlugins.TAG_REALM,
                BuiltinPlatformEntryPlugins.TAG_REALM_PLATFORM);
        assertThat(definition.tags().asMap()).containsEntry("module", "console");

        ConsolePluginContribution contribution = consoleContribution(definition);

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
        ConsolePluginContribution contribution = consoleContribution(platformConsoleDefinition());
        ConsoleContributeNode userModule = contribution.modules().stream()
                .filter(module -> "user".equals(module.moduleKey()))
                .findFirst()
                .orElseThrow();

        assertThat(userModule.children()).isEmpty();
        assertThat(userModule.isModule()).isTrue();
        assertThat(userModule.entryPageKey()).isEqualTo("platform-user-main");

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
        assertThat(settings.entryPageKey()).isEqualTo("platform-settings-registration-mode");
        assertThat(settings.viewGroups()).extracting(group -> group.group())
                .containsExactly(ConsoleContributeNode.DEFAULT_VIEW_GROUP);
    }

    @Test
    void packagesYamlPluginDeclarationAtStandardPath() {
        assertThat(getClass().getResource("/" + ClasspathPluginDiscovery.MANIFEST_RESOURCE))
                .isNotNull();
    }

    @Test
    void doesNotRegisterPlatformConsoleThroughJavaSpi() throws Exception {
        String servicePath = "META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin";
        var stream = getClass().getClassLoader().getResourceAsStream(servicePath);
        if (stream == null) {
            return;
        }
        String content = new String(stream.readAllBytes());
        assertThat(content.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank() && !line.startsWith("#")))
                .noneMatch(line -> line.contains("com.innospots.nexus.platform.entry"));
    }

    private static PluginDefinition platformConsoleDefinition() {
        PluginDiscoveryReport report = new ClasspathPluginDiscovery(
                PlatformConsoleEntryPluginTest.class.getClassLoader(),
                PluginContributionDecoderRegistry.builder()
                        .register(new ConsolePluginContributionDecoder())
                        .build()).discoverReport();
        return report.validCatalog().plugin(BuiltinPlatformEntryPlugins.PLATFORM_CONSOLE)
                .orElseThrow(() -> new AssertionError(
                        "platform console plugin not discovered; rejected=" + report.rejectedDefinitions()))
                .definition();
    }

    private static ConsolePluginContribution consoleContribution(PluginDefinition definition) {
        return definition.contributions().stream()
                .filter(ConsolePluginContribution.class::isInstance)
                .map(ConsolePluginContribution.class::cast)
                .findFirst()
                .orElseThrow();
    }
}

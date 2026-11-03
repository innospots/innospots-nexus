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
    void declaresSystemAndOperationConsoleContributionsFromYaml() {
        PluginDefinition definition = platformConsoleDefinition();

        assertThat(definition.pluginId()).isEqualTo(BuiltinPlatformEntryPlugins.PLATFORM_CONSOLE);
        assertThat(definition.version()).isEqualTo("0.1.0");
        assertThat(definition.tags().asMap()).containsEntry(
                BuiltinPlatformEntryPlugins.TAG_REALM,
                BuiltinPlatformEntryPlugins.TAG_REALM_PLATFORM);
        assertThat(definition.tags().asMap()).containsEntry("module", "console");

        assertThat(definition.contributions()).hasSize(2);
        ConsolePluginContribution system = contribution(definition, "system");
        ConsolePluginContribution operation = contribution(definition, "operation");

        assertThat(system.modules()).extracting(ConsoleContributeNode::moduleKey)
                .containsExactly("settings", "theme", "logger", "dictionary");
        assertThat(operation.modules()).extracting(ConsoleContributeNode::moduleKey)
                .containsExactly("role", "user", "organization");
    }

    @Test
    void systemAndOperationModulesDeclareEntryPageKeys() {
        ConsolePluginContribution system = contribution(platformConsoleDefinition(), "system");
        ConsolePluginContribution operation = contribution(platformConsoleDefinition(), "operation");

        ConsoleContributeNode settings = module(system, "settings");
        assertThat(settings.entryPageKey()).isEqualTo("system-settings");

        ConsoleContributeNode theme = module(system, "theme");
        assertThat(theme.entryPageKey()).isEqualTo("themes");

        ConsoleContributeNode user = module(operation, "user");
        assertThat(user.entryPageKey()).isEqualTo("platform-user-main");
        assertThat(user.children()).isEmpty();
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

    private static ConsoleContributeNode module(ConsolePluginContribution contribution, String moduleKey) {
        return contribution.modules().stream()
                .filter(module -> moduleKey.equals(module.moduleKey()))
                .findFirst()
                .orElseThrow();
    }

    private static ConsolePluginContribution contribution(PluginDefinition definition, String domainKey) {
        return definition.contributions().stream()
                .filter(ConsolePluginContribution.class::isInstance)
                .map(ConsolePluginContribution.class::cast)
                .filter(contribution -> domainKey.equals(contribution.domainKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing console contribution for domain " + domainKey));
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
}

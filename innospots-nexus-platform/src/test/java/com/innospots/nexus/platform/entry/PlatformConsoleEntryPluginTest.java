package com.innospots.nexus.platform.entry;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.ui.spec.PageDsl;
import com.innospots.nexus.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.console.ui.spec.loader.ClasspathPageDslLoader;
import com.innospots.nexus.console.ui.spec.parser.JacksonPageDslParser;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleModuleDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformConsoleEntryPluginTest {

    private static final String DOMAIN = BuiltinPlatformEntryPlugins.DOMAIN_KEY;

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

        assertThat(contribution.modules()).extracting(ConsoleModuleDeclaration::moduleKey)
                .containsExactly("organization", "user", "settings");
        assertThat(definition.capabilities()).isEmpty();
    }

    @Test
    void userModuleDeclaresMultipleMenusAndPages() {
        ConsolePluginContribution contribution = consoleContribution();
        ConsoleModuleDeclaration userModule = contribution.modules().stream()
                .filter(module -> "user".equals(module.moduleKey()))
                .findFirst()
                .orElseThrow();

        assertThat(userModule.pages()).extracting(page -> page.pageKey())
                .contains(
                        PlatformModuleDescriptors.pageKey("user", "main"),
                        PlatformModuleDescriptors.pageKey("user", "invite"),
                        PlatformModuleDescriptors.pageKey("user", "access"),
                        PlatformModuleDescriptors.pageKey("user", "add"));
        assertThat(userModule.menuTree()).hasSize(4);
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

    @ParameterizedTest
    @MethodSource("platformEntryPages")
    void loadsPageDslFromClasspath(String moduleKey, String pageKey) {
        PageDslConfig config = PageDslConfig.defaults();
        ClasspathPageDslLoader loader = new ClasspathPageDslLoader(
                config,
                new JacksonPageDslParser(config),
                getClass().getClassLoader());

        PageDsl document = loader.load(DOMAIN, moduleKey, pageKey);

        assertThat(document.getPage().getId()).isEqualTo(pageKey);
        assertThat(document.getPage().getType()).isEqualTo("general");
    }

    private static ConsolePluginContribution consoleContribution() {
        return new PlatformConsoleEntryPlugin().definition().contributions().stream()
                .filter(ConsolePluginContribution.class::isInstance)
                .map(ConsolePluginContribution.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private static Stream<Arguments> platformEntryPages() {
        return Stream.of(
                Arguments.of(
                        "organization",
                        ConsoleModuleDescriptor.compositePageKey(DOMAIN, "organization", "main")),
                Arguments.of("user", ConsoleModuleDescriptor.compositePageKey(DOMAIN, "user", "main")),
                Arguments.of("user", ConsoleModuleDescriptor.compositePageKey(DOMAIN, "user", "add")),
                Arguments.of("user", ConsoleModuleDescriptor.compositePageKey(DOMAIN, "user", "invite")),
                Arguments.of("user", ConsoleModuleDescriptor.compositePageKey(DOMAIN, "user", "access")),
                Arguments.of(
                        "settings",
                        ConsoleModuleDescriptor.compositePageKey(DOMAIN, "settings", "registration-mode")));
    }
}

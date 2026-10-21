package com.innospots.nexus.console.entry;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.innospots.nexus.console.plugin.entry.PluginManagementEntryPlugin;
import com.innospots.nexus.console.ui.spec.PageDsl;
import com.innospots.nexus.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.console.ui.spec.loader.ClasspathPageDslLoader;
import com.innospots.nexus.console.ui.spec.parser.JacksonPageDslParser;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleModuleEntryPluginsTest {

    @ParameterizedTest
    @MethodSource("builtinEntryPlugins")
    void declaresConsoleContributionWithMainMenu(Plugin plugin) {
        PluginDefinition definition = plugin.definition();
        ConsolePluginContribution contribution = definition.contributions().stream()
                .filter(ConsolePluginContribution.class::isInstance)
                .map(ConsolePluginContribution.class::cast)
                .findFirst()
                .orElseThrow();

        ConsoleContributeNode module = contribution.modules().getFirst();
        String entryPageKey = module.entryPageKey();
        assertThat(module.moduleKey()).isNotBlank();
        assertThat(entryPageKey).isNotBlank();
        assertThat(module.children()).isEmpty();
        assertThat(ConsolePageRoutes.pagePath(
                contribution.domainKey(), module.moduleKey(), entryPageKey))
                .startsWith(ConsolePageRoutes.PAGE_ROUTE_PREFIX);
        assertThat(definition.contributions()).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("moduleEntryPages")
    void loadsPageDslFromClasspath(String moduleKey, String pageKey) {
        PageDslConfig config = PageDslConfig.defaults();
        ClasspathPageDslLoader loader = new ClasspathPageDslLoader(
                config,
                new JacksonPageDslParser(config),
                getClass().getClassLoader());

        PageDsl document = loader.load(ConsolePageRoutes.BUILTIN_DOMAIN_KEY, moduleKey, pageKey);

        assertThat(document.getPage().getId()).isEqualTo(pageKey);
        assertThat(document.getPage().getType()).isEqualTo("general");
        assertThat(document.components()).isEmpty();
        assertThat(document.getBody()).isNull();
    }

    @Test
    void registersAllBuiltinEntryPluginsThroughSpi() throws Exception {
        String servicePath = "META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin";
        String content = new String(getClass().getClassLoader().getResourceAsStream(servicePath).readAllBytes());
        List<String> pluginClasses = content.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        assertThat(pluginClasses).containsExactly(
                "com.innospots.nexus.console.plugin.entry.PluginManagementEntryPlugin");
    }

    private static Stream<Plugin> builtinEntryPlugins() {
        return Stream.of(new PluginManagementEntryPlugin());
    }

    private static Stream<Arguments> moduleEntryPages() {
        String domain = ConsolePageRoutes.BUILTIN_DOMAIN_KEY;
        return Stream.of(
                Arguments.of("plugin", ConsolePageRoutes.compositePageKey(domain, "plugin", "main")));
    }
}

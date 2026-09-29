package com.innospots.nexus.console.entry;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.innospots.nexus.console.ui.spec.PageDsl;
import com.innospots.nexus.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.console.ui.spec.loader.ClasspathPageDslLoader;
import com.innospots.nexus.console.ui.spec.parser.JacksonPageDslParser;
import com.innospots.nexus.console.dictionary.entry.DictionaryEntryPlugin;
import com.innospots.nexus.console.logger.entry.LoggerEntryPlugin;
import com.innospots.nexus.console.menu.entry.MenuEntryPlugin;
import com.innospots.nexus.console.permission.entry.PermissionEntryPlugin;
import com.innospots.nexus.console.plugin.entry.PluginManagementEntryPlugin;
import com.innospots.nexus.console.role.entry.RoleEntryPlugin;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleModuleDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleModuleEntryPluginsTest {

    @ParameterizedTest
    @MethodSource("builtinEntryPlugins")
    void declaresConsoleContributionWithMainPage(Plugin plugin) {
        PluginDefinition definition = plugin.definition();
        ConsolePluginContribution contribution = definition.contributions().stream()
                .filter(ConsolePluginContribution.class::isInstance)
                .map(ConsolePluginContribution.class::cast)
                .findFirst()
                .orElseThrow();

        ConsoleModuleDeclaration module = contribution.modules().getFirst();
        String entryPageKey = module.pages().getFirst().pageKey();
        assertThat(module.moduleKey()).isNotBlank();
        assertThat(entryPageKey).isNotBlank();
        assertThat(module.pages()).singleElement()
                .satisfies(page -> assertThat(page.pagePath()).isEqualTo(ConsoleModuleDescriptor.pagePath(
                        module.domainKey(), module.moduleKey(), entryPageKey)));
        assertThat(module.menuTree()).singleElement()
                .satisfies(menu -> assertThat(menu.pageKey()).isEqualTo(entryPageKey));
        assertThat(definition.capabilities()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("moduleEntryPages")
    void loadsPageDslFromClasspath(String moduleKey, String pageKey) {
        PageDslConfig config = PageDslConfig.defaults();
        ClasspathPageDslLoader loader = new ClasspathPageDslLoader(
                config,
                new JacksonPageDslParser(config),
                getClass().getClassLoader());

        PageDsl document = loader.load(ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY, moduleKey, pageKey);

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
                "com.innospots.nexus.console.menu.entry.MenuEntryPlugin",
                "com.innospots.nexus.console.dictionary.entry.DictionaryEntryPlugin",
                "com.innospots.nexus.console.logger.entry.LoggerEntryPlugin",
                "com.innospots.nexus.console.permission.entry.PermissionEntryPlugin",
                "com.innospots.nexus.console.role.entry.RoleEntryPlugin",
                "com.innospots.nexus.console.plugin.entry.PluginManagementEntryPlugin");
    }

    private static Stream<Plugin> builtinEntryPlugins() {
        return Stream.of(
                new MenuEntryPlugin(),
                new DictionaryEntryPlugin(),
                new LoggerEntryPlugin(),
                new PermissionEntryPlugin(),
                new RoleEntryPlugin(),
                new PluginManagementEntryPlugin());
    }

    private static Stream<Arguments> moduleEntryPages() {
        return Stream.of(
                Arguments.of("menu", "menu-main"),
                Arguments.of("dictionary", "dictionary-main"),
                Arguments.of("logger", "logger-main"),
                Arguments.of("permission", "permission-main"),
                Arguments.of("plugin", "plugin-main"),
                Arguments.of("role", "role-main"));
    }
}

package com.innospots.nexus.console.entry;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.config.UiSpecConfig;
import com.innospots.nexus.base.ui.spec.loader.ClasspathUiSpecLoader;
import com.innospots.nexus.base.ui.spec.parser.JacksonUiSpecParser;
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
        String pageKey = ConsoleModuleDescriptor.mainPageKey(module.moduleKey());
        assertThat(module.moduleKey()).isNotBlank();
        assertThat(module.pages()).singleElement()
                .satisfies(page -> assertThat(page.pageKey()).isEqualTo(pageKey));
        assertThat(module.menuTree()).singleElement()
                .satisfies(menu -> assertThat(menu.pageKey()).isEqualTo(pageKey));
        assertThat(definition.capabilities()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("moduleKeys")
    void loadsPageInfoOnlyUiSpecFromClasspath(String moduleKey) {
        String pageKey = ConsoleModuleDescriptor.mainPageKey(moduleKey);
        UiSpecConfig config = UiSpecConfig.defaults();
        ClasspathUiSpecLoader loader = new ClasspathUiSpecLoader(
                config,
                new JacksonUiSpecParser(config),
                getClass().getClassLoader());

        UiSpec spec = loader.load(moduleKey, pageKey);

        assertThat(spec.pageInfo().pageId()).isEqualTo(pageKey);
        assertThat(spec.pageType()).isEqualTo("general");
        assertThat(spec.components()).isEmpty();
        assertThat(spec.layout()).isNull();
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

    private static Stream<String> moduleKeys() {
        return Stream.of("menu", "dictionary", "logger", "permission", "plugin", "role");
    }
}

package com.innospots.nexus.console.entry;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.menu.entry.MenuEntryPlugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class BuiltinConsoleEntryPluginsTest {

    @Test
    void pluginVersionIsNeverBlank() {
        assertThat(BuiltinConsoleEntryPlugins.pluginVersion()).isNotBlank();
    }

    @Test
    void entryPluginDefinitionUsesPlatformVersionAndModuleTags() {
        PluginDefinition definition = new MenuEntryPlugin().definition();

        assertThat(definition.version()).isEqualTo(BuiltinConsoleEntryPlugins.pluginVersion());
        assertThat(definition.tags().get(BuiltinConsoleEntryPlugins.TAG_KIND))
                .contains(BuiltinConsoleEntryPlugins.TAG_KIND_ENTRY);
        assertThat(definition.tags().get(BuiltinConsoleEntryPlugins.TAG_DOMAIN))
                .contains(ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY);
        assertThat(definition.tags().get(BuiltinConsoleEntryPlugins.TAG_MODULE))
                .contains("menu");
        assertThat(definition.tags().get("scope")).isEmpty();
    }

    @Test
    void tagsForDescriptorReflectsDomainAndModule() {
        ConsoleModuleDescriptor descriptor = ConsoleModuleDescriptor.builtin(
                BuiltinConsoleEntryPlugins.ROLE,
                "role",
                "team",
                30,
                I18nObject.of("en", "Role"),
                I18nObject.of("en", "Role module"),
                I18nObject.of("en", "Role"));

        assertThat(BuiltinConsoleEntryPlugins.tagsFor(descriptor).get(BuiltinConsoleEntryPlugins.TAG_DOMAIN))
                .contains("nexus");
        assertThat(BuiltinConsoleEntryPlugins.tagsFor(descriptor).get(BuiltinConsoleEntryPlugins.TAG_MODULE))
                .contains("role");
    }
}

package com.innospots.nexus.spring.console;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.spring.console.config.ConsoleAuthConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleCatalogConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleDictionaryConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleMenuConfiguration;
import com.innospots.nexus.spring.console.config.ConsolePluginConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleRoleConfiguration;
import com.innospots.nexus.spring.console.support.EnableImportResolver;
import com.innospots.nexus.spring.core.bootstrap.NexusPersistenceConfiguration;
import com.innospots.nexus.spring.core.plugin.PluginHostConfiguration;
import com.innospots.nexus.spring.core.plugin.PluginHostStartupConfiguration;
import com.innospots.nexus.spring.core.setting.NexusSystemSettingConfiguration;

class ConsoleEnableImportsTest {

    @Test
    void simpleConsole_includesCoreDomainsWithoutCatalogPluginMenuDictionary() {
        Set<Class<?>> imported = EnableImportResolver.resolveImportedConfigurationTypes(
                EnableNexusSimpleConsole.class);

        assertThat(imported).contains(
                NexusPersistenceConfiguration.class,
                NexusSystemSettingConfiguration.class,
                ConsoleAuthConfiguration.class,
                ConsoleRoleConfiguration.class);
        assertThat(imported).doesNotContain(
                PluginHostConfiguration.class,
                PluginHostStartupConfiguration.class,
                ConsolePluginConfiguration.class,
                ConsoleCatalogConfiguration.class,
                ConsoleMenuConfiguration.class,
                ConsoleDictionaryConfiguration.class);
    }

    @Test
    void fullConsole_addsPluginCatalogMenuAndDictionary() {
        Set<Class<?>> imported = EnableImportResolver.resolveImportedConfigurationTypes(
                EnableNexusConsole.class);

        assertThat(imported).contains(
                ConsoleAuthConfiguration.class,
                PluginHostConfiguration.class,
                PluginHostStartupConfiguration.class,
                ConsolePluginConfiguration.class,
                ConsoleCatalogConfiguration.class,
                ConsoleMenuConfiguration.class,
                ConsoleDictionaryConfiguration.class);
    }
}

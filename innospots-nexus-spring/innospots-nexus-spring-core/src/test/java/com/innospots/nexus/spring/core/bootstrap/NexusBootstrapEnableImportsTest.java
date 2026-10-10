package com.innospots.nexus.spring.core.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.spring.core.bootstrap.support.EnableImportResolver;
import com.innospots.nexus.spring.core.i18n.NexusI18nConfiguration;
import com.innospots.nexus.spring.core.jaxrs.NexusJaxRsConfiguration;
import com.innospots.nexus.spring.core.plugin.EnableNexusPluginHost;
import com.innospots.nexus.spring.core.plugin.PluginHostConfiguration;
import com.innospots.nexus.spring.core.plugin.PluginHostStartupConfiguration;
import com.innospots.nexus.spring.core.plugin.NexusPluginInstallationDaoConfiguration;
import com.innospots.nexus.spring.core.setting.NexusSystemSettingConfiguration;

class NexusBootstrapEnableImportsTest {

    @Test
    void simpleBootstrap_includesPersistenceTransactionAndWebBasics() {
        Set<Class<?>> imported = EnableImportResolver.resolveImportedConfigurationTypes(
                EnableNexusSimpleBootstrap.class);

        assertThat(imported).contains(
                NexusPersistenceConfiguration.class,
                NexusTransactionConfiguration.class,
                NexusStartupConfiguration.class,
                NexusI18nConfiguration.class,
                NexusJaxRsConfiguration.class);
        assertThat(imported).doesNotContain(NexusSystemSettingConfiguration.class);
    }

    @Test
    void hostBootstrap_addsSystemSettingOnTopOfSimple() {
        Set<Class<?>> imported = EnableImportResolver.resolveImportedConfigurationTypes(
                EnableNexusHostBootstrap.class);

        assertThat(imported).contains(
                NexusPersistenceConfiguration.class,
                NexusTransactionConfiguration.class,
                NexusSystemSettingConfiguration.class);
    }

    @Test
    void pluginHost_isIndependentOfHostBootstrap() {
        Set<Class<?>> imported = EnableImportResolver.resolveImportedConfigurationTypes(
                EnableNexusPluginHost.class);

        assertThat(imported).contains(
                NexusPluginInstallationDaoConfiguration.class,
                PluginHostConfiguration.class,
                PluginHostStartupConfiguration.class);
        assertThat(imported).doesNotContain(
                NexusSystemSettingConfiguration.class,
                NexusPersistenceConfiguration.class);
    }
}

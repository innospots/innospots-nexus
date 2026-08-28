package com.innospots.nexus.core.plugin.discovery;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClasspathPluginDiscoveryTest {

    private static final CapabilityType<SampleProvider> SAMPLE =
            CapabilityType.of("sample.discovery", 1, SampleProvider.class);

    @TempDir
    Path classpathRoot;

    @Test
    void discoversAllVisiblePluginsWithoutCallingProviderFactories() throws Exception {
        SamplePlugin.factoryCalls = 0;
        SamplePlugin.definitionCalls = 0;
        ClassLoader classLoader = serviceClassLoader(SamplePlugin.class.getName());

        var plugins = new ClasspathPluginDiscovery(classLoader).discover();

        assertThat(plugins).hasSize(1);
        assertThat(plugins.getFirst().definition().id()).isEqualTo("sample-discovery");
        assertThat(SamplePlugin.definitionCalls).isEqualTo(1);
        assertThat(SamplePlugin.factoryCalls).isZero();
    }

    @Test
    void supportsStaticClasspathDiscoveryAndImmutableCatalogLookup() throws Exception {
        ClassLoader classLoader = serviceClassLoader(SamplePlugin.class.getName());

        PluginCatalog catalog = PluginCatalog.discover(classLoader);

        assertThat(catalog.plugins()).hasSize(1);
        assertThat(catalog.plugin("sample-discovery")).isPresent();
        assertThat(catalog.definitions()).extracting(PluginDefinition::id)
                .containsExactly("sample-discovery");
        assertThatThrownBy(() -> catalog.plugins().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsDuplicatePluginIdsBeforeLifecycleRuns() throws Exception {
        ClassLoader classLoader = serviceClassLoader(
                SamplePlugin.class.getName(),
                DuplicatePlugin.class.getName());

        assertThatThrownBy(() -> new ClasspathPluginDiscovery(classLoader).discover())
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("sample-discovery");
    }

    private ClassLoader serviceClassLoader(String... implementationClasses) throws Exception {
        Path serviceFile = classpathRoot.resolve(
                "META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin");
        Files.createDirectories(serviceFile.getParent());
        Files.writeString(serviceFile, String.join(System.lineSeparator(), implementationClasses));
        return new URLClassLoader(
                new java.net.URL[]{classpathRoot.toUri().toURL()},
                getClass().getClassLoader());
    }

    public static final class SamplePlugin implements Plugin {

        private static int definitionCalls;
        private static int factoryCalls;

        @Override
        public PluginDefinition definition() {
            definitionCalls++;
            return fixtureDefinition("sample-discovery");
        }
    }

    public static final class DuplicatePlugin implements Plugin {

        @Override
        public PluginDefinition definition() {
            return fixtureDefinition("sample-discovery");
        }
    }

    private static PluginDefinition fixtureDefinition(String id) {
        return PluginDefinition.builder(id)
                .name("Discovery Fixture")
                .version("1.0.0")
                .tags(Tags.of("fixture", "discovery"))
                .provide(SAMPLE, () -> {
                    SamplePlugin.factoryCalls++;
                    return new SampleProviderImpl();
                })
                .build();
    }

    private interface SampleProvider extends CapabilityProvider {
    }

    private static final class SampleProviderImpl implements SampleProvider {
    }
}

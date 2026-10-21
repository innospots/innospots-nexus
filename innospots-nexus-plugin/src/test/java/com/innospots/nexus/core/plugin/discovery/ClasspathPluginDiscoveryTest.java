package com.innospots.nexus.core.plugin.discovery;

import java.net.URLClassLoader;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.declaration.Tags;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClasspathPluginDiscoveryTest {

    @TempDir
    Path classpathRoot;

    @Test
    void discoversAllVisiblePluginsFromSpi() throws Exception {
        SamplePlugin.definitionCalls = 0;
        ClassLoader classLoader = serviceClassLoader(SamplePlugin.class.getName());

        var plugins = new ClasspathPluginDiscovery(classLoader).discoverReport().validCatalog().plugins();

        assertThat(plugins).hasSize(1);
        assertThat(plugins.getFirst().definition().pluginId()).isEqualTo("com.example.sample-discovery");
        assertThat(SamplePlugin.definitionCalls).isEqualTo(1);
    }

    @Test
    void supportsStaticClasspathDiscoveryAndImmutableCatalogLookup() throws Exception {
        ClassLoader classLoader = serviceClassLoader(SamplePlugin.class.getName());

        PluginCatalog catalog = new ClasspathPluginDiscovery(classLoader).discoverReport().validCatalog();

        assertThat(catalog.plugins()).hasSize(1);
        assertThat(catalog.plugin("com.example.sample-discovery")).isPresent();
        assertThat(catalog.definitions()).extracting(PluginDefinition::pluginId)
                .containsExactly("com.example.sample-discovery");
        assertThatThrownBy(() -> catalog.plugins().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsDuplicatePluginIdsBeforeLifecycleRuns() throws Exception {
        ClassLoader classLoader = serviceClassLoader(
                SamplePlugin.class.getName(),
                DuplicatePlugin.class.getName());

        assertThatThrownBy(() -> new ClasspathPluginDiscovery(classLoader).discoverReport())
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("sample-discovery");
    }

    @Test
    void catalogFactorySortsAndValidatesStaticDiscoveryEntries() {
        DiscoveredPlugin second = new DiscoveredPlugin(
                new OtherPlugin(),
                fixtureDefinition("com.example.zeta-discovery"),
                java.time.Instant.now());
        DiscoveredPlugin first = new DiscoveredPlugin(
                new SamplePlugin(),
                fixtureDefinition("com.example.alpha-discovery"),
                java.time.Instant.now());

        PluginCatalog catalog = PluginCatalog.of(Arrays.asList(second, first));

        assertThat(catalog.plugins()).extracting(item -> item.definition().pluginId())
                .containsExactly("com.example.alpha-discovery", "com.example.zeta-discovery");
        assertThatThrownBy(() -> PluginCatalog.of(Arrays.asList(first, null)))
                .isInstanceOf(NexusException.class);

        DiscoveredPlugin incompatible = new DiscoveredPlugin(
                new OtherPlugin(),
                PluginDefinition.builder("com.example.incompatible-discovery")
                        .name("Incompatible Discovery")
                        .version("1.0.0")
                        .apiVersion(PluginDefinition.CURRENT_API_VERSION + 1)
                        .tags(Tags.of("fixture", "discovery"))
                        .build(),
                java.time.Instant.now());
        assertThatThrownBy(() -> PluginCatalog.of(Arrays.asList(incompatible)))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("apiVersion");
    }

    @Test
    void rejectsInvalidDiscoveredPluginRecords() {
        assertThatThrownBy(() -> new DiscoveredPlugin(null, fixtureDefinition("invalid-discovery"),
                java.time.Instant.now()))
                .isInstanceOf(NexusException.class);
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

        @Override
        public PluginDefinition definition() {
            definitionCalls++;
            return fixtureDefinition("com.example.sample-discovery");
        }
    }

    public static final class DuplicatePlugin implements Plugin {

        @Override
        public PluginDefinition definition() {
            return fixtureDefinition("com.example.sample-discovery");
        }
    }

    public static final class OtherPlugin implements Plugin {

        @Override
        public PluginDefinition definition() {
            return fixtureDefinition("com.example.zeta-discovery");
        }
    }

    private static PluginDefinition fixtureDefinition(String id) {
        ConsoleContributeNode menu = new ConsoleContributeNode(
                null,
                I18nObject.of(Map.of("en", "Menu")),
                I18nObject.of(Map.of()),
                0,
                "discovery-page",
                null,
                List.of(),
                List.of(),
                List.of());
        ConsoleContributeNode module = new ConsoleContributeNode(
                "main",
                I18nObject.of(Map.of("en", "Main")),
                I18nObject.of(Map.of()),
                0,
                null,
                null,
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Views")))),
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Ops")))),
                List.of(menu));
        return PluginDefinition.builder(id)
                .name("Discovery Fixture")
                .version("1.0.0")
                .tags(Tags.of("fixture", "discovery"))
                .contribute(new ConsolePluginContribution(
                        "discovery",
                        I18nObject.of(Map.of("en", "Discovery")),
                        I18nObject.of(Map.of()),
                        0,
                        List.of(module)))
                .build();
    }
}

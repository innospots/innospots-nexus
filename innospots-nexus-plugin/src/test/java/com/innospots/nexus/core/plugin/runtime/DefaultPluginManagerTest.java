package com.innospots.nexus.core.plugin.runtime;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.console.ReservedPluginResourceCatalog;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.lifecycle.PluginState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultPluginManagerTest {

    @Test
    void startsDiscoveredPluginsInStableOrder() {
        List<String> starts = new ArrayList<>();
        DefaultPluginManager manager = manager(starts, new SourcePlugin(starts), new SinkPlugin(starts));

        manager.start();

        assertThat(starts).containsExactlyInAnyOrder("source", "sink");
        assertThat(manager.plugins()).hasSize(2);
        assertThat(manager.plugin("com.example.fixture-source")).get()
                .extracting(info -> info.state())
                .isEqualTo(PluginState.ACTIVE);
        manager.close();
    }

    @Test
    void rollsBackWhenRequiredPluginFails() {
        DefaultPluginManager manager = DefaultPluginManager.create(
                runtimeConfig(Set.of("com.example.fixture-fail"), Set.of(), Map.of()),
                PluginCatalog.of(List.of(
                        discovered(new SourcePlugin(new ArrayList<>())),
                        discovered(new FailingPlugin()))),
                handlers());

        assertThatThrownBy(manager::start).isInstanceOf(NexusException.class);
        assertThat(manager.plugin("com.example.fixture-source")).get()
                .extracting(info -> info.state())
                .isNotEqualTo(PluginState.ACTIVE);
        manager.close();
    }

    @Test
    void closeMakesManagerTerminal() {
        DefaultPluginManager manager = manager(new ArrayList<>(), new SourcePlugin(new ArrayList<>()));

        manager.start();
        manager.close();

        assertThatThrownBy(manager::start).isInstanceOf(NexusException.class);
        assertThatThrownBy(manager::plugins).isInstanceOf(NexusException.class);
    }

    @Test
    void rejectsNullAndBlankRuntimeConfigurationEntries() {
        Set<String> invalidIds = new HashSet<>();
        invalidIds.add(null);
        assertThatThrownBy(() -> new PluginRuntimeConfig(
                invalidIds, Set.of(), Map.of(), Map.of(), DefaultPluginManagerTest.class.getClassLoader()))
                .isInstanceOf(NexusException.class);

        Map<String, String> invalidConfig = new HashMap<>();
        invalidConfig.put(null, "value");
        assertThatThrownBy(() -> new PluginRuntimeConfig(
                Set.of(), Set.of(), invalidConfig, Map.of(), DefaultPluginManagerTest.class.getClassLoader()))
                .isInstanceOf(NexusException.class);
    }

    private static DefaultPluginManager manager(List<String> starts, Plugin... plugins) {
        List<DiscoveredPlugin> discovered = new ArrayList<>();
        for (Plugin plugin : plugins) {
            discovered.add(discovered(plugin));
        }
        return DefaultPluginManager.create(
                runtimeConfig(Set.of(), Set.of(), Map.of()),
                PluginCatalog.of(discovered),
                handlers());
    }

    private static List<PluginContributionHandler<?>> handlers() {
        return List.of(new ConsolePluginContributionHandler(
                new ConsoleContributionCatalog(),
                new ReservedPluginResourceCatalog(List.of())));
    }

    private static PluginRuntimeConfig runtimeConfig(
            Set<String> required,
            Set<String> disabled,
            Map<String, String> runtimeVariables
    ) {
        return new PluginRuntimeConfig(
                required, disabled, Map.of(), runtimeVariables, DefaultPluginManagerTest.class.getClassLoader());
    }

    private static DiscoveredPlugin discovered(Plugin plugin) {
        return new DiscoveredPlugin(plugin, plugin.definition(), Instant.now());
    }

    private record SourcePlugin(List<String> starts) implements Plugin {

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.fixture-source")
                    .name("Fixture Source")
                    .version("1.0.0")
                    .contribute(sampleContribution("source", "source-page"))
                    .build();
        }

        @Override
        public void start() {
            starts.add("source");
        }
    }

    private record SinkPlugin(List<String> starts) implements Plugin {

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.fixture-sink")
                    .name("Fixture Sink")
                    .version("1.0.0")
                    .contribute(sampleContribution("sink", "sink-page"))
                    .build();
        }

        @Override
        public void start() {
            starts.add("sink");
        }
    }

    private static final class FailingPlugin implements Plugin {

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.fixture-fail")
                    .name("Fail")
                    .version("1.0.0")
                    .contribute(sampleContribution("fail", "fail-page"))
                    .build();
        }

        @Override
        public void start() {
            throw new IllegalStateException("boom");
        }
    }

    private static ConsolePluginContribution sampleContribution(String moduleKey, String pageKey) {
        ConsoleContributeNode menu = new ConsoleContributeNode(
                null,
                I18nObject.of(Map.of("en", "Menu")),
                I18nObject.of(Map.of()),
                0,
                pageKey,
                null,
                List.of(),
                List.of(),
                List.of());
        ConsoleContributeNode module = new ConsoleContributeNode(
                moduleKey,
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
        return new ConsolePluginContribution(
                "fixture",
                I18nObject.of(Map.of("en", "Fixture")),
                I18nObject.of(Map.of()),
                0,
                List.of(module));
    }
}

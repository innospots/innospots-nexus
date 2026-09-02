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
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigDefinition;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.lifecycle.PluginState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultPluginManagerTest {

    private static final CapabilityType<SourceProvider> SOURCE =
            CapabilityType.of("fixture.source", 1, SourceProvider.class);
    private static final CapabilityType<SinkProvider> SINK =
            CapabilityType.of("fixture.sink", 1, SinkProvider.class);

    @Test
    void startsProvidersBeforeDependentsAndProtectsTheLastRequiredProvider() {
        List<String> starts = new ArrayList<>();
        SourcePlugin source = new SourcePlugin(starts);
        SinkPlugin sink = new SinkPlugin(starts);
        List<DiscoveredPlugin> discovered = List.of(
                discovered(sink),
                discovered(source));
        PluginRuntimeConfig config = new PluginRuntimeConfig(
                Set.of("com.example.fixture-sink"),
                Set.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                getClass().getClassLoader());
        DefaultPluginManager manager = DefaultPluginManager.create(
                config, PluginCatalog.of(discovered), List.of());

        manager.start();

        assertThat(starts).containsExactly("source", "sink");
        assertThat(manager.capabilities().findAll(SOURCE)).hasSize(1);
        assertThat(manager.plugin("com.example.fixture-sink")).get()
                .extracting(info -> info.state())
                .isEqualTo(PluginState.ACTIVE);
        assertThatThrownBy(() -> manager.stop("com.example.fixture-source"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("fixture-sink");

        manager.stop("com.example.fixture-sink");
        manager.stop("com.example.fixture-source");
        assertThat(manager.plugins()).allMatch(info -> info.state() == PluginState.STOPPED);
    }

    @Test
    void isolatesInvalidOptionalPluginConfiguration() {
        List<String> starts = new ArrayList<>();
        SourcePlugin source = new SourcePlugin(starts);
        Plugin invalid = new Plugin() {
            @Override
            public PluginDefinition definition() {
                return PluginDefinition.builder("com.example.invalid-config")
                        .name("Invalid Config")
                        .version("1.0.0")
                        .tags(Tags.of("fixture", "invalid"))
                        .config(ConfigDefinition.builder().string("endpoint").required().end().build())
                        .build();
            }
        };
        DefaultPluginManager manager = DefaultPluginManager.create(
                runtimeConfig(Set.of(), Set.of(), Map.of()),
                PluginCatalog.of(List.of(discovered(invalid), discovered(source))),
                List.of());

        manager.start();

        assertThat(starts).containsExactly("source");
        assertThat(manager.plugin("com.example.invalid-config")).get()
                .extracting(info -> info.state())
                .isEqualTo(PluginState.FAILED);
    }

    @Test
    void rejectsAmbiguousDefaultRouteBeforeStartingAnyPlugin() {
        List<String> starts = new ArrayList<>();
        SourcePlugin first = new SourcePlugin(starts);
        Plugin second = new Plugin() {
            @Override
            public PluginDefinition definition() {
                return PluginDefinition.builder("com.example.fixture-source-two")
                        .name("Fixture Source Two")
                        .version("1.0.0")
                        .tags(Tags.of("fixture", "source"))
                        .provide(SOURCE, SourceProviderImpl::new)
                        .build();
            }

            @Override
            public void start() {
                starts.add("source-two");
            }
        };
        Map<CapabilityKey, Tags> routes = Map.of(SOURCE.key(), Tags.of("fixture", "source"));
        assertThatThrownBy(() -> DefaultPluginManager.create(
                runtimeConfig(Set.of(), Set.of(), routes),
                PluginCatalog.of(List.of(discovered(first), discovered(second))),
                List.of()))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("ambiguous");
        assertThat(starts).isEmpty();
    }

    @Test
    void rejectsAmbiguousDefaultRouteWhenOnlyProviderTagsMatch() {
        Plugin first = providerTaggedSource("com.example.fixture-source-one", "one");
        Plugin second = providerTaggedSource("com.example.fixture-source-two", "two");
        Map<CapabilityKey, Tags> routes = Map.of(SOURCE.key(), Tags.of("region", "cn"));
        assertThatThrownBy(() -> DefaultPluginManager.create(
                runtimeConfig(Set.of(), Set.of(), routes),
                PluginCatalog.of(List.of(discovered(first), discovered(second))),
                List.of()))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("ambiguous");
    }

    @Test
    void rejectsMissingRequiredPluginBeforeStartingOthers() {
        List<String> starts = new ArrayList<>();
        assertThatThrownBy(() -> DefaultPluginManager.create(
                runtimeConfig(Set.of("com.example.missing-plugin"), Set.of(), Map.of()),
                PluginCatalog.of(List.of(discovered(new SourcePlugin(starts)))),
                List.of()))
                .isInstanceOf(NexusException.class);
        assertThat(starts).isEmpty();
    }

    @Test
    void preservesRequiredPluginFailureCauseAfterStartupRollback() {
        Plugin failing = new Plugin() {
            @Override
            public PluginDefinition definition() {
                return PluginDefinition.builder("com.example.required-failure")
                        .name("Required Failure")
                        .version("1.0.0")
                        .tags(Tags.of("fixture", "failure"))
                        .build();
            }

            @Override
            public void start() {
                throw new RuntimeException("expected required startup failure");
            }
        };
        DefaultPluginManager manager = DefaultPluginManager.create(
                runtimeConfig(Set.of("com.example.required-failure"), Set.of(), Map.of()),
                PluginCatalog.of(List.of(discovered(failing))),
                List.of());

        assertThatThrownBy(manager::start)
                .isInstanceOf(NexusException.class)
                .hasCauseInstanceOf(NexusException.class);
    }

    @Test
    void closeMakesManagerTerminalAndReleasesRuntimeOperations() {
        List<String> starts = new ArrayList<>();
        DefaultPluginManager manager = DefaultPluginManager.create(
                runtimeConfig(Set.of(), Set.of(), Map.of()),
                PluginCatalog.of(List.of(discovered(new SourcePlugin(starts)))),
                List.of());

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
                invalidIds, Set.of(), Map.of(), Map.of(), Map.of(), getClass().getClassLoader()))
                .isInstanceOf(NexusException.class);

        Map<String, String> invalidConfig = new HashMap<>();
        invalidConfig.put(null, "value");
        assertThatThrownBy(() -> new PluginRuntimeConfig(
                Set.of(), Set.of(), invalidConfig, Map.of(), Map.of(), getClass().getClassLoader()))
                .isInstanceOf(NexusException.class);
    }

    private PluginRuntimeConfig runtimeConfig(
            Set<String> required,
            Set<String> disabled,
            Map<CapabilityKey, Tags> routes
    ) {
        return new PluginRuntimeConfig(
                required, disabled, Map.of(), Map.of(), routes, getClass().getClassLoader());
    }

    private static DiscoveredPlugin discovered(Plugin plugin) {
        return new DiscoveredPlugin(plugin, plugin.definition(), Instant.now());
    }

    private static Plugin providerTaggedSource(String pluginId, String providerId) {
        return new Plugin() {
            @Override
            public PluginDefinition definition() {
                return PluginDefinition.builder(pluginId)
                        .name("Provider Tagged Source")
                        .version("1.0.0")
                        .tags(Tags.of("fixture", "source"))
                        .provide(SOURCE, providerId, Tags.of("region", "cn"),
                                ConfigDefinition.empty(), SourceProviderImpl::new)
                        .build();
            }
        };
    }

    private interface SourceProvider extends CapabilityProvider {
    }

    private interface SinkProvider extends CapabilityProvider {
    }

    private static final class SourceProviderImpl implements SourceProvider {
    }

    private static final class SinkProviderImpl implements SinkProvider {
    }

    private record SourcePlugin(List<String> starts) implements Plugin {

        @Override
        public PluginDefinition definition() {
                return PluginDefinition.builder("com.example.fixture-source")
                    .name("Fixture Source")
                    .version("1.0.0")
                    .tags(Tags.of("fixture", "source"))
                    .provide(SOURCE, SourceProviderImpl::new)
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
                    .tags(Tags.of("fixture", "sink"))
                    .provide(SINK, SinkProviderImpl::new)
                    .require(SOURCE, true)
                    .build();
        }

        @Override
        public void start() {
            starts.add("sink");
        }
    }
}

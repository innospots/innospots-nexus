package com.innospots.nexus.core.plugin.lifecycle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistry;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigurationManager;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.CapabilityProviderContext;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.contract.PluginContext;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.event.DefaultPluginEventBus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManagedPluginTest {

    private static final CapabilityType<FirstProvider> FIRST =
            CapabilityType.of("lifecycle.first", 1, FirstProvider.class);
    private static final CapabilityType<SecondProvider> SECOND =
            CapabilityType.of("lifecycle.second", 1, SecondProvider.class);

    @Test
    void rollsBackInitializedProvidersAndResourcesWithoutPublishingPartialCapabilities() {
        List<String> calls = new ArrayList<>();
        FailingPlugin plugin = new FailingPlugin(calls);
        PluginDefinition definition = plugin.definition();
        CapabilityRegistry registry = new CapabilityRegistry(Map.of());
        ManagedPlugin managed = new ManagedPlugin(
                new DiscoveredPlugin(plugin, definition, Instant.now()),
                new ConfigurationManager(Map.of(), Map.of(), Map.of(), Map.of()).resolve(definition),
                registry,
                new DefaultPluginEventBus());

        assertThatThrownBy(managed::start)
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("lifecycle-fixture");

        assertThat(registry.findAll(FIRST)).isEmpty();
        assertThat(registry.findAll(SECOND)).isEmpty();
        assertThat(managed.info().state()).isEqualTo(PluginState.FAILED);
        assertThat(calls).containsExactly(
                "plugin-initialize",
                "first-initialize",
                "second-initialize",
                "first-destroy",
                "plugin-stop",
                "resource-close");
    }

    @Test
    void publishesAllCapabilitiesOnlyAfterSuccessfulStartupAndStopsInReverseOrder() {
        List<String> calls = new ArrayList<>();
        SuccessfulPlugin plugin = new SuccessfulPlugin(calls);
        PluginDefinition definition = plugin.definition();
        CapabilityRegistry registry = new CapabilityRegistry(Map.of());
        ManagedPlugin managed = new ManagedPlugin(
                new DiscoveredPlugin(plugin, definition, Instant.now()),
                new ConfigurationManager(Map.of(), Map.of(), Map.of(), Map.of()).resolve(definition),
                registry,
                new DefaultPluginEventBus());

        managed.start();
        assertThat(registry.findAll(FIRST)).hasSize(1);
        assertThat(registry.findAll(SECOND)).hasSize(1);
        assertThat(managed.info().state()).isEqualTo(PluginState.ACTIVE);

        managed.stop();

        assertThat(registry.findAll(FIRST)).isEmpty();
        assertThat(calls).containsExactly(
                "plugin-initialize",
                "first-initialize",
                "second-initialize",
                "plugin-start",
                "second-destroy",
                "first-destroy",
                "plugin-stop",
                "resource-close");
    }

    @Test
    void rollsBackLinkageErrorsAndDoesNotRestartFailedPlugin() {
        List<String> calls = new ArrayList<>();
        Plugin plugin = new RecordingPlugin(calls, false) {
            @Override
            public void start() {
                calls.add("plugin-start");
                throw new NoClassDefFoundError("missing optional library");
            }
        };
        PluginDefinition definition = plugin.definition();
        CapabilityRegistry registry = new CapabilityRegistry(Map.of());
        ManagedPlugin managed = new ManagedPlugin(
                new DiscoveredPlugin(plugin, definition, Instant.now()),
                new ConfigurationManager(Map.of(), Map.of(), Map.of(), Map.of()).resolve(definition),
                registry,
                new DefaultPluginEventBus());

        assertThatThrownBy(managed::start).isInstanceOf(NexusException.class);
        assertThat(managed.info().state()).isEqualTo(PluginState.FAILED);
        assertThat(calls).containsExactly(
                "plugin-initialize",
                "first-initialize",
                "second-initialize",
                "plugin-start",
                "second-destroy",
                "first-destroy",
                "plugin-stop",
                "resource-close");

        assertThatThrownBy(managed::start).isInstanceOf(NexusException.class);
        assertThat(calls).hasSize(8);
    }

    private interface FirstProvider extends CapabilityProvider {
    }

    private interface SecondProvider extends CapabilityProvider {
    }

    private static final class RecordingFirstProvider implements FirstProvider {

        private final List<String> calls;

        private RecordingFirstProvider(List<String> calls) {
            this.calls = calls;
        }

        @Override
        public void initialize(CapabilityProviderContext context) {
            calls.add("first-initialize");
        }

        @Override
        public void destroy() {
            calls.add("first-destroy");
        }
    }

    private static final class RecordingSecondProvider implements SecondProvider {

        private final List<String> calls;
        private final boolean fail;

        private RecordingSecondProvider(List<String> calls, boolean fail) {
            this.calls = calls;
            this.fail = fail;
        }

        @Override
        public void initialize(CapabilityProviderContext context) {
            calls.add("second-initialize");
            if (fail) {
                throw new RuntimeException("expected provider failure");
            }
        }

        @Override
        public void destroy() {
            calls.add("second-destroy");
        }
    }

    private abstract static class RecordingPlugin implements Plugin {

        final List<String> calls;
        final boolean fail;

        private RecordingPlugin(List<String> calls, boolean fail) {
            this.calls = calls;
            this.fail = fail;
        }

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("lifecycle-fixture")
                    .name("Lifecycle Fixture")
                    .version("1.0.0")
                    .tags(Tags.of("fixture", "lifecycle"))
                    .provide(FIRST, () -> new RecordingFirstProvider(calls))
                    .provide(SECOND, () -> new RecordingSecondProvider(calls, fail))
                    .build();
        }

        @Override
        public void initialize(PluginContext context) {
            calls.add("plugin-initialize");
            context.resources().add(() -> calls.add("resource-close"));
        }

        @Override
        public void start() {
            calls.add("plugin-start");
        }

        @Override
        public void stop() {
            calls.add("plugin-stop");
        }
    }

    private static final class FailingPlugin extends RecordingPlugin {

        private FailingPlugin(List<String> calls) {
            super(calls, true);
        }
    }

    private static final class SuccessfulPlugin extends RecordingPlugin {

        private SuccessfulPlugin(List<String> calls) {
            super(calls, false);
        }
    }
}

package com.innospots.nexus.core.plugin.capability;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.lifecycle.PluginAvailability;
import com.innospots.nexus.core.plugin.lifecycle.PluginAvailabilityIndex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CapabilityRegistryTest {

    private static final CapabilityType<MessageProvider> MESSAGE =
            CapabilityType.of("message.push", 1, MessageProvider.class);

    @Test
    void routesByExplicitTagsThenConfiguredDefault() {
        MessageProvider app = new NamedMessageProvider("app");
        MessageProvider robot = new NamedMessageProvider("robot");
        CapabilityRegistry registry = new CapabilityRegistry(Map.of(
                MESSAGE.key(), Tags.of("mode", "robot")));
        registry.registerAll(List.of(
                new CapabilityRegistration<>(MESSAGE, app, "com.example.wecom-app", Tags.of("mode", "app")),
                new CapabilityRegistration<>(MESSAGE, robot, "com.example.wecom-robot", Tags.of("mode", "robot"))));

        assertThat(registry.require(MESSAGE, Tags.of("mode", "app"))).isSameAs(app);
        assertThat(registry.require(MESSAGE, Tags.empty())).isSameAs(robot);
        MessageProvider byName = registry.require("message.push", 1, Tags.of("mode", "app"));
        assertThat(byName).isSameAs(app);
        assertThat(registry.findAll(MESSAGE)).containsExactly(app, robot);
    }

    @Test
    void rejectsAmbiguousLookupAndAtomicallyRemovesPluginRegistrations() {
        CapabilityRegistry registry = new CapabilityRegistry(Map.of());
        registry.registerAll(List.of(
                new CapabilityRegistration<>(
                        MESSAGE, new NamedMessageProvider("one"), "com.example.one", Tags.of("channel", "wecom")),
                new CapabilityRegistration<>(
                        MESSAGE, new NamedMessageProvider("two"), "com.example.two", Tags.of("channel", "wecom"))));

        assertThatThrownBy(() -> registry.require(MESSAGE, Tags.of("channel", "wecom")))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("com.example.one")
                .hasMessageContaining("com.example.two");

        registry.unregisterPlugin("com.example.one");

        assertThat(registry.require(MESSAGE, Tags.of("channel", "wecom")).name()).isEqualTo("two");
    }

    @Test
    void rejectsAnAmbiguousConfiguredDefaultWithoutPublishingTheNewSnapshot() {
        MessageProvider first = new NamedMessageProvider("first");
        CapabilityRegistry registry = new CapabilityRegistry(Map.of(
                MESSAGE.key(), Tags.of("channel", "wecom")));
        registry.registerAll(List.of(new CapabilityRegistration<>(
                MESSAGE, first, "com.example.first", Tags.of("channel", "wecom"))));

        assertThatThrownBy(() -> registry.registerAll(List.of(new CapabilityRegistration<>(
                MESSAGE,
                new NamedMessageProvider("second"),
                "com.example.second",
                Tags.of("channel", "wecom")))))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("default route");

        assertThat(registry.findAll(MESSAGE)).containsExactly(first);
    }

    @Test
    void hidesRegisteredProvidersUntilAvailabilityIsActive() {
        MessageProvider provider = new NamedMessageProvider("hidden");
        PluginAvailabilityIndex availabilityIndex = new PluginAvailabilityIndex();
        PluginAvailability availability = new PluginAvailability();
        availabilityIndex.register("com.example.hidden", availability);
        CapabilityRegistry registry = new CapabilityRegistry(Map.of(), availabilityIndex);
        registry.registerAll(List.of(new CapabilityRegistration<>(
                MESSAGE, provider, "com.example.hidden", Tags.of("mode", "app"))));

        assertThat(registry.findAll(MESSAGE)).isEmpty();
        assertThat(registry.contains(MESSAGE.key())).isFalse();

        availability.activate();

        assertThat(registry.findAll(MESSAGE)).containsExactly(provider);
        assertThat(registry.contains(MESSAGE.key())).isTrue();
    }

    @Test
    void rejectsNullPublicInputsWithNexusExceptions() {
        CapabilityRegistry registry = new CapabilityRegistry(Map.of());

        assertThatThrownBy(() -> registry.contains(null)).isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> registry.find(null, Tags.empty())).isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> registry.unregisterPlugin(null)).isInstanceOf(NexusException.class);
    }

    @Test
    void routerRejectsNullSelectionInputsWithNexusExceptions() {
        CapabilityRouter router = new CapabilityRouter(Map.of());

        assertThatThrownBy(() -> router.select(null, Tags.empty(), List.of()))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> router.select(MESSAGE, Tags.empty(), null))
                .isInstanceOf(NexusException.class);
    }

    private interface MessageProvider extends CapabilityProvider {

        String name();
    }

    private record NamedMessageProvider(String name) implements MessageProvider {
    }
}

package com.innospots.nexus.core.plugin.capability;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;

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
                new CapabilityRegistration<>(MESSAGE, app, "wecom-app", Tags.of("mode", "app")),
                new CapabilityRegistration<>(MESSAGE, robot, "wecom-robot", Tags.of("mode", "robot"))));

        assertThat(registry.require(MESSAGE, Tags.of("mode", "app"))).isSameAs(app);
        assertThat(registry.require(MESSAGE, Tags.empty())).isSameAs(robot);
        assertThat(registry.findAll(MESSAGE)).containsExactly(app, robot);
    }

    @Test
    void rejectsAmbiguousLookupAndAtomicallyRemovesPluginRegistrations() {
        CapabilityRegistry registry = new CapabilityRegistry(Map.of());
        registry.registerAll(List.of(
                new CapabilityRegistration<>(
                        MESSAGE, new NamedMessageProvider("one"), "one", Tags.of("channel", "wecom")),
                new CapabilityRegistration<>(
                        MESSAGE, new NamedMessageProvider("two"), "two", Tags.of("channel", "wecom"))));

        assertThatThrownBy(() -> registry.require(MESSAGE, Tags.of("channel", "wecom")))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("one")
                .hasMessageContaining("two");

        registry.unregisterPlugin("one");

        assertThat(registry.require(MESSAGE, Tags.of("channel", "wecom")).name()).isEqualTo("two");
    }

    @Test
    void rejectsAnAmbiguousConfiguredDefaultWithoutPublishingTheNewSnapshot() {
        MessageProvider first = new NamedMessageProvider("first");
        CapabilityRegistry registry = new CapabilityRegistry(Map.of(
                MESSAGE.key(), Tags.of("channel", "wecom")));
        registry.registerAll(List.of(new CapabilityRegistration<>(
                MESSAGE, first, "first", Tags.of("channel", "wecom"))));

        assertThatThrownBy(() -> registry.registerAll(List.of(new CapabilityRegistration<>(
                MESSAGE,
                new NamedMessageProvider("second"),
                "second",
                Tags.of("channel", "wecom")))))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("default route");

        assertThat(registry.findAll(MESSAGE)).containsExactly(first);
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

package com.innospots.nexus.core.plugin.capability;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.support.PluginTestLog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CapabilityRouterTest {

    private static final CapabilityType<MessageProvider> MESSAGE =
            CapabilityType.of("message.push", 1, MessageProvider.class);

    private final PluginTestLog log = new PluginTestLog(CapabilityRouterTest.class, "router");

    @Test
    void selectsByExplicitTagsBeforeConfiguredDefault() {
        CapabilityRouter router = new CapabilityRouter(Map.of(
                MESSAGE.key(), Tags.of("mode", "robot")));
        List<CapabilityRegistration<MessageProvider>> registrations = List.of(
                registration("app", Tags.of("mode", "app")),
                registration("robot", Tags.of("mode", "robot")));

        CapabilityRegistration<MessageProvider> explicit = router.select(
                MESSAGE, Tags.of("mode", "app"), registrations);
        CapabilityRegistration<MessageProvider> defaulted = router.select(
                MESSAGE, Tags.empty(), registrations);

        log.info("explicit selection pluginId=%s", explicit.pluginId());
        log.info("default selection pluginId=%s", defaulted.pluginId());

        assertThat(explicit.pluginId()).isEqualTo("app");
        assertThat(defaulted.pluginId()).isEqualTo("robot");
    }

    @Test
    void returnsSingleProviderWhenNoTagsAreSpecified() {
        CapabilityRouter router = new CapabilityRouter(Map.of());
        MessageProvider only = new NamedProvider("only");
        List<CapabilityRegistration<MessageProvider>> registrations = List.of(
                new CapabilityRegistration<>(MESSAGE, only, "only", Tags.of("channel", "wecom")));

        CapabilityRegistration<MessageProvider> selected = router.select(MESSAGE, Tags.empty(), registrations);

        log.info("single provider fallback pluginId=%s", selected.pluginId());
        assertThat(selected.provider()).isSameAs(only);
    }

    @Test
    void returnsNullWhenNoProviderMatchesRequestedTags() {
        CapabilityRouter router = new CapabilityRouter(Map.of());
        List<CapabilityRegistration<MessageProvider>> registrations = List.of(
                registration("wecom", Tags.of("channel", "wecom")));

        CapabilityRegistration<MessageProvider> selected = router.select(
                MESSAGE, Tags.of("channel", "dingtalk"), registrations);

        log.info("no match result=%s", selected);
        assertThat(selected).isNull();
    }

    @Test
    void rejectsAmbiguousSelectionsAndInvalidDefaultRoutes() {
        CapabilityRouter router = new CapabilityRouter(Map.of());
        List<CapabilityRegistration<MessageProvider>> registrations = List.of(
                registration("one", Tags.of("channel", "wecom")),
                registration("two", Tags.of("channel", "wecom")));

        assertThatThrownBy(() -> router.select(MESSAGE, Tags.of("channel", "wecom"), registrations))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("one")
                .hasMessageContaining("two");

        CapabilityRouter ambiguousDefault = new CapabilityRouter(Map.of(
                MESSAGE.key(), Tags.of("channel", "wecom")));
        assertThatThrownBy(() -> ambiguousDefault.validateDefaults(Map.of(
                MESSAGE.key(), List.<CapabilityRegistration<?>>copyOf(registrations))))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("default route");
    }

    private static CapabilityRegistration<MessageProvider> registration(String pluginId, Tags tags) {
        return new CapabilityRegistration<>(MESSAGE, new NamedProvider(pluginId), pluginId, tags);
    }

    private interface MessageProvider extends CapabilityProvider {

        String name();
    }

    private record NamedProvider(String name) implements MessageProvider {
    }
}

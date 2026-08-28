package com.innospots.nexus.core.plugin.event;

import java.time.Instant;
import java.util.List;

import com.innospots.nexus.core.plugin.capability.CapabilityKey;

/** Plugin activation observation published after all capabilities become visible. */
public record PluginStartedEvent(
        String pluginId,
        String version,
        List<CapabilityKey> capabilities,
        Instant occurredAt
) implements PluginEvent {

    /** Defensively copies capability identities. */
    public PluginStartedEvent {
        capabilities = List.copyOf(capabilities);
    }
}

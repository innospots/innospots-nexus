package com.innospots.nexus.core.plugin.event;

import java.time.Instant;

/** Plugin stop observation published after resources are released. */
public record PluginStoppedEvent(String pluginId, Instant occurredAt) implements PluginEvent {
}

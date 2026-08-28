package com.innospots.nexus.core.plugin.event;

import java.time.Instant;

/** Sanitized plugin failure observation without Throwable or configuration values. */
public record PluginFailedEvent(
        String pluginId,
        String phase,
        String errorCode,
        Instant occurredAt
) implements PluginEvent {
}

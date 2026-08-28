package com.innospots.nexus.core.plugin.discovery;

import java.time.Instant;

import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * One discovered plugin instance paired with its cached immutable definition.
 *
 * @param plugin ServiceLoader-created plugin instance
 * @param definition definition snapshot read exactly once
 * @param discoveredAt discovery timestamp
 */
public record DiscoveredPlugin(
        Plugin plugin,
        PluginDefinition definition,
        Instant discoveredAt
) {
}

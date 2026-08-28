package com.innospots.nexus.core.plugin.discovery;

import java.time.Instant;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

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

    /** Validates the runtime instance and immutable discovery metadata. */
    public DiscoveredPlugin {
        if (plugin == null || definition == null || discoveredAt == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "discovered plugin instance, definition and timestamp are required");
        }
    }
}

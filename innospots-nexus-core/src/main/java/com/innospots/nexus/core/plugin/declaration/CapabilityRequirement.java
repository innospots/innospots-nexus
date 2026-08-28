package com.innospots.nexus.core.plugin.declaration;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Declares whether a plugin requires a logical capability to start.
 *
 * @param key required capability identity
 * @param required whether absence blocks plugin startup
 */
public record CapabilityRequirement(CapabilityKey key, boolean required) {

    /** Validates the requirement identity. */
    public CapabilityRequirement {
        if (key == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "requirement capability key is required");
        }
    }
}

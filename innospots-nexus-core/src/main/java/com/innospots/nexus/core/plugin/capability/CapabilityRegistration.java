package com.innospots.nexus.core.plugin.capability;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Immutable active provider registration published only after plugin startup succeeds.
 *
 * @param type capability identity and API
 * @param provider active provider instance
 * @param pluginId owning plugin identifier
 * @param tags routing tags inherited from the plugin
 * @param <T> provider contract type
 */
public record CapabilityRegistration<T extends CapabilityProvider>(
        CapabilityType<T> type,
        T provider,
        String pluginId,
        Tags tags
) {

    /** Validates identity, ownership, and runtime provider type. */
    public CapabilityRegistration {
        if (type == null || provider == null || pluginId == null || pluginId.isBlank() || tags == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability registration fields are required");
        }
        if (!type.api().isInstance(provider)) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "provider does not implement capability API: " + type.key());
        }
    }
}

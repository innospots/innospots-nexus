package com.innospots.nexus.core.plugin.capability;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Type-safe link between a logical capability identity and its Java API.
 *
 * @param key stable capability key
 * @param api capability contract shared by host and plugin
 * @param <T> provider contract type
 */
public record CapabilityType<T extends CapabilityProvider>(CapabilityKey key, Class<T> api) {

    /** Validates key and API contract. */
    public CapabilityType {
        if (key == null || api == null || !api.isInterface() || !CapabilityProvider.class.isAssignableFrom(api)) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "capability API must be an interface extending CapabilityProvider");
        }
    }

    /**
     * Creates a capability type.
     *
     * @param name capability name
     * @param majorVersion API major version
     * @param api shared Java API
     * @param <T> provider contract type
     * @return validated capability type
     */
    public static <T extends CapabilityProvider> CapabilityType<T> of(
            String name,
            int majorVersion,
            Class<T> api
    ) {
        return new CapabilityType<>(new CapabilityKey(name, majorVersion), api);
    }
}

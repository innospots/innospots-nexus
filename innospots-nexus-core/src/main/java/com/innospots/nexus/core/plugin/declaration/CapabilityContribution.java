package com.innospots.nexus.core.plugin.declaration;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.CapabilityProviderFactory;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Binds one capability API to the factory owned by a plugin.
 *
 * @param type declared capability API
 * @param factory side-effect-free provider factory
 * @param <T> provider contract type
 */
public record CapabilityContribution<T extends CapabilityProvider>(
        CapabilityType<T> type,
        CapabilityProviderFactory<? extends T> factory
) {

    /** Validates the contribution declaration. */
    public CapabilityContribution {
        if (type == null || factory == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "capability type and factory are required");
        }
    }
}

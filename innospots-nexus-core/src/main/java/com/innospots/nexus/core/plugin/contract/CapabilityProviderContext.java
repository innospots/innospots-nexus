package com.innospots.nexus.core.plugin.contract;

import com.innospots.nexus.core.plugin.capability.CapabilityKey;

/**
 * Plugin context specialized for one declared capability provider.
 */
public interface CapabilityProviderContext extends PluginContext {

    /**
     * Returns the capability key currently being initialized.
     *
     * @return declared capability identity
     */
    CapabilityKey capability();
}

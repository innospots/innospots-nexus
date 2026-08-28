package com.innospots.nexus.core.plugin.dependency;

import java.util.List;

import com.innospots.nexus.core.plugin.capability.CapabilityKey;

/**
 * Immutable diagnostic snapshot for one declared capability dependency.
 *
 * @param key capability dependency
 * @param required whether it blocks startup
 * @param declared whether any discovered plugin provides it
 * @param available whether an active provider currently exists
 * @param providerPluginIds discovered provider plugin identifiers
 */
public record DependencyResolution(
        CapabilityKey key,
        boolean required,
        boolean declared,
        boolean available,
        List<String> providerPluginIds
) {

    /** Defensively copies provider identifiers. */
    public DependencyResolution {
        providerPluginIds = List.copyOf(providerPluginIds);
    }
}

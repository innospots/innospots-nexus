package com.innospots.nexus.core.plugin.contract;

/**
 * Creates a fresh, not-yet-initialized capability provider without side effects.
 *
 * @param <T> provider type
 */
@FunctionalInterface
public interface CapabilityProviderFactory<T extends CapabilityProvider> {

    /**
     * Creates a new provider for one plugin start cycle.
     *
     * @return fresh, uninitialized provider instance
     */
    T create();
}

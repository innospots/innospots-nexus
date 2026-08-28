package com.innospots.nexus.core.plugin.contract;

/**
 * Marker and lifecycle contract for a runtime-managed capability implementation.
 */
public interface CapabilityProvider {

    /** Initializes the provider after its factory creates a fresh instance. */
    default void initialize(CapabilityProviderContext context) {
    }

    /** Releases provider-owned state before the plugin resource scope closes. */
    default void destroy() {
    }
}

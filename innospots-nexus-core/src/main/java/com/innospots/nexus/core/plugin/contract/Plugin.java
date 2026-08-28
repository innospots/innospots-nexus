package com.innospots.nexus.core.plugin.contract;

import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * Sole classpath SPI for declaring one plugin and its plugin-level lifecycle.
 */
public interface Plugin {

    /** Returns an immutable, side-effect-free plugin definition. */
    PluginDefinition definition();

    /** Initializes plugin-level state for one start cycle. */
    default void initialize(PluginContext context) {
    }

    /** Starts behavior after the plugin and provider initialization completes. */
    default void start() {
    }

    /** Stops plugin-level behavior before its resource scope is released. */
    default void stop() {
    }
}

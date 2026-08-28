package com.innospots.nexus.core.plugin.contract;

import com.innospots.nexus.core.plugin.capability.CapabilityManager;
import com.innospots.nexus.core.plugin.config.PluginConfig;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.event.PluginEventBus;
import com.innospots.nexus.core.plugin.resource.ResourceScope;

/**
 * Read-only runtime services exposed to one plugin start cycle.
 */
public interface PluginContext {

    /**
     * Returns the immutable definition snapshot.
     *
     * @return plugin declaration
     */
    PluginDefinition definition();

    /**
     * Returns validated configuration scoped to this plugin.
     *
     * @return plugin-local configuration view
     */
    PluginConfig config();

    /**
     * Returns the read-only capability lookup boundary.
     *
     * @return active capability lookup service
     */
    CapabilityManager capabilities();

    /**
     * Returns the event-bus view bound to this plugin resource scope.
     *
     * @return scoped event bus
     */
    PluginEventBus events();

    /**
     * Returns this start cycle's resource ownership scope.
     *
     * @return resource ownership scope
     */
    ResourceScope resources();

    /**
     * Returns a logger named for the current plugin.
     *
     * @return plugin logger
     */
    System.Logger logger();
}

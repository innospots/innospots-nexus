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

    /** Returns the immutable definition snapshot. */
    PluginDefinition definition();

    /** Returns validated configuration scoped to this plugin. */
    PluginConfig config();

    /** Returns the read-only capability lookup boundary. */
    CapabilityManager capabilities();

    /** Returns the event-bus view bound to this plugin resource scope. */
    PluginEventBus events();

    /** Returns this start cycle's resource ownership scope. */
    ResourceScope resources();

    /** Returns a logger named for the current plugin. */
    System.Logger logger();
}

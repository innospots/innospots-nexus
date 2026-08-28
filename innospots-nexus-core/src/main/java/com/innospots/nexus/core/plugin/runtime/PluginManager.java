package com.innospots.nexus.core.plugin.runtime;

import java.util.List;
import java.util.Optional;

import com.innospots.nexus.core.plugin.capability.CapabilityManager;
import com.innospots.nexus.core.plugin.lifecycle.PluginRuntimeInfo;

/**
 * Host entry point for classpath discovery, dependency-aware lifecycle control, diagnostics, and capability lookup.
 */
public interface PluginManager extends AutoCloseable {

    /**
     * Discovers once and starts every plugin whose required capabilities become available.
     *
     * @throws com.innospots.nexus.base.exception.NexusException when required plugins cannot become active
     */
    void start();

    /**
     * Starts one named plugin when all required capabilities are active.
     *
     * @param pluginId stable plugin identifier
     * @throws com.innospots.nexus.base.exception.NexusException when the plugin is unknown or
     *         dependencies are unavailable
     */
    void start(String pluginId);

    /**
     * Stops one named plugin unless doing so would remove another active plugin's last required provider.
     *
     * @param pluginId stable plugin identifier
     * @throws com.innospots.nexus.base.exception.NexusException when the plugin is in use or stopping fails
     */
    void stop(String pluginId);

    /**
     * Returns immutable runtime snapshots sorted by plugin id.
     *
     * @return immutable plugin runtime snapshots
     */
    List<PluginRuntimeInfo> plugins();

    /**
     * Finds one plugin runtime snapshot.
     *
     * @param pluginId stable plugin identifier
     * @return matching snapshot, or empty when no such plugin was discovered
     */
    Optional<PluginRuntimeInfo> plugin(String pluginId);

    /**
     * Returns the type-safe active capability lookup boundary.
     *
     * @return active capability manager
     */
    CapabilityManager capabilities();

    /** Stops all active plugins in reverse actual startup order. */
    @Override
    void close();
}

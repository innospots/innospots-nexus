package com.innospots.nexus.core.plugin.runtime;

import java.util.List;
import java.util.Optional;

import com.innospots.nexus.core.plugin.capability.CapabilityManager;
import com.innospots.nexus.core.plugin.lifecycle.PluginRuntimeInfo;

/**
 * Host entry point for classpath discovery, dependency-aware lifecycle control, diagnostics, and capability lookup.
 */
public interface PluginManager extends AutoCloseable {

    /** Discovers once and starts every plugin whose required capabilities become available. */
    void start();

    /** Starts one named plugin when all required capabilities are active. */
    void start(String pluginId);

    /** Stops one named plugin unless doing so would remove another active plugin's last required provider. */
    void stop(String pluginId);

    /** Returns immutable runtime snapshots sorted by plugin id. */
    List<PluginRuntimeInfo> plugins();

    /** Finds one plugin runtime snapshot. */
    Optional<PluginRuntimeInfo> plugin(String pluginId);

    /** Returns the type-safe active capability lookup boundary. */
    CapabilityManager capabilities();

    /** Stops all active plugins in reverse actual startup order. */
    @Override
    void close();
}

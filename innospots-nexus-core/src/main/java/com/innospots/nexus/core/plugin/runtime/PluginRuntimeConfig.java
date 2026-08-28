package com.innospots.nexus.core.plugin.runtime;

import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Immutable host configuration for one independent plugin manager instance.
 *
 * @param requiredPluginIds plugins whose activation is required for host startup
 * @param disabledPluginIds plugins excluded from automatic startup
 * @param hostConfig flattened host configuration
 * @param runtimeVariables highest-priority runtime overrides
 * @param defaultRoutes default tags by capability key
 * @param pluginClassLoader class loader used by ServiceLoader, nullable for the Core loader
 */
public record PluginRuntimeConfig(
        Set<String> requiredPluginIds,
        Set<String> disabledPluginIds,
        Map<String, String> hostConfig,
        Map<String, String> runtimeVariables,
        Map<CapabilityKey, Tags> defaultRoutes,
        ClassLoader pluginClassLoader
) {

    /** Defensively copies configuration and rejects contradictory enablement. */
    public PluginRuntimeConfig {
        requiredPluginIds = requiredPluginIds == null ? Set.of() : Set.copyOf(requiredPluginIds);
        disabledPluginIds = disabledPluginIds == null ? Set.of() : Set.copyOf(disabledPluginIds);
        hostConfig = hostConfig == null ? Map.of() : Map.copyOf(hostConfig);
        runtimeVariables = runtimeVariables == null ? Map.of() : Map.copyOf(runtimeVariables);
        defaultRoutes = defaultRoutes == null ? Map.of() : Map.copyOf(defaultRoutes);
        if (!java.util.Collections.disjoint(requiredPluginIds, disabledPluginIds)) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "required and disabled plugin ids must not overlap");
        }
    }

    /** Returns the configured class loader or the supplied runtime fallback. */
    public ClassLoader resolvedClassLoader(ClassLoader fallback) {
        return pluginClassLoader == null ? fallback : pluginClassLoader;
    }
}

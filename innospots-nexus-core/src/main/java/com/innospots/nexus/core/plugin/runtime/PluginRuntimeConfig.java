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
        requiredPluginIds = immutablePluginIds(requiredPluginIds, "required");
        disabledPluginIds = immutablePluginIds(disabledPluginIds, "disabled");
        hostConfig = immutableStringMap(hostConfig, "host configuration");
        runtimeVariables = immutableStringMap(runtimeVariables, "runtime variables");
        defaultRoutes = immutableRoutes(defaultRoutes);
        if (!java.util.Collections.disjoint(requiredPluginIds, disabledPluginIds)) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "required and disabled plugin ids must not overlap");
        }
    }

    /**
     * Returns the configured class loader or the supplied runtime fallback.
     *
     * @param fallback loader used when no plugin-specific loader was configured
     * @return effective plugin class loader
     */
    public ClassLoader resolvedClassLoader(ClassLoader fallback) {
        return pluginClassLoader == null ? fallback : pluginClassLoader;
    }

    private static Set<String> immutablePluginIds(Set<String> source, String kind) {
        if (source == null) {
            return Set.of();
        }
        for (String pluginId : source) {
            if (pluginId == null || pluginId.isBlank()) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        kind + " plugin ids must not be blank");
            }
        }
        return Set.copyOf(source);
    }

    private static Map<String, String> immutableStringMap(Map<String, String> source, String kind) {
        if (source == null) {
            return Map.of();
        }
        for (Map.Entry<String, String> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        kind + " keys and values must not be null");
            }
        }
        return Map.copyOf(source);
    }

    private static Map<CapabilityKey, Tags> immutableRoutes(Map<CapabilityKey, Tags> source) {
        if (source == null) {
            return Map.of();
        }
        for (Map.Entry<CapabilityKey, Tags> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        "default capability routes must not contain null entries");
            }
        }
        return Map.copyOf(source);
    }
}

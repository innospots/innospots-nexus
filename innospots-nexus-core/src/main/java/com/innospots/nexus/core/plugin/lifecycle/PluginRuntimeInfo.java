package com.innospots.nexus.core.plugin.lifecycle;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.declaration.CapabilityRequirement;
import com.innospots.nexus.core.plugin.dependency.DependencyResolution;

/**
 * Immutable sanitized operational snapshot that never retains runtime objects or configuration values.
 */
public record PluginRuntimeInfo(
        String id,
        String name,
        String version,
        String implementationClass,
        PluginState state,
        String phase,
        Tags tags,
        List<CapabilityKey> providedCapabilities,
        List<CapabilityRequirement> requirements,
        Map<CapabilityKey, DependencyResolution> dependencies,
        Instant discoveredAt,
        Instant startedAt,
        String lastError
) {

    /** Defensively copies every diagnostic collection. */
    public PluginRuntimeInfo {
        providedCapabilities = List.copyOf(providedCapabilities);
        requirements = List.copyOf(requirements);
        dependencies = Map.copyOf(dependencies);
    }
}

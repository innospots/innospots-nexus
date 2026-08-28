package com.innospots.nexus.core.plugin.dependency;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistry;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.CapabilityRequirement;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Resolves declared and currently available capability dependencies without binding to a concrete provider.
 */
public final class DependencyResolver {

    private final Map<CapabilityKey, List<String>> declarations;
    private final CapabilityRegistry registry;

    /** Builds a stable declaration index for the discovered plugin set. */
    public DependencyResolver(List<PluginDefinition> definitions, CapabilityRegistry registry) {
        if (definitions == null || registry == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                    "dependency definitions and registry are required");
        }
        Map<CapabilityKey, List<String>> mutable = new LinkedHashMap<>();
        for (PluginDefinition definition : definitions) {
            if (definition == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                        "dependency definition must not be null");
            }
            for (CapabilityContribution<?> contribution : definition.capabilities()) {
                mutable.computeIfAbsent(contribution.type().key(), ignored -> new ArrayList<>())
                        .add(definition.id());
            }
        }
        Map<CapabilityKey, List<String>> snapshot = new LinkedHashMap<>();
        mutable.forEach((key, value) -> snapshot.put(key, List.copyOf(value)));
        this.declarations = Map.copyOf(snapshot);
        this.registry = registry;
    }

    /**
     * Returns immutable dependency diagnostics for one plugin definition.
     *
     * @param definition plugin declaration to resolve
     * @return dependency diagnostics keyed by capability
     * @throws NexusException when the definition is missing
     */
    public Map<CapabilityKey, DependencyResolution> resolve(PluginDefinition definition) {
        if (definition == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                    "plugin definition is required for dependency resolution");
        }
        Map<CapabilityKey, DependencyResolution> result = new LinkedHashMap<>();
        for (CapabilityRequirement requirement : definition.requirements()) {
            List<String> providerIds = declarations.getOrDefault(requirement.key(), List.of());
            result.put(requirement.key(), new DependencyResolution(
                    requirement.key(),
                    requirement.required(),
                    !providerIds.isEmpty(),
                    registry.contains(requirement.key()),
                    providerIds));
        }
        return Map.copyOf(result);
    }

    /**
     * Returns whether all required dependencies currently have an active provider.
     *
     * @param resolutions dependency diagnostics for one plugin
     * @return whether every required dependency is currently available
     * @throws NexusException when the diagnostics map is missing
     */
    public boolean canStart(Map<CapabilityKey, DependencyResolution> resolutions) {
        if (resolutions == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                    "dependency resolutions are required");
        }
        return resolutions.values().stream()
                .filter(DependencyResolution::required)
                .allMatch(DependencyResolution::available);
    }
}

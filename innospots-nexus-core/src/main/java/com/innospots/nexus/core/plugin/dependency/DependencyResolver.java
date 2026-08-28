package com.innospots.nexus.core.plugin.dependency;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistry;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.CapabilityRequirement;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * Resolves declared and currently available capability dependencies without binding to a concrete provider.
 */
public final class DependencyResolver {

    private final Map<CapabilityKey, List<String>> declarations;
    private final CapabilityRegistry registry;

    /** Builds a stable declaration index for the discovered plugin set. */
    public DependencyResolver(List<PluginDefinition> definitions, CapabilityRegistry registry) {
        Map<CapabilityKey, List<String>> mutable = new LinkedHashMap<>();
        for (PluginDefinition definition : definitions) {
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

    /** Returns immutable dependency diagnostics for one plugin definition. */
    public Map<CapabilityKey, DependencyResolution> resolve(PluginDefinition definition) {
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

    /** Returns whether all required dependencies currently have an active provider. */
    public boolean canStart(Map<CapabilityKey, DependencyResolution> resolutions) {
        return resolutions.values().stream()
                .filter(DependencyResolution::required)
                .allMatch(DependencyResolution::available);
    }
}

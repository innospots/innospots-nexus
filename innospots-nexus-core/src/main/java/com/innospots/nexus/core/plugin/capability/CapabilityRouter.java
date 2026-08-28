package com.innospots.nexus.core.plugin.capability;

import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Applies explicit tags, configured defaults, and unique-provider fallback without order-based selection.
 */
public final class CapabilityRouter {

    private final Map<CapabilityKey, Tags> defaultRoutes;

    /** Creates a router with immutable default routes. */
    public CapabilityRouter(Map<CapabilityKey, Tags> defaultRoutes) {
        this.defaultRoutes = defaultRoutes == null ? Map.of() : Map.copyOf(defaultRoutes);
    }

    /** Selects zero or one provider and rejects every ambiguous candidate set. */
    public <T extends CapabilityProvider> CapabilityRegistration<T> select(
            CapabilityType<T> type,
            Tags requiredTags,
            List<CapabilityRegistration<T>> registrations
    ) {
        Tags explicit = requiredTags == null ? Tags.empty() : requiredTags;
        Tags routingTags = explicit.isEmpty()
                ? defaultRoutes.getOrDefault(type.key(), Tags.empty())
                : explicit;
        List<CapabilityRegistration<T>> matches = routingTags.isEmpty()
                ? registrations
                : registrations.stream().filter(item -> item.tags().matches(routingTags)).toList();
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) {
            String pluginIds = matches.stream()
                    .map(CapabilityRegistration::pluginId)
                    .sorted()
                    .toList()
                    .toString();
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_AMBIGUOUS,
                    "ambiguous capability " + type.key() + ", candidates=" + pluginIds);
        }
        return matches.getFirst();
    }

    void validateDefaults(Map<CapabilityKey, List<CapabilityRegistration<?>>> registrations) {
        for (Map.Entry<CapabilityKey, Tags> route : defaultRoutes.entrySet()) {
            List<CapabilityRegistration<?>> matches = registrations
                    .getOrDefault(route.getKey(), List.of())
                    .stream()
                    .filter(item -> item.tags().matches(route.getValue()))
                    .toList();
            if (matches.size() > 1) {
                throw NexusException.build(
                        PluginStatusCode.CAPABILITY_AMBIGUOUS,
                        "default route is ambiguous for " + route.getKey() + ", candidates="
                                + matches.stream().map(CapabilityRegistration::pluginId).sorted().toList());
            }
        }
    }
}

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

    /**
     * Creates a router with immutable default routes.
     *
     * @param defaultRoutes tags to use when callers omit explicit tags
     */
    public CapabilityRouter(Map<CapabilityKey, Tags> defaultRoutes) {
        if (defaultRoutes == null) {
            this.defaultRoutes = Map.of();
            return;
        }
        for (Map.Entry<CapabilityKey, Tags> route : defaultRoutes.entrySet()) {
            if (route.getKey() == null || route.getValue() == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        "default capability routes must not contain null entries");
            }
        }
        this.defaultRoutes = Map.copyOf(defaultRoutes);
    }

    /**
     * Selects zero or one provider and rejects every ambiguous candidate set.
     *
     * @param type requested capability type
     * @param requiredTags explicit routing tags; {@code null} means use the configured default
     * @param registrations active registrations for the requested capability
     * @param <T> provider contract type
     * @return the selected registration, or {@code null} when no provider matches
     * @throws NexusException when the type or registrations are invalid, or selection is ambiguous
     */
    public <T extends CapabilityProvider> CapabilityRegistration<T> select(
            CapabilityType<T> type,
            Tags requiredTags,
            List<CapabilityRegistration<T>> registrations
    ) {
        if (type == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability type must not be null");
        }
        if (registrations == null || registrations.stream().anyMatch(item -> item == null)) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability registrations must not contain null entries");
        }
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

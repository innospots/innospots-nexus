package com.innospots.nexus.core.plugin.capability;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Copy-on-write registry exposing complete immutable snapshots to concurrent readers.
 */
public final class CapabilityRegistry implements CapabilityManager {

    private final AtomicReference<Map<CapabilityKey, List<CapabilityRegistration<?>>>> snapshot =
            new AtomicReference<>(Map.of());
    private final CapabilityRouter router;

    /**
     * Creates an empty registry using the configured fallback routes.
     *
     * @param defaultRoutes tags to use when a lookup does not specify tags
     */
    public CapabilityRegistry(Map<CapabilityKey, Tags> defaultRoutes) {
        this.router = new CapabilityRouter(defaultRoutes);
    }

    /**
     * Atomically publishes all registrations supplied for a successfully started plugin.
     *
     * @param registrations provider registrations to publish as one snapshot
     * @throws NexusException when a registration is invalid or violates a configured route
     */
    public synchronized void registerAll(List<CapabilityRegistration<?>> registrations) {
        if (registrations == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability registrations must not be null");
        }
        Map<CapabilityKey, List<CapabilityRegistration<?>>> mutable = mutableCopy(snapshot.get());
        for (CapabilityRegistration<?> registration : registrations) {
            if (registration == null) {
                throw NexusException.build(
                        PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                        "capability registration must not be null");
            }
            List<CapabilityRegistration<?>> current = mutable.computeIfAbsent(
                    registration.type().key(),
                    ignored -> new ArrayList<>());
            for (CapabilityRegistration<?> existing : current) {
                if (existing.type().api() != registration.type().api()) {
                    throw NexusException.build(
                            PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                            "different API classes registered for " + registration.type().key());
                }
            }
            current.add(registration);
        }
        Map<CapabilityKey, List<CapabilityRegistration<?>>> replacement = immutableCopy(mutable);
        router.validateDefaults(replacement);
        snapshot.set(replacement);
    }

    /**
     * Atomically removes every registration owned by one plugin.
     *
     * @param pluginId stable owner identifier
     * @throws NexusException when the owner identifier is blank
     */
    public synchronized void unregisterPlugin(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "plugin id must not be blank");
        }
        Map<CapabilityKey, List<CapabilityRegistration<?>>> mutable = mutableCopy(snapshot.get());
        mutable.replaceAll((key, values) -> values.stream()
                .filter(value -> !value.pluginId().equals(pluginId))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
        mutable.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        snapshot.set(immutableCopy(mutable));
    }

    /**
     * Returns whether at least one active provider exists for the key.
     *
     * @param key logical capability identity
     * @return whether at least one provider is registered
     */
    public boolean contains(CapabilityKey key) {
        if (key == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability key must not be null");
        }
        return !snapshot.get().getOrDefault(key, List.of()).isEmpty();
    }

    @Override
    public <T extends CapabilityProvider> T require(CapabilityType<T> type, Tags requiredTags) {
        return find(type, requiredTags).orElseThrow(() -> NexusException.build(
                PluginStatusCode.CAPABILITY_NOT_FOUND,
                "capability not found: " + type.key()));
    }

    @Override
    public <T extends CapabilityProvider> Optional<T> find(CapabilityType<T> type, Tags requiredTags) {
        List<CapabilityRegistration<T>> registrations = registrations(type);
        CapabilityRegistration<T> selected = router.select(type, requiredTags, registrations);
        return selected == null ? Optional.empty() : Optional.of(selected.provider());
    }

    @Override
    public <T extends CapabilityProvider> List<T> findAll(CapabilityType<T> type) {
        return registrations(type).stream().map(CapabilityRegistration::provider).toList();
    }

    /**
     * Returns one immutable registry snapshot for dependency and diagnostic calculations.
     *
     * @return immutable capability-to-registration snapshot
     */
    public Map<CapabilityKey, List<CapabilityRegistration<?>>> snapshot() {
        return snapshot.get();
    }

    private <T extends CapabilityProvider> List<CapabilityRegistration<T>> registrations(CapabilityType<T> type) {
        if (type == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability type must not be null");
        }
        List<CapabilityRegistration<?>> raw = snapshot.get().getOrDefault(type.key(), List.of());
        List<CapabilityRegistration<T>> typed = new ArrayList<>(raw.size());
        for (CapabilityRegistration<?> registration : raw) {
            if (registration.type().api() != type.api() || !type.api().isInstance(registration.provider())) {
                throw NexusException.build(
                        PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                        "registered API does not match requested type: " + type.key());
            }
            typed.add(cast(type, registration));
        }
        return List.copyOf(typed);
    }

    private static <T extends CapabilityProvider> CapabilityRegistration<T> cast(
            CapabilityType<T> type,
            CapabilityRegistration<?> registration
    ) {
        return new CapabilityRegistration<>(
                type,
                type.api().cast(registration.provider()),
                registration.pluginId(),
                registration.tags());
    }

    private static Map<CapabilityKey, List<CapabilityRegistration<?>>> mutableCopy(
            Map<CapabilityKey, List<CapabilityRegistration<?>>> source
    ) {
        Map<CapabilityKey, List<CapabilityRegistration<?>>> copy = new LinkedHashMap<>();
        source.forEach((key, values) -> copy.put(key, new ArrayList<>(values)));
        return copy;
    }

    private static Map<CapabilityKey, List<CapabilityRegistration<?>>> immutableCopy(
            Map<CapabilityKey, List<CapabilityRegistration<?>>> source
    ) {
        Map<CapabilityKey, List<CapabilityRegistration<?>>> copy = new LinkedHashMap<>();
        source.forEach((key, values) -> copy.put(key, List.copyOf(values)));
        return Map.copyOf(copy);
    }
}

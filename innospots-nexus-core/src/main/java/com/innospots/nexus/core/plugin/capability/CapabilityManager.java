package com.innospots.nexus.core.plugin.capability;

import java.util.List;
import java.util.Optional;

import com.innospots.nexus.core.plugin.contract.CapabilityProvider;

/**
 * Type-safe read boundary for selecting active capability providers.
 */
public interface CapabilityManager {

    /**
     * Returns exactly one active provider matching the requested type and tags.
     *
     * @param type capability type including its Java API
     * @param requiredTags tags that a provider must contain; {@code null} means no explicit tags
     * @param <T> provider contract type
     * @return the selected provider
     * @throws com.innospots.nexus.base.exception.NexusException when no provider exists or selection is ambiguous
     */
    <T extends CapabilityProvider> T require(CapabilityType<T> type, Tags requiredTags);

    /**
     * Finds an active provider matching the requested type and tags.
     *
     * @param type capability type including its Java API
     * @param requiredTags tags that a provider must contain; {@code null} means no explicit tags
     * @param <T> provider contract type
     * @return the provider, or empty when no provider matches
     * @throws com.innospots.nexus.base.exception.NexusException when selection is ambiguous
     */
    <T extends CapabilityProvider> Optional<T> find(CapabilityType<T> type, Tags requiredTags);

    /**
     * Returns all active providers registered for a capability type.
     *
     * @param type capability type including its Java API
     * @param <T> provider contract type
     * @return immutable provider list in registration order
     */
    <T extends CapabilityProvider> List<T> findAll(CapabilityType<T> type);
}

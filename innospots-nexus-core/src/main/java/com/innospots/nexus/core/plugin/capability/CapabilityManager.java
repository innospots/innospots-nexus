package com.innospots.nexus.core.plugin.capability;

import java.util.List;
import java.util.Optional;

import com.innospots.nexus.core.plugin.contract.CapabilityProvider;

/**
 * Type-safe read boundary for selecting active capability providers.
 */
public interface CapabilityManager {

    /** Returns exactly one matching provider or fails with a diagnostic status. */
    <T extends CapabilityProvider> T require(CapabilityType<T> type, Tags requiredTags);

    /** Returns an optional matching provider and still rejects ambiguous matches. */
    <T extends CapabilityProvider> Optional<T> find(CapabilityType<T> type, Tags requiredTags);

    /** Returns all active providers for the capability key. */
    <T extends CapabilityProvider> List<T> findAll(CapabilityType<T> type);
}

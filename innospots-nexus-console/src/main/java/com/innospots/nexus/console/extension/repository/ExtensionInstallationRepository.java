package com.innospots.nexus.console.extension.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider;
import com.innospots.nexus.console.extension.dao.ExtensionInstallationDao;
import com.innospots.nexus.console.extension.domain.entity.ExtensionInstallationEntity;
import com.innospots.nexus.console.extension.domain.enums.ExtensionState;

/**
 * Repository for extension installation records. It owns persistence
 * coordination while the DAO remains a single-table access contract.
 */
public final class ExtensionInstallationRepository {

    private final ExtensionInstallationDao dao;

    /** Creates a repository backed by the extension installation DAO. */
    public ExtensionInstallationRepository(ExtensionInstallationDao dao) {
        if (dao == null) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "Extension installation DAO is required");
        }
        this.dao = dao;
    }

    /**
     * Registers a provider discovered through Java SPI, preserving an existing
     * enablement choice.
     * A first installation is enabled by default and enters REGISTERED state.
     *
     * @param provider discovered extension provider
     * @return persisted installation record
     */
    public ExtensionInstallationEntity register(ConsoleExtensionProvider provider) {
        validateProvider(provider);
        ExtensionInstallationEntity existing = dao.selectByExtensionKey(
                provider.descriptor().extensionKey());
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            ExtensionInstallationEntity created = new ExtensionInstallationEntity();
            applyDescriptor(created, provider, now);
            created.setEnabled(true);
            created.setState(ExtensionState.REGISTERED.name());
            dao.insert(created);
            return created;
        }

        applyDescriptor(existing, provider, now);
        existing.setState(existing.isEnabled()
                ? ExtensionState.REGISTERED.name()
                : ExtensionState.DISABLED.name());
        existing.setLastError(null);
        dao.updateById(existing);
        return existing;
    }

    /**
     * Persists a runtime lifecycle transition and its diagnostic message.
     *
     * @param extensionKey stable extension key
     * @param state new runtime state
     * @param enabled desired enablement state
     * @param errorMessage failure diagnostic, or null when successful
     * @return updated installation record
     */
    public ExtensionInstallationEntity updateState(
            String extensionKey,
            ExtensionState state,
            boolean enabled,
            String errorMessage
    ) {
        requireText(extensionKey, "extensionKey");
        if (state == null) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "state is required");
        }
        ExtensionInstallationEntity entity = requireByExtensionKey(extensionKey);
        entity.setEnabled(enabled);
        entity.setState(state.name());
        entity.setLastError(errorMessage);
        if (state == ExtensionState.ACTIVE) {
            entity.setActivatedTime(LocalDateTime.now());
        }
        if (state == ExtensionState.DISABLED) {
            entity.setDisabledTime(LocalDateTime.now());
        }
        dao.updateById(entity);
        return entity;
    }

    /** Returns all installation records, including disabled or missing extensions. */
    public List<ExtensionInstallationEntity> findAll() {
        return List.copyOf(dao.selectAll());
    }

    /**
     * Marks persisted records absent from the current discovery result as missing.
     * The enabled flag is retained because it represents the administrator's desired
     * state and must be restored when the JAR is installed again.
     *
     * @param discoveredExtensionKeys keys discovered during the current startup
     * @return records transitioned to MISSING
     */
    public List<ExtensionInstallationEntity> markMissing(
            Collection<String> discoveredExtensionKeys
    ) {
        Set<String> discoveredKeys = discoveredExtensionKeys == null
                ? Set.of()
                : new HashSet<>(discoveredExtensionKeys);
        List<ExtensionInstallationEntity> missing = new java.util.ArrayList<>();
        for (ExtensionInstallationEntity entity : dao.selectAll()) {
            if (!discoveredKeys.contains(entity.getExtensionKey())
                    && !ExtensionState.MISSING.name().equals(entity.getState())) {
                entity.setState(ExtensionState.MISSING.name());
                entity.setLastError("Extension provider was not discovered");
                dao.updateById(entity);
                missing.add(entity);
            }
        }
        return List.copyOf(missing);
    }

    /** Finds an installation record by stable extension key. */
    public Optional<ExtensionInstallationEntity> findByExtensionKey(String extensionKey) {
        return Optional.ofNullable(dao.selectByExtensionKey(extensionKey));
    }

    /** Requires an installation record to exist. */
    public ExtensionInstallationEntity requireByExtensionKey(String extensionKey) {
        ExtensionInstallationEntity entity = dao.selectByExtensionKey(extensionKey);
        if (entity == null) {
            throw NexusException.build(
                    NexusStatusCode.RESOURCE_NOT_FOUND.fullCode(),
                    "Unknown extension installation: " + extensionKey);
        }
        return entity;
    }

    private void applyDescriptor(
            ExtensionInstallationEntity target,
            ConsoleExtensionProvider provider,
            LocalDateTime discoveredTime
    ) {
        target.setExtensionKey(provider.descriptor().extensionKey());
        target.setExtensionVersion(provider.descriptor().version());
        target.setDescriptorSnapshot(Jsons.toJson(provider.descriptor()));
        target.setDiscoveredTime(discoveredTime);
    }

    private static void validateProvider(ConsoleExtensionProvider provider) {
        if (provider == null || provider.descriptor() == null) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "provider and descriptor are required");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    field + " is required");
        }
    }
}

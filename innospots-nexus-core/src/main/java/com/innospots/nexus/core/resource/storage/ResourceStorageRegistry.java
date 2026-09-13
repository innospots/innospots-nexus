package com.innospots.nexus.core.resource.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.resources.ResourceStore;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;

/**
 * Registry of {@link ResourceStore} implementations keyed by {@link ResourceStore#storeMode()}.
 */
public final class ResourceStorageRegistry {

    private final Map<String, ResourceStore> stores = new LinkedHashMap<>();
    private String defaultStoreMode;

    /**
     * Registers a storage backend.
     *
     * @param store storage implementation
     * @return this registry for chaining
     */
    public ResourceStorageRegistry register(ResourceStore store) {
        Checks.notNull(store, "store");
        Checks.notBlank(store.storeMode(), "storeMode");
        stores.put(store.storeMode(), store);
        if (defaultStoreMode == null) {
            defaultStoreMode = store.storeMode();
        }
        return this;
    }

    /**
     * Sets the default store mode used when none is requested explicitly.
     *
     * @param storeMode registered store mode
     * @return this registry for chaining
     */
    public ResourceStorageRegistry defaultStoreMode(String storeMode) {
        Checks.notBlank(storeMode, "storeMode");
        if (!stores.containsKey(storeMode)) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR, "unknown storeMode: " + storeMode);
        }
        this.defaultStoreMode = storeMode;
        return this;
    }

    /**
     * Resolves a store by mode, falling back to the default when mode is blank.
     *
     * @param storeMode optional store mode
     * @return matching store
     */
    public ResourceStore requireStore(String storeMode) {
        String mode = storeMode == null || storeMode.isBlank() ? defaultStoreMode : storeMode;
        if (mode == null) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR, "no resource store registered");
        }
        ResourceStore store = stores.get(mode);
        if (store == null) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR, "unknown storeMode: " + mode);
        }
        return store;
    }

    /**
     * Finds a registered store by mode.
     *
     * @param storeMode store mode
     * @return store when registered
     */
    public Optional<ResourceStore> findStore(String storeMode) {
        if (storeMode == null || storeMode.isBlank()) {
            return Optional.ofNullable(defaultStoreMode == null ? null : stores.get(defaultStoreMode));
        }
        return Optional.ofNullable(stores.get(storeMode));
    }
}

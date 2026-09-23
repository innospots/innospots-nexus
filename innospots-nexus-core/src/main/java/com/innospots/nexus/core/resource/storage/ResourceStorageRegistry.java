package com.innospots.nexus.core.resource.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.resources.ResourceStore;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;

/**
 * 按 {@link ResourceStore#storeMode()} 索引的 {@link ResourceStore} 实现注册表。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ResourceStore
 */
public final class ResourceStorageRegistry {

    private final Map<String, ResourceStore> stores = new LinkedHashMap<>();
    private String defaultStoreMode;

    /**
     * 注册存储后端。
     *
     * @param store 存储实现
     * @return 当前注册表，支持链式调用
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
     * 设置未显式指定时使用的默认存储模式。
     *
     * @param storeMode 已注册的存储模式
     * @return 当前注册表，支持链式调用
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
     * 按模式解析存储后端；模式为空时回退到默认值。
     *
     * @param storeMode 可选存储模式
     * @return 匹配的存储后端
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
     * 按模式查找已注册的存储后端。
     *
     * @param storeMode 存储模式
     * @return 已注册时的存储后端
     */
    public Optional<ResourceStore> findStore(String storeMode) {
        if (storeMode == null || storeMode.isBlank()) {
            return Optional.ofNullable(defaultStoreMode == null ? null : stores.get(defaultStoreMode));
        }
        return Optional.ofNullable(stores.get(storeMode));
    }
}

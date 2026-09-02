package com.innospots.nexus.core.plugin.capability;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Capability 逻辑身份与 Java API 之间的类型安全关联。
 *
 * @param key 稳定的 Capability 标识
 * @param api 宿主与插件共享的 Capability 契约
 * @param <T> Provider 契约类型
 */
public record CapabilityType<T extends CapabilityProvider>(CapabilityKey key, Class<T> api) {

    /** 校验 Capability 标识和 API 契约。 */
    public CapabilityType {
        if (key == null || api == null || !api.isInterface() || !CapabilityProvider.class.isAssignableFrom(api)) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "capability API must be an interface extending CapabilityProvider");
        }
    }

    /**
     * 创建一个 Capability 类型。
     *
     * @param name Capability 名称
     * @param majorVersion API 主版本
     * @param api 共享的 Java API
     * @param <T> Provider 契约类型
     * @return 已校验的 Capability 类型
     */
    public static <T extends CapabilityProvider> CapabilityType<T> of(
            String name,
            int majorVersion,
            Class<T> api
    ) {
        return new CapabilityType<>(new CapabilityKey(name, majorVersion), api);
    }
}

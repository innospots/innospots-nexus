package com.innospots.nexus.core.plugin.capability;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 插件启动成功后才发布的不可变 Provider 注册记录。
 *
 * @param type Capability 身份和 Java API
 * @param provider 已初始化的 Provider 实例
 * @param providerRef Provider 稳定身份
 * @param tags 合并后的路由标签
 * @param <T> Provider 契约类型
 */
public record CapabilityRegistration<T extends CapabilityProvider>(
        CapabilityType<T> type,
        T provider,
        ProviderRef providerRef,
        Tags tags
) {

    /**
     * 校验注册身份、路由标签和运行时 Provider 类型。
     *
     * @throws NexusException 注册字段缺失或 Provider 类型不匹配
     */
    public CapabilityRegistration {
        if (type == null || provider == null || providerRef == null || tags == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability registration fields are required");
        }
        if (!type.api().isInstance(provider)) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "provider does not implement capability API: " + type.key());
        }
    }

    /**
     * 兼容旧的构造顺序，新的调用方应直接传入 {@link ProviderRef}。
     *
     * @param type Capability 身份和 Java API
     * @param provider Provider 实例
     * @param pluginId 所属插件标识
     * @param tags 合并后的路由标签
     */
    public CapabilityRegistration(
            CapabilityType<T> type,
            T provider,
            String pluginId,
            Tags tags
    ) {
        this(type, provider, new ProviderRef(pluginId, defaultProviderId(type)), tags);
    }

    /** 返回注册所属插件标识，便于运行时索引按插件撤销。 */
    public String pluginId() {
        return providerRef.pluginId();
    }

    private static String defaultProviderId(CapabilityType<?> type) {
        if (type == null) {
            return "";
        }
        String name = type.key().name();
        int separator = name.lastIndexOf('.');
        return separator < 0 ? name : name.substring(separator + 1);
    }
}

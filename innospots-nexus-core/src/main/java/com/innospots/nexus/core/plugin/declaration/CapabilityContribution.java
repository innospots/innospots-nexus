package com.innospots.nexus.core.plugin.declaration;

import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigDefinition;
import com.innospots.nexus.core.plugin.config.ConfigItemDefinition;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.CapabilityProviderFactory;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 将一个 Capability API、Provider 身份、路由标签和配置绑定到插件工厂。
 *
 * @param type Capability API 类型
 * @param providerId 插件内全局唯一的 Provider 标识
 * @param tags Provider 专属路由标签
 * @param config Provider 专属配置定义
 * @param factory 不应产生副作用的 Provider 工厂
 * @param <T> Provider 契约类型
 */
public record CapabilityContribution<T extends CapabilityProvider>(
        CapabilityType<T> type,
        String providerId,
        Tags tags,
        ConfigDefinition config,
        CapabilityProviderFactory<? extends T> factory
) {

    /**
     * 校验并快照 Capability 声明，确保配置集合不会被调用方后续修改。
     *
     * @throws NexusException 声明字段缺失或配置定义无效
     */
    public CapabilityContribution {
        if (type == null || factory == null || providerId == null || providerId.isBlank()) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "capability type, providerId and factory are required");
        }
        if (tags == null || config == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "capability tags and config are required");
        }
        config = snapshotConfig(config);
    }

    /**
     * 兼容仅提供一个工厂的 Java SPI 声明，Provider 标识取 Capability 名称末段。
     *
     * @param type Capability API 类型
     * @param factory Provider 工厂
     */
    public CapabilityContribution(
            CapabilityType<T> type,
            CapabilityProviderFactory<? extends T> factory
    ) {
        this(type, defaultProviderId(type), Tags.empty(), ConfigDefinition.empty(), factory);
    }

    private static String defaultProviderId(CapabilityType<?> type) {
        if (type == null) {
            return "";
        }
        String name = type.key().name();
        int separator = name.lastIndexOf('.');
        return separator < 0 ? name : name.substring(separator + 1);
    }

    private static ConfigDefinition snapshotConfig(ConfigDefinition source) {
        List<ConfigItemDefinition> items = List.copyOf(source.items());
        return () -> items;
    }
}

package com.innospots.nexus.core.plugin.dependency;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistry;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.CapabilityRequirement;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 解析已声明且当前可用的 Capability 依赖，不绑定到具体 Provider 实例。
 */
public final class DependencyResolver {

    private final Map<CapabilityKey, List<ProviderDeclaration>> declarations;
    private final CapabilityRegistry registry;

    /**
     * 为已发现插件集合构建稳定的依赖声明索引。
     *
     * @param definitions 已发现插件的不可变定义列表
     * @param registry 当前活动 Capability 注册表
     * @throws NexusException 定义列表、注册表或其元素为空时抛出
     */
    public DependencyResolver(List<PluginDefinition> definitions, CapabilityRegistry registry) {
        if (definitions == null || registry == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                    "dependency definitions and registry are required");
        }
        Map<CapabilityKey, List<ProviderDeclaration>> mutable = new LinkedHashMap<>();
        for (PluginDefinition definition : definitions) {
            if (definition == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                        "dependency definition must not be null");
            }
            for (CapabilityContribution<?> contribution : definition.capabilities()) {
                mutable.computeIfAbsent(contribution.type().key(), ignored -> new ArrayList<>())
                        .add(new ProviderDeclaration(
                                definition.pluginId(),
                                Tags.merge(definition.tags(), contribution.tags())));
            }
        }
        Map<CapabilityKey, List<ProviderDeclaration>> snapshot = new LinkedHashMap<>();
        mutable.forEach((key, value) -> snapshot.put(key, List.copyOf(value)));
        this.declarations = Map.copyOf(snapshot);
        this.registry = registry;
    }

    /**
     * 返回一个插件定义的不可变依赖诊断。
     *
     * @param definition 待解析的插件声明
     * @return 以 Capability 为键的依赖诊断
     * @throws NexusException 插件定义缺失时抛出
     */
    public Map<CapabilityKey, DependencyResolution> resolve(PluginDefinition definition) {
        if (definition == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                    "plugin definition is required for dependency resolution");
        }
        Map<CapabilityKey, DependencyResolution> result = new LinkedHashMap<>();
        for (CapabilityRequirement requirement : definition.requirements()) {
            List<String> providerIds = declarations.getOrDefault(requirement.key(), List.of()).stream()
                    .filter(provider -> provider.tags().matches(requirement.requiredTags()))
                    .map(ProviderDeclaration::pluginId)
                    .toList();
            result.put(requirement.key(), new DependencyResolution(
                    requirement.key(),
                    requirement.requiredTags(),
                    requirement.required(),
                    !providerIds.isEmpty(),
                    registry.contains(requirement.key(), requirement.requiredTags()),
                    providerIds));
        }
        return Map.copyOf(result);
    }

    /**
     * 返回所有必需依赖当前是否都有活动 Provider。
     *
     * @param resolutions 一个插件的依赖诊断
     * @return 每项必需依赖是否都可用
     * @throws NexusException 依赖诊断映射缺失时抛出
     */
    public boolean canStart(Map<CapabilityKey, DependencyResolution> resolutions) {
        if (resolutions == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                    "dependency resolutions are required");
        }
        return resolutions.values().stream()
                .filter(DependencyResolution::required)
                .allMatch(DependencyResolution::available);
    }

    private record ProviderDeclaration(String pluginId, Tags tags) {
    }
}

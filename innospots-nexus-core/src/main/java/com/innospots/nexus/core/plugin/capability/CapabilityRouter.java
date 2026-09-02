package com.innospots.nexus.core.plugin.capability;

import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 应用显式标签、配置默认路由和唯一 Provider 回退，不依赖注册顺序选择 Provider。
 */
public final class CapabilityRouter {

    private final Map<CapabilityKey, Tags> defaultRoutes;

    /**
     * 使用不可变默认路由创建路由器。
     *
     * @param defaultRoutes 调用方未提供显式标签时使用的路由标签
     */
    public CapabilityRouter(Map<CapabilityKey, Tags> defaultRoutes) {
        if (defaultRoutes == null) {
            this.defaultRoutes = Map.of();
            return;
        }
        for (Map.Entry<CapabilityKey, Tags> route : defaultRoutes.entrySet()) {
            if (route.getKey() == null || route.getValue() == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        "default capability routes must not contain null entries");
            }
        }
        this.defaultRoutes = Map.copyOf(defaultRoutes);
    }

    /**
     * 选择零个或一个 Provider，并拒绝所有存在歧义的候选集合。
     *
     * @param type 请求的 Capability 类型
     * @param requiredTags 显式路由标签；{@code null} 表示使用配置的默认路由
     * @param registrations 请求 Capability 的活动注册记录
     * @param <T> Provider 契约类型
     * @return 选中的注册记录；没有匹配 Provider 时返回 {@code null}
     * @throws NexusException 类型或注册记录非法，或选择结果存在歧义时抛出
     */
    public <T extends CapabilityProvider> CapabilityRegistration<T> select(
            CapabilityType<T> type,
            Tags requiredTags,
            List<CapabilityRegistration<T>> registrations
    ) {
        if (type == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability type must not be null");
        }
        if (registrations == null || registrations.stream().anyMatch(item -> item == null)) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability registrations must not contain null entries");
        }
        Tags explicit = requiredTags == null ? Tags.empty() : requiredTags;
        Tags routingTags = explicit.isEmpty()
                ? defaultRoutes.getOrDefault(type.key(), Tags.empty())
                : explicit;
        List<CapabilityRegistration<T>> matches = routingTags.isEmpty()
                ? registrations
                : registrations.stream().filter(item -> item.tags().matches(routingTags)).toList();
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) {
            String pluginIds = matches.stream()
                    .map(CapabilityRegistration::pluginId)
                    .sorted()
                    .toList()
                    .toString();
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_AMBIGUOUS,
                    "ambiguous capability " + type.key() + ", candidates=" + pluginIds);
        }
        return matches.getFirst();
    }

    void validateDefaults(Map<CapabilityKey, List<CapabilityRegistration<?>>> registrations) {
        for (Map.Entry<CapabilityKey, Tags> route : defaultRoutes.entrySet()) {
            List<CapabilityRegistration<?>> matches = registrations
                    .getOrDefault(route.getKey(), List.of())
                    .stream()
                    .filter(item -> item.tags().matches(route.getValue()))
                    .toList();
            if (matches.size() > 1) {
                throw NexusException.build(
                        PluginStatusCode.CAPABILITY_AMBIGUOUS,
                        "default route is ambiguous for " + route.getKey() + ", candidates="
                                + matches.stream().map(CapabilityRegistration::pluginId).sorted().toList());
            }
        }
    }
}

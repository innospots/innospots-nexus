package com.innospots.nexus.core.plugin.lifecycle;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 将插件标识映射到运行时可用性门控，供 Capability 查询过滤未激活 Provider。
 */
public final class PluginAvailabilityIndex {

    private final ConcurrentMap<String, PluginAvailability> availabilities = new ConcurrentHashMap<>();

    /**
     * 登记一个插件的可用性门控。
     *
     * @param pluginId     稳定的插件标识
     * @param availability 插件运行时门控
     * @throws NexusException 参数为空或重复登记时
     */
    public void register(String pluginId, PluginAvailability availability) {
        if (pluginId == null || pluginId.isBlank() || availability == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "plugin availability registration requires pluginId and availability");
        }
        PluginAvailability previous = availabilities.putIfAbsent(pluginId, availability);
        if (previous != null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONCURRENCY_CONFLICT,
                    "plugin availability already registered: " + pluginId);
        }
    }

    /**
     * 移除一个插件的可用性门控。
     *
     * @param pluginId 稳定的插件标识
     */
    public void unregister(String pluginId) {
        if (pluginId != null && !pluginId.isBlank()) {
            availabilities.remove(pluginId);
        }
    }

    /**
     * 判断指定插件的 Capability 是否对外可见。
     *
     * @param pluginId 稳定的插件标识
     * @return 门控已激活时 {@code true}；未登记或门控未激活时 {@code false}
     */
    public boolean isVisible(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            return false;
        }
        PluginAvailability availability = availabilities.get(pluginId);
        return availability != null && availability.isActive();
    }
}

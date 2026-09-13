package com.innospots.nexus.core.plugin.discovery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 一个插件运行时使用的不可变发现快照。
 *
 * <p>目录创建后只包含静态元数据，但其中的插件实例属于消费它的运行时，不能由多个管理器共享。</p>
 */
public final class PluginCatalog {

    private final List<DiscoveredPlugin> plugins;
    private final Map<String, DiscoveredPlugin> byId;

    private PluginCatalog(List<DiscoveredPlugin> plugins) {
        if (plugins == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "plugin catalog entries must not be null");
        }
        List<DiscoveredPlugin> copied = new ArrayList<>(plugins.size());
        for (DiscoveredPlugin plugin : plugins) {
            if (plugin == null || plugin.definition() == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                        "plugin catalog contains an invalid entry");
            }
            copied.add(plugin);
        }
        copied.sort(Comparator.comparing(item -> item.definition().pluginId()));
        validateGlobalConstraints(copied);
        this.plugins = List.copyOf(copied);
        Map<String, DiscoveredPlugin> index = new LinkedHashMap<>();
        for (DiscoveredPlugin plugin : this.plugins) {
            index.put(plugin.definition().pluginId(), plugin);
        }
        this.byId = Map.copyOf(index);
    }

    /**
     * 根据发现列表创建目录，并执行全局 pluginId 与 Capability API 冲突校验。
     *
     * @param plugins 已发现的插件实例和定义
     * @return 按插件标识排序的不可变目录
     */
    public static PluginCatalog of(List<DiscoveredPlugin> plugins) {
        return new PluginCatalog(plugins);
    }

    /** 返回按插件标识确定性排序的不可变发现列表。 */
    public List<DiscoveredPlugin> plugins() {
        return plugins;
    }

    /**
     * 按稳定标识查找一个已发现插件。
     *
     * @param pluginId 稳定的插件标识
     * @return 匹配的已发现插件；未找到时返回空 Optional
     */
    public Optional<DiscoveredPlugin> plugin(String pluginId) {
        return Optional.ofNullable(byId.get(pluginId));
    }

    /**
     * 返回用于诊断和预检校验的不可变插件定义快照。
     *
     * @return 按发现顺序排列的插件定义列表
     */
    public List<PluginDefinition> definitions() {
        return plugins.stream().map(DiscoveredPlugin::definition).toList();
    }

    private static void validateGlobalConstraints(List<DiscoveredPlugin> discovered) {
        Set<String> pluginIds = new HashSet<>();
        Map<CapabilityKey, Class<?>> capabilityApis = new HashMap<>();
        for (DiscoveredPlugin item : discovered) {
            PluginDefinition definition = item.definition();
            if (!pluginIds.add(definition.pluginId())) {
                throw NexusException.build(PluginStatusCode.PLUGIN_DUPLICATE,
                        "duplicate plugin id: " + definition.pluginId());
            }
            if (definition.apiVersion() != PluginDefinition.CURRENT_API_VERSION) {
                throw NexusException.build(PluginStatusCode.PLUGIN_API_INCOMPATIBLE,
                        "unsupported plugin apiVersion for " + definition.pluginId() + ": " + definition.apiVersion());
            }
            for (CapabilityContribution<?> contribution : definition.capabilities()) {
                // 同一 CapabilityKey 在全局只能绑定一种 API 接口，保证 require(name, version) 可解析。
                Class<?> previous = capabilityApis.putIfAbsent(contribution.type().key(), contribution.type().api());
                if (previous != null && previous != contribution.type().api()) {
                    throw NexusException.build(PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                            "different API classes declared for " + contribution.type().key());
                }
            }
        }
    }
}

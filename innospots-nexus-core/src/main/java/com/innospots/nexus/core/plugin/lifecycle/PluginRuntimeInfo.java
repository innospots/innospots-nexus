package com.innospots.nexus.core.plugin.lifecycle;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.declaration.CapabilityRequirement;
import com.innospots.nexus.core.plugin.dependency.DependencyResolution;

/**
 * 不可变且已脱敏的运行快照，不保留运行时对象或配置值。
 */
public record PluginRuntimeInfo(
        String id,
        String name,
        String version,
        String implementationClass,
        PluginState state,
        String phase,
        Tags tags,
        List<CapabilityKey> providedCapabilities,
        List<CapabilityRequirement> requirements,
        Map<CapabilityKey, DependencyResolution> dependencies,
        Instant discoveredAt,
        Instant startedAt,
        String lastError
) {

    /** 返回语义更明确的插件稳定身份别名。 */
    public String pluginId() {
        return id;
    }

    /** 返回插件展示名称别名。 */
    public String displayName() {
        return name;
    }

    /** 防御性复制所有诊断集合。 */
    public PluginRuntimeInfo {
        providedCapabilities = List.copyOf(providedCapabilities);
        requirements = List.copyOf(requirements);
        dependencies = Map.copyOf(dependencies);
    }
}

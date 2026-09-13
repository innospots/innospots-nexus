package com.innospots.nexus.core.plugin.dependency;

import java.util.List;

import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.Tags;

/**
 * 一条 Capability 依赖声明的不可变诊断快照。
 *
 * @param key Capability 依赖身份
 * @param requiredTags 依赖要求的 Provider 标签
 * @param required 是否阻断插件启动
 * @param declared 已发现插件中是否有提供者
 * @param available 当前是否存在活动 Provider
 * @param providerPluginIds 已发现的 Provider 所属插件标识
 */
public record DependencyResolution(
        CapabilityKey key,
        Tags requiredTags,
        boolean required,
        boolean declared,
        boolean available,
        List<String> providerPluginIds
) {

    /** 防御性复制 Provider 标识集合。 */
    public DependencyResolution {
        requiredTags = requiredTags == null ? Tags.empty() : requiredTags;
        providerPluginIds = List.copyOf(providerPluginIds);
    }

    /** 兼容不带标签约束的旧诊断构造形式。 */
    public DependencyResolution(
            CapabilityKey key,
            boolean required,
            boolean declared,
            boolean available,
            List<String> providerPluginIds
    ) {
        this(key, Tags.empty(), required, declared, available, providerPluginIds);
    }
}

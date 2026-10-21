package com.innospots.nexus.core.plugin.lifecycle;

import java.time.Instant;

import com.innospots.nexus.core.plugin.declaration.Tags;

/**
 * 不可变且已脱敏的运行快照，不保留运行时对象或配置值。
 *
 * @author Smars
 * @date 2026/09/13
 */
public record PluginRuntimeInfo(
        String id,
        String name,
        String version,
        String implementationClass,
        PluginState state,
        String phase,
        Tags tags,
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
}

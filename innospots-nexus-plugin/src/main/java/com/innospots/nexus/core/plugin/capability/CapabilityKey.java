package com.innospots.nexus.core.plugin.capability;

import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Capability API 主版本的稳定逻辑身份。
 *
 * @param name 小写点分 Capability 名称
 * @param majorVersion 正整数形式的 API 主版本
 */
public record CapabilityKey(String name, int majorVersion) {

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:[.-][a-z0-9]+)*");

    /**
     * @throws NexusException 名称格式非法或主版本小于 1 时抛出
     */
    public CapabilityKey {
        if (name == null || !NAME_PATTERN.matcher(name).matches() || majorVersion < 1) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "invalid capability key: " + name + "@" + majorVersion);
        }
    }

    @Override
    public String toString() {
        return name + "@" + majorVersion;
    }
}

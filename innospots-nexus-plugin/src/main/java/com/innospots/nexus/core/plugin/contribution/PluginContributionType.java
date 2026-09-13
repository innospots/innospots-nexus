package com.innospots.nexus.core.plugin.contribution;

import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Contribution 的稳定类型标识，例如 {@code console@1}。
 *
 * @param name 小写点分类型名称
 * @param majorVersion 正整数形式的类型主版本
 * @param <T> 该类型对应的 Contribution 接口
 */
public record PluginContributionType<T extends PluginContribution>(
        String name,
        int majorVersion
) {

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:[.-][a-z0-9]+)*");

    /**
     * @throws NexusException 名称格式非法或主版本小于 1 时抛出
     */
    public PluginContributionType {
        if (name == null || !NAME_PATTERN.matcher(name).matches() || majorVersion < 1) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "invalid plugin contribution type: " + name + "@" + majorVersion);
        }
    }

    /** 返回人类可读的 type@major 标识。 */
    @Override
    public String toString() {
        return name + "@" + majorVersion;
    }
}

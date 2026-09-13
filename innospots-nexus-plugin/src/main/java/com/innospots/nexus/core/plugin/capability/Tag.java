package com.innospots.nexus.core.plugin.capability;

import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 一个不可变的路由标签属性。
 *
 * @param name 标签名称
 * @param value 标签值
 */
public record Tag(String name, String value) {

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:[.-][a-z0-9]+)*");

    /**
     * @throws NexusException 名称格式非法、值为空或超出长度限制时抛出
     */
    public Tag {
        if (name == null || value == null || name.length() > 64 || value.length() > 64
                || value.isBlank() || !NAME_PATTERN.matcher(name).matches()) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "tag name must use lowercase dotted or kebab-case and tag value must be non-blank");
        }
    }
}

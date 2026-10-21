package com.innospots.nexus.core.plugin.declaration;

import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Contribution 等扩展条目的稳定身份，由所属插件标识和插件内唯一编号组成。
 *
 * @param pluginId 所属插件的反向域名标识
 * @param providerId 插件内全局唯一的条目标识
 * @author Smars
 * @date 2026/09/13
 */
public record ProviderRef(String pluginId, String providerId) {

    private static final Pattern PLUGIN_ID_PATTERN = Pattern.compile(
            "[a-z][a-z0-9]*(?:-[a-z0-9]+)*(?:\\.[a-z][a-z0-9]*(?:-[a-z0-9]+)*)+");
    private static final Pattern PROVIDER_ID_PATTERN = Pattern.compile(
            "[a-z][a-z0-9]*(?:-[a-z0-9]+)*");

    /**
     * 校验插件和条目的稳定身份格式。
     *
     * @throws NexusException 身份为空或不符合 DSL 标识规则
     */
    public ProviderRef {
        if (pluginId == null || !PLUGIN_ID_PATTERN.matcher(pluginId).matches()) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "pluginId must use reverse-domain format: " + pluginId);
        }
        if (providerId == null || !PROVIDER_ID_PATTERN.matcher(providerId).matches()) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "providerId must use lowercase kebab-case: " + providerId);
        }
    }
}

package com.innospots.nexus.core.plugin.contribution;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.config.PluginConfig;
import com.innospots.nexus.core.plugin.lifecycle.PluginAvailability;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Contribution Handler 使用的只读插件上下文。
 *
 * @param owner 声明 Contribution 的插件身份
 * @param config 插件共享只读配置
 * @param availability 插件与 Contribution 共用的可用性门控
 */
public record PluginContributionContext(
        ProviderRef owner,
        PluginConfig config,
        PluginAvailability availability
) {

    /**
     * @throws NexusException 身份、配置或可用性门控为空时抛出
     */
    public PluginContributionContext {
        if (owner == null || config == null || availability == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "contribution owner and config are required");
        }
    }

    /**
     * 兼容不需要可用性门控的通用 Handler 测试和适配器。
     *
     * @param owner 声明 Contribution 的插件身份
     * @param config 插件共享只读配置
     */
    public PluginContributionContext(ProviderRef owner, PluginConfig config) {
        this(owner, config, new PluginAvailability());
    }
}

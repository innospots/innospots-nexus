package com.innospots.nexus.core.plugin.contribution;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 将 Contribution 与声明它的插件绑定，供 Handler 做全局校验。
 *
 * @param owner 声明该 Contribution 的插件 Provider 身份
 * @param contribution 已解码的 Contribution 值
 */
public record PluginContributionEntry<T extends PluginContribution>(ProviderRef owner, T contribution) {

    /**
     * @throws NexusException 归属或声明为空时抛出
     */
    public PluginContributionEntry {
        if (owner == null || contribution == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "contribution owner and value are required");
        }
    }
}

package com.innospots.nexus.core.plugin.declaration;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 声明插件所需的 Capability 及其路由标签。
 *
 * @param key Capability 逻辑身份
 * @param requiredTags Provider 必须包含的标签子集
 * @param required 缺少匹配 Provider 时是否阻止启动
 */
public record CapabilityRequirement(CapabilityKey key, Tags requiredTags, boolean required) {

    /**
     * 校验 Capability 身份并将空标签归一化为不可变空集合。
     *
     * @throws NexusException Capability 身份为空
     */
    public CapabilityRequirement {
        if (key == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "requirement capability key is required");
        }
        requiredTags = requiredTags == null ? Tags.empty() : requiredTags;
    }

    /**
     * 创建无标签要求的兼容构造形式。
     *
     * @param key Capability 逻辑身份
     * @param required 是否为必需依赖
     */
    public CapabilityRequirement(CapabilityKey key, boolean required) {
        this(key, Tags.empty(), required);
    }
}

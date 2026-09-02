package com.innospots.nexus.core.plugin.discovery;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** 将 YAML 编译定义适配为唯一 Plugin SPI，避免创建第二套运行时。 */
public final class ManifestPlugin implements Plugin {

    private final PluginDefinition definition;

    /** 创建 YAML 插件适配器。 */
    public ManifestPlugin(PluginDefinition definition) {
        if (definition == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "manifest plugin definition is required");
        }
        this.definition = definition;
    }

    @Override
    public PluginDefinition definition() {
        return definition;
    }
}

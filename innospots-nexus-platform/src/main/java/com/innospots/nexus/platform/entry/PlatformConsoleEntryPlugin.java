package com.innospots.nexus.platform.entry;

import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 运营管理平台管理能力聚合 entry 插件（组织、平台用户、注册策略等 MODULE 与 MENU）。
 */
public final class PlatformConsoleEntryPlugin implements Plugin {

    private static final PlatformEntryPluginDescriptor DESCRIPTOR =
            PlatformEntryPluginDescriptor.platformConsole();

    @Override
    public PluginDefinition definition() {
        return PlatformModuleEntrySupport.definition(DESCRIPTOR);
    }
}

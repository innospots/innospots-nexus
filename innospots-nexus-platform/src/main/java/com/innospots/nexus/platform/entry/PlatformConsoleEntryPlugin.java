package com.innospots.nexus.platform.entry;

import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 运营管理平台管理能力聚合 entry 插件（租户、平台用户、注册策略等 PageDsl 与菜单）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.plugin.contract.Plugin
 * @see com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins#PLATFORM_CONSOLE
 */
public final class PlatformConsoleEntryPlugin implements Plugin {

    private static final PlatformEntryPluginDescriptor DESCRIPTOR = PlatformEntryPluginDescriptor.platformConsole();

    /**
     * SPI 入口：返回 platform console 聚合插件定义。
     *
     * @return {@link BuiltinPlatformEntryPlugins#PLATFORM_CONSOLE} 对应定义
     */
    @Override
    public PluginDefinition definition() {
        return PlatformModuleEntrySupport.definition(DESCRIPTOR);
    }
}

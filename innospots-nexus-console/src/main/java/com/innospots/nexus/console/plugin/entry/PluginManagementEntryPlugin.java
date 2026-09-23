package com.innospots.nexus.console.plugin.entry;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleEntrySupport;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 贡献插件管理主页面的内置 entry 插件。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class PluginManagementEntryPlugin implements Plugin {

    private static final String MODULE_KEY = "plugin";
    private static final String PAGE_KEY = ConsoleModuleDescriptor.mainPageKey(MODULE_KEY);

    private static final ConsoleModuleDescriptor DESCRIPTOR = new ConsoleModuleDescriptor(
            "com.innospots.nexus.console.plugin-management",
            MODULE_KEY,
            PAGE_KEY,
            "/console/plugins",
            PAGE_KEY,
            "appstore",
            60,
            I18nObject.of("en", "Plugins", "zh", "插件"),
            I18nObject.of("en", "Console plugin management.", "zh", "控制台插件管理。"),
            I18nObject.of("en", "Plugin Management", "zh", "插件管理"));
    /**
     * 返回插件定义。
     * @return 操作结果
     */

    
    @Override
    public PluginDefinition definition() {
        return ConsoleModuleEntrySupport.definition(DESCRIPTOR);
    }
}

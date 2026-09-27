package com.innospots.nexus.console.menu.entry;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.BuiltinConsoleEntryPlugins;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleEntrySupport;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 贡献菜单管理主页面的内置 entry 插件。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class MenuEntryPlugin implements Plugin {

    private static final String DOMAIN_KEY = ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY;
    private static final String ENTRY_PAGE_KEY = "menu-main";

    private static final ConsoleModuleDescriptor DESCRIPTOR = ConsoleModuleDescriptor.builtin(
            BuiltinConsoleEntryPlugins.MENU,
            DOMAIN_KEY,
            "menu",
            ENTRY_PAGE_KEY,
            "menu",
            10,
            I18nObject.of("en", "Menu", "zh", "菜单"),
            I18nObject.of("en", "Console navigation menu management.", "zh", "控制台导航菜单管理。"),
            I18nObject.of("en", "Menu Management", "zh", "菜单管理"));

    @Override
    public PluginDefinition definition() {
        return ConsoleModuleEntrySupport.definition(DESCRIPTOR);
    }
}

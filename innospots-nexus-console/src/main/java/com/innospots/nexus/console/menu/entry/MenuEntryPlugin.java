package com.innospots.nexus.console.menu.entry;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleEntrySupport;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * Built-in entry plugin that contributes the menu management main page.
 */
public final class MenuEntryPlugin implements Plugin {

    private static final String MODULE_KEY = "menu";
    private static final String PAGE_KEY = ConsoleModuleDescriptor.mainPageKey(MODULE_KEY);

    private static final ConsoleModuleDescriptor DESCRIPTOR = new ConsoleModuleDescriptor(
            "com.innospots.nexus.console.menu",
            MODULE_KEY,
            PAGE_KEY,
            "/console/menu",
            PAGE_KEY,
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

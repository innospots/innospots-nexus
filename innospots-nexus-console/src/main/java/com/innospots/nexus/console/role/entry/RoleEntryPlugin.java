package com.innospots.nexus.console.role.entry;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleEntrySupport;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * Built-in entry plugin that contributes the role management main page.
 */
public final class RoleEntryPlugin implements Plugin {

    private static final String MODULE_KEY = "role";
    private static final String PAGE_KEY = ConsoleModuleDescriptor.mainPageKey(MODULE_KEY);

    private static final ConsoleModuleDescriptor DESCRIPTOR = new ConsoleModuleDescriptor(
            "com.innospots.nexus.console.role",
            MODULE_KEY,
            PAGE_KEY,
            "/console/role",
            PAGE_KEY,
            "team",
            30,
            I18nObject.of("en", "Role", "zh", "角色"),
            I18nObject.of("en", "Console role management.", "zh", "控制台角色管理。"),
            I18nObject.of("en", "Role Management", "zh", "角色管理"));

    @Override
    public PluginDefinition definition() {
        return ConsoleModuleEntrySupport.definition(DESCRIPTOR);
    }
}

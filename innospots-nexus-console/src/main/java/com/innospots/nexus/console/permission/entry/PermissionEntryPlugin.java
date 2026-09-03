package com.innospots.nexus.console.permission.entry;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleEntrySupport;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * Built-in entry plugin that contributes the permission management main page.
 */
public final class PermissionEntryPlugin implements Plugin {

    private static final String MODULE_KEY = "permission";
    private static final String PAGE_KEY = ConsoleModuleDescriptor.mainPageKey(MODULE_KEY);

    private static final ConsoleModuleDescriptor DESCRIPTOR = new ConsoleModuleDescriptor(
            "com.innospots.nexus.console.permission",
            MODULE_KEY,
            PAGE_KEY,
            "/console/permission",
            PAGE_KEY,
            "lock",
            40,
            I18nObject.of("en", "Permission", "zh", "权限"),
            I18nObject.of("en", "Console permission management.", "zh", "控制台权限管理。"),
            I18nObject.of("en", "Permission Management", "zh", "权限管理"));

    @Override
    public PluginDefinition definition() {
        return ConsoleModuleEntrySupport.definition(DESCRIPTOR);
    }
}

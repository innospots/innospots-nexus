package com.innospots.nexus.console.permission.entry;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.BuiltinConsoleEntryPlugins;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleEntrySupport;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 贡献权限管理主页面的内置 entry 插件。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class PermissionEntryPlugin implements Plugin {

    private static final ConsoleModuleDescriptor DESCRIPTOR = ConsoleModuleDescriptor.builtin(
            BuiltinConsoleEntryPlugins.PERMISSION,
            "permission",
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

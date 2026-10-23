package com.innospots.nexus.console.plugin.entry;

import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.BuiltinConsoleEntryPlugins;
import com.innospots.nexus.console.entry.ConsolePageRoutes;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributions;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 贡献插件管理主页面的内置 entry 插件。
 */
public final class PluginManagementEntryPlugin implements Plugin {

    private static final String DOMAIN_KEY = ConsolePageRoutes.BUILTIN_DOMAIN_KEY;
    private static final String ENTRY_PAGE_KEY = "nexus-plugin-main";

    private static final PluginDefinition DEFINITION = ConsoleContributions.entryPlugin(
            BuiltinConsoleEntryPlugins.PLUGIN_MANAGEMENT,
            BuiltinConsoleEntryPlugins.pluginVersion(),
            ConsoleContributions.discoveryTags(
                    BuiltinConsoleEntryPlugins.PLUGIN_MANAGEMENT,
                    DOMAIN_KEY,
                    List.of("plugin")),
            I18nObject.of("en", "Plugins", "zh", "插件"),
            I18nObject.of("en", "Console plugin management.", "zh", "控制台插件管理。"),
            DOMAIN_KEY,
            List.of(ConsoleContributeNode.singlePageModule(
                    "plugin",
                    60,
                    I18nObject.of("en", "Plugins", "zh", "插件"),
                    I18nObject.of("en", "Console plugin management.", "zh", "控制台插件管理。"),
                    ENTRY_PAGE_KEY,
                    "appstore")));

    @Override
    public PluginDefinition definition() {
        return DEFINITION;
    }
}

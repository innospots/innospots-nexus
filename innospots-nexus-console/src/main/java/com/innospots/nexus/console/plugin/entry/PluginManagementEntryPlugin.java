package com.innospots.nexus.console.plugin.entry;

import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.BuiltinConsoleEntryPlugins;
import com.innospots.nexus.console.entry.ConsolePageRoutes;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributions;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 贡献插件管理主页面的内置 entry 插件。
 */
public final class PluginManagementEntryPlugin implements Plugin {

    private static final String DOMAIN_KEY = ConsolePageRoutes.SYSTEM_DOMAIN_KEY;
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
            List.of(new ConsoleContributeNode(
                    "plugin",
                    I18nObject.of("en", "Plugins", "zh", "插件"),
                    I18nObject.of("en", "Console plugin management.", "zh", "控制台插件管理。"),
                    60,
                    ENTRY_PAGE_KEY,
                    "appstore",
                    List.of(new ConsolePermissionGroupDeclaration(
                            "view",
                            I18nObject.of("en", "Browse plugin pages", "zh", "浏览插件页面"))),
                    // 插件管理端点的 grant group 为 operate / execute / status，
                    // 必须在 MODULE 上声明，否则目录不会输出对应操作项。
                    List.of(
                            new ConsolePermissionGroupDeclaration(
                                    "operate",
                                    I18nObject.of("en", "Operations", "zh", "操作")),
                            new ConsolePermissionGroupDeclaration(
                                    "execute",
                                    I18nObject.of("en", "Execute", "zh", "执行操作")),
                            new ConsolePermissionGroupDeclaration(
                                    "status",
                                    I18nObject.of("en", "Status change", "zh", "状态变更"))),
                    List.of())));

    @Override
    public PluginDefinition definition() {
        return DEFINITION;
    }
}

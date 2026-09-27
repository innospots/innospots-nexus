package com.innospots.nexus.console.logger.entry;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.BuiltinConsoleEntryPlugins;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleEntrySupport;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 贡献审计日志管理主页面的内置 entry 插件。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class LoggerEntryPlugin implements Plugin {

    private static final String DOMAIN_KEY = ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY;
    private static final String ENTRY_PAGE_KEY = "logger-main";

    private static final ConsoleModuleDescriptor DESCRIPTOR = ConsoleModuleDescriptor.builtin(
            BuiltinConsoleEntryPlugins.LOGGER,
            DOMAIN_KEY,
            "logger",
            ENTRY_PAGE_KEY,
            "file-text",
            50,
            I18nObject.of("en", "Logger", "zh", "日志"),
            I18nObject.of("en", "Console audit log management.", "zh", "控制台审计日志管理。"),
            I18nObject.of("en", "Audit Logs", "zh", "审计日志"));

    @Override
    public PluginDefinition definition() {
        return ConsoleModuleEntrySupport.definition(DESCRIPTOR);
    }
}

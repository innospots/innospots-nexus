package com.innospots.nexus.console.logger.entry;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleEntrySupport;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * Built-in entry plugin that contributes the audit log management main page.
 */
public final class LoggerEntryPlugin implements Plugin {

    private static final String MODULE_KEY = "logger";
    private static final String PAGE_KEY = ConsoleModuleDescriptor.mainPageKey(MODULE_KEY);

    private static final ConsoleModuleDescriptor DESCRIPTOR = new ConsoleModuleDescriptor(
            "com.innospots.nexus.console.logger",
            MODULE_KEY,
            PAGE_KEY,
            "/console/logger",
            PAGE_KEY,
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

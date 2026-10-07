package com.innospots.nexus.console.dictionary.entry;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.BuiltinConsoleEntryPlugins;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleEntrySupport;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 贡献字典管理主页面的内置 entry 插件。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class DictionaryEntryPlugin implements Plugin {

    private static final String DOMAIN_KEY = ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY;
    private static final String ENTRY_PAGE_KEY =
            ConsoleModuleDescriptor.compositePageKey(DOMAIN_KEY, "dictionary", "main");

    private static final ConsoleModuleDescriptor DESCRIPTOR = ConsoleModuleDescriptor.builtin(
            BuiltinConsoleEntryPlugins.DICTIONARY,
            DOMAIN_KEY,
            "dictionary",
            ENTRY_PAGE_KEY,
            "book",
            20,
            I18nObject.of("en", "Dictionary", "zh", "字典"),
            I18nObject.of("en", "Console dictionary management.", "zh", "控制台字典管理。"),
            I18nObject.of("en", "Dictionary Management", "zh", "字典管理"));

    @Override
    public PluginDefinition definition() {
        return ConsoleModuleEntrySupport.definition(DESCRIPTOR);
    }
}

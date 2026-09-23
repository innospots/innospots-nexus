package com.innospots.nexus.console.dictionary.entry;

import com.innospots.nexus.base.i18n.I18nObject;
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

    private static final String MODULE_KEY = "dictionary";
    private static final String PAGE_KEY = ConsoleModuleDescriptor.mainPageKey(MODULE_KEY);

    private static final ConsoleModuleDescriptor DESCRIPTOR = new ConsoleModuleDescriptor(
            "com.innospots.nexus.console.dictionary",
            MODULE_KEY,
            PAGE_KEY,
            "/console/dictionary",
            PAGE_KEY,
            "book",
            20,
            I18nObject.of("en", "Dictionary", "zh", "字典"),
            I18nObject.of("en", "Console dictionary management.", "zh", "控制台字典管理。"),
            I18nObject.of("en", "Dictionary Management", "zh", "字典管理"));
    /**
     * 返回插件定义。
     * @return 操作结果
     */

    
    @Override
    public PluginDefinition definition() {
        return ConsoleModuleEntrySupport.definition(DESCRIPTOR);
    }
}

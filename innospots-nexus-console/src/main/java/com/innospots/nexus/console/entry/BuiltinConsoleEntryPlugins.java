package com.innospots.nexus.console.entry;

import java.util.List;

/**
 * 内置控制台 entry 插件身份常量。
 */
public final class BuiltinConsoleEntryPlugins {

    public static final String MENU = "com.innospots.nexus.console.menu";
    public static final String DICTIONARY = "com.innospots.nexus.console.dictionary";
    public static final String LOGGER = "com.innospots.nexus.console.logger";
    public static final String PERMISSION = "com.innospots.nexus.console.permission";
    public static final String ROLE = "com.innospots.nexus.console.role";
    public static final String PLUGIN_MANAGEMENT = "com.innospots.nexus.console.plugin-management";

    public static final List<String> REQUIRED_PLUGIN_IDS = List.of(
            MENU,
            DICTIONARY,
            LOGGER,
            PERMISSION,
            ROLE,
            PLUGIN_MANAGEMENT);

    private BuiltinConsoleEntryPlugins() {
    }
}

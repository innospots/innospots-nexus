package com.innospots.nexus.console.entry;

import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributions;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.declaration.Tags;

/**
 * {@code innospots-nexus-console} 模块内置 entry 插件 ID 与构件版本。
 */
public final class BuiltinConsoleEntryPlugins {

    public static final String PLUGIN_MANAGEMENT = "com.innospots.nexus.console.plugin-management";

    /** 宿主应启用的本模块内置 entry 插件 ID。 */
    public static final java.util.List<String> REQUIRED_PLUGIN_IDS = java.util.List.of(PLUGIN_MANAGEMENT);

    static final String DEVELOPMENT_PLUGIN_VERSION = "0.0.0-dev";

    private BuiltinConsoleEntryPlugins() {
    }

    public static String pluginVersion() {
        Package pkg = BuiltinConsoleEntryPlugins.class.getPackage();
        if (pkg == null) {
            return DEVELOPMENT_PLUGIN_VERSION;
        }
        String version = pkg.getImplementationVersion();
        if (version == null || version.isBlank()) {
            return DEVELOPMENT_PLUGIN_VERSION;
        }
        return version;
    }

    public static Tags tagsFor(String pluginId, ConsolePluginContribution contribution) {
        return ConsoleContributions.discoveryTags(pluginId, contribution);
    }
}

package com.innospots.nexus.platform.entry;

import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributions;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 将 {@link PlatformEntryPluginDescriptor} 组装为 console@2 {@link PluginDefinition}。
 */
public final class PlatformModuleEntrySupport {

    private PlatformModuleEntrySupport() {
    }

    public static PluginDefinition definition(PlatformEntryPluginDescriptor entry) {
        return ConsoleContributions.entryPlugin(
                entry.pluginId(),
                BuiltinPlatformEntryPlugins.pluginVersion(),
                BuiltinPlatformEntryPlugins.discoveryTags(entry.modules()),
                entry.displayName(),
                entry.description(),
                BuiltinPlatformEntryPlugins.DOMAIN_KEY,
                entry.modules());
    }
}

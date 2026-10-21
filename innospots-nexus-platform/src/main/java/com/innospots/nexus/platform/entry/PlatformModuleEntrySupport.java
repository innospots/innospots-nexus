package com.innospots.nexus.platform.entry;

import java.util.ArrayList;
import java.util.List;

import com.innospots.nexus.console.entry.ConsoleEntryPluginDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleEntrySupport;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 将 {@link PlatformEntryPluginDescriptor} 组装为 console@2 插件定义。
 */
public final class PlatformModuleEntrySupport {

    private PlatformModuleEntrySupport() {
    }

    public static PluginDefinition definition(PlatformEntryPluginDescriptor entry) {
        ConsoleEntryPluginDescriptor consoleEntry = new ConsoleEntryPluginDescriptor(
                entry.pluginId(),
                entry.displayName(),
                entry.description(),
                List.copyOf(entry.modules()));
        PluginDefinition consoleDefinition = ConsoleModuleEntrySupport.definition(consoleEntry);
        PluginDefinition.Builder builder = PluginDefinition.builder(entry.pluginId())
                .displayName(entry.displayName())
                .description(entry.description())
                .version(BuiltinPlatformEntryPlugins.pluginVersion())
                .tags(BuiltinPlatformEntryPlugins.tagsFor(entry));
        for (PluginContribution contribution : consoleDefinition.contributions()) {
            builder.contribute(contribution);
        }
        return builder.build();
    }
}

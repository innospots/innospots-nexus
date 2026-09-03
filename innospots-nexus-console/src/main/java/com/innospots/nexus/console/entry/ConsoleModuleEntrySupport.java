package com.innospots.nexus.console.entry;

import java.util.List;
import java.util.Map;

import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleModuleDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contribution.console.MenuDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.UiSpecPageDeclaration;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * Shared assembly helpers for built-in console module entry plugins.
 */
public final class ConsoleModuleEntrySupport {

    private ConsoleModuleEntrySupport() {
    }

    /**
     * Builds a contribution-only plugin definition for one console main page.
     *
     * @param descriptor built-in module metadata
     * @return immutable plugin definition
     */
    public static PluginDefinition definition(ConsoleModuleDescriptor descriptor) {
        return PluginDefinition.builder(descriptor.pluginId())
                .displayName(descriptor.displayName())
                .description(descriptor.description())
                .version("1.0.0")
                .tags(Tags.from(Map.of("scope", "console", "module", descriptor.moduleKey())))
                .contribute(consoleContribution(descriptor))
                .build();
    }

    private static ConsolePluginContribution consoleContribution(ConsoleModuleDescriptor descriptor) {
        return new ConsolePluginContribution(List.of(new ConsoleModuleDeclaration(
                descriptor.moduleKey(),
                descriptor.displayName(),
                descriptor.description(),
                List.of(new UiSpecPageDeclaration(descriptor.pageKey(), descriptor.pagePath(), List.of())),
                List.of(MenuDeclaration.page(
                        descriptor.menuKey(),
                        descriptor.pageTitle(),
                        descriptor.menuIcon(),
                        descriptor.orderIndex(),
                        descriptor.pageKey())))));
    }
}

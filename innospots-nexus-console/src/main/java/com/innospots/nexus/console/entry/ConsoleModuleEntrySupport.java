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
 * 内置控制台模块 entry 插件的共享组装辅助工具。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class ConsoleModuleEntrySupport {

    private ConsoleModuleEntrySupport() {
    }

    /**
     * 为单个控制台主页面构建仅贡献型插件定义。
     *
     * @param descriptor 内置模块元数据
     * @return immutable 插件定义
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

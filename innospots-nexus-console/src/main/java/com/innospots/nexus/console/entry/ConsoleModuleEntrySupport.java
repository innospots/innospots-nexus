package com.innospots.nexus.console.entry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.innospots.nexus.core.plugin.contribution.console.ConsoleModuleDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contribution.console.MenuDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.UiSpecPageDeclaration;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 内置控制台 entry 插件的共享组装辅助工具。
 *
 * <p>支持单模块或多模块、每模块多页面与多菜单入口，统一产出 {@code console@1} 贡献。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class ConsoleModuleEntrySupport {

    private ConsoleModuleEntrySupport() {
    }

    /**
     * 为单个控制台模块构建仅贡献型插件定义。
     *
     * @param descriptor 内置模块元数据
     * @return immutable 插件定义
     */
    public static PluginDefinition definition(ConsoleModuleDescriptor descriptor) {
        return definition(ConsoleEntryPluginDescriptor.of(descriptor));
    }

    /**
     * 为一个或多个控制台模块构建仅贡献型插件定义。
     *
     * @param entry entry 插件元数据（可含多个 {@link ConsoleModuleDescriptor})
     * @return immutable 插件定义
     */
    public static PluginDefinition definition(ConsoleEntryPluginDescriptor entry) {
        return PluginDefinition.builder(entry.pluginId())
                .displayName(entry.displayName())
                .description(entry.description())
                .version(BuiltinConsoleEntryPlugins.pluginVersion())
                .tags(BuiltinConsoleEntryPlugins.tagsFor(entry))
                .contribute(consoleContribution(entry))
                .build();
    }

    private static ConsolePluginContribution consoleContribution(ConsoleEntryPluginDescriptor entry) {
        List<ConsoleModuleDeclaration> modules = new ArrayList<>();
        for (ConsoleModuleDescriptor descriptor : entry.modules()) {
            modules.add(moduleDeclaration(descriptor));
        }
        return new ConsolePluginContribution(List.copyOf(modules));
    }

    private static ConsoleModuleDeclaration moduleDeclaration(ConsoleModuleDescriptor descriptor) {
        return new ConsoleModuleDeclaration(
                descriptor.domainKey(),
                descriptor.moduleKey(),
                descriptor.displayName(),
                descriptor.description(),
                buildPageDeclarations(descriptor),
                buildMenuTree(descriptor));
    }

    private static List<MenuDeclaration> buildMenuTree(ConsoleModuleDescriptor descriptor) {
        if (!descriptor.menuEntries().isEmpty()) {
            List<ConsoleMenuItemDescriptor> items = new ArrayList<>(descriptor.menuEntries());
            items.sort(Comparator.comparingInt(ConsoleMenuItemDescriptor::orderIndex));
            List<MenuDeclaration> menus = new ArrayList<>();
            for (ConsoleMenuItemDescriptor item : items) {
                menus.add(MenuDeclaration.page(
                        item.menuKey(),
                        item.title(),
                        item.icon(),
                        item.orderIndex(),
                        item.pageKey()));
            }
            return List.copyOf(menus);
        }
        return List.of(MenuDeclaration.page(
                descriptor.menuKey(),
                descriptor.pageTitle(),
                descriptor.menuIcon(),
                descriptor.orderIndex(),
                descriptor.entryPageKey()));
    }

    private static List<UiSpecPageDeclaration> buildPageDeclarations(ConsoleModuleDescriptor descriptor) {
        List<UiSpecPageDeclaration> pages = new ArrayList<>();
        for (String pageKey : descriptor.allPageKeys()) {
            pages.add(new UiSpecPageDeclaration(
                    pageKey,
                    ConsoleModuleDescriptor.pagePath(descriptor.domainKey(), descriptor.moduleKey(), pageKey),
                    List.of()));
        }
        return List.copyOf(pages);
    }
}

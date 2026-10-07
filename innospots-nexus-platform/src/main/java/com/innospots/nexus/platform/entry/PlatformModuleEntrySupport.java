package com.innospots.nexus.platform.entry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.innospots.nexus.console.entry.ConsoleMenuItemDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleModuleDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contribution.console.MenuDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.UiSpecPageDeclaration;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 将 {@link PlatformEntryPluginDescriptor} 组装为 {@code console@1} 插件定义。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.console.entry.ConsoleModuleEntrySupport
 */
public final class PlatformModuleEntrySupport {

    private PlatformModuleEntrySupport() {
    }

    /**
     * 由 descriptor 构建完整 {@link PluginDefinition}（模块、菜单树、UiSpec 页路径）。
     *
     * @param entry 聚合 entry 描述
     * @return 可注册的插件定义
     */
    public static PluginDefinition definition(PlatformEntryPluginDescriptor entry) {
        return PluginDefinition.builder(entry.pluginId())
                .displayName(entry.displayName())
                .description(entry.description())
                .version(BuiltinPlatformEntryPlugins.pluginVersion())
                .tags(BuiltinPlatformEntryPlugins.tagsFor(entry))
                .contribute(consoleContribution(entry))
                .build();
    }

    private static ConsolePluginContribution consoleContribution(PlatformEntryPluginDescriptor entry) {
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
            // 多菜单模块：按 orderIndex 排序后逐条挂 Page 菜单
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
        // 单入口模块：沿用 module 级 menuKey / entryPageKey
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

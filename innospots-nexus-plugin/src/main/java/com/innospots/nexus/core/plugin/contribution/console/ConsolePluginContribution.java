package com.innospots.nexus.core.plugin.contribution.console;

import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.contribution.PluginContributionInventory;
import com.innospots.nexus.core.plugin.contribution.PluginContributionType;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * console@1 管理模块、页面和菜单的静态资源贡献。
 * @author Smars
 * @date 2026/09/13
 */
public record ConsolePluginContribution(List<ConsoleModuleDeclaration> modules)
        implements PluginContribution, PluginContributionInventory {

    /** Console Contribution 的稳定类型。 */
    public static final PluginContributionType<ConsolePluginContribution> TYPE =
            new PluginContributionType<>("console", 1);

    /** 防御性复制模块列表。 */
    public ConsolePluginContribution {
        modules = modules == null ? List.of() : List.copyOf(modules);
        if (modules.isEmpty()) {
            throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT,
                    "console contribution must declare at least one module");
        }
    }

    /** 返回 console@1 类型。 */
    @Override
    public PluginContributionType<ConsolePluginContribution> type() {
        return TYPE;
    }

    /** 返回声明的管理模块数量。 */
    @Override
    public int moduleCount() {
        return modules.size();
    }

    /** 返回全部模块内页面节点与菜单节点数量之和。 */
    @Override
    public int componentCount() {
        int total = 0;
        for (ConsoleModuleDeclaration module : modules) {
            total += countPages(module.pages());
            total += countMenus(module.menuTree());
        }
        return total;
    }

    private static int countPages(List<UiSpecPageDeclaration> pages) {
        int total = 0;
        for (UiSpecPageDeclaration page : pages) {
            total += 1;
            total += countPages(page.children());
        }
        return total;
    }

    private static int countMenus(List<MenuDeclaration> menus) {
        int total = 0;
        for (MenuDeclaration menu : menus) {
            total += 1;
            total += countMenus(menu.children());
        }
        return total;
    }
}

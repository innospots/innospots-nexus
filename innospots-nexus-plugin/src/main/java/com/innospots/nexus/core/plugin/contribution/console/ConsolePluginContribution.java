package com.innospots.nexus.core.plugin.contribution.console;

import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.contribution.PluginContributionInventory;
import com.innospots.nexus.core.plugin.contribution.PluginContributionType;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * console@2 管理模块与菜单树的静态资源贡献。
 */
public record ConsolePluginContribution(
        String domainKey,
        I18nObject displayName,
        I18nObject description,
        int orderIndex,
        List<ConsoleContributeNode> modules
) implements PluginContribution, PluginContributionInventory {

    /** Console Contribution 的稳定类型。 */
    public static final PluginContributionType<ConsolePluginContribution> TYPE =
            new PluginContributionType<>("console", 2);

    public ConsolePluginContribution {
        domainKey = ConsoleContributeNode.requireKey(domainKey, "domainKey");
        displayName = ConsoleI18n.copy(
                displayName, false, "displayName", PluginStatusCode.RESOURCE_CONFLICT);
        description = ConsoleI18n.copy(
                description, false, "description", PluginStatusCode.RESOURCE_CONFLICT);
        modules = modules == null ? List.of() : List.copyOf(modules);
        if (modules.isEmpty()) {
            throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT,
                    "console contribution must declare at least one module in modules[]");
        }
        for (ConsoleContributeNode module : modules) {
            if (!module.isModule()) {
                throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT,
                        "console contribution modules[] must contain MODULE nodes only");
            }
        }
    }

    @Override
    public PluginContributionType<ConsolePluginContribution> type() {
        return TYPE;
    }

    @Override
    public int moduleCount() {
        return modules.size();
    }

    @Override
    public int componentCount() {
        int total = 0;
        for (ConsoleContributeNode module : modules) {
            total += countNodes(module.children());
        }
        return total;
    }

    private static int countNodes(List<ConsoleContributeNode> nodes) {
        int total = 0;
        for (ConsoleContributeNode node : nodes) {
            total += 1;
            if (node.isModule()) {
                total += countNodes(node.children());
            }
        }
        return total;
    }
}

package com.innospots.nexus.core.plugin.contribution.console;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotter;

/**
 * Console@2 稳定身份快照器。
 */
public final class ConsolePluginContributionSnapshotter
        implements PluginContributionSnapshotter<ConsolePluginContribution> {

    @Override
    public com.innospots.nexus.core.plugin.contribution.PluginContributionType<ConsolePluginContribution> type() {
        return ConsolePluginContribution.TYPE;
    }

    @Override
    public Map<String, Object> snapshot(ConsolePluginContribution contribution) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("type", ConsolePluginContribution.TYPE.toString());
        root.put("domainKey", contribution.domainKey());
        root.put("modules", contribution.modules().stream().map(ConsolePluginContributionSnapshotter::node).toList());
        return Map.copyOf(root);
    }

    private static Map<String, Object> node(ConsoleContributeNode node) {
        Map<String, Object> value = new LinkedHashMap<>();
        if (node.isModule()) {
            value.put("moduleKey", node.moduleKey());
            value.put("resourceKey", node.moduleResourceKey());
        }
        if (node.entryPageKey() != null && !node.entryPageKey().isBlank()) {
            value.put("entryPageKey", node.entryPageKey());
        }
        if (node.icon() != null && !node.icon().isBlank()) {
            value.put("icon", node.icon());
        }
        value.put("children", node.children().stream().map(ConsolePluginContributionSnapshotter::node).toList());
        return Map.copyOf(value);
    }
}

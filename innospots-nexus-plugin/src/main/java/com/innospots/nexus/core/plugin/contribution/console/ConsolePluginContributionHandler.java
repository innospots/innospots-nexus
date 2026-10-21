package com.innospots.nexus.core.plugin.contribution.console;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contribution.PluginContributionContext;
import com.innospots.nexus.core.plugin.contribution.PluginContributionEntry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionType;
import com.innospots.nexus.core.plugin.contribution.PreparedPluginContribution;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Console Contribution 的全局资源校验器和活动目录事务适配器（console@2）。
 */
public final class ConsolePluginContributionHandler
        implements PluginContributionHandler<ConsolePluginContribution> {

    private final ConsoleContributionCatalog catalog;
    private final ReservedPluginResourceCatalog reservedResources;

    public ConsolePluginContributionHandler(
            ConsoleContributionCatalog catalog,
            ReservedPluginResourceCatalog reservedResources
    ) {
        if (catalog == null || reservedResources == null) {
            throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT,
                    "console contribution catalogs are required");
        }
        this.catalog = catalog;
        this.reservedResources = reservedResources;
    }

    @Override
    public PluginContributionType<ConsolePluginContribution> type() {
        return ConsolePluginContribution.TYPE;
    }

    @Override
    public void validate(
            PluginCatalog pluginCatalog,
            List<PluginContributionEntry<ConsolePluginContribution>> entries
    ) {
        if (entries == null) {
            invalid("console contribution entries are required");
        }
        Map<String, String> resourceOwners = new HashMap<>();
        for (PluginContributionEntry<ConsolePluginContribution> entry : entries) {
            if (entry == null) {
                invalid("console contribution entry must not be null");
            }
            String owner = entry.owner().pluginId();
            ConsolePluginContribution contribution = entry.contribution();
            for (ConsoleContributeNode module : contribution.modules()) {
                validateModuleTree(owner, module, resourceOwners, new HashSet<>());
            }
        }
    }

    @Override
    public PreparedPluginContribution prepare(
            PluginContributionContext context,
            ConsolePluginContribution contribution
    ) {
        if (context == null || contribution == null) {
            invalid("console contribution context and value are required");
        }
        validate(null, List.of(new PluginContributionEntry<>(context.owner(), contribution)));
        return catalog.prepare(context.owner(), contribution, context.availability());
    }

    public ConsoleContributionCatalog catalog() {
        return catalog;
    }

    private void validateModuleTree(
            String owner,
            ConsoleContributeNode module,
            Map<String, String> resourceOwners,
            Set<String> moduleKeysInPlugin
    ) {
        if (!module.isModule()) {
            invalid("expected MODULE node");
        }
        if (!moduleKeysInPlugin.add(module.moduleKey())) {
            invalid("duplicate moduleKey in plugin: " + module.moduleKey());
        }
        claim(resourceOwners, owner, "MODULE", module.moduleResourceKey());
        Set<String> entryPageKeys = new HashSet<>();
        for (ConsoleContributeNode child : module.children()) {
            if (child.isMenu()) {
                validateMenu(owner, module, child, resourceOwners, entryPageKeys);
            } else if (child.isModule()) {
                validateModuleTree(owner, child, resourceOwners, moduleKeysInPlugin);
            } else {
                invalid("illegal child under MODULE " + module.moduleKey());
            }
        }
    }

    private void validateMenu(
            String owner,
            ConsoleContributeNode module,
            ConsoleContributeNode menu,
            Map<String, String> resourceOwners,
            Set<String> entryPageKeys
    ) {
        if (!entryPageKeys.add(menu.entryPageKey())) {
            invalid("duplicate entryPageKey in module " + module.moduleKey() + ": " + menu.entryPageKey());
        }
        claim(resourceOwners, owner, "MENU", menu.menuResourceKey(module.moduleKey()));
    }

    private void claim(Map<String, String> resourceOwners, String owner, String type, String resourceKey) {
        String reservedOwner = reservedResources.ownerOf(type, resourceKey).orElse(null);
        if (reservedOwner != null && !reservedOwner.equals(owner)) {
            invalid("resource identity is reserved by another plugin: " + resourceKey);
        }
        String identity = type + ":" + resourceKey;
        String previous = resourceOwners.putIfAbsent(identity, owner);
        if (previous != null && !previous.equals(owner)) {
            invalid("resource identity is owned by another plugin: " + resourceKey);
        }
        if (previous != null) {
            invalid("duplicate resource identity: " + resourceKey);
        }
    }

    private static void invalid(String message) {
        throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT, message);
    }
}

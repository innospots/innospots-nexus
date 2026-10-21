package com.innospots.nexus.console.entry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 内置控制台 entry 插件的共享组装辅助工具（console@2）。
 */
public final class ConsoleModuleEntrySupport {

    private static final List<ConsolePermissionGroupDeclaration> DEFAULT_VIEW_GROUPS = List.of(
            new ConsolePermissionGroupDeclaration(
                    "default",
                    I18nObject.of(Map.of("zh-CN", "页面访问", "en", "Page access"))));
    private static final List<ConsolePermissionGroupDeclaration> DEFAULT_OPERATION_GROUPS = List.of(
            new ConsolePermissionGroupDeclaration(
                    "default",
                    I18nObject.of(Map.of("zh-CN", "操作", "en", "Operations"))));

    private ConsoleModuleEntrySupport() {
    }

    public static PluginDefinition definition(ConsoleModuleDescriptor descriptor) {
        return definition(ConsoleEntryPluginDescriptor.of(descriptor));
    }

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
        String domainKey = entry.modules().isEmpty()
                ? ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY
                : entry.modules().getFirst().domainKey();
        List<ConsoleContributeNode> modules = new ArrayList<>();
        for (ConsoleModuleDescriptor descriptor : entry.modules()) {
            modules.add(moduleNode(descriptor));
        }
        return new ConsolePluginContribution(
                domainKey,
                entry.displayName(),
                entry.description(),
                0,
                List.copyOf(modules));
    }

    private static ConsoleContributeNode moduleNode(ConsoleModuleDescriptor descriptor) {
        List<ConsoleContributeNode> children = new ArrayList<>();
        if (!descriptor.menuEntries().isEmpty()) {
            List<ConsoleMenuItemDescriptor> items = new ArrayList<>(descriptor.menuEntries());
            items.sort(Comparator.comparingInt(ConsoleMenuItemDescriptor::orderIndex));
            for (ConsoleMenuItemDescriptor item : items) {
                children.add(menuNode(item));
            }
        } else {
            children.add(new ConsoleContributeNode(
                    null,
                    descriptor.pageTitle(),
                    I18nObject.of(Map.of()),
                    descriptor.orderIndex(),
                    descriptor.entryPageKey(),
                    descriptor.menuIcon(),
                    "default",
                    null,
                    List.of(),
                    List.of(),
                    List.of()));
        }
        return new ConsoleContributeNode(
                descriptor.moduleKey(),
                descriptor.displayName(),
                descriptor.description(),
                descriptor.orderIndex(),
                null,
                null,
                null,
                null,
                DEFAULT_VIEW_GROUPS,
                DEFAULT_OPERATION_GROUPS,
                List.copyOf(children));
    }

    private static ConsoleContributeNode menuNode(ConsoleMenuItemDescriptor item) {
        return new ConsoleContributeNode(
                null,
                item.title(),
                I18nObject.of(Map.of()),
                item.orderIndex(),
                item.pageKey(),
                item.icon(),
                "default",
                null,
                List.of(),
                List.of(),
                List.of());
    }
}

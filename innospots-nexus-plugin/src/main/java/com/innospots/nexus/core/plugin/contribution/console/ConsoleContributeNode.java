package com.innospots.nexus.core.plugin.contribution.console;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * console@2 递归贡献节点（MODULE 或 MENU，由 {@link #entryPageKey()} 与 {@link #children()} 推导）。
 *
 * <p>MENU 访问授权名在 catalog 组装时恒为 {@link #entryPageKey()}；页面访问分组由 Page DSL {@code grantGroup}
 * 与 MODULE {@link #viewGroups()} 对齐（见 plugin-extensions-v2-design §6.8）。</p>
 */
public record ConsoleContributeNode(
        String moduleKey,
        I18nObject displayName,
        I18nObject description,
        int orderIndex,
        String entryPageKey,
        String icon,
        List<ConsolePermissionGroupDeclaration> viewGroups,
        List<ConsolePermissionGroupDeclaration> operationGroups,
        List<ConsoleContributeNode> children
) {

    /** MODULE {@code viewGroups} / {@code operationGroups} 须包含的分组键。 */
    public static final String DEFAULT_VIEW_GROUP = "default";

    private static final Pattern KEY_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:-[a-z0-9]+)*");
    private static final Pattern GROUP_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:-[a-z0-9]+)*");

    public ConsoleContributeNode {
        List<ConsoleContributeNode> normalizedChildren =
                children == null ? List.of() : List.copyOf(children);
        children = normalizedChildren;
        viewGroups = viewGroups == null ? List.of() : List.copyOf(viewGroups);
        operationGroups = operationGroups == null ? List.of() : List.copyOf(operationGroups);
        displayName = ConsoleI18n.copy(
                displayName, true, "displayName", PluginStatusCode.RESOURCE_CONFLICT);
        description = ConsoleI18n.copy(
                description, false, "description", PluginStatusCode.RESOURCE_CONFLICT);
        boolean moduleNode = !hasText(entryPageKey) && !normalizedChildren.isEmpty();
        boolean menuNode = hasText(entryPageKey) && normalizedChildren.isEmpty();
        if (moduleNode) {
            moduleKey = requireKey(moduleKey, "moduleKey");
            if (hasText(entryPageKey)) {
                invalid("MODULE node must not set entryPageKey");
            }
            if (viewGroups.isEmpty()) {
                viewGroups = ConsolePermissionDefaults.defaultViewGroups();
            }
            if (operationGroups.isEmpty()) {
                operationGroups = ConsolePermissionDefaults.defaultOperationGroups();
            }
            requireDefaultGroup(viewGroups, "viewGroups");
            requireDefaultGroup(operationGroups, "operationGroups");
        } else if (menuNode) {
            if (moduleKey != null) {
                invalid("MENU node must not set moduleKey");
            }
            entryPageKey = requireKey(entryPageKey, "entryPageKey");
        } else {
            invalid("illegal node: need MODULE (empty entry + children) or MENU (entry + no children)");
        }
    }

    public boolean isModule() {
        return !hasText(entryPageKey) && children != null && !children.isEmpty();
    }

    public boolean isMenu() {
        return hasText(entryPageKey) && (children == null || children.isEmpty());
    }

    public String menuResourceKey(String ownerModuleKey) {
        return "menu:" + ownerModuleKey + "." + entryPageKey;
    }

    public String moduleResourceKey() {
        return "module:" + moduleKey;
    }

    public static ConsoleContributeNode module(
            String moduleKey,
            int orderIndex,
            I18nObject displayName,
            I18nObject description,
            List<ConsolePermissionGroupDeclaration> viewGroups,
            List<ConsolePermissionGroupDeclaration> operationGroups,
            List<ConsoleContributeNode> children
    ) {
        List<ConsoleContributeNode> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(ConsoleContributeNode::orderIndex));
        return new ConsoleContributeNode(
                moduleKey,
                displayName,
                description,
                orderIndex,
                null,
                null,
                defaultGroups(viewGroups, ConsolePermissionDefaults.defaultViewGroups()),
                defaultGroups(operationGroups, ConsolePermissionDefaults.defaultOperationGroups()),
                List.copyOf(sorted));
    }

    public static ConsoleContributeNode menu(
            I18nObject displayName,
            I18nObject description,
            int orderIndex,
            String entryPageKey,
            String icon
    ) {
        return new ConsoleContributeNode(
                null,
                displayName,
                description,
                orderIndex,
                entryPageKey,
                icon,
                List.of(),
                List.of(),
                List.of());
    }

    public static ConsoleContributeNode menu(
            I18nObject displayName,
            int orderIndex,
            String entryPageKey,
            String icon
    ) {
        return menu(displayName, I18nObject.of(Map.of()), orderIndex, entryPageKey, icon);
    }

    public static ConsoleContributeNode singlePageModule(
            String moduleKey,
            int orderIndex,
            I18nObject displayName,
            I18nObject description,
            String entryPageKey,
            String icon
    ) {
        return module(
                moduleKey,
                orderIndex,
                displayName,
                description,
                ConsolePermissionDefaults.defaultViewGroups(),
                ConsolePermissionDefaults.defaultOperationGroups(),
                List.of(menu(displayName, description, 0, entryPageKey, icon)));
    }

    private static List<ConsolePermissionGroupDeclaration> defaultGroups(
            List<ConsolePermissionGroupDeclaration> groups,
            List<ConsolePermissionGroupDeclaration> defaults
    ) {
        if (groups == null || groups.isEmpty()) {
            return defaults;
        }
        return groups;
    }

    static String requireKey(String value, String field) {
        if (value == null || value.length() > 128 || !KEY_PATTERN.matcher(value).matches()) {
            invalid("invalid " + field + ": " + value);
        }
        return value;
    }

    static String requireGroupKey(String value, String field) {
        if (value == null || value.length() > 128 || !GROUP_PATTERN.matcher(value).matches()) {
            invalid("invalid " + field + ": " + value);
        }
        return value;
    }

    private static void requireDefaultGroup(
            List<ConsolePermissionGroupDeclaration> groups,
            String field
    ) {
        if (groups.isEmpty()) {
            invalid(field + " must be non-empty and include group 'default'");
        }
        boolean hasDefault = false;
        for (ConsolePermissionGroupDeclaration group : groups) {
            if (DEFAULT_VIEW_GROUP.equals(group.group())) {
                hasDefault = true;
                break;
            }
        }
        if (!hasDefault) {
            invalid(field + " must include group 'default'");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void invalid(String message) {
        throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT, message);
    }
}

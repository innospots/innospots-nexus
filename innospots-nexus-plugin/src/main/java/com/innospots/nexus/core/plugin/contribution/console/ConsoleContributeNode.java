package com.innospots.nexus.core.plugin.contribution.console;

import java.util.List;
import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * console@2 递归贡献节点（MODULE 或 MENU，由 {@link #entryPageKey()} 与 {@link #children()} 推导）。
 */
public record ConsoleContributeNode(
        String moduleKey,
        I18nObject displayName,
        I18nObject description,
        int orderIndex,
        String entryPageKey,
        String icon,
        String viewGroup,
        String grantName,
        List<ConsolePermissionGroupDeclaration> viewGroups,
        List<ConsolePermissionGroupDeclaration> operationGroups,
        List<ConsoleContributeNode> children
) {

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
            requireDefaultGroup(viewGroups, "viewGroups");
            requireDefaultGroup(operationGroups, "operationGroups");
        } else if (menuNode) {
            if (moduleKey != null) {
                invalid("MENU node must not set moduleKey");
            }
            entryPageKey = requireKey(entryPageKey, "entryPageKey");
            viewGroup = viewGroup == null || viewGroup.isBlank() ? "default" : viewGroup;
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

    /** 访问授权名；未声明时使用 {@code view.{entryPageKey}}。 */
    public String resolvedGrantName() {
        if (!isMenu()) {
            return null;
        }
        if (grantName != null && !grantName.isBlank()) {
            return grantName.trim();
        }
        return "view." + entryPageKey;
    }

    public String menuResourceKey(String ownerModuleKey) {
        return "menu:" + ownerModuleKey + "." + entryPageKey;
    }

    public String moduleResourceKey() {
        return "module:" + moduleKey;
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
            if ("default".equals(group.group())) {
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

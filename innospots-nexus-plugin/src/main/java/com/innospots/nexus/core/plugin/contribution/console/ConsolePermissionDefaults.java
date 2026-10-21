package com.innospots.nexus.core.plugin.contribution.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * MODULE 默认权限分组（仅包内使用；对外通过 {@link ConsoleContributeNode} 工厂注入）。
 */
final class ConsolePermissionDefaults {

    private ConsolePermissionDefaults() {
    }

    static List<ConsolePermissionGroupDeclaration> defaultViewGroups() {
        return List.of(group(
                ConsoleContributeNode.DEFAULT_VIEW_GROUP,
                I18nObject.of(Map.of("zh-CN", "页面访问", "en", "Page access"))));
    }

    static List<ConsolePermissionGroupDeclaration> defaultOperationGroups() {
        return List.of(group(
                ConsoleContributeNode.DEFAULT_VIEW_GROUP,
                I18nObject.of(Map.of("zh-CN", "操作", "en", "Operations"))));
    }

    static ConsolePermissionGroupDeclaration group(String groupKey, I18nObject label) {
        return new ConsolePermissionGroupDeclaration(groupKey, label);
    }

    static List<ConsolePermissionGroupDeclaration> operationGroups(
            ConsolePermissionGroupDeclaration... groups
    ) {
        List<ConsolePermissionGroupDeclaration> merged = new ArrayList<>(defaultOperationGroups());
        for (ConsolePermissionGroupDeclaration group : groups) {
            if (merged.stream().noneMatch(existing -> existing.group().equals(group.group()))) {
                merged.add(group);
            }
        }
        return List.copyOf(merged);
    }
}

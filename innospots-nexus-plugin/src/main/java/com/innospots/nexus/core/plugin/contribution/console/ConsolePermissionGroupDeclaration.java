package com.innospots.nexus.core.plugin.contribution.console;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * MODULE 上声明的访问或操作分组元数据（console@2）。
 */
public record ConsolePermissionGroupDeclaration(
        String group,
        I18nObject label
) {

    public ConsolePermissionGroupDeclaration {
        group = ConsoleContributeNode.requireGroupKey(group, "group");
        label = ConsoleI18n.copy(label, true, "label", PluginStatusCode.RESOURCE_CONFLICT);
    }
}

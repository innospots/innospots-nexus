package com.innospots.nexus.core.plugin.contribution.console;

import java.util.List;
import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** 一个 Console 管理模块及其页面树、菜单树。 */
public record ConsoleModuleDeclaration(
        String moduleKey,
        I18nObject displayName,
        I18nObject description,
        List<UiSpecPageDeclaration> pages,
        List<MenuDeclaration> menuTree
) {

    private static final Pattern KEY_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:-[a-z0-9]+)*");

    /** 校验模块身份并复制本地化文本和子声明。 */
    public ConsoleModuleDeclaration {
        if (moduleKey == null || moduleKey.length() > 128 || !KEY_PATTERN.matcher(moduleKey).matches()) {
            invalid("invalid moduleKey: " + moduleKey);
        }
        displayName = ConsoleI18n.copy(
                displayName, true, "displayName", PluginStatusCode.RESOURCE_CONFLICT);
        description = ConsoleI18n.copy(
                description, false, "description", PluginStatusCode.RESOURCE_CONFLICT);
        pages = pages == null ? List.of() : List.copyOf(pages);
        if (pages.isEmpty()) {
            invalid("pages must contain at least one root page");
        }
        menuTree = menuTree == null ? List.of() : List.copyOf(menuTree);
    }

    /** 返回模块稳定资源身份。 */
    public String resourceKey() {
        return "module:" + moduleKey;
    }

    private static void invalid(String message) {
        throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT, message);
    }
}

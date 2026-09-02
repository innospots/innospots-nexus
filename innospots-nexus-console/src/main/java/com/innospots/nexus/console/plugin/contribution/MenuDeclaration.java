package com.innospots.nexus.console.plugin.contribution;

import java.util.List;
import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** Console 菜单树节点；目录和页面入口互斥。 */
public record MenuDeclaration(
        String menuKey,
        I18nObject title,
        String icon,
        int orderIndex,
        String pageKey,
        List<MenuDeclaration> children
) {

    private static final Pattern KEY_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:-[a-z0-9]+)*");

    /** 创建目录菜单节点。 */
    public static MenuDeclaration directory(
            String menuKey, I18nObject title, String icon, int orderIndex, List<MenuDeclaration> children
    ) {
        return new MenuDeclaration(menuKey, title, icon, orderIndex, null, children);
    }

    /** 创建页面入口菜单节点。 */
    public static MenuDeclaration page(
            String menuKey, I18nObject title, String icon, int orderIndex, String pageKey
    ) {
        return new MenuDeclaration(menuKey, title, icon, orderIndex, pageKey, List.of());
    }

    /** 校验菜单节点形态并复制嵌套菜单。 */
    public MenuDeclaration {
        if (menuKey == null || menuKey.length() > 128 || !KEY_PATTERN.matcher(menuKey).matches()) {
            invalid("invalid menuKey: " + menuKey);
        }
        if (title == null || title.isEmpty()) {
            invalid("title is required");
        }
        title = ConsoleI18n.copy(title, true, "title", PluginStatusCode.RESOURCE_CONFLICT);
        pageKey = pageKey == null || pageKey.isBlank() ? null : pageKey.trim();
        if (pageKey != null && !KEY_PATTERN.matcher(pageKey).matches()) {
            invalid("invalid menu pageKey: " + pageKey);
        }
        if (icon != null && (icon.isBlank() || icon.length() > 128)) {
            invalid("icon must be 1-128 characters when present");
        }
        children = children == null ? List.of() : List.copyOf(children);
        if ((pageKey == null) == children.isEmpty()) {
            invalid("menu node must have either pageKey or children");
        }
    }

    /** 返回模块内稳定菜单资源身份。 */
    public String resourceKey(String moduleKey) {
        if (moduleKey == null || moduleKey.length() > 128 || !KEY_PATTERN.matcher(moduleKey).matches()) {
            invalid("invalid moduleKey: " + moduleKey);
        }
        return "menu:" + moduleKey + "." + menuKey;
    }

    private static void invalid(String message) {
        throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT, message);
    }
}

package com.innospots.nexus.core.extension.declaration;

import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * Recursive menu declaration. A directory has children and no page key; a
 * page node has a page key and no children.
 *
 * @param menuKey stable menu key
 * @param title internationalized menu title
 * @param icon optional icon key
 * @param orderIndex sibling ordering value
 * @param pageKey optional page key referenced in the same module
 * @param children directory child nodes
 */
public record MenuDeclaration(
        String menuKey,
        I18nObject title,
        String icon,
        int orderIndex,
        String pageKey,
        List<MenuDeclaration> children
) {

    /** Creates a directory node. */
    public static MenuDeclaration directory(
            String menuKey,
            I18nObject title,
            String icon,
            int orderIndex,
            List<MenuDeclaration> children
    ) {
        return new MenuDeclaration(menuKey, title, icon, orderIndex, null, children);
    }

    /** Creates a page node. */
    public static MenuDeclaration page(
            String menuKey,
            I18nObject title,
            String icon,
            int orderIndex,
            String pageKey
    ) {
        return new MenuDeclaration(menuKey, title, icon, orderIndex, pageKey, List.of());
    }

    /** Creates a validated menu node with immutable children. */
    public MenuDeclaration {
        requireText(menuKey, "menuKey");
        if (title == null || title.isEmpty()) {
            invalid("title");
        }
        title = I18nObject.of(title);
        pageKey = blankToNull(pageKey);
        children = children == null ? List.of() : List.copyOf(children);
        boolean hasPage = pageKey != null;
        boolean hasChildren = !children.isEmpty();
        if (hasPage == hasChildren) {
            invalid("menu node must have either pageKey or children");
        }
    }

    /** Returns the stable menu resource ID for its owning module. */
    public String resourceKey(String moduleKey) {
        requireText(moduleKey, "moduleKey");
        return "menu:" + moduleKey + "." + menuKey;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            invalid(field + " is required");
        }
    }

    private static void invalid(String message) {
        throw NexusException.build(
                NexusStatusCode.INVALID_PARAMETER.fullCode(),
                message);
    }
}

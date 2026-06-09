package com.innospots.nexus.kernel.menu;

/**
 * Kernel menu entity used to describe console navigation.
 *
 * @param menuId    menu identifier
 * @param parentId  parent menu identifier
 * @param name      menu display name
 * @param path      route or link path
 * @param type      menu type
 * @param orderIndex ordering value
 */
public record MenuItem(
        String menuId,
        String parentId,
        String name,
        String path,
        MenuItemType type,
        int orderIndex
) {
}

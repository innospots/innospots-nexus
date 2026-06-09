package com.innospots.nexus.kernel.menu;

import java.util.List;
import java.util.Optional;

/**
 * Port for menu declaration and lookup operations.
 */
public interface MenuOperator {

    /**
     * Finds a menu item by identifier.
     *
     * @param menuId menu identifier
     * @return menu item when found
     */
    Optional<MenuItem> findMenu(String menuId);

    /**
     * Lists menu items available to a user.
     *
     * @param userId user identifier
     * @return ordered menu items
     */
    List<MenuItem> listMenus(String userId);

    /**
     * Saves a menu item.
     *
     * @param menuItem menu item to save
     * @return saved menu item
     */
    MenuItem saveMenu(MenuItem menuItem);
}

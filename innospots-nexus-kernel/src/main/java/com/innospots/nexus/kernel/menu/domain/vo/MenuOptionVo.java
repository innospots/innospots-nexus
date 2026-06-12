package com.innospots.nexus.kernel.menu.domain.vo;

import com.innospots.nexus.kernel.menu.domain.enums.MenuType;

/**
 * Compact menu option for parent selectors and tree controls.
 *
 * @param menuId   menu identifier
 * @param parentId optional parent menu identifier
 * @param menuName display name
 * @param menuType menu node type
 * @param disabled whether the option cannot be selected
 */
public record MenuOptionVo(
        String menuId,
        String parentId,
        String menuName,
        MenuType menuType,
        Boolean disabled
) {
}

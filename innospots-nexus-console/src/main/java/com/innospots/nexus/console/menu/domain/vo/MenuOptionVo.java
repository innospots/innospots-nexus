package com.innospots.nexus.console.menu.domain.vo;

import com.innospots.nexus.console.menu.domain.enums.MenuType;

/**
 * 用于父级选择器与树形控件菜单精简选项。
 *
 * @author Smars
 * @date 2026/09/13
 * @param menuId   菜单标识符
 * @param parentId 可选 parent 菜单标识符
 * @param menuName 显示名称
 * @param menuType 菜单节点类型
 * @param disabled 选项是否不可选择
 */
public record MenuOptionVo(
        String menuId,
        String parentId,
        String menuName,
        MenuType menuType,
        Boolean disabled
) {
}

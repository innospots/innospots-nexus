package com.innospots.nexus.console.menu.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

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
@Schema(name = "MenuOptionVo", description = "菜单选项")
public record MenuOptionVo(
        @Schema(description = "菜单标识符", required = true)
        String menuId,
        @Schema(description = "可选 parent 菜单标识符")
        String parentId,
        @Schema(description = "显示名称", required = true)
        String menuName,
        @Schema(description = "菜单节点类型", required = true)
        MenuType menuType,
        @Schema(description = "选项是否不可选择", required = true)
        Boolean disabled
) {
}

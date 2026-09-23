package com.innospots.nexus.console.menu.domain.request;

import com.innospots.nexus.console.menu.domain.enums.MenuOpenMode;
import com.innospots.nexus.console.menu.domain.enums.MenuType;

/**
 * 现有菜单节点的可变档案数据。
 * <p>
 * 稳定的菜单键刻意排除在更新之外。
 * </p>
 *
 * @author Smars
 * @date 2026/09/13
 * @param parentId     可选 parent 菜单标识符
 * @param menuName     显示名称
 * @param menuType     菜单节点类型
 * @param routePath    内部导航路径
 * @param componentKey 逻辑前端组件标识符
 * @param redirectPath 可选 redirect path
 * @param externalUrl  外部目标地址 for link menus
 * @param icon         可选 icon 标识符
 * @param openMode     浏览器打开模式
 * @param visible      节点是否在导航中可见
 * @param sortOrder    sibling 显示顺序
 */
public record MenuUpdateRequest(
        String parentId,
        String menuName,
        MenuType menuType,
        String routePath,
        String componentKey,
        String redirectPath,
        String externalUrl,
        String icon,
        MenuOpenMode openMode,
        Boolean visible,
        Integer sortOrder
) {
}

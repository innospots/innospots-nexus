package com.innospots.nexus.console.menu.domain.request;

import java.util.List;

/**
 * 同一父节点下的有序同级菜单标识符。
 *
 * @author Smars
 * @date 2026/09/13
 * @param parentId 可选 parent 菜单标识符 for root menus
 * @param menuIds  menu 标识符s in target 显示顺序
 */
public record MenuOrderRequest(String parentId, List<String> menuIds) {

    public MenuOrderRequest {
        menuIds = menuIds == null ? List.of() : List.copyOf(menuIds);
    }
}

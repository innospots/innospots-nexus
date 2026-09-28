package com.innospots.nexus.console.menu.domain.request;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 同一父节点下的有序同级菜单标识符。
 *
 * @author Smars
 * @date 2026/09/13
 * @param parentId 可选 parent 菜单标识符 for root menus
 * @param menuIds  menu 标识符s in target 显示顺序
 */
@Schema(name = "MenuOrderRequest", description = "菜单排序请求")
public record MenuOrderRequest(
        @Schema(description = "可选 parent 菜单标识符")
        String parentId,
        @Schema(description = "目标显示顺序的 menu 标识符列表", required = true)
        List<String> menuIds
) {

    public MenuOrderRequest {
        menuIds = menuIds == null ? List.of() : List.copyOf(menuIds);
    }
}

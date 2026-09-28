package com.innospots.nexus.console.menu.domain.vo;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.console.menu.domain.enums.MenuOpenMode;

/**
 * 交付给管理前端的只读导航节点。
 *
 * @author Smars
 * @date 2026/09/13
 * @param menuKey       稳定的菜单键
 * @param menuName      显示名称
 * @param routePath     内部导航路径
 * @param componentKey  逻辑前端组件标识符
 * @param externalUrl   外部目标地址
 * @param icon          可选 icon 标识符
 * @param openMode      浏览器打开模式
 * @param resourceId    权限资源 ID
 * @param ownerPluginId 来源插件 ID
 * @param moduleKey     控制台模块键
 * @param pageKey       关联页面标识
 * @param children      嵌套可见导航节点
 */
@Schema(name = "NavigationMenuVo", description = "导航菜单视图")
public record NavigationMenuVo(
        @Schema(description = "稳定的菜单键", required = true)
        String menuKey,
        @Schema(description = "显示名称", required = true)
        String menuName,
        @Schema(description = "内部导航路径")
        String routePath,
        @Schema(description = "逻辑前端组件标识符")
        String componentKey,
        @Schema(description = "外部目标地址")
        String externalUrl,
        @Schema(description = "可选 icon 标识符")
        String icon,
        @Schema(description = "浏览器打开模式")
        MenuOpenMode openMode,
        @Schema(description = "权限资源 ID")
        String resourceId,
        @Schema(description = "来源插件 ID")
        String ownerPluginId,
        @Schema(description = "控制台模块键")
        String moduleKey,
        @Schema(description = "关联页面标识")
        String pageKey,
        @Schema(description = "嵌套可见导航节点", required = true)
        List<NavigationMenuVo> children
) {

    public NavigationMenuVo {
        children = children == null ? List.of() : List.copyOf(children);
    }
}

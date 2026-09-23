package com.innospots.nexus.console.menu.domain.vo;

import java.util.List;

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
public record NavigationMenuVo(
        String menuKey,
        String menuName,
        String routePath,
        String componentKey,
        String externalUrl,
        String icon,
        MenuOpenMode openMode,
        String resourceId,
        String ownerPluginId,
        String moduleKey,
        String pageKey,
        List<NavigationMenuVo> children
) {

    public NavigationMenuVo {
        children = children == null ? List.of() : List.copyOf(children);
    }
}

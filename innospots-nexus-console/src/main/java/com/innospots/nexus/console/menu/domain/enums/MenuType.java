package com.innospots.nexus.console.menu.domain.enums;

/**
 * 结构与导航菜单节点类型。
 * <p>
 * 授权动作与 API 资源有意归属
 * 权限领域而非本枚举。
 * </p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum MenuType {

    /**
     * 分组子菜单节点且无导航目标。
     */
    DIRECTORY,

    /**
     * 渲染内部应用页面。
     */
    PAGE,

    /**
     * 打开外部 URL。
     */
    EXTERNAL_LINK
}

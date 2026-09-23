package com.innospots.nexus.console.menu.domain.request;

import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.menu.domain.enums.MenuType;

/**
 * 由查询参数绑定的管理菜单树过滤器。
 *
 * @author Smars
 * @date 2026/09/13
 * @param input    菜单名称或键的模糊匹配
 * @param menuType 可选 menu 字典类型
 * @param status   可选 生命周期状态
 * @param visible  可选 navigation visibility
 */
public record MenuTreeRequest(
        @QueryParam("input") String input,
        @QueryParam("menuType") MenuType menuType,
        @QueryParam("status") BasicStatus status,
        @QueryParam("visible") Boolean visible
) {

    /**
     * 创建用于 Jakarta REST Bean 绑定的无过滤查询。
     */
    public MenuTreeRequest() {
        this(null, null, null, null);
    }
}

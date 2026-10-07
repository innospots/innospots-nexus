package com.innospots.nexus.console.entry;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 模块 {@code console@1} 菜单树中的单个页面入口节点描述。
 *
 * @author Smars
 * @date 2026/09/27
 * @param menuKey     模块内菜单节点稳定键
 * @param title       菜单展示标题
 * @param icon        可选图标
 * @param orderIndex  同级排序
 * @param pageKey     绑定的 PageDsl 页面键
 */
public record ConsoleMenuItemDescriptor(
        String menuKey,
        I18nObject title,
        String icon,
        int orderIndex,
        String pageKey
) {
}

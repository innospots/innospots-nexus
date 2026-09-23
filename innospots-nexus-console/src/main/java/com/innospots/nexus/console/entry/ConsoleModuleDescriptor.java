package com.innospots.nexus.console.entry;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 单个内置控制台模块 entry 插件的不可变元数据。
 *
 * @author Smars
 * @date 2026/09/13
 * @param pluginId    反向域名插件标识
 * @param moduleKey   控制台模块键与 PageDsl 目录名
 * @param pageKey     PageDsl 页面键，通常为 {@code {moduleKey}-main}
 * @param pagePath    前端路由路径
 * @param menuKey     模块内菜单 entry 键
 * @param menuIcon    可选 menu icon
 * @param orderIndex  同级菜单排序
 * @param displayName module 显示名称
 * @param description module 描述
 * @param pageTitle   菜单与页面标题
 */
public record ConsoleModuleDescriptor(
        String pluginId,
        String moduleKey,
        String pageKey,
        String pagePath,
        String menuKey,
        String menuIcon,
        int orderIndex,
        I18nObject displayName,
        I18nObject description,
        I18nObject pageTitle
) {

    /**
     * 返回控制台模块的主页面键。
     *
     * @param moduleKey 控制台模块键
     * @return stable 页面键，例如 {@code menu-main}
     */
    public static String mainPageKey(String moduleKey) {
        return moduleKey + "-main";
    }
}

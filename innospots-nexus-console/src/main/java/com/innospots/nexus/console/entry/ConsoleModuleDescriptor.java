package com.innospots.nexus.console.entry;

import java.util.ArrayList;
import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 单个内置控制台模块 entry 插件的不可变元数据。
 *
 * @author Smars
 * @date 2026/09/13
 * @param pluginId           反向域名插件标识
 * @param domainKey            PageDsl 领域目录名（classpath {@code ui-pages/{domainKey}/...})
 * @param moduleKey            控制台模块键与 PageDsl 模块目录名
 * @param entryPageKey         模块入口页 PageDsl 页面键
 * @param additionalPageKeys   同模块下其余 PageDsl 页面键（子页面，供目录与权限配置）
 * @param menuKey              模块内菜单 entry 键
 * @param menuIcon             可选 menu icon
 * @param orderIndex           同级菜单排序
 * @param displayName          module 显示名称
 * @param description          module 描述
 * @param pageTitle            单菜单模式下的入口菜单与页面标题
 * @param menuEntries          非空时使用多菜单节点；为空时使用 {@code menuKey} 等单菜单字段
 */
public record ConsoleModuleDescriptor(
        String pluginId,
        String domainKey,
        String moduleKey,
        String entryPageKey,
        List<String> additionalPageKeys,
        String menuKey,
        String menuIcon,
        int orderIndex,
        I18nObject displayName,
        I18nObject description,
        I18nObject pageTitle,
        List<ConsoleMenuItemDescriptor> menuEntries
) {

    /** 内置控制台模块 PageDsl 默认领域键。 */
    public static final String BUILTIN_DOMAIN_KEY = "nexus";

    public ConsoleModuleDescriptor {
        additionalPageKeys = additionalPageKeys == null ? List.of() : List.copyOf(additionalPageKeys);
        menuEntries = menuEntries == null ? List.of() : List.copyOf(menuEntries);
    }

    /**
     * 返回控制台模块的默认入口页面键。
     *
     * @param moduleKey 控制台模块键
     * @return stable 页面键，例如 {@code menu-main}
     */
    public static String mainPageKey(String moduleKey) {
        return moduleKey + "-main";
    }

    /**
     * 构建模块页面的前端路由路径：{@code /{domainKey}/{moduleKey}/{pageKey}}。
     *
     * @param domainKey 领域键
     * @param moduleKey 模块键
     * @param pageKey   PageDsl 页面键
     * @return 绝对路径
     */
    public static String pagePath(String domainKey, String moduleKey, String pageKey) {
        return "/" + domainKey + "/" + moduleKey + "/" + pageKey;
    }

    /**
     * 返回本模块声明的全部 PageDsl 页面键（入口页在前，其余按配置顺序）。
     *
     * @return 不可变页面键列表
     */
    public List<String> allPageKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(entryPageKey);
        for (String pageKey : additionalPageKeys) {
            if (!pageKey.equals(entryPageKey)) {
                keys.add(pageKey);
            }
        }
        return List.copyOf(keys);
    }

    /**
     * 构建内置控制台模块描述符；入口页键为 {@link #mainPageKey(String)}，菜单键与入口页键一致。
     *
     * @param pluginId    反向域名插件标识
     * @param moduleKey   模块键
     * @param menuIcon    菜单图标
     * @param orderIndex  同级排序
     * @param displayName 模块显示名称
     * @param description 模块描述
     * @param pageTitle   入口菜单与页面标题
     * @return 描述符
     */
    public static ConsoleModuleDescriptor builtin(
            String pluginId,
            String moduleKey,
            String menuIcon,
            int orderIndex,
            I18nObject displayName,
            I18nObject description,
            I18nObject pageTitle
    ) {
        return builtin(
                pluginId, moduleKey, menuIcon, orderIndex, displayName, description, pageTitle, List.of(), List.of());
    }

    /**
     * 构建内置控制台模块描述符，并声明模块内除入口页以外的 PageDsl 子页面键。
     *
     * @param pluginId             反向域名插件标识
     * @param moduleKey            模块键
     * @param menuIcon             菜单图标
     * @param orderIndex           同级排序
     * @param displayName          模块显示名称
     * @param description          模块描述
     * @param pageTitle            入口菜单与页面标题
     * @param additionalPageKeys   子页面 PageDsl 键
     * @return 描述符
     */
    public static ConsoleModuleDescriptor builtin(
            String pluginId,
            String moduleKey,
            String menuIcon,
            int orderIndex,
            I18nObject displayName,
            I18nObject description,
            I18nObject pageTitle,
            List<String> additionalPageKeys
    ) {
        return builtin(
                pluginId,
                moduleKey,
                menuIcon,
                orderIndex,
                displayName,
                description,
                pageTitle,
                additionalPageKeys,
                List.of());
    }

    /**
     * 构建内置控制台模块描述符，并声明额外页面键与多条顶层菜单入口。
     *
     * @param pluginId             反向域名插件标识
     * @param moduleKey            模块键
     * @param menuIcon             单菜单模式下的图标（多菜单时可为 {@code null})
     * @param orderIndex           单菜单模式下的排序
     * @param displayName          模块显示名称
     * @param description          模块描述
     * @param pageTitle            单菜单模式下的菜单标题
     * @param additionalPageKeys   子页面 PageDsl 键
     * @param menuEntries          多菜单节点；非空时忽略单菜单字段
     * @return 描述符
     */
    public static ConsoleModuleDescriptor builtin(
            String pluginId,
            String moduleKey,
            String menuIcon,
            int orderIndex,
            I18nObject displayName,
            I18nObject description,
            I18nObject pageTitle,
            List<String> additionalPageKeys,
            List<ConsoleMenuItemDescriptor> menuEntries
    ) {
        String entryPageKey = mainPageKey(moduleKey);
        return new ConsoleModuleDescriptor(
                pluginId,
                BUILTIN_DOMAIN_KEY,
                moduleKey,
                entryPageKey,
                additionalPageKeys,
                entryPageKey,
                menuIcon,
                orderIndex,
                displayName,
                description,
                pageTitle,
                menuEntries);
    }

    /**
     * 构建自定义入口页键的控制台模块描述符（非 {@link #mainPageKey} 约定）。
     *
     * @param pluginId             插件标识
     * @param domainKey            PageDsl 领域键
     * @param moduleKey            模块键
     * @param entryPageKey         入口页 pageKey
     * @param additionalPageKeys   同模块其它页面键
     * @param menuEntries          菜单树顶层节点；为空时使用单菜单参数
     * @param menuKey              单菜单节点键
     * @param menuIcon             单菜单图标
     * @param orderIndex           单菜单排序
     * @param displayName          模块显示名称
     * @param description          模块描述
     * @param pageTitle            单菜单标题
     * @return 描述符
     */
    public static ConsoleModuleDescriptor module(
            String pluginId,
            String domainKey,
            String moduleKey,
            String entryPageKey,
            List<String> additionalPageKeys,
            List<ConsoleMenuItemDescriptor> menuEntries,
            String menuKey,
            String menuIcon,
            int orderIndex,
            I18nObject displayName,
            I18nObject description,
            I18nObject pageTitle
    ) {
        return new ConsoleModuleDescriptor(
                pluginId,
                domainKey,
                moduleKey,
                entryPageKey,
                additionalPageKeys,
                menuKey,
                menuIcon,
                orderIndex,
                displayName,
                description,
                pageTitle,
                menuEntries);
    }
}

package com.innospots.nexus.console.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 控制台模块 entry 贡献的不可变元数据（PageDsl 页面清单与菜单树源数据）。
 *
 * @author Smars
 * @date 2026/09/13
 * @param pluginId           所属 entry 插件反向域名标识
 * @param domainKey          PageDsl 领域目录名（classpath {@code ui-pages/{domainKey}/...})
 * @param moduleKey          控制台模块键与 PageDsl 模块目录名
 * @param entryPageKey       模块入口页 PageDsl 页面键（调用方显式指定，无命名推导）
 * @param additionalPageKeys 同模块下其余 PageDsl 页面键
 * @param menuKey            单菜单模式下的菜单节点键
 * @param menuIcon           单菜单模式下的图标
 * @param orderIndex         单菜单模式下的同级排序
 * @param displayName        模块显示名称
 * @param description        模块描述
 * @param pageTitle          单菜单模式下的菜单标题
 * @param menuEntries        非空时生成多条顶层菜单，并忽略单菜单字段
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

    private static final Pattern KEY_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:-[a-z0-9]+)*");

    /**
     * 本仓库随 {@code innospots-nexus-console} 发布的六个内置 entry 插件沿用的 PageDsl 领域键。
     *
     * <p>非框架限制：其它插件应传入各自的 {@code domainKey}（与 {@code console@1} 及 classpath 一致）。</p>
     */
    public static final String BUILTIN_DOMAIN_KEY = "nexus";

    public ConsoleModuleDescriptor {
        domainKey = requireKey(domainKey, "domainKey");
        moduleKey = requireKey(moduleKey, "moduleKey");
        additionalPageKeys = additionalPageKeys == null ? List.of() : List.copyOf(additionalPageKeys);
        menuEntries = menuEntries == null ? List.of() : List.copyOf(menuEntries);
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
     * 构建单菜单模块描述符；{@code menuKey} 默认与 {@code entryPageKey} 相同。
     *
     * @param domainKey     PageDsl / console 贡献领域键
     * @param pluginId      插件标识
     * @param moduleKey     模块键
     * @param entryPageKey  入口页 pageKey
     * @param menuIcon      菜单图标
     * @param orderIndex    菜单排序
     * @param displayName   模块显示名称
     * @param description   模块描述
     * @param pageTitle     单菜单标题
     * @return 描述符
     */
    public static ConsoleModuleDescriptor builtin(
            String pluginId,
            String domainKey,
            String moduleKey,
            String entryPageKey,
            String menuIcon,
            int orderIndex,
            I18nObject displayName,
            I18nObject description,
            I18nObject pageTitle
    ) {
        return builtin(
                pluginId,
                domainKey,
                moduleKey,
                entryPageKey,
                menuIcon,
                orderIndex,
                displayName,
                description,
                pageTitle,
                List.of(),
                List.of());
    }

    /**
     * 构建模块描述符，并声明除入口页以外的 PageDsl 页面键。
     *
     * @param additionalPageKeys 子页面 pageKey 列表
     * @return 描述符
     */
    public static ConsoleModuleDescriptor builtin(
            String pluginId,
            String domainKey,
            String moduleKey,
            String entryPageKey,
            String menuIcon,
            int orderIndex,
            I18nObject displayName,
            I18nObject description,
            I18nObject pageTitle,
            List<String> additionalPageKeys
    ) {
        return builtin(
                pluginId,
                domainKey,
                moduleKey,
                entryPageKey,
                menuIcon,
                orderIndex,
                displayName,
                description,
                pageTitle,
                additionalPageKeys,
                List.of());
    }

    /**
     * 构建模块描述符，并声明额外页面与多条顶层菜单。
     *
     * @param menuEntries 非空时忽略单菜单字段
     * @return 描述符
     */
    public static ConsoleModuleDescriptor builtin(
            String pluginId,
            String domainKey,
            String moduleKey,
            String entryPageKey,
            String menuIcon,
            int orderIndex,
            I18nObject displayName,
            I18nObject description,
            I18nObject pageTitle,
            List<String> additionalPageKeys,
            List<ConsoleMenuItemDescriptor> menuEntries
    ) {
        return of(
                pluginId,
                domainKey,
                moduleKey,
                entryPageKey,
                additionalPageKeys,
                menuEntries,
                entryPageKey,
                menuIcon,
                orderIndex,
                displayName,
                description,
                pageTitle);
    }

    /**
     * 构建完整模块描述符；字段均由调用方显式传入。
     *
     * @param menuKey 单菜单节点键；多菜单时可为占位值（由 {@code menuEntries} 生效）
     * @return 描述符
     */
    public static ConsoleModuleDescriptor of(
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

    private static String requireKey(String value, String field) {
        if (value == null || value.length() > 128 || !KEY_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("invalid " + field + ": " + value);
        }
        return value;
    }
}

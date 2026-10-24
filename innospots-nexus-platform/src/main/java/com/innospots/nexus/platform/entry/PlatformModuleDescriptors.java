package com.innospots.nexus.platform.entry;

import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;

/**
 * Platform 控制台 MODULE 常量（console@2：单页模块直接使用叶子 MODULE，不再复制 MENU）。
 *
 * <p>浏览分组键统一为 {@code default}，资源键 {@code platform.&lt;module&gt;:default}。</p>
 */
public final class PlatformModuleDescriptors {

    private static final String DOMAIN = BuiltinPlatformEntryPlugins.DOMAIN_KEY;

    private static final String DEFAULT_VIEW_GROUP = ConsoleContributeNode.DEFAULT_VIEW_GROUP;

    public static final ConsoleContributeNode ORGANIZATION = pageModule(
            "organization",
            10,
            I18nObject.of("en", "Organizations", "zh", "组织"),
            I18nObject.of(
                    "en",
                    "Platform organizations (enterprise, team, or individual).",
                    "zh",
                    "平台组织开通与生命周期（企业、团队或个人形态）。"),
            "main",
            I18nObject.of("en", "Browse organization pages", "zh", "浏览组织管理页面"),
            "apartment");

    public static final ConsoleContributeNode MENU = pageModule(
            "menu",
            10,
            I18nObject.of("en", "Menu Management", "zh", "菜单管理"),
            I18nObject.of("en", "Platform navigation menu management.", "zh", "平台导航菜单管理。"),
            "main",
            I18nObject.of("en", "Browse menu management pages", "zh", "浏览菜单管理页面"),
            "menu");

    public static final ConsoleContributeNode USERS = pageModule(
            "user",
            20,
            I18nObject.of("en", "Users", "zh", "用户列表"),
            I18nObject.of("en", "Platform IAM and onboarding.", "zh", "平台用户与开通。"),
            "main",
            I18nObject.of("en", "Browse user management pages", "zh", "浏览用户管理页面"),
            "team");

    public static final ConsoleContributeNode DICTIONARY = pageModule(
            "dictionary",
            20,
            I18nObject.of("en", "Dictionary Management", "zh", "字典管理"),
            I18nObject.of("en", "Platform dictionary management.", "zh", "平台字典管理。"),
            "main",
            I18nObject.of("en", "Browse dictionary management pages", "zh", "浏览字典管理页面"),
            "book");

    public static final ConsoleContributeNode ROLE = pageModule(
            "role",
            30,
            I18nObject.of("en", "Role Management", "zh", "角色管理"),
            I18nObject.of("en", "Platform role management.", "zh", "平台角色管理。"),
            "main",
            I18nObject.of("en", "Browse role management pages", "zh", "浏览角色管理页面"),
            "team");

    public static final ConsoleContributeNode PERMISSION = pageModule(
            "permission",
            40,
            I18nObject.of("en", "Permission Management", "zh", "权限管理"),
            I18nObject.of("en", "Platform permission management.", "zh", "平台权限管理。"),
            "main",
            I18nObject.of("en", "Browse permission management pages", "zh", "浏览权限管理页面"),
            "lock");

    public static final ConsoleContributeNode LOGGER = pageModule(
            "logger",
            50,
            I18nObject.of("en", "Audit Logs", "zh", "审计日志"),
            I18nObject.of("en", "Platform audit log management.", "zh", "平台审计日志管理。"),
            "main",
            I18nObject.of("en", "Browse audit log pages", "zh", "浏览审计日志页面"),
            "file-text");

    public static final ConsoleContributeNode SETTINGS = pageModule(
            "settings",
            40,
            I18nObject.of("en", "Registration mode", "zh", "注册模式"),
            I18nObject.of("en", "Platform operational settings.", "zh", "运营平台配置项。"),
            "registration-mode",
            I18nObject.of("en", "Browse registration mode settings", "zh", "浏览注册模式配置页面"),
            "setting");

    /** {@link PlatformConsoleEntryPlugin} 贡献的模块顺序（与 catalog 一致）。 */
    public static final List<ConsoleContributeNode> CONSOLE_MODULES = List.of(
            ORGANIZATION,
            MENU,
            USERS,
            DICTIONARY,
            ROLE,
            PERMISSION,
            LOGGER,
            SETTINGS);

    private PlatformModuleDescriptors() {
    }

    private static ConsoleContributeNode pageModule(
            String moduleKey,
            int orderIndex,
            I18nObject displayName,
            I18nObject description,
            String pageSuffix,
            I18nObject viewGroupLabel,
            String icon
    ) {
        return ConsoleContributeNode.singlePageModule(
                moduleKey,
                orderIndex,
                displayName,
                description,
                entryPageKey(moduleKey, pageSuffix),
                icon,
                List.of(viewGroup(DEFAULT_VIEW_GROUP, viewGroupLabel)));
    }

    private static ConsolePermissionGroupDeclaration viewGroup(String groupKey, I18nObject label) {
        return new ConsolePermissionGroupDeclaration(groupKey, label);
    }

    private static String entryPageKey(String moduleKey, String pageSuffix) {
        return DOMAIN + "-" + moduleKey + "-" + pageSuffix;
    }
}

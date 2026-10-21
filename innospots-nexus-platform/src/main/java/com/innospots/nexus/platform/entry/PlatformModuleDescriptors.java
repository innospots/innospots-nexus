package com.innospots.nexus.platform.entry;

import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.ui.spec.PageDslPageRef;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;

/**
 * Platform 控制台 MODULE 常量（console@2：单页模块直接使用叶子 MODULE，不再复制 MENU）。
 */
public final class PlatformModuleDescriptors {

    private static final String DOMAIN = BuiltinPlatformEntryPlugins.DOMAIN_KEY;

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
            "apartment");

    public static final ConsoleContributeNode MENU = pageModule(
            "menu",
            10,
            I18nObject.of("en", "Menu Management", "zh", "菜单管理"),
            I18nObject.of("en", "Platform navigation menu management.", "zh", "平台导航菜单管理。"),
            "main",
            "menu");

    public static final ConsoleContributeNode USERS = pageModule(
            "user",
            20,
            I18nObject.of("en", "Users", "zh", "用户列表"),
            I18nObject.of("en", "Platform IAM and onboarding.", "zh", "平台用户与开通。"),
            "main",
            "team");

    public static final ConsoleContributeNode DICTIONARY = pageModule(
            "dictionary",
            20,
            I18nObject.of("en", "Dictionary Management", "zh", "字典管理"),
            I18nObject.of("en", "Platform dictionary management.", "zh", "平台字典管理。"),
            "main",
            "book");

    public static final ConsoleContributeNode ROLE = pageModule(
            "role",
            30,
            I18nObject.of("en", "Role Management", "zh", "角色管理"),
            I18nObject.of("en", "Platform role management.", "zh", "平台角色管理。"),
            "main",
            "team");

    public static final ConsoleContributeNode PERMISSION = pageModule(
            "permission",
            40,
            I18nObject.of("en", "Permission Management", "zh", "权限管理"),
            I18nObject.of("en", "Platform permission management.", "zh", "平台权限管理。"),
            "main",
            "lock");

    public static final ConsoleContributeNode LOGGER = pageModule(
            "logger",
            50,
            I18nObject.of("en", "Audit Logs", "zh", "审计日志"),
            I18nObject.of("en", "Platform audit log management.", "zh", "平台审计日志管理。"),
            "main",
            "file-text");

    public static final ConsoleContributeNode SETTINGS = pageModule(
            "settings",
            40,
            I18nObject.of("en", "Registration mode", "zh", "注册模式"),
            I18nObject.of("en", "Platform operational settings.", "zh", "运营平台配置项。"),
            "registration-mode",
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
            String icon
    ) {
        return ConsoleContributeNode.singlePageModule(
                moduleKey,
                orderIndex,
                displayName,
                description,
                PageDslPageRef.encode(DOMAIN, moduleKey, pageSuffix),
                icon);
    }
}

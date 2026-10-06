package com.innospots.nexus.platform.entry;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.ConsoleMenuItemDescriptor;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;

/**
 * 各 platform 控制台模块的 {@link com.innospots.nexus.console.entry.ConsoleModuleDescriptor} 常量。
 *
 * <p>与 {@code src/main/resources/ui-pages/platform/{moduleKey}/} 下 YAML 文件名一致。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.entry.PlatformEntryPluginDescriptor#platformConsole()
 */
public final class PlatformModuleDescriptors {

    private static final String PLUGIN_ID = BuiltinPlatformEntryPlugins.PLATFORM_CONSOLE;
    private static final String DOMAIN = BuiltinPlatformEntryPlugins.DOMAIN_KEY;

    public static final ConsoleModuleDescriptor TENANT = ConsoleModuleDescriptor.builtin(
            PLUGIN_ID,
            DOMAIN,
            "tenant",
            pageKey("tenant", "main"),
            "apartment",
            10,
            I18nObject.of("en", "Tenants", "zh", "租户"),
            I18nObject.of("en", "Platform tenant lifecycle.", "zh", "平台租户生命周期管理。"),
            I18nObject.of("en", "Tenants", "zh", "租户管理"));

    public static final ConsoleModuleDescriptor USERS = ConsoleModuleDescriptor.of(
            PLUGIN_ID,
            DOMAIN,
            "user",
            pageKey("user", "main"),
            java.util.List.of(
                    pageKey("user", "invite"),
                    pageKey("user", "access"),
                    pageKey("user", "add")),
            java.util.List.of(
                    menu("user-main", pageKey("user", "main"), "team", 10,
                            I18nObject.of("en", "Users", "zh", "用户列表")),
                    menu("user-invite", pageKey("user", "invite"), "mail", 20,
                            I18nObject.of("en", "Invites", "zh", "邀请列表")),
                    menu("user-access", pageKey("user", "access"), "audit", 30,
                            I18nObject.of("en", "Registration approval", "zh", "注册审批")),
                    menu("user-add", pageKey("user", "add"), "user-add", 15,
                            I18nObject.of("en", "Add user", "zh", "添加用户"))),
            pageKey("user", "main"),
            "team",
            20,
            I18nObject.of("en", "Platform users", "zh", "平台用户"),
            I18nObject.of("en", "Platform IAM and onboarding.", "zh", "平台用户与开通。"),
            I18nObject.of("en", "Users", "zh", "用户列表"));

    public static final ConsoleModuleDescriptor SETTINGS = ConsoleModuleDescriptor.builtin(
            PLUGIN_ID,
            DOMAIN,
            "settings",
            pageKey("settings", "registration-mode"),
            "setting",
            40,
            I18nObject.of("en", "Settings", "zh", "平台设置"),
            I18nObject.of("en", "Platform operational settings.", "zh", "运营平台配置项。"),
            I18nObject.of("en", "Registration mode", "zh", "注册模式"));

    private PlatformModuleDescriptors() {
    }

    static String domainKey(ConsoleModuleDescriptor descriptor) {
        return descriptor.domainKey();
    }

    static String moduleKey(ConsoleModuleDescriptor descriptor) {
        return descriptor.moduleKey();
    }

    static String pageKey(String moduleKey, String suffix) {
        return ConsoleModuleDescriptor.compositePageKey(DOMAIN, moduleKey, suffix);
    }

    private static ConsoleMenuItemDescriptor menu(
            String menuKey,
            String pageKey,
            String icon,
            int order,
            I18nObject title
    ) {
        return new ConsoleMenuItemDescriptor(menuKey, title, icon, order, pageKey);
    }
}

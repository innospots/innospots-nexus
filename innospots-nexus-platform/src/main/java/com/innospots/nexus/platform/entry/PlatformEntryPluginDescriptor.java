package com.innospots.nexus.platform.entry;

import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;

/**
 * 单个 platform entry 插件元数据（可含多个控制台模块）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.entry.PlatformModuleEntrySupport
 */
public record PlatformEntryPluginDescriptor(
        String pluginId,
        I18nObject displayName,
        I18nObject description,
        List<ConsoleModuleDescriptor> modules
) {

    public PlatformEntryPluginDescriptor {
        modules = modules == null ? List.of() : List.copyOf(modules);
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("modules must not be empty");
        }
        for (ConsoleModuleDescriptor module : modules) {
            if (!pluginId.equals(module.pluginId())) {
                throw new IllegalArgumentException(
                        "module pluginId must match entry pluginId: " + module.moduleKey());
            }
        }
    }

    /**
     * 默认 platform 管理控制台 entry：租户、平台用户、注册策略三个模块。
     *
     * @return 不可变 entry 描述符
     */
    public static PlatformEntryPluginDescriptor platformConsole() {
        return new PlatformEntryPluginDescriptor(
                BuiltinPlatformEntryPlugins.PLATFORM_CONSOLE,
                I18nObject.of("en", "Platform Console", "zh", "运营管理平台"),
                I18nObject.of(
                        "en",
                        "Tenant lifecycle, platform users, invites, and registration policy.",
                        "zh",
                        "租户生命周期、平台用户、邀请与注册策略。"),
                List.of(
                        PlatformModuleDescriptors.TENANT,
                        PlatformModuleDescriptors.USERS,
                        PlatformModuleDescriptors.SETTINGS));
    }
}

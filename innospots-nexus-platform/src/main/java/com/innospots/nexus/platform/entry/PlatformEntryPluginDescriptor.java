package com.innospots.nexus.platform.entry;

import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;

/**
 * Platform 聚合 entry 插件元数据（多 MODULE 合一插件）。
 */
public record PlatformEntryPluginDescriptor(
        String pluginId,
        I18nObject displayName,
        I18nObject description,
        List<ConsoleContributeNode> modules
) {

    public PlatformEntryPluginDescriptor {
        modules = modules == null ? List.of() : List.copyOf(modules);
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("modules must not be empty");
        }
    }

    public static PlatformEntryPluginDescriptor platformConsole() {
        return new PlatformEntryPluginDescriptor(
                BuiltinPlatformEntryPlugins.PLATFORM_CONSOLE,
                I18nObject.of("en", "Platform Console", "zh", "运营管理平台"),
                I18nObject.of(
                        "en",
                        "Organizations, platform users, invites, and registration policy.",
                        "zh",
                        "组织开通、平台用户、邀请与注册策略。"),
                PlatformModuleDescriptors.CONSOLE_MODULES);
    }
}

package com.innospots.nexus.console.entry;

import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 单个 entry 插件的元数据，可包含多个控制台模块。
 *
 * @author Smars
 * @date 2026/09/27
 * @param pluginId    反向域名插件标识
 * @param displayName 插件显示名称
 * @param description 插件描述
 * @param modules     各模块的 PageDsl 与菜单贡献（至少一项）
 */
public record ConsoleEntryPluginDescriptor(
        String pluginId,
        I18nObject displayName,
        I18nObject description,
        List<ConsoleModuleDescriptor> modules
) {

    public ConsoleEntryPluginDescriptor {
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
     * 由单模块描述符构建 entry 插件描述符。
     *
     * @param module 模块元数据
     * @return entry 描述符
     */
    public static ConsoleEntryPluginDescriptor of(ConsoleModuleDescriptor module) {
        return new ConsoleEntryPluginDescriptor(
                module.pluginId(),
                module.displayName(),
                module.description(),
                List.of(module));
    }

    /**
     * 构建多模块 entry 插件描述符。
     *
     * @param pluginId    插件标识
     * @param displayName 插件显示名称
     * @param description 插件描述
     * @param modules     模块列表
     * @return entry 描述符
     */
    public static ConsoleEntryPluginDescriptor of(
            String pluginId,
            I18nObject displayName,
            I18nObject description,
            List<ConsoleModuleDescriptor> modules
    ) {
        return new ConsoleEntryPluginDescriptor(pluginId, displayName, description, modules);
    }
}

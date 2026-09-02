package com.innospots.nexus.core.plugin.installation.config;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 插件安装策略配置；默认关闭首次发现自动安装。
 *
 * @param autoInstall 首次发现时是否自动安装
 */
public record PluginInstallationConfig(boolean autoInstall) {

    /** 系统配置键 {@value}。 */
    public static final String AUTO_INSTALL_KEY = "nexus.plugin.auto-install";

    /**
     * 从系统配置读取布尔值，缺省为空时按 false 处理。
     *
     * @param value 配置文本；允许 {@code true} 或 {@code false}
     * @return 解析后的安装策略配置
     * @throws NexusException 文本不是合法布尔值时抛出
     */
    public static PluginInstallationConfig from(String value) {
        if (value == null || value.isBlank()) {
            return new PluginInstallationConfig(false);
        }
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "nexus.plugin.auto-install must be true or false");
        }
        return new PluginInstallationConfig(Boolean.parseBoolean(value));
    }
}

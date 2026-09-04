package com.innospots.nexus.spring.plugin.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 插件宿主安装与启停策略配置属性。
 *
 * <p>绑定 {@code nexus.plugin.*} 与 {@code nexus.plugins.*} 命名空间。
 * 插件实例配置键 {@code plugins.<pluginId>.<key>} 由 {@link PluginHostConfigBinder} 单独汇总。</p>
 *
 * @see PluginHostConfigBinder
 */
@ConfigurationProperties(prefix = "nexus")
public class PluginHostProperties {

    /** {@code nexus.plugin.*} 安装策略。 */
    private Plugin plugin = new Plugin();

    /** {@code nexus.plugins.*} 启停策略。 */
    private Plugins plugins = new Plugins();

    /** 返回安装策略配置。 */
    public Plugin getPlugin() {
        return plugin;
    }

    /** 设置安装策略配置。 */
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    /** 返回启停策略配置。 */
    public Plugins getPlugins() {
        return plugins;
    }

    /** 设置启停策略配置。 */
    public void setPlugins(Plugins plugins) {
        this.plugins = plugins;
    }

    /**
     * {@code nexus.plugin.*} 安装策略。
     *
     * <p>映射示例：{@code nexus.plugin.auto-install=true}。</p>
     */
    public static class Plugin {

        /** 首次 classpath 发现时是否自动安装并期望启用。 */
        private boolean autoInstall = true;

        /** 是否自动安装首次发现的插件。 */
        public boolean isAutoInstall() {
            return autoInstall;
        }

        /** 设置是否自动安装首次发现的插件。 */
        public void setAutoInstall(boolean autoInstall) {
            this.autoInstall = autoInstall;
        }
    }

    /**
     * {@code nexus.plugins.*} 启停策略。
     *
     * <p>映射示例：{@code nexus.plugins.required[0]=com.example.plugin}。</p>
     */
    public static class Plugins {

        /** 启动时必须处于 ACTIVE 的插件 ID 列表。 */
        private List<String> required = new ArrayList<>();

        /** 永不自动启动的插件 ID 列表。 */
        private List<String> disabled = new ArrayList<>();

        /** 返回必须 ACTIVE 的插件列表。 */
        public List<String> getRequired() {
            return required;
        }

        /**
         * 设置必须 ACTIVE 的插件列表。
         *
         * @param required 插件 ID 列表；{@code null} 时重置为空列表
         */
        public void setRequired(List<String> required) {
            this.required = required == null ? new ArrayList<>() : new ArrayList<>(required);
        }

        /** 返回禁用自动启动的插件列表。 */
        public List<String> getDisabled() {
            return disabled;
        }

        /**
         * 设置禁用自动启动的插件列表。
         *
         * @param disabled 插件 ID 列表；{@code null} 时重置为空列表
         */
        public void setDisabled(List<String> disabled) {
            this.disabled = disabled == null ? new ArrayList<>() : new ArrayList<>(disabled);
        }
    }
}

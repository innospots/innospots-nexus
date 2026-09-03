package com.innospots.nexus.quarkus.plugin.config;

import java.util.List;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * 插件宿主安装与启停策略配置映射。
 *
 * <p>绑定 {@code nexus.plugin.*} 与 {@code nexus.plugins.*}。
 * 插件实例键 {@code plugins.*} 由 {@link PluginHostConfigBinder} 单独汇总。</p>
 */
@ConfigMapping(prefix = "nexus")
public interface NexusPluginHostConfig {

    /**
     * 首次 classpath 发现时是否自动安装并期望启用。
     *
     * <p>映射键：{@code nexus.plugin.auto-install}。</p>
     */
    @WithName("plugin.auto-install")
    @WithDefault("false")
    boolean autoInstall();

    /** {@code nexus.plugins.*} 启停策略分组。 */
    Plugins plugins();

    /**
     * {@code nexus.plugins.*} 启停策略。
     */
    interface Plugins {

        /**
         * 启动时必须 ACTIVE 的插件 ID 列表。
         *
         * <p>未配置时为空；不要用空字符串作为默认值，SmallRye 会把它当成 null。</p>
         */
        Optional<List<String>> required();

        /**
         * 永不自动启动的插件 ID 列表。
         *
         * <p>未配置时为空。</p>
         */
        Optional<List<String>> disabled();
    }
}

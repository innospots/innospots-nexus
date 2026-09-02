package com.innospots.nexus.core.plugin.config;

import java.util.Map;

/**
 * 宿主侧动态配置来源扩展点。
 *
 * <p>由应用在 {@link com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig} 中注册，
 * 在每次插件启动解析配置时调用 {@link #values()}。典型实现从数据库、配置中心或密钥服务读取
 * 扁平化 {@code plugins.<pluginId>.<key>} 键值对。</p>
 *
 * <p>实现应返回不可变快照；调用方不得修改 {@link #values()} 返回的映射。
 * 不得由插件 JAR 提供，避免与 {@code PluginManager} 启动形成循环依赖。</p>
 */
public interface ConfigSource {

    /**
     * 返回诊断用配置来源名称。
     *
     * @return 来源标识，用于日志和错误诊断
     */
    String name();

    /**
     * 返回当前时刻的不可变原始键值对。
     *
     * <p>在插件 {@code start()} 的 config-resolve 阶段按声明顺序调用；可实现为查询数据库等
     * 动态来源。仅覆盖插件 schema 中已声明的键。</p>
     *
     * @return 扁平化配置键到字符串值的不可变映射
     */
    Map<String, String> values();
}

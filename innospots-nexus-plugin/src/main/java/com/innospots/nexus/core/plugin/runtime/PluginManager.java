package com.innospots.nexus.core.plugin.runtime;

import java.util.List;
import java.util.Optional;

import com.innospots.nexus.core.plugin.lifecycle.PluginRuntimeInfo;

/**
 * 宿主侧插件运行时入口，负责生命周期与诊断。
 *
 * <p>实现不是全局单例；每个实例独立管理一组由宿主预先发现的插件。关闭后不可复用。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public interface PluginManager extends AutoCloseable {

    /**
     * 启动所有未禁用的插件。
     *
     * @throws com.innospots.nexus.base.exception.NexusException 必需插件无法激活时抛出
     */
    void start();

    /**
     * 启动指定插件。
     *
     * @param pluginId 稳定的插件标识
     * @throws com.innospots.nexus.base.exception.NexusException 插件未知或启动失败时抛出
     */
    void start(String pluginId);

    /**
     * 停止指定插件。
     *
     * @param pluginId 稳定的插件标识
     * @throws com.innospots.nexus.base.exception.NexusException 停止失败时抛出
     */
    void stop(String pluginId);

    /**
     * 返回按插件标识排序的不可变运行快照。
     *
     * @return 不可变插件运行快照
     */
    List<PluginRuntimeInfo> plugins();

    /**
     * 查找一个插件运行快照。
     *
     * @param pluginId 稳定的插件标识
     * @return 匹配的快照；未发现该插件时返回空 Optional
     */
    Optional<PluginRuntimeInfo> plugin(String pluginId);

    /**
     * 按实际启动顺序逆序停止所有活动插件。
     *
     * @throws com.innospots.nexus.base.exception.NexusException 一个或多个插件停止失败时抛出
     */
    @Override
    void close();
}

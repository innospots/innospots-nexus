package com.innospots.nexus.core.plugin.runtime;

import java.util.List;
import java.util.Optional;

import com.innospots.nexus.core.plugin.capability.CapabilityManager;
import com.innospots.nexus.core.plugin.lifecycle.PluginRuntimeInfo;

/**
 * 宿主侧插件运行时入口，负责依赖感知生命周期、诊断和 Capability 查询。
 *
 * <p>实现不是全局单例；每个实例独立管理一组由宿主预先发现的插件。关闭后不可复用。</p>
 */
public interface PluginManager extends AutoCloseable {

    /**
     * 启动所有必需 Capability 已可用的插件。
     *
     * @throws com.innospots.nexus.base.exception.NexusException 必需插件无法激活时抛出
     */
    void start();

    /**
     * 在所有必需 Capability 均活动时启动指定插件。
     *
     * @param pluginId 稳定的插件标识
     * @throws com.innospots.nexus.base.exception.NexusException 插件未知或依赖不可用时抛出
     */
    void start(String pluginId);

    /**
     * 停止指定插件；如果会移除另一个活动插件的最后一个必需 Provider，则拒绝操作。
     *
     * @param pluginId 稳定的插件标识
     * @throws com.innospots.nexus.base.exception.NexusException 插件仍被使用或停止失败时抛出
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
     * 返回类型安全的活动 Capability 查询边界。
     *
     * @return 活动 Capability 管理器
     */
    CapabilityManager capabilities();

    /**
     * 按实际启动顺序逆序停止所有活动插件。
     *
     * @throws com.innospots.nexus.base.exception.NexusException 一个或多个插件停止失败时抛出
     */
    @Override
    void close();
}

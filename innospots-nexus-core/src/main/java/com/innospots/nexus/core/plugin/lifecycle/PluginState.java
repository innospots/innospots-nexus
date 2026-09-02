package com.innospots.nexus.core.plugin.lifecycle;

/**
 * 对外暴露的插件粗粒度生命周期状态。
 *
 * @see PluginRuntimeInfo
 */
public enum PluginState {

    /** 已在 Catalog 中发现，尚未完成定义编译或预检。 */
    DISCOVERED,

    /** 定义已编译并通过预检，尚未进入启动队列。 */
    DESCRIBED,

    /** 等待依赖满足或宿主允许启动。 */
    WAITING,

    /** 正在执行启动事务（初始化 Provider、Contribution 与资源）。 */
    STARTING,

    /** 启动成功且对外可用。 */
    ACTIVE,

    /** 正在执行停止事务。 */
    STOPPING,

    /** 已停止且资源作用域已释放。 */
    STOPPED,

    /** 启动或停止失败；是否可重试由上层策略决定。 */
    FAILED
}

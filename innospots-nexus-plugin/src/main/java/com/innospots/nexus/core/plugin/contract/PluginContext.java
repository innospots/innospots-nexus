package com.innospots.nexus.core.plugin.contract;

import com.innospots.nexus.core.plugin.capability.CapabilityManager;
import com.innospots.nexus.core.plugin.config.PluginConfig;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.event.PluginEventBus;
import com.innospots.nexus.core.plugin.resource.ResourceScope;

/**
 * 一次插件启动周期内向插件暴露的只读运行时服务。
 */
public interface PluginContext {

    /**
     * 返回不可变插件定义快照。
     *
     * @return 插件声明
     */
    PluginDefinition definition();

    /**
     * 返回限定在当前插件作用域内的已校验配置。
     *
     * @return 插件本地配置视图
     */
    PluginConfig config();

    /**
     * 返回只读 Capability 查询边界。
     *
     * @return 活动 Capability 查询服务
     */
    CapabilityManager capabilities();

    /**
     * 返回绑定到当前插件资源作用域的事件总线视图。
     *
     * @return 插件作用域事件总线
     */
    PluginEventBus events();

    /**
     * 返回当前启动周期的资源所有权作用域。
     *
     * @return 资源所有权作用域
     */
    ResourceScope resources();

    /**
     * 返回使用当前插件命名的日志记录器。
     *
     * @return 插件日志记录器
     */
    System.Logger logger();
}

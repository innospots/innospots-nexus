package com.innospots.nexus.core.plugin.contract;

import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

/**
 * 用于声明插件及其插件级生命周期的唯一 classpath SPI。
 */
public interface Plugin {

    /**
     * 返回不可变且无副作用的插件定义。
     *
     * @return 发现和预检校验阶段使用的插件声明
     */
    PluginDefinition definition();

    /**
     * 初始化一次启动周期的插件级状态。
     *
     * @param context 当前插件启动周期的作用域运行时服务
     */
    default void initialize(PluginContext context) {
    }

    /**
     * 在插件和 Provider 初始化完成后启动插件行为。
     *
     * <p>默认实现为空操作。
     */
    default void start() {
    }

    /**
     * 在释放插件资源作用域前停止插件级行为。
     *
     * <p>默认实现为空操作；在 {@link com.innospots.nexus.core.plugin.resource.ResourceScope}
     * 关闭之前调用。
     */
    default void stop() {
    }
}

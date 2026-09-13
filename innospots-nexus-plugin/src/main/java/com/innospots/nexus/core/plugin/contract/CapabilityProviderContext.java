package com.innospots.nexus.core.plugin.contract;

import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.config.PluginConfig;

/**
 * 面向一个已声明 Capability Provider 的专用插件上下文。
 */
public interface CapabilityProviderContext extends PluginContext {

    /**
     * 返回当前 Provider 的稳定身份。
     *
     * @return 插件与 Provider 的组合身份
     */
    ProviderRef providerRef();

    /**
     * 返回当前 Provider 的私有只读配置。
     *
     * @return Provider 作用域配置视图
     */
    PluginConfig providerConfig();

    /**
     * 返回当前正在初始化的 Capability 标识。
     *
     * @return 已声明的 Capability 身份
     */
    CapabilityKey capability();
}

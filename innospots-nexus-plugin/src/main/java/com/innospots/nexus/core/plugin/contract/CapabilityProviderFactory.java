package com.innospots.nexus.core.plugin.contract;

/**
 * 无副作用地创建一个全新的、尚未初始化的 Capability Provider。
 *
 * @param <T> Provider 类型
 */
@FunctionalInterface
public interface CapabilityProviderFactory<T extends CapabilityProvider> {

    /**
     * 为一次插件启动周期创建新的 Provider。
     *
     * @return 新的、尚未初始化的 Provider 实例
     */
    T create();
}

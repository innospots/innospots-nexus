package com.innospots.nexus.core.plugin.contract;

/**
 * 运行时管理的 Capability 实现所遵循的标记和生命周期契约。
 */
public interface CapabilityProvider {

    /**
     * 工厂创建新实例后初始化 Provider。
     *
     * @param context Provider 作用域运行时服务
     */
    default void initialize(CapabilityProviderContext context) {
    }

    /**
     * 在插件资源作用域关闭前释放 Provider 自有状态。
     *
     * <p>默认实现为空操作；在 {@link com.innospots.nexus.core.plugin.resource.ResourceScope}
     * 关闭之前调用。</p>
     */
    default void destroy() {
    }
}

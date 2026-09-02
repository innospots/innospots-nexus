package com.innospots.nexus.quarkus.plugin.config;

import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * 持有已启用的 {@link PluginInstallationManager}。
 *
 * <p>在 {@link PluginHostLifecycle} 启动后写入，供 REST 与业务代码在
 * {@link io.quarkus.runtime.StartupEvent} 完成之后访问。使用 {@code volatile}
 * 保证启动线程与请求线程之间的可见性。</p>
 */
@ApplicationScoped
public class PluginInstallationManagerHolder {

    private volatile PluginInstallationManager manager;

    /**
     * 记录 enable 成功后的安装管理器。
     *
     * @param manager 已启动的安装管理器
     */
    void setManager(PluginInstallationManager manager) {
        this.manager = manager;
    }

    /** 清空持有引用，避免关闭后误用。 */
    void clearManager() {
        this.manager = null;
    }

    /**
     * 返回当前持有的安装管理器。
     *
     * @return 安装管理器；未启动时返回 {@code null}
     */
    public PluginInstallationManager managerIfPresent() {
        return manager;
    }

    /**
     * 返回已启用的安装管理器。
     *
     * @return 安装管理器
     * @throws IllegalStateException 插件子系统尚未启动时
     */
    public PluginInstallationManager requireManager() {
        PluginInstallationManager current = manager;
        if (current == null) {
            throw new IllegalStateException("plugin subsystem has not started yet");
        }
        return current;
    }
}

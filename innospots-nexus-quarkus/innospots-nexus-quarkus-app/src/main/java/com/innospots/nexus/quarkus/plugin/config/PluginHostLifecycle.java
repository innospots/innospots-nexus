package com.innospots.nexus.quarkus.plugin.config;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import com.innospots.nexus.core.bootstrap.NexusStartup;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * 插件子系统 Quarkus 生命周期监听器。
 *
 * <p>在基础设施就绪后的 {@link StartupEvent} 中调用 {@link NexusStartup#run()}。</p>
 */
@ApplicationScoped
public class PluginHostLifecycle {

    private final NexusStartup nexusStartup;
    private final PluginInstallationManagerHolder managerHolder;

    /**
     * @param nexusStartup  启动编排入口
     * @param managerHolder 安装管理器持有器
     */
    @Inject
    public PluginHostLifecycle(NexusStartup nexusStartup, PluginInstallationManagerHolder managerHolder) {
        this.nexusStartup = nexusStartup;
        this.managerHolder = managerHolder;
    }

    /**
     * 应用启动完成后执行启动编排。
     *
     * @param event Quarkus 启动事件（未使用，仅作触发）
     */
    void onStart(@Observes @Priority(100) StartupEvent event) {
        nexusStartup.run();
    }

    /**
     * 应用关闭时按逆序释放插件运行时。
     *
     * @param event Quarkus 关闭事件（未使用，仅作触发）
     */
    void onStop(@Observes @Priority(100) ShutdownEvent event) {
        PluginInstallationManager manager = managerHolder.managerIfPresent();
        if (manager != null) {
            manager.close();
        }
        managerHolder.clearManager();
    }

    /**
     * 返回已启用的安装管理器，供业务 Service 查询 Capability。
     *
     * @return 安装管理器
     * @throws IllegalStateException 插件子系统尚未启动时
     */
    public PluginInstallationManager installationManager() {
        return managerHolder.requireManager();
    }
}

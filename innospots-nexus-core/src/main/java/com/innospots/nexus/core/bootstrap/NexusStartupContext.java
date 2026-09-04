package com.innospots.nexus.core.bootstrap;

import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;

/**
 * 启动编排过程中的跨步骤上下文。
 */
public final class NexusStartupContext {

    private PluginInstallationManager installationManager;

    /** 返回插件安装管理器；插件宿主任务完成前可能为 {@code null}。 */
    public PluginInstallationManager installationManager() {
        return installationManager;
    }

    /**
     * 由插件宿主启动任务写入安装管理器。
     *
     * @param installationManager 已启用的安装管理器
     */
    void attachInstallationManager(PluginInstallationManager installationManager) {
        this.installationManager = installationManager;
    }
}

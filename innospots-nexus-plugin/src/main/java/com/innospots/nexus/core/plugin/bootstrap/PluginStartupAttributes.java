package com.innospots.nexus.core.plugin.bootstrap;

import com.innospots.nexus.core.bootstrap.NexusStartupContext;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;

/**
 * 插件子系统的启动属性键与辅助方法。
 * @author Smars
 * @date 2026/09/13
 */
public final class PluginStartupAttributes {

    public static final String INSTALLATION_MANAGER = "plugin.installationManager";

    private PluginStartupAttributes() {
    }

    /**
     * 返回由 {@link PluginHostStartupTask} 附加的安装管理器。
     *
     * @param context 启动上下文
     * @return 插件宿主启动完成后的安装管理器
     */
    public static PluginInstallationManager installationManager(NexusStartupContext context) {
        return context.getAttribute(INSTALLATION_MANAGER, PluginInstallationManager.class).orElse(null);
    }
}

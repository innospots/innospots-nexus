package com.innospots.nexus.core.bootstrap;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrap;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;

/**
 * 内置启动任务：启用插件子系统。
 */
public final class PluginHostStartupTask implements NexusStartupTask {

    private static final int ORDER = 100;

    private final Supplier<PluginHostBootstrapRequest> requestSupplier;
    private final Consumer<PluginInstallationManager> installationManagerConsumer;

    /**
     * 创建插件宿主启动任务。
     *
     * @param requestSupplier             插件宿主启用依赖供应器
     * @param installationManagerConsumer 安装管理器就绪后的回调；可为 {@code null}
     */
    public PluginHostStartupTask(
            Supplier<PluginHostBootstrapRequest> requestSupplier,
            Consumer<PluginInstallationManager> installationManagerConsumer
    ) {
        if (requestSupplier == null) {
            throw new IllegalArgumentException("requestSupplier is required");
        }
        this.requestSupplier = requestSupplier;
        this.installationManagerConsumer = installationManagerConsumer;
    }

    @Override
    public String name() {
        return "plugin-host";
    }

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public void run(NexusStartupContext context) {
        PluginInstallationManager manager = PluginHostBootstrap.enable(requestSupplier.get());
        context.attachInstallationManager(manager);
        if (installationManagerConsumer != null) {
            installationManagerConsumer.accept(manager);
        }
    }
}

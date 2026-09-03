package com.innospots.nexus.core.plugin.installation.bootstrap;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.discovery.ClasspathPluginDiscovery;
import com.innospots.nexus.core.plugin.discovery.PluginDiscoveryReport;
import com.innospots.nexus.core.plugin.installation.repository.PluginInstallationRepository;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.installation.service.PluginRuntimeFactory;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 应用宿主启用插件子系统的唯一入口。
 *
 * <p>按固定顺序执行 classpath 发现、安装事实对账、运行时创建与 eligible 插件启动。
 * 返回的 {@link PluginInstallationManager} 由宿主持有，用于管理命令、Capability 查询与关闭。</p>
 */
public final class PluginHostBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(PluginHostBootstrap.class);

    private PluginHostBootstrap() {
    }

    /**
     * 完成插件子系统启用：发现 → 对账 → 启动 eligible 插件。
     *
     * @param request 宿主注入的全部依赖
     * @return 已启动的安装管理器；关闭时调用 {@link PluginInstallationManager#close()}
     * @throws NexusException 发现、对账或启动失败时
     */
    public static PluginInstallationManager enable(PluginHostBootstrapRequest request) {
        if (request == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "plugin host bootstrap request is required");
        }
        logger.info("Enabling plugin host");
        ClassLoader classLoader = request.resolvedClassLoader();
        // 发现阶段：枚举 SPI 与 YAML 并编译为统一 Plugin 目录。
        PluginDiscoveryReport report = new ClasspathPluginDiscovery(
                classLoader,
                request.contributionDecoders())
                .discoverReport();
        PluginInstallationRepository repository = new PluginInstallationRepository(request.installationDao());
        PluginRuntimeFactory runtimeFactory = new PluginRuntimeFactory(
                request.runtimeConfig(),
                request.contributionHandlers(),
                request.contributionSnapshotters());
        // 对账与启动阶段：登记安装事实、对齐 MISSING 状态并启动 eligible 插件。
        PluginInstallationManager installationManager = new PluginInstallationManager(
                repository,
                runtimeFactory,
                request.installationConfig(),
                report);
        installationManager.start();
        logger.info("Plugin host enabled");
        return installationManager;
    }
}

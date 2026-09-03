package com.innospots.nexus.core.plugin.installation.bootstrap;

import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 应用宿主启用插件子系统所需的全部依赖。
 *
 * @param installationDao           安装表 DAO
 * @param runtimeConfig             运行时配置
 * @param installationConfig        安装策略（含是否自动安装）
 * @param contributionDecoders      YAML Contribution 解码器表
 * @param contributionHandlers      运行时 Contribution Handler 列表
 * @param contributionSnapshotters  安装快照序列化器表
 * @param pluginClassLoader         发现用的类加载器；为 {@code null} 时回退到运行时配置或当前线程 CL
 */
public record PluginHostBootstrapRequest(
        PluginInstallationDao installationDao,
        PluginRuntimeConfig runtimeConfig,
        PluginInstallationConfig installationConfig,
        PluginContributionDecoderRegistry contributionDecoders,
        List<PluginContributionHandler<?>> contributionHandlers,
        PluginContributionSnapshotterRegistry contributionSnapshotters,
        ClassLoader pluginClassLoader
) {

    /**
     * @throws NexusException 任一必需依赖为 {@code null}，或 Handler 列表含 {@code null} 元素时
     */
    public PluginHostBootstrapRequest {
        if (installationDao == null || runtimeConfig == null || installationConfig == null
                || contributionDecoders == null || contributionSnapshotters == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "plugin host bootstrap dependencies are required");
        }
        if (contributionHandlers == null || contributionHandlers.stream().anyMatch(java.util.Objects::isNull)) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "contribution handlers must not be null");
        }
        contributionHandlers = List.copyOf(contributionHandlers);
    }

    /**
     * 解析用于 classpath 发现的类加载器。
     *
     * <p>优先使用显式注入的类加载器，再回退到运行时配置、线程上下文和定义类加载器，
     * 以兼容 Spring/Quarkus 宿主在不同启动阶段的 CL 可见性差异。</p>
     *
     * @return 非空类加载器
     */
    ClassLoader resolvedClassLoader() {
        if (pluginClassLoader != null) {
            return pluginClassLoader;
        }
        ClassLoader fromConfig = runtimeConfig.pluginClassLoader();
        if (fromConfig != null) {
            return fromConfig;
        }
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (context != null) {
            return context;
        }
        return PluginHostBootstrapRequest.class.getClassLoader();
    }
}

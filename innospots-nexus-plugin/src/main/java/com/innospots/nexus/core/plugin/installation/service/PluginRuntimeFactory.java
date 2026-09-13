package com.innospots.nexus.core.plugin.installation.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.runtime.DefaultPluginManager;
import com.innospots.nexus.core.plugin.runtime.PluginManager;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Core 运行时工厂，集中组装唯一的 PluginManager，不访问数据库或 Console。 */
public final class PluginRuntimeFactory {

    private static final Logger logger = LoggerFactory.getLogger(PluginRuntimeFactory.class);

    private final PluginRuntimeConfig baseConfig;
    private final List<PluginContributionHandler<?>> handlers;
    private final PluginContributionSnapshotterRegistry snapshotters;

    /**
     * 创建带宿主运行参数和 Contribution Handler 的运行时工厂。
     *
     * @param baseConfig 宿主运行时基础配置
     * @param handlers 按类型注册的 Contribution Handler 列表
     * @param snapshotters 宿主注册的 Contribution 快照器表
     * @throws NexusException 任一依赖为空时抛出
     */
    public PluginRuntimeFactory(
            PluginRuntimeConfig baseConfig,
            List<PluginContributionHandler<?>> handlers,
            PluginContributionSnapshotterRegistry snapshotters
    ) {
        if (baseConfig == null || handlers == null || snapshotters == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "runtime factory dependencies are required");
        }
        this.baseConfig = baseConfig;
        this.handlers = List.copyOf(handlers);
        this.snapshotters = snapshotters;
    }

    /**
     * 创建本次安装对账允许启动的运行时。
     *
     * @param catalog 已通过全局校验的插件目录
     * @param eligiblePluginIds 已安装且期望启用的插件身份
     * @return 独立 PluginManager
     */
    public PluginManager create(PluginCatalog catalog, Set<String> eligiblePluginIds) {
        if (catalog == null || eligiblePluginIds == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "catalog and eligible plugin ids are required");
        }
        validateContributionHandlers(catalog);
        Set<String> allIds = catalog.plugins().stream()
                .map(item -> item.definition().pluginId())
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        Set<String> disabled = new HashSet<>(baseConfig.disabledPluginIds());
        // 未进入 eligible 集合的已发现插件视为 disabled，即使宿主未显式配置。
        allIds.stream().filter(id -> !eligiblePluginIds.contains(id)).forEach(disabled::add);
        Set<String> required = new HashSet<>(baseConfig.requiredPluginIds());
        // required 仅对本次会尝试启动的插件生效，避免对未安装插件误报启动失败。
        required.retainAll(eligiblePluginIds);
        PluginRuntimeConfig effective = new PluginRuntimeConfig(
                required,
                disabled,
                baseConfig.hostConfig(),
                baseConfig.configSources(),
                baseConfig.runtimeVariables(),
                baseConfig.defaultRoutes(),
                baseConfig.pluginClassLoader());
        logger.info("Creating plugin runtime: eligible={}, disabled={}, required={}",
                eligiblePluginIds.size(), disabled.size(), required);
        return DefaultPluginManager.create(effective, catalog, handlers);
    }

    /**
     * 返回工厂使用的安全 Contribution 快照器。
     *
     * @return 不可变快照器注册表
     */
    public PluginContributionSnapshotterRegistry snapshotters() {
        return snapshotters;
    }

    /**
     * 返回当前工厂配置的 Contribution Handler 快照。
     *
     * @return 不可变 Handler 列表
     */
    public List<PluginContributionHandler<?>> handlers() {
        return handlers;
    }

    private void validateContributionHandlers(PluginCatalog catalog) {
        Map<com.innospots.nexus.core.plugin.contribution.PluginContributionType<?>,
                PluginContributionHandler<?>> byType = handlers.stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        PluginContributionHandler::type,
                        handler -> handler,
                        (left, right) -> {
                            throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                                    "duplicate contribution handler: " + left.type());
                        }));
        for (var item : catalog.plugins()) {
            for (PluginContribution contribution : item.definition().contributions()) {
                if (!byType.containsKey(contribution.type())) {
                    throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                            "missing contribution handler: " + contribution.type());
                }
                if (snapshotters.find(contribution.type()).isEmpty()) {
                    throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                            "missing contribution snapshotter: " + contribution.type());
                }
            }
        }
    }
}

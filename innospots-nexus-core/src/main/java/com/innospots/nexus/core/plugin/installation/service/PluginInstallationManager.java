package com.innospots.nexus.core.plugin.installation.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.discovery.PluginDiscoveryReport;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.domain.enums.PluginPresence;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginDefinitionSnapshot;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginDefinitionSnapshotMapper;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginInstallation;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginManagementView;
import com.innospots.nexus.core.plugin.installation.repository.PluginInstallationRepository;
import com.innospots.nexus.core.plugin.capability.CapabilityManager;
import com.innospots.nexus.core.plugin.lifecycle.PluginRuntimeInfo;
import com.innospots.nexus.core.plugin.lifecycle.PluginState;
import com.innospots.nexus.core.plugin.runtime.PluginManager;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core 插件安装管理器。
 *
 * <p>该类只协调安装事实与当前 JVM 运行时，管理命令按“提交意图、执行运行时、记录诊断”执行，
 * 不把安装状态写入 {@link PluginRuntimeInfo}。</p>
 */
public final class PluginInstallationManager implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(PluginInstallationManager.class);

    private final PluginInstallationRepository repository;
    private final PluginRuntimeFactory runtimeFactory;
    private final PluginInstallationConfig installationConfig;
    private final Object lifecycleMonitor = new Object();
    private final Map<String, ReentrantLock> commandLocks = new ConcurrentHashMap<>();

    private PluginDiscoveryReport discoveryReport;
    private PluginCatalog catalog;
    private PluginManager runtime;
    private boolean reconciled;
    private boolean closed;

    /**
     * 创建使用有效发现报告的安装管理器。
     *
     * @param repository          安装事实仓储
     * @param runtimeFactory      运行时工厂
     * @param installationConfig  安装行为配置
     * @param discoveryReport     已完成发现的报告
     * @throws NexusException 任一依赖为 {@code null} 时
     */
    public PluginInstallationManager(
            PluginInstallationRepository repository,
            PluginRuntimeFactory runtimeFactory,
            PluginInstallationConfig installationConfig,
            PluginDiscoveryReport discoveryReport
    ) {
        if (repository == null || runtimeFactory == null || installationConfig == null
                || discoveryReport == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "plugin installation manager dependencies are required");
        }
        this.repository = repository;
        this.runtimeFactory = runtimeFactory;
        this.installationConfig = installationConfig;
        this.discoveryReport = discoveryReport;
        this.catalog = discoveryReport.validCatalog();
    }

    /**
     * 对账当前发现报告。
     *
     * <p>拒绝列表中的插件不会形成安装事实。
     *
     * @throws NexusException 管理器已关闭或运行时仍活跃时
     */
    public void reconcile() {
        PluginDiscoveryReport report;
        synchronized (lifecycleMonitor) {
            ensureOpen();
            report = discoveryReport;
        }
        reconcile(report);
    }

    /**
     * 替换发现报告并执行一次全量对账。
     *
     * @param report 新的发现报告
     * @throws NexusException 报告无效、管理器已关闭或运行时仍活跃时
     */
    public void reconcile(PluginDiscoveryReport report) {
        if (report == null || report.validCatalog() == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "plugin discovery report is required");
        }
        synchronized (lifecycleMonitor) {
            ensureOpen();
            if (runtime != null) {
                throw NexusException.build(PluginStatusCode.PLUGIN_CONCURRENCY_CONFLICT,
                        "cannot reconcile while plugin runtime is active");
            }
            List<PluginDefinitionSnapshot> snapshots = report.validCatalog().plugins().stream()
                    .map(this::snapshot)
                    .toList();
            for (PluginDefinitionSnapshot snapshot : snapshots) {
                repository.register(snapshot, installationConfig.autoInstall());
            }
            repository.markMissing(report.validCatalog().plugins().stream()
                    .map(item -> item.definition().pluginId())
                    .toList());
            this.discoveryReport = report;
            this.catalog = report.validCatalog();
            this.reconciled = true;
            logger.info("Plugin installation reconciled: {} valid plugin(s), {} rejected",
                    snapshots.size(), report.rejectedDefinitions().size());
        }
    }

    /**
     * 对账后创建唯一运行时并启动已安装且期望启用的插件。
     *
     * <p>重复调用在运行时已存在时保持幂等。
     *
     * @throws NexusException 对账失败、启动失败或管理器已关闭时
     */
    public void start() {
        PluginManager manager;
        Set<String> eligible;
        synchronized (lifecycleMonitor) {
            ensureOpen();
            if (!reconciled) {
                reconcile(discoveryReport);
            }
            if (runtime != null) {
                return;
            }
            eligible = repository.findAll().stream()
                    .filter(this::eligible)
                    .map(PluginInstallation::pluginId)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            manager = runtimeFactory.create(catalog, eligible);
            runtime = manager;
        }
        logger.info("Starting plugin installation runtime with {} eligible plugin(s)", eligible.size());
        try {
            manager.start();
            recordRuntimeDiagnostics(manager.plugins());
            logger.info("Plugin installation runtime started");
        } catch (RuntimeException exception) {
            logger.warn("Plugin installation runtime failed to start", exception);
            try {
                recordRuntimeDiagnostics(manager.plugins());
            } catch (RuntimeException diagnosticsFailure) {
                exception.addSuppressed(diagnosticsFailure);
            }
            throw exception;
        }
    }

    /**
     * 返回已启动运行时的 Capability 查询边界。
     *
     * @return 活动 Capability 管理器
     * @throws NexusException 运行时尚未创建或管理器已关闭时
     */
    public CapabilityManager capabilities() {
        return runtimeOrThrow().capabilities();
    }

    /**
     * 查询全部插件的持久化事实和当前运行快照。
     *
     * @return 聚合管理视图列表
     * @throws NexusException 管理器已关闭时
     */
    public List<PluginManagementView> plugins() {
        ensureOpen();
        Map<String, PluginRuntimeInfo> runtimes = runtimeSnapshots();
        return repository.findAll().stream()
                .map(installation -> view(installation, runtimes.get(installation.pluginId())))
                .toList();
    }

    /**
     * 查询一个插件的聚合管理视图。
     *
     * @param pluginId 插件标识
     * @return 管理视图；安装事实不存在时为空
     * @throws NexusException 管理器已关闭时
     */
    public Optional<PluginManagementView> plugin(String pluginId) {
        ensureOpen();
        PluginInstallation installation = repository.find(pluginId).orElse(null);
        if (installation == null) {
            return Optional.empty();
        }
        return Optional.of(view(installation, runtimeSnapshot(pluginId)));
    }

    /**
     * 安装并启动一个当前存在于有效 Catalog 的插件。
     *
     * @param pluginId 插件标识
     * @return 更新后的管理视图
     * @throws NexusException 插件缺失、未安装或启动失败时
     */
    public PluginManagementView installAndStart(String pluginId) {
        return withPluginLock(pluginId, () -> {
            logger.info("Installing and starting plugin {}", pluginId);
            PluginInstallation current = requireInstallation(pluginId);
            requirePresent(current);
            ensureRuntime();
            repository.setIntent(pluginId, true, true);
            return startAndRecord(pluginId);
        });
    }

    /**
     * 启用一个已安装且当前存在的插件。
     *
     * @param pluginId 插件标识
     * @return 更新后的管理视图
     * @throws NexusException 插件未安装、缺失或启动失败时
     */
    public PluginManagementView enable(String pluginId) {
        return withPluginLock(pluginId, () -> {
            logger.info("Enabling plugin {}", pluginId);
            PluginInstallation current = requireInstallation(pluginId);
            requirePresent(current);
            if (!current.installed()) {
                throw NexusException.build(PluginStatusCode.PLUGIN_NOT_INSTALLED,
                        "plugin is not installed: " + pluginId);
            }
            ensureRuntime();
            repository.setIntent(pluginId, true, true);
            return startAndRecord(pluginId);
        });
    }

    /**
     * 先提交停用意图，再停止当前运行实例。
     *
     * <p>停止失败不会恢复启用意图。
     *
     * @param pluginId 插件标识
     * @return 更新后的管理视图
     * @throws NexusException 插件未安装、缺失或停止失败时
     */
    public PluginManagementView disable(String pluginId) {
        return withPluginLock(pluginId, () -> {
            logger.info("Disabling plugin {}", pluginId);
            PluginInstallation current = requireInstallation(pluginId);
            requirePresent(current);
            if (!current.installed()) {
                throw NexusException.build(PluginStatusCode.PLUGIN_NOT_INSTALLED,
                        "plugin is not installed: " + pluginId);
            }
            ensureRuntime();
            repository.setIntent(pluginId, true, false);
            try {
                runtimeOrThrow().stop(pluginId);
                return recordCurrentRuntime(pluginId);
            } catch (RuntimeException exception) {
                recordRuntimeFailure(pluginId, exception);
                throw exception;
            }
        });
    }

    /**
     * 重试已安装、期望启用且当前失败的插件。
     *
     * <p>不修改安装意图。
     *
     * @param pluginId 插件标识
     * @return 更新后的管理视图
     * @throws NexusException 插件状态不是 {@link PluginState#FAILED} 或启动失败时
     */
    public PluginManagementView retryStart(String pluginId) {
        return withPluginLock(pluginId, () -> {
            logger.info("Retrying start for plugin {}", pluginId);
            PluginInstallation current = requireInstallation(pluginId);
            requirePresent(current);
            if (!current.installed() || !current.desiredEnabled()) {
                throw NexusException.build(PluginStatusCode.PLUGIN_NOT_INSTALLED,
                        "plugin is not installed and enabled: " + pluginId);
            }
            ensureRuntime();
            PluginRuntimeInfo info = runtimeOrThrow().plugin(pluginId).orElseThrow(() ->
                    NexusException.build(PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                            "plugin was not discovered: " + pluginId));
            if (info.state() != PluginState.FAILED) {
                throw NexusException.build(PluginStatusCode.PLUGIN_CONCURRENCY_CONFLICT,
                        "plugin is not in FAILED state: " + pluginId);
            }
            return startAndRecord(pluginId);
        });
    }

    /**
     * 关闭此管理器拥有的唯一 {@link PluginManager}。
     *
     * <p>重复关闭保持幂等。
     */
    @Override
    public void close() {
        PluginManager current;
        synchronized (lifecycleMonitor) {
            if (closed) {
                return;
            }
            current = runtime;
            runtime = null;
            closed = true;
        }
        if (current != null) {
            logger.info("Closing plugin installation manager runtime");
            current.close();
        }
    }

    private PluginDefinitionSnapshot snapshot(DiscoveredPlugin discovered) {
        return PluginDefinitionSnapshotMapper.from(
                discovered.definition(), discovered.source(), runtimeFactory.snapshotters());
    }

    private PluginManagementView startAndRecord(String pluginId) {
        try {
            runtimeOrThrow().start(pluginId);
            return recordCurrentRuntime(pluginId);
        } catch (RuntimeException exception) {
            recordRuntimeFailure(pluginId, exception);
            throw exception;
        }
    }

    private PluginManagementView recordCurrentRuntime(String pluginId) {
        PluginRuntimeInfo info = runtimeOrThrow().plugin(pluginId).orElse(null);
        String state = info == null ? null : info.state().name();
        String error = info == null ? null : info.lastError();
        PluginInstallation installation = repository.updateRuntime(pluginId, state, error);
        return view(installation, info);
    }

    private void recordRuntimeFailure(String pluginId, RuntimeException exception) {
        PluginRuntimeInfo info = runtime == null ? null : runtime.plugin(pluginId).orElse(null);
        String state = info == null ? PluginState.FAILED.name() : info.state().name();
        String error = info != null && info.lastError() != null
                ? info.lastError()
                : exception.getMessage();
        try {
            repository.updateRuntime(pluginId, state, error);
        } catch (RuntimeException persistenceFailure) {
            exception.addSuppressed(persistenceFailure);
        }
    }

    private void recordRuntimeDiagnostics(Collection<PluginRuntimeInfo> infos) {
        for (PluginRuntimeInfo info : infos) {
            try {
                repository.updateRuntime(info.id(), info.state().name(), info.lastError());
            } catch (RuntimeException ignored) {
                // 启动异常已经由运行时返回；诊断写入失败不能伪装成成功状态。
            }
        }
    }

    private Map<String, PluginRuntimeInfo> runtimeSnapshots() {
        PluginManager current = runtime;
        if (current == null) {
            return Map.of();
        }
        Map<String, PluginRuntimeInfo> snapshots = new LinkedHashMap<>();
        for (PluginRuntimeInfo info : current.plugins()) {
            snapshots.put(info.id(), info);
        }
        return Map.copyOf(snapshots);
    }

    private PluginRuntimeInfo runtimeSnapshot(String pluginId) {
        PluginManager current = runtime;
        return current == null ? null : current.plugin(pluginId).orElse(null);
    }

    private PluginManagementView view(PluginInstallation installation, PluginRuntimeInfo info) {
        return new PluginManagementView(installation, Optional.ofNullable(info));
    }

    private PluginInstallation requireInstallation(String pluginId) {
        return repository.require(pluginId);
    }

    private void requirePresent(PluginInstallation installation) {
        if (installation.presence() != PluginPresence.PRESENT) {
            throw NexusException.build(PluginStatusCode.PLUGIN_MISSING,
                    "plugin is missing from the current catalog: " + installation.pluginId());
        }
    }

    private boolean eligible(PluginInstallation installation) {
        return installation.presence() == PluginPresence.PRESENT
                && installation.installed() && installation.desiredEnabled();
    }

    private void ensureRuntime() {
        synchronized (lifecycleMonitor) {
            ensureOpen();
            if (!reconciled) {
                reconcile(discoveryReport);
            }
            if (runtime == null) {
                Set<String> eligible = repository.findAll().stream()
                        .filter(this::eligible)
                        .map(PluginInstallation::pluginId)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet());
                runtime = runtimeFactory.create(catalog, eligible);
            }
        }
    }

    private PluginManager runtimeOrThrow() {
        PluginManager current = runtime;
        if (current == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "plugin runtime has not been created");
        }
        return current;
    }

    private <T> T withPluginLock(String pluginId, java.util.function.Supplier<T> command) {
        if (pluginId == null || pluginId.isBlank()) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "pluginId is required");
        }
        ReentrantLock lock = commandLocks.computeIfAbsent(pluginId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            ensureOpen();
            return command.get();
        } finally {
            lock.unlock();
        }
    }

    private void ensureOpen() {
        synchronized (lifecycleMonitor) {
            if (closed) {
                throw NexusException.build(PluginStatusCode.PLUGIN_STOP_FAILED,
                        "plugin installation manager is closed");
            }
        }
    }
}

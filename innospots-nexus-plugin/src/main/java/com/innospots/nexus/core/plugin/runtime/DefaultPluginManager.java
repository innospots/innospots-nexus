package com.innospots.nexus.core.plugin.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.CapabilityManager;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistration;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistry;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigurationManager;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.contribution.PluginContributionEntry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionType;
import com.innospots.nexus.core.plugin.declaration.CapabilityRequirement;
import com.innospots.nexus.core.plugin.dependency.DependencyResolution;
import com.innospots.nexus.core.plugin.dependency.DependencyResolver;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.event.DefaultPluginEventBus;
import com.innospots.nexus.core.plugin.lifecycle.ManagedPlugin;
import com.innospots.nexus.core.plugin.lifecycle.PluginAvailabilityIndex;
import com.innospots.nexus.core.plugin.lifecycle.PluginRuntimeInfo;
import com.innospots.nexus.core.plugin.lifecycle.PluginState;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 轻量级依赖感知插件运行时，使用一把串行生命周期锁，不持有全局单例状态。
 *
 * <p>消费宿主已完成的全局校验目录；不在本类内执行 classpath 发现。管理操作通过短暂令牌串行化；
 * 插件生命周期代码不在该锁内执行。每个实例独立，关闭后不可复用。</p>
 */
public final class DefaultPluginManager implements PluginManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPluginManager.class);

    private PluginRuntimeConfig config;
    private final PluginAvailabilityIndex availabilityIndex = new PluginAvailabilityIndex();
    private final CapabilityRegistry registry;
    private final DefaultPluginEventBus eventBus = new DefaultPluginEventBus();
    private final List<PluginContributionHandler<?>> contributionHandlers;
    private final List<String> startupOrder = new ArrayList<>();
    private final Object operationMonitor = new Object();
    private Map<String, ManagedPlugin> managedPlugins;
    private DependencyResolver dependencyResolver;
    private boolean operationInProgress;
    private volatile boolean closed;

    private DefaultPluginManager(
            PluginRuntimeConfig config,
            PluginCatalog catalog,
            List<PluginContributionHandler<?>> contributionHandlers
    ) {
        if (config == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, "plugin runtime config is required");
        }
        if (catalog == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DISCOVERY_FAILED, "plugin catalog is required");
        }
        if (contributionHandlers == null || contributionHandlers.stream().anyMatch(java.util.Objects::isNull)) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "contribution handlers must not be null");
        }
        this.config = config;
        this.contributionHandlers = List.copyOf(contributionHandlers);
        this.registry = new CapabilityRegistry(config.defaultRoutes(), availabilityIndex);
        initialize(catalog);
    }

    /**
     * 根据已发现目录创建运行时管理器，并在构造时完成预检与 {@link ManagedPlugin} 装配。
     *
     * @param config 宿主运行时配置
     * @param catalog 已完成全局校验的发现目录
     * @param contributionHandlers 按类型注册的 Contribution Handler 列表
     * @return 独立的插件管理器实例
     */
    public static DefaultPluginManager create(
            PluginRuntimeConfig config,
            PluginCatalog catalog,
            List<PluginContributionHandler<?>> contributionHandlers
    ) {
        return new DefaultPluginManager(config, catalog, contributionHandlers);
    }

    /**
     * 以依赖感知的多轮方式发现并启动符合条件的插件。
     *
     * @throws NexusException 发现失败或必需插件无法激活时抛出
     */
    @Override
    public void start() {
        enterOperation();
        try {
            ensureOpen();
            logger.info("Starting plugin runtime for {} managed plugins", managedPlugins.size());
            Set<String> attempted = new LinkedHashSet<>();
            List<String> startedThisCall = new ArrayList<>();
            List<NexusException> startupFailures = new ArrayList<>();
            boolean progressed;
            do {
                progressed = false;
                // 多轮扫描：上一轮新启动的插件可能满足本轮等待者的 Capability 依赖。
                for (ManagedPlugin plugin : managedPlugins.values()) {
                    String pluginId = plugin.definition().pluginId();
                    PluginState state = plugin.info().state();
                    if (config.disabledPluginIds().contains(pluginId)
                            || state == PluginState.ACTIVE
                            || attempted.contains(pluginId)) {
                        continue;
                    }
                    Map<CapabilityKey, DependencyResolution> dependencies = dependencyResolver.resolve(plugin.definition());
                    plugin.dependencies(dependencies);
                    if (!dependencyResolver.canStart(dependencies)) {
                        plugin.waiting(dependencies);
                        logger.debug("Plugin {} waiting for dependencies", pluginId);
                        continue;
                    }
                    attempted.add(pluginId);
                    try {
                        plugin.start();
                        startupOrder.add(pluginId);
                        startedThisCall.add(pluginId);
                        progressed = true;
                        logger.info("Plugin {} started", pluginId);
                    } catch (NexusException exception) {
                        // 普通插件失败与其它插件隔离，宿主是否失败由后续必需插件校验决定。
                        logger.warn("Plugin {} failed to start", pluginId, exception);
                        startupFailures.add(exception);
                    }
                }
            } while (progressed);

            // required 插件未全部 ACTIVE 时回滚本次启动批次，避免半初始化运行时对外暴露。
            List<String> inactiveRequired = config.requiredPluginIds().stream()
                    .filter(id -> managedPlugins.get(id) == null
                            || managedPlugins.get(id).info().state() != PluginState.ACTIVE)
                    .sorted()
                    .toList();
            if (!inactiveRequired.isEmpty()) {
                logger.error("Required plugins did not become active: {}", inactiveRequired);
                rollbackStarted(startedThisCall);
                NexusException failure = NexusException.build(
                        PluginStatusCode.PLUGIN_START_FAILED.fullCode(),
                        "required plugins did not become active: " + inactiveRequired,
                        startupFailures.isEmpty() ? null : startupFailures.getFirst());
                for (int index = 1; index < startupFailures.size(); index++) {
                    failure.addSuppressed(startupFailures.get(index));
                }
                throw failure;
            }
            logger.info("Plugin runtime started: {} active plugin(s)", startupOrder.size());
        } finally {
            leaveOperation();
        }
    }

    /**
     * 解析当前依赖诊断后启动指定插件。
     *
     * @param pluginId 稳定的插件标识
     * @throws NexusException 插件未知、被依赖阻断或启动失败时抛出
     */
    @Override
    public void start(String pluginId) {
        enterOperation();
        try {
            ensureOpen();
            ManagedPlugin plugin = requirePlugin(pluginId);
            Map<CapabilityKey, DependencyResolution> dependencies = dependencyResolver.resolve(plugin.definition());
            plugin.dependencies(dependencies);
            if (!dependencyResolver.canStart(dependencies)) {
                plugin.waiting(dependencies);
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                        "required capabilities are unavailable for plugin " + pluginId);
            }
            plugin.start();
            if (!startupOrder.contains(pluginId)) {
                startupOrder.add(pluginId);
            }
            logger.info("Plugin {} started on demand", pluginId);
        } finally {
            leaveOperation();
        }
    }

    /**
     * 检查不会移除活动依赖的最后一个 Provider 后停止指定活动插件。
     *
     * @param pluginId 稳定的插件标识
     * @throws NexusException 插件未知、仍被使用或停止失败时抛出
     */
    @Override
    public void stop(String pluginId) {
        enterOperation();
        try {
            ensureOpen();
            ManagedPlugin target = requirePlugin(pluginId);
            if (target.info().state() != PluginState.ACTIVE) {
                return;
            }
            protectRequiredCapabilities(target);
            target.stop();
            startupOrder.remove(pluginId);
            logger.info("Plugin {} stopped", pluginId);
        } finally {
            leaveOperation();
        }
    }

    /**
     * 返回当前全部插件运行快照，并按标识排序。
     *
     * @return 不可变运行快照列表
     */
    @Override
    public List<PluginRuntimeInfo> plugins() {
        enterOperation();
        try {
            ensureOpen();
            return managedPlugins.values().stream()
                    .map(ManagedPlugin::info)
                    .sorted(Comparator.comparing(PluginRuntimeInfo::id))
                    .toList();
        } finally {
            leaveOperation();
        }
    }

    /**
     * 查找一个当前插件运行快照。
     *
     * @param pluginId 稳定的插件标识
     * @return 匹配的快照；未发现时返回空 Optional
     */
    @Override
    public Optional<PluginRuntimeInfo> plugin(String pluginId) {
        enterOperation();
        try {
            ensureOpen();
            ManagedPlugin plugin = managedPlugins.get(pluginId);
            return plugin == null ? Optional.empty() : Optional.of(plugin.info());
        } finally {
            leaveOperation();
        }
    }

    /**
     * 返回此管理器的活动 Capability 查询边界。
     *
     * @return 活动 Capability 管理器
     */
    @Override
    public CapabilityManager capabilities() {
        enterOperation();
        try {
            ensureOpen();
            return registry;
        } finally {
            leaveOperation();
        }
    }

    /**
     * 按启动顺序逆序停止活动插件并释放运行时引用。
     *
     * @throws NexusException 一个或多个插件停止失败时抛出，但仍会尝试停止全部插件
     */
    @Override
    public void close() {
        enterOperation();
        try {
            if (closed) {
                return;
            }
            logger.info("Closing plugin runtime");
            List<String> reverse = new ArrayList<>(startupOrder);
            Collections.reverse(reverse);
            RuntimeException first = null;
            for (String pluginId : reverse) {
                try {
                    managedPlugins.get(pluginId).stop();
                } catch (RuntimeException exception) {
                    logger.warn("Plugin {} failed during runtime shutdown", pluginId, exception);
                    if (first == null) {
                        first = exception;
                    } else {
                        first.addSuppressed(exception);
                    }
                }
            }
            startupOrder.clear();
            managedPlugins = null;
            dependencyResolver = null;
            eventBus.close();
            config = null;
            closed = true;
            if (first != null) {
                throw first;
            }
            logger.info("Plugin runtime closed");
        } finally {
            leaveOperation();
        }
    }

    private void initialize(PluginCatalog catalog) {
        List<DiscoveredPlugin> discoveries = catalog.plugins();
        ConfigurationManager configuration = ConfigurationManager.standard(
                config.hostConfig(),
                config.configSources(),
                config.runtimeVariables());
        // 构造期完成全部全局预检，避免 ManagedPlugin 启动时才暴露 classpath 级冲突。
        validateRequiredPlugins(discoveries);
        validateDefaultRoutes(discoveries);
        validateContributions(discoveries, catalog);
        ConfigurationManager.validateEnvironmentNames(
                discoveries.stream().map(DiscoveredPlugin::definition).toList());
        Map<String, ManagedPlugin> created = new LinkedHashMap<>();
        for (DiscoveredPlugin discovery : discoveries) {
            ManagedPlugin managed = new ManagedPlugin(
                    discovery,
                    () -> configuration.resolve(discovery.definition()),
                    contribution -> configuration.resolveProvider(discovery.definition(), contribution),
                    registry,
                    eventBus,
                    contributionHandlers);
            availabilityIndex.register(discovery.definition().pluginId(), managed.availability());
            created.put(discovery.definition().pluginId(), managed);
        }
        managedPlugins = Collections.unmodifiableMap(created);
        dependencyResolver = new DependencyResolver(
                discoveries.stream().map(DiscoveredPlugin::definition).toList(),
                registry);
        logger.info("Plugin runtime initialized: {} plugin(s), disabled={}, required={}",
                created.size(),
                config.disabledPluginIds(),
                config.requiredPluginIds());
    }

    private void validateContributions(List<DiscoveredPlugin> discoveries, PluginCatalog catalog) {
        Map<PluginContributionType<?>, PluginContributionHandler<?>> handlers = contributionHandlers.stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        PluginContributionHandler::type,
                        handler -> handler,
                        (left, right) -> {
                            throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                                    "duplicate contribution handler: " + left.type());
                        }));
        Map<PluginContributionType<?>, List<PluginContributionEntry<?>>> entries = new LinkedHashMap<>();
        for (DiscoveredPlugin discovered : discoveries) {
            for (PluginContribution contribution : discovered.definition().contributions()) {
                PluginContributionHandler<?> handler = handlers.get(contribution.type());
                if (handler == null) {
                    throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                            "missing contribution handler: " + contribution.type());
                }
                // Contribution 无独立 providerId；合成稳定身份供 Handler 与权限同步使用。
                String providerId = "contribution-" + contribution.type().name().replace('.', '-')
                        + "-" + contribution.type().majorVersion();
                entries.computeIfAbsent(contribution.type(), ignored -> new ArrayList<>())
                        .add(new PluginContributionEntry<>(
                                new ProviderRef(discovered.definition().pluginId(), providerId),
                                contribution));
            }
        }
        for (PluginContributionHandler<?> handler : contributionHandlers) {
            validateHandler(handler, catalog, entries.getOrDefault(handler.type(), List.of()));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void validateHandler(
            PluginContributionHandler handler,
            PluginCatalog catalog,
            List<PluginContributionEntry<?>> entries
    ) {
        handler.validate(catalog, entries);
    }

    private void ensureOpen() {
        if (closed) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_STOP_FAILED,
                    "plugin manager is already closed");
        }
    }

    private void validateRequiredPlugins(List<DiscoveredPlugin> discoveries) {
        Set<String> discoveredIds = discoveries.stream()
                .map(item -> item.definition().pluginId())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        List<String> missing = config.requiredPluginIds().stream()
                .filter(id -> !discoveredIds.contains(id))
                .sorted()
                .toList();
        if (!missing.isEmpty()) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "required plugins were not discovered: " + missing);
        }
    }

    private void validateDefaultRoutes(List<DiscoveredPlugin> discoveries) {
        for (Map.Entry<CapabilityKey, com.innospots.nexus.core.plugin.capability.Tags> route
                : config.defaultRoutes().entrySet()) {
            List<String> candidates = discoveries.stream()
                    .filter(item -> !config.disabledPluginIds().contains(item.definition().pluginId()))
                    .filter(item -> item.definition().capabilities().stream()
                            .filter(capability -> capability.type().key().equals(route.getKey()))
                            .anyMatch(capability -> Tags.merge(
                                    item.definition().tags(), capability.tags()).matches(route.getValue())))
                    .map(item -> item.definition().pluginId())
                    .sorted()
                    .toList();
            if (candidates.size() > 1) {
                throw NexusException.build(
                        PluginStatusCode.CAPABILITY_AMBIGUOUS,
                        "default route is ambiguous for " + route.getKey() + ", candidates=" + candidates);
            }
        }
    }

    private ManagedPlugin requirePlugin(String pluginId) {
        ManagedPlugin plugin = managedPlugins.get(pluginId);
        if (plugin == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DISCOVERY_FAILED, "plugin not found: " + pluginId);
        }
        return plugin;
    }

    private void protectRequiredCapabilities(ManagedPlugin target) {
        Set<CapabilityKey> removedKeys = target.definition().capabilities().stream()
                .map(item -> item.type().key())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Map<CapabilityKey, List<CapabilityRegistration<?>>> snapshot = registry.snapshot();
        for (ManagedPlugin dependent : managedPlugins.values()) {
            if (dependent == target || dependent.info().state() != PluginState.ACTIVE) {
                continue;
            }
            for (CapabilityRequirement requirement : dependent.definition().requirements()) {
                if (!requirement.required() || !removedKeys.contains(requirement.key())) {
                    continue;
                }
                long remaining = snapshot.getOrDefault(requirement.key(), List.of()).stream()
                        .filter(registration -> !registration.pluginId().equals(target.definition().pluginId()))
                        .filter(registration -> availabilityIndex.isVisible(registration.pluginId()))
                        .filter(registration -> registration.tags().matches(requirement.requiredTags()))
                        .count();
                // 单插件 stop 保护：不得移除其他 ACTIVE 插件 required 依赖的最后一个匹配 Provider。
                if (remaining == 0) {
                    throw NexusException.build(
                            PluginStatusCode.PLUGIN_IN_USE,
                            "plugin " + target.definition().pluginId() + " is required by "
                                    + dependent.definition().pluginId() + " for " + requirement.key());
                }
            }
        }
    }

    private void rollbackStarted(List<String> startedThisCall) {
        if (!startedThisCall.isEmpty()) {
            logger.warn("Rolling back {} plugin(s) after startup failure: {}",
                    startedThisCall.size(), startedThisCall);
        }
        List<String> reverse = new ArrayList<>(startedThisCall);
        Collections.reverse(reverse);
        for (String pluginId : reverse) {
            try {
                managedPlugins.get(pluginId).stop();
            } catch (RuntimeException exception) {
                logger.warn("Plugin {} failed during startup rollback", pluginId, exception);
            }
            startupOrder.remove(pluginId);
        }
    }

    /** 获取一次短暂的管理操作令牌，令牌本身不包围插件代码执行。 */
    private void enterOperation() {
        synchronized (operationMonitor) {
            while (operationInProgress) {
                try {
                    operationMonitor.wait();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw NexusException.build(PluginStatusCode.PLUGIN_CONCURRENCY_CONFLICT.fullCode(),
                            "plugin manager operation was interrupted", exception);
                }
            }
            operationInProgress = true;
        }
    }

    /** 释放一次管理操作令牌并唤醒排队调用。 */
    private void leaveOperation() {
        synchronized (operationMonitor) {
            operationInProgress = false;
            operationMonitor.notifyAll();
        }
    }
}

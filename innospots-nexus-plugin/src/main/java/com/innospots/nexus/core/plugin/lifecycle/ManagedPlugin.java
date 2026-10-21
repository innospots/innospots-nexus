package com.innospots.nexus.core.plugin.lifecycle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.config.PluginConfig;
import com.innospots.nexus.core.plugin.contract.PluginContext;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.contribution.PluginContributionContext;
import com.innospots.nexus.core.plugin.contribution.PluginContributionInventories;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionType;
import com.innospots.nexus.core.plugin.contribution.PreparedPluginContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.declaration.ProviderRef;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.event.DefaultPluginEventBus;
import com.innospots.nexus.core.plugin.event.PluginEvent;
import com.innospots.nexus.core.plugin.event.PluginEventBus;
import com.innospots.nexus.core.plugin.event.PluginFailedEvent;
import com.innospots.nexus.core.plugin.event.PluginStartedEvent;
import com.innospots.nexus.core.plugin.event.PluginStoppedEvent;
import com.innospots.nexus.core.plugin.resource.DefaultResourceScope;
import com.innospots.nexus.core.plugin.resource.ResourceScope;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 持有一个插件实例、上下文、资源以及原子生命周期转换。
 *
 * <p>生命周期转换串行执行，但未知插件代码不会在本对象的生命周期锁内执行。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class ManagedPlugin {

    private static final Logger log = LoggerFactory.getLogger(ManagedPlugin.class);

    private final DiscoveredPlugin discovered;
    private final Supplier<PluginConfig> configSupplier;
    private final DefaultPluginEventBus eventBus;
    private final Map<PluginContributionType<?>, PluginContributionHandler<?>> contributionHandlers;
    private final System.Logger logger;
    private final PluginAvailability availability = new PluginAvailability();
    private final ReentrantLock lifecycleLock = new ReentrantLock();

    private volatile PluginState state = PluginState.DESCRIBED;
    private volatile String phase = "definition-validated";
    private volatile Instant startedAt;
    private volatile String lastError;
    private ResourceScope resources;
    private List<PreparedPluginContribution> preparedContributions = List.of();
    private PluginConfig config;

    /**
     * 为一个已发现插件创建生命周期管理包装器。
     *
     * @param discovered 已发现的插件实例和不可变定义
     * @param config 每次启动使用的配置快照
     * @param eventBus 运行时本地事件总线
     */
    public ManagedPlugin(
            DiscoveredPlugin discovered,
            PluginConfig config,
            DefaultPluginEventBus eventBus
    ) {
        this(discovered, () -> config, eventBus, List.of());
    }

    /**
     * 创建一个仅在插件启动时解析配置的生命周期管理包装器。
     *
     * @param discovered 已发现的插件实例和不可变定义
     * @param configSupplier 延迟配置解析器
     * @param eventBus 运行时本地事件总线
     */
    public ManagedPlugin(
            DiscoveredPlugin discovered,
            Supplier<PluginConfig> configSupplier,
            DefaultPluginEventBus eventBus
    ) {
        this(discovered, configSupplier, eventBus, List.of());
    }

    /**
     * 创建管理 Contribution 事务的插件运行时包装器。
     *
     * @param discovered 已发现插件
     * @param configSupplier 插件共享配置解析器
     * @param eventBus 插件事件总线
     * @param contributionHandlers 按类型索引的 Contribution Handler 列表
     * @throws NexusException 任一依赖为空或 Handler 类型重复时抛出
     */
    public ManagedPlugin(
            DiscoveredPlugin discovered,
            Supplier<PluginConfig> configSupplier,
            DefaultPluginEventBus eventBus,
            List<PluginContributionHandler<?>> contributionHandlers
    ) {
        if (discovered == null || configSupplier == null || eventBus == null || contributionHandlers == null
                || contributionHandlers.stream().anyMatch(java.util.Objects::isNull)) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "managed plugin dependencies are required");
        }
        this.discovered = discovered;
        this.configSupplier = configSupplier;
        this.eventBus = eventBus;
        this.contributionHandlers = contributionHandlers.stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        PluginContributionHandler::type,
                        handler -> handler,
                        (left, right) -> {
                            throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                                    "duplicate contribution handler: " + left.type());
                        }));
        this.logger = System.getLogger("plugin." + discovered.definition().pluginId());
    }

    /**
     * 初始化、启动并原子提交全部 Contribution。
     *
     * @throws NexusException 任一生命周期阶段失败时抛出；失败后会回滚已分配资源
     */
    public void start() {
        String pluginId = discovered.definition().pluginId();
        lifecycleLock.lock();
        try {
            if (state == PluginState.ACTIVE || state == PluginState.STARTING
                    || state == PluginState.STOPPING) {
                log.debug("Plugin {} start skipped: state={}", pluginId, state);
                return;
            }
            state = PluginState.STARTING;
            lastError = null;
        } finally {
            lifecycleLock.unlock();
        }
        log.info("Starting plugin {} version {}", pluginId, discovered.definition().version());
        List<PreparedPluginContribution> prepared = new ArrayList<>();
        boolean pluginInitializeStarted = false;
        PluginStartedEvent startedEvent = null;

        try {
            phase = "config-resolve";
            PluginConfig resolvedConfig = configSupplier.get();
            if (resolvedConfig == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        "plugin config supplier returned null: " + discovered.definition().pluginId());
            }
            config = resolvedConfig;
            resources = new DefaultResourceScope();
            PluginEventBus scopedEvents = eventBus.scoped(resources);
            PluginContext pluginContext = new DefaultPluginContext(
                    discovered.definition(), config, scopedEvents, resources, logger);

            phase = "contribution-prepare";
            for (PluginContribution contribution : discovered.definition().contributions()) {
                PluginContributionHandler<?> handler = contributionHandlers.get(contribution.type());
                if (handler == null) {
                    throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                            "missing contribution handler: " + contribution.type());
                }
                log.info(
                        "Plugin {} loading contribution {}: {} module(s), {} component(s)",
                        pluginId,
                        contribution.type(),
                        PluginContributionInventories.moduleCount(contribution),
                        PluginContributionInventories.componentCount(contribution));
                PreparedPluginContribution handle = prepareContribution(handler, contribution);
                prepared.add(handle);
            }

            phase = "plugin-initialize";
            pluginInitializeStarted = true;
            discovered.plugin().initialize(pluginContext);

            phase = "plugin-start";
            discovered.plugin().start();

            phase = "contribution-stage";
            for (PreparedPluginContribution handle : prepared) {
                handle.stage();
            }

            phase = "contribution-commit";
            for (PreparedPluginContribution handle : prepared) {
                handle.commit();
            }
            preparedContributions = List.copyOf(prepared);
            startedAt = Instant.now();
            availability.activate();
            state = PluginState.ACTIVE;
            phase = "active";
            startedEvent = new PluginStartedEvent(
                    discovered.definition().pluginId(),
                    discovered.definition().version(),
                    discovered.definition().contributions().stream()
                            .map(item -> item.type().name() + "@" + item.type().majorVersion())
                            .toList(),
                    startedAt);
        } catch (RuntimeException | LinkageError exception) {
            log.warn("Plugin {} failed during phase {}", pluginId, phase, exception);
            rollback(prepared, pluginInitializeStarted, exception);
            lifecycleLock.lock();
            try {
                state = PluginState.FAILED;
                lastError = errorCode(exception);
            } finally {
                lifecycleLock.unlock();
            }
            eventBus.publish(new PluginFailedEvent(
                    discovered.definition().pluginId(), phase, errorCode(exception), Instant.now()));
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_START_FAILED.fullCode(),
                    "failed to start plugin " + discovered.definition().pluginId() + " during " + phase,
                    exception);
        }
        if (startedEvent != null) {
            PluginContributionInventories.Summary contributions = PluginContributionInventories.summarize(
                    discovered.definition().contributions());
            log.info(
                    "Plugin {} started with {} contribution(s), {} extension module(s), {} extension component(s)",
                    pluginId,
                    contributions.contributionCount(),
                    contributions.moduleCount(),
                    contributions.componentCount());
            eventBus.publish(startedEvent);
        }
    }

    /**
     * 原子停止 Contribution、插件状态和资源。
     *
     * @throws NexusException 停止或资源释放失败时抛出
     */
    public void stop() {
        String pluginId = discovered.definition().pluginId();
        List<PreparedPluginContribution> reverseContributions;
        ResourceScope currentResources;
        lifecycleLock.lock();
        try {
            if (state != PluginState.ACTIVE) {
                log.debug("Plugin {} stop skipped: state={}", pluginId, state);
                return;
            }
            log.info("Stopping plugin {}", pluginId);
            state = PluginState.STOPPING;
            phase = "contribution-withdraw";
            availability.deactivate();
            reverseContributions = new ArrayList<>(preparedContributions);
            Collections.reverse(reverseContributions);
            currentResources = resources;
        } finally {
            lifecycleLock.unlock();
        }
        Throwable failure = null;
        String failurePhase = null;

        phase = "contribution-rollback";
        for (PreparedPluginContribution contribution : reverseContributions) {
            Throwable previousContribution = failure;
            failure = runCleanup(contribution::rollback, failure);
            if (previousContribution == null && failure != null) {
                failurePhase = phase;
            }
            previousContribution = failure;
            failure = runCleanup(contribution::close, failure);
            if (previousContribution == null && failure != null) {
                failurePhase = phase;
            }
        }

        phase = "plugin-stop";
        Throwable previous = failure;
        failure = runCleanup(discovered.plugin()::stop, failure);
        if (previous == null && failure != null) {
            failurePhase = phase;
        }
        phase = "resource-close";
        if (currentResources != null) {
            previous = failure;
            failure = runCleanup(currentResources::close, failure);
            if (previous == null && failure != null) {
                failurePhase = phase;
            }
        }
        PluginEvent pluginEvent;
        NexusException stopFailure = null;
        lifecycleLock.lock();
        try {
            preparedContributions = List.of();
            resources = null;
            config = null;
            if (failure != null) {
                state = PluginState.FAILED;
                phase = failurePhase == null ? phase : failurePhase;
                lastError = PluginStatusCode.PLUGIN_STOP_FAILED.fullCode();
                pluginEvent = new PluginFailedEvent(
                        discovered.definition().pluginId(), phase,
                        PluginStatusCode.PLUGIN_STOP_FAILED.fullCode(), Instant.now());
                stopFailure = NexusException.build(
                        PluginStatusCode.PLUGIN_STOP_FAILED.fullCode(),
                        "failed to stop plugin " + discovered.definition().pluginId(), failure);
            } else {
                state = PluginState.STOPPED;
                phase = "stopped";
                pluginEvent = new PluginStoppedEvent(discovered.definition().pluginId(), Instant.now());
            }
        } finally {
            lifecycleLock.unlock();
        }
        if (failure != null) {
            log.warn("Plugin {} stop failed during phase {}", pluginId, failurePhase, failure);
            eventBus.publish(pluginEvent);
            throw stopFailure;
        }
        log.info("Plugin {} stopped", pluginId);
        eventBus.publish(pluginEvent);
    }

    /**
     * 返回不含运行时对象和配置值的不可变运行快照。
     *
     * @return 当前插件运行诊断
     */
    public PluginRuntimeInfo info() {
        PluginDefinition definition = discovered.definition();
        return new PluginRuntimeInfo(
                definition.pluginId(),
                definition.displayName().defaultValue(),
                definition.version(),
                discovered.plugin().getClass().getName(),
                state,
                phase,
                definition.tags(),
                discovered.discoveredAt(),
                startedAt,
                lastError);
    }

    /**
     * 返回 Contribution 共用的可用性门控。
     *
     * @return 插件可用性门控
     */
    public PluginAvailability availability() {
        return availability;
    }

    /**
     * 返回不可变的插件定义快照。
     *
     * @return 发现时缓存的插件定义
     */
    public PluginDefinition definition() {
        return discovered.definition();
    }

    private void rollback(
            List<PreparedPluginContribution> prepared,
            boolean pluginInitializeStarted,
            Throwable original
    ) {
        log.debug("Rolling back plugin {} after start failure", discovered.definition().pluginId());
        availability.deactivate();
        List<PreparedPluginContribution> reverseContributions = new ArrayList<>(prepared);
        Collections.reverse(reverseContributions);
        for (PreparedPluginContribution contribution : reverseContributions) {
            addCleanupFailure(contribution::rollback, original);
            addCleanupFailure(contribution::close, original);
        }
        if (pluginInitializeStarted) {
            addCleanupFailure(discovered.plugin()::stop, original);
        }
        if (resources != null) {
            addCleanupFailure(resources::close, original);
        }
        preparedContributions = List.of();
        resources = null;
        config = null;
    }

    private static void addCleanupFailure(Runnable cleanup, Throwable original) {
        try {
            cleanup.run();
        } catch (RuntimeException | LinkageError cleanupFailure) {
            original.addSuppressed(cleanupFailure);
        }
    }

    @SuppressWarnings("rawtypes")
    private PreparedPluginContribution prepareContribution(
            PluginContributionHandler handler,
            PluginContribution contribution
    ) {
        PreparedPluginContribution prepared = handler.prepare(
                new PluginContributionContext(
                        new ProviderRef(discovered.definition().pluginId(),
                                "contribution-" + contribution.type().name().replace('.', '-')
                                        + "-" + contribution.type().majorVersion()),
                        config,
                        availability),
                contribution);
        if (prepared == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_START_FAILED,
                    "contribution handler returned null prepared handle: " + contribution.type());
        }
        return prepared;
    }

    private static Throwable runCleanup(Runnable cleanup, Throwable existing) {
        try {
            cleanup.run();
            return existing;
        } catch (Throwable failure) {
            if (existing == null) {
                return failure;
            }
            existing.addSuppressed(failure);
            return existing;
        }
    }

    private static String errorCode(Throwable exception) {
        return exception instanceof NexusException nexus
                ? nexus.code()
                : PluginStatusCode.PLUGIN_START_FAILED.fullCode();
    }

    private record DefaultPluginContext(
            PluginDefinition definition,
            PluginConfig config,
            PluginEventBus events,
            ResourceScope resources,
            System.Logger logger
    ) implements PluginContext {
    }
}

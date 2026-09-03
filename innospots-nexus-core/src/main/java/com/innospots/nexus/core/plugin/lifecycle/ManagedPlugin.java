package com.innospots.nexus.core.plugin.lifecycle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.CapabilityManager;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistration;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistry;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.config.ConfigurationManager;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.PluginConfig;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.CapabilityProviderContext;
import com.innospots.nexus.core.plugin.contract.PluginContext;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.contribution.PluginContributionContext;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionType;
import com.innospots.nexus.core.plugin.contribution.PreparedPluginContribution;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.dependency.DependencyResolution;
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
 * 持有一个插件实例、Provider、上下文、资源以及原子生命周期转换。
 * 生命周期转换串行执行，但未知插件代码不会在本对象的生命周期锁内执行。
 */
public final class ManagedPlugin {

    private static final Logger log = LoggerFactory.getLogger(ManagedPlugin.class);

    private final DiscoveredPlugin discovered;
    private final Supplier<PluginConfig> configSupplier;
    private final Function<CapabilityContribution<?>, PluginConfig> providerConfigSupplier;
    private final CapabilityRegistry registry;
    private final DefaultPluginEventBus eventBus;
    private final Map<PluginContributionType<?>, PluginContributionHandler<?>> contributionHandlers;
    private final System.Logger logger;
    private final PluginAvailability availability = new PluginAvailability();
    private final ReentrantLock lifecycleLock = new ReentrantLock();

    private volatile PluginState state = PluginState.DESCRIBED;
    private volatile String phase = "definition-validated";
    private volatile Instant startedAt;
    private volatile String lastError;
    private volatile Map<CapabilityKey, DependencyResolution> dependencies = Map.of();
    private ResourceScope resources;
    private List<ProviderHolder<?>> providers = List.of();
    private List<PreparedPluginContribution> preparedContributions = List.of();
    private PluginConfig config;

    /**
     * 为一个已发现插件创建生命周期管理包装器。
     *
     * @param discovered 已发现的插件实例和不可变定义
     * @param config 每次启动使用的配置快照
     * @param registry 运行时共享的 Capability 注册表
     * @param eventBus 运行时本地事件总线
     */
    public ManagedPlugin(
            DiscoveredPlugin discovered,
            PluginConfig config,
            CapabilityRegistry registry,
            DefaultPluginEventBus eventBus
    ) {
        this(discovered, () -> config, contribution -> ConfigurationManager.standard(Map.of(), Map.of())
                .resolveProvider(discovered.definition(), contribution), registry, eventBus, List.of());
    }

    /**
     * 创建一个仅在插件启动时解析配置的生命周期管理包装器。
     *
     * @param discovered 已发现的插件实例和不可变定义
     * @param configSupplier 延迟配置解析器
     * @param registry 运行时共享的 Capability 注册表
     * @param eventBus 运行时本地事件总线
     */
    public ManagedPlugin(
            DiscoveredPlugin discovered,
            Supplier<PluginConfig> configSupplier,
            CapabilityRegistry registry,
            DefaultPluginEventBus eventBus
    ) {
        this(discovered, configSupplier, contribution -> ConfigurationManager.standard(Map.of(), Map.of())
                .resolveProvider(discovered.definition(), contribution), registry, eventBus, List.of());
    }

    /**
     * 创建可以分别解析插件共享配置和 Provider 私有配置的运行时包装器。
     *
     * @param discovered 已发现插件
     * @param configSupplier 插件共享配置解析器
     * @param providerConfigSupplier Provider 私有配置解析器
     * @param registry Capability 注册表
     * @param eventBus 插件事件总线
     */
    public ManagedPlugin(
            DiscoveredPlugin discovered,
            Supplier<PluginConfig> configSupplier,
            Function<CapabilityContribution<?>, PluginConfig> providerConfigSupplier,
            CapabilityRegistry registry,
            DefaultPluginEventBus eventBus
    ) {
        this(discovered, configSupplier, providerConfigSupplier, registry, eventBus, List.of());
    }

    /**
     * 创建同时管理 Capability 和通用 Contribution 事务的插件运行时包装器。
     *
     * @param discovered 已发现插件
     * @param configSupplier 插件共享配置解析器
     * @param providerConfigSupplier Provider 私有配置解析器
     * @param registry Capability 注册表
     * @param eventBus 插件事件总线
     * @param contributionHandlers 按类型索引的 Contribution Handler 列表
     * @throws NexusException 任一依赖为空或 Handler 类型重复时抛出
     */
    public ManagedPlugin(
            DiscoveredPlugin discovered,
            Supplier<PluginConfig> configSupplier,
            Function<CapabilityContribution<?>, PluginConfig> providerConfigSupplier,
            CapabilityRegistry registry,
            DefaultPluginEventBus eventBus,
            List<PluginContributionHandler<?>> contributionHandlers
    ) {
        if (discovered == null || configSupplier == null || providerConfigSupplier == null
                || registry == null || eventBus == null || contributionHandlers == null
                || contributionHandlers.stream().anyMatch(java.util.Objects::isNull)) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "managed plugin dependencies are required");
        }
        this.discovered = discovered;
        this.configSupplier = configSupplier;
        this.providerConfigSupplier = providerConfigSupplier;
        this.registry = registry;
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
     * 创建、初始化、启动并原子发布全部 Provider 和 Contribution。
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
            // 失败事务已经在 rollback 中清除了 Provider、资源和注册索引，因此允许受控重试。
            state = PluginState.STARTING;
            lastError = null;
        } finally {
            lifecycleLock.unlock();
        }
        log.info("Starting plugin {} version {}", pluginId, discovered.definition().version());
        List<ProviderHolder<?>> created = new ArrayList<>();
        List<ProviderHolder<?>> initialized = new ArrayList<>();
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
                    discovered.definition(), config, registry, scopedEvents, resources, logger);

            phase = "contribution-prepare";
            for (PluginContribution contribution : discovered.definition().contributions()) {
                PluginContributionHandler<?> handler = contributionHandlers.get(contribution.type());
                if (handler == null) {
                    throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                            "missing contribution handler: " + contribution.type());
                }
                PreparedPluginContribution handle = prepareContribution(handler, contribution);
                // 先纳入回滚清单，确保后续任意阶段失败时都能调用清理钩子。
                prepared.add(handle);
            }

            phase = "provider-create";
            for (CapabilityContribution<?> contribution : discovered.definition().capabilities()) {
                created.add(createProvider(contribution));
            }

            phase = "plugin-initialize";
            pluginInitializeStarted = true;
            discovered.plugin().initialize(pluginContext);

            phase = "provider-initialize";
            for (ProviderHolder<?> holder : created) {
                holder.initialize(pluginContext, providerConfigSupplier.apply(holder.contribution()));
                initialized.add(holder);
            }

            phase = "plugin-start";
            discovered.plugin().start();

            phase = "contribution-stage";
            for (PreparedPluginContribution handle : prepared) {
                handle.stage();
            }

            phase = "capability-publish";
            List<CapabilityRegistration<?>> registrations = new ArrayList<>();
            for (ProviderHolder<?> holder : created) {
                registrations.add(holder.registration(
                        discovered.definition().pluginId(),
                        Tags.merge(discovered.definition().tags(), holder.contribution().tags())));
            }
            registry.registerAll(registrations);

            phase = "contribution-commit";
            for (PreparedPluginContribution handle : prepared) {
                handle.commit();
            }
            providers = List.copyOf(created);
            preparedContributions = List.copyOf(prepared);
            startedAt = Instant.now();
            availability.activate();
            state = PluginState.ACTIVE;
            phase = "active";
            startedEvent = new PluginStartedEvent(
                    discovered.definition().pluginId(),
                    discovered.definition().version(),
                    discovered.definition().capabilities().stream()
                            .map(item -> item.type().key())
                            .toList(),
                    startedAt);
        } catch (RuntimeException | LinkageError exception) {
            log.warn("Plugin {} failed during phase {}", pluginId, phase, exception);
            rollback(initialized, prepared, pluginInitializeStarted, exception);
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
            log.info("Plugin {} started with {} capability provider(s)",
                    pluginId, created.size());
            eventBus.publish(startedEvent);
        }
    }

    /**
     * 原子撤出 Capability，再销毁 Provider、插件状态和资源。
     *
     * @throws NexusException 停止或资源释放失败时抛出
     */
    public void stop() {
        String pluginId = discovered.definition().pluginId();
        List<PreparedPluginContribution> reverseContributions;
        List<ProviderHolder<?>> reverse;
        ResourceScope currentResources;
        lifecycleLock.lock();
        try {
            if (state != PluginState.ACTIVE) {
                log.debug("Plugin {} stop skipped: state={}", pluginId, state);
                return;
            }
            log.info("Stopping plugin {}", pluginId);
            state = PluginState.STOPPING;
            phase = "capability-withdraw";
            availability.deactivate();
            registry.unregisterPlugin(discovered.definition().pluginId());
            reverseContributions = new ArrayList<>(preparedContributions);
            Collections.reverse(reverseContributions);
            reverse = new ArrayList<>(providers);
            Collections.reverse(reverse);
            currentResources = resources;
        } finally {
            lifecycleLock.unlock();
        }
        // 销毁钩子可能在未知插件代码中阻塞；在锁外执行避免死锁其它生命周期操作。
        Throwable failure = null;
        String failurePhase = null;

        phase = "provider-destroy";
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
        for (ProviderHolder<?> holder : reverse) {
            Throwable previous = failure;
            failure = runCleanup(holder.provider()::destroy, failure);
            if (previous == null && failure != null) {
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
            providers = List.of();
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
     * 将插件标记为等待所需 Capability。
     *
     * @param dependencies 当前声明快照的依赖诊断
     */
    public void waiting(Map<CapabilityKey, DependencyResolution> dependencies) {
        if (dependencies == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                    "dependency diagnostics are required");
        }
        lifecycleLock.lock();
        try {
            // 进行中的启动/停止/失败状态不可被 waiting() 覆盖，避免与并发 start/stop 交错。
            if (state != PluginState.ACTIVE && state != PluginState.STARTING
                    && state != PluginState.STOPPING && state != PluginState.FAILED) {
                this.dependencies = Map.copyOf(dependencies);
                state = PluginState.WAITING;
                phase = "dependency-wait";
                log.debug("Plugin {} waiting for dependencies", discovered.definition().pluginId());
            }
        } finally {
            lifecycleLock.unlock();
        }
    }

    /**
     * 更新依赖诊断，但不改变生命周期状态。
     *
     * @param dependencies 当前声明快照的依赖诊断
     */
    public void dependencies(Map<CapabilityKey, DependencyResolution> dependencies) {
        if (dependencies == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEPENDENCY_MISSING,
                    "dependency diagnostics are required");
        }
        lifecycleLock.lock();
        try {
            this.dependencies = Map.copyOf(dependencies);
        } finally {
            lifecycleLock.unlock();
        }
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
                definition.capabilities().stream().map(item -> item.type().key()).toList(),
                definition.requirements(),
                dependencies,
                discovered.discoveredAt(),
                startedAt,
                lastError);
    }

    /**
     * 返回 Capability 与 Contribution 共用的可用性门控。
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
            List<ProviderHolder<?>> initialized,
            List<PreparedPluginContribution> prepared,
            boolean pluginInitializeStarted,
            Throwable original
    ) {
        log.debug("Rolling back plugin {} after start failure", discovered.definition().pluginId());
        availability.deactivate();
        registry.unregisterPlugin(discovered.definition().pluginId());
        List<PreparedPluginContribution> reverseContributions = new ArrayList<>(prepared);
        Collections.reverse(reverseContributions);
        for (PreparedPluginContribution contribution : reverseContributions) {
            addCleanupFailure(contribution::rollback, original);
            addCleanupFailure(contribution::close, original);
        }
        List<ProviderHolder<?>> reverse = new ArrayList<>(initialized);
        Collections.reverse(reverse);
        for (ProviderHolder<?> holder : reverse) {
            addCleanupFailure(holder.provider()::destroy, original);
        }
        if (pluginInitializeStarted) {
            addCleanupFailure(discovered.plugin()::stop, original);
        }
        if (resources != null) {
            addCleanupFailure(resources::close, original);
        }
        providers = List.of();
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

    private static <T extends CapabilityProvider> ProviderHolder<T> createProvider(
            CapabilityContribution<T> contribution
    ) {
        T provider = contribution.factory().create();
        if (provider == null || !contribution.type().api().isInstance(provider)) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "factory returned an invalid provider for " + contribution.type().key());
        }
        return new ProviderHolder<>(contribution, provider);
    }

    private record DefaultPluginContext(
            PluginDefinition definition,
            PluginConfig config,
            CapabilityManager capabilities,
            PluginEventBus events,
            ResourceScope resources,
            System.Logger logger
    ) implements PluginContext {
    }

    private record DefaultCapabilityProviderContext(
            PluginContext delegate,
            CapabilityKey capability,
            ProviderRef providerRef,
            PluginConfig providerConfig
    ) implements CapabilityProviderContext {

        @Override
        public ProviderRef providerRef() {
            return providerRef;
        }

        @Override
        public PluginConfig providerConfig() {
            return providerConfig;
        }

        @Override
        public PluginDefinition definition() {
            return delegate.definition();
        }

        @Override
        public PluginConfig config() {
            return delegate.config();
        }

        @Override
        public CapabilityManager capabilities() {
            return delegate.capabilities();
        }

        @Override
        public PluginEventBus events() {
            return delegate.events();
        }

        @Override
        public ResourceScope resources() {
            return delegate.resources();
        }

        @Override
        public System.Logger logger() {
            return delegate.logger();
        }
    }

    private record ProviderHolder<T extends CapabilityProvider>(
            CapabilityContribution<T> contribution,
            T provider
    ) {

        private void initialize(PluginContext context, PluginConfig providerConfig) {
            provider.initialize(new DefaultCapabilityProviderContext(
                    context,
                    contribution.type().key(),
                    new ProviderRef(context.definition().pluginId(), contribution.providerId()),
                    providerConfig));
        }

        private CapabilityRegistration<T> registration(String pluginId, Tags tags) {
            return new CapabilityRegistration<>(
                    contribution.type(),
                    provider,
                    new ProviderRef(pluginId, contribution.providerId()),
                    tags);
        }
    }
}

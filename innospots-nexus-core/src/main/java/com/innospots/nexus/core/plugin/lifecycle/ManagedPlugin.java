package com.innospots.nexus.core.plugin.lifecycle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.CapabilityManager;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistration;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistry;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.PluginConfig;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.CapabilityProviderContext;
import com.innospots.nexus.core.plugin.contract.PluginContext;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.dependency.DependencyResolution;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.event.DefaultPluginEventBus;
import com.innospots.nexus.core.plugin.event.PluginEventBus;
import com.innospots.nexus.core.plugin.event.PluginFailedEvent;
import com.innospots.nexus.core.plugin.event.PluginStartedEvent;
import com.innospots.nexus.core.plugin.event.PluginStoppedEvent;
import com.innospots.nexus.core.plugin.resource.DefaultResourceScope;
import com.innospots.nexus.core.plugin.resource.ResourceScope;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Owns one plugin instance, its providers, contexts, resources, and atomic lifecycle transitions.
 * Lifecycle methods are serialized; business capability calls never execute under this object's monitor.
 */
public final class ManagedPlugin {

    private final DiscoveredPlugin discovered;
    private final Supplier<PluginConfig> configSupplier;
    private final CapabilityRegistry registry;
    private final DefaultPluginEventBus eventBus;
    private final System.Logger logger;

    private PluginState state = PluginState.DESCRIBED;
    private String phase = "definition-validated";
    private Instant startedAt;
    private String lastError;
    private Map<CapabilityKey, DependencyResolution> dependencies = Map.of();
    private ResourceScope resources;
    private List<ProviderHolder<?>> providers = List.of();
    private PluginConfig config;

    /** Creates a managed lifecycle wrapper around one discovered plugin. */
    public ManagedPlugin(
            DiscoveredPlugin discovered,
            PluginConfig config,
            CapabilityRegistry registry,
            DefaultPluginEventBus eventBus
    ) {
        if (discovered == null || config == null || registry == null || eventBus == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "managed plugin dependencies are required");
        }
        this.discovered = discovered;
        this.configSupplier = () -> config;
        this.registry = registry;
        this.eventBus = eventBus;
        this.logger = System.getLogger("plugin." + discovered.definition().id());
    }

    /** Creates a managed plugin whose configuration is resolved only when that plugin starts. */
    public ManagedPlugin(
            DiscoveredPlugin discovered,
            Supplier<PluginConfig> configSupplier,
            CapabilityRegistry registry,
            DefaultPluginEventBus eventBus
    ) {
        if (discovered == null || configSupplier == null || registry == null || eventBus == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "managed plugin dependencies are required");
        }
        this.discovered = discovered;
        this.configSupplier = configSupplier;
        this.registry = registry;
        this.eventBus = eventBus;
        this.logger = System.getLogger("plugin." + discovered.definition().id());
    }

    /** Creates, initializes, starts, and atomically publishes every provider. */
    public synchronized void start() {
        if (state == PluginState.ACTIVE || state == PluginState.STARTING) {
            return;
        }
        if (state == PluginState.FAILED) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_START_FAILED,
                    "failed plugin requires a new runtime instance before retry: "
                            + discovered.definition().id());
        }
        state = PluginState.STARTING;
        lastError = null;
        List<ProviderHolder<?>> created = new ArrayList<>();
        List<ProviderHolder<?>> initialized = new ArrayList<>();
        boolean pluginInitializeStarted = false;

        try {
            phase = "config-resolve";
            config = configSupplier.get();
            resources = new DefaultResourceScope();
            PluginEventBus scopedEvents = eventBus.scoped(resources);
            PluginContext pluginContext = new DefaultPluginContext(
                    discovered.definition(), config, registry, scopedEvents, resources, logger);

            phase = "provider-create";
            for (CapabilityContribution<?> contribution : discovered.definition().capabilities()) {
                created.add(createProvider(contribution));
            }

            phase = "plugin-initialize";
            pluginInitializeStarted = true;
            discovered.plugin().initialize(pluginContext);

            phase = "provider-initialize";
            for (ProviderHolder<?> holder : created) {
                holder.initialize(pluginContext);
                initialized.add(holder);
            }

            phase = "plugin-start";
            discovered.plugin().start();

            phase = "capability-publish";
            List<CapabilityRegistration<?>> registrations = new ArrayList<>();
            for (ProviderHolder<?> holder : created) {
                registrations.add(holder.registration(
                        discovered.definition().id(),
                        discovered.definition().tags()));
            }
            registry.registerAll(registrations);
            providers = List.copyOf(created);
            startedAt = Instant.now();
            state = PluginState.ACTIVE;
            phase = "active";
            eventBus.publish(new PluginStartedEvent(
                    discovered.definition().id(),
                    discovered.definition().version(),
                    discovered.definition().capabilities().stream()
                            .map(item -> item.type().key())
                            .toList(),
                    startedAt));
        } catch (RuntimeException | LinkageError exception) {
            rollback(initialized, pluginInitializeStarted, exception);
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_START_FAILED.fullCode(),
                    "failed to start plugin " + discovered.definition().id() + " during " + phase,
                    exception);
        }
    }

    /** Atomically withdraws capabilities, then destroys providers, plugin state, and resources. */
    public synchronized void stop() {
        if (state != PluginState.ACTIVE) {
            return;
        }
        state = PluginState.STOPPING;
        phase = "capability-withdraw";
        registry.unregisterPlugin(discovered.definition().id());
        Throwable failure = null;

        phase = "provider-destroy";
        List<ProviderHolder<?>> reverse = new ArrayList<>(providers);
        Collections.reverse(reverse);
        for (ProviderHolder<?> holder : reverse) {
            failure = runCleanup(holder.provider()::destroy, failure);
        }

        phase = "plugin-stop";
        failure = runCleanup(discovered.plugin()::stop, failure);
        phase = "resource-close";
        ResourceScope currentResources = resources;
        failure = runCleanup(currentResources::close, failure);
        providers = List.of();
        resources = null;

        if (failure != null) {
            state = PluginState.FAILED;
            lastError = failure.getMessage();
            eventBus.publish(new PluginFailedEvent(
                    discovered.definition().id(),
                    phase,
                    PluginStatusCode.PLUGIN_STOP_FAILED.fullCode(),
                    Instant.now()));
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_STOP_FAILED.fullCode(),
                    "failed to stop plugin " + discovered.definition().id(),
                    failure);
        }
        state = PluginState.STOPPED;
        phase = "stopped";
        eventBus.publish(new PluginStoppedEvent(discovered.definition().id(), Instant.now()));
    }

    /** Marks this plugin as waiting for its required capabilities. */
    public synchronized void waiting(Map<CapabilityKey, DependencyResolution> dependencies) {
        if (state != PluginState.ACTIVE && state != PluginState.FAILED) {
            this.dependencies = Map.copyOf(dependencies);
            state = PluginState.WAITING;
            phase = "dependency-wait";
        }
    }

    /** Updates dependency diagnostics without mutating lifecycle state. */
    public synchronized void dependencies(Map<CapabilityKey, DependencyResolution> dependencies) {
        this.dependencies = Map.copyOf(dependencies);
    }

    /** Returns a sanitized immutable runtime snapshot. */
    public synchronized PluginRuntimeInfo info() {
        PluginDefinition definition = discovered.definition();
        return new PluginRuntimeInfo(
                definition.id(),
                definition.name(),
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

    /** Returns the immutable definition snapshot. */
    public PluginDefinition definition() {
        return discovered.definition();
    }

    private void rollback(
            List<ProviderHolder<?>> initialized,
            boolean pluginInitializeStarted,
            Throwable original
    ) {
        registry.unregisterPlugin(discovered.definition().id());
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
        resources = null;
        state = PluginState.FAILED;
        lastError = original.getMessage();
        eventBus.publish(new PluginFailedEvent(
                discovered.definition().id(), phase, errorCode(original), Instant.now()));
    }

    private static void addCleanupFailure(Runnable cleanup, Throwable original) {
        try {
            cleanup.run();
        } catch (RuntimeException | LinkageError cleanupFailure) {
            original.addSuppressed(cleanupFailure);
        }
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
            CapabilityKey capability
    ) implements CapabilityProviderContext {

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

        private void initialize(PluginContext context) {
            provider.initialize(new DefaultCapabilityProviderContext(context, contribution.type().key()));
        }

        private CapabilityRegistration<T> registration(String pluginId, Tags tags) {
            return new CapabilityRegistration<>(contribution.type(), provider, pluginId, tags);
        }
    }
}

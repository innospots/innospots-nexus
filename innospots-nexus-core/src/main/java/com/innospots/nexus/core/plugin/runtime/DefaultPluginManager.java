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
import com.innospots.nexus.core.plugin.config.ConfigurationManager;
import com.innospots.nexus.core.plugin.declaration.CapabilityRequirement;
import com.innospots.nexus.core.plugin.dependency.DependencyResolution;
import com.innospots.nexus.core.plugin.dependency.DependencyResolver;
import com.innospots.nexus.core.plugin.discovery.ClasspathPluginDiscovery;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.event.DefaultPluginEventBus;
import com.innospots.nexus.core.plugin.lifecycle.ManagedPlugin;
import com.innospots.nexus.core.plugin.lifecycle.PluginRuntimeInfo;
import com.innospots.nexus.core.plugin.lifecycle.PluginState;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Lightweight dependency-aware plugin runtime with one serialized lifecycle lock and no global singleton state.
 */
public final class DefaultPluginManager implements PluginManager {

    private PluginRuntimeConfig config;
    private final CapabilityRegistry registry;
    private final DefaultPluginEventBus eventBus = new DefaultPluginEventBus();
    private final List<String> startupOrder = new ArrayList<>();
    private List<DiscoveredPlugin> suppliedDiscoveries;
    private Map<String, ManagedPlugin> managedPlugins;
    private DependencyResolver dependencyResolver;
    private volatile boolean closed;

    private DefaultPluginManager(PluginRuntimeConfig config, List<DiscoveredPlugin> suppliedDiscoveries) {
        if (config == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, "plugin runtime config is required");
        }
        this.config = config;
        this.suppliedDiscoveries = suppliedDiscoveries == null ? null : List.copyOf(suppliedDiscoveries);
        this.registry = new CapabilityRegistry(config.defaultRoutes());
    }

    /**
     * Creates a manager that discovers plugins on the first start call.
     *
     * @param config host runtime configuration
     * @return independent plugin manager instance
     */
    public static DefaultPluginManager create(PluginRuntimeConfig config) {
        return new DefaultPluginManager(config, null);
    }

    /**
     * Creates a manager from a precomputed immutable discovery catalog.
     *
     * @param config host runtime configuration
     * @param catalog static discovery snapshot to consume
     * @return independent plugin manager instance
     */
    public static DefaultPluginManager create(PluginRuntimeConfig config, PluginCatalog catalog) {
        if (catalog == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "plugin catalog is required");
        }
        return new DefaultPluginManager(config, catalog.plugins());
    }

    static DefaultPluginManager create(PluginRuntimeConfig config, List<DiscoveredPlugin> discoveries) {
        return new DefaultPluginManager(config, discoveries);
    }

    /**
     * Discovers and starts eligible plugins in dependency-aware passes.
     *
     * @throws NexusException when discovery fails or a required plugin cannot be activated
     */
    @Override
    public synchronized void start() {
        ensureOpen();
        initializeIfNecessary();
        Set<String> attempted = new LinkedHashSet<>();
        List<String> startedThisCall = new ArrayList<>();
        List<NexusException> startupFailures = new ArrayList<>();
        boolean progressed;
        do {
            progressed = false;
            for (ManagedPlugin plugin : managedPlugins.values()) {
                String pluginId = plugin.definition().id();
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
                    continue;
                }
                attempted.add(pluginId);
                try {
                    plugin.start();
                    startupOrder.add(pluginId);
                    startedThisCall.add(pluginId);
                    progressed = true;
                } catch (NexusException exception) {
                    // Ordinary plugin failure is isolated; required-plugin verification below decides host outcome.
                    startupFailures.add(exception);
                }
            }
        } while (progressed);

        List<String> inactiveRequired = config.requiredPluginIds().stream()
                .filter(id -> managedPlugins.get(id) == null
                        || managedPlugins.get(id).info().state() != PluginState.ACTIVE)
                .sorted()
                .toList();
        if (!inactiveRequired.isEmpty()) {
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
    }

    /**
     * Starts one plugin after resolving its current dependency diagnostics.
     *
     * @param pluginId stable plugin identifier
     * @throws NexusException when the plugin is unknown, blocked, or fails to start
     */
    @Override
    public synchronized void start(String pluginId) {
        ensureOpen();
        initializeIfNecessary();
        ManagedPlugin plugin = requirePlugin(pluginId);
        Map<CapabilityKey, DependencyResolution> dependencies = dependencyResolver.resolve(plugin.definition());
        plugin.dependencies(dependencies);
        if (!dependencyResolver.canStart(dependencies)) {
            plugin.waiting(dependencies);
            boolean missing = dependencies.values().stream()
                    .anyMatch(value -> value.required() && !value.declared());
            throw NexusException.build(
                    missing ? PluginStatusCode.PLUGIN_DEPENDENCY_MISSING : PluginStatusCode.PLUGIN_DEPENDENCY_CYCLE,
                    "required capabilities are unavailable for plugin " + pluginId);
        }
        plugin.start();
        if (!startupOrder.contains(pluginId)) {
            startupOrder.add(pluginId);
        }
    }

    /**
     * Stops one active plugin after checking that no active dependent loses its last provider.
     *
     * @param pluginId stable plugin identifier
     * @throws NexusException when the plugin is unknown, in use, or fails to stop
     */
    @Override
    public synchronized void stop(String pluginId) {
        ensureOpen();
        initializeIfNecessary();
        ManagedPlugin target = requirePlugin(pluginId);
        if (target.info().state() != PluginState.ACTIVE) {
            return;
        }
        protectRequiredCapabilities(target);
        target.stop();
        startupOrder.remove(pluginId);
    }

    /**
     * Returns all current plugin runtime snapshots sorted by id.
     *
     * @return immutable runtime snapshot list
     */
    @Override
    public synchronized List<PluginRuntimeInfo> plugins() {
        ensureOpen();
        initializeIfNecessary();
        return managedPlugins.values().stream()
                .map(ManagedPlugin::info)
                .sorted(Comparator.comparing(PluginRuntimeInfo::id))
                .toList();
    }

    /**
     * Finds one current plugin runtime snapshot.
     *
     * @param pluginId stable plugin identifier
     * @return matching snapshot, or empty when not discovered
     */
    @Override
    public synchronized Optional<PluginRuntimeInfo> plugin(String pluginId) {
        ensureOpen();
        initializeIfNecessary();
        ManagedPlugin plugin = managedPlugins.get(pluginId);
        return plugin == null ? Optional.empty() : Optional.of(plugin.info());
    }

    /** Returns the active capability lookup boundary for this manager. */
    @Override
    public synchronized CapabilityManager capabilities() {
        ensureOpen();
        return registry;
    }

    /**
     * Stops active plugins in reverse startup order and releases runtime references.
     *
     * @throws NexusException when one or more plugin stops fail; all stop attempts are still made
     */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        if (managedPlugins == null) {
            eventBus.close();
            config = null;
            closed = true;
            return;
        }
        List<String> reverse = new ArrayList<>(startupOrder);
        Collections.reverse(reverse);
        RuntimeException first = null;
        for (String pluginId : reverse) {
            try {
                managedPlugins.get(pluginId).stop();
            } catch (RuntimeException exception) {
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
        suppliedDiscoveries = null;
        eventBus.close();
        config = null;
        closed = true;
        if (first != null) {
            throw first;
        }
    }

    private void initializeIfNecessary() {
        if (managedPlugins != null) {
            return;
        }
        List<DiscoveredPlugin> discoveries = suppliedDiscoveries;
        if (discoveries == null) {
            ClassLoader classLoader = config.resolvedClassLoader(DefaultPluginManager.class.getClassLoader());
            discoveries = new ClasspathPluginDiscovery(classLoader).discover();
        }
        ConfigurationManager configuration = ConfigurationManager.standard(
                config.hostConfig(),
                config.runtimeVariables());
        validateRequiredPlugins(discoveries);
        validateDefaultRoutes(discoveries);
        ConfigurationManager.validateEnvironmentNames(
                discoveries.stream().map(DiscoveredPlugin::definition).toList());
        Map<String, ManagedPlugin> created = new LinkedHashMap<>();
        for (DiscoveredPlugin discovery : discoveries) {
            created.put(discovery.definition().id(), new ManagedPlugin(
                    discovery,
                    () -> configuration.resolve(discovery.definition()),
                    registry,
                    eventBus));
        }
        managedPlugins = Collections.unmodifiableMap(created);
        dependencyResolver = new DependencyResolver(
                discoveries.stream().map(DiscoveredPlugin::definition).toList(),
                registry);
        suppliedDiscoveries = null;
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
                .map(item -> item.definition().id())
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
                    .filter(item -> !config.disabledPluginIds().contains(item.definition().id()))
                    .filter(item -> item.definition().tags().matches(route.getValue()))
                    .filter(item -> item.definition().capabilities().stream()
                            .anyMatch(capability -> capability.type().key().equals(route.getKey())))
                    .map(item -> item.definition().id())
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
                        .filter(registration -> !registration.pluginId().equals(target.definition().id()))
                        .count();
                if (remaining == 0) {
                    throw NexusException.build(
                            PluginStatusCode.PLUGIN_IN_USE,
                            "plugin " + target.definition().id() + " is required by "
                                    + dependent.definition().id() + " for " + requirement.key());
                }
            }
        }
    }

    private void rollbackStarted(List<String> startedThisCall) {
        List<String> reverse = new ArrayList<>(startedThisCall);
        Collections.reverse(reverse);
        for (String pluginId : reverse) {
            try {
                managedPlugins.get(pluginId).stop();
            } catch (RuntimeException ignored) {
                // Required startup is already failing; ManagedPlugin retains its own sanitized failure diagnostics.
            }
            startupOrder.remove(pluginId);
        }
    }
}

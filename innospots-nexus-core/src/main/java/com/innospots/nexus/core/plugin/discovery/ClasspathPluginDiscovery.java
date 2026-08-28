package com.innospots.nexus.core.plugin.discovery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Discovers every Plugin visible to one class loader through Java ServiceLoader.
 * Discovery reads definitions but deliberately never invokes provider factories or lifecycle methods.
 */
public final class ClasspathPluginDiscovery {

    private final ClassLoader classLoader;

    /**
     * Creates a discovery operation for the supplied class loader.
     *
     * @param classLoader loader whose visible ServiceLoader entries are inspected
     */
    public ClasspathPluginDiscovery(ClassLoader classLoader) {
        if (classLoader == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "plugin classLoader is required");
        }
        this.classLoader = classLoader;
    }

    /**
     * Performs one static-style discovery operation without retaining a global mutable cache.
     *
     * @param classLoader loader whose visible ServiceLoader entries are inspected
     * @return immutable, validated discovery snapshot
     */
    public static List<DiscoveredPlugin> discover(ClassLoader classLoader) {
        return new ClasspathPluginDiscovery(classLoader).discover();
    }

    /**
     * Discovers and globally validates visible plugins.
     *
     * @return deterministic immutable plugin list sorted by plugin id
     */
    public List<DiscoveredPlugin> discover() {
        List<DiscoveredPlugin> discovered = new ArrayList<>();
        try {
            for (ServiceLoader.Provider<Plugin> provider : ServiceLoader.load(Plugin.class, classLoader).stream()
                    .toList()) {
                Plugin plugin = provider.get();
                PluginDefinition definition = plugin.definition();
                if (definition == null) {
                    throw NexusException.build(
                            PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                            "plugin returned null definition: " + provider.type().getName());
                }
                discovered.add(new DiscoveredPlugin(plugin, definition, Instant.now()));
            }
        } catch (NexusException exception) {
            throw exception;
        } catch (ServiceConfigurationError | RuntimeException | LinkageError exception) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DISCOVERY_FAILED.fullCode(),
                    "failed to discover classpath plugins",
                    exception);
        }

        validate(discovered);
        discovered.sort(Comparator.comparing(item -> item.definition().id()));
        return List.copyOf(discovered);
    }

    static void validate(List<DiscoveredPlugin> discovered) {
        Set<String> pluginIds = new HashSet<>();
        Map<CapabilityKey, Class<?>> capabilityApis = new HashMap<>();
        for (DiscoveredPlugin item : discovered) {
            PluginDefinition definition = item.definition();
            if (!pluginIds.add(definition.id())) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_DUPLICATE,
                        "duplicate plugin id: " + definition.id());
            }
            if (definition.apiVersion() != PluginDefinition.CURRENT_API_VERSION) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_API_INCOMPATIBLE,
                        "unsupported plugin apiVersion for " + definition.id() + ": " + definition.apiVersion());
            }
            for (CapabilityContribution<?> contribution : definition.capabilities()) {
                Class<?> previous = capabilityApis.putIfAbsent(
                        contribution.type().key(),
                        contribution.type().api());
                if (previous != null && previous != contribution.type().api()) {
                    throw NexusException.build(
                            PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                            "different API classes declared for " + contribution.type().key());
                }
            }
        }
    }
}

package com.innospots.nexus.core.plugin.discovery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Immutable discovery snapshot for one plugin runtime.
 *
 * <p>The catalog is static metadata after creation, but its discovered plugin instances belong to
 * the runtime that consumes it and must not be shared by multiple managers.</p>
 */
public final class PluginCatalog {

    private final List<DiscoveredPlugin> plugins;
    private final Map<String, DiscoveredPlugin> byId;

    private PluginCatalog(List<DiscoveredPlugin> plugins) {
        if (plugins == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "plugin catalog entries must not be null");
        }
        this.plugins = List.copyOf(plugins);
        Map<String, DiscoveredPlugin> index = new LinkedHashMap<>();
        for (DiscoveredPlugin plugin : this.plugins) {
            if (plugin == null || plugin.definition() == null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                        "plugin catalog contains an invalid entry");
            }
            if (index.putIfAbsent(plugin.definition().id(), plugin) != null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_DUPLICATE,
                        "duplicate plugin id: " + plugin.definition().id());
            }
        }
        this.byId = Map.copyOf(index);
    }

    /** Discovers and globally validates all plugins visible to the supplied ClassLoader. */
    public static PluginCatalog discover(ClassLoader classLoader) {
        return new PluginCatalog(new ClasspathPluginDiscovery(classLoader).discover());
    }

    /** Creates a catalog from an already validated discovery list. */
    public static PluginCatalog of(List<DiscoveredPlugin> plugins) {
        return new PluginCatalog(plugins);
    }

    /** Returns the immutable discovery list in deterministic plugin-id order. */
    public List<DiscoveredPlugin> plugins() {
        return plugins;
    }

    /** Finds one discovered plugin by stable id. */
    public Optional<DiscoveredPlugin> plugin(String pluginId) {
        return Optional.ofNullable(byId.get(pluginId));
    }

    /** Returns immutable definition snapshots for diagnostics and preflight validation. */
    public List<PluginDefinition> definitions() {
        return plugins.stream().map(DiscoveredPlugin::definition).toList();
    }
}

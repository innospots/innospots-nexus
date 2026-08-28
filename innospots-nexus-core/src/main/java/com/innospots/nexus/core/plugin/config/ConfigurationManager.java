package com.innospots.nexus.core.plugin.config;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Resolves plugin defaults, host values, environment, system properties, and runtime overrides.
 */
public final class ConfigurationManager {

    private final Map<String, String> hostConfig;
    private final Map<String, String> environment;
    private final Map<String, String> systemProperties;
    private final Map<String, String> runtimeVariables;

    /** Creates a resolver with sources ordered from host through runtime precedence. */
    public ConfigurationManager(
            Map<String, String> hostConfig,
            Map<String, String> environment,
            Map<String, String> systemProperties,
            Map<String, String> runtimeVariables
    ) {
        this.hostConfig = immutable(hostConfig);
        this.environment = immutable(environment);
        this.systemProperties = immutable(systemProperties);
        this.runtimeVariables = immutable(runtimeVariables);
    }

    /** Creates a resolver backed by current process environment and system properties. */
    public static ConfigurationManager standard(
            Map<String, String> hostConfig,
            Map<String, String> runtimeVariables
    ) {
        Map<String, String> properties = System.getProperties().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        entry -> String.valueOf(entry.getKey()),
                        entry -> String.valueOf(entry.getValue())));
        return new ConfigurationManager(hostConfig, System.getenv(), properties, runtimeVariables);
    }

    /** Resolves and validates one plugin's isolated configuration snapshot. */
    public PluginConfig resolve(PluginDefinition definition) {
        String prefix = "plugins." + definition.id() + ".";
        Map<String, ConfigItemDefinition> items = definition.config().items().stream()
                .collect(Collectors.toUnmodifiableMap(ConfigItemDefinition::key, item -> item));
        rejectUnknown(prefix, items.keySet(), hostConfig);
        rejectUnknown(prefix, items.keySet(), systemProperties);
        rejectUnknown(prefix, items.keySet(), runtimeVariables);

        Map<String, String> merged = new LinkedHashMap<>();
        for (ConfigItemDefinition item : definition.config().items()) {
            if (item.defaultValue() != null) {
                merged.put(item.key(), item.defaultValue());
            }
        }
        overlay(prefix, items.keySet(), hostConfig, merged);
        overlayEnvironment(definition.id(), items.keySet(), merged);
        overlay(prefix, items.keySet(), systemProperties, merged);
        overlay(prefix, items.keySet(), runtimeVariables, merged);

        Map<String, Object> typed = new LinkedHashMap<>();
        Map<String, String> display = new LinkedHashMap<>();
        for (ConfigItemDefinition item : definition.config().items()) {
            String raw = merged.get(item.key());
            if ((raw == null || raw.isBlank()) && item.required()) {
                throw invalid("required plugin config is missing: " + item.key());
            }
            if (raw != null) {
                Object value = convert(item, raw);
                typed.put(item.key(), value);
                // Diagnostics intentionally reveal presence only, never raw configuration values.
                display.put(item.key(), item.secret() ? "<secret>" : "<set>");
            }
        }
        return new DefaultPluginConfig(typed, display);
    }

    /** Maps one full plugin key to its deterministic environment variable name. */
    public static String environmentName(String pluginId, String itemKey) {
        return "NEXUS_PLUGIN_" + (pluginId + "." + itemKey)
                .replace('-', '_')
                .replace('.', '_')
                .toUpperCase(java.util.Locale.ROOT);
    }

    private static Map<String, String> immutable(Map<String, String> source) {
        return source == null ? Map.of() : Map.copyOf(source);
    }

    private static void rejectUnknown(
            String prefix,
            Set<String> knownKeys,
            Map<String, String> source
    ) {
        for (String fullKey : source.keySet()) {
            if (fullKey.startsWith(prefix) && !knownKeys.contains(fullKey.substring(prefix.length()))) {
                throw invalid("unknown plugin config key: " + fullKey);
            }
        }
    }

    private static void overlay(
            String prefix,
            Set<String> knownKeys,
            Map<String, String> source,
            Map<String, String> target
    ) {
        for (String key : knownKeys) {
            String value = source.get(prefix + key);
            if (value != null) {
                target.put(key, value);
            }
        }
    }

    private void overlayEnvironment(String pluginId, Set<String> knownKeys, Map<String, String> target) {
        Map<String, String> environmentNames = new HashMap<>();
        for (String key : knownKeys) {
            String name = environmentName(pluginId, key);
            String previous = environmentNames.putIfAbsent(name, key);
            if (previous != null && !previous.equals(key)) {
                throw invalid("plugin config environment name conflict: " + previous + ", " + key);
            }
            String value = environment.get(name);
            if (value != null) {
                target.put(key, value);
            }
        }
    }

    private static Object convert(ConfigItemDefinition item, String raw) {
        try {
            return switch (item.type()) {
                case STRING -> raw;
                case INTEGER -> Integer.valueOf(raw);
                case LONG -> Long.valueOf(raw);
                case BOOLEAN -> parseBoolean(item.key(), raw);
                case DURATION -> Duration.parse(raw);
                case SECRET -> SecretValue.of(raw);
            };
        } catch (NumberFormatException | DateTimeParseException exception) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_CONFIG_INVALID.fullCode(),
                    "cannot convert plugin config: " + item.key(),
                    exception);
        }
    }

    private static boolean parseBoolean(String key, String raw) {
        if ("true".equalsIgnoreCase(raw)) {
            return true;
        }
        if ("false".equalsIgnoreCase(raw)) {
            return false;
        }
        throw invalid("cannot convert plugin config to boolean: " + key);
    }

    private static NexusException invalid(String message) {
        return NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, message);
    }
}

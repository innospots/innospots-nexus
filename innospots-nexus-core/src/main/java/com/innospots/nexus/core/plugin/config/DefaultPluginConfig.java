package com.innospots.nexus.core.plugin.config;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Immutable typed configuration snapshot for one plugin.
 */
final class DefaultPluginConfig implements PluginConfig {

    private final Map<String, Object> values;
    private final Map<String, String> displayValues;

    DefaultPluginConfig(Map<String, Object> values, Map<String, String> displayValues) {
        this.values = Map.copyOf(values);
        this.displayValues = Map.copyOf(displayValues);
    }

    @Override
    public Optional<String> get(String key) {
        Object value = values.get(key);
        return value instanceof String text ? Optional.of(text) : Optional.empty();
    }

    @Override
    public String require(String key) {
        return get(key).orElseThrow(() -> invalid("required string config is missing: " + key));
    }

    @Override
    public int getInt(String key, int defaultValue) {
        Object value = values.get(key);
        return value instanceof Integer number ? number : defaultValue;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        Object value = values.get(key);
        return value instanceof Long number ? number : defaultValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = values.get(key);
        return value instanceof Boolean flag ? flag : defaultValue;
    }

    @Override
    public Duration getDuration(String key, Duration defaultValue) {
        Object value = values.get(key);
        return value instanceof Duration duration ? duration : defaultValue;
    }

    @Override
    public SecretValue requireSecret(String key) {
        Object value = values.get(key);
        if (value instanceof SecretValue secret) {
            // Callers own the returned handle and cannot clear the runtime's retained snapshot.
            return secret.copy();
        }
        throw invalid("required secret config is missing: " + key);
    }

    @Override
    public String toString() {
        return new LinkedHashMap<>(displayValues).toString();
    }

    private static NexusException invalid(String message) {
        return NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, message);
    }
}

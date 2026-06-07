package com.innospots.nexus.base.config;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable configuration store that wraps a flat key-value map.
 * <p>Keys must be non-blank strings; null values are skipped during construction.
 * Typed accessor methods ({@link #getBoolean}, {@link #getInt}) perform
 * automatic conversion via Hutool's {@code Convert} utility.</p>
 */
public final class NexusConfig {

    private final Map<String, String> values;

    private NexusConfig(Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    /**
     * Creates an immutable config from a source map. Null values are omitted;
     * blank keys cause an immediate failure.
     *
     * @param source the source key-value pairs
     * @return a new NexusConfig with copied values
     */
    public static NexusConfig of(Map<String, ?> source) {
        if (MapUtil.isEmpty(source)) {
            return new NexusConfig(Map.of());
        }
        Map<String, String> values = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (CharSequenceUtil.isBlank(key)) {
                throw new IllegalArgumentException("Config key must not be blank");
            }
            if (value != null) {
                values.put(key, Convert.toStr(value));
            }
        });
        return new NexusConfig(values);
    }

    /**
     * Returns the raw value for the given key.
     *
     * @param key config key
     * @return Optional containing the value, or empty if absent
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(values.get(key));
    }

    /**
     * Returns the value for the given key, falling back to a default if absent.
     */
    public String get(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }

    /**
     * Returns the boolean value for the given key, falling back to a default.
     * Conversion is performed by {@link Convert#toBool(Object)}.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return get(key).map(Convert::toBool).orElse(defaultValue);
    }

    /**
     * Returns the int value for the given key, falling back to a default.
     * Conversion is performed by {@link Convert#toInt(Object)}.
     */
    public int getInt(String key, int defaultValue) {
        return get(key).map(Convert::toInt).orElse(defaultValue);
    }

    /**
     * Returns an unmodifiable view of the underlying config map.
     */
    public Map<String, String> asMap() {
        return values;
    }
}

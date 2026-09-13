package com.innospots.nexus.core.bootstrap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cross-step context passed through startup task execution.
 */
public final class NexusStartupContext {

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    /**
     * Stores a startup attribute.
     *
     * @param key   attribute key
     * @param value attribute value; {@code null} removes the key
     */
    public void putAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    /**
     * Reads a typed startup attribute.
     *
     * @param key  attribute key
     * @param type expected value type
     * @return attribute value when present and type-compatible
     */
    public <T> Optional<T> getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }
}

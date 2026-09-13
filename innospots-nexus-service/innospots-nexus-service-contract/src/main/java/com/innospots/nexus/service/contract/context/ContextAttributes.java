package com.innospots.nexus.service.contract.context;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;

/**
 * Immutable typed attribute bag. Values are frozen scalars, records, or copied collections.
 *
 * @author Smars
 * @date 2026/09/13
 * @see AttributeKey
 * @see ServiceContext
 */
public final class ContextAttributes {

    private final Map<AttributeKey<?>, Object> values;

    private ContextAttributes(Map<AttributeKey<?>, Object> values) {
        this.values = Map.copyOf(values);
    }

    /**
     * Returns an empty attribute bag.
     *
     * @return empty attributes
     */
    public static ContextAttributes empty() {
        return new ContextAttributes(Map.of());
    }

    /**
     * Finds the value for {@code key} when present and assignable.
     *
     * @param key typed attribute key
     * @param <T> value type
     * @return value or empty
     */
    public <T> Optional<T> find(AttributeKey<T> key) {
        Checks.notNull(key, "key");
        Object value = values.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(key.type().cast(value));
    }

    /**
     * Returns a copy with {@code key} set to a frozen {@code value}.
     *
     * @param key   typed attribute key
     * @param value immutable-compatible value
     * @param <T>   value type
     * @return new attributes instance
     */
    public <T> ContextAttributes with(AttributeKey<T> key, T value) {
        Checks.notNull(key, "key");
        Checks.notNull(value, "value");
        Map<AttributeKey<?>, Object> next = new LinkedHashMap<>(values);
        next.put(key, freeze(value));
        return new ContextAttributes(next);
    }

    private static Object freeze(Object value) {
        if (value instanceof String
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof Enum<?>
                || value instanceof Duration
                || value instanceof Instant
                || value instanceof UUID
                || value instanceof Record) {
            return value;
        }
        if (value instanceof List<?> list) {
            return List.copyOf(list);
        }
        if (value instanceof Set<?> set) {
            return Set.copyOf(set);
        }
        if (value instanceof Map<?, ?> map) {
            return Map.copyOf(map);
        }
        throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, "attribute value must be immutable");
    }
}

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
 * 不可变的类型化属性容器。值为冻结标量、记录或复制后的集合。
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
     * 返回空属性容器。
     *
     * @return 空属性
     */
    public static ContextAttributes empty() {
        return new ContextAttributes(Map.of());
    }

    /**
     * 在存在且可赋值时查找 {@code key} 对应的值。
     *
     * @param key 类型化属性键
     * @param <T> 值类型
     * @return 值，不存在时为空
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
     * 返回将 {@code key} 设为冻结 {@code value} 后的副本。
     *
     * @param key   类型化属性键
     * @param value 不可变兼容值
     * @param <T>   值类型
     * @return 新属性实例
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

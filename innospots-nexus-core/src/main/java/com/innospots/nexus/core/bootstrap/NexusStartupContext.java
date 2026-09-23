package com.innospots.nexus.core.bootstrap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 启动任务执行过程中跨步骤传递的轻量上下文。
 *
 * @author Smars
 * @date 2026/09/13
 * @see NexusStartupTask
 */
public final class NexusStartupContext {

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    /**
     * 存储启动属性。
     *
     * @param key   属性键
     * @param value 属性值；为 {@code null} 时移除该键
     */
    public void putAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    /**
     * 读取指定类型的启动属性。
     *
     * @param key  属性键
     * @param type 期望的值类型
     * @param <T>  值类型
     * @return 存在且类型匹配时的属性值
     */
    public <T> Optional<T> getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }
}

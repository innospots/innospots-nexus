package com.innospots.nexus.core.plugin.capability;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 不可变且按名称排序的路由标签。
 *
 * <p>实例线程安全，可自由跨线程共享；迭代顺序稳定，便于诊断输出。
 */
public final class Tags {

    private static final Tags EMPTY = new Tags(Map.of());

    private final Map<String, String> values;

    private Tags(Map<String, String> values) {
        TreeMap<String, String> sorted = new TreeMap<>();
        values.forEach((name, value) -> {
            Tag tag = new Tag(name, value);
            sorted.put(tag.name(), tag.value());
        });
        // Map.copyOf 不保证遍历顺序，因此显式保留排序结果，确保诊断信息稳定。
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(sorted));
    }

    /**
     * 返回用于无约束查询的空标签集合。
     *
     * @return 共享的空标签实例
     */
    public static Tags empty() {
        return EMPTY;
    }

    /**
     * 创建包含一个属性的标签集合。
     *
     * @param name  标签名
     * @param value 标签值
     * @return 新标签集合
     * @throws NexusException {@code name} 或 {@code value} 为 {@code null} 时
     */
    public static Tags of(String name, String value) {
        if (name == null || value == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "tag name and value must not be null");
        }
        return new Tags(Map.of(name, value));
    }

    /**
     * 根据属性映射创建标签集合。
     *
     * @param values 标签映射；空映射返回 {@link #empty()}
     * @return 新标签集合
     * @throws NexusException {@code values} 为 {@code null} 时
     */
    public static Tags from(Map<String, String> values) {
        if (values == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID, "tags must not be null");
        }
        return values.isEmpty() ? EMPTY : new Tags(values);
    }

    /**
     * 合并插件级与 Provider 级标签。
     *
     * @param pluginTags   插件默认标签
     * @param providerTags Provider 标签
     * @return 合并后的不可变标签集合
     */
    public static Tags merge(Tags pluginTags, Tags providerTags) {
        Tags merged = pluginTags == null ? EMPTY : pluginTags;
        if (providerTags == null) {
            return merged;
        }
        for (Map.Entry<String, String> entry : providerTags.asMap().entrySet()) {
            merged = merged.and(entry.getKey(), entry.getValue());
        }
        return merged;
    }

    /**
     * 返回新增一个无冲突属性后的标签集合。
     *
     * @param name  标签名
     * @param value 标签值
     * @return 包含新属性的不可变副本
     * @throws NexusException 同名标签已存在且值不同时
     */
    public Tags and(String name, String value) {
        Tag tag = new Tag(name, value);
        String existing = values.get(tag.name());
        if (existing != null && !existing.equals(tag.value())) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "tag already has another value: " + name);
        }
        TreeMap<String, String> copy = new TreeMap<>(values);
        copy.put(tag.name(), tag.value());
        return new Tags(copy);
    }

    /**
     * 查找指定标签值。
     *
     * @param name 标签名
     * @return 标签值；不存在时为空
     */
    public Optional<String> get(String name) {
        return Optional.ofNullable(values.get(name));
    }

    /**
     * 判断当前 Provider 标签是否包含请求的全部标签。
     *
     * @param required 请求侧必需标签
     * @return 全部包含时 {@code true}；{@code required} 为 {@code null} 时 {@code false}
     */
    public boolean matches(Tags required) {
        if (required == null) {
            return false;
        }
        return values.entrySet().containsAll(required.values.entrySet());
    }

    /**
     * 返回不可变标签映射。
     *
     * @return 按稳定顺序排列的标签快照
     */
    public Map<String, String> asMap() {
        return values;
    }

    /**
     * 返回当前是否没有任何标签。
     *
     * @return 无标签时 {@code true}
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Tags tags && values.equals(tags.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return values.toString();
    }
}

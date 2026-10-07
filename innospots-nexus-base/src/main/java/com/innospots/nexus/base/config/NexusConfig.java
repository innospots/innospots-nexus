package com.innospots.nexus.base.config;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 包装扁平键值映射的不可变配置存储。
 * <p>键必须为非空白字符串；构造时跳过 null 值。
 * 类型化访问方法（{@link #getBoolean}、{@link #getInt}）通过 Hutool 的 {@code Convert} 工具自动转换。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class NexusConfig {

    private final Map<String, String> values;

    private NexusConfig(Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    /**
     * 从源映射创建不可变配置。null 值被省略；空白键立即失败。
     *
     * @param source 源键值对
     * @return 新的 NexusConfig 实例
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
     * 返回指定键的原始值。
     *
     * @param key 配置键
     * @return 包含值的 Optional，不存在时为空
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(values.get(key));
    }

    /**
     * 返回指定键的值，不存在时回退到默认值。
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public String get(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }

    /**
     * 返回指定键的布尔值，不存在时回退到默认值。
     * 转换由 {@link Convert#toBool(Object)} 执行。
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 布尔值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return get(key).map(Convert::toBool).orElse(defaultValue);
    }

    /**
     * 返回指定键的整数值，不存在时回退到默认值。
     * 转换由 {@link Convert#toInt(Object)} 执行。
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 整数值
     */
    public int getInt(String key, int defaultValue) {
        return get(key).map(Convert::toInt).orElse(defaultValue);
    }

    /**
     * 返回底层配置映射的不可修改视图。
     *
     * @return 配置映射
     */
    public Map<String, String> asMap() {
        return values;
    }
}

package com.innospots.nexus.base.util;

import java.util.Map;
import java.util.Properties;

/**
 * 环境属性解析器，支持程序化覆盖。查找顺序为：覆盖值 → 系统属性 → 环境变量。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class EnvUtils {

    private static final Properties OVERRIDES = new Properties();

    private EnvUtils() {
    }

    /**
     * 按键查找环境值，未找到时返回 {@code null}。
     *
     * @param key 属性键
     * @return 解析到的值，或 {@code null}
     */
    public static String value(String key) {
        return value(key, null);
    }

    /**
     * 按键查找环境值，未找到时返回默认值。
     *
     * @param key          属性键
     * @param defaultValue 未找到时的回退值
     * @return 解析到的值或默认值
     */
    public static String value(String key, String defaultValue) {
        String value = OVERRIDES.getProperty(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        if (value == null) {
            value = System.getenv(key);
        }
        return value == null ? defaultValue : value;
    }

    /**
     * 设置程序化覆盖值；传入 {@code null} 时清除该键。
     *
     * @param key   属性键
     * @param value 覆盖值
     */
    public static void set(String key, String value) {
        if (value == null) {
            clear(key);
        } else {
            OVERRIDES.setProperty(key, value);
        }
    }

    /**
     * 批量写入程序化覆盖值，跳过 null 条目。
     *
     * @param values 键值映射
     */
    public static void putAll(Map<String, ?> values) {
        if (values == null) {
            return;
        }
        values.forEach((key, value) -> {
            if (value != null) {
                OVERRIDES.setProperty(key, String.valueOf(value));
            }
        });
    }

    /**
     * 清除指定键的程序化覆盖值。
     *
     * @param key 属性键
     */
    public static void clear(String key) {
        OVERRIDES.remove(key);
    }
}

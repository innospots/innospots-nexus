package com.innospots.nexus.core.plugin.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 描述一个插件本地配置键的不可变 schema 项。
 *
 * @param key 插件本地配置键；小写驼峰，最长 128 字符
 * @param type 配置值类型
 * @param required 是否必须提供值
 * @param defaultValue 可选文本默认值；SECRET 类型禁止声明默认值
 * @param secret 诊断信息是否必须遮罩该值；SECRET 类型会强制为 {@code true}
 * @param description 面向用户的配置说明
 * @param enumValues ENUM 类型允许的值；非 ENUM 类型必须为空
 */
public record ConfigItemDefinition(
        String key,
        ConfigType type,
        boolean required,
        String defaultValue,
        boolean secret,
        String description,
        List<String> enumValues
) {

    private static final Pattern KEY_PATTERN = Pattern.compile(
            "[a-z][a-zA-Z0-9]*");

    /**
     * @throws NexusException 键、类型、枚举值或默认值组合非法时抛出
     */
    public ConfigItemDefinition {
        if (key == null || key.length() > 128 || !KEY_PATTERN.matcher(key).matches() || type == null) {
            invalid("invalid plugin config key or type: " + key);
        }
        List<String> values = new ArrayList<>();
        if (enumValues != null) {
            for (String value : enumValues) {
                if (value == null || value.isBlank() || value.length() > 256) {
                    invalid("enum config values must be non-blank and at most 256 characters: " + key);
                }
                if (values.contains(value)) {
                    invalid("duplicate enum config value: " + value);
                }
                values.add(value);
            }
        }
        enumValues = List.copyOf(values);
        if (type == ConfigType.ENUM && enumValues.isEmpty()) {
            invalid("ENUM config must declare enum values: " + key);
        }
        if (type != ConfigType.ENUM && !enumValues.isEmpty()) {
            invalid("only ENUM config may declare enum values: " + key);
        }
        if ((secret || type == ConfigType.SECRET) && defaultValue != null) {
            invalid("secret config cannot declare a default value: " + key);
        }
        if (type == ConfigType.SECRET && !secret) {
            secret = true;
        }
        description = description == null ? "" : description;
    }

    /**
     * 保留旧的六参数构造方式，默认不声明枚举值。
     *
     * @throws NexusException 键、类型或默认值组合非法时抛出
     */
    public ConfigItemDefinition(
            String key,
            ConfigType type,
            boolean required,
            String defaultValue,
            boolean secret,
            String description
    ) {
        this(key, type, required, defaultValue, secret, description, List.of());
    }

    private static void invalid(String message) {
        throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, message);
    }
}

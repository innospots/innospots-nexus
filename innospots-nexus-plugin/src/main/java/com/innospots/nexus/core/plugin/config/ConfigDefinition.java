package com.innospots.nexus.core.plugin.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 一个插件允许使用的配置键不可变 schema。
 */
public interface ConfigDefinition {

    /**
     * 按声明顺序返回配置项。
     *
     * @return 不可变配置项集合
     */
    Collection<ConfigItemDefinition> items();

    /**
     * 返回空配置 schema。
     *
     * @return 不包含任何配置项的 schema
     */
    static ConfigDefinition empty() {
        return () -> List.of();
    }

    /**
     * 创建流式配置 schema 构建器。
     *
     * @return 新的 schema 构建器
     */
    static Builder builder() {
        return new Builder();
    }

    /** 不可变配置 schema 的流式构建器。 */
    final class Builder {

        private final Map<String, ConfigItemDefinition> items = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * 开始声明字符串配置项。
         *
         * @param key 插件本地配置键
         * @return 配置项构建器
         */
        public ItemBuilder string(String key) {
            return item(key, ConfigType.STRING);
        }

        /**
         * 开始声明整数配置项。
         *
         * @param key 插件本地配置键
         * @return 配置项构建器
         */
        public ItemBuilder integer(String key) {
            return item(key, ConfigType.INTEGER);
        }

        /**
         * 开始声明长整数配置项。
         *
         * @param key 插件本地配置键
         * @return 配置项构建器
         */
        public ItemBuilder longNumber(String key) {
            return item(key, ConfigType.LONG);
        }

        /**
         * 开始声明布尔配置项。
         *
         * @param key 插件本地配置键
         * @return 配置项构建器
         */
        public ItemBuilder bool(String key) {
            return item(key, ConfigType.BOOLEAN);
        }

        /**
         * 开始声明时长配置项。
         *
         * @param key 插件本地配置键
         * @return 配置项构建器
         */
        public ItemBuilder duration(String key) {
            return item(key, ConfigType.DURATION);
        }

        /**
         * 开始声明十进制定点数配置项。
         *
         * @param key 插件本地配置键
         * @return 配置项构建器
         */
        public ItemBuilder decimal(String key) {
            return item(key, ConfigType.DECIMAL);
        }

        /**
         * 开始声明绝对 URI 配置项。
         *
         * @param key 插件本地配置键
         * @return 配置项构建器
         */
        public ItemBuilder uri(String key) {
            return item(key, ConfigType.URI);
        }

        /**
         * 开始声明枚举配置项。
         *
         * @param key         插件本地配置键
         * @param enumValues  允许取值列表
         * @return 配置项构建器
         */
        public ItemBuilder enumeration(String key, String... enumValues) {
            return item(key, ConfigType.ENUM).enumValues(enumValues);
        }

        /**
         * 开始声明密文配置项。
         *
         * @param key 插件本地配置键
         * @return 配置项构建器
         */
        public ItemBuilder secret(String key) {
            return item(key, ConfigType.SECRET).secret();
        }

        /**
         * 按声明顺序构建不可变 schema。
         *
         * @return 不可变配置 schema
         */
        public ConfigDefinition build() {
            List<ConfigItemDefinition> snapshot = List.copyOf(items.values());
            return () -> snapshot;
        }

        private ItemBuilder item(String key, ConfigType type) {
            if (items.containsKey(key)) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        "duplicate plugin config key: " + key);
            }
            return new ItemBuilder(this, key, type);
        }

        private void add(ConfigItemDefinition item) {
            if (items.putIfAbsent(item.key(), item) != null) {
                throw NexusException.build(
                        PluginStatusCode.PLUGIN_CONFIG_INVALID,
                        "duplicate plugin config key: " + item.key());
            }
        }
    }

    /** 单个配置项的构建器，通过 {@link #end()} 返回所属 schema 构建器。 */
    final class ItemBuilder {

        private final Builder parent;
        private final String key;
        private final ConfigType type;
        private boolean required;
        private String defaultValue;
        private boolean secret;
        private String description;
        private List<String> enumValues = List.of();

        private ItemBuilder(Builder parent, String key, ConfigType type) {
            this.parent = parent;
            this.key = key;
            this.type = type;
        }

        /**
         * 将配置项标记为必填。
         *
         * @return 当前配置项构建器
         */
        public ItemBuilder required() {
            this.required = true;
            return this;
        }

        /**
         * 设置在配置解析时转换的文本默认值。
         *
         * @param defaultValue 文本默认值
         * @return 当前配置项构建器
         */
        public ItemBuilder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * 将该配置项的诊断信息标记为密文。
         *
         * @return 当前配置项构建器
         */
        public ItemBuilder secret() {
            this.secret = true;
            return this;
        }

        /**
         * 设置面向用户的配置项说明。
         *
         * @param description 展示说明
         * @return 当前配置项构建器
         */
        public ItemBuilder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * 设置枚举配置项允许的值。
         *
         * @param enumValues 允许取值列表；{@code null} 视为空列表
         * @return 当前配置项构建器
         */
        public ItemBuilder enumValues(String... enumValues) {
            this.enumValues = enumValues == null
                    ? List.of()
                    : new ArrayList<>(Arrays.asList(enumValues));
            return this;
        }

        /**
         * 完成当前配置项并返回 schema 构建器。
         *
         * @return 所属 schema 构建器
         */
        public Builder end() {
            parent.add(new ConfigItemDefinition(
                    key,
                    type,
                    required,
                    defaultValue,
                    secret,
                    description,
                    enumValues));
            return parent;
        }
    }
}

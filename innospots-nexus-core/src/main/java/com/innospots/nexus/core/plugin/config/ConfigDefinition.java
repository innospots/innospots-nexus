package com.innospots.nexus.core.plugin.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Immutable schema of configuration keys accepted by one plugin.
 */
public interface ConfigDefinition {

    /**
     * Returns declared configuration items in declaration order.
     *
     * @return immutable configuration item collection
     */
    Collection<ConfigItemDefinition> items();

    /** Returns an empty configuration schema. */
    static ConfigDefinition empty() {
        return () -> List.of();
    }

    /** Creates a fluent configuration schema builder. */
    static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for an immutable configuration schema. */
    final class Builder {

        private final Map<String, ConfigItemDefinition> items = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Starts a string item declaration.
         *
         * @param key plugin-local configuration key
         * @return item builder
         */
        public ItemBuilder string(String key) {
            return item(key, ConfigType.STRING);
        }

        /**
         * Starts an integer item declaration.
         *
         * @param key plugin-local configuration key
         * @return item builder
         */
        public ItemBuilder integer(String key) {
            return item(key, ConfigType.INTEGER);
        }

        /**
         * Starts a long item declaration.
         *
         * @param key plugin-local configuration key
         * @return item builder
         */
        public ItemBuilder longNumber(String key) {
            return item(key, ConfigType.LONG);
        }

        /**
         * Starts a boolean item declaration.
         *
         * @param key plugin-local configuration key
         * @return item builder
         */
        public ItemBuilder bool(String key) {
            return item(key, ConfigType.BOOLEAN);
        }

        /**
         * Starts a duration item declaration.
         *
         * @param key plugin-local configuration key
         * @return item builder
         */
        public ItemBuilder duration(String key) {
            return item(key, ConfigType.DURATION);
        }

        /**
         * Starts a secret item declaration.
         *
         * @param key plugin-local configuration key
         * @return item builder
         */
        public ItemBuilder secret(String key) {
            return item(key, ConfigType.SECRET).secret();
        }

        /** Builds the immutable schema in declaration order. */
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

    /** Builder for one item that returns to its owning schema builder through {@link #end()}. */
    final class ItemBuilder {

        private final Builder parent;
        private final String key;
        private final ConfigType type;
        private boolean required;
        private String defaultValue;
        private boolean secret;
        private String description;

        private ItemBuilder(Builder parent, String key, ConfigType type) {
            this.parent = parent;
            this.key = key;
            this.type = type;
        }

        /**
         * Marks the item as required.
         *
         * @return this item builder
         */
        public ItemBuilder required() {
            this.required = true;
            return this;
        }

        /**
         * Sets a textual default converted during configuration resolution.
         *
         * @param defaultValue textual default value
         * @return this item builder
         */
        public ItemBuilder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Marks diagnostics for this item as secret.
         *
         * @return this item builder
         */
        public ItemBuilder secret() {
            this.secret = true;
            return this;
        }

        /**
         * Sets a human-readable item description.
         *
         * @param description display description
         * @return this item builder
         */
        public ItemBuilder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Finishes this item and returns to the schema builder.
         *
         * @return owning schema builder
         */
        public Builder end() {
            parent.add(new ConfigItemDefinition(
                    key,
                    type,
                    required,
                    defaultValue,
                    secret,
                    description));
            return parent;
        }
    }
}

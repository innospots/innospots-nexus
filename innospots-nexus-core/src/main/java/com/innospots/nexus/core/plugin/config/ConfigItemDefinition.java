package com.innospots.nexus.core.plugin.config;

import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Describes one plugin-local configuration key.
 *
 * @param key plugin-local key
 * @param type value type
 * @param required whether a value is mandatory
 * @param defaultValue optional textual default
 * @param secret whether diagnostics must mask the value
 * @param description human-readable description
 */
public record ConfigItemDefinition(
        String key,
        ConfigType type,
        boolean required,
        String defaultValue,
        boolean secret,
        String description
) {

    private static final Pattern KEY_PATTERN = Pattern.compile(
            "[a-z][a-zA-Z0-9]*(?:\\.[a-z][a-zA-Z0-9]*)*");

    /** Validates a plugin-local configuration item. */
    public ConfigItemDefinition {
        if (key == null || !KEY_PATTERN.matcher(key).matches() || type == null) {
            invalid("invalid plugin config key or type: " + key);
        }
        if ((secret || type == ConfigType.SECRET) && defaultValue != null) {
            invalid("secret config cannot declare a default value: " + key);
        }
        if (type == ConfigType.SECRET && !secret) {
            secret = true;
        }
        description = description == null ? "" : description;
    }

    private static void invalid(String message) {
        throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, message);
    }
}

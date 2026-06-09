package com.innospots.nexus.kernel.config;

/**
 * System configuration entity.
 *
 * @param configId    configuration identifier
 * @param configKey   stable configuration key
 * @param configValue configuration value
 * @param valueType   value type
 * @param description configuration description
 */
public record SystemConfig(
        String configId,
        String configKey,
        String configValue,
        ConfigValueType valueType,
        String description
) {
}

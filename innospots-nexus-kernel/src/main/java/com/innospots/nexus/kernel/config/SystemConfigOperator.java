package com.innospots.nexus.kernel.config;

import java.util.List;
import java.util.Optional;

/**
 * Port for system configuration operations.
 */
public interface SystemConfigOperator {

    /**
     * Finds a configuration value by key.
     *
     * @param configKey configuration key
     * @return configuration when found
     */
    Optional<SystemConfig> findConfig(String configKey);

    /**
     * Lists all system configurations.
     *
     * @return system configurations
     */
    List<SystemConfig> listConfigs();

    /**
     * Saves a system configuration.
     *
     * @param config system configuration
     * @return saved configuration
     */
    SystemConfig saveConfig(SystemConfig config);
}

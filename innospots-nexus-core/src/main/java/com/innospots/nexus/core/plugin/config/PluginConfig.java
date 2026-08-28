package com.innospots.nexus.core.plugin.config;

import java.time.Duration;
import java.util.Optional;

/**
 * Immutable validated configuration view limited to one plugin namespace.
 */
public interface PluginConfig {

    /** Finds a textual value. */
    Optional<String> get(String key);

    /** Returns a required textual value. */
    String require(String key);

    /** Returns an integer value or the caller default. */
    int getInt(String key, int defaultValue);

    /** Returns a long value or the caller default. */
    long getLong(String key, long defaultValue);

    /** Returns a boolean value or the caller default. */
    boolean getBoolean(String key, boolean defaultValue);

    /** Returns a duration value or the caller default. */
    Duration getDuration(String key, Duration defaultValue);

    /** Returns a required masked secret value. */
    SecretValue requireSecret(String key);
}

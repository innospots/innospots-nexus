package com.innospots.nexus.core.plugin.config;

import java.util.Map;

/**
 * Bootstrap configuration source ordered from lower to higher precedence.
 */
public interface ConfigSource {

    /** Returns a diagnostic source name. */
    String name();

    /** Returns immutable raw key-value pairs. */
    Map<String, String> values();
}

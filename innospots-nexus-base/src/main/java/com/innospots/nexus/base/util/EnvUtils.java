package com.innospots.nexus.base.util;

import java.util.Map;
import java.util.Properties;

/**
 * Environment property resolver with override support. Looks up values
 * in this order: overrides (programmatic) → system properties → environment
 * variables.
 */
public final class EnvUtils {

    private static final Properties OVERRIDES = new Properties();

    private EnvUtils() {
    }

    public static String value(String key) {
        return value(key, null);
    }

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

    public static void set(String key, String value) {
        if (value == null) {
            clear(key);
        } else {
            OVERRIDES.setProperty(key, value);
        }
    }

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

    public static void clear(String key) {
        OVERRIDES.remove(key);
    }
}

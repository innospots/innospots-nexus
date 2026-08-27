package com.innospots.nexus.base.ui.spec.config;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * Immutable configuration for locating and parsing page specifications.
 *
 * @param basePath classpath base directory
 * @param fileSuffix YAML file suffix, {@code .yaml} or {@code .yml}
 * @param failOnUnknownProperties whether unknown fields fail parsing
 */
public record UiSpecConfig(
        String basePath,
        String fileSuffix,
        boolean failOnUnknownProperties
) {

    /** Creates a validated configuration. */
    public UiSpecConfig {
        basePath = normalizeBasePath(basePath);
        if (!".yaml".equals(fileSuffix) && !".yml".equals(fileSuffix)) {
            invalid("UiSpec fileSuffix must be '.yaml' or '.yml'");
        }
    }

    /** Returns the strict default YAML configuration. */
    public static UiSpecConfig defaults() {
        return new UiSpecConfig("ui-spec", ".yaml", true);
    }

    /** Builds the classpath resource path for one module page. */
    public String resourcePath(String moduleKey, String pageKey) {
        requireSegment(moduleKey, "moduleKey");
        requireSegment(pageKey, "pageKey");
        return basePath + "/" + moduleKey + "/" + pageKey + fileSuffix;
    }

    private static String normalizeBasePath(String basePath) {
        if (basePath == null || basePath.isBlank()) {
            invalid("UiSpec basePath is required");
        }
        String normalized = basePath.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank() || normalized.contains("..")) {
            invalid("UiSpec basePath is invalid");
        }
        return normalized;
    }

    private static void requireSegment(String value, String field) {
        if (value == null || !value.matches("[A-Za-z0-9][A-Za-z0-9._-]*")) {
            invalid("UiSpec " + field + " is invalid");
        }
    }

    private static void invalid(String message) {
        throw NexusException.build(NexusStatusCode.CONFIG_ERROR.fullCode(), message);
    }
}

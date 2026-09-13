package com.innospots.nexus.core.plugin.contribution.console.ui.spec.config;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * Immutable configuration for locating and parsing Pactor page DSL files.
 *
 * <p>Default resources live under {@code ui-pages/{moduleKey}/{pageKey}.yaml}.</p>
 *
 * @param basePath classpath base directory
 * @param fileSuffix page file suffix, typically {@code .yaml}
 * @param failOnUnknownProperties whether unknown YAML fields fail parsing
 */
public record PageDslConfig(
        String basePath,
        String fileSuffix,
        boolean failOnUnknownProperties
) {

    /** Creates a validated configuration. */
    public PageDslConfig {
        basePath = normalizeBasePath(basePath);
        if (!".yaml".equals(fileSuffix) && !".yml".equals(fileSuffix)) {
            invalid("PageDsl fileSuffix must be '.yaml' or '.yml'");
        }
    }

    /** Returns the strict default configuration for {@code *.yaml} resources. */
    public static PageDslConfig defaults() {
        return new PageDslConfig("ui-pages", ".yaml", true);
    }

    /**
     * Builds the classpath resource path for one module page.
     *
     * @param moduleKey owning module key
     * @param pageKey page key matching {@code page.id}
     * @return classpath resource path
     */
    public String resourcePath(String moduleKey, String pageKey) {
        requireSegment(moduleKey, "moduleKey");
        requireSegment(pageKey, "pageKey");
        return basePath + "/" + moduleKey + "/" + pageKey + fileSuffix;
    }

    private static String normalizeBasePath(String basePath) {
        if (basePath == null || basePath.isBlank()) {
            invalid("PageDsl basePath is required");
        }
        String normalized = basePath.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank() || normalized.contains("..")) {
            invalid("PageDsl basePath is invalid");
        }
        return normalized;
    }

    private static void requireSegment(String value, String field) {
        if (value == null || !value.matches("[A-Za-z0-9][A-Za-z0-9._-]*")) {
            invalid("PageDsl " + field + " is invalid");
        }
    }

    private static void invalid(String message) {
        throw NexusException.build(NexusStatusCode.CONFIG_ERROR.fullCode(), message);
    }
}

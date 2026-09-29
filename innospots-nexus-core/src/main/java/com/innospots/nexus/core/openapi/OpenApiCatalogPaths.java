package com.innospots.nexus.core.openapi;

/**
 * OpenAPI 规范目录与 Scalar 文档页的 HTTP 路径常量。
 */
public final class OpenApiCatalogPaths {

    public static final String SPECS_BASE = "/openapi/specs";

    public static final String SPECS_ITEM_PREFIX = SPECS_BASE + "/";

    public static final String UI_DEFAULT = "/openapi/ui";

    /** Scalar 文档页挂载的前端脚本文件名（相对 {@link #UI_DEFAULT}）。 */
    public static final String SCALAR_JS_SEGMENT = "scalar.js";

    private OpenApiCatalogPaths() {
    }

    /**
     * 规范化 OpenAPI 规范目录根路径（无前导 {@code /} 时补齐，并去掉末尾 {@code /}）。
     */
    public static String normalizeSpecsBase(String specsBase) {
        if (specsBase == null || specsBase.isBlank()) {
            return SPECS_BASE;
        }
        String normalized = specsBase.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * 单个规范 YAML 的 HTTP 前缀，形如 {@code /openapi/specs/}。
     */
    public static String specItemUrlPrefix(String specsBase) {
        return normalizeSpecsBase(specsBase) + "/";
    }

    /**
     * Scalar 文档页对应的 {@code scalar.js} 绝对路径。
     */
    public static String scalarJavascriptPath(String documentationPath, String scriptSegment) {
        String docsPath = normalizeDocumentationPath(documentationPath);
        String segment = scriptSegment;
        if (segment == null || segment.isBlank()) {
            segment = SCALAR_JS_SEGMENT;
        } else {
            segment = segment.trim();
            while (segment.startsWith("/")) {
                segment = segment.substring(1);
            }
        }
        return docsPath + "/" + segment;
    }

    /**
     * 规范化 Scalar HTML 文档页路径（无前导 {@code /} 时补齐）。
     */
    public static String normalizeDocumentationPath(String path) {
        if (path == null || path.isBlank()) {
            return UI_DEFAULT;
        }
        String normalized = path.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}

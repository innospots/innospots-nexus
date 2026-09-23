package com.innospots.nexus.core.plugin.contribution.console.ui.spec.config;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * 定位与解析 Pactor 页面 DSL 文件的不可变配置。
 *
 * <p>默认资源位于 {@code ui-pages/{moduleKey}/{pageKey}.yaml}。</p>
 *
 * @param basePath classpath 基础目录
 * @param fileSuffix 页面文件后缀，通常为 {@code .yaml}
 * @param failOnUnknownProperties 未知 YAML 字段是否导致解析失败
 * @author Smars
 * @date 2026/09/13
 */
public record PageDslConfig(
        String basePath,
        String fileSuffix,
        boolean failOnUnknownProperties
) {

    /** 创建并校验配置。 */
    public PageDslConfig {
        basePath = normalizeBasePath(basePath);
        if (!".yaml".equals(fileSuffix) && !".yml".equals(fileSuffix)) {
            invalid("PageDsl fileSuffix must be '.yaml' or '.yml'");
        }
    }

    /**
     * 返回 {@code *.yaml} 资源的严格默认配置。
     *
     * @return 默认配置
     */
    public static PageDslConfig defaults() {
        return new PageDslConfig("ui-pages", ".yaml", true);
    }

    /**
     * 构建一个模块页面的 classpath 资源路径。
     *
     * @param moduleKey 所属模块键
     * @param pageKey 与 {@code page.id} 匹配的页面键
     * @return classpath 资源路径
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

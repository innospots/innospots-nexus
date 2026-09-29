package com.innospots.nexus.console.ui.spec;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * 页面 DSL 复合 {@code pageKey}：{@code {domainKey}-{moduleKey}-{xxx}}。
 *
 * <p>仅有两个 {@code -} 作为分隔符（{@code split("-", 3)}）：domain 与 module 名称本身禁止含 {@code -}；
 * {@code xxx} 为页面后缀，可含连字符。classpath 文件：
 * {@code ui-pages/{domainKey}/{moduleKey}/{pageKey}.yaml}，其中 {@code pageKey} 为完整复合键。</p>
 */
public final class PageDslPageRef {

    private final String pageKey;
    private final String domainKey;
    private final String moduleKey;
    private final String pageSuffix;

    private PageDslPageRef(String pageKey, String domainKey, String moduleKey, String pageSuffix) {
        this.pageKey = pageKey;
        this.domainKey = domainKey;
        this.moduleKey = moduleKey;
        this.pageSuffix = pageSuffix;
    }

    /**
     * 由 domain、module 与页面后缀构建完整 {@code pageKey}。
     */
    public static String encode(String domainKey, String moduleKey, String pageSuffix) {
        requireLocatorSegment(domainKey, "domainKey");
        requireLocatorSegment(moduleKey, "moduleKey");
        requireSegment(pageSuffix, "pageSuffix");
        return domainKey + "-" + moduleKey + "-" + pageSuffix;
    }

    /**
     * 解析完整 {@code pageKey}。
     */
    public static PageDslPageRef decode(String pageKey) {
        if (pageKey == null || pageKey.isBlank()) {
            invalid("PageDsl pageKey is required");
        }
        String normalized = pageKey.trim();
        String[] parts = normalized.split("-", 3);
        if (parts.length < 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            invalid("PageDsl pageKey must be {domainKey}-{moduleKey}-{xxx}: " + pageKey);
        }
        requireLocatorSegment(parts[0], "domainKey");
        requireLocatorSegment(parts[1], "moduleKey");
        requireSegment(parts[2], "pageSuffix");
        return new PageDslPageRef(normalized, parts[0], parts[1], parts[2]);
    }

    public String pageKey() {
        return pageKey;
    }

    public String domainKey() {
        return domainKey;
    }

    public String moduleKey() {
        return moduleKey;
    }

    /**
     * {@code pageKey} 中 module 之后的后缀段（可含连字符）。
     */
    public String pageSuffix() {
        return pageSuffix;
    }

    private static void requireLocatorSegment(String value, String field) {
        if (value == null || !value.matches("[A-Za-z0-9][A-Za-z0-9._]*")) {
            invalid("PageDsl " + field + " is invalid");
        }
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

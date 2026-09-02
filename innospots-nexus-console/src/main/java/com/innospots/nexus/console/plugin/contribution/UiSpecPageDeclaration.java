package com.innospots.nexus.console.plugin.contribution;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** 由 UiSpec pageInfo.pageId 唯一对应的页面身份声明。 */
public record UiSpecPageDeclaration(
        String pageKey,
        String pagePath,
        List<UiSpecPageDeclaration> children
) {

    private static final Pattern KEY_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:-[a-z0-9]+)*");

    /** 校验页面身份、路径模板并复制子页面列表。 */
    public UiSpecPageDeclaration {
        if (pageKey == null || pageKey.length() > 128 || !KEY_PATTERN.matcher(pageKey).matches()) {
            invalid("invalid pageKey: " + pageKey);
        }
        requireText(pagePath, "pagePath");
        if (pagePath.length() > 2048 || !pagePath.startsWith("/")
                || pagePath.contains("?") || pagePath.contains("#")) {
            invalid("pagePath must be an absolute path without query or fragment");
        }
        pagePath = normalize(pagePath);
        validatePath(pagePath);
        children = children == null ? List.of() : List.copyOf(children);
    }

    /** 返回模块内稳定页面资源身份。 */
    public String resourceKey(String moduleKey) {
        requireText(moduleKey, "moduleKey");
        return "page:" + moduleKey + "." + pageKey;
    }

    /** 返回页面是否含有不能由静态菜单提供值的路径变量。 */
    public boolean hasRequiredPathVariables() {
        return pagePath.matches(".*\\{[^}]+}.*");
    }

    /** 返回用于全局路由冲突检查的路径模板。 */
    public String normalizedRouteTemplate() {
        return pagePath.replaceAll("\\{[^}]+}", "{}");
    }

    private static void validatePath(String path) {
        Set<String> names = new HashSet<>();
        String value = path.length() == 1 ? "" : path.substring(1);
        for (String segment : value.split("/", -1)) {
            String decoded = decode(segment);
            if (".".equals(decoded) || "..".equals(decoded)
                    || decoded.indexOf('/') >= 0 || decoded.indexOf('\\') >= 0) {
                invalid("pagePath must not contain traversal segments");
            }
            boolean opening = segment.indexOf('{') >= 0;
            boolean closing = segment.indexOf('}') >= 0;
            if (!opening && !closing) {
                continue;
            }
            if (!segment.matches("\\{[A-Za-z][A-Za-z0-9_]*}")) {
                invalid("pagePath variables must occupy complete path segments");
            }
            String name = segment.substring(1, segment.length() - 1);
            if (!names.add(name)) {
                invalid("pagePath variable names must be unique");
            }
        }
    }

    private static String normalize(String path) {
        String normalized = path.replaceAll("/{2,}", "/");
        return normalized.length() > 1 && normalized.endsWith("/")
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }

    private static String decode(String segment) {
        try {
            return URLDecoder.decode(segment, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            invalid("pagePath contains an invalid percent escape");
            return segment;
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            invalid(field + " is required");
        }
    }

    private static void invalid(String message) {
        throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT, message);
    }
}

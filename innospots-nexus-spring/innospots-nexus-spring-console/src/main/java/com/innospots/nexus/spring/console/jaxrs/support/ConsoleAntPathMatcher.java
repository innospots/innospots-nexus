package com.innospots.nexus.spring.console.jaxrs.support;

/**
 * 简单 Ant 风格路径匹配（{@code *}、{@code **}）。
 */
public final class ConsoleAntPathMatcher {

    private ConsoleAntPathMatcher() {
    }

    public static boolean matches(String pattern, String path) {
        if (pattern == null || path == null) {
            return false;
        }
        String normalizedPattern = normalize(pattern);
        String normalizedPath = normalize(path);
        if ("**".equals(normalizedPattern) || normalizedPattern.equals(normalizedPath)) {
            return true;
        }
        if (normalizedPattern.endsWith("/**")) {
            String prefix = normalizedPattern.substring(0, normalizedPattern.length() - 3);
            return normalizedPath.equals(prefix) || normalizedPath.startsWith(prefix + "/");
        }
        return matchSegments(normalizedPattern.split("/"), normalizedPath.split("/"));
    }

    public static boolean matchesAny(java.util.Collection<String> patterns, String path) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (matches(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchSegments(String[] patternParts, String[] pathParts) {
        return matchSegments(patternParts, 0, pathParts, 0);
    }

    private static boolean matchSegments(
            String[] patternParts,
            int patternIndex,
            String[] pathParts,
            int pathIndex) {
        if (patternIndex >= patternParts.length) {
            return pathIndex >= pathParts.length;
        }
        String patternPart = patternParts[patternIndex];
        if ("**".equals(patternPart)) {
            if (patternIndex == patternParts.length - 1) {
                return true;
            }
            for (int i = pathIndex; i <= pathParts.length; i++) {
                if (matchSegments(patternParts, patternIndex + 1, pathParts, i)) {
                    return true;
                }
            }
            return false;
        }
        if (pathIndex >= pathParts.length) {
            return false;
        }
        if (!"*".equals(patternPart) && !patternPart.equals(pathParts[pathIndex])) {
            return false;
        }
        return matchSegments(patternParts, patternIndex + 1, pathParts, pathIndex + 1);
    }

    private static String normalize(String value) {
        if (value.isEmpty()) {
            return "/";
        }
        String trimmed = value.trim();
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        if (trimmed.length() > 1 && trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}

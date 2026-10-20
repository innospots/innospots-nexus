package com.innospots.nexus.console.endpoint.grant.support;

import com.innospots.nexus.console.jaxrs.support.ConsoleAntPathMatcher;

/**
 * 将 JAX-RS 路径模板与请求路径匹配（{@code {param}} 视为单段通配）。
 */
public final class EndpointGrantPathMatcher {

    private EndpointGrantPathMatcher() {
    }

    /**
     * 判断请求路径是否匹配模板。
     *
     * @param pathTemplate JAX-RS 路径模板
     * @param requestPath  实际请求路径（可含或不含前导 {@code /}）
     * @return 是否匹配
     */
    public static boolean matches(String pathTemplate, String requestPath) {
        if (pathTemplate == null || pathTemplate.isBlank()) {
            return false;
        }
        if (requestPath == null || requestPath.isBlank()) {
            return false;
        }
        String antPattern = toAntPattern(pathTemplate);
        return ConsoleAntPathMatcher.matches(antPattern, requestPath);
    }

    private static String toAntPattern(String jaxRsTemplate) {
        String normalized = jaxRsTemplate.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.replaceAll("\\{[^{}/]+\\}", "*");
    }
}

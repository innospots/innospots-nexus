package com.innospots.nexus.spring.console.jaxrs.support;

import com.innospots.nexus.console.config.ConsoleConstant;

/**
 * {@link ConsoleConstant#PUBLIC_API_PREFIX} 路径匹配（Jersey 请求路径）。
 */
public final class ConsolePublicApiPaths {

    private static final String PATTERN = ConsoleConstant.PUBLIC_API_PREFIX + "/**";

    private ConsolePublicApiPaths() {
    }

    /**
     * 是否为公共开放 API 路径。
     *
     * @param normalizedPath 以 {@code /} 开头的请求路径
     */
    public static boolean matches(String normalizedPath) {
        return ConsoleAntPathMatcher.matches(PATTERN, normalizedPath);
    }
}

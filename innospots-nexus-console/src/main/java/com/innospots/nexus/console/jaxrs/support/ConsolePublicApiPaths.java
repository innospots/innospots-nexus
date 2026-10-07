package com.innospots.nexus.console.jaxrs.support;

import com.innospots.nexus.console.config.ConsoleConstant;

/**
 * 公共开放 API 路径匹配（Jersey 请求路径）。
 *
 * <ul>
 *   <li>{@link ConsoleConstant#PUBLIC_API_PREFIX} 及其子路径（如 {@code /api/public/pages/...}）</li>
 *   <li>{@code /api/d/{domain}/public} 及其子路径，{@code domain} 为领域键（如 {@code nexus}、{@code platform}、{@code nexmux}）</li>
 * </ul>
 */
public final class ConsolePublicApiPaths {

    /** {@link ConsoleConstant#PUBLIC_API_PREFIX} 及其子路径。 */
    public static final String LEGACY_PUBLIC_PATTERN = ConsoleConstant.PUBLIC_API_PREFIX + "/**";

    /** {@code /api/d/{domain}/public} 及其子路径。 */
    public static final String DOMAIN_PUBLIC_PATTERN = "/api/d/*/public/**";

    private ConsolePublicApiPaths() {
    }

    /**
     * 是否为公共开放 API 路径。
     *
     * @param normalizedPath 以 {@code /} 开头的请求路径
     */
    public static boolean matches(String normalizedPath) {
        return ConsoleAntPathMatcher.matches(LEGACY_PUBLIC_PATTERN, normalizedPath)
                || ConsoleAntPathMatcher.matches(DOMAIN_PUBLIC_PATTERN, normalizedPath);
    }
}

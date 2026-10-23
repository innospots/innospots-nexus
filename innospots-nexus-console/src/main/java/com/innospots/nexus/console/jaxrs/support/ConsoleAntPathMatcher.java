package com.innospots.nexus.console.jaxrs.support;

import java.util.Collection;

import com.innospots.nexus.core.endpoint.grant.support.AntPathMatcher;

/**
 * 简单 Ant 风格路径匹配（{@code *}、{@code **}）；委托 {@link AntPathMatcher}。
 */
public final class ConsoleAntPathMatcher {

    private ConsoleAntPathMatcher() {
    }

    public static boolean matches(String pattern, String path) {
        return AntPathMatcher.matches(pattern, path);
    }

    public static boolean matchesAny(Collection<String> patterns, String path) {
        return AntPathMatcher.matchesAny(patterns, path);
    }
}

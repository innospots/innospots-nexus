package com.innospots.nexus.console.jaxrs.support;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleAntPathMatcherTest {

    @Test
    void rejectsNullPatternOrPath() {
        assertThat(ConsoleAntPathMatcher.matches(null, "/health")).isFalse();
        assertThat(ConsoleAntPathMatcher.matches("/health", null)).isFalse();
        assertThat(ConsoleAntPathMatcher.matches(null, null)).isFalse();
    }

    @Test
    void matchesExactPathAndNormalizesInput() {
        assertThat(ConsoleAntPathMatcher.matches("/health", "/health")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/health", "health")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/health/", "/health")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/health", "/health/extra")).isFalse();
    }

    @Test
    void matchesSimplePrefixGlobWithoutWildcardInPrefix() {
        assertThat(ConsoleAntPathMatcher.matches("/openapi/**", "/openapi")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/openapi/**", "/openapi/specs")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/openapi/**", "/openapi/ui/scalar.js")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/openapi/**", "/api/openapi/specs")).isFalse();
        assertThat(ConsoleAntPathMatcher.matches("/openapi/**", "/openapifoo")).isFalse();
    }

    @Test
    void matchesSingleSegmentWildcard() {
        assertThat(ConsoleAntPathMatcher.matches("/api/d/*/public/**", "/api/d/nexus/public/status"))
                .isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/api/d/*/public/**", "/api/d/nexus/public")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/api/d/*/public/**", "/api/d/nexus/users")).isFalse();
        assertThat(ConsoleAntPathMatcher.matches("/api/d/*/auth/refresh", "/api/d/platform/auth/refresh"))
                .isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/api/d/*/auth/refresh", "/api/d/platform/auth/logout"))
                .isFalse();
        assertThat(ConsoleAntPathMatcher.matches("/api/d/*/auth/refresh", "/api/d/platform/public/refresh"))
                .isFalse();
    }

    @Test
    void singleSegmentWildcardDoesNotMatchExtraPathSegments() {
        assertThat(ConsoleAntPathMatcher.matches("/api/d/*/public/**", "/api/d/nexus/publication/list"))
                .isFalse();
        assertThat(ConsoleAntPathMatcher.matches("/api/d/*/public/**", "/api/d/nexus/not-public/x"))
                .isFalse();
    }

    @Test
    void matchesDatasourceConsoleProxyPrefix() {
        assertThat(ConsoleAntPathMatcher.matches("/console/datasource/**", "/console/datasource/orders"))
                .isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/console/datasource/**", "/console/datasource"))
                .isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/console/datasource/**", "/console/other/orders"))
                .isFalse();
    }

    @Test
    void matchesActuatorHealthSubtree() {
        assertThat(ConsoleAntPathMatcher.matches("/actuator/health/**", "/actuator/health")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/actuator/health/**", "/actuator/health/liveness"))
                .isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/actuator/health/**", "/actuator/info")).isFalse();
    }

    @Test
    void matchesAnyReturnsFalseForEmptyOrNullPatterns() {
        assertThat(ConsoleAntPathMatcher.matchesAny(null, "/health")).isFalse();
        assertThat(ConsoleAntPathMatcher.matchesAny(List.of(), "/health")).isFalse();
        assertThat(ConsoleAntPathMatcher.matchesAny(List.of("/health"), "/metrics")).isFalse();
        assertThat(ConsoleAntPathMatcher.matchesAny(List.of("/health", "/openapi/**"), "/openapi/ui"))
                .isTrue();
    }
}

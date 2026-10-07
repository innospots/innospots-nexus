package com.innospots.nexus.console.jaxrs.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsolePermitAllPathsTest {

    @Test
    void publicApiPatternsAliasConsolePublicApiPaths() {
        assertThat(ConsolePermitAllPaths.PUBLIC_API_LEGACY)
                .isEqualTo(ConsolePublicApiPaths.LEGACY_PUBLIC_PATTERN);
        assertThat(ConsolePermitAllPaths.PUBLIC_API_DOMAIN)
                .isEqualTo(ConsolePublicApiPaths.DOMAIN_PUBLIC_PATTERN);
    }

    @Test
    void defaultPatternsListIsMutableCopy() {
        assertThat(ConsolePermitAllPaths.defaultPatterns()).containsExactly(
                ConsolePermitAllPaths.ROOT,
                ConsolePermitAllPaths.OPENAPI,
                ConsolePermitAllPaths.PUBLIC_API_LEGACY,
                ConsolePermitAllPaths.PUBLIC_API_DOMAIN,
                ConsolePermitAllPaths.HEALTH,
                ConsolePermitAllPaths.ACTUATOR_HEALTH);

        ConsolePermitAllPaths.defaultPatterns().add("/custom/**");
        assertThat(ConsolePermitAllPaths.defaultPatterns()).doesNotContain("/custom/**");
    }

    @Test
    void defaultPatternsPermitOpenApiAndPublicApi() {
        assertThat(matchesDefault("/openapi")).isTrue();
        assertThat(matchesDefault("/openapi/specs")).isTrue();
        assertThat(matchesDefault("/api/public/pages/demo")).isTrue();
        assertThat(matchesDefault("/api/d/platform/public/auth/login")).isTrue();
        assertThat(matchesDefault("/api/d/nexmux/public/health")).isTrue();
    }

    @Test
    void defaultPatternsPermitHealthEndpoints() {
        assertThat(matchesDefault("/health")).isTrue();
        assertThat(matchesDefault("/actuator/health")).isTrue();
        assertThat(matchesDefault("/actuator/health/readiness")).isTrue();
    }

    @Test
    void defaultPatternsRejectSecuredBusinessPaths() {
        assertThat(matchesDefault("/api/d/nexus/status")).isFalse();
        assertThat(matchesDefault("/api/d/platform/auth/refresh")).isFalse();
        assertThat(matchesDefault("/api/d/platform/auth/logout")).isFalse();
        assertThat(matchesDefault("/tenant/auth/login")).isFalse();
        assertThat(matchesDefault("/console/datasource/orders")).isFalse();
    }

    @Test
    void eachDefaultPatternMatchesExpectedPathsOnly() {
        assertThat(ConsoleAntPathMatcher.matches(ConsolePermitAllPaths.OPENAPI, "/openapi/ping")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches(ConsolePermitAllPaths.OPENAPI, "/api/openapi/ping")).isFalse();

        assertThat(ConsoleAntPathMatcher.matches(ConsolePermitAllPaths.PUBLIC_API_LEGACY, "/api/public"))
                .isTrue();
        assertThat(ConsoleAntPathMatcher.matches(ConsolePermitAllPaths.PUBLIC_API_LEGACY, "/api/private"))
                .isFalse();

        assertThat(ConsoleAntPathMatcher.matches(
                ConsolePermitAllPaths.PUBLIC_API_DOMAIN, "/api/d/nexus/public/x"))
                .isTrue();
        assertThat(ConsoleAntPathMatcher.matches(
                ConsolePermitAllPaths.PUBLIC_API_DOMAIN, "/api/d/nexus/private/x"))
                .isFalse();

        assertThat(ConsoleAntPathMatcher.matches(ConsolePermitAllPaths.HEALTH, "/health")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches(ConsolePermitAllPaths.HEALTH, "/healthz")).isFalse();

        assertThat(ConsoleAntPathMatcher.matches(ConsolePermitAllPaths.ACTUATOR_HEALTH, "/actuator/health"))
                .isTrue();
        assertThat(ConsoleAntPathMatcher.matches(ConsolePermitAllPaths.ACTUATOR_HEALTH, "/actuator/metrics"))
                .isFalse();
    }

    private static boolean matchesDefault(String path) {
        return ConsoleAntPathMatcher.matchesAny(ConsolePermitAllPaths.defaultPatterns(), path);
    }
}

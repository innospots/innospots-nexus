package com.innospots.nexus.console.jaxrs.support;

import com.innospots.nexus.console.config.ConsoleConstant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsolePublicApiPathsTest {

    @Test
    void exposesPatternsAlignedWithConsoleConstant() {
        assertThat(ConsolePublicApiPaths.LEGACY_PUBLIC_PATTERN)
                .isEqualTo(ConsoleConstant.PUBLIC_API_PREFIX + "/**");
        assertThat(ConsolePublicApiPaths.DOMAIN_PUBLIC_PATTERN).isEqualTo("/api/d/*/public/**");
    }

    @Test
    void matchesLegacyPublicApiPrefixAndDescendants() {
        assertThat(ConsolePublicApiPaths.matches("/api/public")).isTrue();
        assertThat(ConsolePublicApiPaths.matches("/api/public/pages/nexus-menu-main")).isTrue();
        assertThat(ConsolePublicApiPaths.matches("/api/public/ui/sitemap/nexus")).isTrue();
        assertThat(ConsolePublicApiPaths.matches("api/public/pages/foo")).isTrue();
    }

    @Test
    void matchesDomainScopedPublicPaths() {
        assertThat(ConsolePublicApiPaths.matches("/api/d/nexus/public/status")).isTrue();
        assertThat(ConsolePublicApiPaths.matches("/api/d/platform/public/registration/open")).isTrue();
        assertThat(ConsolePublicApiPaths.matches("/api/d/nexmux/public/health")).isTrue();
        assertThat(ConsolePublicApiPaths.matches("/api/d/platform/public")).isTrue();
    }

    @Test
    void rejectsNonPublicAndMisplacedPublicSegment() {
        assertThat(ConsolePublicApiPaths.matches("/api/nexus/ui/pages/nexus/foo/bar")).isFalse();
        assertThat(ConsolePublicApiPaths.matches("/api/d/nexus/users")).isFalse();
        assertThat(ConsolePublicApiPaths.matches("/api/d/nexus/private/public")).isFalse();
        assertThat(ConsolePublicApiPaths.matches("/api/d/nexus/public-extra")).isFalse();
        assertThat(ConsolePublicApiPaths.matches("/api/d/platform/auth/refresh")).isFalse();
        assertThat(ConsolePublicApiPaths.matches("/openapi/specs")).isFalse();
    }

    @Test
    void legacyAndDomainMatchersAgreeWithAntPatterns() {
        assertThat(ConsoleAntPathMatcher.matches(
                ConsolePublicApiPaths.LEGACY_PUBLIC_PATTERN, "/api/public/pages/x"))
                .isTrue();
        assertThat(ConsoleAntPathMatcher.matches(
                ConsolePublicApiPaths.DOMAIN_PUBLIC_PATTERN, "/api/d/platform/public/auth/login"))
                .isTrue();
        assertThat(ConsolePublicApiPaths.matches("/api/public/pages/x")).isTrue();
        assertThat(ConsolePublicApiPaths.matches("/api/d/platform/public/auth/login")).isTrue();
    }
}

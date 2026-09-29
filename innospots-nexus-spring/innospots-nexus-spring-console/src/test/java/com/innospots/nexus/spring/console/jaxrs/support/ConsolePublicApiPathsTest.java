package com.innospots.nexus.spring.console.jaxrs.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsolePublicApiPathsTest {

    @Test
    void matchesPublicUiPaths() {
        assertThat(ConsolePublicApiPaths.matches("/api/public/ui/pages/nexus/foo/bar")).isTrue();
        assertThat(ConsolePublicApiPaths.matches("/api/public/ui/sitemap/nexus")).isTrue();
        assertThat(ConsolePublicApiPaths.matches("/api/nexus/ui/pages/nexus/foo/bar")).isFalse();
    }
}

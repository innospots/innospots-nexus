package com.innospots.nexus.spring.console.jaxrs.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleAntPathMatcherTest {

    @Test
    void matchesOpenApiPrefix() {
        assertThat(ConsoleAntPathMatcher.matches("/openapi/**", "/openapi/specs")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/openapi/**", "/openapi/ui/scalar.js")).isTrue();
        assertThat(ConsoleAntPathMatcher.matches("/openapi/**", "/api/openapi/specs")).isFalse();
    }

    @Test
    void matchesDatasourcePrefix() {
        assertThat(ConsoleAntPathMatcher.matches("/console/datasource/**", "/console/datasource/orders"))
                .isTrue();
    }
}

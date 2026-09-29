package com.innospots.nexus.console.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleConstantTest {

    @Test
    void publicPathsMatchOpenApiPrefix() {
        assertThat(ConsoleConstant.PUBLIC_API_PREFIX).isEqualTo("/api/public");
        assertThat(ConsoleConstant.publicPath("/health")).isEqualTo("/api/public/health");
        assertThat(ConsoleConstant.publicPath("status")).isEqualTo("/api/public/status");
        assertThat(ConsoleConstant.publicPath("")).isEqualTo("/api/public");
    }
}

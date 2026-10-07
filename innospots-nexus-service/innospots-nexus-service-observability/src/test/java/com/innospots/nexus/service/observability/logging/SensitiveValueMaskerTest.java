package com.innospots.nexus.service.observability.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 敏感值默认掩码测试。
 */
class SensitiveValueMaskerTest {

    private final SensitiveValueMasker masker = new SensitiveValueMasker();

    @Test
    void masksAuthorizationHeader() {
        assertThat(masker.maskHeader("Authorization", "Bearer secret-token")).isEqualTo("***");
    }

    @Test
    void masksCookieHeader() {
        assertThat(masker.maskHeader("Cookie", "session=abc123")).isEqualTo("***");
    }

    @Test
    void masksJwtValues() {
        assertThat(masker.mask("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.signature")).isEqualTo("***");
    }

    @Test
    void masksApiKeyValues() {
        assertThat(masker.mask("sk_live_1234567890abcdef")).isEqualTo("***");
    }
}

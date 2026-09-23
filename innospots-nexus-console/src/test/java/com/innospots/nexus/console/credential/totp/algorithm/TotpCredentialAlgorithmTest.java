package com.innospots.nexus.console.credential.totp.algorithm;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TotpCredentialAlgorithmTest {

    @Test
    void matchesRfc6238Sha1TestVector() {
        TotpCredentialAlgorithm algorithm = new TotpCredentialAlgorithm();
        byte[] secret = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
        Instant instant = Instant.ofEpochSecond(59);
        assertThat(algorithm.generateAt(secret, instant)).isEqualTo("94287082");
        assertThat(algorithm.verify(secret, "94287082", instant)).isTrue();
        assertThat(algorithm.verify(secret, "00000000", instant)).isFalse();
    }
}

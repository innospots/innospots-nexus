package com.innospots.nexus.console.credential.password.algorithm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptCredentialAlgorithmTest {

    @Test
    void encodesAndVerifiesWithoutExposingPlaintextInVerifier() {
        BcryptCredentialAlgorithm algorithm = new BcryptCredentialAlgorithm();
        EncodedCredential encoded = algorithm.encode("nexus-secret");

        assertThat(encoded.algorithm()).isEqualTo(CredentialAlgorithms.BCRYPT_V1);
        assertThat(encoded.verifier()).isNotEqualTo("nexus-secret");
        assertThat(algorithm.verify(encoded.verifier(), encoded.verifierParams(), "nexus-secret")).isTrue();
        assertThat(algorithm.verify(encoded.verifier(), encoded.verifierParams(), "wrong")).isFalse();
    }
}

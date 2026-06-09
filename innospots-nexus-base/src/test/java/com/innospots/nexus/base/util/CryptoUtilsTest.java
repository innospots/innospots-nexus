package com.innospots.nexus.base.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoUtilsTest {

    @Test
    void createsSha256HexDigest() {
        String digest = CryptoUtils.sha256Hex("innospots");

        assertThat(digest).hasSize(64);
        assertThat(digest).isEqualTo(CryptoUtils.sha256Hex("innospots"));
    }

    @Test
    void encryptsAndDecryptsWithAesGcm() {
        String secret = "0123456789abcdef";
        String encrypted = CryptoUtils.encryptAesGcm("hello nexus", secret);

        assertThat(encrypted).isNotEqualTo("hello nexus");
        assertThat(CryptoUtils.decryptAesGcm(encrypted, secret)).isEqualTo("hello nexus");
    }

    @Test
    void encryptsPasswordWithSaltAndMatchesRawPassword() {
        String first = CryptoUtils.encryptPassword("nexus-secret");
        String second = CryptoUtils.encryptPassword("nexus-secret");

        assertThat(first).isNotBlank();
        assertThat(second).isNotBlank();
        assertThat(first).isNotEqualTo(second);
        assertThat(CryptoUtils.matchesPassword("nexus-secret", first)).isTrue();
        assertThat(CryptoUtils.matchesPassword("nexus-secret", second)).isTrue();
    }

    @Test
    void encryptsPasswordWithExternallyProvidedSalt() {
        String salt = CryptoUtils.generatePasswordSalt();

        String first = CryptoUtils.encryptPassword("nexus-secret", salt);
        String second = CryptoUtils.encryptPassword("nexus-secret", salt);

        assertThat(salt).isNotBlank();
        assertThat(first).isEqualTo(second);
        assertThat(CryptoUtils.matchesPassword("nexus-secret", first)).isTrue();
    }

    @Test
    void rejectsPasswordMismatch() {
        String encrypted = CryptoUtils.encryptPassword("nexus-secret");

        assertThat(CryptoUtils.matchesPassword("wrong-secret", encrypted)).isFalse();
    }

    @Test
    void generatesRsaKeyPairAndDecryptsPublicKeyEncryptedText() {
        CryptoUtils.AsymmetricKeyPair keyPair = CryptoUtils.generateRsaKeyPair();

        String encrypted = CryptoUtils.encryptRsa("hello asymmetric nexus", keyPair.publicKey());
        String decrypted = CryptoUtils.decryptRsa(encrypted, keyPair.privateKey());

        assertThat(keyPair.publicKey()).isNotBlank();
        assertThat(keyPair.privateKey()).isNotBlank();
        assertThat(encrypted).isNotEqualTo("hello asymmetric nexus");
        assertThat(decrypted).isEqualTo("hello asymmetric nexus");
    }

    @Test
    void generatesRsaKeyPairWithCustomKeySize() {
        CryptoUtils.AsymmetricKeyPair keyPair = CryptoUtils.generateRsaKeyPair(3072);

        String encrypted = CryptoUtils.encryptRsa("custom key size", keyPair.publicKey());

        assertThat(CryptoUtils.decryptRsa(encrypted, keyPair.privateKey())).isEqualTo("custom key size");
    }

    @Test
    void encryptsAndDecryptsTextLargerThanSingleRsaBlock() {
        CryptoUtils.AsymmetricKeyPair keyPair = CryptoUtils.generateRsaKeyPair();
        String plaintext = "nexus-".repeat(80);

        String encrypted = CryptoUtils.encryptRsa(plaintext, keyPair.publicKey());

        assertThat(CryptoUtils.decryptRsa(encrypted, keyPair.privateKey())).isEqualTo(plaintext);
    }
}

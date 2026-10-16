package com.innospots.nexus.base.util;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void decryptsWebCryptoOaepCiphertext() {
        String privateKey =
                "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCtwgwE8LlWbM8Cis4wr9HA6F/s7OYN/mjGIGGurviLTXzQEoJzPu3U+G6XeMwzWzRBX82Aizf5eQKg6I2ytP4VtpkMtcq6wjztks1pqBpBCwfdKrrjp08DpugTlV8bSjgNoyI7Dzu4cmavRotY3u6wHv2rUKmnOEoWisQ4H2y4FjKq3ZCHGHVv31SoUSI82vFmb3T+WOV/1nsmiS/QFA/PbWtXIQUxbmmfBoXOr/01MhCb5QmKcQFobjQ49H2a7S2yawdUICb3BRtuq3n+hErsoKfPeMqjlepBborfy0c2MaHw1tXrxMw1MvuTS/CWwiTffXn+bIRA+AhRJpm3O7PvAgMBAAECggEACbUQPCvBWaAKkKHLPdJqGCo4nGE/w621khwNA61SxWA224kChghl+8KaqxZckiHuRsNgv7sHYttCglV7qm2Mzw8Rzdv6JM+P3StDvpCy/B9ETomB7LtUIWgZtGkL6de4+fS7MiwlB1DyMP3ymwHEDqLXQ9Ll0inFjyzWztaH7wt23PO64MJNVFHQ/BhRLYqwDdQj+p7wvPAG0y5PHKL0Y4DexM9aUhK3rOxaHQ8yZt8bHl40iXU18yKr2KKbU+rRj6QVoMpMSpM1ZEP6vXC6rFPHqPFi/uxdS9cdWnMlgzx3VD1oc51HRxxuvTCZ2yqbunURncJs7I5IFAOIt9APgQKBgQDWNhkjReHWKaJsip4EGy391OLH8GzOBzKv8c7u22h+MqwEiUTw+Gd01848bd23p9EMl9chU6t0Ab0WEvMsChk49VU1J+lHj1WRZkWo6oYRaAaZHaCDbstor+PfXspXA8HaPA3jWretoEBcsK2f+z9AkddTxyeJ/X4TWuGjjRRohwKBgQDPp6ySmri2rWf6uI0V5A/wjJhL0ACNmQd4MlFOjByskcKMQcdBzdZbKoESFuT0ZNAwYb9kycLMBnBNpjTgHnmz3hDKj2mbAkpMKsuwiTdFeGdvsWYUeCUzPnK4cj0qTAdfnbTbAIW9IgULGdrK1/uarE+5ISv94J5dNxbDZuH7WQKBgFLQvW02ojYDCKl/+dFKlMBSoo579AS/IaiGCuOrbb2EZ56lV0L2NkElAbW5IAPV3GtK9MAMsfd6iJjVILBBAzlTF+siD/07e/BnorzIVqa6GfruSlUl63wArE96PS3g5PwzRYdFVkfI8bkkESwNnYmgGKhqEW4205w2AZAShfZXAoGAK1X1yN6rP8xuj79mCjR/MxdUTTYKd4KBMY9msnrBHS6KKFUt7Ui0vG/hYdIPVAWaAQ7VL4Z/ta5+T2YpUMBra6MjY/KL+oJ+2Ohxe9UXCNN42rFt+UWdldUpgd6tbx9Qxp6V+MJiJrdr0AZER+LNKlldhegrUx9GTXPKt1rIPHkCgYAYGGQohTYiH/++n5iqALeZ7Rbtq4wM7ikGiqGWuQnoqPj8yoeA6rPAZfNIceNj4zh5+r1NjrczE+A5U1XkF+RlKVaH3rKRvNplyRBaqW2K4rJOm/zIGpZs/4UoNYSMDA1qIhiG+Z8B55ygoQcztb2dvFBPicNrYkTwaPNCpWtONQ==";
        String webCryptoCipher =
                "CU7gDumUHL1K9dTmdUB9vL7DKUKqEgd85tQ/MVXPp/fzhjGcdJ/u3VMUOFLg1YH/Iuk5CL9zjrKUQiCijYp3iIhG381vZgiyJZGgfxwCMTEIwXu/7UPkCEIFDfybv/KwUxoaccFjSHO/N/zRoYSWkG717neQlqhuFuFpjokJkAgOSJrOry2O2/X3azTOhZHQpxkmvXsUm72//TGxhxyXn7C1tQqJnNJO+LdOfx5p2/5yZV3/R1s8+rB110Zef9wJbYglEV+1xbbyTz7H4TXoVDcNpfPixXpee7a4AwUSG780okVFexBh+HCK+WaMUIwO566yrOhB2Q7Wrp693jdTfw==";

        assertThat(CryptoUtils.decryptRsa(webCryptoCipher, privateKey)).isEqualTo("pactor123");
    }

    @Test
    void encryptsAndDecryptsTextLargerThanSingleRsaBlock() {
        CryptoUtils.AsymmetricKeyPair keyPair = CryptoUtils.generateRsaKeyPair();
        String plaintext = "nexus-".repeat(80);

        String encrypted = CryptoUtils.encryptRsa(plaintext, keyPair.publicKey());

        assertThat(CryptoUtils.decryptRsa(encrypted, keyPair.privateKey())).isEqualTo(plaintext);
    }

    @Test
    void rejectsNullPasswordWithInvalidParameter() {
        assertThatThrownBy(() -> CryptoUtils.encryptPassword(null))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
    }

    @Test
    void decryptFailureUsesCryptoStatusCode() {
        assertThatThrownBy(() -> CryptoUtils.decryptAesGcm("not-ciphertext", "0123456789abcdef"))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(NexusStatusCode.CRYPTO_FAILED.fullCode());
    }
}

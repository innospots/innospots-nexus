package com.innospots.nexus.base.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import cn.hutool.core.codec.Base64;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 探测浏览器 Web Crypto RSA-OAEP 与 Java 解密的 OAEP 参数组合。
 */
class CryptoUtilsWebCryptoInteropTest {

    private static final String SAMPLE_PRIVATE_KEY_BASE64 =
            "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCtwgwE8LlWbM8Cis4wr9HA6F/s7OZN/mjGIGGurviLTXzQEoJzPu3U+G6XeMwzWzRBX82Aizf5eQKg6I2ytP4VtpkMtcq6wjztks1pqBpBCwfdKrrjp08DpugTlV8bSjgNoyI7Dzu4cmavRotY3u6wHv2rUKmnOEoWisQ4H2y4FjKq3ZCHGHVv31SoUSI82vFmb3T+WOV/1nsmiS/QFA/PbWtXIQUxbmmfBoXOr/01MhCb5QmKcQFobjQ49H2a7S2yawdUICb3BRtuq3n+hErsoKfPeMqjlepBborfy0c2MaHw1tXrxMw1MvuTS/CWwiTffXn+bIRA+AhRJpm3O7PvAgMBAAECggEACbUQPCvBWaAKkKHLPdJqGCo4nGE/w621khwNA61SxWA224kChghl+8KaqxZckiHuRsNgv7sHYttCglV7qm2Mzw8Rzdv6JM+P3StDvpCy/B9ETomB7LtUIWgZtGkL6de4+fS7MiwlB1DyMP3ymwHEDqLXQ9Ll0inFjyzWztaH7wt23PO64MJNVFHQ/BhRLYqwDdQj+p7wvPAG0y5PHKL0Y4DexM9aUhK3rOxaHQ8yZt8bHl40iXU18yKr2KKbU+rRj6QVoMpMSpM1ZEP6vXC6rFPHqPFi/uxdS9cdWnMlgzx3VD1oc51HRxxuvTCZ2yqbunURncJs7I5IFAOIt9APgQKBgQDWNhkjReHWKaJsip4EGy391OLH8GzOBzKv8c7u22h+MqwEiUTw+Gd01848bd23p9EMl9chU6t0Ab0WEvMsChk49VU1J+lHj1WRZkWo6oYRaAaZHaCDbstor+PfXspXA8HaPA3jWretoEBcsK2f+z9AkddTxyeJ/X4TWuGjjRRohwKBgQDPp6ySmri2rWf6uI0V5A/wjJhL0ACNmQd4MlFOjByskcKMQcdBzdZbKoESFuT0ZNAwYb9kycLMBnBNpjTgHnmz3hDKj2mbAkpMKsuwiTdFeGdvsWYUeCUzPnK4cj0qTAdfnbTbAIW9IgULGdrK1/uarE+5ISv94J5dNxbDZuH7WQKBgFLQvW02ojYDCKl/+dFKlMBSoo579AS/IaiGCuOrbb2EZ56lV0L2NkElAbW5IAPV3GtK9MAMsfd6iJjVILBBAzlTF+siD/07e/BnorzIVqa6GfruSlUl63wArE96PS3g5PwzRYdFVkfI8bkkESwNnYmgGKhqEW4205w2AZAShfZXAoGAK1X1yN6rP8xuj79mCjR/MxdUTTYKd4KBMY9msnrBHS6KKFUt7Ui0vG/hYdIPVAWaAQ7VL4Z/ta5+T2YpUMBra6MjY/KL+oJ+2Ohxe9UXCNN42rFt+UWdldUpgd6tbx9Qxp6V+MJiJrdr0AZER+LNKlldhegrUx9GTXPKt1rIPHkCgYAYGGQohTYiH/++n5iqALeZ7Rbtq4wM7ikGiqGWuQnoqPj8yoeA6rPAZfNIceNj4zh5+r1NjrczE+A5U1XkF+RlKVaH3rKRvNplyRBaqW2K4rJOm/zIGpZs/4UoNYSMDA1qIhiG+Z8B55ygoQcztb2dvFBPicNrYkTwaPNCpWtONQ==";

    /** Node {@code webcrypto.subtle.encrypt} RSA-OAEP SHA-256, sample public key, plaintext {@code pactor123}. */
    private static final String WEB_CRYPTO_CIPHER_B64 =
            "CU7gDumUHL1K9dTmdUB9vL7DKUKqEgd85tQ/MVXPp/fzhjGcdJ/u3VMUOFLg1YH/Iuk5CL9zjrKUQiCijYp3iIhG381vZgiyJZGgfxwCMTEIwXu/7UPkCEIFDfybv/KwUxoaccFjSHO/N/zRoYSWkG717neQlqhuFuFpjokJkAgOSJrOry2O2/X3azTOhZHQpxkmvXsUm72//TGxhxyXn7C1tQqJnNJO+LdOfx5p2/5yZV3/R1s8+rB110Zef9wJbYglEV+1xbbyTz7H4TXoVDcNpfPixXpee7a4AwUSG780okVFexBh+HCK+WaMUIwO566yrOhB2Q7Wrp693jdTfw==";

    private static final String FORGE_OAEP_CIPHER_B64 =
            "SLLp5cOv9ZZoSpQHwLWQSX6/eMIxetKXnJBExBeIPNKN353iozfjWa66bo2Gy3wW/dAPETZk3n/tSsTnWdWkEaeSKu1p4sL6zJDQ1ifp7V68Dw3POPSesUI7qrDJILvoJjMsRucL+nsrHcmgctOGwGyppGqHjL++OCFVZ+QVPQt8jpfe5z8qf+CyphWatlqU9WTNEWb/vpnVokVipdAW4+BR6ffVq3mIlFrsSTdKcdu+A110ynsQ5A6FkIdYU4eMYrtEthG39w2ejxfmm0yLLlTgzAs+2325s3NUo35J53xxkEsqel/hnQewzF1kvM+zMjTV701ovsGzjgw8gPwyMg==";

    private static final String FORGE_PKCS1_CIPHER_B64 =
            "LZmVbk3f5dLqaWM9RnGa8Exv+LnCh03MCnAA7iW3An9zr6NO88mBxrzVsja7v9KXciONDS7mUPlM7mGdo/tVJGIU4DhqpsAkmgOelL13zvsKJgF08Dzed4WSWn7gXqehfvFxse0gRtXQie53GkTqmJfKeYEkLKRtSircbTRCJe4zi+B0pskzp9on4F5ZXjgsKroyAd0kBSfcuDYrMpBk1j5s6mbVa4ZjnVZBbwQ3Ga0LSyx/kiKl9XiIvM0D2TCT07NzcW765GJiheZ3XPEZqF9EQgWbJz/P3yzlGYvkysuQt7mO+J0VqFln4QhJxcIQBlsWirljPuzQo4wmaGqKoA==";

    @Test
    void javaPkcs1DecryptsForgePkcs1Ciphertext() throws Exception {
        PrivateKey privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(SAMPLE_PRIVATE_KEY_BASE64)));
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plain = cipher.doFinal(Base64.decode(FORGE_PKCS1_CIPHER_B64));
        assertThat(new String(plain, java.nio.charset.StandardCharsets.UTF_8)).isEqualTo("pactor123");
    }

    @Test
    void javaOaepWithExplicitSpecEncryptsInteropWithWebCrypto() throws Exception {
        java.security.PublicKey publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new java.security.spec.X509EncodedKeySpec(Base64.decode(
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArcIMBPC5VmzPAorOMK/RwOhf7OzmDf5oxiBhrq74i0180BKCcz7t1Phul3jMM1s0QV/NgIs3+XkCoOiNsrT+FbaZDLXKusI87ZLNaagaQQsH3Sq646dPA6boE5VfG0o4DaMiOw87uHJmr0aLWN7usB79q1CppzhKForEOB9suBYyqt2Qhxh1b99UqFEiPNrxZm90/ljlf9Z7Jokv0BQPz21rVyEFMW5pnwaFzq/9NTIQm+UJinEBaG40OPR9mu0tsmsHVCAm9wUbbqt5/oRK7KCnz3jKo5XqQW6K38tHNjGh8NbV68TMNTL7k0vwlsIk3315/myEQPgIUSaZtzuz7wIDAQAB")));
        OAEPParameterSpec oaep = new OAEPParameterSpec(
                "SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
        Cipher encrypt = Cipher.getInstance("RSA/ECB/OAEPPadding");
        encrypt.init(Cipher.ENCRYPT_MODE, publicKey, oaep);
        byte[] cipherText = encrypt.doFinal("pactor123".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        System.out.println("JAVA_OAEP_EXPLICIT_B64=" + Base64.encode(cipherText));

        PrivateKey privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(SAMPLE_PRIVATE_KEY_BASE64)));
        Cipher decrypt = Cipher.getInstance("RSA/ECB/OAEPPadding");
        decrypt.init(Cipher.DECRYPT_MODE, privateKey, oaep);
        assertThat(new String(decrypt.doFinal(cipherText), java.nio.charset.StandardCharsets.UTF_8))
                .isEqualTo("pactor123");
    }

    @Test
    void discoversOaepSpecForWebCryptoCiphertext() throws Exception {
        PrivateKey privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(SAMPLE_PRIVATE_KEY_BASE64)));
        byte[] cipherText = Base64.decode(WEB_CRYPTO_CIPHER_B64);

        String[][] candidates = {
                {"RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "SHA-256", "SHA-256"},
                {"RSA/ECB/OAEPPadding", "SHA-256", "SHA-256"},
                {"RSA/ECB/OAEPPadding", "SHA-256", "SHA-1"},
                {"RSA/ECB/OAEPWithSHA-1AndMGF1Padding", "SHA-1", "SHA-1"},
        };

        String matched = null;
        for (String[] candidate : candidates) {
            String transformation = candidate[0];
            String digest = candidate[1];
            String mgfDigest = candidate[2];
            try {
                Cipher cipher = Cipher.getInstance(transformation);
                OAEPParameterSpec spec = new OAEPParameterSpec(
                        digest,
                        "MGF1",
                        new MGF1ParameterSpec(mgfDigest),
                        PSource.PSpecified.DEFAULT);
                cipher.init(Cipher.DECRYPT_MODE, privateKey, spec);
                byte[] plain = cipher.doFinal(cipherText);
                if ("pactor123".equals(new String(plain, java.nio.charset.StandardCharsets.UTF_8))) {
                    matched = transformation + " digest=" + digest + " mgf=" + mgfDigest;
                    break;
                }
            } catch (Exception ignored) {
                // try next
            }
        }

        assertThat(matched)
                .as("Web Crypto interop OAEP parameters")
                .isNotNull();
        System.out.println("MATCHED_OAEP=" + matched);
    }
}

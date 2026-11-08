package com.innospots.nexus.base.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import cn.hutool.core.codec.Base64;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 校验浏览器 Web Crypto RSA-OAEP 与 Java 解密的参数组合互通。
 *
 * <p>三个密文均由<b>同一对</b>密钥生成（{@link #PRIVATE_KEY_BASE64} 与其公钥
 * 一一对应），分别代表：</p>
 * <ul>
 *   <li>{@code WEB_CRYPTO_OAEP_SHA256_CIPHER_B64}：浏览器 {@code subtle.encrypt}
 *       的 RSA-OAEP SHA-256（MGF1 同为 SHA-256）；</li>
 *   <li>{@code OAEP_SHA1_CIPHER_B64}：OAEP 但 MGF1 用 SHA-1；</li>
 *   <li>{@code PKCS1_CIPHER_B64}：非 OAEP 的 PKCS#1 v1.5。</li>
 * </ul>
 *
 * <p>明文统一为 {@code pactor123}。</p>
 */
class CryptoUtilsWebCryptoInteropTest {

    /** 测试私钥（PKCS#8 / DER / Base64），与 {@link #PUBLIC_KEY_BASE64} 为同一密钥对。 */
    private static final String PRIVATE_KEY_BASE64 =
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC1ibFZTKcz3omK9b3nr4vZZLypg2fF6+Owo+U+DlL3DJ0T3+VXkqNVaz4ZrmTioNerURGig2gJlnPkU10UHeJbCAohfMIhYSC6p+4jfemFwNCl7okR5AH/PILZ4ZobTN6vOzQQvmlbqUFRmqUoyR/RzqQkimYD3IlyFV1xBbgRLqe1qUUW6TwAlXD6sftiQpZKt1CI++kUX65+Gn8wCxNzLdnsAIn3QRTSNL66PSfBpNgGpzX1QSsxjAFMhZP7tToSkKri+TQ2akTQRPhZyzNB2MI6lo4vljoYVDIUX05DmFA7I2ORwhb1yKAZHTxZJuT4L7dzL82Pikr0Xc32+KdJAgMBAAECggEADxndP3oLghpxy1YKFjDhg6PYOEsiqaGH2xilIAqBvwSE+ANk0Oo1LUy6mZkK0hnSWvFHU+MVt091REHlgSADxXIHorW9sdhiKDjCK+BkWbCagqUTMKJsgpp6YYy9XKZCM2YrvHVUbxAnK3Y/ZSNG4vVrLNF47zsMEWoCz/0rbL1WW9tPl3IEQ1SaSERVkmbbPaI9/2i4nQj7Dhm8pTajoHLrlaE4OR577HB0eqTA7X+xwuZtM3A2bKv5vwfkeHcLxt9Qr1ZYCWVc9oQL7tzlp0ArkGUJBjZCYrQYNHJDY0aiBjpsq1Q8P3C6h0SgVfku0sI6wmv5kUDcHsh+DtJ3UQKBgQDvnvcqWhbU/co62B+PZdEDRNlYKMeSjWNehSLxTMZihGYsUCEI3J+juKo//fp3+kWS+4UjD8eutEZ1QNE1TsAHMPqnJ6biD1KVTssRR9e+TVH1cut5tmXh2cQKgA64w7tf1iIQxv1+gLq3kOiwMS6aZY9H8q0kTx3+foe+Qu6qmQKBgQDB8lqHgT59svP9VspJV9iHqlEDJssV2/tc9Wbtnu4teubhaFhn45dohY6bljUTutQYlkSi6aJqwxeFFXWlHTkW39IbZEJa5jHam4smiGroHqmX1kwLsrqggOSUGTVxo5LxHgAf+hnHzP21iQmR2d2F1Mtp1559i8t4ycrqidoAMQKBgQC52hMcvzk8RAfpch2wbCkcHvdwoFiCe7VZ52t9uq2YVdalghTNOcl8A8vQzF3LVstFwF24HxIil+KaKQbbENRx/3Q519BhP78u3Sk1owJbsL7j4Blxkp4c+XlLJ53DgtS+9WgAmUaeKpwnqnY/yjW6PRFPnZLbOZ4q+NqyIFna6QKBgA1aQiFSJJtC1sl6UDL8eUvdzfYJlbHLk2KzAoMZjgxTidG6/VspsCLiDw9nKABTvIZ00gTlUOlIxTXEZHUZrfSFNFHudSMM9iG2072zZqjUc5igiFvF0lN6jEIl+if4uwUeq6OH9H5s/oqhBKel+DpejCKdVawPE9MdEXtUQk4BAoGAKkO70kk8IpdhgESLHbWvTkOrDRvdMhS07rt0KC0r1tK3sr06S5aNI3HEVHuELx51elMgIJt9NCIg7PM0TfVbm1yNXqMPDH0g7k2t/qQ5xtps6ay7//FWAP5gPO3T3p9WwL0DODri45q7Oei//8mD4gJBgBIFBgnhwhC/8rLgx7A=";

    /** 与 {@link #PRIVATE_KEY_BASE64} 配对的公钥（X.509 SPKI / DER / Base64）。 */
    private static final String PUBLIC_KEY_BASE64 =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtYmxWUynM96JivW956+L2WS8qYNnxevjsKPlPg5S9wydE9/lV5KjVWs+Ga5k4qDXq1ERooNoCZZz5FNdFB3iWwgKIXzCIWEguqfuI33phcDQpe6JEeQB/zyC2eGaG0zerzs0EL5pW6lBUZqlKMkf0c6kJIpmA9yJchVdcQW4ES6ntalFFuk8AJVw+rH7YkKWSrdQiPvpFF+ufhp/MAsTcy3Z7ACJ90EU0jS+uj0nwaTYBqc19UErMYwBTIWT+7U6EpCq4vk0NmpE0ET4WcszQdjCOpaOL5Y6GFQyFF9OQ5hQOyNjkcIW9cigGR08WSbk+C+3cy/Nj4pK9F3N9vinSQIDAQAB";

    /** 明文统一值。 */
    private static final String PLAINTEXT = "pactor123";

    /** 浏览器 Web Crypto RSA-OAEP SHA-256 加密结果。 */
    private static final String WEB_CRYPTO_OAEP_SHA256_CIPHER_B64 =
            "gvMxNlCiKuNdIyYqLRiiVI+IkQY8BtsWjh0hFrZ78zmlGuWOVb7eRCOtXEYFchWSeDHDWmvxVpgMt2yKqhQDu+5i9Qo/ThldUvjDsf4+jQrtvUuXgYlEUESdoW2yI8zNl06lcb4o8rPLO1mI494J1RMol1btiTIMXO73JpNvPILGkcdwOcCSofYO87K4yhNxuL9XYAeNuPrm8HVvhcj0PsIV8QiDMl4WBZhR1RjyJFyE482T7+gt/9P3zkr8XJ8YM8Sfra8SCKsK1c23+NVBSYMngM77I9jsMk1th0/8NfkZyHMXnef+/5EmLBnuFzpjgc4URVaaUjGiOcd3o2I7Lw==";

    /** RSA-OAEP，但 MGF1 摘要为 SHA-1。 */
    private static final String OAEP_SHA1_CIPHER_B64 =
            "eqxP8IRRrOwNysR5o732MW6d7oV1z4uDn3sncvtgMHA9RupaZNlQuOfSZBMRwlrLpE2bsUI5Kijgk7jMU4G3fzQrUJol1RnGdGIdvBWD8GPAiHKfLYAGZ6fv8FBasV3KKD5PuiUXf9vaLV6yfZ8HRJOLN3594a0z0KTST2cT5XuRz3edo0MqM2mcf6XLnULulm9ADNOpzr5YVI2uIrW5q853/ZyiEbw2f2Whf8kwa8H2MxHWItWnod2bSagZSpHQ7rsXdutCIPWvCpcfPHNahdCO9BJfvteELK4nwRmARhC292ijA6qF8yuZBVk3dP3JdTKFWbFpuFBVJXngj1xZHQ==";

    /** 非 OAEP 的 PKCS#1 v1.5。 */
    private static final String PKCS1_CIPHER_B64 =
            "Z8TgKS3O8yYy/3jcDZEOzMAPAV/opG3P09QV5CZ2c43qojv49Z16lRUbblC8tY7r3SVbTf2ZaZCUkbvCcmf71+4KUhpdmlec20bK2akGfdvMVZcpGK87aEKiGXmlPlZEP6kZGah1UTQonWXsdNK2EWUCkE0xZbou8M8KW91fIq5mOQihGVsY1h5nh/LaKT6p+YGCzvTUTUNgIG7XTQw1rYzE/1sx7/1Ip+wQ85x0ayh3XWaYzja+5wK9nySq4SwqtlKEHqhBHCY3OwWeeD/9OsgZEaDoateHm+or6aiWlwBQmIeleQc82cpBdwzJJtazj0CxGKBvqWCXTKGKfxXcfA==";

    private static PrivateKey privateKey() throws Exception {
        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(PRIVATE_KEY_BASE64)));
    }

    private static PublicKey publicKey() throws Exception {
        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(Base64.decode(PUBLIC_KEY_BASE64)));
    }

    private static String decrypt(Cipher cipher, String cipherBase64) throws Exception {
        return new String(
                cipher.doFinal(Base64.decode(cipherBase64)),
                java.nio.charset.StandardCharsets.UTF_8);
    }

    /** 公钥与私钥必须成对，否则后续断言都会因 padding 失败。 */
    @Test
    void sampleKeyPairIsMatched() throws Exception {
        Cipher encrypt = Cipher.getInstance("RSA/ECB/OAEPPadding");
        encrypt.init(Cipher.ENCRYPT_MODE, publicKey(), oaepSpec("SHA-256", "SHA-256"));
        String cipherText = Base64.encode(encrypt.doFinal(
                PLAINTEXT.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        Cipher decrypt = Cipher.getInstance("RSA/ECB/OAEPPadding");
        decrypt.init(Cipher.DECRYPT_MODE, privateKey(), oaepSpec("SHA-256", "SHA-256"));
        assertThat(decrypt(decrypt, cipherText)).isEqualTo(PLAINTEXT);
    }

    /** 浏览器 Web Crypto 使用 OAEP SHA-256，Java 侧必须用同样的 MGF1 摘要。 */
    @Test
    void javaDecryptsWebCryptoOaepSha256Ciphertext() throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey(), oaepSpec("SHA-256", "SHA-256"));

        assertThat(decrypt(cipher, WEB_CRYPTO_OAEP_SHA256_CIPHER_B64)).isEqualTo(PLAINTEXT);
    }

    /** 主摘要与 MGF1 摘要同为 SHA-1 的 OAEP 密文同样可解。 */
    @Test
    void javaDecryptsOaepSha1Ciphertext() throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey(), oaepSpec("SHA-1", "SHA-1"));

        assertThat(decrypt(cipher, OAEP_SHA1_CIPHER_B64)).isEqualTo(PLAINTEXT);
    }

    /** 非 OAEP 的 PKCS#1 v1.5 密文走独立 transformation。 */
    @Test
    void javaDecryptsPkcs1Ciphertext() throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey());

        assertThat(decrypt(cipher, PKCS1_CIPHER_B64)).isEqualTo(PLAINTEXT);
    }

    /**
     * 前端 RSA-OAEP SHA-256 加密 → 后端解密，覆盖真实登录链路。
     */
    @Test
    void roundTripMatchesFrontendEncryptionPath() throws Exception {
        Cipher encrypt = Cipher.getInstance("RSA/ECB/OAEPPadding");
        encrypt.init(Cipher.ENCRYPT_MODE, publicKey(), oaepSpec("SHA-256", "SHA-256"));
        String cipherText = Base64.encode(encrypt.doFinal(
                PLAINTEXT.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        Cipher decrypt = Cipher.getInstance("RSA/ECB/OAEPPadding");
        decrypt.init(Cipher.DECRYPT_MODE, privateKey(), oaepSpec("SHA-256", "SHA-256"));
        assertThat(decrypt(decrypt, cipherText)).isEqualTo(PLAINTEXT);
    }

    private static OAEPParameterSpec oaepSpec(String digest, String mgfDigest) {
        return new OAEPParameterSpec(
                digest, "MGF1", new MGF1ParameterSpec(mgfDigest), PSource.PSpecified.DEFAULT);
    }
}

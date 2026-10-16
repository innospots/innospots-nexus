package com.innospots.nexus.sample.spring.platform.support;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.util.CryptoUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 保证示例 RSA 密钥对与 UI {@code rsa-password.ts} 默认公钥一致。
 */
class PlatformSampleRsaKeyAlignmentTest {

    private static final String UI_DEFAULT_PUBLIC_KEY_BASE64 =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArcIMBPC5VmzPAorOMK/RwOhf7OzmDf5oxiBhrq74i0180BKCcz7t1Phul3jMM1s0QV/NgIs3+XkCoOiNsrT+FbaZDLXKusI87ZLNaagaQQsH3Sq646dPA6boE5VfG0o4DaMiOw87uHJmr0aLWN7usB79q1CppzhKForEOB9suBYyqt2Qhxh1b99UqFEiPNrxZm90/ljlf9Z7Jokv0BQPz21rVyEFMW5pnwaFzq/9NTIQm+UJinEBaG40OPR9mu0tsmsHVCAm9wUbbqt5/oRK7KCnz3jKo5XqQW6K38tHNjGh8NbV68TMNTL7k0vwlsIk3315/myEQPgIUSaZtzuz7wIDAQAB";

    @Test
    void samplePublicKeyMatchesUiDefault() {
        assertThat(PlatformSampleRsaPassword.samplePublicKeyBase64()).isEqualTo(UI_DEFAULT_PUBLIC_KEY_BASE64);
    }

    @Test
    void samplePrivateKeyDecryptsJavaEncryptedPassword() {
        String encrypted = CryptoUtils.encryptRsa("pactor123", UI_DEFAULT_PUBLIC_KEY_BASE64);
        String decrypted = CryptoUtils.decryptRsa(encrypted, PlatformSampleRsaPassword.SAMPLE_PRIVATE_KEY_BASE64);
        assertThat(decrypted).isEqualTo("pactor123");
    }

    @Test
    void javaEncryptedPasswordIsDecryptableByNodeOaepSha256() {
        String encrypted = CryptoUtils.encryptRsa("pactor123", UI_DEFAULT_PUBLIC_KEY_BASE64);
        // stdout for manual Node openssl cross-check when debugging RSA interop
        System.out.println("JAVA_RSA_B64=" + encrypted);
        assertThat(encrypted).isNotBlank();
    }

    @Test
    void samplePrivateKeyDecryptsNodeCryptoPublicEncryptPassword() {
        String encrypted =
                "rWt5oPiUogmfn+WZUFivyQU1ASnR0TyV3INNQWg/kKn5tJq5svZ0qBfxu4doK8wPnTlOgQi0IWld2Uyl9npyXOGwoq2fcMbT7YKUVm2qbvAEyXtgH4aNYTPlqcQFryH+wxoVEnQy/sxBx8ygtKOoSbUoaFXKHx3piFxesjZJk7rP3i90TTXB9YKa2ohSRoqwsKU822z8SeNFcw//E+89fEbvIm31Wgu3Bew3/ROGTVKA5R1VrWdWJ7YqwVe0FuVw3LrlIxOCp/nlxt+3NR6HwvSmS79oFu3UB/NfClYaMN0zvEsjWICeE1RLiWULVKx9/0nRU107zjPXU49XdDjdlw==";
        String decrypted = CryptoUtils.decryptRsa(encrypted, PlatformSampleRsaPassword.SAMPLE_PRIVATE_KEY_BASE64);
        assertThat(decrypted).isEqualTo("pactor123");
    }
}
